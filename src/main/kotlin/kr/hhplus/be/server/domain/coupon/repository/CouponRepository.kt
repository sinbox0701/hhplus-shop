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
    fun delete(coupon: Coupon)
}
