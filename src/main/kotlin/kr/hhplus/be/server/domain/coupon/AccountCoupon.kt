package kr.hhplus.be.server.domain.coupon

import java.time.LocalDateTime

data class AccountCoupon private constructor(
    val accountCouponId: Int,
    val accountId: Int,
    val couponId: Int,
    var issueDate: LocalDateTime,
    var issued: Boolean,
    var used: Boolean
) {
    companion object {
        fun create(
            accountCouponId: Int,
            accountId: Int,
            couponId: Int
        ): AccountCoupon {
            return AccountCoupon(
                accountCouponId = accountCouponId,
                accountId = accountId,
                couponId = couponId,
                issueDate = LocalDateTime.MIN,
                issued = false,
                used = false
            )
        }
    }
    
    fun issue(couponStartDate: LocalDateTime, couponEndDate: LocalDateTime): AccountCoupon {
        require(!issued) { "이미 발행된 쿠폰입니다." }
        
        val now = LocalDateTime.now()
        require(now.isAfter(couponStartDate) && now.isBefore(couponEndDate)) { "쿠폰 유효 기간이 아닙니다." }
        
        this.issueDate = now
        this.issued = true
        return this
    }
    
    fun use(): AccountCoupon {
        require(issued) { "발행되지 않은 쿠폰은 사용할 수 없습니다." }
        require(!used) { "이미 사용된 쿠폰입니다." }
        
        this.used = true
        return this
    }
    
    fun isIssued(): Boolean = issued
    
    fun isUsed(): Boolean = used
}
