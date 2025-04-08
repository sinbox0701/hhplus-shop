package kr.hhplus.be.server.repository.user

import kr.hhplus.be.server.domain.user.Account

interface AccountRepository {
    fun findById(accountId: Long): Account?
    fun login(loginId: String, password: String): Account?
    fun save(account: Account): Account
    fun update(account: Account): Account
    fun delete(account: Account)
}