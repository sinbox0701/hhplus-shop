package kr.hhplus.be.server.coupon

import kr.hhplus.be.server.domain.common.TimeProvider
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.domain.coupon.service.CouponService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 실제 Spring 컨텍스트를 사용한 선착순 쿠폰 발급 동시성 테스트.
 *
 * 실제 CouponService + 실제 JPA 리포지토리(CouponRepositoryImpl -> JpaCouponRepository)를 사용하여
 * H2(MODE=MySQL) 위에서 여러 스레드가 동시에 같은 쿠폰 코드로 발급을 시도한다.
 *
 * 프로덕션 가드는 JpaCouponRepository.findByCodeWithPessimisticLock의
 * @Lock(LockModeType.PESSIMISTIC_WRITE) 이다. 이 락을 제거하면 재고 확인(hasRemainingQuantity)과
 * 수량 차감(decreaseQuantity) 사이에 경쟁 조건이 생겨 10개 한정 쿠폰이 10개보다 많이 발급될 수 있으므로
 * 이 테스트가 실패해야 한다.
 */
@SpringBootTest
@ActiveProfiles("test")
class CouponConcurrencyTest {

    @Autowired
    private lateinit var couponService: CouponService

    @Autowired
    private lateinit var couponRepository: CouponRepository

    @Autowired
    private lateinit var timeProvider: TimeProvider

    private val couponCode = "FSTCME"
    private var couponId: Long = 0L

    @BeforeEach
    fun setup() {
        val now = timeProvider.now()
        val coupon = Coupon.create(
            code = couponCode,
            discountRate = 10.0,
            description = "선착순 10명 한정 쿠폰",
            startDate = now.minusDays(1),
            endDate = now.plusDays(7),
            quantity = 10,
            couponType = CouponType.DISCOUNT_ORDER,
            timeProvider = timeProvider
        )
        val saved = couponRepository.save(coupon)
        couponId = saved.id!!
    }

    @Test
    fun `동시에 여러 사용자가 선착순 쿠폰을 신청할 때 정확히 지정된 수량만 발급되어야 한다`() {
        // given
        val numberOfThreads = 20 // 20명이 동시에 요청 (쿠폰은 10개만 있음)
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        val exceptionList = mutableListOf<String>()

        // when
        for (i in 1..numberOfThreads) {
            val userId = i.toLong()
            executor.submit {
                try {
                    couponService.issueFirstComeFirstServedCoupon(userId, couponCode)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                    synchronized(exceptionList) {
                        exceptionList.add("쿠폰 발급 실패 (user: $userId): ${e.message}")
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 스레드가 작업을 완료할 때까지 대기 (최대 10초)
        assertTrue(latch.await(10, TimeUnit.SECONDS))
        executor.shutdown()

        // then
        exceptionList.forEach(::println)

        val finalCoupon = couponService.findById(couponId)

        // 남은 쿠폰 수량은 0이 되어야 함 (음수가 되면 절대 안 됨)
        assertEquals(0, finalCoupon.remainingQuantity)
        // 정확히 10명만 발급 성공해야 함
        assertEquals(10, successCount.get())
        // 나머지 10명은 실패해야 함
        assertEquals(10, failCount.get())
    }

    @Test
    fun `쿠폰 발급 시 동일 사용자의 중복 발급이 방지되어야 한다`() {
        // given
        val userId = 100L
        val numberOfThreads = 5 // 동일 사용자가 5번 동시에 요청
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        val exceptionList = mutableListOf<String>()

        // when
        for (i in 1..numberOfThreads) {
            executor.submit {
                try {
                    couponService.issueFirstComeFirstServedCoupon(userId, couponCode)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                    synchronized(exceptionList) {
                        exceptionList.add("쿠폰 발급 실패: ${e.message}")
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS))
        executor.shutdown()

        // then
        exceptionList.forEach(::println)

        // 사용자는 쿠폰을 1개만 발급받을 수 있음
        assertEquals(1, successCount.get())
        assertEquals(numberOfThreads - 1, failCount.get())

        val userCoupons = couponService.findUserCouponsByUserId(userId)
        assertEquals(1, userCoupons.size)
    }
}
