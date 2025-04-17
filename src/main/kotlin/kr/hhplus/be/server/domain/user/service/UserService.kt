package kr.hhplus.be.server.domain.user.service

import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun create(command: UserCommand.CreateUserCommand): User {
        // 이메일, 로그인 ID 중복 체크
        userRepository.findByEmail(command.email)?.let {
            throw IllegalArgumentException("이미 사용 중인 이메일입니다: $command.email")
        }
        
        userRepository.findByLoginId(command.loginId)?.let {
            throw IllegalArgumentException("이미 사용 중인 로그인 ID입니다: $command.loginId")
        }
        
        val user = User.create(command.name, command.email, command.loginId, command.password)
        return userRepository.save(user)
    }

    fun findAll(): List<User> {
        return userRepository.findAll()
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
    
    fun update(command: UserCommand.UpdateUserCommand): User {
        val user = findById(command.id)
        val updatedUser = user.update(command.loginId, command.password)
        return userRepository.update(updatedUser)
    }
    
    fun delete(id: Long) {
        userRepository.delete(id)
    }
    
    fun login(command: UserCommand.LoginCommand): User {
        val user = userRepository.findByLoginId(command.loginId) 
            ?: throw IllegalArgumentException("존재하지 않는 사용자입니다")
            
        if (user.password != command.password) {
            throw IllegalArgumentException("비밀번호가 일치하지 않습니다")
        }
        
        return user
    }
}