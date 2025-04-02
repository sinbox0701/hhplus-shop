package kr.hhplus.be.server.balance

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.repository.balance.BalanceRepository
import kr.hhplus.be.server.service.balance.BalanceService
import org.junit.jupiter.api.BeforeEach
import kr.hhplus.be.server.domain.balance.Balance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class BalanceServiceUnitTest {
    private lateinit var balanceRepository: BalanceRepository
    private lateinit var balanceService: BalanceService

    @BeforeEach
    fun setUp() {
        balanceRepository = mockk()
        balanceService = BalanceService(balanceRepository)
    }

    @Test
    fun `accountId로 잔액 조회 성공`() {
        // Arrange
        val accountId = 1
        val balance = Balance.create(balanceId = 1, accountId = accountId, initialAmount = BigDecimal("100.00"))
        every { balanceRepository.findByAccountId(accountId) } returns balance

        // Act
        val result = balanceService.getByAccountId(accountId)

        // Assert
        assertEquals(balance, result)
        verify(exactly = 1) { balanceRepository.findByAccountId(accountId) }
    }

    @Test
    fun `accountId로 잔액 조회 실패`() {
        // Arrange
        val accountId = 1
        every { balanceRepository.findByAccountId(accountId) } returns null

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balanceService.getByAccountId(accountId)
        }
        assertEquals("Balance not found for account: $accountId", exception.message)
        verify(exactly = 1) { balanceRepository.findByAccountId(accountId) }
    }

    @Test
    fun `잔액 충전 시 업데이트된 잔액 반환`() {
        // Arrange
        val accountId = 1
        val initialAmount = BigDecimal("100.00")
        val chargeAmount = BigDecimal("50.00")
        val expectedAmount = BigDecimal("150.00")
        val balance = Balance.create(balanceId = 1, accountId = accountId, initialAmount = initialAmount)

        every { balanceRepository.findByAccountId(accountId) } returns balance
        every { balanceRepository.saveAmount(any()) } answers { firstArg() }

        // Act
        val updatedBalance = balanceService.charge(accountId, chargeAmount)

        // Assert
        assertEquals(expectedAmount, updatedBalance.amount)
        verify(exactly = 1) { balanceRepository.findByAccountId(accountId) }
        verify(exactly = 1) { balanceRepository.saveAmount(match { it.amount == expectedAmount }) }
    }

    @Test
    fun `출금 시 잔액이 충분하면 정상적으로 차감된다`() {
        // Arrange
        val accountId = 1
        val initialAmount = BigDecimal("200.00")
        val withdrawAmount = BigDecimal("50.00")
        val expectedAmount = BigDecimal("150.00")
        val balance = Balance.create(balanceId = 1, accountId = accountId, initialAmount = initialAmount)

        every { balanceRepository.findByAccountId(accountId) } returns balance
        every { balanceRepository.saveAmount(any()) } answers { firstArg() }

        // Act
        val updatedBalance = balanceService.withdraw(accountId, withdrawAmount)

        // Assert
        assertEquals(expectedAmount, updatedBalance.amount)
        verify(exactly = 1) { balanceRepository.findByAccountId(accountId) }
        verify(exactly = 1) { balanceRepository.saveAmount(match { it.amount == expectedAmount }) }
    }

    @Test
    fun `출금 시 잔액 부족이면 예외 발생`() {
        // Arrange
        val accountId = 1
        val initialAmount = BigDecimal("100.00")
        val withdrawAmount = BigDecimal("150.00")
        val balance = Balance.create(balanceId = 1, accountId = accountId, initialAmount = initialAmount)

        every { balanceRepository.findByAccountId(accountId) } returns balance

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balanceService.withdraw(accountId, withdrawAmount)
        }
        assertEquals("Insufficient funds", exception.message)
        verify(exactly = 1) { balanceRepository.findByAccountId(accountId) }
    }
}