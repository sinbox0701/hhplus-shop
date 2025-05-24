package kr.hhplus.be.server.order

import kr.hhplus.be.server.application.order.OrderCriteria
import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.application.order.OrderResult
import kr.hhplus.be.server.domain.order.OrderEventPublisher
import kr.hhplus.be.server.domain.order.service.OrderService
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.service.ProductCommand
import kr.hhplus.be.server.domain.product.service.ProductOptionCommand
import kr.hhplus.be.server.domain.product.service.ProductOptionService
import kr.hhplus.be.server.domain.product.service.ProductService
import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.user.service.AccountCommand
import kr.hhplus.be.server.domain.user.service.AccountService
import kr.hhplus.be.server.domain.user.service.UserCommand
import kr.hhplus.be.server.domain.user.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = ["spring.jpa.properties.hibernate.order_inserts=true", "spring.jpa.properties.hibernate.order_updates=true"])
class ConcurrencyOrderFacadeTest {

    /**
     * 이벤트 테스트용 설정 클래스 추가
     */
    @Configuration
    class TestConfig {
        
        @Bean
        @Primary
        fun testOrderEventPublisher(): OrderEventPublisher {
            // 테스트용 이벤트 발행자 - 실제 이벤트 발행하지 않고 로깅만 함
            return object : OrderEventPublisher {
                override fun publish(event: kr.hhplus.be.server.domain.order.event.OrderEvent) {
                    println("Test event published: $event")
                }
            }
        }
    }

    @Autowired
    private lateinit var orderFacade: OrderFacade

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var productOptionService: ProductOptionService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var accountService: AccountService

    @Autowired
    private lateinit var orderService: OrderService

    private lateinit var testUser: User
    private lateinit var testProduct: Product
    private lateinit var testProductOption: ProductOption

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

        // 테스트용 계좌 생성 - 충분한 잔액
        accountService.create(AccountCommand.CreateAccountCommand(
            userId = testUser.id!!,
            amount = 1000000.0
        ))

        // 테스트용 상품 생성
        val productCommand = ProductCommand.CreateProductCommand(
            name = "테스트 상품",
            description = "동시성 테스트용 상품",
            price = 1000.0
        )
        testProduct = productService.create(productCommand)

