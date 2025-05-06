package kr.hhplus.be.server.domain.coupon.repository

import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType

interface CouponRepository {
    fun save(coupon: Coupon): Coupon
    fun findAll(): List<Coupon>
    fun findById(id: Long): Coupon?
    fun findByCode(code: String): Coupon?
    fun findByType(type: CouponType): List<Coupon>
    fun update(coupon: Coupon): Coupon
    fun delete(id: Long)
    
    /**
     * 비관적 락을 사용하여 쿠폰을 조회
     */
    fun findByIdWithPessimisticLock(id: Long): Coupon?
    
    /**
     * 비관적 락을 사용하여 쿠폰 코드로 쿠폰을 조회
     */
    fun findByCodeWithPessimisticLock(code: String): Coupon?
}
