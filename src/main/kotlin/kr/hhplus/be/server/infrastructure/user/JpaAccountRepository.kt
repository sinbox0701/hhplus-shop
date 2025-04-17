package kr.hhplus.be.server.infrastructure.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaAccountRepository : JpaRepository<AccountEntity, Long> {
    fun findByUserId(userId: Long): AccountEntity?
} 