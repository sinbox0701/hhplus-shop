package kr.hhplus.be.server.domain.user.model

import java.time.LocalDateTime

data class User private constructor(
    val id: Long? = null, // 데이터베이스가 자동 생성하므로 null 허용
    var name: String,
    var email: String,
    var loginId: String,
    var password: String,
    var createdAt: LocalDateTime,
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
