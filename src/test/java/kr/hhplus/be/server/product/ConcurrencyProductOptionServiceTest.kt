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
} 