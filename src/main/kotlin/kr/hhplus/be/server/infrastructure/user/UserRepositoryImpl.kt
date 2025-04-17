package kr.hhplus.be.server.infrastructure.user

import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.infrastructure.user.JpaUserRepository
import kr.hhplus.be.server.domain.user.repository.UserRepository
import org.springframework.stereotype.Repository
import org.springframework.data.repository.findByIdOrNull

@Repository
class UserRepositoryImpl(
    private val jpaUserRepository: JpaUserRepository
) : UserRepository {
    
    override fun save(user: User): User {
        val userEntity = UserEntity.fromUser(user)
        val savedEntity = jpaUserRepository.save(userEntity)
        return savedEntity.toUser()
    }
    
    override fun findAll(): List<User> {
        return jpaUserRepository.findAll().map { it.toUser() }
    }
    
    override fun findByEmail(email: String): User? {
        return jpaUserRepository.findByEmail(email)?.toUser()
    }
    
    override fun findByLoginId(loginId: String): User? {
        return jpaUserRepository.findByLoginId(loginId)?.toUser()
    }
    
    override fun findById(id: Long): User? {
        return jpaUserRepository.findByIdOrNull(id)?.toUser()
    }
    
    override fun update(user: User): User {
        val userEntity = UserEntity.fromUser(user)
        val savedEntity = jpaUserRepository.save(userEntity)
        return savedEntity.toUser()
    }
    
    override fun delete(id: Long) {
        jpaUserRepository.deleteById(id)
    }
} 