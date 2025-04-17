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

    @Test
    @DisplayName("할인율 최소값(1.0%) 테스트")
    fun createCouponWithMinDiscountRate() {
        // given
        val code = "ABCDEF"
        val minDiscountRate = Coupon.MIN_DISCOUNT_RATE // 1.0
        val description = "테스트 쿠폰"
        val startDate = LocalDateTime.now().minusDays(1)
        val endDate = LocalDateTime.now().plusDays(10)
        val quantity = 50
        val couponType = CouponType.DISCOUNT_PRODUCT
        
        // when
        val coupon = Coupon.create(
            code = code,
            discountRate = minDiscountRate,
            description = description,
            startDate = startDate,
            endDate = endDate,
            quantity = quantity,
            couponType = couponType
        )
        
        // then
        assertEquals(minDiscountRate, coupon.discountRate)
    }
    
    @Test
    @DisplayName("할인율 최대값(100.0%) 테스트")
    fun createCouponWithMaxDiscountRate() {
        // given
        val code = "ABCDEF"
        val maxDiscountRate = Coupon.MAX_DISCOUNT_RATE // 100.0
        val description = "테스트 쿠폰"
        val startDate = LocalDateTime.now().minusDays(1)
        val endDate = LocalDateTime.now().plusDays(10)
        val quantity = 50
        val couponType = CouponType.DISCOUNT_PRODUCT
        
        // when
        val coupon = Coupon.create(
            code = code,
            discountRate = maxDiscountRate,
            description = description,
            startDate = startDate,
            endDate = endDate,
            quantity = quantity,
            couponType = couponType
        )
        
        // then
        assertEquals(maxDiscountRate, coupon.discountRate)
    }
    
    @Test
    @DisplayName("설명 최소 길이(2자) 테스트")
    fun createCouponWithMinDescriptionLength() {
        // given
        val code = "ABCDEF"
        val discountRate = 10.0
        val minLengthDescription = "테스" // 최소 2자
        val startDate = LocalDateTime.now().minusDays(1)
        val endDate = LocalDateTime.now().plusDays(10)
        val quantity = 50
        val couponType = CouponType.DISCOUNT_PRODUCT
        
        // when
        val coupon = Coupon.create(
            code = code,
            discountRate = discountRate,
            description = minLengthDescription,
            startDate = startDate,
            endDate = endDate,
            quantity = quantity,
            couponType = couponType
        )
        
        // then
        assertEquals(minLengthDescription, coupon.description)
        assertEquals(Coupon.MIN_DESCRIPTION_LENGTH, coupon.description.length)
    }
    
    @Test
    @DisplayName("설명 최대 길이(30자) 테스트")
    fun createCouponWithMaxDescriptionLength() {
        // given
        val code = "ABCDEF"
        val discountRate = 10.0
        val maxLengthDescription = "이것은최대길이서른자테스트입니다일이삼사오육칠팔구십" // 정확히 30자
        val startDate = LocalDateTime.now().minusDays(1)
        val endDate = LocalDateTime.now().plusDays(10)
        val quantity = 50
        val couponType = CouponType.DISCOUNT_PRODUCT
        
        // when
        val coupon = Coupon.create(
            code = code,
            discountRate = discountRate,
            description = maxLengthDescription,
            startDate = startDate,
            endDate = endDate,
            quantity = quantity,
            couponType = couponType
        )
        
        // then
        assertEquals(maxLengthDescription, coupon.description)
        assertEquals(Coupon.MAX_DESCRIPTION_LENGTH, coupon.description.length)
    }
    
    @Test
    @DisplayName("쿠폰 최소 수량(1개) 테스트")
    fun createCouponWithMinQuantity() {
        // given
        val code = "ABCDEF"
        val discountRate = 10.0
        val description = "테스트 쿠폰"
        val startDate = LocalDateTime.now().minusDays(1)
        val endDate = LocalDateTime.now().plusDays(10)
        val minQuantity = Coupon.MIN_QUANTITY // 1
        val couponType = CouponType.DISCOUNT_PRODUCT
        
        // when
        val coupon = Coupon.create(
            code = code,
            discountRate = discountRate,
            description = description,
            startDate = startDate,
            endDate = endDate,
            quantity = minQuantity,
            couponType = couponType
        )
        
        // then
        assertEquals(minQuantity, coupon.quantity)
        assertEquals(minQuantity, coupon.remainingQuantity)
    }
    
    @Test
    @DisplayName("쿠폰 최대 수량(100개) 테스트")
    fun createCouponWithMaxQuantity() {
        // given
        val code = "ABCDEF"
        val discountRate = 10.0
        val description = "테스트 쿠폰"
        val startDate = LocalDateTime.now().minusDays(1)
        val endDate = LocalDateTime.now().plusDays(10)
        val maxQuantity = Coupon.MAX_QUANTITY // 100
        val couponType = CouponType.DISCOUNT_PRODUCT
        
        // when
        val coupon = Coupon.create(
            code = code,
            discountRate = discountRate,
            description = description,
            startDate = startDate,
            endDate = endDate,
            quantity = maxQuantity,
            couponType = couponType
        )
        
        // then
        assertEquals(maxQuantity, coupon.quantity)
        assertEquals(maxQuantity, coupon.remainingQuantity)
    }

    @Test
    @DisplayName("감소량이 0일 때 예외 발생")
    fun decreaseQuantityWithZeroCount() {
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
        val exception = assertThrows<IllegalArgumentException> {
            coupon.decreaseQuantity(0)
        }
        
        assertTrue(exception.message!!.contains("쿠폰 수량은 0보다 크게 감소할 수 없습니다"))
    }
    
    @Test
    @DisplayName("감소량이 음수일 때 예외 발생")
    fun decreaseQuantityWithNegativeCount() {
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
        val exception = assertThrows<IllegalArgumentException> {
            coupon.decreaseQuantity(-1)
        }
        
        assertTrue(exception.message!!.contains("쿠폰 수량은 0보다 크게 감소할 수 없습니다"))
    }

    @Test
    @DisplayName("모든 필드가 null인 업데이트 테스트")
    fun updateCouponWithAllNullFields() {
        // given
        val originalCoupon = Coupon.create(
            code = "ABCDEF",
            discountRate = 10.0,
            description = "테스트 쿠폰",
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now().plusDays(10),
            quantity = 50,
            couponType = CouponType.DISCOUNT_PRODUCT
        )
        
        // when
        val updatedCoupon = originalCoupon.update(
            discountRate = null,
            description = null,
            startDate = null,
            endDate = null,
            quantity = null
        )
        
        // then
        assertEquals(originalCoupon.discountRate, updatedCoupon.discountRate)
        assertEquals(originalCoupon.description, updatedCoupon.description)
        assertEquals(originalCoupon.startDate, updatedCoupon.startDate)
        assertEquals(originalCoupon.endDate, updatedCoupon.endDate)
        assertEquals(originalCoupon.quantity, updatedCoupon.quantity)
        assertEquals(originalCoupon.remainingQuantity, updatedCoupon.remainingQuantity)
        assertNotEquals(originalCoupon.updatedAt, updatedCoupon.updatedAt)
    }
    
    @Test
    @DisplayName("수량 감소로 남은 쿠폰 수량이 음수가 될 때 예외 발생")
    fun updateCouponQuantityToNegativeRemaining() {
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
        
        // 쿠폰 45개 사용
        val updatedCoupon = coupon.decreaseQuantity(45)
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            updatedCoupon.update(quantity = 4) // 남은 수량은 5개인데 4개로 줄이려고 함
        }
        
        assertTrue(exception.message!!.contains("남은 쿠폰 수량은 0보다 작을 수 없습니다"))
    }

    @Test
    @DisplayName("주문 할인(DISCOUNT_ORDER) 타입 쿠폰 생성 테스트")
    fun createDiscountOrderTypeCoupon() {
        // given
        val code = "ABCDEF"
        val discountRate = 10.0
        val description = "주문 할인 쿠폰"
        val startDate = LocalDateTime.now().minusDays(1)
        val endDate = LocalDateTime.now().plusDays(10)
        val quantity = 50
        val couponType = CouponType.DISCOUNT_ORDER
        
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
        assertEquals(couponType, coupon.couponType)
    }

    @Test
    @DisplayName("시작일 직전에는 쿠폰이 유효하지 않음")
    fun couponIsNotValidJustBeforeStartDate() {
        // given
        val now = LocalDateTime.now()
        val startDate = now.plusMinutes(1) // 1분 후 시작
        val endDate = now.plusDays(10)
        
        val coupon = Coupon.create(
            code = "ABCDEF",
            discountRate = 10.0,
            description = "테스트 쿠폰",
            startDate = startDate,
            endDate = endDate,
            quantity = 5,
            couponType = CouponType.DISCOUNT_PRODUCT
        )
        
        // when & then
        assertFalse(coupon.isValid())
    }

    @Test
    @DisplayName("종료일 직후에는 쿠폰이 유효하지 않음")
    fun couponIsNotValidJustAfterEndDate() {
        // given
        val now = LocalDateTime.now()
        val startDate = now.minusDays(10)
        val endDate = now.minusMinutes(1) // 1분 전 종료
        
        val coupon = Coupon.create(
            code = "ABCDEF",
            discountRate = 10.0,
            description = "테스트 쿠폰",
            startDate = startDate,
            endDate = endDate,
            quantity = 5,
            couponType = CouponType.DISCOUNT_PRODUCT
        )
        
        // when & then
        assertFalse(coupon.isValid())
    }

}
