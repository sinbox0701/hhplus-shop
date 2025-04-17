package kr.hhplus.be.server.user

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.user.UserAccountFacade
import kr.hhplus.be.server.application.user.UserCriteria
import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.user.service.AccountService
import kr.hhplus.be.server.domain.user.service.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class UserAccountFacadeIntegrationTest {

    private lateinit var userService: UserService
    private lateinit var accountService: AccountService
    private lateinit var userAccountFacade: UserAccountFacade

    companion object {
        private const val TEST_ID = 1L
        private const val TEST_NAME = "테스트유저"
        private const val TEST_EMAIL = "test@example.com"
        private const val TEST_LOGIN_ID = "testuser"
        private const val TEST_PASSWORD = "password123"
        private const val CHARGE_AMOUNT = 10000.0
        private const val WITHDRAW_AMOUNT = 5000.0
    }

    @BeforeEach
    fun setup() {
        userService = mockk()
        accountService = mockk()
        userAccountFacade = UserAccountFacade(userService, accountService)
    }

    @Test
    @DisplayName("유저 생성과 계좌 생성이 함께 진행되는지 확인")
    fun createUserWithAccountSuccess() {
        // given
        val criteria = UserCriteria.CreateUserCriteria(
            name = TEST_NAME,
            email = TEST_EMAIL,
            loginId = TEST_LOGIN_ID,
            password = TEST_PASSWORD
        )

        val user = User.create(
            name = criteria.name,
            email = criteria.email,
            loginId = criteria.loginId,
            password = criteria.password
        ).apply { 
            val field = this::class.java.getDeclaredField("id")
            field.isAccessible = true
            field.set(this, TEST_ID)
        }

        val account = Account.create(
            userId = TEST_ID,
            initialAmount = Account.MIN_BALANCE
        )

        every { userService.create(any()) } returns user
        every { accountService.create(any()) } returns account

        // when
        val createdUser = userAccountFacade.createUserWithAccount(criteria)

        // then
        assertNotNull(createdUser)
        assertEquals(TEST_ID, createdUser.id)
        assertEquals(criteria.name, createdUser.name)
        assertEquals(criteria.email, createdUser.email)
        assertEquals(criteria.loginId, createdUser.loginId)

        verify(exactly = 1) { userService.create(any()) }
        verify(exactly = 1) { accountService.create(any()) }
    }

    @Test
    @DisplayName("유저와 계좌 정보 조회 성공")
    fun findUserWithAccountSuccess() {
        // given
        val user = User.create(
            name = TEST_NAME,
            email = TEST_EMAIL,
            loginId = TEST_LOGIN_ID,
            password = TEST_PASSWORD
        ).apply { 
            val field = this::class.java.getDeclaredField("id")
            field.isAccessible = true
            field.set(this, TEST_ID)
        }

        val account = Account.create(
            userId = TEST_ID,
            initialAmount = Account.MIN_BALANCE
        )

        every { userService.findById(TEST_ID) } returns user
        every { accountService.findByUserId(TEST_ID) } returns account

        // when
        val (foundUser, foundAccount) = userAccountFacade.findUserWithAccount(TEST_ID)

        // then
        assertNotNull(foundUser)
        assertNotNull(foundAccount)
        assertEquals(TEST_ID, foundUser.id)
        assertEquals(TEST_ID, foundAccount.userId)
        assertEquals(Account.MIN_BALANCE, foundAccount.amount)

        verify(exactly = 1) { userService.findById(TEST_ID) }
        verify(exactly = 1) { accountService.findByUserId(TEST_ID) }
    }

    @Test
    @DisplayName("존재하지 않는 유저 조회 시 예외 발생")
    fun findNonExistentUserWithAccountThrowsException() {
        // given
        val nonExistentUserId = 999999L
        val errorMessage = "사용자를 찾을 수 없습니다: $nonExistentUserId"
        
        every { userService.findById(nonExistentUserId) } throws IllegalArgumentException(errorMessage)

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            userAccountFacade.findUserWithAccount(nonExistentUserId)
        }
        
        assertEquals(errorMessage, exception.message)
        verify(exactly = 1) { userService.findById(nonExistentUserId) }
    }

    @Test
    @DisplayName("계좌 충전 성공")
    fun chargeAccountSuccess() {
        // given
        val user = User.create(
            name = TEST_NAME,
            email = TEST_EMAIL,
            loginId = TEST_LOGIN_ID,
            password = TEST_PASSWORD
        ).apply { 
            val field = this::class.java.getDeclaredField("id")
            field.isAccessible = true
            field.set(this, TEST_ID)
        }

        val accountBeforeCharge = Account.create(
            userId = TEST_ID,
            initialAmount = Account.MIN_BALANCE
        ).apply {
            val field = this::class.java.getDeclaredField("id")
            field.isAccessible = true
            field.set(this, TEST_ID)
        }

        val accountAfterCharge = Account.create(
            userId = TEST_ID,
            initialAmount = Account.MIN_BALANCE + CHARGE_AMOUNT
        ).apply {
            val field = this::class.java.getDeclaredField("id")
            field.isAccessible = true
            field.set(this, TEST_ID)
        }

        val chargeCriteria = UserCriteria.ChargeAccountCriteria(
            userId = TEST_ID,
            amount = CHARGE_AMOUNT
        )

        every { userService.findById(TEST_ID) } returns user
        every { accountService.findByUserId(TEST_ID) } returns accountBeforeCharge
        every { accountService.charge(any()) } returns accountAfterCharge

        // when
        val chargedAccount = userAccountFacade.chargeAccount(chargeCriteria)

        // then
        assertNotNull(chargedAccount)
        assertEquals(Account.MIN_BALANCE + CHARGE_AMOUNT, chargedAccount.amount)

        verify(exactly = 1) { userService.findById(TEST_ID) }
        verify(exactly = 1) { accountService.findByUserId(TEST_ID) }
        verify(exactly = 1) { accountService.charge(any()) }
    }

    @Test
    @DisplayName("존재하지 않는 유저의 계좌 충전 시 예외 발생")
    fun chargeNonExistentUserAccountThrowsException() {
        // given
        val nonExistentUserId = 999999L
        val errorMessage = "사용자를 찾을 수 없습니다: $nonExistentUserId"
        
        val chargeCriteria = UserCriteria.ChargeAccountCriteria(
            userId = nonExistentUserId,
            amount = CHARGE_AMOUNT
        )

        every { userService.findById(nonExistentUserId) } throws IllegalArgumentException(errorMessage)

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            userAccountFacade.chargeAccount(chargeCriteria)
        }
        
        assertEquals(errorMessage, exception.message)
        verify(exactly = 1) { userService.findById(nonExistentUserId) }
    }

    @Test
    @DisplayName("계좌 출금 성공")
    fun withdrawAccountSuccess() {
        // given
        val user = User.create(
            name = TEST_NAME,
            email = TEST_EMAIL,
            loginId = TEST_LOGIN_ID,
            password = TEST_PASSWORD
        ).apply { 
            val field = this::class.java.getDeclaredField("id")
            field.isAccessible = true
            field.set(this, TEST_ID)
        }

        val initialAmount = Account.MIN_BALANCE + CHARGE_AMOUNT

        val accountBeforeWithdraw = Account.create(
            userId = TEST_ID,
            initialAmount = initialAmount
        ).apply {
            val field = this::class.java.getDeclaredField("id")
            field.isAccessible = true
            field.set(this, TEST_ID)
        }

        val accountAfterWithdraw = Account.create(
            userId = TEST_ID,
            initialAmount = initialAmount - WITHDRAW_AMOUNT
        ).apply {
            val field = this::class.java.getDeclaredField("id")
            field.isAccessible = true
            field.set(this, TEST_ID)
        }

        every { userService.findById(TEST_ID) } returns user
        every { accountService.findByUserId(TEST_ID) } returns accountBeforeWithdraw
        every { accountService.withdraw(any()) } returns accountAfterWithdraw

        // when
        val withdrawnAccount = userAccountFacade.withdrawAccount(TEST_ID, WITHDRAW_AMOUNT)

        // then
        assertNotNull(withdrawnAccount)
        assertEquals(initialAmount - WITHDRAW_AMOUNT, withdrawnAccount.amount)

        verify(exactly = 1) { userService.findById(TEST_ID) }
        verify(exactly = 1) { accountService.findByUserId(TEST_ID) }
        verify(exactly = 1) { accountService.withdraw(any()) }
    }

    @Test
    @DisplayName("유저와 계좌 삭제 성공")
    fun deleteUserWithAccountSuccess() {
        // given
        val accountId = TEST_ID
        val account = Account.create(
            userId = TEST_ID,
            initialAmount = Account.MIN_BALANCE
        ).apply {
            val field = this::class.java.getDeclaredField("id")
            field.isAccessible = true
            field.set(this, accountId)
        }

        every { accountService.findByUserId(TEST_ID) } returns account
        every { accountService.delete(accountId) } returns Unit
        every { userService.delete(TEST_ID) } returns Unit

        // when
        userAccountFacade.deleteUserWithAccount(TEST_ID)

        // then
        verify(exactly = 1) { accountService.findByUserId(TEST_ID) }
        verify(exactly = 1) { accountService.delete(accountId) }
        verify(exactly = 1) { userService.delete(TEST_ID) }
    }
} 