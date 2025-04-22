package kr.hhplus.be.server.coupon

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import java.time.LocalDateTime

object TestFixtures {
    // 상수 정의
    const val USER_ID = 1L
    const val COUPON_ID = 1L
    const val USER_COUPON_ID = 1L
    const val DISCOUNT_RATE = 10.0
    const val COUPON_QUANTITY = 100
    
    // 쿠폰 Fixture
    fun createCoupon(
        id: Long = COUPON_ID,
        code: String = "TESTCP",
        description: String = "테스트 쿠폰 설명",
        couponType: CouponType = CouponType.DISCOUNT_PRODUCT,
        discountRate: Double = DISCOUNT_RATE,
        isValid: Boolean = true,
        quantity: Int = COUPON_QUANTITY,
        remainingQuantity: Int = COUPON_QUANTITY
    ): Coupon {
        val now = LocalDateTime.now()
        val coupon = mockk<Coupon>()
        
        every { coupon.id } returns id
        every { coupon.code } returns code
        every { coupon.description } returns description
        every { coupon.couponType } returns couponType
        every { coupon.discountRate } returns discountRate
        every { coupon.startDate } returns now.minusDays(1)
        every { coupon.endDate } returns if (isValid) now.plusDays(7) else now.minusDays(1)
        every { coupon.quantity } returns quantity
        every { coupon.remainingQuantity } returns remainingQuantity
        every { coupon.createdAt } returns now.minusDays(2)
        every { coupon.updatedAt } returns now.minusDays(2)
        
        return coupon
    }
    
    // 사용자 쿠폰 Fixture
    fun createUserCoupon(
        id: Long = USER_COUPON_ID,
        userId: Long = USER_ID,
        couponId: Long = COUPON_ID,
        used: Boolean = false
    ): UserCoupon {
        val now = LocalDateTime.now()
        val userCoupon = mockk<UserCoupon>()
        
        every { userCoupon.id } returns id
        every { userCoupon.userId } returns userId
        every { userCoupon.couponId } returns couponId
        every { userCoupon.used } returns used
        every { userCoupon.issuedAt } returns now.minusDays(1)
        every { userCoupon.usedAt } returns if (used) now else null
        every { userCoupon.createdAt } returns now.minusDays(1)
        every { userCoupon.updatedAt } returns if (used) now else now.minusDays(1)
        
        return userCoupon
    }
    
    // 쿠폰 컬렉션 생성 헬퍼 메서드
    fun createCoupons(count: Int): List<Coupon> {
        return (1..count).map { 
            createCoupon(id = it.toLong(), code = "TEST${it.toString().padStart(2, '0')}")
        }
    }
    
    // 사용자 쿠폰 컬렉션 생성 헬퍼 메서드
    fun createUserCoupons(count: Int): List<UserCoupon> {
        return (1..count).map {
            createUserCoupon(id = it.toLong(), couponId = it.toLong())
        }
    }
} 