package kr.hhplus.be.server.domain.coupon.repository

import kr.hhplus.be.server.domain.coupon.model.AccountCoupon

interface AccountCouponRepository {
    fun save(accountCoupon: AccountCoupon): AccountCoupon
    fun findById(id: Long): AccountCoupon?
    fun findByAccountId(accountId: Long): List<AccountCoupon>
    fun findByCouponId(couponId: Long): List<AccountCoupon>
    fun findByAccountIdAndCouponId(accountId: Long, couponId: Long): AccountCoupon?
    fun update(accountCoupon: AccountCoupon): AccountCoupon
    fun delete(id: Long)
}
