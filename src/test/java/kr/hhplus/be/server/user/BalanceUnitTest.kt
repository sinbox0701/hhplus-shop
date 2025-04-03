package kr.hhplus.be.server.user

import kr.hhplus.be.server.domain.user.Balance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class BalanceUnitTest {

    @Test
    fun `create Balance with valid initial amount`() {
        // Arrange
        val balanceId = 1
        val accountId = 100
        val initialAmount = BigDecimal("5000000") // 5,000,000원, 유효 범위 내

        // Act
        val balance = Balance.create(balanceId, accountId, initialAmount)

        // Assert
        assertEquals(initialAmount, balance.amount)
        assertEquals(balanceId, balance.balanceId)
        assertEquals(accountId, balance.accountId)
    }

    @Test
    fun `create Balance throws exception when initial amount is negative`() {
        // Arrange
        val balanceId = 1
        val accountId = 100
        val invalidInitialAmount = BigDecimal("-1")

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Balance.create(balanceId, accountId, invalidInitialAmount)
        }
        assertEquals("Initial amount must be between 0 and 10000000", exception.message)
    }

    @Test
    fun `create Balance throws exception when initial amount exceeds maximum`() {
        // Arrange
        val balanceId = 1
        val accountId = 100
        val invalidInitialAmount = BigDecimal("10000001") // 10,000,001원

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Balance.create(balanceId, accountId, invalidInitialAmount)
        }
        assertEquals("Initial amount must be between 0 and 10000000", exception.message)
    }
    
    @Test
    fun `create Balance with boundary values succeeds`() {
        // Arrange
        val balanceId = 1
        val accountId = 100
        val minAmount = BigDecimal.ZERO
        val maxAmount = BigDecimal("10000000")
        
        // Act & Assert - 최소값
        val minBalance = Balance.create(balanceId, accountId, minAmount)
        assertEquals(minAmount, minBalance.amount)
        
        // Act & Assert - 최대값
        val maxBalance = Balance.create(balanceId, accountId, maxAmount)
        assertEquals(maxAmount, maxBalance.amount)
    }

    @Test
    fun `charge valid amount updates balance`() {
        // Arrange
        val balance = Balance.create(1, 100, BigDecimal("5000000"))
        val chargeAmount = BigDecimal("100000") // 100,000원, 유효한 충전액 (1,000,000원 이하)
        val expectedAmount = BigDecimal("5100000") // 5,100,000원

        // Act
        balance.charge(chargeAmount)

        // Assert
        assertEquals(expectedAmount, balance.amount)
    }

    @Test
    fun `charge amount greater than maximum transaction amount throws exception`() {
        // Arrange
        val balance = Balance.create(1, 100, BigDecimal("5000000"))
        val invalidChargeAmount = BigDecimal("1000001") // 1,000,001원, 한 번에 충전 가능한 금액 초과

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balance.charge(invalidChargeAmount)
        }
        assertEquals("Charge amount cannot exceed 1000000", exception.message)
    }

    @Test
    fun `charge resulting in balance exceeding maximum throws exception`() {
        // Arrange
        val balance = Balance.create(1, 100, BigDecimal("9900000"))
        val chargeAmount = BigDecimal("200000") // 9900000 + 200000 = 10100000원, 최대 잔고 초과

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balance.charge(chargeAmount)
        }
        assertEquals("Resulting balance cannot exceed 10000000", exception.message)
    }
    
    @Test
    fun `charge with maximum allowed transaction amount succeeds`() {
        // Arrange
        val balance = Balance.create(1, 100, BigDecimal("5000000"))
        val maxChargeAmount = BigDecimal("1000000") // 1,000,000원 (최대 충전 가능 금액)
        val expectedAmount = BigDecimal("6000000") // 6,000,000원

        // Act
        balance.charge(maxChargeAmount)

        // Assert
        assertEquals(expectedAmount, balance.amount)
    }
    
    @Test
    fun `multiple consecutive charge operations succeed`() {
        // Arrange
        val balance = Balance.create(1, 100, BigDecimal("1000000"))
        val chargeAmount1 = BigDecimal("500000") // 첫 번째 충전: 500,000원
        val chargeAmount2 = BigDecimal("300000") // 두 번째 충전: 300,000원 
        val chargeAmount3 = BigDecimal("200000") // 세 번째 충전: 200,000원
        val expectedFinalAmount = BigDecimal("2000000") // 1,000,000 + 500,000 + 300,000 + 200,000 = 2,000,000원

        // Act
        balance.charge(chargeAmount1)
            .charge(chargeAmount2)
            .charge(chargeAmount3)

        // Assert
        assertEquals(expectedFinalAmount, balance.amount)
    }

    @Test
    fun `withdraw valid amount updates balance`() {
        // Arrange
        val balance = Balance.create(1, 100, BigDecimal("5000000"))
        val withdrawAmount = BigDecimal("100000") // 100,000원, 유효한 인출액 (1,000,000원 이하)
        val expectedAmount = BigDecimal("4900000") // 5,000,000 - 100,000 = 4,900,000원

        // Act
        balance.withdraw(withdrawAmount)

        // Assert
        assertEquals(expectedAmount, balance.amount)
    }

    @Test
    fun `withdraw amount greater than maximum transaction amount throws exception`() {
        // Arrange
        val balance = Balance.create(1, 100, BigDecimal("5000000"))
        val invalidWithdrawAmount = BigDecimal("1000001") // 1,000,001원, 한 번에 인출 가능한 금액 초과

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balance.withdraw(invalidWithdrawAmount)
        }
        assertEquals("Withdrawal amount cannot exceed 1000000", exception.message)
    }

    @Test
    fun `withdraw amount exceeding current balance throws exception`() {
        // Arrange
        val balance = Balance.create(1, 100, BigDecimal("500000"))
        val withdrawAmount = BigDecimal("600000") // 600,000원, 잔고 부족

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balance.withdraw(withdrawAmount)
        }
        assertEquals("Insufficient funds", exception.message)
    }
    
    @Test
    fun `withdraw with maximum allowed transaction amount succeeds`() {
        // Arrange
        val balance = Balance.create(1, 100, BigDecimal("5000000"))
        val maxWithdrawAmount = BigDecimal("1000000") // 1,000,000원 (최대 출금 가능 금액)
        val expectedAmount = BigDecimal("4000000") // 5,000,000 - 1,000,000 = 4,000,000원

        // Act
        balance.withdraw(maxWithdrawAmount)

        // Assert
        assertEquals(expectedAmount, balance.amount)
    }
    
    @Test
    fun `multiple consecutive withdraw operations succeed`() {
        // Arrange
        val balance = Balance.create(1, 100, BigDecimal("2000000"))
        val withdrawAmount1 = BigDecimal("500000") // 첫 번째 출금: 500,000원
        val withdrawAmount2 = BigDecimal("300000") // 두 번째 출금: 300,000원 
        val withdrawAmount3 = BigDecimal("200000") // 세 번째 출금: 200,000원
        val expectedFinalAmount = BigDecimal("1000000") // 2,000,000 - 500,000 - 300,000 - 200,000 = 1,000,000원

        // Act
        balance.withdraw(withdrawAmount1)
            .withdraw(withdrawAmount2)
            .withdraw(withdrawAmount3)

        // Assert
        assertEquals(expectedFinalAmount, balance.amount)
    }
    
    @Test
    fun `mixed charge and withdraw operations succeed`() {
        // Arrange
        val balance = Balance.create(1, 100, BigDecimal("1000000"))
        val chargeAmount1 = BigDecimal("500000") // 첫 번째 충전: 500,000원
        val withdrawAmount1 = BigDecimal("200000") // 첫 번째 출금: 200,000원
        val chargeAmount2 = BigDecimal("300000") // 두 번째 충전: 300,000원
        val withdrawAmount2 = BigDecimal("100000") // 두 번째 출금: 100,000원
        
        // 1,000,000 + 500,000 - 200,000 + 300,000 - 100,000 = 1,500,000원
        val expectedFinalAmount = BigDecimal("1500000")

        // Act
        balance.charge(chargeAmount1)
            .withdraw(withdrawAmount1)
            .charge(chargeAmount2)
            .withdraw(withdrawAmount2)

        // Assert
        assertEquals(expectedFinalAmount, balance.amount)
    }

    @Test
    fun `charge negative amount throws exception`() {
        // Arrange
        val balance = Balance.create(1, 100, BigDecimal("5000000"))
        val negativeAmount = BigDecimal("-100")

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balance.charge(negativeAmount)
        }
        assertEquals("Charge amount must be positive", exception.message)
    }

    @Test
    fun `withdraw negative amount throws exception`() {
        // Arrange
        val balance = Balance.create(1, 100, BigDecimal("5000000"))
        val negativeAmount = BigDecimal("-100")

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balance.withdraw(negativeAmount)
        }
        assertEquals("Withdrawal amount must be positive", exception.message)
    }

    @Test
    fun `charge zero amount throws exception`() {
        // Arrange
        val balance = Balance.create(1, 100, BigDecimal("5000000"))
        val zeroAmount = BigDecimal.ZERO

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balance.charge(zeroAmount)
        }
        assertEquals("Charge amount must be positive", exception.message)
    }

    @Test
    fun `withdraw zero amount throws exception`() {
        // Arrange
        val balance = Balance.create(1, 100, BigDecimal("5000000"))
        val zeroAmount = BigDecimal.ZERO

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balance.withdraw(zeroAmount)
        }
        assertEquals("Withdrawal amount must be positive", exception.message)
    }
    
    @Test
    fun `withdraw entire balance succeeds`() {
        // Arrange
        val initialAmount = BigDecimal("1000000")
        val balance = Balance.create(1, 100, initialAmount)
        
        // Act
        balance.withdraw(initialAmount)
        
        // Assert
        assertEquals(BigDecimal.ZERO, balance.amount)
    }
}