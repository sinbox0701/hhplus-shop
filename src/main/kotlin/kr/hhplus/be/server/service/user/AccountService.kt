package kr.hhplus.be.server.service.user

import kr.hhplus.be.server.domain.user.Account
import kr.hhplus.be.server.repository.user.AccountRepository
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val accountRepository: AccountRepository
) {
    fun save(account: Account): Account {
        val newUser = Account.create(account.accountId, account.name, account.email, account.loginId, account.password)
        return accountRepository.save(newUser)
    }

    fun getById(accountId: Int): Account {
        return accountRepository.findById(accountId)
            ?: throw IllegalArgumentException("Account not found for accountId: $accountId")
    }

    fun verify(loginId: String, password: String): Account {
        return accountRepository.login(loginId, password)
            ?: throw IllegalArgumentException("Wrong Id or Password")
    }

    fun update(accountId: Int,  name: String?, email: String?, loginId: String?, password: String?): Account {
        val account = getById(accountId)
        account.update(name, email, loginId, password)
        return accountRepository.update(account)
    }

    fun delete(accountId: Int) {
        val account = getById(accountId)
        accountRepository.delete(account)
    }
}