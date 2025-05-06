package kr.hhplus.be.server.product

import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.repository.ProductOptionRepository
import kr.hhplus.be.server.domain.product.service.ProductOptionService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@ActiveProfiles("test")
class ProductOptionConcurrencyTest {

    @Autowired
    private lateinit var productOptionRepository: ProductOptionRepository

    @Autowired
    private lateinit var productOptionService: ProductOptionService

    private var productOptionId: Long = 0

    @BeforeEach
    @Transactional
    fun setup() {
        // 테스트용 상품 옵션 생성 (초기 재고 100개)
        val productOption = ProductOption.create(
            productId = 1L,
            name = "테스트옵션",
            availableQuantity = 100,
            additionalPrice = 1000.0
        )
        val savedOption = productOptionRepository.save(productOption)
        productOptionId = savedOption.id!!
    }

    @Test
    fun `동시에 여러 스레드에서 재고를 차감할 때 정확한 재고가 유지되어야 한다`() {
        // given
        val numberOfThreads = 10
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)

        // when
        for (i in 1..numberOfThreads) {
            executor.submit {
                try {
                    // 각 스레드는 상품 옵션에서 5개의 재고를 차감
                    productOptionService.subtractQuantityWithPessimisticLock(productOptionId, 5)
                } catch (e: Exception) {
                    // 예외 발생시 로그만 출력하고 계속 진행
                    println("재고 차감 중 오류 발생: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 스레드가 작업을 완료할 때까지 대기 (최대 10초)
        assertTrue(latch.await(10, TimeUnit.SECONDS))
        executor.shutdown()

        // then
        val updatedOption = productOptionRepository.findById(productOptionId)
        // 초기 재고 100개에서 10개 스레드가 각각 5개씩 차감했으므로 50개가 남아야 함
        assertEquals(50, updatedOption?.availableQuantity)
    }

    @Test
    fun `재고보다 많은 양을 차감하려고 하면 실패해야 한다`() {
        // given
        val numberOfThreads = 30
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCounter = AtomicInteger(0)

        // when
        for (i in 1..numberOfThreads) {
            executor.submit {
                try {
                    // 각 스레드는 상품 옵션에서 5개의 재고를 차감 (총 150개 차감 시도)
                    productOptionService.subtractQuantityWithPessimisticLock(productOptionId, 5)
                    successCounter.incrementAndGet()
                } catch (e: Exception) {
                    // 재고 부족 예외는 예상된 동작
                    println("재고 차감 중 오류 발생: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 스레드가 작업을 완료할 때까지 대기 (최대 10초)
        assertTrue(latch.await(10, TimeUnit.SECONDS))
        executor.shutdown()

        // then
        val updatedOption = productOptionRepository.findById(productOptionId)
        // 초기 재고 100개에서 최대 100개까지만 차감 가능
        assertEquals(0, updatedOption?.availableQuantity)
        // 성공 횟수는 최대 20회여야 함 (100개 재고를 5개씩 차감)
        assertEquals(20, successCounter.get())
    }
    
    @Test
    fun `동시에 여러 요청이 상품 재고를 증가시킬 때 정확한 재고가 유지되어야 한다`() {
        // given
        val numberOfThreads = 10
        val increaseQuantity = 5 // 각 스레드가 5개씩 증가
        val initialQuantity = productOptionRepository.findById(productOptionId)?.availableQuantity ?: 100
        val expectedFinalQuantity = initialQuantity + (numberOfThreads * increaseQuantity)
        
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCount = AtomicInteger(0)
        
        // when - 여러 스레드에서 동시에 재고 증가 시도
        for (i in 1..numberOfThreads) {
            executor.submit {
                try {
                    productOptionService.addQuantityWithPessimisticLock(productOptionId, increaseQuantity)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    println("재고 증가 실패: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // 모든 스레드가 작업을 완료할 때까지 대기
        assertTrue(latch.await(10, TimeUnit.SECONDS))
        executor.shutdown()
        
        // then
        // 모든 스레드가 성공해야 함
        assertEquals(numberOfThreads, successCount.get())
        
        // 최종 재고 검증
        val updatedOption = productOptionRepository.findById(productOptionId)
        assertEquals(expectedFinalQuantity, updatedOption?.availableQuantity)
    }
} 