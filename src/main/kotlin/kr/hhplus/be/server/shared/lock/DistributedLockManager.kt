package kr.hhplus.be.server.shared.lock

import java.util.concurrent.TimeUnit

/**
 * 분산 락 관리자 인터페이스
 * 다양한 분산 락 구현체(Redisson, Zookeeper 등)에 대한 추상화 제공
 */
interface DistributedLockManager {
    /**
     * 지정된 키에 대한 분산 락을 획득하고 작업을 실행한 후 락을 해제합니다.
     *
     * @param key 락을 획득할 리소스 키
     * @param timeout 락 획득 대기 시간
     * @param unit 시간 단위
     * @param supplier 락을 획득한 후 실행할 작업
     * @return 작업 실행 결과
     * @throws LockAcquisitionException 락 획득 실패 시 발생
     */
    fun <T> executeWithLock(
        key: String,
        timeout: Long = 10,
        unit: TimeUnit = TimeUnit.SECONDS,
        supplier: () -> T
    ): T
    
    /**
     * 지정된 키에 대한 분산 락을 획득합니다.
     *
     * @param key 락을 획득할 리소스 키
     * @param timeout 락 획득 대기 시간
     * @param unit 시간 단위
     * @return 락 획득 성공 여부
     */
    fun tryLock(key: String, timeout: Long = 10, unit: TimeUnit = TimeUnit.SECONDS): Boolean
    
    /**
     * 지정된 키에 대한 분산 락을 해제합니다.
     *
     * @param key 해제할 락의 리소스 키
     */
    fun unlock(key: String)
} 