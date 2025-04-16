package kr.hhplus.be.server.domain.coupon.model

import java.time.LocalDateTime

data class UserCoupon private constructor(
    val id: Long? = null,
    val userId: Long,
    val couponId: Long,
    var issueDate: LocalDateTime,
    var issued: Boolean,
    var used: Boolean,
    var quantity: Int
) {
    companion object {
        fun create(
            userId: Long,
            couponId: Long,
            quantity: Int
        ): UserCoupon {
            return UserCoupon(
                userId = userId,
                couponId = couponId,
                issueDate = LocalDateTime.MIN,
                issued = false,
                used = false,
                quantity = quantity
            )
        }
    }
    
    fun issue(couponStartDate: LocalDateTime, couponEndDate: LocalDateTime): UserCoupon {
        require(!issued) { "이미 발행된 쿠폰입니다." }
        
        val now = LocalDateTime.now()
        require(now.isAfter(couponStartDate) && now.isBefore(couponEndDate)) { "쿠폰 유효 기간이 아닙니다." }
        
        this.issueDate = now
        this.issued = true
        return this
    }
    
    fun use(): UserCoupon {
        require(issued) { "발행되지 않은 쿠폰은 사용할 수 없습니다." }
        require(!used) { "이미 사용된 쿠폰입니다." }
        
        this.used = true
        return this
    }
    
    fun isIssued(): Boolean = issued
    
    fun isUsed(): Boolean = used
}
