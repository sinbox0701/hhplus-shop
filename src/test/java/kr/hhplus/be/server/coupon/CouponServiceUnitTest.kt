package kr.hhplus.be.server.domain.coupon.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.domain.coupon.repository.UserCouponRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class CouponServiceUnitTest {

    private lateinit var couponRepository: CouponRepository
    private lateinit var userCouponRepository: UserCouponRepository
    private lateinit var couponService: CouponService

    @BeforeEach
    fun setup() {
        couponRepository = mockk()
        userCouponRepository = mockk()
        couponService = CouponService(couponRepository, userCouponRepository)
    }

    @Test
    fun `create 메서드는 쿠폰을 생성하고 저장해야 한다`() {
        // given
        val command = CouponCommand.CreateCouponCommand(
            code = "ABCDEF",
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

    @Test
    fun `updateRemainingQuantity 메서드는 남은 수량보다 많은 감소량에 대해 예외를 발생시켜야 한다`() {
        // given
        val coupon = mockk<Coupon>(relaxed = true) {
            every { id } returns 1L
            every { remainingQuantity } returns 3
            every { decreaseQuantity(5) } throws IllegalArgumentException("남은 쿠폰이 없습니다.")
        }
        
        val command = CouponCommand.UpdateCouponRemainingQuantityCommand(id = 1L, quantity = 5)

        every { couponRepository.findById(1L) } returns coupon

        // when & then
        assertThrows<IllegalArgumentException> {
            couponService.updateRemainingQuantity(command)
        }
        
        verify { couponRepository.findById(1L) }
        verify { coupon.decreaseQuantity(command.quantity) }
    }

    @Test
    fun `findByCode 메서드는 존재하지 않는 코드일 경우 예외를 발생시켜야 한다`() {
        // given
        every { couponRepository.findByCode("NONEXIST") } returns null

        // when & then
        assertThrows<IllegalArgumentException> {
            couponService.findByCode("NONEXIST")
        }
        
        verify { couponRepository.findByCode("NONEXIST") }
    }

    @Test
    fun `update 메서드는 존재하지 않는 쿠폰에 대해 예외를 발생시켜야 한다`() {
        // given
        val command = CouponCommand.UpdateCouponCommand(
            id = 999L,
            discountRate = 20.0,
            description = "존재하지 않는 쿠폰"
        )

        every { couponRepository.findById(999L) } returns null

        // when & then
        assertThrows<IllegalArgumentException> {
            couponService.update(command)
        }
        
        verify { couponRepository.findById(999L) }
    }

    @Test
    fun `createUserCoupon 메서드는 사용자 쿠폰을 생성하고 저장해야 한다`() {
        // given
        val command = CouponCommand.CreateUserCouponCommand(
            userId = 1L,
            couponId = 2L,
            quantity = 1
        )

        val expectedUserCoupon = mockk<UserCoupon> {
            every { id } returns 1L
            every { userId } returns 1L
            every { couponId } returns 2L
            every { quantity } returns 1
        }
        
        every { userCouponRepository.save(any()) } returns expectedUserCoupon

        // when
        val result = couponService.createUserCoupon(command)

        // then
        assertEquals(expectedUserCoupon.id, result.id)
        assertEquals(1L, result.userId)
        assertEquals(2L, result.couponId)
        assertEquals(1, result.quantity)
        verify { userCouponRepository.save(any()) }
    }

    @Test
    fun `findUserCouponById 메서드는 ID로 사용자 쿠폰을 찾아야 한다`() {
        // given
        val userCoupon = mockk<UserCoupon> {
            every { id } returns 1L
        }
        every { userCouponRepository.findById(1L) } returns userCoupon

        // when
        val result = couponService.findUserCouponById(1L)

        // then
        assertEquals(1L, result.id)
        verify { userCouponRepository.findById(1L) }
    }

    @Test
    fun `findUserCouponById 메서드는 존재하지 않는 ID일 경우 예외를 발생시켜야 한다`() {
        // given
        every { userCouponRepository.findById(999L) } returns null

        // when & then
        assertThrows<IllegalArgumentException> {
            couponService.findUserCouponById(999L)
        }
        verify { userCouponRepository.findById(999L) }
    }

    @Test
    fun `findUserCouponByUserId 메서드는 사용자 ID로 쿠폰 목록을 찾아야 한다`() {
        // given
        val userCoupons = listOf(
            mockk<UserCoupon> {
                every { id } returns 1L
            },
            mockk<UserCoupon> {
                every { id } returns 2L
            }
        )
        every { userCouponRepository.findByUserId(1L) } returns userCoupons

        // when
        val result = couponService.findUserCouponsByUserId(1L)

        // then
        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(2L, result[1].id)
        verify { userCouponRepository.findByUserId(1L) }
    }

    @Test
    fun `findUserCouponByCouponId 메서드는 쿠폰 ID로 사용자 쿠폰 목록을 찾아야 한다`() {
        // given
        val userCoupons = listOf(
            mockk<UserCoupon> {
                every { id } returns 1L
            },
            mockk<UserCoupon> {
                every { id } returns 2L
            }
        )
        every { userCouponRepository.findByCouponId(1L) } returns userCoupons

        // when
        val result = couponService.findUserCouponByCouponId(1L)

        // then
        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(2L, result[1].id)
        verify { userCouponRepository.findByCouponId(1L) }
    }

    @Test
    fun `findUserCouponByUserIdAndCouponId 메서드는 사용자 ID와 쿠폰 ID로 사용자 쿠폰을 찾아야 한다`() {
        // given
        val userCoupon = mockk<UserCoupon> {
            every { id } returns 1L
        }
        every { userCouponRepository.findByUserIdAndCouponId(1L, 2L) } returns userCoupon

        // when
        val result = couponService.findUserCouponByUserIdAndCouponId(1L, 2L)

        // then
        assertEquals(1L, result.id)
        verify { userCouponRepository.findByUserIdAndCouponId(1L, 2L) }
    }

    @Test
    fun `findUserCouponByUserIdAndCouponId 메서드는 존재하지 않는 경우 예외를 발생시켜야 한다`() {
        // given
        every { userCouponRepository.findByUserIdAndCouponId(1L, 999L) } returns null

        // when & then
        assertThrows<IllegalArgumentException> {
            couponService.findUserCouponByUserIdAndCouponId(1L, 999L)
        }
        verify { userCouponRepository.findByUserIdAndCouponId(1L, 999L) }
    }

    @Test
    fun `issueUserCoupon 메서드는 사용자 쿠폰을 발급해야 한다`() {
        // given
        val startDate = LocalDateTime.now().minusDays(1)  // 어제 시작
        val endDate = LocalDateTime.now().plusDays(30)    // 30일 후 종료
        
        val userCoupon = mockk<UserCoupon> {
            every { id } returns 1L
            every { issued } returns false  // 아직 발급되지 않은 쿠폰
            every { issue(startDate, endDate) } returns this@mockk  // issue 호출 시 자기 자신 반환
        }
        
        val command = CouponCommand.IssueCouponCommand(
            id = 1L,
            couponStartDate = startDate,
            couponEndDate = endDate
        )

        every { userCouponRepository.findById(1L) } returns userCoupon
        every { userCouponRepository.save(any()) } returns userCoupon

        // when
        couponService.issueUserCoupon(command)

        // then
        verify { userCouponRepository.findById(1L) }
        verify { userCoupon.issue(startDate, endDate) }  // 정확한 파라미터로 issue 호출 확인
        verify { userCouponRepository.save(userCoupon) }
    }

    @Test
    fun `issueUserCoupon 메서드는 이미 발행된 쿠폰에 대해 예외를 발생시켜야 한다`() {
        // given
        val startDate = LocalDateTime.now().minusDays(1)
        val endDate = LocalDateTime.now().plusDays(30)
        
        val userCoupon = mockk<UserCoupon> {
            every { id } returns 1L
            every { issued } returns true  // 이미 발행된 쿠폰
            every { issue(any(), any()) } throws IllegalArgumentException("이미 발행된 쿠폰입니다.")
        }
        
        val command = CouponCommand.IssueCouponCommand(
            id = 1L,
            couponStartDate = startDate,
            couponEndDate = endDate
        )

        every { userCouponRepository.findById(1L) } returns userCoupon

        // when & then
        assertThrows<IllegalArgumentException> {
            couponService.issueUserCoupon(command)
        }
        
        verify { userCouponRepository.findById(1L) }
        verify { userCoupon.issue(startDate, endDate) }
    }

    @Test
    fun `issueUserCoupon 메서드는 유효기간이 지난 쿠폰에 대해 예외를 발생시켜야 한다`() {
        // given
        val startDate = LocalDateTime.now().minusDays(30)
        val endDate = LocalDateTime.now().minusDays(10)  // 이미 종료된 쿠폰
        
        val userCoupon = mockk<UserCoupon> {
            every { id } returns 1L
            every { issued } returns false
            every { issue(startDate, endDate) } throws IllegalArgumentException("쿠폰 유효 기간이 아닙니다.")
        }
        
        val command = CouponCommand.IssueCouponCommand(
            id = 1L,
            couponStartDate = startDate,
            couponEndDate = endDate
        )

        every { userCouponRepository.findById(1L) } returns userCoupon

        // when & then
        assertThrows<IllegalArgumentException> {
            couponService.issueUserCoupon(command)
        }
        
        verify { userCouponRepository.findById(1L) }
        verify { userCoupon.issue(startDate, endDate) }
    }

    @Test
    fun `useUserCoupon 메서드는 사용자 쿠폰을 사용해야 한다`() {
        // given
        // 더 완전한 UserCoupon mock 생성 - 초기 상태 설정
        val userCoupon = mockk<UserCoupon> {
            every { id } returns 1L
            every { isIssued() } returns true  // 발행된 쿠폰이어야 사용 가능
            every { isUsed() } returns false   // 아직 사용되지 않은 쿠폰
            every { use() } returns this@mockk // use() 메서드 호출 시 자기 자신 반환
        }

        // 저장소 계층 모킹
        every { userCouponRepository.findById(1L) } returns userCoupon
        every { userCouponRepository.save(any()) } returns userCoupon

        // when
        couponService.useUserCoupon(1L)

        // then
        verify { userCouponRepository.findById(1L) }
        verify { userCoupon.use() }             // use() 메서드가 호출되었는지 확인
        verify { userCouponRepository.save(userCoupon) }
    }

    @Test
    fun `useUserCoupon 메서드는 발행되지 않은 쿠폰에 대해 예외를 발생시켜야 한다`() {
        // given
        val userCoupon = mockk<UserCoupon> {
            every { id } returns 1L
            every { isIssued() } returns false  // 발행되지 않은 쿠폰
            every { use() } throws IllegalArgumentException("발행되지 않은 쿠폰은 사용할 수 없습니다.")
        }

        every { userCouponRepository.findById(1L) } returns userCoupon

        // when & then
        assertThrows<IllegalArgumentException> {
            couponService.useUserCoupon(1L)
        }
        
        verify { userCouponRepository.findById(1L) }
        verify { userCoupon.use() }
    }

    @Test
    fun `useUserCoupon 메서드는 이미 사용된 쿠폰에 대해 예외를 발생시켜야 한다`() {
        // given
        val userCoupon = mockk<UserCoupon> {
            every { id } returns 1L
            every { isIssued() } returns true
            every { isUsed() } returns true  // 이미 사용된 쿠폰
            every { use() } throws IllegalArgumentException("이미 사용된 쿠폰입니다.")
        }

        every { userCouponRepository.findById(1L) } returns userCoupon

        // when & then
        assertThrows<IllegalArgumentException> {
            couponService.useUserCoupon(1L)
        }
        
        verify { userCouponRepository.findById(1L) }
        verify { userCoupon.use() }
    }

    @Test
    fun `deleteUserCoupon 메서드는 사용자 쿠폰을 삭제해야 한다`() {
        // given
        every { userCouponRepository.delete(1L) } returns Unit

        // when
        couponService.deleteUserCoupon(1L)

        // then
        verify { userCouponRepository.delete(1L) }
    }

    @Test
    fun `deleteUserCouponByUserIdAndCouponId 메서드는 사용자 ID와 쿠폰 ID로 사용자 쿠폰을 삭제해야 한다`() {
        // given
        every { userCouponRepository.deleteByUserIdAndCouponId(1L, 2L) } returns Unit

        // when
        couponService.deleteUserCouponByUserIdAndCouponId(1L, 2L)

        // then
        verify { userCouponRepository.deleteByUserIdAndCouponId(1L, 2L) }
    }

    @Test
    fun `deleteAllUserCouponByUserId 메서드는 사용자 ID로 모든 사용자 쿠폰을 삭제해야 한다`() {
        // given
        val userCoupons = listOf(
            mockk<UserCoupon> {
                every { id } returns 1L
            },
            mockk<UserCoupon> {
                every { id } returns 2L
            }
        )
        every { userCouponRepository.findByUserId(1L) } returns userCoupons
        every { userCouponRepository.delete(any()) } returns Unit

        // when
        couponService.deleteAllUserCouponByUserId(1L)

        // then
        verify { userCouponRepository.findByUserId(1L) }
        verify(exactly = 2) { userCouponRepository.delete(any()) }
    }

    @Test
    fun `deleteAllUserCouponByCouponId 메서드는 쿠폰 ID로 모든 사용자 쿠폰을 삭제해야 한다`() {
        // given
        val userCoupons = listOf(
            mockk<UserCoupon> {
                every { id } returns 1L
            },
            mockk<UserCoupon> {
                every { id } returns 2L
            }
        )
        every { userCouponRepository.findByCouponId(1L) } returns userCoupons
        every { userCouponRepository.delete(any()) } returns Unit

        // when
        couponService.deleteAllUserCouponByCouponId(1L)

        // then
        verify { userCouponRepository.findByCouponId(1L) }
        verify(exactly = 2) { userCouponRepository.delete(any()) }
    }
} 