package kr.hhplus.be.server.shared.lock

import kr.hhplus.be.server.shared.lock.DistributedLock
import kr.hhplus.be.server.shared.lock.DistributedLockManager
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * 분산 락 어노테이션(@DistributedLock, @CompositeLock)을 처리하는 AOP Aspect
 * 트랜잭션 시작 전에 락을 획득하기 위해 높은 우선순위로 설정
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class DistributedLockAspect(private val lockManager: DistributedLockManager) {
    
    /**
     * @DistributedLock 어노테이션이 적용된 메서드 실행을 가로채서
     * 락을 획득한 후 메서드를 실행하고 락을 해제합니다.
     *
     * @param joinPoint 메서드 실행 지점
     * @return 메서드 실행 결과
     */
    @Around("@annotation(kr.hhplus.be.server.shared.lock.DistributedLock)")
    fun executeWithLock(joinPoint: ProceedingJoinPoint): Any {
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method
        val distributedLock = method.getAnnotation(DistributedLock::class.java)

        val resourceId = resolveResourceId(distributedLock.resourceIdExpression, joinPoint)

        val lockKey = LockKeyGenerator.generate(
            distributedLock.domain,
            distributedLock.resourceType,
            resourceId
        )
        
        return lockManager.executeWithLock(
            key = lockKey,
            timeout = distributedLock.timeout,
            unit = distributedLock.timeUnit
        ) {
            joinPoint.proceed()
        }
    }
    
    /**
     * @CompositeLock 어노테이션이 적용된 메서드 실행을 가로채서
     * 여러 락을 순서대로 획득한 후 메서드를 실행하고 락을 해제합니다.
     *
     * @param joinPoint 메서드 실행 지점
     * @return 메서드 실행 결과
     */
    @Around("@annotation(kr.hhplus.be.server.shared.lock.CompositeLock)")
    fun executeWithCompositeLock(joinPoint: ProceedingJoinPoint): Any {
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method
        val compositeLock = method.getAnnotation(CompositeLock::class.java)


        // 각 락에 대한 키와 타임아웃 준비
        val lockKeys = compositeLock.locks.map { lock ->
            val resourceId = resolveResourceId(lock.resourceIdExpression, joinPoint)
            LockKeyGenerator.generate(lock.domain, lock.resourceType, resourceId)
        }
        
        // 락 획득 순서 정렬 (필요시)
        val orderedLockKeys = if (compositeLock.ordered) {
            DistributedLockUtils.sortLockKeys(lockKeys)
        } else {
            lockKeys
        }
        
        // 타임아웃 설정 (첫 번째 락은 EXTENDED_TIMEOUT, 나머지는 DEFAULT_TIMEOUT)
        val timeouts = if (orderedLockKeys.isNotEmpty()) {
            listOf(LockKeyConstants.EXTENDED_TIMEOUT) + 
                List(orderedLockKeys.size - 1) { LockKeyConstants.DEFAULT_TIMEOUT }
        } else {
            emptyList()
        }
        
        // 모든 락을 순서대로 획득하고 메서드 실행
        return DistributedLockUtils.withOrderedLocks(
            lockManager = lockManager,
            keys = orderedLockKeys,
            timeouts = timeouts,
            action = {
                joinPoint.proceed()
            }
        )
    }
    
    /**
     * 표현식에서 리소스 ID를 추출합니다.
     * 예: "criteria.userId"와 같은 표현식에서 실제 userId 값을 추출
     *
     * @param expression 표현식
     * @param joinPoint 메서드 실행 지점
     * @return 추출된 리소스 ID 문자열
     */
    private fun resolveResourceId(expression: String, joinPoint: ProceedingJoinPoint): String {
        val methodSignature = joinPoint.signature as MethodSignature
        val parameterNames = methodSignature.parameterNames
        val args = joinPoint.args
        
        // 표현식에서 파라미터 경로 파싱 (예: "criteria.userId")
        val parts = expression.split(".")
        val rootParamName = parts[0]
        
        // 파라미터 인덱스 찾기
        val paramIndex = parameterNames.indexOf(rootParamName)
        if (paramIndex == -1) {
            throw IllegalArgumentException("파라미터 이름 '$rootParamName'을 찾을 수 없습니다: $expression")
        }
        
        // 파라미터 값 가져오기
        var value: Any? = args[paramIndex]
        
        // 중첩 필드 처리
        for (i in 1 until parts.size) {
            if (value == null) break
            
            val fieldName = parts[i]
            val getterMethod = value::class.java.methods.find { 
                it.name == "get${fieldName.capitalize()}" || 
                it.name == fieldName || 
                it.name == "is${fieldName.capitalize()}"
            }
            
            if (getterMethod == null) {
                throw IllegalArgumentException("필드 '$fieldName'을 클래스 '${value::class.java.simpleName}'에서 찾을 수 없습니다: $expression")
            }
            
            value = getterMethod.invoke(value)
        }
        
        return value?.toString() ?: throw IllegalArgumentException("표현식 '$expression'에서 null 값이 추출되었습니다")
    }
} 