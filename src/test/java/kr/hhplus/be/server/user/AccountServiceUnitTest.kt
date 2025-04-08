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
        val account = Account.create(testId, testUserId, testInitialAmount)
        
        every { accountRepository.save(any()) } returns account
        
        // when
        val createdAccount = accountService.create(testUserId, testInitialAmount)
        
        // then
        assertEquals(testUserId, createdAccount.userId)
        assertEquals(testInitialAmount, createdAccount.amount)
        
        verify(exactly = 1) { accountRepository.save(any()) }
    }
    
    @Test
    @DisplayName("기본 금액으로 계좌 생성 성공")
    fun createAccountWithDefaultAmount() {
        // given
        val account = Account.create(testId, testUserId, Account.MIN_BALANCE)
        
        every { accountRepository.save(any()) } returns account
        
        // when
        val createdAccount = accountService.create(testUserId)
        
        // then
        assertEquals(testUserId, createdAccount.userId)
        assertEquals(Account.MIN_BALANCE, createdAccount.amount)
        
        verify(exactly = 1) { accountRepository.save(any()) }
    }
    
    @Test
    @DisplayName("사용자 ID로 계좌 조회 성공")
    fun findAccountByUserIdSuccess() {
        // given
        val account = Account.create(testId, testUserId, testInitialAmount)
        
        every { accountRepository.findByUserId(testUserId) } returns account
        
        // when
        val foundAccount = accountService.findByUserId(testUserId)
        
        // then
        assertEquals(testId, foundAccount.id)
        assertEquals(testUserId, foundAccount.userId)
        assertEquals(testInitialAmount, foundAccount.amount)
        
        verify(exactly = 1) { accountRepository.findByUserId(testUserId) }
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자 ID로 계좌 조회 시 예외 발생")
    fun findAccountByNonExistentUserIdThrowsException() {
        // given
        every { accountRepository.findByUserId(testUserId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountService.findByUserId(testUserId)
        }
        
        assertTrue(exception.message!!.contains("계좌를 찾을 수 없습니다"))
        
        verify(exactly = 1) { accountRepository.findByUserId(testUserId) }
    }
    
    @Test
    @DisplayName("계좌 ID로 계좌 조회 성공")
    fun findAccountByIdSuccess() {
        // given
        val account = Account.create(testId, testUserId, testInitialAmount)
        
        every { accountRepository.findById(testId) } returns account
        
        // when
        val foundAccount = accountService.findById(testId)
        
        // then
        assertNotNull(foundAccount)
        assertEquals(testId, foundAccount!!.id)
        assertEquals(testUserId, foundAccount.userId)
        assertEquals(testInitialAmount, foundAccount.amount)
        
        verify(exactly = 1) { accountRepository.findById(testId) }
    }
    
    @Test
    @DisplayName("유효한 금액으로 계좌 충전 성공")
    fun chargeAccountWithValidAmountSuccess() {
        // given
        val account = Account.create(testId, testUserId, testInitialAmount)
        val chargeAmount = 3000.0
        val updatedAmount = testInitialAmount + chargeAmount
        val chargedAccount = account.charge(chargeAmount)
        
        every { accountRepository.findById(testId) } returns account
        every { accountRepository.update(testId, updatedAmount) } returns chargedAccount
        
        // when
        val result = accountService.charge(testId, chargeAmount)
        
        // then
        assertEquals(updatedAmount, result.amount)
        
        verify(exactly = 1) { accountRepository.findById(testId) }
        verify(exactly = 1) { accountRepository.update(testId, updatedAmount) }
    }
    
    @Test
    @DisplayName("존재하지 않는 계좌 충전 시 예외 발생")
    fun chargeNonExistentAccountThrowsException() {
        // given
        val chargeAmount = 3000.0
        
        every { accountRepository.findById(testId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountService.charge(testId, chargeAmount)
        }
        
        assertTrue(exception.message!!.contains("계좌를 찾을 수 없습니다"))
        
        verify(exactly = 1) { accountRepository.findById(testId) }
        verify(exactly = 0) { accountRepository.update(any(), any()) }
    }
    
    @Test
    @DisplayName("최대 거래 금액을 초과하는 금액으로 충전 시 예외 발생")
    fun chargeWithAmountExceedingMaxTransactionAmountThrowsException() {
        // given
        val account = Account.create(testId, testUserId, testInitialAmount)
        val invalidChargeAmount = Account.MAX_TRANSACTION_AMOUNT + 1000.0
        
        every { accountRepository.findById(testId) } returns account
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountService.charge(testId, invalidChargeAmount)
        }
        
        assertTrue(exception.message!!.contains("Charge amount cannot exceed"))
        
        verify(exactly = 1) { accountRepository.findById(testId) }
        verify(exactly = 0) { accountRepository.update(any(), any()) }
    }
    
    @Test
    @DisplayName("유효한 금액으로 계좌 출금 성공")
    fun withdrawAccountWithValidAmountSuccess() {
        // given
        val account = Account.create(testId, testUserId, testInitialAmount)
        val withdrawAmount = 2000.0
        val updatedAmount = testInitialAmount - withdrawAmount
        val withdrawnAccount = account.withdraw(withdrawAmount)
        
        every { accountRepository.findById(testId) } returns account
        every { accountRepository.update(testId, updatedAmount) } returns withdrawnAccount
        
        // when
        val result = accountService.withdraw(testId, withdrawAmount)
        
        // then
        assertEquals(updatedAmount, result.amount)
        
        verify(exactly = 1) { accountRepository.findById(testId) }
        verify(exactly = 1) { accountRepository.update(testId, updatedAmount) }
    }
    
    @Test
    @DisplayName("존재하지 않는 계좌 출금 시 예외 발생")
    fun withdrawNonExistentAccountThrowsException() {
        // given
        val withdrawAmount = 2000.0
        
        every { accountRepository.findById(testId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountService.withdraw(testId, withdrawAmount)
        }
        
        assertTrue(exception.message!!.contains("계좌를 찾을 수 없습니다"))
        
        verify(exactly = 1) { accountRepository.findById(testId) }
        verify(exactly = 0) { accountRepository.update(any(), any()) }
    }
    
    @Test
    @DisplayName("잔액보다 큰 금액 출금 시 예외 발생")
    fun withdrawWithAmountExceedingBalanceThrowsException() {
        // given
        val account = Account.create(testId, testUserId, testInitialAmount)
        val invalidWithdrawAmount = testInitialAmount + 1000.0 // 잔액보다 큰 금액
        
        every { accountRepository.findById(testId) } returns account
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountService.withdraw(testId, invalidWithdrawAmount)
        }
        
        assertTrue(exception.message!!.contains("Insufficient funds"))
        
        verify(exactly = 1) { accountRepository.findById(testId) }
        verify(exactly = 0) { accountRepository.update(any(), any()) }
    }
    
    @Test
    @DisplayName("최대 거래 금액을 초과하는 금액으로 출금 시 예외 발생")
    fun withdrawWithAmountExceedingMaxTransactionAmountThrowsException() {
        // given
        val account = Account.create(testId, testUserId, Account.MAX_TRANSACTION_AMOUNT * 2) // 충분한 잔액
        val invalidWithdrawAmount = Account.MAX_TRANSACTION_AMOUNT + 1000.0
        
        every { accountRepository.findById(testId) } returns account
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountService.withdraw(testId, invalidWithdrawAmount)
        }
        
        assertTrue(exception.message!!.contains("Withdrawal amount cannot exceed"))
        
        verify(exactly = 1) { accountRepository.findById(testId) }
        verify(exactly = 0) { accountRepository.update(any(), any()) }
    }
    
    @Test
    @DisplayName("계좌 삭제 성공")
    fun deleteAccountSuccess() {
        // given
        every { accountRepository.delete(testId) } just Runs
        
        // when
        accountService.delete(testId)
        
        // then
        verify(exactly = 1) { accountRepository.delete(testId) }
    }
} 