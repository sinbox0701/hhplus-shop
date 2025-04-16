package kr.hhplus.be.server.application.user

import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.user.service.AccountCommand
import kr.hhplus.be.server.domain.user.service.AccountService
import kr.hhplus.be.server.domain.user.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserAccountFacade(
    private val userService: UserService,
    private val accountService: AccountService
) {
    /**
     * 유저 생성과 동시에 계좌 생성
     */
    @Transactional
    fun createUserWithAccount(criteria: UserCriteria.CreateUserCriteria): User {
        // 유저 생성
        val user = userService.create(criteria.toUserCommand())
        
        // 계좌 생성
        accountService.create(criteria.toAccountCommand(user.id!!))
        
        return user
    }
    
    /**
     * 유저와 계좌 정보 조회
     */
    @Transactional(readOnly = true)
    fun findUserWithAccount(userId: Long): Pair<User, Account> {
        val user = userService.findById(userId)
        val account = accountService.findByUserId(userId)
        
        return Pair(user, account)
    }
    
    /**
     * 유저 확인 후 계좌 잔액 충전
     */
    @Transactional
    fun chargeAccount(criteria: UserCriteria.ChargeAccountCriteria): Account {
        // 유저 확인
        val user = userService.findById(criteria.userId)
        
        // 계좌 확인 - findByUserId는 계좌가 없을 경우 이미 예외를 던집니다
        val account = accountService.findByUserId(user.id!!)
        
        // account.id가 null이 아님을 보장 (이 시점에서 account 객체는 유효함)
        val accountId = account.id ?: throw IllegalStateException("계좌 ID가 null입니다")
        
        // 계좌 충전
        return accountService.charge(criteria.toChargeAccountCommand(accountId))
    }
    
    /**
     * 유저 확인 후 계좌 잔액 차감
     */
    @Transactional
    fun withdrawAccount(userId: Long, amount: Double): Account {
        // 유저 확인
        val user = userService.findById(userId)
        
        // 계좌 확인 - findByUserId는 계좌가 없을 경우 이미 예외를 던집니다
        val account = accountService.findByUserId(user.id!!)
        
        // account.id가 null이 아님을 보장 (이 시점에서 account 객체는 유효함)
        val accountId = account.id ?: throw IllegalStateException("계좌 ID가 null입니다")
        
        // 계좌 차감
        val command = AccountCommand.UpdateAccountCommand(id = accountId, amount = amount)
        return accountService.withdraw(command)
    }
    
    /**
     * 유저 삭제 시 계좌도 함께 삭제
     */
    @Transactional
    fun deleteUserWithAccount(userId: Long) {
        // 계좌 확인 및 삭제
        val account = accountService.findByUserId(userId)
        
        // account.id가 null이 아님을 보장
        val accountId = account.id ?: throw IllegalStateException("계좌 ID가 null입니다")
        accountService.delete(accountId)
        
        // 유저 삭제
        userService.delete(userId)
    }
}