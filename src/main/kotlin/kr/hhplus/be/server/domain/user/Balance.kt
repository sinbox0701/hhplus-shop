package kr.hhplus.be.server.domain.user

import java.time.LocalDateTime

data class Balance private constructor(
    val id: Long,
    val accountId: Long,
    var amount: Double,
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
) {
    companion object {
        private val MIN_BALANCE: Double = 0.0
        private val MAX_BALANCE: Double = 100000000.0 // 100,000,000 원
        private val MAX_TRANSACTION_AMOUNT: Double = 10000000.0 // 10,000,000 원

        fun create(id: Long, accountId: Long, initialAmount: Double = MIN_BALANCE): Balance {
            require(initialAmount >= MIN_BALANCE && initialAmount <= MAX_BALANCE) {
                "Initial amount must be between $MIN_BALANCE and $MAX_BALANCE"
            }
            return Balance(id, accountId, initialAmount, LocalDateTime.now(), LocalDateTime.now())
        }
    }

    fun charge(amount: Double): Balance {
        require(amount > 0) { "Charge amount must be positive" }
        require(amount <= MAX_TRANSACTION_AMOUNT) {
            "Charge amount cannot exceed $MAX_TRANSACTION_AMOUNT"
        }
        val newAmount = this.amount + amount
        require(newAmount <= MAX_BALANCE) {
            "Resulting balance cannot exceed $MAX_BALANCE"
        }
        this.amount = newAmount
        this.updatedAt = LocalDateTime.now()
        return this
    }

    fun withdraw(amount: Double): Balance {
        require(amount > 0) { "Withdrawal amount must be positive" }
        require(amount <= MAX_TRANSACTION_AMOUNT) {
            "Withdrawal amount cannot exceed $MAX_TRANSACTION_AMOUNT"
        }
        require(this.amount >= amount) { "Insufficient funds" }
        val newAmount = this.amount - amount
        require(newAmount >= MIN_BALANCE) { "Resulting balance cannot be negative" }
        this.amount = newAmount
        this.updatedAt = LocalDateTime.now()
        return this
    }
}