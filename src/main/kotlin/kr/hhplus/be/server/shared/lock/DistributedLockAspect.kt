package kr.hhplus.be.server.shared.lock

import kr.hhplus.be.server.shared.lock.DistributedLock
import kr.hhplus.be.server.shared.lock.DistributedLockManager
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.stereotype.Component

/**
 * 분산 락 어노테이션(@DistributedLock, @CompositeLock)을 처리하는 AOP Aspect
 * 트랜잭션 시작 전에 락을 획득하기 위해 높은 우선순위로 설정
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class DistributedLockAspect(private val lockManager: DistributedLockManager) {
    
    private val parser: ExpressionParser = SpelExpressionParser()
    
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
     * 표현식에서 리소스 ID 값을 추출합니다.
     * 단순한 파라미터 이름이나 중첩된 필드 접근(criteria.userId 등)을 처리합니다.
     *
     * @param expression 리소스 ID 표현식
     * @param joinPoint 메서드 실행 지점
     * @return 추출된 리소스 ID 문자열
     */
    private fun resolveResourceId(expression: String, joinPoint: ProceedingJoinPoint): String {
        val methodSignature = joinPoint.signature as MethodSignature
        val parameterNames = methodSignature.parameterNames
        val args = joinPoint.args
        
        // 단순 파라미터 이름인 경우 직접 매핑
        val parameterIndex = parameterNames.indexOf(expression)
        if (parameterIndex >= 0 && parameterIndex < args.size) {
            return args[parameterIndex].toString()
        }
        
        // SPEL 표현식으로 처리
        val context = StandardEvaluationContext()
        
        // 파라미터를 컨텍스트에 등록
        for (i in parameterNames.indices) {
            if (i < args.size) {
                context.setVariable(parameterNames[i], args[i])
            }
        }
        
        // 표현식 평가
        val resourceId = parser.parseExpression("#${expression}").getValue(context)
        
        return resourceId?.toString() ?: throw IllegalArgumentException(
            "리소스 ID 표현식이 null을 반환했습니다: $expression"
        )
    }
} 