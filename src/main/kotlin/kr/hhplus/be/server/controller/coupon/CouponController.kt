package kr.hhplus.be.server.controller.coupon

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.controller.coupon.api.CouponApi
import kr.hhplus.be.server.controller.coupon.dto.request.CouponCreateRequest
import kr.hhplus.be.server.controller.coupon.dto.request.CouponIssueRequest
import kr.hhplus.be.server.controller.coupon.dto.request.CouponUpdateRequest
import kr.hhplus.be.server.controller.coupon.dto.response.AccountCouponResponse
import kr.hhplus.be.server.controller.coupon.dto.response.CouponResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/coupons")
@Validated
class CouponController : CouponApi {

    @GetMapping
    override fun getAllCoupons(): ResponseEntity<List<CouponResponse>> {
        // Mock API response
        val now = LocalDateTime.now()
        val coupons = listOf(
            CouponResponse(
                couponId = 1,
                discountRate = 10.0,
                description = "신규 가입 축하 쿠폰",
                startDate = now.minusDays(10),
                endDate = now.plusDays(20),
                quantity = 100,
                remainingQuantity = 80,
                createdAt = now.minusDays(10),
                updatedAt = now.minusDays(10)
            ),
            CouponResponse(
                couponId = 2,
                discountRate = 15.0,
                description = "첫 구매 감사 쿠폰",
                startDate = now.minusDays(5),
                endDate = now.plusDays(25),
                quantity = 50,
                remainingQuantity = 45,
                createdAt = now.minusDays(5),
                updatedAt = now.minusDays(5)
            )
        )
        return ResponseEntity.ok(coupons)
    }

    @GetMapping("/{couponId}")
    override fun getCouponById(
        @Parameter(description = "쿠폰 ID", required = true)
        @PathVariable couponId: Int
    ): ResponseEntity<CouponResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        val coupon = CouponResponse(
            couponId = couponId,
            discountRate = 10.0,
            description = "쿠폰 $couponId",
            startDate = now.minusDays(10),
            endDate = now.plusDays(20),
            quantity = 100,
            remainingQuantity = 80,
            createdAt = now.minusDays(10),
            updatedAt = now.minusDays(10)
        )
        return ResponseEntity.ok(coupon)
    }

    @PostMapping
    override fun createCoupon(
        @Parameter(description = "쿠폰 생성 정보", required = true)
        @Valid @RequestBody request: CouponCreateRequest
    ): ResponseEntity<CouponResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        val createdCoupon = CouponResponse(
            couponId = 3,
            discountRate = request.discountRate,
            description = request.description,
            startDate = request.startDate,
            endDate = request.endDate,
            quantity = request.quantity,
            remainingQuantity = request.quantity,
            createdAt = now,
            updatedAt = now
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCoupon)
    }

    @PutMapping("/{couponId}")
    override fun updateCoupon(
        @Parameter(description = "쿠폰 ID", required = true)
        @PathVariable couponId: Int,
        
        @Parameter(description = "쿠폰 수정 정보", required = true)
        @Valid @RequestBody request: CouponUpdateRequest
    ): ResponseEntity<CouponResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        
        // 기본값 설정
        val description = request.description ?: "쿠폰 $couponId"
        val discountRate = request.discountRate ?: 10.0
        val startDate = request.startDate ?: now.minusDays(10)
        val endDate = request.endDate ?: now.plusDays(20)
        val quantity = request.quantity ?: 100
        
        val updatedCoupon = CouponResponse(
            couponId = couponId,
            discountRate = discountRate,
            description = description,
            startDate = startDate,
            endDate = endDate,
            quantity = quantity,
            remainingQuantity = 80, // Mock value for remaining quantity
            createdAt = now.minusDays(10),
            updatedAt = now
        )
        return ResponseEntity.ok(updatedCoupon)
    }

    @DeleteMapping("/{couponId}")
    override fun deleteCoupon(
        @Parameter(description = "쿠폰 ID", required = true)
        @PathVariable couponId: Int
    ): ResponseEntity<Void> {
        // Mock deletion - assume coupon with ID 999 is already issued and cannot be deleted
        if (couponId == 999) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{couponId}/issue")
    override fun issueCoupon(
        @Parameter(description = "쿠폰 ID", required = true)
        @PathVariable couponId: Int,
        
        @Parameter(description = "쿠폰 발급 정보", required = true)
        @Valid @RequestBody request: CouponIssueRequest
    ): ResponseEntity<AccountCouponResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        
        // Mock coupon
        val coupon = CouponResponse(
            couponId = couponId,
            discountRate = 10.0,
            description = "쿠폰 $couponId",
            startDate = now.minusDays(10),
            endDate = now.plusDays(20),
            quantity = 100,
            remainingQuantity = 79, // Decreased by one after issuing
            createdAt = now.minusDays(10),
            updatedAt = now
        )
        
        // Mock coupon issue - assume coupon with ID 888 is out of stock
        if (couponId == 888) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
        
        val issuedCoupon = AccountCouponResponse(
            accountCouponId = 1,
            accountId = request.accountId,
            couponId = couponId,
            issueDate = now,
            issued = true,
            used = false,
            coupon = coupon
        )       
        return ResponseEntity.status(HttpStatus.CREATED).body(issuedCoupon)
    }

    @PostMapping("/{accountId}/use/{accountCouponId}")
    override fun useCoupon(
        @Parameter(description = "계정 ID", required = true)
        @PathVariable accountId: Int,
        
        @Parameter(description = "계정 쿠폰 ID", required = true)
        @PathVariable accountCouponId: Int
    ): ResponseEntity<AccountCouponResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        
        // Mock error case - assume accountCouponId 777 is already used
        if (accountCouponId == 777) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
        
        // Mock coupon
        val coupon = CouponResponse(
            couponId = 1,
            discountRate = 10.0,
            description = "신규 가입 축하 쿠폰",
            startDate = now.minusDays(10),
            endDate = now.plusDays(20),
            quantity = 100,
            remainingQuantity = 80,
            createdAt = now.minusDays(10),
            updatedAt = now.minusDays(10)
        )
        
        val usedCoupon = AccountCouponResponse(
            accountCouponId = accountCouponId,
            accountId = accountId,
            couponId = 1,
            issueDate = now.minusDays(2),
            issued = true,
            used = true,
            coupon = coupon
        )
        return ResponseEntity.ok(usedCoupon)
    }
}