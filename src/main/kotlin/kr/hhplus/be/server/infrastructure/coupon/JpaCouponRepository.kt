package kr.hhplus.be.server.infrastructure.coupon

import kr.hhplus.be.server.domain.coupon.model.CouponType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import jakarta.persistence.LockModeType

@Repository
interface JpaCouponRepository : JpaRepository<CouponEntity, Long> {
    fun findByCode(code: String): CouponEntity?
    fun findByCouponType(couponType: CouponType): List<CouponEntity>
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponEntity c WHERE c.id = :id")
    fun findByIdWithPessimisticLock(@Param("id") id: Long): CouponEntity?
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponEntity c WHERE c.code = :code")
    fun findByCodeWithPessimisticLock(@Param("code") code: String): CouponEntity?
} 