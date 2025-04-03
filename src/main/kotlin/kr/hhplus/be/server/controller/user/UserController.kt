package kr.hhplus.be.server.controller.user

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
} 