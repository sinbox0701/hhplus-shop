package kr.hhplus.be.server.infrastructure.redis

import kr.hhplus.be.server.shared.lock.DistributedLockManager
import kr.hhplus.be.server.shared.lock.LockAcquisitionException
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * Redisson 기반 분산 락 관리자 구현체
 */
@Component
class RedisDistributedLockManager(private val redissonClient: RedissonClient) : DistributedLockManager {
    
    /**
     * Redisson의 분산 락을 사용하여 작업을 안전하게 실행합니다.
     * 락 획득에 실패하면 LockAcquisitionException을 발생시킵니다.
     *
     * @param key 락을 획득할 리소스 키
     * @param timeout 락 획득 대기 시간
     * @param unit 시간 단위
     * @param supplier 락을 획득한 후 실행할 작업
     * @return 작업 실행 결과
     * @throws LockAcquisitionException 락 획득 실패 시 발생
     */
    override fun <T> executeWithLock(
        key: String,
        timeout: Long,
        unit: TimeUnit,
        supplier: () -> T
    ): T {
        val lock = redissonClient.getLock("lock:$key")
        val locked = lock.tryLock(timeout, unit)
        
        if (!locked) {
            throw LockAcquisitionException("락 획득 실패: $key")
        }
        
        try {
            return supplier()
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }
    
    /**
     * 지정된 키에 대한 분산 락을 획득합니다.
     *
     * @param key 락을 획득할 리소스 키
     * @param timeout 락 획득 대기 시간
     * @param unit 시간 단위
     * @return 락 획득 성공 여부
     */
    override fun tryLock(key: String, timeout: Long, unit: TimeUnit): Boolean {
        val lock = redissonClient.getLock("lock:$key")
        return lock.tryLock(timeout, unit)
    }
    
    /**
     * 지정된 키에 대한 분산 락을 해제합니다.
     *
     * @param key 해제할 락의 리소스 키
     */
    override fun unlock(key: String) {
        val lock = redissonClient.getLock("lock:$key")
        if (lock.isHeldByCurrentThread) {
            lock.unlock()
        }
    }
} 