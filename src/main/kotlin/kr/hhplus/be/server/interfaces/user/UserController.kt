package kr.hhplus.be.server.interfaces.user

import kr.hhplus.be.server.interfaces.user.api.UserApi
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kr.hhplus.be.server.application.user.UserAccountFacade
import kr.hhplus.be.server.domain.user.service.UserService
import kr.hhplus.be.server.interfaces.user.UserRequest
import kr.hhplus.be.server.interfaces.user.UserResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

import jakarta.validation.Valid
import kr.hhplus.be.server.domain.user.service.AccountCommand
import kr.hhplus.be.server.domain.user.service.AccountService
import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.interfaces.user.UserRequest.ChargeAccountWithOptimisticLockRequest
import kr.hhplus.be.server.interfaces.user.UserRequest.WithdrawAccountWithOptimisticLockRequest

@RestController
@RequestMapping("/api/users")
@Validated
class UserController(
    private val userAccountFacade: UserAccountFacade,
    private val userService: UserService,
    private val accountService: AccountService
): UserApi {

    @GetMapping
    override fun getAllUsers(): ResponseEntity<List<UserResponse.Response>> {
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

    /**
     * 낙관적 락을 사용한 포인트 충전 API
     */
    @PostMapping("/{userId}/accounts/{accountId}/charge-with-optimistic-lock")
    fun chargeAccountWithOptimisticLock(
        @PathVariable userId: Long,
        @PathVariable accountId: Long,
        @RequestBody request: ChargeAccountWithOptimisticLockRequest
    ): Account {
        val command = AccountCommand.UpdateAccountCommand(
            id = accountId,
            amount = request.amount
        )
        return accountService.chargeWithOptimisticLock(command)
    }
    
    /**
     * 낙관적 락을 사용한 포인트 출금 API
     */
    @PostMapping("/{userId}/accounts/{accountId}/withdraw-with-optimistic-lock")
    fun withdrawAccountWithOptimisticLock(
        @PathVariable userId: Long,
        @PathVariable accountId: Long,
        @RequestBody request: WithdrawAccountWithOptimisticLockRequest
    ): Account {
        val command = AccountCommand.UpdateAccountCommand(
            id = accountId,
            amount = request.amount
        )
        return accountService.withdrawWithOptimisticLock(command)
    }
} 