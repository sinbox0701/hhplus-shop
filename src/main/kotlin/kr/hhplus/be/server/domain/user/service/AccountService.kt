package kr.hhplus.be.server.domain.user.service

import kr.hhplus.be.server.domain.user.service.AccountCommand
import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.user.repository.AccountRepository
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class AccountService(
    private val accountRepository: AccountRepository
) {
    fun create(command: AccountCommand.CreateAccountCommand): Account {
        val account = Account.create(command.user, command.amount)
        return accountRepository.save(account)
    }

    fun findByUserId(userId: Long): Account {
        return accountRepository.findByUserId(userId) ?: throw IllegalArgumentException("계좌를 찾을 수 없습니다: $userId")
    }

    fun findById(id: Long): Account? {
        return accountRepository.findById(id)
    }
    
    fun charge(command: AccountCommand.UpdateAccountCommand): Account {
        val account = findById(command.id) ?: throw IllegalArgumentException("계좌를 찾을 수 없습니다: ${command.id}")
        account.charge(command.amount)
        return accountRepository.update(command.id, account.amount)
    }
    
    fun withdraw(command: AccountCommand.UpdateAccountCommand): Account {
        val account = findById(command.id) ?: throw IllegalArgumentException("계좌를 찾을 수 없습니다: ${command.id}")
        account.withdraw(command.amount)
        return accountRepository.update(command.id, account.amount)
    }
    
    fun delete(id: Long) {
        accountRepository.delete(id)
    }
    
}