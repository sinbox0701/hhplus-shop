package kr.hhplus.be.server.user

import kr.hhplus.be.server.domain.user.model.Account
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime
import io.mockk.mockk
import kr.hhplus.be.server.domain.user.model.User
class AccountUnitTest {
    val userId = 1L

    @Test
    @DisplayName("유효한 데이터로 Account 객체 생성 성공")
    fun createAccountWithValidData() {
        // given
        val initialAmount = BigDecimal("5000")

        // when
        val account = Account.create(userId, initialAmount)

        // then
        assertEquals(userId, account.userId)
        assertEquals(0, initialAmount.compareTo(account.amount))
        assertNotNull(account.createdAt)
        assertNotNull(account.updatedAt)
    }

    @Test
    @DisplayName("기본 금액으로 Account 객체 생성 성공")
    fun createAccountWithDefaultAmount() {
        // given
        val initialAmount = BigDecimal.ZERO

        // when
        val account = Account.create(userId, initialAmount)

        // then
        assertEquals(userId, account.userId)
        assertEquals(0, Account.MIN_BALANCE.compareTo(account.amount))
        assertNotNull(account.createdAt)
        assertNotNull(account.updatedAt)
    }

    @Test
    @DisplayName("최소 금액보다 작은 초기 금액으로 Account 생성 시 예외 발생")
    fun createAccountWithTooSmallInitialAmount() {
        // given
        val tooSmallAmount = BigDecimal("-100") // 최소 금액(0.0)보다 작음

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            Account.create(userId, tooSmallAmount)
        }

        assertTrue(exception.message!!.contains("Initial amount must be between"))
    }

    @Test
    @DisplayName("최대 금액보다 큰 초기 금액으로 Account 생성 시 예외 발생")
    fun createAccountWithTooLargeInitialAmount() {
        // given
        val tooLargeAmount = Account.MAX_BALANCE.add(BigDecimal("1000"))

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            Account.create(userId, tooLargeAmount)
        }

        assertTrue(exception.message!!.contains("Initial amount must be between"))
    }

    @Test
    @DisplayName("유효한 금액으로 계좌 충전 성공")
    fun chargeAccountWithValidAmount() {
        // given
        val account = Account.create(userId, BigDecimal("1000"))
        val chargeAmount = BigDecimal("5000")
        val expectedAmount = account.amount.add(chargeAmount)

        // when
        val chargedAccount = account.charge(chargeAmount)

        // then
        assertEquals(0, expectedAmount.compareTo(chargedAccount.amount))
        assertNotEquals(chargedAccount.createdAt, chargedAccount.updatedAt)
    }

    @Test
    @DisplayName("음수 금액으로 계좌 충전 시 예외 발생")
    fun chargeAccountWithNegativeAmount() {
        // given
        val account = Account.create(userId, BigDecimal("1000"))
        val negativeAmount = BigDecimal("-100")

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
        val account = Account.create(userId, BigDecimal("1000"))
        val tooLargeAmount = Account.MAX_TRANSACTION_AMOUNT.add(BigDecimal("1000"))

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
        val account = Account.create(userId, Account.MAX_BALANCE.subtract(BigDecimal("1000")))
        val chargeAmount = BigDecimal("2000") // 충전 후 잔액이 최대치를 초과

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
        val initialAmount = BigDecimal("30000")
        val account = Account.create(userId, initialAmount)
        val withdrawAmount = BigDecimal("1000")
        val expectedAmount = initialAmount.subtract(withdrawAmount)

        // when
        val withdrawnAccount = account.withdraw(withdrawAmount)

        // then
        assertEquals(0, expectedAmount.compareTo(withdrawnAccount.amount))
        assertNotEquals(withdrawnAccount.createdAt, withdrawnAccount.updatedAt)
    }

    @Test
    @DisplayName("음수 금액으로 계좌 출금 시 예외 발생")
    fun withdrawAccountWithNegativeAmount() {
        // given
        val account = Account.create(userId, BigDecimal("5000"))
        val negativeAmount = BigDecimal("-100")

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
        val account = Account.create(userId, Account.MAX_TRANSACTION_AMOUNT.add(BigDecimal("5000")))
        val tooLargeAmount = Account.MAX_TRANSACTION_AMOUNT.add(BigDecimal("1000"))

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
        val initialAmount = BigDecimal("5000")
        val account = Account.create(userId, initialAmount)
        val withdrawAmount = initialAmount.add(BigDecimal("1000")) // 잔액보다 큰 금액

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
        val initialAmount = Account.MIN_BALANCE.add(BigDecimal("100"))
        val account = Account.create(userId, initialAmount)
        val withdrawAmount = BigDecimal("200") // 출금 후 잔액이 최소치보다 작아짐

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            account.withdraw(withdrawAmount)
        }

        assertTrue(exception.message!!.contains("Insufficient funds"))
    }
}
