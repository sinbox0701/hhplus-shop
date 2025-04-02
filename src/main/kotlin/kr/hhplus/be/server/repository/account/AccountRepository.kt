package kr.hhplus.be.server.repository.account

import kr.hhplus.be.server.domain.account.Account

interface AccountRepository {
    fun findById(accountId: Int): Account?
    fun login(loginId: String, password: String): Account?
    fun save(account: Account): Account
}