package kr.hhplus.be.server.domain.user.service

import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun create(name: String, email: String, loginId: String, password: String): User {
        // 이메일, 로그인 ID 중복 체크
        userRepository.findByEmail(email)?.let {
            throw IllegalArgumentException("이미 사용 중인 이메일입니다: $email")
        }
        
        userRepository.findByLoginId(loginId)?.let {
            throw IllegalArgumentException("이미 사용 중인 로그인 ID입니다: $loginId")
        }
        
        val userId = System.currentTimeMillis() // 현재 시간을 밀리초 단위로 사용하여 고유한 ID 생성
        val user = User.create(userId, name, email, loginId, password)
        return userRepository.save(user)
    }
    
    fun findById(id: Long): User {
        return userRepository.findById(id) ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $id")
    }
    
    fun findByLoginId(loginId: String): User? {
        return userRepository.findByLoginId(loginId)
    }
    
    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }
    
    fun update(id: Long, loginId: String?, password: String?): User {
        val user = findById(id)
        user.update(loginId, password)
        return userRepository.update(loginId, password)
    }
    
    fun delete(id: Long) {
        userRepository.delete(id)
    }
    
    fun login(loginId: String, password: String): User {
        val user = userRepository.findByLoginId(loginId) 
            ?: throw IllegalArgumentException("존재하지 않는 사용자입니다")
            
        if (user.password != password) {
            throw IllegalArgumentException("비밀번호가 일치하지 않습니다")
        }
        
        return user
    }
}