package kr.hhplus.be.server.coupon

import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class CouponUnitTest {
    
    @Test
    @DisplayName("유효한 데이터로 쿠폰 생성 성공")
    fun createCouponWithValidData() {
        // given
        val code = "ABCDEF"
        val discountRate = 10.0
        val description = "테스트 쿠폰"
        val startDate = LocalDateTime.now().minusDays(1)
        val endDate = LocalDateTime.now().plusDays(10)
        val quantity = 50
        val couponType = CouponType.DISCOUNT_PRODUCT
        
        // when
        val coupon = Coupon.create(
            code = code,
            discountRate = discountRate,
            description = description,
            startDate = startDate,
            endDate = endDate,
            quantity = quantity,
            couponType = couponType
        )
        
        // then
        assertEquals(code, coupon.code)
        assertEquals(discountRate, coupon.discountRate)
        assertEquals(description, coupon.description)
        assertEquals(startDate, coupon.startDate)
        assertEquals(endDate, coupon.endDate)
        assertEquals(quantity, coupon.quantity)
        assertEquals(quantity, coupon.remainingQuantity)
        assertEquals(couponType, coupon.couponType)
        assertNotNull(coupon.createdAt)
        assertNotNull(coupon.updatedAt)
    }
    
    @Test
    @DisplayName("쿠폰 코드가 유효하지 않을 경우 예외 발생")
    fun createCouponWithInvalidCode() {
        // given
        val invalidCode = "abc123" // 대문자 영어 6자리가 아님
        val discountRate = 10.0
        val description = "테스트 쿠폰"
        val startDate = LocalDateTime.now().minusDays(1)
        val endDate = LocalDateTime.now().plusDays(10)
        val quantity = 50
        val couponType = CouponType.DISCOUNT_PRODUCT
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            Coupon.create(
                code = invalidCode,
                discountRate = discountRate,
                description = description,
                startDate = startDate,
                endDate = endDate,
                quantity = quantity,
                couponType = couponType
            )
        }
        
        assertTrue(exception.message!!.contains("쿠폰 코드는 대문자 영어 6자리여야 합니다"))
    }
    
    @Test
    @DisplayName("할인율이 유효 범위를 벗어날 경우 예외 발생")
    fun createCouponWithInvalidDiscountRate() {
        // given
        val code = "ABCDEF"
        val invalidDiscountRate = 101.0 // 최대 100%
        val description = "테스트 쿠폰"
        val startDate = LocalDateTime.now().minusDays(1)
        val endDate = LocalDateTime.now().plusDays(10)
        val quantity = 50
        val couponType = CouponType.DISCOUNT_PRODUCT
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            Coupon.create(
                code = code,
                discountRate = invalidDiscountRate,
                description = description,
                startDate = startDate,
                endDate = endDate,
                quantity = quantity,
                couponType = couponType
            )
        }
        
        assertTrue(exception.message!!.contains("할인율은"))
    }
    
    @Test
    @DisplayName("설명 길이가 유효 범위를 벗어날 경우 예외 발생")
    fun createCouponWithInvalidDescription() {
        // given
        val code = "ABCDEF"
        val discountRate = 10.0
        val invalidDescription = "일" // 최소 2자 이상 필요
        val startDate = LocalDateTime.now().minusDays(1)
        val endDate = LocalDateTime.now().plusDays(10)
        val quantity = 50
        val couponType = CouponType.DISCOUNT_PRODUCT
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            Coupon.create(
                code = code,
                discountRate = discountRate,
                description = invalidDescription,
                startDate = startDate,
                endDate = endDate,
                quantity = quantity,
                couponType = couponType
            )
        }
        
        assertTrue(exception.message!!.contains("설명은"))
    }
    
    @Test
    @DisplayName("시작일이 종료일보다 늦을 경우 예외 발생")
    fun createCouponWithInvalidDateRange() {
        // given
        val code = "ABCDEF"
        val discountRate = 10.0
        val description = "테스트 쿠폰"
        val endDate = LocalDateTime.now().minusDays(10)
        val startDate = LocalDateTime.now() // 시작일이 종료일보다 늦음
        val quantity = 50
        val couponType = CouponType.DISCOUNT_PRODUCT
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            Coupon.create(
                code = code,
                discountRate = discountRate,
                description = description,
                startDate = startDate,
                endDate = endDate,
                quantity = quantity,
                couponType = couponType
            )
        }
        
        assertTrue(exception.message!!.contains("시작일은 종료일보다 이전이어야 합니다"))
    }
    
    @Test
    @DisplayName("쿠폰 수량이 유효 범위를 벗어날 경우 예외 발생")
    fun createCouponWithInvalidQuantity() {
        // given
        val code = "ABCDEF"
        val discountRate = 10.0
        val description = "테스트 쿠폰"
        val startDate = LocalDateTime.now().minusDays(1)
        val endDate = LocalDateTime.now().plusDays(10)
        val invalidQuantity = 101 // 최대 100개
        val couponType = CouponType.DISCOUNT_PRODUCT
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            Coupon.create(
                code = code,
                discountRate = discountRate,
                description = description,
                startDate = startDate,
                endDate = endDate,
                quantity = invalidQuantity,
                couponType = couponType
            )
        }
        
        assertTrue(exception.message!!.contains("쿠폰 수량은"))
    }
    
    @Test
    @DisplayName("쿠폰 정보 업데이트 성공")
    fun updateCouponSuccess() {
        // given
        val coupon = Coupon.create(
            code = "ABCDEF",
            discountRate = 10.0,
            description = "테스트 쿠폰",
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now().plusDays(10),
            quantity = 50,
            couponType = CouponType.DISCOUNT_PRODUCT
        )
        
        val newDiscountRate = 20.0
        val newDescription = "업데이트된 쿠폰"
        val newStartDate = LocalDateTime.now().plusDays(1)
        val newEndDate = LocalDateTime.now().plusDays(20)
        val newQuantity = 70
        
        // when
        val updatedCoupon = coupon.update(
            discountRate = newDiscountRate,
            description = newDescription,
            startDate = newStartDate,
            endDate = newEndDate,
            quantity = newQuantity
        )
        
        // then
        assertEquals(newDiscountRate, updatedCoupon.discountRate)
        assertEquals(newDescription, updatedCoupon.description)
        assertEquals(newStartDate, updatedCoupon.startDate)
        assertEquals(newEndDate, updatedCoupon.endDate)
        assertEquals(newQuantity, updatedCoupon.quantity)
        assertEquals(70, updatedCoupon.remainingQuantity) // 기존 50에서 20 증가
        assertNotEquals(updatedCoupon.createdAt, updatedCoupon.updatedAt)
    }
    
    @Test
    @DisplayName("쿠폰 수량 감소 성공")
    fun decreaseQuantitySuccess() {
        // given
        val coupon = Coupon.create(
            code = "ABCDEF",
            discountRate = 10.0,
            description = "테스트 쿠폰",
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now().plusDays(10),
            quantity = 50,
            couponType = CouponType.DISCOUNT_PRODUCT
        )
        
        val decreaseCount = 10
        val expectedRemainingQuantity = 40
        
        // when
        val updatedCoupon = coupon.decreaseQuantity(decreaseCount)
        
        // then
        assertEquals(expectedRemainingQuantity, updatedCoupon.remainingQuantity)
        assertNotEquals(updatedCoupon.createdAt, updatedCoupon.updatedAt)
    }
    
    @Test
    @DisplayName("남은 쿠폰 수량보다 많은 수량을 감소시킬 경우 예외 발생")
    fun decreaseQuantityMoreThanRemaining() {
        // given
        val coupon = Coupon.create(
            code = "ABCDEF",
            discountRate = 10.0,
            description = "테스트 쿠폰",
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now().plusDays(10),
            quantity = 5,
            couponType = CouponType.DISCOUNT_PRODUCT
        )
        
        // 모든 쿠폰 소진
        coupon.decreaseQuantity(5)
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            coupon.decreaseQuantity(1)
        }
        
        assertTrue(exception.message!!.contains("남은 쿠폰이 없습니다"))
    }
    
    @Test
    @DisplayName("쿠폰 유효성 확인")
    fun checkCouponValidity() {
        // given
        val validCoupon = Coupon.create(
            code = "ABCDEF",
            discountRate = 10.0,
            description = "유효한 쿠폰",
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now().plusDays(10),
            quantity = 5,
            couponType = CouponType.DISCOUNT_PRODUCT
        )
        
        val expiredCoupon = Coupon.create(
            code = "GHIJKL",
            discountRate = 10.0,
            description = "만료된 쿠폰",
            startDate = LocalDateTime.now().minusDays(20),
            endDate = LocalDateTime.now().minusDays(10),
            quantity = 5,
            couponType = CouponType.DISCOUNT_PRODUCT
        )
        
        val futureCoupon = Coupon.create(
            code = "MNOPQR",
            discountRate = 10.0,
            description = "미래 쿠폰",
            startDate = LocalDateTime.now().plusDays(10),
            endDate = LocalDateTime.now().plusDays(20),
            quantity = 5,
            couponType = CouponType.DISCOUNT_PRODUCT
        )
        
        val depletesCoupon = Coupon.create(
            code = "STUVWX",
            discountRate = 10.0,
            description = "소진된 쿠폰",
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now().plusDays(10),
            quantity = 5,
            couponType = CouponType.DISCOUNT_PRODUCT
        )
        depletesCoupon.decreaseQuantity(5) // 수량 모두 소진
        
        // when & then
        assertTrue(validCoupon.isValid())
        assertFalse(expiredCoupon.isValid())
        assertFalse(futureCoupon.isValid())
        assertFalse(depletesCoupon.isValid())
    }
    
    @Test
    @DisplayName("남은 수량 확인")
    fun checkRemainingQuantity() {
        // given
        val coupon = Coupon.create(
            code = "ABCDEF",
            discountRate = 10.0,
            description = "테스트 쿠폰",
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now().plusDays(10),
            quantity = 5,
            couponType = CouponType.DISCOUNT_PRODUCT
        )
        
        // when & then
        assertTrue(coupon.hasRemainingQuantity())
        
        coupon.decreaseQuantity(5)
        assertFalse(coupon.hasRemainingQuantity())
    }
}
