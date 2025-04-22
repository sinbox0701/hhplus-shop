package kr.hhplus.be.server.domain.coupon.model

import kr.hhplus.be.server.domain.common.TimeProvider
import java.time.LocalDateTime

data class UserCoupon private constructor(
    val id: Long? = null,
    val userId: Long,
    val couponId: Long,
    val issueDate: LocalDateTime,
    val issued: Boolean,
    val used: Boolean,
    val quantity: Int
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
    
    fun issue(couponStartDate: LocalDateTime, couponEndDate: LocalDateTime, timeProvider: TimeProvider): UserCoupon {
        require(!issued) { "이미 발행된 쿠폰입니다." }
        
        val now = timeProvider.now()
        require(now.isAfter(couponStartDate) && now.isBefore(couponEndDate)) { "쿠폰 유효 기간이 아닙니다." }
        
        return UserCoupon(
            id = this.id,
            userId = this.userId,
            couponId = this.couponId,
            issueDate = now,
            issued = true,
            used = this.used,
            quantity = this.quantity
        )
    }
    
    fun use(): UserCoupon {
        require(issued) { "발행되지 않은 쿠폰은 사용할 수 없습니다." }
        require(!used) { "이미 사용된 쿠폰입니다." }
        
        return UserCoupon(
            id = this.id,
            userId = this.userId,
            couponId = this.couponId,
            issueDate = this.issueDate,
            issued = this.issued,
            used = true,
            quantity = this.quantity
        )
    }
    
    fun isIssued(): Boolean = issued
    
    fun isUsed(): Boolean = used
}
