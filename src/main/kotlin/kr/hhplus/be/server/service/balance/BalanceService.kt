package kr.hhplus.be.server.service.balance

import kr.hhplus.be.server.domain.balance.Balance
import kr.hhplus.be.server.repository.balance.BalanceRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class BalanceService(
    private val balanceRepository: BalanceRepository
) {
    fun create(balanceId: Int, accountId: Int): Balance {
        val balance = Balance.create(balanceId, accountId)
        return balanceRepository.save(balance.balanceId, balance.accountId)
    }

    fun getByAccountId(accountId: Int): Balance {
        return balanceRepository.findByAccountId(accountId)
            ?: throw IllegalArgumentException("Balance not found for account: $accountId")
    }

    fun charge(accountId: Int, amount: BigDecimal): Balance {
        val balance = balanceRepository.findByAccountId(accountId)
            ?: throw IllegalArgumentException("Balance not found for account: $accountId")
        balance.amount = balance.amount.add(amount)
        return balanceRepository.saveAmount(balance)
    }

    fun withdraw(accountId: Int, amount: BigDecimal): Balance {
        val balance = balanceRepository.findByAccountId(accountId)
            ?: throw IllegalArgumentException("Balance not found for account: $accountId")
        if (balance.amount < amount) {
            throw IllegalArgumentException("Insufficient funds")
        }
        balance.amount = balance.amount.subtract(amount)
        return balanceRepository.saveAmount(balance)
    }
}