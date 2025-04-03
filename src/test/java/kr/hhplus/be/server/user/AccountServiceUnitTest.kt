package kr.hhplus.be.server.user

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.slot
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

    @Test
    fun `계정 생성 성공`() {
        // Arrange
        val account = Account.create(
            accountId = 1, 
            name = "Test User", 
            email = "test@example.com", 
            loginId = "test", 
            password = "pass1234"
        )
        every { accountRepository.save(any()) } returns account

        // Act
        val result = accountService.save(account)

        // Assert
        assertEquals(account, result)
        verify(exactly = 1) { accountRepository.save(any()) }
    }

    @Test
    fun `계정 업데이트 성공`() {
        // Arrange
        val accountId = 1
        val initialAccount = Account.create(
            accountId = accountId, 
            name = "Old Name", 
            email = "old@example.com", 
            loginId = "oldid", 
            password = "oldpass1"
        )
        val newName = "New Name"
        val capturedAccount = slot<Account>()

        every { accountRepository.findById(accountId) } returns initialAccount
        every { accountRepository.update(capture(capturedAccount)) } answers { capturedAccount.captured }

        // Act
        val result = accountService.update(
            accountId = accountId,
            name = newName,
            email = null,
            loginId = null,
            password = null
        )

        // Assert
        assertEquals(newName, result.name)
        assertEquals(initialAccount.email, result.email)  // unchanged
        assertEquals(initialAccount.loginId, result.loginId)  // unchanged
        assertEquals(initialAccount.password, result.password)  // unchanged
        verify(exactly = 1) { accountRepository.findById(accountId) }
        verify(exactly = 1) { accountRepository.update(any()) }
    }

    @Test
    fun `계정 업데이트 시 계정을 찾을 수 없으면 예외를 발생시킨다`() {
        // Arrange
        val accountId = 1
        every { accountRepository.findById(accountId) } returns null

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            accountService.update(
                accountId = accountId,
                name = "New Name",
                email = null,
                loginId = null,
                password = null
            )
        }
        assertEquals("Account not found for accountId: $accountId", exception.message)
        verify(exactly = 1) { accountRepository.findById(accountId) }
        verify(exactly = 0) { accountRepository.update(any()) }
    }

    @Test
    fun `계정 삭제 성공`() {
        // Arrange
        val accountId = 1
        val account = Account.create(
            accountId = accountId, 
            name = "Test User", 
            email = "test@example.com", 
            loginId = "test", 
            password = "pass1234"
        )
        
        every { accountRepository.findById(accountId) } returns account
        every { accountRepository.delete(account) } returns Unit

        // Act
        accountService.delete(accountId)

        // Assert
        verify(exactly = 1) { accountRepository.findById(accountId) }
        verify(exactly = 1) { accountRepository.delete(account) }
    }

    @Test
    fun `계정 삭제 시 계정을 찾을 수 없으면 예외를 발생시킨다`() {
        // Arrange
        val accountId = 1
        every { accountRepository.findById(accountId) } returns null

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            accountService.delete(accountId)
        }
        assertEquals("Account not found for accountId: $accountId", exception.message)
        verify(exactly = 1) { accountRepository.findById(accountId) }
        verify(exactly = 0) { accountRepository.delete(any()) }
    }
}