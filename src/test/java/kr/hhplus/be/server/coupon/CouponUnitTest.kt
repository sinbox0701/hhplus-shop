package kr.hhplus.be.server.coupon

import kr.hhplus.be.server.domain.common.FixedTimeProvider
import kr.hhplus.be.server.domain.common.TimeProvider
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class CouponUnitTest {
    
    private lateinit var timeProvider: TimeProvider
    
    @BeforeEach
    fun setup() {
        // 테스트에 사용할 고정된 시간 제공자 설정
        timeProvider = FixedTimeProvider(LocalDateTime.now())
    }
    
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
            couponType = couponType,
            timeProvider = timeProvider
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
                couponType = couponType,
                timeProvider = timeProvider
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
                couponType = couponType,
                timeProvider = timeProvider
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
                couponType = couponType,
                timeProvider = timeProvider
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
                couponType = couponType,
                timeProvider = timeProvider
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
                couponType = couponType,
                timeProvider = timeProvider
            )
        }
        
        assertTrue(exception.message!!.contains("쿠폰 수량은"))
    }
    
    @Test
    @DisplayName("쿠폰 정보 업데이트 성공")
    fun updateCouponSuccess() {
        // given
        // 고정된 시간 생성
        val fixedTime = LocalDateTime.of(2023, 5, 1, 12, 0)
        val initialTimeProvider = object : TimeProvider {
            override fun now() = fixedTime
            override fun today() = fixedTime.toLocalDate()
        }
        
        // 초기 쿠폰 생성
        val initialCoupon = Coupon.create(
            code = "ABCDEF",
            discountRate = 10.0,
            description = "테스트 쿠폰",
            startDate = fixedTime.minusDays(1),
            endDate = fixedTime.plusDays(10),
            quantity = 50,
            couponType = CouponType.DISCOUNT_PRODUCT,
            timeProvider = initialTimeProvider
        )
        
        // 업데이트 값 설정
        val newDiscountRate = 20.0
        val newDescription = "업데이트된 쿠폰"
        val newStartDate = fixedTime.minusDays(2)
        val newEndDate = fixedTime.plusDays(20)
        val newQuantity = 70
        
        // 업데이트 시간 설정 (+1시간)
        val updateTime = fixedTime.plusHours(1)
        val updateTimeProvider = object : TimeProvider {
            override fun now() = updateTime
            override fun today() = updateTime.toLocalDate()
        }
        
        // when
        val updatedCoupon = initialCoupon.update(
            discountRate = newDiscountRate,
            description = newDescription,
            startDate = newStartDate,
            endDate = newEndDate,
            quantity = newQuantity,
            timeProvider = updateTimeProvider
        )
        
        // then
        assertEquals(newDiscountRate, updatedCoupon.discountRate)
        assertEquals(newDescription, updatedCoupon.description)
        assertEquals(newStartDate, updatedCoupon.startDate)
        assertEquals(newEndDate, updatedCoupon.endDate)
        assertEquals(newQuantity, updatedCoupon.quantity)
        assertEquals(initialCoupon.remainingQuantity + (newQuantity - initialCoupon.quantity), updatedCoupon.remainingQuantity)
        assertEquals(initialCoupon.createdAt, updatedCoupon.createdAt)
        assertEquals(updateTime, updatedCoupon.updatedAt)
    }
    
    @Test
    @DisplayName("쿠폰 수량 감소 성공")
    fun decreaseQuantitySuccess() {
        // given
        // 고정된 시간 생성
        val fixedTime = LocalDateTime.of(2023, 5, 1, 12, 0)
        val initialTimeProvider = object : TimeProvider {
            override fun now() = fixedTime
            override fun today() = fixedTime.toLocalDate()
        }
        
        // 쿠폰 생성
        val initialQuantity = 50
        val coupon = Coupon.create(
            code = "ABCDEF",
            discountRate = 10.0,
            description = "테스트 쿠폰",
            startDate = fixedTime.minusDays(1),
            endDate = fixedTime.plusDays(10),
            quantity = initialQuantity,
            couponType = CouponType.DISCOUNT_PRODUCT,
            timeProvider = initialTimeProvider
        )
        
        val decreaseCount = 10
        val expectedRemainingQuantity = initialQuantity - decreaseCount
        
        // 감소 시간 설정 (+1시간)
        val decreaseTime = fixedTime.plusHours(1)
        val decreaseTimeProvider = object : TimeProvider {
            override fun now() = decreaseTime
            override fun today() = decreaseTime.toLocalDate()
        }
        
        // when
        val updatedCoupon = coupon.decreaseQuantity(decreaseCount, decreaseTimeProvider)
        
        // then
        assertEquals(initialQuantity, updatedCoupon.quantity)
        assertEquals(expectedRemainingQuantity, updatedCoupon.remainingQuantity)
        assertEquals(coupon.createdAt, updatedCoupon.createdAt)
        assertEquals(decreaseTime, updatedCoupon.updatedAt)
    }
    
    @Test
    @DisplayName("남은 쿠폰 수량보다 많은 수량을 감소시킬 경우 예외 발생")
    fun decreaseQuantityMoreThanRemaining() {
        // given
        val initialQuantity = 5
        val coupon = createTestCoupon(quantity = initialQuantity)
        
        // 모든 쿠폰 소진
        val emptyCoupon = coupon.decreaseQuantity(initialQuantity, timeProvider)
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            emptyCoupon.decreaseQuantity(1, timeProvider)
        }
        
        assertTrue(exception.message!!.contains("남은 쿠폰이 부족합니다"))
    }
    
    @Test
    @DisplayName("쿠폰 유효성 확인")
    fun checkCouponValidity() {
        // given
        val now = timeProvider.now()
        
        val validCoupon = createTestCoupon(
            startDate = now.minusDays(1),
            endDate = now.plusDays(10),
            quantity = 5
        )
        
        val expiredCoupon = createTestCoupon(
            startDate = now.minusDays(20),
            endDate = now.minusDays(10),
            quantity = 5
        )
        
        val futureCoupon = createTestCoupon(
            startDate = now.plusDays(10),
            endDate = now.plusDays(20),
            quantity = 5
        )
        
        val depletesCoupon = createTestCoupon(
            startDate = now.minusDays(1),
            endDate = now.plusDays(10),
            quantity = 5
        )
        val emptyCoupon = depletesCoupon.decreaseQuantity(5, timeProvider) // 수량 모두 소진
        
        // when & then
        assertTrue(validCoupon.isValid(timeProvider))
        assertFalse(expiredCoupon.isValid(timeProvider))
        assertFalse(futureCoupon.isValid(timeProvider))
        assertFalse(emptyCoupon.isValid(timeProvider))
    }
    
    @Test
    @DisplayName("쿠폰 남은 수량 확인")
    fun hasRemainingQuantity() {
        // given
        val coupon = createTestCoupon(quantity = 5)
        
        // when & then
        assertTrue(coupon.hasRemainingQuantity())
        
        val emptyCoupon = coupon.decreaseQuantity(5, timeProvider)
        assertFalse(emptyCoupon.hasRemainingQuantity())
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
            couponType = couponType,
            timeProvider = timeProvider
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
            couponType = couponType,
            timeProvider = timeProvider
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
            couponType = couponType,
            timeProvider = timeProvider
        )
        
        // then
        assertEquals(minLengthDescription, coupon.description)
        assertEquals(Coupon.MIN_DESCRIPTION_LENGTH, coupon.description.length)
    }
    
    @Test
    @DisplayName("설명 최대 길이(30자) 테스트")
    fun createCouponWithMaxDescriptionLength() {
        // given
        val maxLengthDescription = "123456789012345678901234567890" // 정확히 30자
        
        // when
        val coupon = createTestCoupon(description = maxLengthDescription)
        
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
            couponType = couponType,
            timeProvider = timeProvider
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
            couponType = couponType,
            timeProvider = timeProvider
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
            couponType = CouponType.DISCOUNT_PRODUCT,
            timeProvider = timeProvider
        )
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            coupon.decreaseQuantity(0, timeProvider)
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
            couponType = CouponType.DISCOUNT_PRODUCT,
            timeProvider = timeProvider
        )
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            coupon.decreaseQuantity(-1, timeProvider)
        }
        
        assertTrue(exception.message!!.contains("쿠폰 수량은 0보다 크게 감소할 수 없습니다"))
    }

    @Test
    @DisplayName("모든 필드가 null인 업데이트 테스트")
    fun updateAllFieldsNull() {
        // given
        // 고정된 시간 제공자 생성
        val fixedTime = LocalDateTime.of(2023, 5, 1, 12, 0)
        val initialTimeProvider = object : TimeProvider {
            override fun now() = fixedTime
            override fun today() = fixedTime.toLocalDate()
        }
        
        // 쿠폰 생성
        val originalCoupon = Coupon.create(
            code = "ABCDEF",
            discountRate = 10.0,
            description = "테스트 쿠폰",
            startDate = fixedTime.minusDays(1),
            endDate = fixedTime.plusDays(10),
            quantity = 50,
            couponType = CouponType.DISCOUNT_PRODUCT,
            timeProvider = initialTimeProvider
        )
        
        // 업데이트 시간 설정 (+1일)
        val updateTime = fixedTime.plusDays(1)
        val updateTimeProvider = object : TimeProvider {
            override fun now() = updateTime
            override fun today() = updateTime.toLocalDate()
        }
        
        // when
        val updatedCoupon = originalCoupon.update(
            discountRate = null,
            description = null,
            startDate = null,
            endDate = null,
            quantity = null,
            timeProvider = updateTimeProvider
        )
        
        // then
        assertEquals(originalCoupon.id, updatedCoupon.id)
        assertEquals(originalCoupon.couponType, updatedCoupon.couponType)
        assertEquals(originalCoupon.code, updatedCoupon.code)
        assertEquals(originalCoupon.discountRate, updatedCoupon.discountRate)
        assertEquals(originalCoupon.description, updatedCoupon.description)
        assertEquals(originalCoupon.startDate, updatedCoupon.startDate)
        assertEquals(originalCoupon.endDate, updatedCoupon.endDate)
        assertEquals(originalCoupon.quantity, updatedCoupon.quantity)
        assertEquals(originalCoupon.remainingQuantity, updatedCoupon.remainingQuantity)
        assertEquals(originalCoupon.createdAt, updatedCoupon.createdAt)
        assertEquals(updateTime, updatedCoupon.updatedAt)
        assertNotEquals(fixedTime, updatedCoupon.updatedAt)
    }
    
    @Test
    @DisplayName("수량 변경 시 남은 수량 검증")
    fun validateRemainingQuantityOnQuantityUpdate() {
        // given
        val coupon = Coupon.create(
            code = "ABCDEF",
            discountRate = 10.0,
            description = "테스트 쿠폰",
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now().plusDays(10),
            quantity = 50,
            couponType = CouponType.DISCOUNT_PRODUCT,
            timeProvider = timeProvider
        )
        
        // 쿠폰 45개 사용
        val updatedCoupon = coupon.decreaseQuantity(45, timeProvider)
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            updatedCoupon.update(
                discountRate = null,
                description = null,
                startDate = null,
                endDate = null,
                quantity = 4,
                timeProvider = timeProvider
            ) // 남은 수량은 5개인데 4개로 줄이려고 함
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
            couponType = couponType,
            timeProvider = timeProvider
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
            couponType = CouponType.DISCOUNT_PRODUCT,
            timeProvider = timeProvider
        )
        
        // when & then
        assertFalse(coupon.isValid(timeProvider))
    }

    @Test
    @DisplayName("종료일 직후에는 쿠폰이 유효하지 않음")
    fun couponInvalidJustAfterEndDate() {
        // given
        // 고정된 시간 생성
        val fixedTime = LocalDateTime.of(2023, 5, 1, 12, 0)
        
        // 쿠폰 생성용 시간 제공자
        val couponTimeProvider = object : TimeProvider {
            override fun now() = fixedTime.minusDays(1) // 쿠폰 생성 시간은 1일 전
            override fun today() = fixedTime.minusDays(1).toLocalDate()
        }
        
        // 쿠폰 종료일을 fixedTime으로 설정 (고정 시간에 쿠폰이 만료됨)
        val coupon = Coupon.create(
            code = "ABCDEF",
            discountRate = 10.0,
            description = "테스트 쿠폰",
            startDate = fixedTime.minusDays(10),
            endDate = fixedTime,
            quantity = 5,
            couponType = CouponType.DISCOUNT_PRODUCT,
            timeProvider = couponTimeProvider
        )
        
        // 유효성 검사용 시간 제공자 (검사 시간이 종료 시간과 같음)
        val validationTimeProvider = object : TimeProvider {
            override fun now() = fixedTime.plusSeconds(1) // 종료 시간 직후 1초
            override fun today() = fixedTime.toLocalDate()
        }
        
        // when & then
        assertFalse(coupon.isValid(validationTimeProvider), "종료일 직후에는 쿠폰이 유효하지 않아야 함")
    }

    // 헬퍼 메서드 추가
    private fun createTestCoupon(
        code: String = "ABCDEF",
        discountRate: Double = 10.0,
        description: String = "테스트 쿠폰",
        startDate: LocalDateTime = LocalDateTime.now().minusDays(1),
        endDate: LocalDateTime = LocalDateTime.now().plusDays(10),
        quantity: Int = 50,
        couponType: CouponType = CouponType.DISCOUNT_PRODUCT
    ): Coupon {
        return Coupon.create(
            code = code,
            discountRate = discountRate,
            description = description,
            startDate = startDate,
            endDate = endDate,
            quantity = quantity,
            couponType = couponType,
            timeProvider = timeProvider
        )
    }

}
