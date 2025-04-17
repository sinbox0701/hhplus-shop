package kr.hhplus.be.server.infrastructure.user

import kr.hhplus.be.server.domain.user.model.Account
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "accounts")
class AccountEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val userId: Long,
    
    @Column(nullable = false)
    val amount: Double,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime,
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime
) {
    fun toAccount(): Account {
        return Account.create(
            userId = userId,
            initialAmount = amount
        ).apply {
            // 불변 객체이므로 리플렉션을 사용하여 id와 생성/수정 시간을 설정
            val accountClass = Account::class.java
            val idField = accountClass.getDeclaredField("id")
            val createdAtField = accountClass.getDeclaredField("createdAt")
            val updatedAtField = accountClass.getDeclaredField("updatedAt")
            
            idField.isAccessible = true
            createdAtField.isAccessible = true
            updatedAtField.isAccessible = true
            
            idField.set(this, id)
            createdAtField.set(this, createdAt)
            updatedAtField.set(this, updatedAt)
        }
    }
    
    companion object {
        fun fromAccount(account: Account): AccountEntity {
            return AccountEntity(
                id = account.id,
                userId = account.userId,
                amount = account.amount,
                createdAt = account.createdAt,
                updatedAt = account.updatedAt
            )
        }
    }
} 