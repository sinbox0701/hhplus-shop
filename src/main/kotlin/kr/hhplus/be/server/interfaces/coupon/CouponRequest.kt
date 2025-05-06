package kr.hhplus.be.server.interfaces.coupon

import java.time.LocalDateTime
import jakarta.validation.constraints.*
import kr.hhplus.be.server.domain.coupon.service.CouponCommand
import kr.hhplus.be.server.application.coupon.CouponCriteria
import kr.hhplus.be.server.domain.coupon.model.CouponType
import java.util.UUID
import io.swagger.v3.oas.annotations.media.Schema

class CouponRequest {
    data class CreateCouponRequest(
        @field:NotBlank(message = "쿠폰 코드는 필수입니다")
        @field:Size(min = 3, max = 50, message = "쿠폰 코드는 3자 이상 50자 이하여야 합니다")
        val code: String,
        
        @field:NotNull(message = "쿠폰 타입은 필수입니다")
        val couponType: CouponType,

        @field:NotBlank(message = "쿠폰 설명은 필수입니다")
        @field:Size(min = 2, max = 100, message = "쿠폰 설명은 2자 이상 100자 이하여야 합니다")
        val description: String,
        
        @field:NotNull(message = "할인율은 필수입니다")
        @field:Min(value = 1, message = "할인율은 최소 1% 이상이어야 합니다")
        @field:Max(value = 100, message = "할인율은 최대 100%까지 가능합니다")
        val discountRate: Double,
        
        @field:NotNull(message = "시작일은 필수입니다")
        val startDate: LocalDateTime,
        
        @field:NotNull(message = "종료일은 필수입니다")
        val endDate: LocalDateTime,
        
        @field:NotNull(message = "수량은 필수입니다")
        @field:Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다")
        val quantity: Int
    ) {
        fun toCommand(): CouponCommand.CreateCouponCommand {
            return CouponCommand.CreateCouponCommand(
                code = code,
                couponType = couponType,
                discountRate = discountRate,
                description = description,
                startDate = startDate,
                endDate = endDate,
                quantity = quantity
            )
        }
    }

    data class CouponIssueRequest(
        @field:NotNull(message = "계정 ID는 필수입니다")
        @field:Min(value = 1L, message = "계정 ID는 1 이상이어야 합니다")
        val accountId: Long
    ) {
        fun toCriteria(couponId: Long): CouponCriteria.UpdateCouponCommand {
            return CouponCriteria.UpdateCouponCommand(
                userId = accountId,
                couponId = couponId
            )
        }
    }

    data class UpdateCouponRequest(
        @field:Size(min = 2, max = 30, message = "쿠폰 설명은 2자 이상 30자 이하여야 합니다")
        val description: String? = null,
        
        @field:Min(value = 1, message = "할인율은 최소 1% 이상이어야 합니다")
        @field:Max(value = 100, message = "할인율은 최대 100%까지 가능합니다")
        val discountRate: Double? = null,
        
        val startDate: LocalDateTime? = null,
        
        val endDate: LocalDateTime? = null,
        
        @field:Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다")
        val quantity: Int? = null
    ) {
        fun toCommand(id: Long): CouponCommand.UpdateCouponCommand {
            return CouponCommand.UpdateCouponCommand(
                id = id,
                description = description,
                discountRate = discountRate,
                startDate = startDate,
                endDate = endDate,
                quantity = quantity
            )
        }
    }

    data class IssueFirstComeFirstServedCouponRequest(
        @field:NotBlank(message = "쿠폰 코드는 필수입니다.")
        @field:Pattern(regexp = "^[A-Z]{6}$", message = "쿠폰 코드는 대문자 영어 6자리여야 합니다.")
        @field:Schema(description = "쿠폰 코드", example = "ABCDEF")
        val couponCode: String
    )
}