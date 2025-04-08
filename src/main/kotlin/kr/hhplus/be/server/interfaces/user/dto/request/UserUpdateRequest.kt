package kr.hhplus.be.server.controller.user.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserUpdateRequest(
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
) 