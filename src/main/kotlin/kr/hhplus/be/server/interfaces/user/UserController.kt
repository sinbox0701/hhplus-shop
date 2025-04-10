package kr.hhplus.be.server.interfaces.user

import kr.hhplus.be.server.interfaces.user.api.UserApi
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kr.hhplus.be.server.application.user.UserAccountFacade
import kr.hhplus.be.server.controller.coupon.dto.response.AccountCouponResponse
import kr.hhplus.be.server.controller.coupon.dto.response.CouponResponse
import kr.hhplus.be.server.domain.user.service.UserService
import kr.hhplus.be.server.interfaces.user.dto.request.UserRequest
import kr.hhplus.be.server.interfaces.user.dto.response.UserResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

import java.time.LocalDateTime
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/users")
@Validated
class UserController(
    private val userAccountFacade: UserAccountFacade,
    private val userService: UserService
): UserApi {

    @GetMapping
    override fun getAllUsers(): ResponseEntity<List<UserResponse.Response>> {
        // 목업 데이터 (추후 userService.findAll()로 대체 필요)
        val users = userService.findAll()
        val userResponses = users.map { user ->
            UserResponse.Response(
                id = user.id!!,
                name = user.name,
                email = user.email,
                loginId = user.loginId,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
        return ResponseEntity.ok(userResponses)
    }

    @GetMapping("/{id}")
    override fun getUserById(@PathVariable id: Long): ResponseEntity<UserResponse.DetailResponse> {
        // 유저와 계좌 정보 함께 조회
        val (user, account) = userAccountFacade.findUserWithAccount(id)
        
        val accountResponse = UserResponse.AccountResponse(
            id = account.id!!,
            userId = user.id!!,
            amount = account.amount,
            createdAt = account.createdAt,
            updatedAt = account.updatedAt
        )
        
        val userDetailResponse = UserResponse.DetailResponse(
            id = user.id,
            name = user.name,
            email = user.email,
            loginId = user.loginId,
            account = accountResponse,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
        
        return ResponseEntity.ok(userDetailResponse)
    }

    @PostMapping
    override fun createUser(@Valid @RequestBody request: UserRequest.CreateRequest): ResponseEntity<UserResponse.DetailResponse> {
        // 유저 생성과 동시에 계좌 생성
        val criteria = request.toCriteria()
        val user = userAccountFacade.createUserWithAccount(criteria)
        
        
        // 생성된 계좌 정보 조회
        val account = userAccountFacade.findUserWithAccount(user.id!!).second
        
        val accountResponse = UserResponse.AccountResponse(
            id = account.id!!,
            userId = user.id,
            amount = account.amount,
            createdAt = account.createdAt,
            updatedAt = account.updatedAt
        )
        
        val userDetailResponse = UserResponse.DetailResponse(
            id = user.id,
            name = user.name,
            email = user.email,
            loginId = user.loginId,
            account = accountResponse,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(userDetailResponse)
    }

    @PutMapping("/{id}")
    override fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UserRequest.UpdateRequest
    ): ResponseEntity<UserResponse.Response> {
        // 유저 정보 업데이트
        val criteria = request.toCommand(id)
        val updatedUser = userService.update(criteria)
        
        val userResponse = UserResponse.Response(
            id = updatedUser.id!!,
            name = updatedUser.name,
            email = updatedUser.email,
            loginId = updatedUser.loginId,
            createdAt = updatedUser.createdAt,
            updatedAt = updatedUser.updatedAt
        )
        
        return ResponseEntity.ok(userResponse)
    }

    @DeleteMapping("/{id}")
    override fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        // 유저와 계좌 함께 삭제
        userAccountFacade.deleteUserWithAccount(id)
        return ResponseEntity.noContent().build()
    }
    
    @PostMapping("/{id}/deposit")
    override fun depositBalance(
        @PathVariable id: Long,
        @Valid @RequestBody request: UserRequest.AccountDepositRequest
    ): ResponseEntity<UserResponse.AccountResponse> {
        // 계좌 충전
        val criteria = request.toCriteria(id)
        val user = userService.findById(id)
        val updatedAccount = userAccountFacade.chargeAccount(criteria)
        
        val accountResponse = UserResponse.AccountResponse(
            id = updatedAccount.id!!,
            userId = user.id!!,
            amount = updatedAccount.amount,
            createdAt = updatedAccount.createdAt,
            updatedAt = updatedAccount.updatedAt
        )
        
        return ResponseEntity.ok(accountResponse)
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
        // 유저 존재 확인
        userService.findById(id)
        
        // 쿠폰 서비스를 통해 쿠폰 목록 조회 (실제 구현 필요)
        val now = LocalDateTime.now()
        
        // Mock coupons (실제 구현 시 삭제 필요)
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