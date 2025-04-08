package kr.hhplus.be.server.application.user

import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.user.model.User
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
    fun createUserWithAccount(name: String, email: String, loginId: String, password: String, initialAmount: Double = Account.MIN_BALANCE): User {
        // 유저 생성
        val user = userService.create(name, email, loginId, password)
        
        // 계좌 생성
        accountService.create(user.id, initialAmount)
        
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
    fun chargeAccount(userId: Long, amount: Double): Account {
        // 유저 확인
        val user = userService.findById(userId)
        
        // 계좌 확인
        val account = accountService.findByUserId(userId) 
            ?: throw IllegalArgumentException("해당 유저의 계좌가 존재하지 않습니다: ${user.id}")
        
        // 계좌 충전
        return accountService.charge(account.id, amount)
    }
    
    /**
     * 유저 확인 후 계좌 잔액 차감
     */
    @Transactional
    fun withdrawAccount(userId: Long, amount: Double): Account {
        // 유저 확인
        val user = userService.findById(userId)
        
        // 계좌 확인
        val account = accountService.findByUserId(userId)
            ?: throw IllegalArgumentException("해당 유저의 계좌가 존재하지 않습니다: ${user.id}")
        
        // 계좌 차감
        return accountService.withdraw(account.id, amount)
    }
    
    /**
     * 유저 삭제 시 계좌도 함께 삭제
     */
    @Transactional
    fun deleteUserWithAccount(userId: Long) {
        // 계좌 확인 및 삭제
        val account = accountService.findByUserId(userId)
        account?.let { accountService.delete(it.id) }
        
        // 유저 삭제
        userService.delete(userId)
    }
}