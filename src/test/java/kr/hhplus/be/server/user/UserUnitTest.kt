package kr.hhplus.be.server.user

import kr.hhplus.be.server.domain.user.model.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class UserUnitTest {
    
    @Test
    @DisplayName("유효한 데이터로 User 객체 생성 성공")
    fun createUserWithValidData() {
        // given
        val name = "홍길동"
        val email = "user@example.com"
        val loginId = "user123"
        val password = "pass123a"
        
        // when
        val user = User.create(name, email, loginId, password)
        
        // then
        assertEquals(name, user.name)
        assertEquals(email, user.email)
        assertEquals(loginId, user.loginId)
        assertEquals(password, user.password)
        assertNotNull(user.createdAt)
        assertNotNull(user.updatedAt)
    }
    
    @Test
    @DisplayName("로그인ID가 최소 길이보다 짧을 경우 예외 발생")
    fun createUserWithTooShortLoginId() {
        // given
        val name = "홍길동"
        val email = "user@example.com"
        val loginId = "usr" // 3자 (최소 4자 필요)
        val password = "pass123a"
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            User.create(name, email, loginId, password)
        }
        
        assertTrue(exception.message!!.contains("Login ID must be between"))
    }
    
    @Test
    @DisplayName("로그인ID가 최대 길이보다 길 경우 예외 발생")
    fun createUserWithTooLongLoginId() {
        // given
        val name = "홍길동"
        val email = "user@example.com"
        val loginId = "user12345" // 9자 (최대 8자 필요)
        val password = "pass123a"
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            User.create(name, email, loginId, password)
        }
        
        assertTrue(exception.message!!.contains("Login ID must be between"))
    }
    
    @Test
    @DisplayName("비밀번호가 영문자와 숫자의 조합이 아닐 경우 예외 발생")
    fun createUserWithInvalidPasswordFormat() {
        // given
        val name = "홍길동"
        val email = "user@example.com"
        val loginId = "user123"
        val password = "password" // 숫자 없음
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            User.create(name, email, loginId, password)
        }
        
        assertTrue(exception.message!!.contains("Password must be a combination"))
    }
    
    @Test
    @DisplayName("비밀번호가 최소 길이보다 짧을 경우 예외 발생")
    fun createUserWithTooShortPassword() {
        // given
        val name = "홍길동"
        val email = "user@example.com"
        val loginId = "user123"
        val password = "pass12" // 6자 (최소 8자 필요)
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            User.create(name, email, loginId, password)
        }
        
        assertTrue(exception.message!!.contains("Password must be a combination"))
    }
    
    @Test
    @DisplayName("비밀번호가 최대 길이보다 길 경우 예외 발생")
    fun createUserWithTooLongPassword() {
        // given
        val name = "홍길동"
        val email = "user@example.com"
        val loginId = "user123"
        val password = "password12345" // 13자 (최대 12자 필요)
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            User.create(name, email, loginId, password)
        }
        
        assertTrue(exception.message!!.contains("Password must be a combination"))
    }
    
    @Test
    @DisplayName("유효한 데이터로 사용자 정보 업데이트 성공")
    fun updateUserWithValidData() {
        // given
        val user = User.create("홍길동", "user@example.com", "user123", "pass123a")
        val newLoginId = "user456"
        val newPassword = "pass456a"
        
        // when
        val updatedUser = user.update(newLoginId, newPassword)
        
        // then
        assertEquals(newLoginId, updatedUser.loginId)
        assertEquals(newPassword, updatedUser.password)
        assertNotEquals(updatedUser.createdAt, updatedUser.updatedAt)
    }
    
    @Test
    @DisplayName("로그인ID만 업데이트 성공")
    fun updateOnlyLoginId() {
        // given
        val user = User.create("홍길동", "user@example.com", "user123", "pass123a")
        val originalPassword = user.password
        val newLoginId = "user456"
        
        // when
        val updatedUser = user.update(newLoginId, null)
        
        // then
        assertEquals(newLoginId, updatedUser.loginId)
        assertEquals(originalPassword, updatedUser.password)
    }
    
    @Test
    @DisplayName("비밀번호만 업데이트 성공")
    fun updateOnlyPassword() {
        // given
        val user = User.create("홍길동", "user@example.com", "user123", "pass123a")
        val originalLoginId = user.loginId
        val newPassword = "pass456a"
        
        // when
        val updatedUser = user.update(null, newPassword)
        
        // then
        assertEquals(originalLoginId, updatedUser.loginId)
        assertEquals(newPassword, updatedUser.password)
    }
    
    @Test
    @DisplayName("잘못된 로그인ID로 업데이트 시 예외 발생")
    fun updateWithInvalidLoginId() {
        // given
        val user = User.create("홍길동", "user@example.com", "user123", "pass123a")
        val invalidLoginId = "usr" // 3자 (최소 4자 필요)
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            user.update(invalidLoginId, null)
        }
        
        assertTrue(exception.message!!.contains("Login ID must be between"))
    }
    
    @Test
    @DisplayName("잘못된 비밀번호로 업데이트 시 예외 발생")
    fun updateWithInvalidPassword() {
        // given
        val user = User.create("홍길동", "user@example.com", "user123", "pass123a")
        val invalidPassword = "password" // 숫자 없음
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            user.update(null, invalidPassword)
        }
        
        assertTrue(exception.message!!.contains("Password must be a combination"))
    }
}