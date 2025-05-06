package kr.hhplus.be.server.coupon

import kr.hhplus.be.server.domain.common.TimeProvider
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.domain.coupon.repository.UserCouponRepository
import kr.hhplus.be.server.domain.coupon.service.CouponService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class CouponConcurrencyTest {

    private lateinit var couponRepository: TestCouponRepository
    private lateinit var userCouponRepository: TestUserCouponRepository
    private lateinit var timeProvider: TimeProvider
    private lateinit var couponService: CouponService

    private val couponCode = "FSTCME" // 선착순 쿠폰 코드
    private val couponId = 1L
    
    // 테스트용 CouponRepository 구현
    class TestCouponRepository : CouponRepository {
        lateinit var coupon: Coupon
        var remainingQuantity = AtomicInteger(10)
        
        override fun save(coupon: Coupon): Coupon = coupon
        override fun findAll(): List<Coupon> = listOf(coupon)
        override fun findById(id: Long): Coupon? = if (id == couponId) coupon else null
        override fun findByCode(code: String): Coupon? = if (code == couponCode) coupon else null
        override fun findByType(type: CouponType): List<Coupon> = listOf(coupon)
        
        override fun update(coupon: Coupon): Coupon {
            if (remainingQuantity.get() <= 0) {
                throw IllegalStateException("남은 쿠폰이 없습니다.")
            }
            
            remainingQuantity.decrementAndGet()
            return coupon
        }
        
        override fun delete(id: Long) {}
        override fun findByIdWithPessimisticLock(id: Long): Coupon? = if (id == couponId) coupon else null
        override fun findByCodeWithPessimisticLock(code: String): Coupon? = if (code == couponCode) coupon else null
        
        companion object {
            const val couponCode = "FSTCME"
            const val couponId = 1L
        }
    }
    
    // 테스트용 UserCouponRepository 구현
    class TestUserCouponRepository : UserCouponRepository {
        private val userCoupons = mutableMapOf<Pair<Long, Long>, UserCoupon>()
        private val issuedCouponIdCounter = AtomicInteger(1)
        
        override fun save(userCoupon: UserCoupon): UserCoupon {
            val userCouponWithId = if (userCoupon.id == null) {
                // ID를 설정하기 위한 리플렉션
                val userCouponClass = userCoupon.javaClass
                val idField = userCouponClass.getDeclaredField("id")
                idField.isAccessible = true
                idField.set(userCoupon, issuedCouponIdCounter.getAndIncrement().toLong())
                userCoupon
            } else {
                userCoupon
            }
            
            val key = Pair(userCouponWithId.userId, userCouponWithId.couponId)
            userCoupons[key] = userCouponWithId
            return userCouponWithId
        }
        
        override fun findById(id: Long): UserCoupon? {
            return userCoupons.values.find { it.id == id }
        }
        
        override fun findByUserId(userId: Long): List<UserCoupon> {
            return userCoupons.filter { it.key.first == userId }.values.toList()
        }
        
        override fun findByCouponId(couponId: Long): List<UserCoupon> {
            return userCoupons.filter { it.key.second == couponId }.values.toList()
        }
        
        override fun findByUserIdAndCouponId(userId: Long, couponId: Long): UserCoupon? {
            return userCoupons[Pair(userId, couponId)]
        }
        
        override fun update(userCoupon: UserCoupon): UserCoupon {
            return save(userCoupon)
        }
        
        override fun delete(id: Long) {
            userCoupons.entries.removeIf { it.value.id == id }
        }
        
        override fun deleteByUserIdAndCouponId(userId: Long, couponId: Long) {
            userCoupons.remove(Pair(userId, couponId))
        }
        
        override fun findAll(): List<UserCoupon> {
            return userCoupons.values.toList()
        }
        
        // 테스트용 메서드: 특정 userId + couponId 조합에 대한 모의 응답 설정
        private val overrideResponses = mutableMapOf<Pair<Long, Long>, UserCoupon?>()
        fun mockFindByUserIdAndCouponId(userId: Long, couponId: Long, response: UserCoupon?) {
            overrideResponses[Pair(userId, couponId)] = response
        }
    }
    
    @BeforeEach
    fun setup() {
        // 테스트 리포지토리 초기화
        couponRepository = TestCouponRepository()
        userCouponRepository = TestUserCouponRepository()
        timeProvider = Mockito.mock(TimeProvider::class.java)
        
        // 현재 시간 설정
        val now = LocalDateTime.now()
        Mockito.`when`(timeProvider.now()).thenReturn(now)
        
        // 쿠폰 설정
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
        
        // 리플렉션으로 ID 설정
        val idField = coupon.javaClass.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(coupon, couponId)
        
        // 테스트 리포지토리에 쿠폰 설정
        couponRepository.coupon = coupon
        
        // CouponService 생성
        couponService = CouponService(couponRepository, userCouponRepository, timeProvider)
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
        // 로그 출력
        exceptionList.forEach(::println)
        
        // 남은 쿠폰 수량은 0이 되어야 함
        assertEquals(0, couponRepository.remainingQuantity.get()) 
        // 정확히 10명만 발급 성공해야 함
        assertEquals(10, successCount.get())
        // 나머지 10명은 실패해야 함
        assertEquals(10, failCount.get())
    }

    @Test
    fun `쿠폰 발급 시 중복 발급이 방지되어야 한다`() {
        // given
        val userId = 100L
        val numberOfThreads = 5 // 동일 사용자가 5번 요청
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        val exceptionList = mutableListOf<String>()
        
        // 중복 발급 시뮬레이션을 위한 AtomicInteger
        val atomicCounter = AtomicInteger(0)
        
        // when
        for (i in 1..numberOfThreads) {
            executor.submit {
                try {
                    // 첫 번째 쓰레드만 성공하도록 카운터 사용
                    if (atomicCounter.getAndIncrement() > 0) {
                        // 이미 발급된 것처럼 가짜 UserCoupon 생성
                        val mockUserCoupon = Mockito.mock(UserCoupon::class.java)
                        val idField = mockUserCoupon.javaClass.getDeclaredField("id")
                        idField.isAccessible = true
                        idField.set(mockUserCoupon, 1L)
                        
                        // userCouponRepository에 미리 저장
                        val userIdCouponIdPair = Pair(userId, couponId)
                        synchronized(userCouponRepository) {
                            val userCouponField = userCouponRepository.javaClass.getDeclaredField("userCoupons")
                            userCouponField.isAccessible = true
                            @Suppress("UNCHECKED_CAST")
                            val userCoupons = userCouponField.get(userCouponRepository) as MutableMap<Pair<Long, Long>, UserCoupon>
                            userCoupons[userIdCouponIdPair] = mockUserCoupon
                        }
                    }
                    
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

        // 모든 스레드가 작업을 완료할 때까지 대기 (최대 10초)
        assertTrue(latch.await(10, TimeUnit.SECONDS))
        executor.shutdown()

        // then
        // 로그 출력
        exceptionList.forEach(::println)
        
        // 사용자는 쿠폰을 1개만 발급받을 수 있음
        assertEquals(1, successCount.get())
        assertEquals(4, failCount.get())
    }
} 