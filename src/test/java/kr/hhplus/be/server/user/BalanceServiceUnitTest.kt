package kr.hhplus.be.server.user

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.slot
import io.mockk.throws
import kr.hhplus.be.server.repository.user.BalanceRepository
import kr.hhplus.be.server.service.user.BalanceService
import org.junit.jupiter.api.BeforeEach
import kr.hhplus.be.server.domain.user.Balance
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
    fun `잔액 조회 시 레포지토리 예외가 발생하면 예외를 전파한다`() {
        // Arrange
        val accountId = 1
        val errorMessage = "데이터베이스 연결 오류"
        every { balanceRepository.findByAccountId(accountId) } throws RuntimeException(errorMessage)
        
        // Act & Assert
        val exception = assertThrows<RuntimeException> {
            balanceService.getByAccountId(accountId)
        }
        assertEquals(errorMessage, exception.message)
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
        every { balanceRepository.update(any()) } answers { firstArg() }

        // Act
        val updatedBalance = balanceService.charge(accountId, chargeAmount)

        // Assert
        assertEquals(expectedAmount, updatedBalance.amount)
        verify(exactly = 1) { balanceRepository.findByAccountId(accountId) }
        verify(exactly = 1) { balanceRepository.update(match { it.amount == expectedAmount }) }
    }
    
    @Test
    fun `잔액 충전 시 레포지토리 업데이트 예외가 발생하면 예외를 전파한다`() {
        // Arrange
        val accountId = 1
        val initialAmount = BigDecimal("100.00")
        val chargeAmount = BigDecimal("50.00")
        val balance = Balance.create(balanceId = 1, accountId = accountId, initialAmount = initialAmount)
        val errorMessage = "데이터베이스 업데이트 오류"
        
        every { balanceRepository.findByAccountId(accountId) } returns balance
        every { balanceRepository.update(any()) } throws RuntimeException(errorMessage)
        
        // Act & Assert
        val exception = assertThrows<RuntimeException> {
            balanceService.charge(accountId, chargeAmount)
        }
        assertEquals(errorMessage, exception.message)
        verify(exactly = 1) { balanceRepository.findByAccountId(accountId) }
        verify(exactly = 1) { balanceRepository.update(any()) }
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
        every { balanceRepository.update(any()) } answers { firstArg() }

        // Act
        val updatedBalance = balanceService.withdraw(accountId, withdrawAmount)

        // Assert
        assertEquals(expectedAmount, updatedBalance.amount)
        verify(exactly = 1) { balanceRepository.findByAccountId(accountId) }
        verify(exactly = 1) { balanceRepository.update(match { it.amount == expectedAmount }) }
    }
    
    @Test
    fun `출금 시 레포지토리 업데이트 예외가 발생하면 예외를 전파한다`() {
        // Arrange
        val accountId = 1
        val initialAmount = BigDecimal("200.00")
        val withdrawAmount = BigDecimal("50.00")
        val balance = Balance.create(balanceId = 1, accountId = accountId, initialAmount = initialAmount)
        val errorMessage = "데이터베이스 업데이트 오류"
        
        every { balanceRepository.findByAccountId(accountId) } returns balance
        every { balanceRepository.update(any()) } throws RuntimeException(errorMessage)
        
        // Act & Assert
        val exception = assertThrows<RuntimeException> {
            balanceService.withdraw(accountId, withdrawAmount)
        }
        assertEquals(errorMessage, exception.message)
        verify(exactly = 1) { balanceRepository.findByAccountId(accountId) }
        verify(exactly = 1) { balanceRepository.update(any()) }
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

    @Test
    fun `신규 계좌 생성 성공`() {
        // Arrange
        val balanceId = 1
        val accountId = 100
        val expectedBalance = Balance.create(balanceId, accountId)
        
        // Capture the balance that is passed to save
        val capturedBalance = slot<Balance>()
        every { balanceRepository.save(capture(capturedBalance)) } returns expectedBalance
        
        // Act
        val result = balanceService.create(balanceId, accountId)
        
        // Assert
        assertEquals(expectedBalance, result)
        assertEquals(balanceId, result.balanceId)
        assertEquals(accountId, result.accountId)
        assertEquals(BigDecimal.ZERO, result.amount) // Default initial amount
        verify(exactly = 1) { balanceRepository.save(any()) }
    }
    
    @Test
    fun `계좌 생성 시 레포지토리 예외가 발생하면 예외를 전파한다`() {
        // Arrange
        val balanceId = 1
        val accountId = 100
        val errorMessage = "중복된 계좌 생성 시도"
        
        every { balanceRepository.save(any()) } throws RuntimeException(errorMessage)
        
        // Act & Assert
        val exception = assertThrows<RuntimeException> {
            balanceService.create(balanceId, accountId)
        }
        assertEquals(errorMessage, exception.message)
        verify(exactly = 1) { balanceRepository.save(any()) }
    }
    
    @Test
    fun `계좌 삭제 성공`() {
        // Arrange
        val accountId = 100
        val balance = Balance.create(1, accountId)
        
        every { balanceRepository.findByAccountId(accountId) } returns balance
        every { balanceRepository.delete(balance) } returns Unit
        
        // Act
        balanceService.deleteByAccountId(accountId)
        
        // Assert
        verify(exactly = 1) { balanceRepository.findByAccountId(accountId) }
        verify(exactly = 1) { balanceRepository.delete(balance) }
    }
    
    @Test
    fun `계좌 삭제 시 계좌를 찾을 수 없으면 예외를 발생시킨다`() {
        // Arrange
        val accountId = 100
        every { balanceRepository.findByAccountId(accountId) } returns null
        
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balanceService.deleteByAccountId(accountId)
        }
        assertEquals("Balance not found for account: $accountId", exception.message)
        verify(exactly = 1) { balanceRepository.findByAccountId(accountId) }
        verify(exactly = 0) { balanceRepository.delete(any()) }
    }
    
    @Test
    fun `계좌 삭제 시 레포지토리 예외가 발생하면 예외를 전파한다`() {
        // Arrange
        val accountId = 100
        val balance = Balance.create(1, accountId)
        val errorMessage = "데이터베이스 삭제 오류"
        
        every { balanceRepository.findByAccountId(accountId) } returns balance
        every { balanceRepository.delete(balance) } throws RuntimeException(errorMessage)
        
        // Act & Assert
        val exception = assertThrows<RuntimeException> {
            balanceService.deleteByAccountId(accountId)
        }
        assertEquals(errorMessage, exception.message)
        verify(exactly = 1) { balanceRepository.findByAccountId(accountId) }
        verify(exactly = 1) { balanceRepository.delete(balance) }
    }
    
    @Test
    fun `충전 시 계좌를 찾을 수 없으면 예외를 발생시킨다`() {
        // Arrange
        val accountId = 100
        val chargeAmount = BigDecimal("100.00")
        every { balanceRepository.findByAccountId(accountId) } returns null
        
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balanceService.charge(accountId, chargeAmount)
        }
        assertEquals("Balance not found for account: $accountId", exception.message)
        verify(exactly = 1) { balanceRepository.findByAccountId(accountId) }
        verify(exactly = 0) { balanceRepository.update(any()) }
    }
    
    @Test
    fun `출금 시 계좌를 찾을 수 없으면 예외를 발생시킨다`() {
        // Arrange
        val accountId = 100
        val withdrawAmount = BigDecimal("100.00")
        every { balanceRepository.findByAccountId(accountId) } returns null
        
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            balanceService.withdraw(accountId, withdrawAmount)
        }
        assertEquals("Balance not found for account: $accountId", exception.message)
        verify(exactly = 1) { balanceRepository.findByAccountId(accountId) }
        verify(exactly = 0) { balanceRepository.update(any()) }
    }
}