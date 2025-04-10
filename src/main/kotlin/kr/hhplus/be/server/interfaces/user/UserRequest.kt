package kr.hhplus.be.server.interfaces.user.dto.request

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import kr.hhplus.be.server.application.user.UserCriteria
import kr.hhplus.be.server.domain.user.service.UserCommand
class UserRequest {
    
    data class CreateRequest(
        @field:NotBlank(message = "이름은 필수입니다")
        @field:Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
        @field:Pattern(
            regexp = "^[가-힣a-zA-Z]*$",
            message = "이름은 한글 또는 영문만 입력 가능합니다"
        )
        val name: String,
        
        @field:NotBlank(message = "이메일은 필수입니다")
        @field:Email(message = "유효한 이메일 형식이 아닙니다")
        @field:Size(max = 100, message = "이메일은 최대 100자까지 가능합니다")
        val email: String,
        
        @field:NotBlank(message = "로그인 ID는 필수입니다")
        @field:Size(min = 4, max = 8, message = "로그인 ID는 4자 이상 8자 이하여야 합니다")
        val loginId: String,
        
        @field:NotBlank(message = "비밀번호는 필수입니다")
        @field:Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])[a-zA-Z0-9]{8,12}$",
            message = "비밀번호는 8-12자의 영문, 숫자 조합이어야 합니다"
        )
        val password: String
    ) {
        fun toCriteria(): UserCriteria.CreateUserCriteria {
            return UserCriteria.CreateUserCriteria(
                name = name,
                email = email,
                loginId = loginId,
                password = password
            )
        }
    }
    
    data class UpdateRequest(
        @field:Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
        @field:Pattern(
            regexp = "^[가-힣a-zA-Z]*$",
            message = "이름은 한글 또는 영문만 입력 가능합니다"
        )
        val name: String? = null,
        
        @field:Email(message = "유효한 이메일 형식이 아닙니다")
        @field:Size(max = 100, message = "이메일은 최대 100자까지 가능합니다")
        val email: String? = null,
        
        @field:Size(min = 4, max = 8, message = "로그인 ID는 4자 이상 8자 이하여야 합니다")
        val loginId: String? = null,
        
        @field:Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])[a-zA-Z0-9]{8,12}$",
            message = "비밀번호는 8-12자의 영문, 숫자 조합이어야 합니다"
        )
        val password: String? = null
    ) {
        fun toCommand(id: Long): UserCommand.UpdateUserCommand {
            return UserCommand.UpdateUserCommand(
                id = id,
                loginId = loginId,
                password = password
            )
        }
    }
    
    data class AccountDepositRequest(
        @field:NotNull(message = "입금액은 필수입니다")
        @field:DecimalMin(value = "1", message = "최소 입금액은 1원입니다")
        @field:DecimalMax(value = "10000000", message = "최대 입금액은 1000만원입니다")
        val amount: Double
    ) {
        fun toCriteria(userId: Long): UserCriteria.ChargeAccountCriteria {
            return UserCriteria.ChargeAccountCriteria(
                userId = userId,
                amount = amount
            )
        }
    }
}