package kr.hhplus.be.server.domain.user.model

import java.time.LocalDateTime

data class Account private constructor(
    val id: Long? = null, // 데이터베이스가 자동 생성하므로 null 허용
    val userId: Long,
    val amount: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        const val MIN_BALANCE: Double = 0.0
        const val MAX_BALANCE: Double = 100000000.0 // 100,000,000 원
        const val MAX_TRANSACTION_AMOUNT: Double = 10000000.0 // 10,000,000 원

        fun create(userId: Long, initialAmount: Double = MIN_BALANCE): Account {
            require(initialAmount >= MIN_BALANCE && initialAmount <= MAX_BALANCE) {
                "Initial amount must be between $MIN_BALANCE and $MAX_BALANCE"
            }
            return Account(userId=userId, amount=initialAmount, createdAt=LocalDateTime.now(), updatedAt=LocalDateTime.now())
        }
    }

    fun charge(amount: Double): Account {
        require(amount > 0) { "Charge amount must be positive" }
        require(amount <= MAX_TRANSACTION_AMOUNT) {
            "Charge amount cannot exceed $MAX_TRANSACTION_AMOUNT"
        }
        val newAmount = this.amount + amount
        require(newAmount <= MAX_BALANCE) {
            "Resulting balance cannot exceed $MAX_BALANCE"
        }
        return Account(
            id = this.id,
            userId = this.userId,
            amount = newAmount,
            createdAt = this.createdAt,
            updatedAt = LocalDateTime.now()
        )
    }

    fun withdraw(amount: Double): Account {
        require(amount > 0) { "Withdrawal amount must be positive" }
        require(amount <= MAX_TRANSACTION_AMOUNT) {
            "Withdrawal amount cannot exceed $MAX_TRANSACTION_AMOUNT"
        }
        require(this.amount >= amount) { "Insufficient funds" }
        val newAmount = this.amount - amount
        require(newAmount >= MIN_BALANCE) { "Resulting balance cannot be below MIN_BALANCE" }
        return Account(
            id = this.id,
            userId = this.userId,
            amount = newAmount,
            createdAt = this.createdAt,
            updatedAt = LocalDateTime.now()
        )
    }
}