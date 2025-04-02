package kr.hhplus.be.server.repository.balance

import kr.hhplus.be.server.domain.balance.Balance

interface BalanceRepository {
    fun findByAccountId(accountId: Int): Balance?
    fun save(balance: Balance): Balance
}