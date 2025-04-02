package kr.hhplus.be.server.account

import kr.hhplus.be.server.domain.account.Account
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows


class AccountUnitTest {

    @Test
    fun `create returns Account when valid parameters provided`() {
        // Arrange
        val accountId = 1
        val name = "John Doe"
        val email = "john@example.com"
        val loginId = "user1" // 5글자 (4~8 글자 조건 충족)
        val password = "pass1234" // 8글자, 영문과 숫자 조합

        // Act
        val createdAccount = Account.create(accountId, name, email, loginId, password)

        // Assert
        assertEquals(accountId, createdAccount.accountId)
        assertEquals(name, createdAccount.name)
        assertEquals(email, createdAccount.email)
        assertEquals(loginId, createdAccount.loginId)
        assertEquals(password, createdAccount.password)
    }

    @Test
    fun `create throws exception when loginId is too short`() {
        // Arrange
        val accountId = 1
        val name = "John Doe"
        val email = "john@example.com"
        val invalidLoginId = "usr" // 3글자, 조건 미충족
        val password = "pass1234" // 유효한 비밀번호

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Account.create(accountId, name, email, invalidLoginId, password)
        }
        assertEquals(
            "Login ID must be between ${Account.MIN_LOGIN_ID_LENGTH} and ${Account.MAX_LOGIN_ID_LENGTH} characters",
            exception.message
        )
    }

    @Test
    fun `create throws exception when loginId is too long`() {
        // Arrange
        val accountId = 1
        val name = "John Doe"
        val email = "john@example.com"
        val invalidLoginId = "verylonguser" // 12글자, 조건 미충족
        val password = "pass1234" // 유효한 비밀번호

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Account.create(accountId, name, email, invalidLoginId, password)
        }
        assertEquals(
            "Login ID must be between ${Account.MIN_LOGIN_ID_LENGTH} and ${Account.MAX_LOGIN_ID_LENGTH} characters",
            exception.message
        )
    }

    @Test
    fun `create throws exception when password does not match policy`() {
        // Arrange
        val accountId = 1
        val name = "John Doe"
        val email = "john@example.com"
        val loginId = "user1" // 유효한 로그인 아이디
        val invalidPassword = "password" // 숫자가 없는 비밀번호 -> 정책 미충족

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Account.create(accountId, name, email, loginId, invalidPassword)
        }
        assertEquals(
            "Password must be a combination of letters and numbers and between ${Account.MIN_PASSWORD_LENGTH} and ${Account.MAX_PASSWORD_LENGTH} characters",
            exception.message
        )
    }
}