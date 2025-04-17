package kr.hhplus.be.server.infrastructure.user

import kr.hhplus.be.server.domain.user.model.User
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false, unique = true, length = 8)
    val loginId: String,

    @Column(nullable = false)
    val password: String,

    @Column(nullable = false)
    val createdAt: LocalDateTime,

    @Column(nullable = false)
    val updatedAt: LocalDateTime
) {
    fun toUser(): User {
        return User.create(
            name = name,
            email = email,
            loginId = loginId,
            password = password
        ).apply {
            // 불변 객체이므로 리플렉션을 사용하여 id와 생성/수정 시간을 설정
            val userClass = User::class.java
            val idField = userClass.getDeclaredField("id")
            val createdAtField = userClass.getDeclaredField("createdAt")
            val updatedAtField = userClass.getDeclaredField("updatedAt")
            
            idField.isAccessible = true
            createdAtField.isAccessible = true
            updatedAtField.isAccessible = true
            
            idField.set(this, id)
            createdAtField.set(this, createdAt)
            updatedAtField.set(this, updatedAt)
        }
    }

    companion object {
        fun fromUser(user: User): UserEntity {
            return UserEntity(
                id = user.id,
                name = user.name,
                email = user.email,
                loginId = user.loginId,
                password = user.password,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
} 