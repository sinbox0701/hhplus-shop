package kr.hhplus.be.server.domain.user

import java.math.BigDecimal

data class Balance private constructor(
    val balanceId: Int,
    val accountId: Int,
    var amount: BigDecimal
) {
    companion object {
        private val MIN_BALANCE: BigDecimal = BigDecimal.ZERO
        private val MAX_BALANCE: BigDecimal = BigDecimal("100000000") // 100,000,000 원
        private val MAX_TRANSACTION_AMOUNT: BigDecimal = BigDecimal("10000000") // 10,000,000 원

        fun create(balanceId: Int, accountId: Int, initialAmount: BigDecimal = MIN_BALANCE): Balance {
            require(initialAmount >= MIN_BALANCE && initialAmount <= MAX_BALANCE) {
                "Initial amount must be between $MIN_BALANCE and $MAX_BALANCE"
            }
            return Balance(balanceId, accountId, initialAmount)
        }
    }

    fun charge(amount: BigDecimal): Balance {
        require(amount > BigDecimal.ZERO) { "Charge amount must be positive" }
        require(amount <= MAX_TRANSACTION_AMOUNT) {
            "Charge amount cannot exceed $MAX_TRANSACTION_AMOUNT"
        }
        val newAmount = this.amount + amount
        require(newAmount <= MAX_BALANCE) {
            "Resulting balance cannot exceed $MAX_BALANCE"
        }
        this.amount = newAmount
        return this
    }

    fun withdraw(amount: BigDecimal): Balance {
        require(amount > BigDecimal.ZERO) { "Withdrawal amount must be positive" }
        require(amount <= MAX_TRANSACTION_AMOUNT) {
            "Withdrawal amount cannot exceed $MAX_TRANSACTION_AMOUNT"
        }
        require(this.amount >= amount) { "Insufficient funds" }
        val newAmount = this.amount - amount
        require(newAmount >= MIN_BALANCE) { "Resulting balance cannot be negative" }
        this.amount = newAmount
        return this
    }
}