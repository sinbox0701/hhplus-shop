package kr.hhplus.be.server.domain.user.repository

import kr.hhplus.be.server.domain.user.model.User

interface UserRepository {
    fun save(user: User): User
    fun findAll(): List<User>
    fun findByEmail(email: String): User?
    fun findByLoginId(loginId: String): User?
    fun findById(id: Long): User?
    fun update(user: User): User
    fun delete(id: Long)
}