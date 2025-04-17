package kr.hhplus.be.server.coupon

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.user.model.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class UserCouponUnitTest {
    val userId = 1L
    val couponId = 2L
    
    @Test
    @DisplayName("계정 쿠폰 생성 성공")
    fun createAccountCoupon() {
        // given

        // when
        val userCoupon = UserCoupon.create(userId, couponId, 1)
        
        // then
        assertEquals(userId, userCoupon.userId)
        assertEquals(couponId, userCoupon.couponId)
        assertEquals(LocalDateTime.MIN, userCoupon.issueDate)
        assertFalse(userCoupon.issued)
        assertFalse(userCoupon.used)
    }
    
    @Test
    @DisplayName("쿠폰 발행 성공")
    fun issueCouponSuccess() {
        // given
        val coupon = mockk<Coupon> {
            every { id } returns couponId
            every { startDate } returns LocalDateTime.now().minusDays(1)
            every { endDate } returns LocalDateTime.now().plusDays(10)
        }
        val userCoupon = UserCoupon.create(userId, couponId, 1)
        
        // when
        val issuedCoupon = userCoupon.issue(coupon.startDate, coupon.endDate)
        
        // then
        assertTrue(issuedCoupon.isIssued())
        assertFalse(issuedCoupon.isUsed())
        assertTrue(issuedCoupon.issueDate.isAfter(LocalDateTime.MIN))
    }
    
    @Test
    @DisplayName("이미 발행된 쿠폰 재발행 시도 시 예외 발생")
    fun issueAlreadyIssuedCoupon() {
        // given
        val coupon = mockk<Coupon> {
            every { id } returns couponId
            every { startDate } returns LocalDateTime.now().minusDays(1)
            every { endDate } returns LocalDateTime.now().plusDays(10)
        }
        val userCoupon = UserCoupon.create(userId, couponId, 1)
        
        // 쿠폰 발행
        userCoupon.issue(coupon.startDate, coupon.endDate)
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            userCoupon.issue(coupon.startDate, coupon.endDate)
        }
        
        assertTrue(exception.message!!.contains("이미 발행된 쿠폰입니다"))
    }
    
    @Test
    @DisplayName("쿠폰 유효기간 외 발행 시도 시 예외 발생")
    fun issueCouponOutOfValidPeriod() {
        // given
        val futureCoupon = mockk<Coupon> {
            every { id } returns couponId
            every { startDate } returns LocalDateTime.now().plusDays(10)
            every { endDate } returns LocalDateTime.now().plusDays(20)
        }
        val userCoupon = UserCoupon.create(userId, futureCoupon.id!!, 1)
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            userCoupon.issue(futureCoupon.startDate, futureCoupon.endDate)
        }
        
        assertTrue(exception.message!!.contains("쿠폰 유효 기간이 아닙니다"))
    }
    
    @Test
    @DisplayName("쿠폰 사용 성공")
    fun useCouponSuccess() {
        // given
        val coupon = mockk<Coupon> {
            every { id } returns couponId
            every { startDate } returns LocalDateTime.now().minusDays(1)
            every { endDate } returns LocalDateTime.now().plusDays(10)
        }
        val userCoupon = UserCoupon.create(userId, couponId, 1)
        
        // 쿠폰 발행
        userCoupon.issue(coupon.startDate, coupon.endDate)
        
        // when
        val usedCoupon = userCoupon.use()
        
        // then
        assertTrue(usedCoupon.isIssued())
        assertTrue(usedCoupon.isUsed())
    }
    
    @Test
    @DisplayName("발행되지 않은 쿠폰 사용 시도 시 예외 발생")
    fun useNotIssuedCoupon() {
        // given
        val userCoupon = UserCoupon.create(userId, couponId, 1)
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            userCoupon.use()
        }
        
        assertTrue(exception.message!!.contains("발행되지 않은 쿠폰은 사용할 수 없습니다"))
    }
    
    @Test
    @DisplayName("이미 사용한 쿠폰 재사용 시도 시 예외 발생")
    fun useAlreadyUsedCoupon() {
        // given
        val coupon = mockk<Coupon> {
            every { id } returns couponId
            every { startDate } returns LocalDateTime.now().minusDays(1)
            every { endDate } returns LocalDateTime.now().plusDays(10)
        }
        val userCoupon = UserCoupon.create(userId, couponId, 1)
        
        // 쿠폰 발행 및 사용
        userCoupon.issue(coupon.startDate, coupon.endDate)
        userCoupon.use()
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            userCoupon.use()
        }
        
        assertTrue(exception.message!!.contains("이미 사용된 쿠폰입니다"))
    }
}
