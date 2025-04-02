package kr.hhplus.be.server.domain.account

data class Account(
    val accountId: Int,
    var name: String,
    var email: String,
    var loginId: String,
    var password: String
)
