package kr.hhplus.be.server.coupon

import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.coupon.service.CouponCommand
import kr.hhplus.be.server.domain.coupon.service.CouponService
import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.user.service.UserCommand
import kr.hhplus.be.server.domain.user.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@ActiveProfiles("test")
class ConcurrencyCouponServiceTest {

    @Autowired
    private lateinit var couponService: CouponService

    @Autowired
    private lateinit var userService: UserService

    private lateinit var testCoupon: Coupon
    private lateinit var testUser: User

    @BeforeEach
    fun setup() {
        // 테스트용 사용자 생성
        val userCommand = UserCommand.CreateUserCommand(
            name = "테스트 사용자",
            email = "test${System.currentTimeMillis()}@example.com",
            loginId = "testuser${System.currentTimeMillis()}",
            password = "password123"
        )
        testUser = userService.create(userCommand)

        // 테스트용 쿠폰 생성 (총 50개 수량)
        val couponCommand = CouponCommand.CreateCouponCommand(
            code = "TEST${System.currentTimeMillis()}",
            couponType = CouponType.DISCOUNT_ORDER,
            discountRate = 10.0,
            description = "동시성 테스트용 쿠폰",
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusDays(7),
            quantity = 50
        )
        testCoupon = couponService.create(couponCommand)
    }

    /**
     * 동시에 여러 스레드에서 쿠폰을 발급받는 테스트
     * 동시성 문제가 없다면 최종 남은 쿠폰 수량은 초기값 - 성공한 발급 수와 일치해야 함
     */
    @Test
    fun `동시에 여러 요청이 쿠폰을 발급받을 때 정확한 수량이 유지되어야 한다`() {
        // given
        val threadCount = 20 // 스레드 수가 쿠폰 수량보다 적어야 함
        val successCount = AtomicInteger(0)
        
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // when
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    // 사용자 쿠폰 생성
                    val createUserCouponCommand = CouponCommand.CreateUserCouponCommand(
                        userId = testUser.id!!,
                        couponId = testCoupon.id!!,
                        quantity = 1
                    )
                    
                    // 쿠폰 발급
                    couponService.createUserCoupon(createUserCouponCommand)
                    
                    // 쿠폰 수량 감소
                    val updateQuantityCommand = CouponCommand.UpdateCouponRemainingQuantityCommand(
                        id = testCoupon.id!!,
                        quantity = 1
                    )
                    couponService.updateRemainingQuantity(updateQuantityCommand)
                    
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    println("Error in thread $i: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 스레드 완료 대기
        latch.await()
        executor.shutdown()

        // then
        val updatedCoupon = couponService.findById(testCoupon.id!!)
        val expectedRemainingQuantity = testCoupon.quantity - successCount.get()
        assertEquals(expectedRemainingQuantity, updatedCoupon.quantity,
            "동시 쿠폰 발급 후 남은 쿠폰 수량이 예상값과 일치해야 함")
    }

    /**
     * 동시에 여러 스레드에서 특정 사용자의 쿠폰을 사용하는 테스트
     * 동시성 문제가 없다면 쿠폰은 1번만 사용되어야 함
     */
    @Test
    fun `동시에 여러 요청이 같은 쿠폰을 사용할 때 한 번만 성공해야 한다`() {
        // given
        // 사용자의 쿠폰 먼저 생성
        val createUserCouponCommand = CouponCommand.CreateUserCouponCommand(
            userId = testUser.id!!,
            couponId = testCoupon.id!!,
            quantity = 1
        )
        val userCoupon = couponService.createUserCoupon(createUserCouponCommand)
        
        // 쿠폰 발급 처리
        val issueCouponCommand = CouponCommand.IssueCouponCommand(
            id = userCoupon.id!!,
            couponStartDate = LocalDateTime.now(),
            couponEndDate = LocalDateTime.now().plusDays(7)
        )
        couponService.issueUserCoupon(issueCouponCommand)
        
        // 여러 스레드에서 동시에 쿠폰 사용
        val threadCount = 5
        val successCount = AtomicInteger(0)
        
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // when
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    couponService.useUserCoupon(userCoupon.id!!)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    println("Error in thread $i: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 스레드 완료 대기
        latch.await()
        executor.shutdown()

        // then
        // 쿠폰은 한 번만 성공적으로 사용되어야 함
        assertEquals(1, successCount.get(), "쿠폰은 한 번만 성공적으로 사용되어야 함")
        
        // 쿠폰 상태 확인
        val updatedUserCoupon = couponService.findUserCouponById(userCoupon.id!!)
        assertEquals(true, updatedUserCoupon.isUsed(), "쿠폰이 사용됨 상태여야 함")
    }
} 