package kr.hhplus.be.server.infrastructure.user

import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.user.repository.AccountRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
class AccountRepositoryImpl(
    private val jpaAccountRepository: JpaAccountRepository
) : AccountRepository {
    
    override fun save(account: Account): Account {
        val accountEntity = AccountEntity.fromAccount(account)
        val savedEntity = jpaAccountRepository.save(accountEntity)
        return savedEntity.toAccount()
    }
    
    override fun findByUserId(userId: Long): Account? {
        return jpaAccountRepository.findByUserId(userId)?.toAccount()
    }
    
    override fun findById(id: Long): Account? {
        return jpaAccountRepository.findByIdOrNull(id)?.toAccount()
    }
    
    override fun update(id: Long, amount: Double): Account {
        val accountEntity = jpaAccountRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("계좌를 찾을 수 없습니다: $id")
        
        // 불변 객체이므로 새 엔티티를 생성하여 저장
        val updatedEntity = AccountEntity(
            id = accountEntity.id,
            userId = accountEntity.userId,
            amount = amount,
            createdAt = accountEntity.createdAt,
            updatedAt = LocalDateTime.now(),
            version = accountEntity.version
        )
        
        val savedEntity = jpaAccountRepository.save(updatedEntity)
        return savedEntity.toAccount()
    }
    
    @Transactional
    override fun updateWithOptimisticLock(account: Account): Account {
        // 이미 도메인 모델에서 로직 처리가 완료되었으므로, 해당 객체를 그대로 엔티티로 변환하여 저장
        val accountEntity = AccountEntity.fromAccount(account)
        val savedEntity = jpaAccountRepository.save(accountEntity)
        return savedEntity.toAccount()
    }
    
    override fun delete(id: Long) {
        jpaAccountRepository.deleteById(id)
    }
} 