package kr.hhplus.be.server.product

import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.service.ProductCommand
import kr.hhplus.be.server.domain.product.service.ProductOptionCommand
import kr.hhplus.be.server.domain.product.service.ProductOptionService
import kr.hhplus.be.server.domain.product.service.ProductService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@ActiveProfiles("test")
class ConcurrencyProductOptionServiceTest {

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var productOptionService: ProductOptionService

    private lateinit var testProduct: Product
    private lateinit var testProductOption: ProductOption

    @BeforeEach
    fun setup() {
        // 테스트용 상품 생성
        val productCommand = ProductCommand.CreateProductCommand(
            name = "테스트 상품",
            description = "동시성 테스트용 상품",
            price = 1000.0
        )
        testProduct = productService.create(productCommand)

        // 테스트용 상품 옵션 생성 (초기 재고 100개)
        val optionCommand = ProductOptionCommand.CreateProductOptionCommand(
            productId = testProduct.id!!,
            name = "기본 옵션",
            availableQuantity = 100,
            additionalPrice = 0.0
        )
        testProductOption = productOptionService.create(optionCommand)
    }

    /**
     * 동시에 여러 스레드에서 상품 재고를 감소시키는 테스트
     * 동시성 문제가 없다면 최종 재고는 초기값 - (차감 수량 * 스레드 수)와 일치해야 함
     */
    @Test
    fun `동시에 여러 요청이 상품 재고를 차감할 때 정확한 재고가 유지되어야 한다`() {
        // given
        val threadCount = 10
        val decreaseQuantity = 2
        val expectedFinalQuantity = testProductOption.availableQuantity - (decreaseQuantity * threadCount)
        
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // when
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    val command = ProductOptionCommand.UpdateQuantityCommand(
                        id = testProductOption.id!!,
                        quantity = decreaseQuantity
                    )
                    productOptionService.subtractQuantity(command)
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
        val updatedOption = productOptionService.get(testProductOption.id!!)
        assertEquals(expectedFinalQuantity, updatedOption.availableQuantity,
            "동시 재고 차감 후 남은 재고량이 예상값과 일치해야 함")
    }

    /**
     * 동시에 여러 스레드에서 상품 재고를 증가시키는 테스트
     * 동시성 문제가 없다면 최종 재고는 초기값 + (증가 수량 * 스레드 수)와 일치해야 함
     */
    @Test
    fun `동시에 여러 요청이 상품 재고를 증가시킬 때 정확한 재고가 유지되어야 한다`() {
        // given
        val threadCount = 10
        val increaseQuantity = 2
        val expectedFinalQuantity = testProductOption.availableQuantity + (increaseQuantity * threadCount)
        
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // when
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    val command = ProductOptionCommand.UpdateQuantityCommand(
                        id = testProductOption.id!!,
                        quantity = increaseQuantity
                    )
                    productOptionService.updateQuantity(command)
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
        val updatedOption = productOptionService.get(testProductOption.id!!)
        assertEquals(expectedFinalQuantity, updatedOption.availableQuantity,
            "동시 재고 증가 후 남은 재고량이 예상값과 일치해야 함")
    }

    /**
     * 재고가 0이 되는 상황에서 동시에 여러 요청이 재고를 차감하려 할 때 테스트
     * 동시성 문제가 없다면 재고는 0 이하로 내려가지 않아야 함
     */
    @Test
    fun `재고가 0이 되는 상황에서 동시에 여러 요청이 재고를 차감하려 할 때 적절히 처리되어야 한다`() {
        // given
        // 정확히 계산된 재고 설정 (10개)
        val initialStock = 10
        val decreaseQuantity = 1
        
        // 새 상품과 옵션 생성으로 정확한 테스트 환경 구성
        val productCommand = ProductCommand.CreateProductCommand(
            name = "제한 재고 상품",
            description = "재고 소진 테스트용 상품",
            price = 500.0
        )
        val product = productService.create(productCommand)

        val optionCommand = ProductOptionCommand.CreateProductOptionCommand(
            productId = product.id!!,
            name = "기본 옵션",
            availableQuantity = initialStock,
            additionalPrice = 0.0
        )
        val productOption = productOptionService.create(optionCommand)
        
        // 재고보다 많은 스레드로 동시에 시도
        val threadCount = initialStock + 5
        val successCount = AtomicInteger(0)
        
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // when
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    val command = ProductOptionCommand.UpdateQuantityCommand(
                        id = productOption.id!!,
                        quantity = decreaseQuantity
                    )
                    productOptionService.subtractQuantity(command)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    // 재고 부족 예외는 정상적으로 처리
                    println("Expected error in thread $i: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 스레드 완료 대기
        latch.await()
        executor.shutdown()

        // then
        val updatedOption = productOptionService.get(productOption.id!!)
        
        // 성공한 재고 차감 횟수는 초기 재고량을 초과할 수 없음
        assertEquals(initialStock, successCount.get(),
            "성공한 재고 차감 횟수는 초기 재고량과 일치해야 함")
        
        // 재고는 0이어야 함 (음수가 되면 안 됨)
        assertEquals(0, updatedOption.availableQuantity,
            "모든 재고가 소진되어 재고량은 0이어야 함")
    }
} 