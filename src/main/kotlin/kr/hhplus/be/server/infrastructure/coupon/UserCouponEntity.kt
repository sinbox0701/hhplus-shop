package kr.hhplus.be.server.infrastructure.coupon

import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "user_coupons")
class UserCouponEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val userId: Long,
    
    @Column(nullable = false)
    val couponId: Long,
    
    @Column(nullable = false)
    val issueDate: LocalDateTime,
    
    @Column(nullable = false)
    val issued: Boolean,
    
    @Column(nullable = false)
    val used: Boolean,
    
    @Column(nullable = false)
    val quantity: Int
) {
    fun toUserCoupon(): UserCoupon {
        // UserCoupon의 private 생성자 접근을 위한 리플렉션 사용
        val userCouponClass = UserCoupon::class.java
        val constructor = userCouponClass.getDeclaredConstructor(
            Long::class.javaObjectType, Long::class.javaObjectType, Long::class.javaObjectType,
            LocalDateTime::class.java, Boolean::class.javaObjectType, Boolean::class.javaObjectType, 
            Int::class.javaObjectType
        )
        constructor.isAccessible = true
        
        return constructor.newInstance(
            id, userId, couponId, issueDate, issued, used, quantity
        )
    }
    
    companion object {
        fun fromUserCoupon(userCoupon: UserCoupon): UserCouponEntity {
            // UserCoupon의 필드에 접근하기 위한 리플렉션
            val userCouponClass = UserCoupon::class.java
            
            val idField = userCouponClass.getDeclaredField("id")
            val userIdField = userCouponClass.getDeclaredField("userId")
            val couponIdField = userCouponClass.getDeclaredField("couponId")
            val issueDateField = userCouponClass.getDeclaredField("issueDate")
            val issuedField = userCouponClass.getDeclaredField("issued")
            val usedField = userCouponClass.getDeclaredField("used")
            val quantityField = userCouponClass.getDeclaredField("quantity")
            
            idField.isAccessible = true
            userIdField.isAccessible = true
            couponIdField.isAccessible = true
            issueDateField.isAccessible = true
            issuedField.isAccessible = true
            usedField.isAccessible = true
            quantityField.isAccessible = true
            
            return UserCouponEntity(
                id = idField.get(userCoupon) as? Long,
                userId = userIdField.get(userCoupon) as Long,
                couponId = couponIdField.get(userCoupon) as Long,
                issueDate = issueDateField.get(userCoupon) as LocalDateTime,
                issued = issuedField.get(userCoupon) as Boolean,
                used = usedField.get(userCoupon) as Boolean,
                quantity = quantityField.get(userCoupon) as Int
            )
        }
    }
} 