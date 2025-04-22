package kr.hhplus.be.server.infrastructure.coupon

import kr.hhplus.be.server.domain.coupon.model.CouponType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaCouponRepository : JpaRepository<CouponEntity, Long> {
    fun findByCode(code: String): CouponEntity?
    fun findByCouponType(couponType: CouponType): List<CouponEntity>
} 