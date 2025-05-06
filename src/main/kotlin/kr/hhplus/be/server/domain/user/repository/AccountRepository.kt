package kr.hhplus.be.server.domain.user.repository

import kr.hhplus.be.server.domain.user.model.Account
import org.springframework.dao.OptimisticLockingFailureException

interface AccountRepository {
    fun save(account: Account): Account
    fun findByUserId(userId: Long): Account?
    fun findById(id: Long): Account?
    fun update(id: Long, amount: Double): Account
    fun delete(id: Long)
    
    /**
     * 낙관적 락을 활용한 계정 업데이트
     * @throws OptimisticLockingFailureException 동시성 충돌이 발생했을 경우
     */
    fun updateWithOptimisticLock(account: Account): Account
}