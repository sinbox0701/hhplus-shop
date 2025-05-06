package kr.hhplus.be.server.user

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.user.model.User
import java.time.LocalDateTime

object TestFixtures {
    // 상수 정의
    const val USER_ID = 1L
    const val ACCOUNT_ID = 1L
    const val DEFAULT_BALANCE = 10000.0
    
    // 사용자 Fixture
    fun createUser(
        id: Long = USER_ID,
        name: String = "테스트 사용자",
        email: String = "test@example.com",
        loginId: String = "testuser",
        password: String = "pass123a"
    ): User {
        val now = LocalDateTime.now()
        val user = mockk<User>()
        
        every { user.id } returns id
        every { user.name } returns name
        every { user.email } returns email
        every { user.loginId } returns loginId
        every { user.password } returns password
        every { user.createdAt } returns now.minusDays(2)
        every { user.updatedAt } returns now.minusDays(2)
        
        return user
    }
    
    // 계좌 Fixture
    fun createAccount(
        id: Long = ACCOUNT_ID,
        userId: Long = USER_ID,
        amount: Double = DEFAULT_BALANCE
    ): Account {
        val now = LocalDateTime.now()
        val account = mockk<Account>()
        
        every { account.id } returns id
        every { account.userId } returns userId
        every { account.amount } returns amount
        every { account.createdAt } returns now.minusDays(2)
        every { account.updatedAt } returns now.minusDays(2)
        
        return account
    }
    
    // 사용자 컬렉션 생성 헬퍼 메서드
    fun createUsers(count: Int): List<User> {
        return (1..count).map {
            createUser(id = it.toLong(), loginId = "user$it", email = "user$it@example.com")
        }
    }
    
    // 계좌 컬렉션 생성 헬퍼 메서드
    fun createAccounts(count: Int): List<Account> {
        return (1..count).map {
            createAccount(id = it.toLong(), userId = it.toLong())
        }
    }
} 