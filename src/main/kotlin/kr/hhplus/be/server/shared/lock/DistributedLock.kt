package kr.hhplus.be.server.shared.lock



import java.util.concurrent.TimeUnit

/**
 * 분산 락 어노테이션
 * 메서드에 적용하여 해당 메서드 실행 시 분산 락을 획득하도록 합니다.
 * LockKeyGenerator 및 LockKeyConstants와 통합된 방식으로 락 키를 생성합니다.
 *
 * @param domain 도메인 프리픽스 (LockKeyConstants 상수 사용)
 * @param resourceType 리소스 타입 (LockKeyConstants 상수 사용)
 * @param resourceIdExpression 리소스 ID를 추출할 표현식 (예: "criteria.userId")
 * @param timeout 락 획득 대기 시간
 * @param timeUnit 시간 단위
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val domain: String,
    val resourceType: String,
    val resourceIdExpression: String,
    val timeout: Long = LockKeyConstants.DEFAULT_TIMEOUT,
    val timeUnit: TimeUnit = TimeUnit.SECONDS
)

/**
 * 여러 분산 락을 복합적으로 적용하기 위한 어노테이션
 * 메서드에 적용하여 여러 락을 순서대로 획득하도록 합니다.
 *
 * @param locks 획득할 락들의 배열
 * @param ordered 락 획득 시 정렬 여부 (데드락 방지를 위해 기본값 true)
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CompositeLock(
    val locks: Array<DistributedLock>,
    val ordered: Boolean = true
) 