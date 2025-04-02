package kr.hhplus.be.server.domain.account

data class Account private constructor(
    val accountId: Int,
    var name: String,
    var email: String,
    var loginId: String,
    var password: String
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
            accountId: Int,
            name: String,
            email: String,
            loginId: String,
            password: String
        ): Account {
            require(loginId.length in MIN_LOGIN_ID_LENGTH..MAX_LOGIN_ID_LENGTH) {
                "Login ID must be between $MIN_LOGIN_ID_LENGTH and $MAX_LOGIN_ID_LENGTH characters"
            }
            require(PASSWORD_REGEX.matches(password)) {
                "Password must be a combination of letters and numbers and between $MIN_PASSWORD_LENGTH and $MAX_PASSWORD_LENGTH characters"
            }
            return Account(accountId, name, email, loginId, password)
        }
    }
}
