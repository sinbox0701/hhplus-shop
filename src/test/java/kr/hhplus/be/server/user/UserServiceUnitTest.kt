package kr.hhplus.be.server.user

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.user.repository.UserRepository
import kr.hhplus.be.server.domain.user.service.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserServiceUnitTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private lateinit var userService: UserService

    private val testId = 1L
    private val testName = "홍길동"
    private val testEmail = "user@example.com"
    private val testLoginId = "user123"
    private val testPassword = "pass123a"

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    @DisplayName("새로운 사용자 생성 성공")
    fun createUserSuccess() {
        // given
        val user = User.create(testId, testName, testEmail, testLoginId, testPassword)
        
        every { userRepository.findByEmail(testEmail) } returns null
        every { userRepository.findByLoginId(testLoginId) } returns null
        every { userRepository.save(any()) } returns user
        
        // when
        val createdUser = userService.create(testName, testEmail, testLoginId, testPassword)
        
        // then
        assertEquals(testName, createdUser.name)
        assertEquals(testEmail, createdUser.email)
        assertEquals(testLoginId, createdUser.loginId)
        assertEquals(testPassword, createdUser.password)
        
        verify(exactly = 1) { userRepository.findByEmail(testEmail) }
        verify(exactly = 1) { userRepository.findByLoginId(testLoginId) }
        verify(exactly = 1) { userRepository.save(any()) }
    }
    
    @Test
    @DisplayName("이미 사용 중인 이메일로 사용자 생성 시 예외 발생")
    fun createUserWithExistingEmail() {
        // given
        val existingUser = User.create(testId, testName, testEmail, testLoginId, testPassword)
        
        every { userRepository.findByEmail(testEmail) } returns existingUser
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            userService.create(testName, testEmail, testLoginId, testPassword)
        }
        
        assertTrue(exception.message!!.contains("이미 사용 중인 이메일입니다"))
        
        verify(exactly = 1) { userRepository.findByEmail(testEmail) }
        verify(exactly = 0) { userRepository.findByLoginId(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }
    
    @Test
    @DisplayName("이미 사용 중인 로그인ID로 사용자 생성 시 예외 발생")
    fun createUserWithExistingLoginId() {
        // given
        val existingUser = User.create(testId, testName, testEmail, testLoginId, testPassword)
        
        every { userRepository.findByEmail(testEmail) } returns null
        every { userRepository.findByLoginId(testLoginId) } returns existingUser
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            userService.create(testName, testEmail, testLoginId, testPassword)
        }
        
        assertTrue(exception.message!!.contains("이미 사용 중인 로그인 ID입니다"))
        
        verify(exactly = 1) { userRepository.findByEmail(testEmail) }
        verify(exactly = 1) { userRepository.findByLoginId(testLoginId) }
        verify(exactly = 0) { userRepository.save(any()) }
    }
    
    @Test
    @DisplayName("ID로 사용자 조회 성공")
    fun findUserByIdSuccess() {
        // given
        val user = User.create(testId, testName, testEmail, testLoginId, testPassword)
        
        every { userRepository.findById(testId) } returns user
        
        // when
        val foundUser = userService.findById(testId)
        
        // then
        assertEquals(testId, foundUser.id)
        assertEquals(testName, foundUser.name)
        assertEquals(testEmail, foundUser.email)
        
        verify(exactly = 1) { userRepository.findById(testId) }
    }
    
    @Test
    @DisplayName("존재하지 않는 ID로 사용자 조회 시 예외 발생")
    fun findUserByIdNotFound() {
        // given
        every { userRepository.findById(testId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            userService.findById(testId)
        }
        
        assertTrue(exception.message!!.contains("사용자를 찾을 수 없습니다"))
        
        verify(exactly = 1) { userRepository.findById(testId) }
    }
    
    @Test
    @DisplayName("사용자 정보 업데이트 성공")
    fun updateUserSuccess() {
        // given
        val user = User.create(testId, testName, testEmail, testLoginId, testPassword)
        val newLoginId = "newuser"
        val newPassword = "newpass1"
        val updatedUser = user.update(newLoginId, newPassword)
        
        every { userRepository.findById(testId) } returns user
        every { userRepository.update(newLoginId, newPassword) } returns updatedUser
        
        // when
        val result = userService.update(testId, newLoginId, newPassword)
        
        // then
        assertEquals(newLoginId, result.loginId)
        assertEquals(newPassword, result.password)
        
        verify(exactly = 1) { userRepository.findById(testId) }
        verify(exactly = 1) { userRepository.update(newLoginId, newPassword) }
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자 업데이트 시 예외 발생")
    fun updateNonExistentUser() {
        // given
        val newLoginId = "newuser"
        val newPassword = "newpass1"
        
        every { userRepository.findById(testId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            userService.update(testId, newLoginId, newPassword)
        }
        
        assertTrue(exception.message!!.contains("사용자를 찾을 수 없습니다"))
        
        verify(exactly = 1) { userRepository.findById(testId) }
        verify(exactly = 0) { userRepository.update(any(), any()) }
    }
    
    @Test
    @DisplayName("사용자 로그인 성공")
    fun loginSuccess() {
        // given
        val user = User.create(testId, testName, testEmail, testLoginId, testPassword)
        
        every { userRepository.findByLoginId(testLoginId) } returns user
        
        // when
        val loggedInUser = userService.login(testLoginId, testPassword)
        
        // then
        assertEquals(testId, loggedInUser.id)
        assertEquals(testName, loggedInUser.name)
        assertEquals(testLoginId, loggedInUser.loginId)
        
        verify(exactly = 1) { userRepository.findByLoginId(testLoginId) }
    }
    
    @Test
    @DisplayName("존재하지 않는 로그인ID로 로그인 시 예외 발생")
    fun loginWithNonExistentLoginId() {
        // given
        every { userRepository.findByLoginId(testLoginId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            userService.login(testLoginId, testPassword)
        }
        
        assertTrue(exception.message!!.contains("존재하지 않는 사용자입니다"))
        
        verify(exactly = 1) { userRepository.findByLoginId(testLoginId) }
    }
    
    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 예외 발생")
    fun loginWithWrongPassword() {
        // given
        val user = User.create(testId, testName, testEmail, testLoginId, testPassword)
        val wrongPassword = "wrongpass1"
        
        every { userRepository.findByLoginId(testLoginId) } returns user
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            userService.login(testLoginId, wrongPassword)
        }
        
        assertTrue(exception.message!!.contains("비밀번호가 일치하지 않습니다"))
        
        verify(exactly = 1) { userRepository.findByLoginId(testLoginId) }
    }
} 