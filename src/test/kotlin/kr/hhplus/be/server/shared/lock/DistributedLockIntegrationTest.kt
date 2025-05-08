package kr.hhplus.be.server.shared.lock

import kr.hhplus.be.server.application.coupon.CouponCriteria
import kr.hhplus.be.server.application.coupon.CouponFacade
import kr.hhplus.be.server.domain.coupon.model.CouponType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class DistributedLockIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val redisContainer = GenericContainer(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379)

        @DynamicPropertySource
        @JvmStatic
        fun registerRedisProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.redis.host") { redisContainer.host }
            registry.add("spring.redis.port") { redisContainer.getMappedPort(6379) }
        }
    }

    @Autowired
    private lateinit var couponFacade: CouponFacade
    
    @BeforeEach
    fun setUp() {
        // 테스트 전 설정 - 필요한 경우 데이터 초기화 등
    }

    @AfterEach
    fun tearDown() {
        // 테스트 후 정리 작업
    }

    @Test
    fun `여러 스레드에서 동시에 같은 사용자의 쿠폰을 생성할 때 분산락이 동작해야 한다`() {
        // 테스트 설정
        val threadCount = 10
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(1)
        val completionLatch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        
        // 동일한 사용자에 대한 쿠폰 생성 요청 준비
        val userId = 1L
        
        // 여러 스레드에서 동시에 쿠폰 생성 요청
        for (i in 1..threadCount) {
            executorService.submit {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await()
                    
                    val criteria = CouponCriteria.CreateUserCouponCommand(
                        userId = userId,
                        code = "TEST_COUPON_${System.currentTimeMillis()}_$i",
                        description = "테스트 쿠폰 $i",
                        couponType = CouponType.DISCOUNT_ORDER,
                        discountRate = 10.0,
                        startDate = LocalDateTime.now(),
                        endDate = LocalDateTime.now().plusDays(30),
                        userCouponQuantity = 1,
                        quantity = 1
                    )
                    
                    couponFacade.create(criteria)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    // 락 획득 실패 등의 예외 발생 시
                    failCount.incrementAndGet()
                    println("쿠폰 생성 실패: ${e.message}")
                } finally {
                    completionLatch.countDown()
                }
            }
        }
        
        // 모든 스레드 동시 시작
        latch.countDown()
        
        // 모든 스레드 완료 대기 (최대 10초)
        completionLatch.await(10, TimeUnit.SECONDS)
        executorService.shutdown()
        
        // 검증: 정확히 하나의 쿠폰만 생성되었는지 확인
        // 분산락이 제대로 작동한다면 하나의 요청만 성공해야 함
        val userCoupons = couponFacade.findByUserId(userId)
        
        println("성공한 요청 수: ${successCount.get()}")
        println("실패한 요청 수: ${failCount.get()}")
        println("실제 생성된 쿠폰 수: ${userCoupons.size}")
        
        assertEquals(1, successCount.get(), "분산락이 제대로 작동한다면 하나의 요청만 성공해야 합니다")
        assertEquals(threadCount - 1, failCount.get(), "분산락이 제대로 작동한다면 나머지 요청은 실패해야 합니다")
        assertEquals(1, userCoupons.size, "실제로 생성된 쿠폰은 하나여야 합니다")
    }
    
    @Test
    fun `서로 다른 사용자의 쿠폰 생성은 분산락 경합 없이 모두 성공해야 한다`() {
        // 테스트 설정
        val userCount = 5
        val executorService = Executors.newFixedThreadPool(userCount)
        val latch = CountDownLatch(1)
        val completionLatch = CountDownLatch(userCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        
        // 서로 다른 사용자에 대한 쿠폰 생성 요청
        for (i in 1..userCount) {
            val userId = 100L + i // 서로 다른 사용자 ID
            
            executorService.submit {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await()
                    
                    val criteria = CouponCriteria.CreateUserCouponCommand(
                        userId = userId,
                        code = "TEST_COUPON_DIFF_USER_${System.currentTimeMillis()}_$i",
                        description = "다른 사용자 테스트 쿠폰 $i",
                        couponType = CouponType.DISCOUNT_PRODUCT,
                        discountRate = 15.0,
                        startDate = LocalDateTime.now(),
                        endDate = LocalDateTime.now().plusDays(14),
                        userCouponQuantity = 1,
                        quantity = 1
                    )
                    
                    couponFacade.create(criteria)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                    println("쿠폰 생성 실패 (사용자 $userId): ${e.message}")
                } finally {
                    completionLatch.countDown()
                }
            }
        }
        
        // 모든 스레드 동시 시작
        latch.countDown()
        
        // 모든 스레드 완료 대기 (최대 10초)
        completionLatch.await(10, TimeUnit.SECONDS)
        executorService.shutdown()
        
        // 검증: 모든 요청이 성공해야 함 (분산락 경합이 없으므로)
        println("성공한 요청 수: ${successCount.get()}")
        println("실패한 요청 수: ${failCount.get()}")
        
        assertEquals(userCount, successCount.get(), "서로 다른 사용자의 쿠폰 생성은 모두 성공해야 합니다")
        assertEquals(0, failCount.get(), "분산락 경합이 없으므로 실패가 없어야 합니다")
    }
} 