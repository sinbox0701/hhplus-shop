package kr.hhplus.be.server.domain.coupon.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.coupon.repository.UserCouponRepository
import kr.hhplus.be.server.domain.user.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class UserCouponServiceUnitTest {

    private lateinit var userCouponRepository: UserCouponRepository
    private lateinit var userCouponService: UserCouponService

    @BeforeEach
    fun setup() {
        userCouponRepository = mockk()
        userCouponService = UserCouponService(userCouponRepository)
    }

    @Test
    fun `create 메서드는 사용자 쿠폰을 생성하고 저장해야 한다`() {
        // given
        val user = mockk<User> {
            every { id } returns 1L
        }
        val coupon = mockk<Coupon> {
            every { id } returns 2L
        }
        val command = UserCouponCommand.CreateUserCouponCommand(
            user = user,
            coupon = coupon,
            quantity = 1
        )

        // 실제 UserCoupon.create 메서드를 사용하여 객체 생성
        val expectedUserCoupon = UserCoupon.create(user, coupon, 1)
        
        // 저장소 계층만 모킹
        every { userCouponRepository.save(any()) } returns expectedUserCoupon

        // when
        val result = userCouponService.create(command)

        // then
        assertEquals(expectedUserCoupon.id, result.id)
        assertEquals(user.id, result.user.id)
        assertEquals(coupon.id, result.coupon.id)
        verify { userCouponRepository.save(any()) }
    }

    @Test
    fun `findById 메서드는 ID로 사용자 쿠폰을 찾아야 한다`() {
        // given
        val userCoupon = mockk<UserCoupon> {
            every { id } returns 1L
        }
        every { userCouponRepository.findById(1L) } returns userCoupon

        // when
        val result = userCouponService.findById(1L)

        // then
        assertEquals(1L, result.id)
        verify { userCouponRepository.findById(1L) }
    }

    @Test
    fun `findById 메서드는 존재하지 않는 ID일 경우 예외를 발생시켜야 한다`() {
        // given
        every { userCouponRepository.findById(999L) } returns null

        // when & then
        assertThrows<IllegalArgumentException> {
            userCouponService.findById(999L)
        }
        verify { userCouponRepository.findById(999L) }
    }

    @Test
    fun `findByUserId 메서드는 사용자 ID로 쿠폰 목록을 찾아야 한다`() {
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
        val result = userCouponService.findByUserId(1L)

        // then
        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(2L, result[1].id)
        verify { userCouponRepository.findByUserId(1L) }
    }

    @Test
    fun `findByCouponId 메서드는 쿠폰 ID로 사용자 쿠폰 목록을 찾아야 한다`() {
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
        val result = userCouponService.findByCouponId(1L)

        // then
        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(2L, result[1].id)
        verify { userCouponRepository.findByCouponId(1L) }
    }

    @Test
    fun `findByUserIdAndCouponId 메서드는 사용자 ID와 쿠폰 ID로 사용자 쿠폰을 찾아야 한다`() {
        // given
        val userCoupon = mockk<UserCoupon> {
            every { id } returns 1L
        }
        every { userCouponRepository.findByUserIdAndCouponId(1L, 2L) } returns userCoupon

        // when
        val result = userCouponService.findByUserIdAndCouponId(1L, 2L)

        // then
        assertEquals(1L, result.id)
        verify { userCouponRepository.findByUserIdAndCouponId(1L, 2L) }
    }

    @Test
    fun `findByUserIdAndCouponId 메서드는 존재하지 않는 경우 예외를 발생시켜야 한다`() {
        // given
        every { userCouponRepository.findByUserIdAndCouponId(1L, 999L) } returns null

        // when & then
        assertThrows<IllegalArgumentException> {
            userCouponService.findByUserIdAndCouponId(1L, 999L)
        }
        verify { userCouponRepository.findByUserIdAndCouponId(1L, 999L) }
    }

    @Test
    fun `issue 메서드는 사용자 쿠폰을 발급해야 한다`() {
        // given
        val startDate = LocalDateTime.now().minusDays(1)  // 어제 시작
        val endDate = LocalDateTime.now().plusDays(30)    // 30일 후 종료
        
        val userCoupon = mockk<UserCoupon> {
            every { id } returns 1L
            every { issued } returns false  // 아직 발급되지 않은 쿠폰
            every { issue(startDate, endDate) } returns this@mockk  // issue 호출 시 자기 자신 반환
        }
        
        val command = UserCouponCommand.IssueCouponCommand(
            id = 1L,
            couponStartDate = startDate,
            couponEndDate = endDate
        )

        every { userCouponRepository.findById(1L) } returns userCoupon
        every { userCouponRepository.save(any()) } returns userCoupon

        // when
        userCouponService.issue(command)

        // then
        verify { userCouponRepository.findById(1L) }
        verify { userCoupon.issue(startDate, endDate) }  // 정확한 파라미터로 issue 호출 확인
        verify { userCouponRepository.save(userCoupon) }
    }

    @Test
    fun `use 메서드는 사용자 쿠폰을 사용해야 한다`() {
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
        userCouponService.use(1L)

        // then
        verify { userCouponRepository.findById(1L) }
        verify { userCoupon.use() }             // use() 메서드가 호출되었는지 확인
        verify { userCouponRepository.save(userCoupon) }
    }

    @Test
    fun `delete 메서드는 사용자 쿠폰을 삭제해야 한다`() {
        // given
        every { userCouponRepository.delete(1L) } returns Unit

        // when
        userCouponService.delete(1L)

        // then
        verify { userCouponRepository.delete(1L) }
    }

    @Test
    fun `deleteByUserIdAndCouponId 메서드는 사용자 ID와 쿠폰 ID로 사용자 쿠폰을 삭제해야 한다`() {
        // given
        every { userCouponRepository.deleteByUserIdAndCouponId(1L, 2L) } returns Unit

        // when
        userCouponService.deleteByUserIdAndCouponId(1L, 2L)

        // then
        verify { userCouponRepository.deleteByUserIdAndCouponId(1L, 2L) }
    }

    @Test
    fun `deleteAllByUserId 메서드는 사용자 ID로 모든 사용자 쿠폰을 삭제해야 한다`() {
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
        userCouponService.deleteAllByUserId(1L)

        // then
        verify { userCouponRepository.findByUserId(1L) }
        verify(exactly = 2) { userCouponRepository.delete(any()) }
    }

    @Test
    fun `deleteAllByCouponId 메서드는 쿠폰 ID로 모든 사용자 쿠폰을 삭제해야 한다`() {
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
        userCouponService.deleteAllByCouponId(1L)

        // then
        verify { userCouponRepository.findByCouponId(1L) }
        verify(exactly = 2) { userCouponRepository.delete(any()) }
    }
} 