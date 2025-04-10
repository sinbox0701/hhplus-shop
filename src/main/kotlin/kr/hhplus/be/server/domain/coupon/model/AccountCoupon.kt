package kr.hhplus.be.server.domain.coupon.model

import java.time.LocalDateTime
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Column
import jakarta.persistence.ManyToOne
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.coupon.model.Coupon

@Entity
@Table(name = "account_coupons")
data class AccountCoupon private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    val account: Account,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    val coupon: Coupon,

    @Column(nullable = false)
    var issueDate: LocalDateTime,

    @Column(nullable = false)
    var issued: Boolean,

    @Column(nullable = false)
    var used: Boolean
) {
    companion object {
        fun create(
            account: Account,
            coupon: Coupon
        ): AccountCoupon {
            return AccountCoupon(
                account = account,
                coupon = coupon,
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
