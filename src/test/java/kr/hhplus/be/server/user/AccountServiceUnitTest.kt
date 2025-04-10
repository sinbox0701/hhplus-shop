package kr.hhplus.be.server.user

import io.mockk.*
import io.mockk.junit5.MockKExtension
import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.user.repository.AccountRepository
import kr.hhplus.be.server.domain.user.service.AccountService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.user.service.AccountCommand
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AccountServiceUnitTest {

    private lateinit var accountRepository: AccountRepository
    private lateinit var accountService: AccountService

    // 테스트 상수 정의
    companion object {
        private const val TEST_ID = 1L
        private const val TEST_USER_ID = 2L
        private const val TEST_INITIAL_AMOUNT = 5000.0
        private const val CHARGE_AMOUNT = 3000.0
        private const val WITHDRAW_AMOUNT = 2000.0
    }

    @BeforeEach
    fun setup() {
        accountRepository = mockk()
        accountService = AccountService(accountRepository)
    }

    @Test
    @DisplayName("새로운 계좌 생성 성공")
    fun createAccountSuccess() {
        // given
        val user = mockk<User> {
            every { id } returns TEST_USER_ID
        }
        
        val account = mockk<Account> {
            every { amount } returns TEST_INITIAL_AMOUNT
            every { user.id } returns TEST_USER_ID
        }
        every { account.user.id } returns TEST_USER_ID
        
        every { accountRepository.save(any()) } returns account
        
        // when
        val createdAccount = accountService.create(AccountCommand.CreateAccountCommand(user, TEST_INITIAL_AMOUNT))
        
        // then
        assertEquals(user.id, createdAccount.user.id)
        assertEquals(TEST_INITIAL_AMOUNT, createdAccount.amount)
        
        verify(exactly = 1) { accountRepository.save(any()) }
    }
    
    @Test
    @DisplayName("사용자 ID로 계좌 조회 성공")
    fun findAccountByUserIdSuccess() {
        // given
        val user = mockk<User> {
            every { id } returns TEST_USER_ID
        }
        
        val account = mockk<Account> {
            every { id } returns TEST_ID
            every { amount } returns TEST_INITIAL_AMOUNT
            every { user.id } returns TEST_USER_ID
        }
        every { account.user.id } returns TEST_USER_ID
        
        every { accountRepository.findByUserId(TEST_USER_ID) } returns account
        
        // when
        val foundAccount = accountService.findByUserId(TEST_USER_ID)
        
        // then
        assertEquals(TEST_ID, foundAccount.id)
        assertEquals(user.id, foundAccount.user.id)
        assertEquals(TEST_INITIAL_AMOUNT, foundAccount.amount)
        
        verify(exactly = 1) { accountRepository.findByUserId(TEST_USER_ID) }
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자 ID로 계좌 조회 시 예외 발생")
    fun findAccountByNonExistentUserIdThrowsException() {
        // given
        every { accountRepository.findByUserId(TEST_USER_ID) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountService.findByUserId(TEST_USER_ID)
        }
        
        assertTrue(exception.message!!.contains("계좌를 찾을 수 없습니다"))
        
        verify(exactly = 1) { accountRepository.findByUserId(TEST_USER_ID) }
    }
    
    @Test
    @DisplayName("계좌 ID로 계좌 조회 성공")
    fun findAccountByIdSuccess() {
        // given
        val user = mockk<User> {
            every { id } returns TEST_USER_ID
        }
        
        val account = mockk<Account> {
            every { id } returns TEST_ID
            every { amount } returns TEST_INITIAL_AMOUNT
            every { user.id } returns TEST_USER_ID
        }
        every { account.user.id } returns TEST_USER_ID
        
        every { accountRepository.findById(TEST_ID) } returns account
        
        // when
        val foundAccount = accountService.findById(TEST_ID)
        
        // then
        assertNotNull(foundAccount)
        assertEquals(TEST_ID, foundAccount.id)
        assertEquals(user.id, foundAccount.user.id)
        assertEquals(TEST_INITIAL_AMOUNT, foundAccount.amount)
        
        verify(exactly = 1) { accountRepository.findById(TEST_ID) }
    }
    
    @Test
    @DisplayName("유효한 금액으로 계좌 충전 성공")
    fun chargeAccountWithValidAmountSuccess() {
        // given
        val updatedAmount = TEST_INITIAL_AMOUNT + CHARGE_AMOUNT
        
        val updatedAccount = mockk<Account> {
            every { id } returns TEST_ID
            every { amount } returns updatedAmount
        }
        
        val account = mockk<Account> {
            every { id } returns TEST_ID
            every { amount } returns TEST_INITIAL_AMOUNT
            every { charge(CHARGE_AMOUNT) } returns updatedAccount
        }
        
        every { accountRepository.findById(TEST_ID) } returns account
        every { accountRepository.update(any(), any()) } returns updatedAccount
        
        // when
        val result = accountService.charge(AccountCommand.UpdateAccountCommand(TEST_ID, CHARGE_AMOUNT))
        
        // then
        assertEquals(updatedAmount, result.amount)
        
        verify(exactly = 1) { accountRepository.findById(TEST_ID) }
        verify(exactly = 1) { account.charge(CHARGE_AMOUNT) }
        verify(exactly = 1) { accountRepository.update(any(), any()) }
    }
    
    @Test
    @DisplayName("존재하지 않는 계좌 충전 시 예외 발생")
    fun chargeNonExistentAccountThrowsException() {
        // given
        every { accountRepository.findById(TEST_ID) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountService.charge(AccountCommand.UpdateAccountCommand(TEST_ID, CHARGE_AMOUNT))
        }
        
        assertTrue(exception.message!!.contains("계좌를 찾을 수 없습니다"))
        
        verify(exactly = 1) { accountRepository.findById(TEST_ID) }
        verify(exactly = 0) { accountRepository.update(any(), any()) }
    }
    
    @Test
    @DisplayName("유효한 금액으로 계좌 출금 성공")
    fun withdrawAccountWithValidAmountSuccess() {
        // given
        val updatedAmount = TEST_INITIAL_AMOUNT - WITHDRAW_AMOUNT
  
        val updatedAccount = mockk<Account> {
            every { id } returns TEST_ID
            every { amount } returns updatedAmount
        }
        
        val account = mockk<Account> {
            every { id } returns TEST_ID
            every { amount } returns TEST_INITIAL_AMOUNT
            every { withdraw(any()) } returns mockk {
                every { id } returns TEST_ID
                every { amount } returns updatedAmount
            }
        }

        every { accountRepository.findById(TEST_ID) } returns account
        every { accountRepository.update(any(), any()) } returns updatedAccount
        
        // when
        val result = accountService.withdraw(AccountCommand.UpdateAccountCommand(TEST_ID, WITHDRAW_AMOUNT))
        
        // then
        assertEquals(updatedAmount, result.amount)
        
        verify(exactly = 1) { accountRepository.findById(TEST_ID) }
        verify { account.withdraw(WITHDRAW_AMOUNT) }
        verify(exactly = 1) { accountRepository.update(any(), any()) }
    }
    
    @Test
    @DisplayName("존재하지 않는 계좌 출금 시 예외 발생")
    fun withdrawNonExistentAccountThrowsException() {
        // given
        every { accountRepository.findById(TEST_ID) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountService.withdraw(AccountCommand.UpdateAccountCommand(TEST_ID, WITHDRAW_AMOUNT))
        }
        
        assertTrue(exception.message!!.contains("계좌를 찾을 수 없습니다"))
        
        verify(exactly = 1) { accountRepository.findById(TEST_ID) }
        verify(exactly = 0) { accountRepository.update(any(), any()) }
    }
    
    @Test
    @DisplayName("계좌 삭제 성공")
    fun deleteAccountSuccess() {
        // given
        val mockUser = mockk<User>()
        val account = mockk<Account> {
            every { id } returns TEST_ID
            every { user } returns mockUser
        }
        
        every { accountRepository.findById(TEST_ID) } returns account
        every { accountRepository.delete(TEST_ID) } just Runs
        
        // when
        accountService.delete(TEST_ID)
        
        // then
        verify(exactly = 1) { accountRepository.delete(TEST_ID) }
    }
} 