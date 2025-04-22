package kr.hhplus.be.server.infrastructure.coupon

import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "coupons")
class CouponEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val couponType: CouponType,
    
    @Column(nullable = false, unique = true, length = 6)
    val code: String,
    
    @Column(nullable = false)
    val discountRate: Double,
    
    @Column(nullable = false, length = 30)
    val description: String,
    
    @Column(nullable = false)
    val startDate: LocalDateTime,
    
    @Column(nullable = false)
    val endDate: LocalDateTime,
    
    @Column(nullable = false)
    val quantity: Int,
    
    @Column(nullable = false)
    val remainingQuantity: Int,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime,
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime
) {
    fun toCoupon(): Coupon {
        // Coupon의 private 생성자 접근을 위한 리플렉션 사용
        val couponClass = Coupon::class.java
        val constructor = couponClass.getDeclaredConstructor(
            Long::class.java, CouponType::class.java, String::class.java,
            Double::class.java, String::class.java, LocalDateTime::class.java,
            LocalDateTime::class.java, Int::class.java, Int::class.java,
            LocalDateTime::class.java, LocalDateTime::class.java
        )
        constructor.isAccessible = true
        
        return constructor.newInstance(
            id, couponType, code, discountRate, description,
            startDate, endDate, quantity, remainingQuantity,
            createdAt, updatedAt
        )
    }
    
    companion object {
        fun fromCoupon(coupon: Coupon): CouponEntity {
            return CouponEntity(
                id = coupon.id,
                couponType = coupon.couponType,
                code = coupon.code,
                discountRate = coupon.discountRate,
                description = coupon.description,
                startDate = coupon.startDate,
                endDate = coupon.endDate,
                quantity = coupon.quantity,
                remainingQuantity = coupon.remainingQuantity,
                createdAt = coupon.createdAt,
                updatedAt = coupon.updatedAt
            )
        }
    }
} 