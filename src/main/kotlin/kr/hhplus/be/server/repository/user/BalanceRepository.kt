package kr.hhplus.be.server.repository.user

import kr.hhplus.be.server.domain.user.Balance

interface BalanceRepository {
    fun findByAccountId(accountId: Int): Balance?
    fun update(balance: Balance): Balance
    fun save(balance: Balance): Balance
    fun delete(balance: Balance)
}