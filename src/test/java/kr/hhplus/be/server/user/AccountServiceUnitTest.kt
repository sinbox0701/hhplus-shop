package kr.hhplus.be.server.user

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
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
class AccountServiceUnitTest {

    @MockK
    private lateinit var accountRepository: AccountRepository

    @InjectMockKs
    private lateinit var accountService: AccountService

    private val testId = 1L
    private val testUserId = 1L
    private val testInitialAmount = 5000.0

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    @DisplayName("새로운 계좌 생성 성공")
    fun createAccountSuccess() {
        // given
        val user = mockk<User>()
        val account = Account.create(user, testInitialAmount)
        
        every { accountRepository.save(any()) } returns account
        
        // when
        val createdAccount = accountService.create(AccountCommand.CreateAccountCommand(user, testInitialAmount))
        
        // then
        assertEquals(user, createdAccount.user)
        assertEquals(testInitialAmount, createdAccount.amount)
        
        verify(exactly = 1) { accountRepository.save(any()) }
    }
    
    @Test
    @DisplayName("기본 금액으로 계좌 생성 성공")
    fun createAccountWithDefaultAmount() {
        // given
        val user = mockk<User>()
        val account = Account.create(user, Account.MIN_BALANCE)
        
        every { accountRepository.save(any()) } returns account
        
        // when
        val createdAccount = accountService.create(AccountCommand.CreateAccountCommand(user, Account.MIN_BALANCE))
        
        // then
        assertEquals(user, createdAccount.user)
        assertEquals(Account.MIN_BALANCE, createdAccount.amount)
        
        verify(exactly = 1) { accountRepository.save(any()) }
    }
    
    @Test
    @DisplayName("사용자 ID로 계좌 조회 성공")
    fun findAccountByUserIdSuccess() {
        // given
        val user = mockk<User>()
        val account = Account.create(user, testInitialAmount)
        
        every { accountRepository.findByUserId(user.id!!) } returns account
        
        // when
        val foundAccount = accountService.findByUserId(user.id!!)
        
        // then
        assertEquals(testId, foundAccount.id)
        assertEquals(user, foundAccount.user)
        assertEquals(testInitialAmount, foundAccount.amount)
        
        verify(exactly = 1) { accountRepository.findByUserId(testUserId) }
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자 ID로 계좌 조회 시 예외 발생")
    fun findAccountByNonExistentUserIdThrowsException() {
        // given
        val user = mockk<User>()
        every { accountRepository.findByUserId(user.id!!) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountService.findByUserId(testUserId)
        }
        
        assertTrue(exception.message!!.contains("계좌를 찾을 수 없습니다"))
        
        verify(exactly = 1) { accountRepository.findByUserId(user.id!!) }
    }
    
    @Test
    @DisplayName("계좌 ID로 계좌 조회 성공")
    fun findAccountByIdSuccess() {
        // given
        val user = mockk<User>()
        val account = Account.create(user, testInitialAmount)
        
        every { accountRepository.findById(account.id!!) } returns account
        
        // when
        val foundAccount = accountService.findById(account.id!!)
        
        // then
        assertNotNull(foundAccount)
        assertEquals(account.id!!, foundAccount.id)
        assertEquals(user, foundAccount.user)
        assertEquals(testInitialAmount, foundAccount.amount)
        
        verify(exactly = 1) { accountRepository.findById(account.id!!) }
    }
    
    @Test
    @DisplayName("유효한 금액으로 계좌 충전 성공")
    fun chargeAccountWithValidAmountSuccess() {
        // given
        val user = mockk<User>()
        val account = Account.create(user, testInitialAmount)
        val chargeAmount = 3000.0
        val updatedAmount = testInitialAmount + chargeAmount
        val chargedAccount = account.charge(chargeAmount)
        
        every { accountRepository.findById(account.id!!) } returns account
        every { accountRepository.update(account.id!!, updatedAmount) } returns chargedAccount
        
        // when
        val result = accountService.charge(AccountCommand.UpdateAccountCommand(account.id!!, updatedAmount))
        
        // then
        assertEquals(updatedAmount, result.amount)
        
        verify(exactly = 1) { accountRepository.findById(account.id!!) }
        verify(exactly = 1) { accountRepository.update(account.id!!, updatedAmount) }
    }
    
    @Test
    @DisplayName("존재하지 않는 계좌 충전 시 예외 발생")
    fun chargeNonExistentAccountThrowsException() {
        // given
        val chargeAmount = 3000.0
        val user = mockk<User>()
        val account = Account.create(user, testInitialAmount)
        
        every { accountRepository.findById(account.id!!) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountService.charge(AccountCommand.UpdateAccountCommand(account.id!!, chargeAmount))
        }
        
        assertTrue(exception.message!!.contains("계좌를 찾을 수 없습니다"))
        
        verify(exactly = 1) { accountRepository.findById(testId) }
        verify(exactly = 0) { accountRepository.update(any(), any()) }
    }
    
    @Test
    @DisplayName("최대 거래 금액을 초과하는 금액으로 충전 시 예외 발생")
    fun chargeWithAmountExceedingMaxTransactionAmountThrowsException() {
        // given
        val user = mockk<User>()
        val account = Account.create(user, testInitialAmount)
        val invalidChargeAmount = Account.MAX_TRANSACTION_AMOUNT + 1000.0
        
        every { accountRepository.findById(account.id!!) } returns account
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountService.charge(AccountCommand.UpdateAccountCommand(account.id!!, invalidChargeAmount))
        }
        
        assertTrue(exception.message!!.contains("Charge amount cannot exceed"))
        
        verify(exactly = 1) { accountRepository.findById(account.id!!) }
        verify(exactly = 0) { accountRepository.update(any(), any()) }
    }
    
