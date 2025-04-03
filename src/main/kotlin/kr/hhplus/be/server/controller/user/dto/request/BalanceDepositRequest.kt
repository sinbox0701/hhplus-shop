package kr.hhplus.be.server.controller.user.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import java.math.BigDecimal

data class BalanceDepositRequest(
    @field:NotNull(message = "입금액은 필수입니다")
    @field:DecimalMin(value = "1", message = "최소 입금액은 1원입니다")
    @field:DecimalMax(value = "10000000", message = "최대 입금액은 1000만원입니다")
    val amount: BigDecimal
) 