package kr.hhplus.be.server.domain.coupon.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class CouponServiceUnitTest {

    private lateinit var couponRepository: CouponRepository
    private lateinit var couponService: CouponService

    @BeforeEach
    fun setup() {
        couponRepository = mockk()
        couponService = CouponService(couponRepository)
    }

    @Test
    fun `create 메서드는 쿠폰을 생성하고 저장해야 한다`() {
        // given
        val command = CouponCommand.CreateCouponCommand(
            code = "TEST001",
            couponType = CouponType.DISCOUNT_PRODUCT,
            discountRate = 10.0,
            description = "테스트 쿠폰",
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusDays(30),
            quantity = 100
        )

        val savedCoupon = mockk<Coupon> {
            every { id } returns 1L
            every { code } returns "TEST001"
        }

        every { couponRepository.save(any()) } returns savedCoupon

        // when
        val result = couponService.create(command)

        // then
        assertEquals(1L, result.id)
        assertEquals("TEST001", result.code)
        verify { couponRepository.save(any()) }
    }

    @Test
    fun `findAll 메서드는 모든 쿠폰을 반환해야 한다`() {
        // given
        val coupons = listOf(
            mockk<Coupon> {
                every { id } returns 1L
            },
            mockk<Coupon> {
                every { id } returns 2L
            }
        )
        every { couponRepository.findAll() } returns coupons

        // when
        val result = couponService.findAll()

        // then
        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(2L, result[1].id)
        verify { couponRepository.findAll() }
    }

    @Test
    fun `findById 메서드는 ID로 쿠폰을 찾아야 한다`() {
        // given
        val coupon = mockk<Coupon> {
            every { id } returns 1L
            every { code } returns "CODE1"
        }
        
        every { couponRepository.findById(1L) } returns coupon

        // when
        val result = couponService.findById(1L)

        // then
        assertEquals(1L, result.id)
        assertEquals("CODE1", result.code)
        verify { couponRepository.findById(1L) }
    }

    @Test
    fun `findById 메서드는 존재하지 않는 ID일 경우 예외를 발생시켜야 한다`() {
        // given
        every { couponRepository.findById(999L) } returns null

        // when & then
        assertThrows<IllegalArgumentException> {
            couponService.findById(999L)
        }
        verify { couponRepository.findById(999L) }
    }

    @Test
    fun `findByCode 메서드는 코드로 쿠폰을 찾아야 한다`() {
        // given
        val coupon = mockk<Coupon> {
            every { id } returns 1L
            every { code } returns "CODE1"
        }
        
        every { couponRepository.findByCode("CODE1") } returns coupon

        // when
        val result = couponService.findByCode("CODE1")

        // then
        assertEquals(1L, result.id)
        assertEquals("CODE1", result.code)
        verify { couponRepository.findByCode("CODE1") }
    }

    @Test
    fun `findByType 메서드는 타입으로 쿠폰 목록을 찾아야 한다`() {
        // given
        val coupons = listOf(
            mockk<Coupon> {
                every { id } returns 1L
                every { couponType } returns CouponType.DISCOUNT_PRODUCT
            },
            mockk<Coupon> {
                every { id } returns 2L
                every { couponType } returns CouponType.DISCOUNT_PRODUCT
            }
        )
        every { couponRepository.findByType(CouponType.DISCOUNT_PRODUCT) } returns coupons

        // when
        val result = couponService.findByType(CouponType.DISCOUNT_PRODUCT)

        // then
        assertEquals(2, result.size)
        assertEquals(CouponType.DISCOUNT_PRODUCT, result[0].couponType)
        assertEquals(CouponType.DISCOUNT_PRODUCT, result[1].couponType)
        verify { couponRepository.findByType(CouponType.DISCOUNT_PRODUCT) }
    }

    @Test
    fun `update 메서드는 쿠폰 정보를 업데이트해야 한다`() {
        // given
        val coupon = mockk<Coupon>(relaxed = true) {
            every { id } returns 1L
            every { discountRate } returns 20.0
            every { description } returns "수정된 쿠폰"
            every { remainingQuantity } returns 200
        }
        
        val now = LocalDateTime.now()
        val command = CouponCommand.UpdateCouponCommand(
            id = 1L,
            discountRate = 20.0,
            description = "수정된 쿠폰",
            startDate = now,
            endDate = now.plusDays(30),
            quantity = 200
        )

        every { couponRepository.findById(1L) } returns coupon
        every { couponRepository.save(any()) } returns coupon

        // when
        val result = couponService.update(command)

        // then
        assertEquals(1L, result.id)
        assertEquals(20.0, result.discountRate)
        assertEquals("수정된 쿠폰", result.description)
        assertEquals(200, result.remainingQuantity)
        verify { couponRepository.findById(1L) }
        verify { coupon.update(command.discountRate, command.description, command.startDate, command.endDate, command.quantity) }
        verify { couponRepository.save(coupon) }
    }

    @Test
    fun `updateRemainingQuantity 메서드는 쿠폰의 남은 수량을 갱신해야 한다`() {
        // given
        val coupon = mockk<Coupon>(relaxed = true) {
            every { id } returns 1L
            every { remainingQuantity } returns 95
        }
        
        val command = CouponCommand.UpdateCouponRemainingQuantityCommand(id = 1L, quantity = 5)

        every { couponRepository.findById(1L) } returns coupon
        every { couponRepository.save(any()) } returns coupon

        // when
        val result = couponService.updateRemainingQuantity(command)

        // then
        assertEquals(1L, result.id)
        assertEquals(95, result.remainingQuantity)
        verify { couponRepository.findById(1L) }
        verify { coupon.decreaseQuantity(command.quantity) }
        verify { couponRepository.save(coupon) }
    }

    @Test
    fun `delete 메서드는 쿠폰을 삭제해야 한다`() {
        // given
        every { couponRepository.delete(1L) } returns Unit

        // when
        couponService.delete(1L)

        // then
        verify { couponRepository.delete(1L) }
    }

    @Test
    fun `deleteAll 메서드는 여러 쿠폰을 삭제해야 한다`() {
        // given
        val ids = listOf(1L, 2L, 3L)
        every { couponRepository.delete(any()) } returns Unit

        // when
        couponService.deleteAll(ids)

        // then
        verify(exactly = 3) { couponRepository.delete(any()) }
    }
} 