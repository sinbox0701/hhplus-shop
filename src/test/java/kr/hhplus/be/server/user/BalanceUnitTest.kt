package kr.hhplus.be.server.user

import kr.hhplus.be.server.domain.user.Balance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BalanceUnitTest {

    @Test
    fun `create Balance with valid initial amount`() {
        // Arrange
        val balanceId = 1L
        val accountId = 100L
        val initialAmount = 5000000.0 // 5,000,000원, 유효 범위 내

        // Act
        val balance = Balance.create(balanceId, accountId, initialAmount)

        // Assert
        assertEquals(initialAmount, balance.amount)
        assertEquals(balanceId, balance.id)
        assertEquals(accountId, balance.accountId)
    }

    @Test
    fun `create Balance throws exception when initial amount is negative`() {
        // Arrange
        val balanceId = 1L
        val accountId = 100L
        val invalidInitialAmount = -1.0

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Balance.create(balanceId, accountId, invalidInitialAmount)
        }
        assertEquals("Initial amount must be between 0 and 10000000", exception.message)
    }

    @Test
    fun `create Balance throws exception when initial amount exceeds maximum`() {
        // Arrange
        val balanceId = 1L
        val accountId = 100L
        val invalidInitialAmount = 10000001.0

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Balance.create(balanceId, accountId, invalidInitialAmount)
        }
        assertEquals("Initial amount must be between 0 and 10000000", exception.message)
    }
    
    @Test
    fun `create Balance with boundary values succeeds`() {
        // Arrange
        val balanceId = 1L
        val accountId = 100L
        val minAmount = 0.0
        val maxAmount = 10000000.0
        
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
        val balance = Balance.create(1L, 100L, 5000000.0)
        val chargeAmount = 100000.0 // 100,000원, 유효한 충전액 (1,000,000원 이하)
        val expectedAmount = 5100000.0 // 5,100,000원

        // Act
        balance.charge(chargeAmount)

        // Assert
        assertEquals(expectedAmount, balance.amount)
    }

    @Test
    fun `charge amount greater than maximum transaction amount throws exception`() {
        // Arrange
        val balance = Balance.create(1L, 100L, 5000000.0)
        val invalidChargeAmount = 1000001.0 // 1,000,001원, 한 번에 충전 가능한 금액 초과

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balance.charge(invalidChargeAmount)
        }
        assertEquals("Charge amount cannot exceed 1000000", exception.message)
    }

    @Test
    fun `charge resulting in balance exceeding maximum throws exception`() {
        // Arrange
        val balance = Balance.create(1L, 100L, 9900000.0)
        val chargeAmount = 200000.0 // 9900000 + 200000 = 10100000원, 최대 잔고 초과

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balance.charge(chargeAmount)
        }
        assertEquals("Resulting balance cannot exceed 10000000", exception.message)
    }
    
    @Test
    fun `charge with maximum allowed transaction amount succeeds`() {
        // Arrange
        val balance = Balance.create(1L, 100L, 5000000.0)
        val maxChargeAmount = 1000000.0 // 1,000,000원 (최대 충전 가능 금액)
        val expectedAmount = 6000000.0 // 6,000,000원

        // Act
        balance.charge(maxChargeAmount)

        // Assert
        assertEquals(expectedAmount, balance.amount)
    }
    
    @Test
    fun `multiple consecutive charge operations succeed`() {
        // Arrange
        val balance = Balance.create(1L, 100L, 1000000.0)
        val chargeAmount1 = 500000.0 // 첫 번째 충전: 500,000원
        val chargeAmount2 = 300000.0 // 두 번째 충전: 300,000원 
        val chargeAmount3 = 200000.0 // 세 번째 충전: 200,000원
        val expectedFinalAmount = 2000000.0 // 1,000,000 + 500,000 + 300,000 + 200,000 = 2,000,000원

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
        val balance = Balance.create(1L, 100L, 5000000.0)
        val withdrawAmount = 100000.0 // 100,000원, 유효한 인출액 (1,000,000원 이하)
        val expectedAmount = 4900000.0 // 5,000,000 - 100,000 = 4,900,000원

        // Act
        balance.withdraw(withdrawAmount)

        // Assert
        assertEquals(expectedAmount, balance.amount)
    }

    @Test
    fun `withdraw amount greater than maximum transaction amount throws exception`() {
        // Arrange
        val balance = Balance.create(1L, 100L, 5000000.0)
        val invalidWithdrawAmount = 1000001.0 // 1,000,001원, 한 번에 인출 가능한 금액 초과

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balance.withdraw(invalidWithdrawAmount)
        }
        assertEquals("Withdrawal amount cannot exceed 1000000", exception.message)
    }

    @Test
    fun `withdraw amount exceeding current balance throws exception`() {
        // Arrange
        val balance = Balance.create(1L, 100L, 500000.0)
        val withdrawAmount = 600000.0 // 600,000원, 잔고 부족

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balance.withdraw(withdrawAmount)
        }
        assertEquals("Insufficient funds", exception.message)
    }
    
    @Test
    fun `withdraw with maximum allowed transaction amount succeeds`() {
        // Arrange
        val balance = Balance.create(1L, 100L, 5000000.0)
        val maxWithdrawAmount = 1000000.0 // 1,000,000원 (최대 출금 가능 금액)
        val expectedAmount = 4000000.0 // 5,000,000 - 1,000,000 = 4,000,000원

        // Act
        balance.withdraw(maxWithdrawAmount)

        // Assert
        assertEquals(expectedAmount, balance.amount)
    }
    
    @Test
    fun `multiple consecutive withdraw operations succeed`() {
        // Arrange
        val balance = Balance.create(1L, 100L, 2000000.0)
        val withdrawAmount1 = 500000.0 // 첫 번째 출금: 500,000원
        val withdrawAmount2 = 300000.0 // 두 번째 출금: 300,000원 
        val withdrawAmount3 = 200000.0 // 세 번째 출금: 200,000원
        val expectedFinalAmount = 1000000.0 // 2,000,000 - 500,000 - 300,000 - 200,000 = 1,000,000원

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
        val balance = Balance.create(1L, 100L, 1000000.0)
        val chargeAmount1 = 500000.0 // 첫 번째 충전: 500,000원
        val withdrawAmount1 = 200000.0 // 첫 번째 출금: 200,000원
        val chargeAmount2 = 300000.0 // 두 번째 충전: 300,000원
        val withdrawAmount2 = 100000.0 // 두 번째 출금: 100,000원
        
        // 1,000,000 + 500,000 - 200,000 + 300,000 - 100,000 = 1,500,000원
        val expectedFinalAmount = 1500000.0

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
        val balance = Balance.create(1L, 100L, 5000000.0)
        val negativeAmount = -100.0

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balance.charge(negativeAmount)
        }
        assertEquals("Charge amount must be positive", exception.message)
    }

    @Test
    fun `withdraw negative amount throws exception`() {
        // Arrange
        val balance = Balance.create(1L, 100L, 5000000.0)
        val negativeAmount = -100.0

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balance.withdraw(negativeAmount)
        }
        assertEquals("Withdrawal amount must be positive", exception.message)
    }

    @Test
    fun `charge zero amount throws exception`() {
        // Arrange
        val balance = Balance.create(1L, 100L, 5000000.0)
        val zeroAmount = 0.0

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balance.charge(zeroAmount)
        }
        assertEquals("Charge amount must be positive", exception.message)
    }

    @Test
    fun `withdraw zero amount throws exception`() {
        // Arrange
        val balance = Balance.create(1L, 100L, 5000000.0)
        val zeroAmount = 0.0

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balance.withdraw(zeroAmount)
        }
        assertEquals("Withdrawal amount must be positive", exception.message)
    }
    
    @Test
    fun `withdraw entire balance succeeds`() {
        // Arrange
        val initialAmount = 1000000.0
        val balance = Balance.create(1L, 100L, initialAmount)
        
        // Act
        balance.withdraw(initialAmount)
        
        // Assert
        assertEquals(0.0, balance.amount)
    }
}