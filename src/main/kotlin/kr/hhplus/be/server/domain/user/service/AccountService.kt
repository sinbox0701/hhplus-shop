package kr.hhplus.be.server.domain.user.service

import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.user.repository.AccountRepository
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountService(
    private val accountRepository: AccountRepository
) {
    fun create(command: AccountCommand.CreateAccountCommand): Account {
        val account = Account.create(command.userId, command.amount)
        return accountRepository.save(account)
    }

    fun findByUserId(userId: Long): Account {
        return accountRepository.findByUserId(userId) ?: throw IllegalArgumentException("계좌를 찾을 수 없습니다: $userId")
    }

    fun findById(id: Long): Account {
        return accountRepository.findById(id) ?: throw IllegalArgumentException("계좌를 찾을 수 없습니다: $id")
    }
    
    fun charge(command: AccountCommand.UpdateAccountCommand): Account {
        val account = findById(command.id)
        account.charge(command.amount)
        return accountRepository.update(command.id, account.amount)
    }
    
    fun withdraw(command: AccountCommand.UpdateAccountCommand): Account {
        val account = findById(command.id)
        account.withdraw(command.amount)
        return accountRepository.update(command.id, account.amount)
    }
    
    fun delete(id: Long) {
        accountRepository.delete(id)
    }
    
    /**
     * 낙관적 락을 사용하여 포인트 충전
     * 충돌 시 최대 2번까지 재시도
     */
    @Transactional
    fun chargeWithOptimisticLock(command: AccountCommand.UpdateAccountCommand, maxRetries: Int = 2): Account {
        var retry = 0
        
        while (true) {
            try {
                val account = findById(command.id)
                val updatedAccount = account.charge(command.amount)
                return accountRepository.updateWithOptimisticLock(updatedAccount)
            } catch (e: OptimisticLockingFailureException) {
                if (retry >= maxRetries) {
                    throw e
                }
                retry++
            }
        }
    }
    
    /**
     * 낙관적 락을 사용하여 포인트 출금
     * 충돌 시 최대 2번까지 재시도
     */
    @Transactional
    fun withdrawWithOptimisticLock(command: AccountCommand.UpdateAccountCommand, maxRetries: Int = 2): Account {
        var retry = 0
        
        while (true) {
            try {
                val account = findById(command.id)
                val updatedAccount = account.withdraw(command.amount)
                return accountRepository.updateWithOptimisticLock(updatedAccount)
            } catch (e: OptimisticLockingFailureException) {
                if (retry >= maxRetries) {
                    throw e
                }
                retry++
            }
        }
    }
}