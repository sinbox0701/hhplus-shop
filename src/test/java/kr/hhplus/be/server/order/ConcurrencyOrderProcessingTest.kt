package kr.hhplus.be.server.order

import kr.hhplus.be.server.application.order.OrderCriteria
import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.domain.order.model.OrderStatus
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

@SpringBootTest
@ActiveProfiles("test")
class ConcurrencyOrderProcessingTest {

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

        // 테스트용 계좌 생성 - 정확한 잔액 설정
        accountService.create(AccountCommand.CreateAccountCommand(
            userId = testUser.id!!,
            amount = 5000.0 // 주문 동시성 테스트용 금액
        ))

        // 테스트용 상품 생성
        val productCommand = ProductCommand.CreateProductCommand(
            name = "테스트 상품",
            description = "동시성 테스트용 상품",
            price = 1000.0
        )
        testProduct = productService.create(productCommand)

        // 테스트용 상품 옵션 생성 (한정 수량 5개)
        val optionCommand = ProductOptionCommand.CreateProductOptionCommand(
            productId = testProduct.id!!,
            name = "기본 옵션",
            availableQuantity = 5,
            additionalPrice = 0.0
        )
        testProductOption = productOptionService.create(optionCommand)
    }

    /**
     * 동시에 여러 요청이 동일한 계좌로 결제를 시도하는 테스트
     * 동시성 문제가 없다면 잔액 범위 내의 주문만 성공해야 함
     */
    @Test
    fun `동시에 여러 요청이 동일한 계좌로 결제할 때 정확한 잔액이 유지되어야 한다`() {
        // given
        val threadCount = 10
        val orderPrice = 1000.0 // 각 주문당 1000원
        val successCount = AtomicInteger(0)
        val initialBalance = accountService.findByUserId(testUser.id!!).amount
        val expectedSuccessCount = (initialBalance / orderPrice).toInt()
        
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        
        val orderIds = ArrayList<Long>()

        // when
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    // 주문 생성 요청
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
                    
                    // 주문 생성
                    val orderResult = orderFacade.createOrder(createOrderCriteria)
                    
                    synchronized(orderIds) {
                        orderIds.add(orderResult.order.id!!)
                    }
                    
                    // 주문 결제 시도
                    try {
                        val paymentCriteria = OrderCriteria.OrderPaymentCriteria(
                            userId = testUser.id!!,
                            orderId = orderResult.order.id!!
                        )
                        
                        orderFacade.processPayment(paymentCriteria)
                        successCount.incrementAndGet()
                    } catch (e: Exception) {
                        println("Payment failed in thread $i: ${e.message}")
                    }
                } catch (e: Exception) {
                    println("Order creation failed in thread $i: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 스레드 완료 대기
        latch.await()
        executor.shutdown()

        // then
        // 1. 성공한 결제 횟수는 초기 잔액으로 가능한 결제 횟수와 같아야 함
        assertEquals(expectedSuccessCount, successCount.get(),
            "성공한 결제 횟수는 초기 잔액으로 가능한 결제 횟수와 일치해야 함")
        
        // 2. 계좌 잔액은 정확히 남아있어야 함
        val finalBalance = accountService.findByUserId(testUser.id!!).amount
        val expectedFinalBalance = initialBalance - (orderPrice * successCount.get())
        assertEquals(expectedFinalBalance, finalBalance, 0.01,
            "최종 계좌 잔액이 예상값과 일치해야 함")
        
        // 3. 주문 상태 확인 - 성공한 주문은 COMPLETED, 나머지는 PENDING 상태여야 함
        val completedOrders = orderIds.filter { 
            orderService.getOrder(it).status == OrderStatus.COMPLETED 
        }
        assertEquals(successCount.get(), completedOrders.size,
            "COMPLETED 상태의 주문 수는 성공한 결제 수와 일치해야 함")
    }

    /**
     * 동시에 여러 요청이 재고가 부족한 상품을 주문하는 테스트
     * 동시성 문제가 없다면 재고 범위 내의 주문만 성공해야 함
     */
    @Test
    fun `동시에 여러 요청이 한정 재고 상품을 주문할 때 정확한 재고 관리가 되어야 한다`() {
        // given
        val threadCount = 10
        val initialStock = testProductOption.availableQuantity
        val successCount = AtomicInteger(0)
        val firstErrorMessage = AtomicReference<String>()
        
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
                        quantity = 1
                    )
                    
                    val createOrderCriteria = OrderCriteria.OrderCreateCriteria(
                        userId = testUser.id!!,
                        orderItems = listOf(orderItem),
                        userCouponId = null
                    )
                    
                    // 주문 생성 시도
                    val orderResult = orderFacade.createOrder(createOrderCriteria)
                    
                    // 주문 결제
                    val paymentCriteria = OrderCriteria.OrderPaymentCriteria(
                        userId = testUser.id!!,
                        orderId = orderResult.order.id!!
                    )
                    
                    orderFacade.processPayment(paymentCriteria)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    // 재고 부족 메시지 기록
                    if (firstErrorMessage.get() == null) {
                        firstErrorMessage.set(e.message)
                    }
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
        // 1. 성공한 주문 수는 초기 재고량을 초과하면 안 됨
        assertEquals(initialStock, successCount.get(),
            "성공한 주문 수는 초기 재고량과 일치해야 함")
        
        // 2. 재고가 모두 소진되어야 함
        val updatedOption = productOptionService.get(testProductOption.id!!)
        assertEquals(0, updatedOption.availableQuantity,
            "재고가 모두 소진되어야 함")
        
        // 3. 재고 부족 오류 메시지가 있어야 함
        assertTrue(firstErrorMessage.get()?.contains("재고가 부족") == true ||
                 firstErrorMessage.get()?.contains("available") == true,
            "재고 부족 관련 오류 메시지가 나타나야 함")
    }
} 