package kr.hhplus.be.server.service.user

import kr.hhplus.be.server.domain.user.Balance
import kr.hhplus.be.server.repository.user.BalanceRepository
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
        
        // Use domain method for validation and update
        balance.charge(amount)
        return balanceRepository.update(balance)
    }

    fun withdraw(accountId: Int, amount: BigDecimal): Balance {
        val balance = balanceRepository.findByAccountId(accountId)
            ?: throw IllegalArgumentException("Balance not found for account: $accountId")
        
        // Use domain method for validation and update
        balance.withdraw(amount)
        return balanceRepository.update(balance)
    }

    fun deleteByAccountId(accountId: Int){
        val balance = getByAccountId(accountId)
        balanceRepository.delete(balance)
    }
}