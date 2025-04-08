package kr.hhplus.be.server.domain.user.repository

import kr.hhplus.be.server.domain.user.model.Account

interface AccountRepository {
    fun save(account: Account): Account
    fun findByUserId(userId: Long): Account?
    fun update(id: Long, amount: Double): Account
    fun delete(id: Long)
}