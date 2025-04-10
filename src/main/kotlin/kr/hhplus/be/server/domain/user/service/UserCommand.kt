package kr.hhplus.be.server.domain.user.service

import kr.hhplus.be.server.domain.user.model.User

class UserCommand {
    data class CreateUserCommand(
        val name: String,
        val email: String,
        val loginId: String,
        val password: String
    )
    
    data class UpdateUserCommand(
        val id: Long,
        val loginId: String?,
        val password: String?
    )

    data class LoginCommand(
        val loginId: String,
        val password: String
    )
    
}
