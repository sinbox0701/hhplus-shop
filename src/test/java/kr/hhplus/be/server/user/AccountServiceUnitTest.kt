package kr.hhplus.be.server.user

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.user.Account
import kr.hhplus.be.server.repository.user.AccountRepository
import kr.hhplus.be.server.service.user.AccountService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows

class AccountServiceUnitTest {
    private lateinit var accountRepository: AccountRepository
    private lateinit var accountService: AccountService

    @BeforeEach
    fun setUp() {
        accountRepository = mockk()
        accountService = AccountService(accountRepository)
    }

    @Test
    fun `계정이 존재하면 정상 반환한다`() {
        // Arrange
        val accountId = 1
        val account = Account.create(accountId = accountId, name = "Test User", email = "test@example.com", loginId = "test", password = "123456")

        every { accountRepository.findById(accountId) } returns account

        // Act
        val result = accountService.getById(accountId)

        // Assert
        assertEquals(account, result)
        verify(exactly = 1) { accountRepository.findById(accountId) }
    }

    @Test
    fun `계정을 찾지 못하면 예외를 발생시킨다`() {
        // Arrange
        val accountId = 1
        every { accountRepository.findById(accountId) } returns null

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            accountService.getById(accountId)
        }
        assertEquals("Account not found for accountId: $accountId", exception.message)
        verify(exactly = 1) { accountRepository.findById(accountId) }
    }

    @Test
    fun `계정이 존재하면 로그인 성공`() {
        // Arrange
        val loginId = "test"
        val password = "123456"
        val account = Account.create(accountId = 1, name = "Test User", email = "test@example.com", loginId = loginId, password = password)

        every { accountRepository.login(loginId, password) } returns account

        // Act
        val result = accountService.verify(loginId, password)

        // Assert
        assertEquals(account, result)
        verify(exactly = 1) { accountRepository.login(loginId, password) }
    }

    @Test
    fun `계정의 아이디나 비밀번호가 유효하지 않으면 예외를 발생시킨다`() {
        // Arrange
        val loginId = "test"
        val password = "123456"
        every { accountRepository.login(loginId, password) } returns null

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            accountService.verify(loginId, password)
        }
        assertEquals("Wrong Id or Password", exception.message)
        verify(exactly = 1) { accountRepository.login(loginId, password) }
    }
}