        // 테스트용 상품 옵션 생성 (한정 수량 20개)
        val optionCommand = ProductOptionCommand.CreateProductOptionCommand(
            productId = testProduct.id!!,
            name = "기본 옵션",
            availableQuantity = 20,
            additionalPrice = 0.0
        )
        testProductOption = productOptionService.create(optionCommand)
    }

    /**
     * 동시에 여러 스레드에서 동일한 상품에 대한 주문 생성 테스트
     * 동시성 문제가 없다면 재고는 정확히 감소해야 함
     */
    @Test
    fun `동시에 여러 요청이 동일한 상품을 주문할 때 재고가 정확히 관리되어야 한다`() {
        // given
        val threadCount = 10
        val orderQuantity = 1 // 각 주문당 1개씩
        val successCount = AtomicInteger(0)
        val initialQuantity = testProductOption.availableQuantity
        
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // when
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    // 주문 생성 요청
                    val orderItem = OrderCriteria.OrderItemCreateCriteria(
                        productId = testProduct.id!!,
                        productOptionId = testProductOption.id!!,
                        quantity = orderQuantity
                    )
                    
                    val createOrderCriteria = OrderCriteria.OrderCreateCriteria(
                        userId = testUser.id!!,
                        orderItems = listOf(orderItem),
                        userCouponId = null
                    )
                    
                    // 주문 생성
                    val orderResult = orderFacade.createOrder(createOrderCriteria)
                    
                    // 주문 결제
                    val paymentCriteria = OrderCriteria.OrderPaymentCriteria(
                        userId = testUser.id!!,
                        orderId = orderResult.order.id!!
                    )
                    
                    orderFacade.processPayment(paymentCriteria)
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
        val updatedOption = productOptionService.get(testProductOption.id!!)
        val expectedFinalQuantity = initialQuantity - (orderQuantity * successCount.get())
        
        assertEquals(expectedFinalQuantity, updatedOption.availableQuantity,
            "동시 주문 후 상품 재고가 정확히 감소해야 함")
        
        // 성공한 주문 수와 실제 주문 수가 일치해야 함
        val orders = orderService.getOrdersByUserId(testUser.id!!)
        assertEquals(successCount.get(), orders.size,
            "성공한 주문 수와 실제 주문 수가 일치해야 함")
    }

    /**
     * 동시에 여러 스레드에서 동일한 주문을 취소하는 테스트
     * 동시성 문제가 없다면 주문은 한 번만 취소되어야 함
     */
    @Test
    fun `동시에 여러 요청이 동일한 주문을 취소할 때 한 번만 성공해야 한다`() {
        // given
        // 먼저 주문 생성
        val orderItem = OrderCriteria.OrderItemCreateCriteria(
            productId = testProduct.id!!,
            productOptionId = testProductOption.id!!,
            quantity = 1
        )
        
        val createOrderCriteria = OrderCriteria.OrderCreateCriteria(
            userId = testUser.id!!,
            orderItems = listOf(orderItem),
            userCouponId = null
        )
        
        val orderResult = orderFacade.createOrder(createOrderCriteria)
        
        // 주문 결제
        val paymentCriteria = OrderCriteria.OrderPaymentCriteria(
            userId = testUser.id!!,
            orderId = orderResult.order.id!!
        )
        
        orderFacade.processPayment(paymentCriteria)
        
        // 동시 취소 테스트 준비
        val threadCount = 5
        val successCount = AtomicInteger(0)
        val initialOptionQuantity = productOptionService.get(testProductOption.id!!).availableQuantity
        
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // when
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    orderFacade.cancelOrder(orderResult.order.id!!, testUser.id!!)
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
        // 취소는 한 번만 성공해야 함
        assertEquals(1, successCount.get(), "주문 취소는 한 번만 성공해야 함")
        
        // 재고는 원래대로 복구되어야 함
        val updatedOption = productOptionService.get(testProductOption.id!!)
        val expectedQuantity = initialOptionQuantity + 1
        assertEquals(expectedQuantity, updatedOption.availableQuantity, 
            "주문 취소 후 재고가 정확히 복구되어야 함")
    }
    
    /**
     * 재고가 부족한 상황에서 동시에 여러 요청이 들어올 때 적절히 처리되는지 테스트
     */
    @Test
    fun `재고가 부족한 상황에서 동시에 여러 요청이 들어올 때 적절히 처리되어야 한다`() {
        // given
        // 재고를 5개로 제한
        val limitedQuantity = 5
        productOptionService.update(ProductOptionCommand.UpdateProductOptionCommand(
            id = testProductOption.id!!,
            name = testProductOption.name,
            additionalPrice = testProductOption.additionalPrice
        ))
        productOptionService.updateQuantity(ProductOptionCommand.UpdateQuantityCommand(
            id = testProductOption.id!!,
            quantity = limitedQuantity - testProductOption.availableQuantity
        ))
        
        val threadCount = 10 // 재고보다 많은 요청
        val orderQuantity = 1
        val successCount = AtomicInteger(0)
        
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        
        // when
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    val orderItem = OrderCriteria.OrderItemCreateCriteria(
                        productId = testProduct.id!!,
                        productOptionId = testProductOption.id!!,
                        quantity = orderQuantity
                    )
                    
                    val createOrderCriteria = OrderCriteria.OrderCreateCriteria(
                        userId = testUser.id!!,
                        orderItems = listOf(orderItem),
                        userCouponId = null
                    )
                    
                    val orderResult = orderFacade.createOrder(createOrderCriteria)
                    
                    val paymentCriteria = OrderCriteria.OrderPaymentCriteria(
                        userId = testUser.id!!,
                        orderId = orderResult.order.id!!
                    )
                    
                    orderFacade.processPayment(paymentCriteria)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    // 재고 부족 예외는 정상적으로 처리됨
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // 모든 스레드 완료 대기
        latch.await()
        executor.shutdown()
        
        // then
        // 성공한 주문 수는 초기 재고량을 초과할 수 없음
        val finalOption = productOptionService.get(testProductOption.id!!)
        
        assertEquals(limitedQuantity, successCount.get(), 
            "성공한 주문 수는 초기 재고량과 일치해야 함")
        assertEquals(0, finalOption.availableQuantity, 
            "모든 재고가 소진되어야 함")
    }
} 