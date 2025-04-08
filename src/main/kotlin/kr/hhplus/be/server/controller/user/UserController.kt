package kr.hhplus.be.server.controller.user

import kr.hhplus.be.server.controller.user.api.UserApi
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

import java.time.LocalDateTime
import java.util.UUID
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/users")
@Validated
class UserController(): UserApi {

    @GetMapping
    override fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        // Mock API response
        val now = LocalDateTime.now()
        val users = listOf(
            UserResponse(
                id = 1L,
                name = "홍길동",
                email = "hong@example.com",
                loginId = "hong1234",
                createdAt = now.minusDays(5),
                updatedAt = now.minusDays(2)
            ),
            UserResponse(
                id = 2L,
                name = "김철수",
                email = "kim@example.com",
                loginId = "kim5678",
                createdAt = now.minusDays(3),
                updatedAt = now.minusDays(1)
            )
        )
        return ResponseEntity.ok(users)
    }

    @GetMapping("/{id}")
    override fun getUserById(@PathVariable id: Long): ResponseEntity<UserDetailResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        val user = UserDetailResponse(
            id = id,
            name = "사용자$id",
            email = "user$id@example.com",
            loginId = "user$id",
            balance = BalanceResponse(
                id = id,
                accountId = id,
                amount = 10000.00,
                createdAt = now.minusDays(5),
                updatedAt = now.minusDays(1)
            ),
            createdAt = now.minusDays(5),
            updatedAt = now.minusDays(1)
        )
        return ResponseEntity.ok(user)
    }

    @PostMapping
    override fun createUser(@Valid @RequestBody request: UserCreateRequest): ResponseEntity<UserDetailResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        
        val createdUser = UserDetailResponse(
            id = 1L,
            name = request.name,
            email = request.email,
            loginId = request.loginId,
            balance = BalanceResponse(
                id = 1L,
                accountId = 1L,
                amount = 0.0,
                createdAt = now,
                updatedAt = now
            ),
            createdAt = now,
            updatedAt = now
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)
    }

    @PutMapping("/{id}")
    override fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UserUpdateRequest
    ): ResponseEntity<UserResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        
        // 기본값 설정
        val name = request.name ?: "사용자$id"
        val email = request.email ?: "user$id@example.com"
        val loginId = request.loginId ?: "user$id"
        
        val updatedUser = UserResponse(
            id = id,
            name = name,
            email = email,
            loginId = loginId,
            createdAt = now.minusDays(5),
            updatedAt = now
        )
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/{id}")
    override fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        // Mock deletion
        return ResponseEntity.noContent().build()
    }
    
    @PostMapping("/{id}/deposit")
    override fun depositBalance(
        @PathVariable id: Long,
        @Valid @RequestBody request: BalanceDepositRequest
    ): ResponseEntity<BalanceResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        
        // 기존 계좌 잔액 + 입금액
        val currentBalance = 10000.00
        val newBalance = currentBalance + request.amount
        
        val balanceResponse = BalanceResponse(
            id = id,
            accountId = id,
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
    @GetMapping("/{id}/coupons")
    override fun getUserCoupons(
        @Parameter(description = "계정 ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<List<AccountCouponResponse>> {
        // Mock API response
        val now = LocalDateTime.now()
        
        // Mock coupons
        val coupon1 = CouponResponse(
            id = 1L,
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
            id = 2L,
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
                id = 1L,
                accountId = id,
                couponId = 1L,
                issueDate = now.minusDays(2),
                issued = true,
                used = false,
                coupon = coupon1
            ),
            AccountCouponResponse(
                id = 2L,
                accountId = id,
                couponId = 2L,
                issueDate = now.minusDays(1),
                issued = true,
                used = true,
                coupon = coupon2
            )
        )
        return ResponseEntity.ok(accountCoupons)
    }
} 