package kr.hhplus.be.server.coupon

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.coupon.CouponCriteria
import kr.hhplus.be.server.application.coupon.CouponFacade
import kr.hhplus.be.server.application.coupon.CouponResult
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.coupon.service.CouponService
import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.user.service.UserService
import kr.hhplus.be.server.shared.transaction.TransactionHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class CouponFacadeIntegrationTest {

    private lateinit var couponService: CouponService
    private lateinit var userService: UserService
    private lateinit var couponFacade: CouponFacade

    companion object {
        private const val TEST_USER_ID = 1L
        private const val TEST_COUPON_ID = 2L
        private const val TEST_USER_COUPON_ID = 3L
        private const val TEST_CODE = "ABC123"
        private const val TEST_DISCOUNT_RATE = 10.0
        private const val TEST_DESCRIPTION = "테스트 쿠폰"
        private const val TEST_QUANTITY = 100
        private const val TEST_USER_COUPON_QUANTITY = 1
    }

    @BeforeEach
    fun setup() {
        couponService = mockk(relaxed = true)
        userService = mockk(relaxed = true)
        val transactionHelper = mockk<TransactionHelper>(relaxed = true)
        couponFacade = CouponFacade(couponService, userService, transactionHelper)
    }

    @Test
    @DisplayName("쿠폰 생성 성공")
    fun createCouponSuccess() {
        // given
        val now = LocalDateTime.now()
        val startDate = now.plusDays(1)
        val endDate = now.plusDays(30)

        val criteria = CouponCriteria.CreateUserCouponCommand(
            userId = TEST_USER_ID,
            code = TEST_CODE,
            couponType = CouponType.DISCOUNT_PRODUCT,
            discountRate = TEST_DISCOUNT_RATE,
            description = TEST_DESCRIPTION,
            startDate = startDate,
            endDate = endDate,
            quantity = TEST_QUANTITY,
            userCouponQuantity = TEST_USER_COUPON_QUANTITY
        )

        val user = createTestUser(TEST_USER_ID)
        val coupon = createTestCoupon(TEST_COUPON_ID, TEST_CODE, CouponType.DISCOUNT_PRODUCT, startDate, endDate)
        val userCoupon = createTestUserCoupon(TEST_USER_COUPON_ID, TEST_USER_ID, TEST_COUPON_ID)

        every { userService.findById(TEST_USER_ID) } returns user
        every { couponService.create(any()) } returns coupon
        every { couponService.createUserCoupon(any()) } returns userCoupon
        every { couponService.updateRemainingQuantity(any()) } returns coupon

        // when
        val result = couponFacade.create(criteria)

        // then
        assertNotNull(result)
        assertEquals(TEST_USER_ID, result.userId)
        assertEquals(TEST_COUPON_ID, result.couponId)

        verify(exactly = 1) { userService.findById(TEST_USER_ID) }
        verify(exactly = 1) { couponService.create(any()) }
        verify(exactly = 1) { couponService.createUserCoupon(any()) }
        verify(exactly = 1) { couponService.updateRemainingQuantity(any()) }
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 쿠폰 생성 실패")
    fun createCouponWithNonExistentUserFails() {
        // given
        val now = LocalDateTime.now()
        val startDate = now.plusDays(1)
        val endDate = now.plusDays(30)
        val nonExistentUserId = 999L
        val errorMessage = "사용자를 찾을 수 없습니다: $nonExistentUserId"

        val criteria = CouponCriteria.CreateUserCouponCommand(
            userId = nonExistentUserId,
            code = TEST_CODE,
            couponType = CouponType.DISCOUNT_PRODUCT,
            discountRate = TEST_DISCOUNT_RATE,
            description = TEST_DESCRIPTION,
            startDate = startDate,
            endDate = endDate,
            quantity = TEST_QUANTITY,
            userCouponQuantity = TEST_USER_COUPON_QUANTITY
        )

        every { userService.findById(nonExistentUserId) } throws IllegalArgumentException(errorMessage)

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            couponFacade.create(criteria)
        }

        assertEquals(errorMessage, exception.message)
        verify(exactly = 1) { userService.findById(nonExistentUserId) }
        verify(exactly = 0) { couponService.create(any()) }
    }

    @Test
    @DisplayName("사용자 ID로 쿠폰 조회 성공")
    fun findCouponsByUserIdSuccess() {
        // given
        val now = LocalDateTime.now()
        val startDate = now.plusDays(1)
        val endDate = now.plusDays(30)

        val user = createTestUser(TEST_USER_ID)
        val coupon = createTestCoupon(TEST_COUPON_ID, TEST_CODE, CouponType.DISCOUNT_PRODUCT, startDate, endDate)
        val userCoupon = createTestUserCoupon(TEST_USER_COUPON_ID, TEST_USER_ID, TEST_COUPON_ID)
        val userCouponResult = CouponResult.UserCouponResult.from(userCoupon, coupon)

        every { userService.findById(TEST_USER_ID) } returns user
        every { couponService.findUserCouponsByUserId(TEST_USER_ID) } returns listOf(userCoupon)
        every { couponService.findAllByIds(listOf(TEST_COUPON_ID)) } returns listOf(coupon)

        // when
        val results = couponFacade.findByUserId(TEST_USER_ID)

        // then
        assertNotNull(results)
        assertEquals(1, results.size)
        assertEquals(TEST_USER_ID, results[0].userId)
        assertEquals(TEST_COUPON_ID, results[0].couponId)
        assertEquals(TEST_CODE, results[0].code)
        assertEquals(CouponType.DISCOUNT_PRODUCT, results[0].type)

        verify(exactly = 1) { userService.findById(TEST_USER_ID) }
        verify(exactly = 1) { couponService.findUserCouponsByUserId(TEST_USER_ID) }
        verify(exactly = 1) { couponService.findAllByIds(listOf(TEST_COUPON_ID)) }
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 쿠폰 조회 실패")
    fun findCouponsByNonExistentUserIdFails() {
        // given
        val nonExistentUserId = 999L
        val errorMessage = "사용자를 찾을 수 없습니다: $nonExistentUserId"

        every { userService.findById(nonExistentUserId) } throws IllegalArgumentException(errorMessage)

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            couponFacade.findByUserId(nonExistentUserId)
        }

        assertEquals(errorMessage, exception.message)
        verify(exactly = 1) { userService.findById(nonExistentUserId) }
        verify(exactly = 0) { couponService.findUserCouponsByUserId(any()) }
    }

    @Test
    @DisplayName("사용자 ID와 쿠폰 ID로 쿠폰 조회 성공")
    fun findCouponByUserIdAndCouponIdSuccess() {
        // given
        val now = LocalDateTime.now()
        val startDate = now.plusDays(1)
        val endDate = now.plusDays(30)

        val coupon = createTestCoupon(TEST_COUPON_ID, TEST_CODE, CouponType.DISCOUNT_PRODUCT, startDate, endDate)
        val userCoupon = createTestUserCoupon(TEST_USER_COUPON_ID, TEST_USER_ID, TEST_COUPON_ID)

        every { couponService.findById(TEST_COUPON_ID) } returns coupon
        every { couponService.findUserCouponByUserIdAndCouponId(TEST_USER_ID, TEST_COUPON_ID) } returns userCoupon

        // when
        val result = couponFacade.findByUserIdAndCouponId(TEST_USER_ID, TEST_COUPON_ID)

        // then
        assertNotNull(result)
        assertEquals(TEST_USER_ID, result.userId)
        assertEquals(TEST_COUPON_ID, result.couponId)
        assertEquals(TEST_CODE, result.code)
        assertEquals(CouponType.DISCOUNT_PRODUCT, result.type)

        verify(exactly = 1) { couponService.findById(TEST_COUPON_ID) }
        verify(exactly = 1) { couponService.findUserCouponByUserIdAndCouponId(TEST_USER_ID, TEST_COUPON_ID) }
    }

    @Test
    @DisplayName("쿠폰 발급 성공")
    fun issueCouponSuccess() {
        // given
        val now = LocalDateTime.now()
        val startDate = now.plusDays(1)
        val endDate = now.plusDays(30)

        val criteria = CouponCriteria.UpdateCouponCommand(
            userId = TEST_USER_ID,
            couponId = TEST_COUPON_ID
        )

        val coupon = createTestCoupon(TEST_COUPON_ID, TEST_CODE, CouponType.DISCOUNT_PRODUCT, startDate, endDate)
        val userCoupon = createTestUserCoupon(TEST_USER_COUPON_ID, TEST_USER_ID, TEST_COUPON_ID)

        every { couponService.findById(TEST_COUPON_ID) } returns coupon
        every { couponService.findUserCouponByUserIdAndCouponId(TEST_USER_ID, TEST_COUPON_ID) } returns userCoupon

        // when
        couponFacade.issue(criteria)

        // then
        verify(exactly = 1) { couponService.findById(TEST_COUPON_ID) }
        verify(exactly = 1) { couponService.findUserCouponByUserIdAndCouponId(TEST_USER_ID, TEST_COUPON_ID) }
        verify(exactly = 1) { couponService.issueUserCoupon(any()) }
    }

    @Test
    @DisplayName("쿠폰 사용 성공")
    fun useCouponSuccess() {
        // given
        val criteria = CouponCriteria.UpdateCouponCommand(
            userId = TEST_USER_ID,
            couponId = TEST_COUPON_ID
        )

        val user = createTestUser(TEST_USER_ID)
        val coupon = createTestCoupon(TEST_COUPON_ID, TEST_CODE, CouponType.DISCOUNT_PRODUCT)
        val userCoupon = createTestUserCoupon(TEST_USER_COUPON_ID, TEST_USER_ID, TEST_COUPON_ID)

        every { userService.findById(TEST_USER_ID) } returns user
        every { couponService.findById(TEST_COUPON_ID) } returns coupon
        every { couponService.findUserCouponByUserIdAndCouponId(TEST_USER_ID, TEST_COUPON_ID) } returns userCoupon

        // when
        couponFacade.use(criteria)

        // then
        verify(exactly = 1) { userService.findById(TEST_USER_ID) }
        verify(exactly = 1) { couponService.findById(TEST_COUPON_ID) }
        verify(exactly = 1) { couponService.findUserCouponByUserIdAndCouponId(TEST_USER_ID, TEST_COUPON_ID) }
        verify(exactly = 1) { couponService.useUserCoupon(TEST_USER_COUPON_ID) }
    }

    @Test
    @DisplayName("사용자 ID와 쿠폰 ID로 쿠폰 삭제 성공")
    fun deleteCouponByUserIdAndCouponIdSuccess() {
        // given
        val criteria = CouponCriteria.UpdateCouponCommand(
            userId = TEST_USER_ID,
            couponId = TEST_COUPON_ID
        )

        val user = createTestUser(TEST_USER_ID)
        val coupon = createTestCoupon(TEST_COUPON_ID, TEST_CODE, CouponType.DISCOUNT_PRODUCT)

        every { userService.findById(TEST_USER_ID) } returns user
        every { couponService.findById(TEST_COUPON_ID) } returns coupon

        // when
        couponFacade.deleteByUserIdAndCouponId(criteria)

        // then
        verify(exactly = 1) { userService.findById(TEST_USER_ID) }
        verify(exactly = 1) { couponService.findById(TEST_COUPON_ID) }
        verify(exactly = 1) { couponService.deleteUserCouponByUserIdAndCouponId(TEST_USER_ID, TEST_COUPON_ID) }
        verify(exactly = 1) { couponService.delete(TEST_COUPON_ID) }
    }

    @Test
    @DisplayName("사용자 ID로 모든 쿠폰 삭제 성공")
    fun deleteAllCouponsByUserIdSuccess() {
        // given
        val user = createTestUser(TEST_USER_ID)
        val userCoupon = createTestUserCoupon(TEST_USER_COUPON_ID, TEST_USER_ID, TEST_COUPON_ID)

        every { userService.findById(TEST_USER_ID) } returns user
        every { couponService.findUserCouponsByUserId(TEST_USER_ID) } returns listOf(userCoupon)

        // when
        couponFacade.deleteAllByUserId(TEST_USER_ID)

        // then
        verify(exactly = 1) { userService.findById(TEST_USER_ID) }
        verify(exactly = 1) { couponService.findUserCouponsByUserId(TEST_USER_ID) }
        verify(exactly = 1) { couponService.deleteAll(listOf(TEST_COUPON_ID)) }
        verify(exactly = 1) { couponService.deleteAllUserCouponByUserId(TEST_USER_ID) }
    }

    // 테스트 유틸리티 메서드
    private fun createTestUser(id: Long): User {
        val user = mockk<User>()
        every { user.id } returns id
        return user
    }

    private fun createTestCoupon(
        id: Long,
        code: String = TEST_CODE,
        couponType: CouponType = CouponType.DISCOUNT_PRODUCT,
        startDate: LocalDateTime = LocalDateTime.now(),
        endDate: LocalDateTime = LocalDateTime.now().plusDays(30)
    ): Coupon {
        val coupon = mockk<Coupon>()
        every { coupon.id } returns id
        every { coupon.code } returns code
        every { coupon.couponType } returns couponType
        every { coupon.discountRate } returns TEST_DISCOUNT_RATE
        every { coupon.description } returns TEST_DESCRIPTION
        every { coupon.startDate } returns startDate
        every { coupon.endDate } returns endDate
        return coupon
    }

    private fun createTestUserCoupon(
        id: Long,
        userId: Long,
        couponId: Long,
        issued: Boolean = false,
        used: Boolean = false
    ): UserCoupon {
        val userCoupon = mockk<UserCoupon>()
        every { userCoupon.id } returns id
        every { userCoupon.userId } returns userId
        every { userCoupon.couponId } returns couponId
        every { userCoupon.issueDate } returns LocalDateTime.now()
        every { userCoupon.issued } returns issued
        every { userCoupon.used } returns used
        return userCoupon
    }
} 