    @Test
    @DisplayName("유효한 금액으로 계좌 출금 성공")
    fun withdrawAccountWithValidAmountSuccess() {
        // given
        val user = mockk<User>()
        val account = Account.create(user, testInitialAmount)
        val withdrawAmount = 2000.0
        val updatedAmount = testInitialAmount - withdrawAmount
        val withdrawnAccount = account.withdraw(withdrawAmount)
        
        every { accountRepository.findById(account.id!!) } returns account
        every { accountRepository.update(account.id!!, updatedAmount) } returns withdrawnAccount
        
        // when
        val result = accountService.withdraw(AccountCommand.UpdateAccountCommand(account.id!!, withdrawAmount))
        
        // then
        assertEquals(updatedAmount, result.amount)
        
        verify(exactly = 1) { accountRepository.findById(account.id!!) }
        verify(exactly = 1) { accountRepository.update(account.id!!, updatedAmount) }
    }
    
    @Test
    @DisplayName("존재하지 않는 계좌 출금 시 예외 발생")
    fun withdrawNonExistentAccountThrowsException() {
        // given
        val withdrawAmount = 2000.0
        val user = mockk<User>()
        val account = Account.create(user, testInitialAmount)
        
        every { accountRepository.findById(account.id!!) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountService.withdraw(AccountCommand.UpdateAccountCommand(account.id!!, withdrawAmount))
        }
        
        assertTrue(exception.message!!.contains("계좌를 찾을 수 없습니다"))
        
        verify(exactly = 1) { accountRepository.findById(testId) }
        verify(exactly = 0) { accountRepository.update(any(), any()) }
    }
    
    @Test
    @DisplayName("잔액보다 큰 금액 출금 시 예외 발생")
    fun withdrawWithAmountExceedingBalanceThrowsException() {
        // given
        val user = mockk<User>()
        val account = Account.create(user, testInitialAmount)
        val invalidWithdrawAmount = testInitialAmount + 1000.0 // 잔액보다 큰 금액
        
        every { accountRepository.findById(testId) } returns account
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountService.withdraw(AccountCommand.UpdateAccountCommand(account.id!!, invalidWithdrawAmount))
        }
        
        assertTrue(exception.message!!.contains("Insufficient funds"))
        
        verify(exactly = 1) { accountRepository.findById(account.id!!) }
        verify(exactly = 0) { accountRepository.update(any(), any()) }
    }
    
    @Test
    @DisplayName("최대 거래 금액을 초과하는 금액으로 출금 시 예외 발생")
    fun withdrawWithAmountExceedingMaxTransactionAmountThrowsException() {
        // given
        val user = mockk<User>()
        val account = Account.create(user, Account.MAX_TRANSACTION_AMOUNT * 2) // 충분한 잔액
        val invalidWithdrawAmount = Account.MAX_TRANSACTION_AMOUNT + 1000.0
        
        every { accountRepository.findById(account.id!!) } returns account
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountService.withdraw(AccountCommand.UpdateAccountCommand(account.id!!, invalidWithdrawAmount))
        }
        
        assertTrue(exception.message!!.contains("Withdrawal amount cannot exceed"))
        
        verify(exactly = 1) { accountRepository.findById(account.id!!) }
        verify(exactly = 0) { accountRepository.update(any(), any()) }
    }
    
    @Test
    @DisplayName("계좌 삭제 성공")
    fun deleteAccountSuccess() {
        // given
        val user = mockk<User>()
        val account = Account.create(user, testInitialAmount)
        every { accountRepository.delete(account.id!!) } just Runs
        
        // when
        accountService.delete(account.id!!)
        
        // then
        verify(exactly = 1) { accountRepository.delete(account.id!!) }
    }
} 