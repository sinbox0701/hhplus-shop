package kr.hhplus.be.server.user

import kr.hhplus.be.server.domain.user.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows


class UserUnitTest {

    @Test
    fun `create returns Account when valid parameters provided`() {
        // Arrange
        val accountId = 1
        val name = "John Doe"
        val email = "john@example.com"
        val loginId = "user1" // 5글자 (4~8 글자 조건 충족)
        val password = "pass1234" // 8글자, 영문과 숫자 조합

        // Act
        val createdUser = User.create(accountId, name, email, loginId, password)

        // Assert
        assertEquals(accountId, createdUser.accountId)
        assertEquals(name, createdUser.name)
        assertEquals(email, createdUser.email)
        assertEquals(loginId, createdUser.loginId)
        assertEquals(password, createdUser.password)
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
            User.create(accountId, name, email, invalidLoginId, password)
        }
        assertEquals(
            "Login ID must be between ${User.MIN_LOGIN_ID_LENGTH} and ${User.MAX_LOGIN_ID_LENGTH} characters",
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
            User.create(accountId, name, email, invalidLoginId, password)
        }
        assertEquals(
            "Login ID must be between ${User.MIN_LOGIN_ID_LENGTH} and ${User.MAX_LOGIN_ID_LENGTH} characters",
            exception.message
        )
    }

    @Test
    fun `create with loginId boundary values succeeds`() {
        // Arrange
        val accountId = 1
        val name = "John Doe"
        val email = "john@example.com"
        val minLengthLoginId = "abcd" // 최소 길이 4글자
        val maxLengthLoginId = "abcdefgh" // 최대 길이 8글자
        val password = "pass1234"

        // Act - 최소 길이 검증
        val minLengthUser = User.create(accountId, name, email, minLengthLoginId, password)
        
        // Assert - 최소 길이
        assertEquals(minLengthLoginId, minLengthUser.loginId)
        
        // Act - 최대 길이 검증
        val maxLengthUser = User.create(accountId, name, email, maxLengthLoginId, password)
        
        // Assert - 최대 길이
        assertEquals(maxLengthLoginId, maxLengthUser.loginId)
    }

    @Test
    fun `create with password boundary values succeeds`() {
        // Arrange
        val accountId = 1
        val name = "John Doe"
        val email = "john@example.com"
        val loginId = "user1"
        val minLengthPassword = "pass123" // 최소 길이 7글자 (영문 + 숫자 조합)
        val maxLengthPassword = "password12345" // 최대 길이 12글자 (영문 + 숫자 조합)
        
        // Act - 최소 길이 검증
        val minLengthUser = User.create(accountId, name, email, loginId, minLengthPassword)
        
        // Assert - 최소 길이
        assertEquals(minLengthPassword, minLengthUser.password)
        
        // Act - 최대 길이 검증
        val maxLengthUser = User.create(accountId, name, email, loginId, maxLengthPassword)
        
        // Assert - 최대 길이
        assertEquals(maxLengthPassword, maxLengthUser.password)
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
            User.create(accountId, name, email, loginId, invalidPassword)
        }
        assertEquals(
            "Password must be a combination of letters and numbers and between ${User.MIN_PASSWORD_LENGTH} and ${User.MAX_PASSWORD_LENGTH} characters",
            exception.message
        )
    }

    @Test
    fun `create throws exception when password is too short with right policy`() {
        // Arrange
        val accountId = 1
        val name = "John Doe"
        val email = "john@example.com"
        val loginId = "user1"
        val tooShortPassword = "abc123" // 6글자 (영문+숫자이지만 최소길이 미달)

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            User.create(accountId, name, email, loginId, tooShortPassword)
        }
        assertEquals(
            "Password must be a combination of letters and numbers and between ${User.MIN_PASSWORD_LENGTH} and ${User.MAX_PASSWORD_LENGTH} characters",
            exception.message
        )
    }

    @Test
    fun `create throws exception when password is too long with right policy`() {
        // Arrange
        val accountId = 1
        val name = "John Doe"
        val email = "john@example.com"
        val loginId = "user1"
        val tooLongPassword = "abcdefghi123456" // 15글자 (영문+숫자이지만 최대길이 초과)

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            User.create(accountId, name, email, loginId, tooLongPassword)
        }
        assertEquals(
            "Password must be a combination of letters and numbers and between ${User.MIN_PASSWORD_LENGTH} and ${User.MAX_PASSWORD_LENGTH} characters",
            exception.message
        )
    }

    @Test
    fun `create throws exception when password has only numbers`() {
        // Arrange
        val accountId = 1
        val name = "John Doe"
        val email = "john@example.com"
        val loginId = "user1"
        val onlyNumbersPassword = "12345678" // 숫자만 있는 비밀번호

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            User.create(accountId, name, email, loginId, onlyNumbersPassword)
        }
        assertEquals(
            "Password must be a combination of letters and numbers and between ${User.MIN_PASSWORD_LENGTH} and ${User.MAX_PASSWORD_LENGTH} characters",
            exception.message
        )
    }

    @Test
    fun `update with valid values successfully updates the account`() {
        // Arrange
        val user = User.create(
            accountId = 1, 
            name = "Old Name", 
            email = "old@example.com", 
            loginId = "oldid", 
            password = "oldpass1"
        )
        val newName = "New Name"
        val newEmail = "new@example.com"
        val newLoginId = "newid"
        val newPassword = "newpass2"
        
        // Act
        val updatedAccount = user.update(
            newName = newName,
            newEmail = newEmail,
            newLoginId = newLoginId,
            newPassword = newPassword
        )
        
        // Assert
        assertEquals(newName, updatedAccount.name)
        assertEquals(newEmail, updatedAccount.email)
        assertEquals(newLoginId, updatedAccount.loginId)
        assertEquals(newPassword, updatedAccount.password)
    }
    
    @Test
    fun `update with invalid loginId throws exception`() {
        // Arrange
        val user = User.create(
            accountId = 1, 
            name = "User", 
            email = "user@example.com", 
            loginId = "valid1", 
            password = "valid123"
        )
        val invalidLoginId = "verylongloginid" // Too long
        
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            user.update(newLoginId = invalidLoginId)
        }
        assertEquals(
            "Login ID must be between ${User.MIN_LOGIN_ID_LENGTH} and ${User.MAX_LOGIN_ID_LENGTH} characters",
            exception.message
        )
    }
    
    @Test
    fun `update with invalid password throws exception`() {
        // Arrange
        val user = User.create(
            accountId = 1, 
            name = "User", 
            email = "user@example.com", 
            loginId = "valid1", 
            password = "valid123"
        )
        val invalidPassword = "onlyletters" // No numbers
        
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            user.update(newPassword = invalidPassword)
        }
        assertEquals(
            "Password must be a combination of letters and numbers and between ${User.MIN_PASSWORD_LENGTH} and ${User.MAX_PASSWORD_LENGTH} characters",
            exception.message
        )
    }
    
    @Test
    fun `partial update only updates provided fields`() {
        // Arrange
        val originalName = "Original Name"
        val originalEmail = "original@example.com"
        val originalLoginId = "origId"
        val originalPassword = "origPass1"
        
        val user = User.create(
            accountId = 1, 
            name = originalName, 
            email = originalEmail, 
            loginId = originalLoginId, 
            password = originalPassword
        )
        
        val newName = "New Name"
        
        // Act - only update name
        val updatedAccount = user.update(newName = newName)
        
        // Assert
        assertEquals(newName, updatedAccount.name)
        assertEquals(originalEmail, updatedAccount.email)
        assertEquals(originalLoginId, updatedAccount.loginId)
        assertEquals(originalPassword, updatedAccount.password)
    }
    
    @Test
    fun `multiple fields partial update works correctly`() {
        // Arrange
        val originalName = "Original Name"
        val originalEmail = "original@example.com"
        val originalLoginId = "origId"
        val originalPassword = "origPass1"
        
        val user = User.create(
            accountId = 1, 
            name = originalName, 
            email = originalEmail, 
            loginId = originalLoginId, 
            password = originalPassword
        )
        
        val newEmail = "new@example.com"
        val newPassword = "newPass2"
        
        // Act - update email and password only
        val updatedAccount = user.update(
            newEmail = newEmail,
            newPassword = newPassword
        )
        
        // Assert
        assertEquals(originalName, updatedAccount.name) // unchanged
        assertEquals(newEmail, updatedAccount.email)
        assertEquals(originalLoginId, updatedAccount.loginId) // unchanged
        assertEquals(newPassword, updatedAccount.password)
    }
}