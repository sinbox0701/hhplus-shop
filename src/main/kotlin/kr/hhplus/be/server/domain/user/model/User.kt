package kr.hhplus.be.server.domain.user.model

import java.time.LocalDateTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
data class User private constructor(
    @Id
    val id: Long,
    
    @Column(nullable = false)
    var name: String,
    
    @Column(nullable = false, unique = true)
    var email: String,
    
    @Column(nullable = false, unique = true, length = MAX_LOGIN_ID_LENGTH)
    var loginId: String,
    
    @Column(nullable = false, length = MAX_PASSWORD_LENGTH)
    var password: String,
    
    @Column(nullable = false)
    var createdAt: LocalDateTime,
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime
) {
    companion object {
        const val MIN_LOGIN_ID_LENGTH = 4
        const val MAX_LOGIN_ID_LENGTH = 8

        const val MIN_PASSWORD_LENGTH = 8
        const val MAX_PASSWORD_LENGTH = 12

        // 영어와 숫자의 조합인지 검사하는 정규식
        private val PASSWORD_REGEX =
            Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{$MIN_PASSWORD_LENGTH,$MAX_PASSWORD_LENGTH}\$")

        fun create(
            id: Long,
            name: String,
            email: String,
            loginId: String,
            password: String
        ): User {
            require(loginId.length in MIN_LOGIN_ID_LENGTH..MAX_LOGIN_ID_LENGTH) {
                "Login ID must be between $MIN_LOGIN_ID_LENGTH and $MAX_LOGIN_ID_LENGTH characters"
            }
            require(PASSWORD_REGEX.matches(password)) {
                "Password must be a combination of letters and numbers and between $MIN_PASSWORD_LENGTH and $MAX_PASSWORD_LENGTH characters"
            }
            return User(id, name, email, loginId, password, LocalDateTime.now(), LocalDateTime.now())
        }
    }

    fun update(
        newLoginId: String? = null,
        newPassword: String? = null
    ): User {
        newLoginId?.let {
            require(it.length in MIN_LOGIN_ID_LENGTH..MAX_LOGIN_ID_LENGTH) {
                "Login ID must be between $MIN_LOGIN_ID_LENGTH and $MAX_LOGIN_ID_LENGTH characters"
            }
            this.loginId = it
            this.updatedAt = LocalDateTime.now()
        }
        newPassword?.let {
            require(PASSWORD_REGEX.matches(it)) {
                "Password must be a combination of letters and numbers and between $MIN_PASSWORD_LENGTH and $MAX_PASSWORD_LENGTH characters"
            }
            this.password = it
            this.updatedAt = LocalDateTime.now()
        }
        return this
    }
}
