package kr.hhplus.be.server.domain.user.model

import java.time.LocalDateTime

data class User private constructor(
    val id: Long? = null, // 데이터베이스가 자동 생성하므로 null 허용
    val name: String,
    val email: String,
    val loginId: String,
    val password: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
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
            val now = LocalDateTime.now()
            return User(
                name = name,
                email = email,
                loginId = loginId,
                password = password,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    fun update(
        newLoginId: String? = null,
        newPassword: String? = null
    ): User {
         // 변경할 필드가 없으면 현재 객체 그대로 반환
         if (newLoginId == null && newPassword == null) {
            return this
        }

        val updatedLoginId = newLoginId?.also {
            require(it.length in MIN_LOGIN_ID_LENGTH..MAX_LOGIN_ID_LENGTH) {
                "Login ID must be between $MIN_LOGIN_ID_LENGTH and $MAX_LOGIN_ID_LENGTH characters"
            }
        } ?: this.loginId

        val updatedPassword = newPassword?.also {
            require(PASSWORD_REGEX.matches(it)) {
                "Password must be a combination of letters and numbers and between $MIN_PASSWORD_LENGTH and $MAX_PASSWORD_LENGTH characters"
            }
        } ?: this.password

        return User(
            id = this.id,
            name = this.name,
            email = this.email,
            loginId = updatedLoginId,
            password = updatedPassword,
            createdAt = this.createdAt,
            updatedAt = LocalDateTime.now()
        )
    }
}
