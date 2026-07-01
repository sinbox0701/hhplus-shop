package kr.hhplus.be.server.order

import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.service.ProductCommand
import kr.hhplus.be.server.domain.product.service.ProductOptionCommand
import kr.hhplus.be.server.domain.product.service.ProductOptionService
import kr.hhplus.be.server.domain.product.service.ProductService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 실제 Spring 컨텍스트를 사용한 "주문 시 재고 차감" 동시성 테스트.
 *
 * 실제 ProductService/ProductOptionService + 실제 JPA 리포지토리
 * (ProductOptionRepositoryImpl -> JpaProductOptionRepository)를 사용하여
 * H2(MODE=MySQL) 위에서 여러 스레드가 동시에 같은 상품 옵션의 재고를 차감(주문)하도록 한다.
 *
 * 프로덕션 가드는 JpaProductOptionRepository.findByIdWithPessimisticLock의
 * @Lock(LockModeType.PESSIMISTIC_WRITE) 이며, ProductOptionService.subtractQuantityWithPessimisticLock
 * 이 이를 통해 "조회 -> 재고 검증/차감 -> 저장"을 하나의 트랜잭션 내에서 원자적으로 수행한다.
 * 이 락을 제거하면 재고보다 많은 주문이 동시에 성공하는 오버셀(overselling)이 발생하므로
 * 이 테스트가 실패해야 한다.
 */
@SpringBootTest
@ActiveProfiles("test")
class ConcurrencyOrderTest {

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var productOptionService: ProductOptionService

    private lateinit var testProduct: Product
    private lateinit var testProductOption: ProductOption

    @BeforeEach
    fun setup() {
        val productCommand = ProductCommand.CreateProductCommand(
            name = "동시성 주문 테스트 상품",
            description = "재고 10개 한정 상품",
            price = BigDecimal("1000")
        )
        testProduct = productService.create(productCommand)

        val optionCommand = ProductOptionCommand.CreateProductOptionCommand(
            productId = testProduct.id!!,
            name = "기본",
            availableQuantity = 10,
            additionalPrice = BigDecimal.ZERO
        )
        testProductOption = productOptionService.create(optionCommand)
    }

    @Test
    fun `동시에 여러 요청이 동일한 상품 옵션을 주문할 때 정확히 재고만큼만 성공해야 한다`() {
        // given: 재고 10개, 20명이 동시에 1개씩 주문 시도
        val numberOfThreads = 20
        val orderQuantity = 1
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        val exceptionList = mutableListOf<String>()

        // when
        for (i in 1..numberOfThreads) {
            executor.submit {
                try {
                    productOptionService.subtractQuantityWithPessimisticLock(testProductOption.id!!, orderQuantity)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                    synchronized(exceptionList) {
                        exceptionList.add("주문 실패 (thread $i): ${e.message}")
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

        val updatedOption = productOptionService.get(testProductOption.id!!)

        // 재고는 절대 음수가 될 수 없고, 정확히 소진되어 0이어야 함
        assertEquals(0, updatedOption.availableQuantity)
        // 정확히 재고 수량(10)만큼만 성공해야 함 (오버셀 방지)
        assertEquals(10, successCount.get())
        // 나머지는 재고 부족으로 실패해야 함
        assertEquals(numberOfThreads - 10, failCount.get())
    }

    @Test
    fun `재고보다 많은 수량을 동시에 주문 시도해도 재고는 음수가 되지 않아야 한다`() {
        // given: 재고 5개, 각 주문이 2개씩, 10명이 동시에 시도 (총 요청량 20 > 재고 5)
        val optionCommand = ProductOptionCommand.CreateProductOptionCommand(
            productId = testProduct.id!!,
            name = "제한재고",
            availableQuantity = 5,
            additionalPrice = BigDecimal.ZERO
        )
        val limitedOption = productOptionService.create(optionCommand)

        val numberOfThreads = 10
        val orderQuantity = 2
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        // when
        for (i in 1..numberOfThreads) {
            executor.submit {
                try {
                    productOptionService.subtractQuantityWithPessimisticLock(limitedOption.id!!, orderQuantity)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS))
        executor.shutdown()

        // then
        val updatedOption = productOptionService.get(limitedOption.id!!)

        // 재고(5)를 2개씩 차감하면 최대 2번만 성공 가능 (5 / 2 = 2, 나머지 1은 부족하여 실패)
        assertEquals(2, successCount.get())
        assertEquals(numberOfThreads - 2, failCount.get())
        assertTrue(updatedOption.availableQuantity >= 0) {
            "재고는 음수가 될 수 없습니다 (실제: ${updatedOption.availableQuantity})"
        }
        assertEquals(1, updatedOption.availableQuantity)
    }
}
