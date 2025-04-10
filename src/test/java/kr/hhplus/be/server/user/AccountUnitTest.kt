package kr.hhplus.be.server.user

import kr.hhplus.be.server.domain.user.model.Account
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import io.mockk.mockk
import kr.hhplus.be.server.domain.user.model.User
class AccountUnitTest {
    
    @Test
    @DisplayName("유효한 데이터로 Account 객체 생성 성공")
    fun createAccountWithValidData() {
        // given
        val user = mockk<User>()
        val initialAmount = 5000.0
        
        // when
        val account = Account.create(user, initialAmount)
        
        // then
        assertEquals(user, account.user)
        assertEquals(initialAmount, account.amount)
        assertNotNull(account.createdAt)
        assertNotNull(account.updatedAt)
    }
    
    @Test
    @DisplayName("기본 금액으로 Account 객체 생성 성공")
    fun createAccountWithDefaultAmount() {
        // given
        val user = mockk<User>()
        val initialAmount = 0.0
        
        // when
        val account = Account.create(user, initialAmount)
        
        // then
        assertEquals(user, account.user)
        assertEquals(Account.MIN_BALANCE, account.amount)
        assertNotNull(account.createdAt)
        assertNotNull(account.updatedAt)
    }
    
    @Test
    @DisplayName("최소 금액보다 작은 초기 금액으로 Account 생성 시 예외 발생")
    fun createAccountWithTooSmallInitialAmount() {
        // given
        val user = mockk<User>()
        val tooSmallAmount = -100.0 // 최소 금액(0.0)보다 작음
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            Account.create(user, tooSmallAmount)
        }
        
        assertTrue(exception.message!!.contains("Initial amount must be between"))
    }
    
    @Test
    @DisplayName("최대 금액보다 큰 초기 금액으로 Account 생성 시 예외 발생")
    fun createAccountWithTooLargeInitialAmount() {
        // given
        val user = mockk<User>()
        val tooLargeAmount = Account.MAX_BALANCE + 1000.0
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            Account.create(user, tooLargeAmount)
        }
        
        assertTrue(exception.message!!.contains("Initial amount must be between"))
    }
    
    @Test
    @DisplayName("유효한 금액으로 계좌 충전 성공")
    fun chargeAccountWithValidAmount() {
        // given
        val account = Account.create(mockk<User>(), 1000.0)
        val chargeAmount = 5000.0
        val expectedAmount = account.amount + chargeAmount
        
        // when
        val chargedAccount = account.charge(chargeAmount)
        
        // then
        assertEquals(expectedAmount, chargedAccount.amount)
        assertNotEquals(chargedAccount.createdAt, chargedAccount.updatedAt)
    }
    
    @Test
    @DisplayName("음수 금액으로 계좌 충전 시 예외 발생")
    fun chargeAccountWithNegativeAmount() {
        // given
        val account = Account.create(mockk<User>(), 1000.0)
        val negativeAmount = -100.0
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            account.charge(negativeAmount)
        }
        
        assertTrue(exception.message!!.contains("Charge amount must be positive"))
    }
    
    @Test
    @DisplayName("최대 거래 금액보다 큰 금액으로 계좌 충전 시 예외 발생")
    fun chargeAccountWithExceedingTransactionAmount() {
        // given
        val account = Account.create(mockk<User>(), 1000.0)
        val tooLargeAmount = Account.MAX_TRANSACTION_AMOUNT + 1000.0
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            account.charge(tooLargeAmount)
        }
        
        assertTrue(exception.message!!.contains("Charge amount cannot exceed"))
    }
    
    @Test
    @DisplayName("충전 후 최대 잔액을 초과하는 경우 예외 발생")
    fun chargeAccountResultingInExceedingMaxBalance() {
        // given
        val account = Account.create(mockk<User>(), Account.MAX_BALANCE - 1000.0)
        val chargeAmount = 2000.0 // 충전 후 잔액이 최대치를 초과
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            account.charge(chargeAmount)
        }
        
        assertTrue(exception.message!!.contains("Resulting balance cannot exceed"))
    }
    
    @Test
    @DisplayName("유효한 금액으로 계좌 출금 성공")
    fun withdrawAccountWithValidAmount() {
        // given
        val initialAmount = 10000.0
        val account = Account.create(mockk<User>(), initialAmount)
        val withdrawAmount = 3000.0
        val expectedAmount = initialAmount - withdrawAmount
        
        // when
        val withdrawnAccount = account.withdraw(withdrawAmount)
        
        // then
        assertEquals(expectedAmount, withdrawnAccount.amount)
        assertNotEquals(withdrawnAccount.createdAt, withdrawnAccount.updatedAt)
    }
    
    @Test
    @DisplayName("음수 금액으로 계좌 출금 시 예외 발생")
    fun withdrawAccountWithNegativeAmount() {
        // given
        val account = Account.create(mockk<User>(), 5000.0)
        val negativeAmount = -100.0
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            account.withdraw(negativeAmount)
        }
        
        assertTrue(exception.message!!.contains("Withdrawal amount must be positive"))
    }
    
    @Test
    @DisplayName("최대 거래 금액보다 큰 금액으로 계좌 출금 시 예외 발생")
    fun withdrawAccountWithExceedingTransactionAmount() {
        // given
        val account = Account.create(mockk<User>(), Account.MAX_TRANSACTION_AMOUNT + 5000.0)
        val tooLargeAmount = Account.MAX_TRANSACTION_AMOUNT + 1000.0
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            account.withdraw(tooLargeAmount)
        }
        
        assertTrue(exception.message!!.contains("Withdrawal amount cannot exceed"))
    }
    
    @Test
    @DisplayName("잔액보다 큰 금액으로 계좌 출금 시 예외 발생")
    fun withdrawAccountWithInsufficientFunds() {
        // given
        val initialAmount = 5000.0
        val account = Account.create(mockk<User>(), initialAmount)
        val withdrawAmount = initialAmount + 1000.0 // 잔액보다 큰 금액
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            account.withdraw(withdrawAmount)
        }
        
        assertTrue(exception.message!!.contains("Insufficient funds"))
    }
    
    @Test
    @DisplayName("출금 후 잔액이 최소 잔액보다 작아지는 경우 예외 발생")
    fun withdrawAccountResultingInBelowMinBalance() {
        // given
        val initialAmount = Account.MIN_BALANCE + 100.0
        val account = Account.create(mockk<User>(), initialAmount)
        val withdrawAmount = 200.0 // 출금 후 잔액이 최소치보다 작아짐
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            account.withdraw(withdrawAmount)
        }
        
        assertTrue(exception.message!!.contains("Resulting balance cannot be negative"))
    }
}