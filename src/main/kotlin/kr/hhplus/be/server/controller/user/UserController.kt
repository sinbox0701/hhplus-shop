package kr.hhplus.be.server.controller.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kr.hhplus.be.server.controller.coupon.dto.response.AccountCouponResponse
import kr.hhplus.be.server.controller.coupon.dto.response.CouponResponse
import kr.hhplus.be.server.controller.user.dto.request.BalanceDepositRequest
import kr.hhplus.be.server.controller.user.dto.request.UserCreateRequest
import kr.hhplus.be.server.controller.user.dto.request.UserUpdateRequest
import kr.hhplus.be.server.controller.user.dto.response.BalanceResponse
import kr.hhplus.be.server.controller.user.dto.response.BalanceTransactionResponse
import kr.hhplus.be.server.controller.user.dto.response.UserDetailResponse
import kr.hhplus.be.server.controller.user.dto.response.UserResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/users")
@Validated
class UserController() {

    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        // Mock API response
        val now = LocalDateTime.now()
        val users = listOf(
            UserResponse(
                accountId = 1,
                name = "홍길동",
                email = "hong@example.com",
                loginId = "hong1234",
                createdAt = now.minusDays(5),
                updatedAt = now.minusDays(2)
            ),
            UserResponse(
                accountId = 2,
                name = "김철수",
                email = "kim@example.com",
                loginId = "kim5678",
                createdAt = now.minusDays(3),
                updatedAt = now.minusDays(1)
            )
        )
        return ResponseEntity.ok(users)
    }

    @GetMapping("/{accountId}")
    fun getUserById(@PathVariable accountId: Int): ResponseEntity<UserDetailResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        val user = UserDetailResponse(
            accountId = accountId,
            name = "사용자$accountId",
            email = "user$accountId@example.com",
            loginId = "user$accountId",
            balance = BalanceResponse(
                balanceId = accountId,
                accountId = accountId,
                amount = BigDecimal("10000.00"),
                createdAt = now.minusDays(5),
                updatedAt = now.minusDays(1)
            ),
            createdAt = now.minusDays(5),
            updatedAt = now.minusDays(1)
        )
        return ResponseEntity.ok(user)
    }

    @PostMapping
    fun createUser(@Valid @RequestBody request: UserCreateRequest): ResponseEntity<UserDetailResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        
        val createdUser = UserDetailResponse(
            accountId = 1,
            name = request.name,
            email = request.email,
            loginId = request.loginId,
            balance = BalanceResponse(
                balanceId = 1,
                accountId = 1,
                amount = BigDecimal.ZERO,
                createdAt = now,
                updatedAt = now
            ),
            createdAt = now,
            updatedAt = now
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)
    }

    @PutMapping("/{accountId}")
    fun updateUser(
        @PathVariable accountId: Int,
        @Valid @RequestBody request: UserUpdateRequest
    ): ResponseEntity<UserResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        
        // 기본값 설정
        val name = request.name ?: "사용자$accountId"
        val email = request.email ?: "user$accountId@example.com"
        val loginId = request.loginId ?: "user$accountId"
        
        val updatedUser = UserResponse(
            accountId = accountId,
            name = name,
            email = email,
            loginId = loginId,
            createdAt = now.minusDays(5),
            updatedAt = now
        )
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/{accountId}")
    fun deleteUser(@PathVariable accountId: Int): ResponseEntity<Void> {
        // Mock deletion
        return ResponseEntity.noContent().build()
    }
    
    @PostMapping("/{accountId}/deposit")
    fun depositBalance(
        @PathVariable accountId: Int,
        @Valid @RequestBody request: BalanceDepositRequest
    ): ResponseEntity<BalanceTransactionResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        
        // 기존 계좌 잔액 + 입금액
        val currentBalance = BigDecimal("10000.00")
        val newBalance = currentBalance.add(request.amount)
        
        val balanceResponse = BalanceTransactionResponse(
            balanceId = accountId,
            accountId = accountId,
            amount = newBalance,
            createdAt = now,
            updatedAt = now
        )
        
        return ResponseEntity.ok(balanceResponse)
    }

    @Operation(summary = "사용자별 쿠폰 목록 조회", description = "특정 사용자에게 발급된 모든 쿠폰 목록을 조회합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = AccountCouponResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = [Content()]
        )
    )
    @GetMapping("/{accountId}/coupons")
    fun getUserCoupons(
        @Parameter(description = "계정 ID", required = true)
        @PathVariable accountId: Int
    ): ResponseEntity<List<AccountCouponResponse>> {
        // Mock API response
        val now = LocalDateTime.now()
        
        // Mock coupons
        val coupon1 = CouponResponse(
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
        
        val coupon2 = CouponResponse(
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
        
        val accountCoupons = listOf(
            AccountCouponResponse(
                accountCouponId = 1,
                accountId = accountId,
                couponId = 1,
                issueDate = now.minusDays(2),
                issued = true,
                used = false,
                coupon = coupon1
            ),
            AccountCouponResponse(
                accountCouponId = 2,
                accountId = accountId,
                couponId = 2,
                issueDate = now.minusDays(1),
                issued = true,
                used = true,
                coupon = coupon2
            )
        )
        return ResponseEntity.ok(accountCoupons)
    }
} 