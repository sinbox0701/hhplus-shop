package kr.hhplus.be.server.domain.user.service

import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.user.repository.AccountRepository
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class AccountService(
    private val accountRepository: AccountRepository
) {
    fun create(userId: Long, initialAmount: Double = Account.MIN_BALANCE): Account {
        val accountId = System.currentTimeMillis() // 현재 시간을 밀리초 단위로 사용하여 고유한 ID 생성 (추후 DB 시퀀스 사용 예정)
        val account = Account.create(accountId, userId, initialAmount)
        return accountRepository.save(account)
    }

    fun findByUserId(userId: Long): Account {
        return accountRepository.findByUserId(userId) ?: throw IllegalArgumentException("계좌를 찾을 수 없습니다: $userId")
    }

    fun findById(id: Long): Account? {
        return accountRepository.findById(id)
    }
    
    fun charge(id: Long, amount: Double): Account {
        val account = findById(id) ?: throw IllegalArgumentException("계좌를 찾을 수 없습니다: $id")
        account.charge(amount)
        return accountRepository.update(id, account.amount)
    }
    
    fun withdraw(id: Long, amount: Double): Account {
        val account = findById(id) ?: throw IllegalArgumentException("계좌를 찾을 수 없습니다: $id")
        account.withdraw(amount)
        return accountRepository.update(id, account.amount)
    }
    
    fun delete(id: Long) {
        accountRepository.delete(id)
    }
    
}