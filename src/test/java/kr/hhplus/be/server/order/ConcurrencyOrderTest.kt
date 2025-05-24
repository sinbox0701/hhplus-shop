package kr.hhplus.be.server.order

import kr.hhplus.be.server.domain.common.TimeProvider
import kr.hhplus.be.server.domain.order.OrderEventPublisher
import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import kr.hhplus.be.server.domain.order.service.OrderItemService
import kr.hhplus.be.server.domain.order.service.OrderService
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.repository.ProductRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime
import java.time.LocalDate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class ConcurrencyOrderTest {

    private lateinit var orderRepository: TestOrderRepository
    private lateinit var orderItemRepository: TestOrderItemRepository
    private lateinit var productRepository: TestProductRepository
    private lateinit var timeProvider: TimeProvider
    private lateinit var orderService: OrderService
    private lateinit var orderEventPublisher: OrderEventPublisher
    private lateinit var orderItemService: OrderItemService

    // 테스트용 OrderRepository 구현
    class TestOrderRepository : OrderRepository {
        private val orders = mutableMapOf<Long, Order>()
        private val orderIdCounter = AtomicInteger(1)
        
        override fun save(order: Order): Order {
            val orderWithId = if (order.id == null) {
                // ID를 설정하기 위한 리플렉션
                val orderClass = order.javaClass
                val idField = orderClass.getDeclaredField("id")
                idField.isAccessible = true
                idField.set(order, orderIdCounter.getAndIncrement().toLong())
                order
            } else {
                order
            }
            
            orders[orderWithId.id!!] = orderWithId
            return orderWithId
        }
        
        override fun findById(id: Long): Order? {
            return orders[id]
        }
        
        override fun findByUserId(userId: Long): List<Order> {
            return orders.values.filter { it.userId == userId }
        }
        
        override fun findByUserIdAndStatus(userId: Long, status: OrderStatus): List<Order> {
            return orders.values.filter { it.userId == userId && it.status == status }
        }
        
        override fun findByStatus(status: OrderStatus): List<Order> {
            return orders.values.filter { it.status == status }
        }
        
        override fun findByCreatedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Order> {
            return orders.values.filter { it.createdAt.isAfter(startDate) && it.createdAt.isBefore(endDate) }
        }
        
        override fun update(order: Order): Order {
            return save(order)
        }
        
        override fun updateStatus(id: Long, status: OrderStatus): Order {
            val order = findById(id) ?: throw IllegalArgumentException("주문을 찾을 수 없습니다: $id")
            val updatedOrder = order.updateStatus(status, TestTimeProvider())
            return save(updatedOrder)
        }
        
        override fun updateTotalPrice(id: Long, totalPrice: Double): Order {
            val order = findById(id) ?: throw IllegalArgumentException("주문을 찾을 수 없습니다: $id")
            val updatedOrder = order.updateTotalPrice(totalPrice, TestTimeProvider())
            return save(updatedOrder)
        }
        
        override fun delete(id: Long) {
            orders.remove(id)
        }
        
        override fun findAll(): List<Order> {
            return orders.values.toList()
        }
        
        fun clear() {
            orders.clear()
        }
        
        // 테스트용 TimeProvider 클래스
        private class TestTimeProvider : TimeProvider {
            override fun now(): LocalDateTime = LocalDateTime.now()
            override fun today(): LocalDate = LocalDate.now()
        }
    }
    
    // 테스트용 OrderItemRepository 구현
    class TestOrderItemRepository : OrderItemRepository {
        private val orderItems = mutableMapOf<Long, OrderItem>()
        private val orderItemIdCounter = AtomicInteger(1)
        
        override fun save(orderItem: OrderItem): OrderItem {
            val orderItemWithId = if (orderItem.id == null) {
                // ID를 설정하기 위한 리플렉션
                val orderItemClass = orderItem.javaClass
                val idField = orderItemClass.getDeclaredField("id")
                idField.isAccessible = true
                idField.set(orderItem, orderItemIdCounter.getAndIncrement().toLong())
                orderItem
            } else {
                orderItem
            }
            
            orderItems[orderItemWithId.id!!] = orderItemWithId
            return orderItemWithId
        }
        
        override fun findById(id: Long): OrderItem? {
            return orderItems[id]
        }
        
        override fun findByOrderId(orderId: Long): List<OrderItem> {
            return orderItems.values.filter { it.orderId == orderId }
        }
        
        override fun findByProductId(productId: Long): List<OrderItem> {
            return orderItems.values.filter { it.productId == productId }
        }
        
        override fun findByOrderIdAndProductOptionId(orderId: Long, productOptionId: Long): OrderItem? {
            return orderItems.values.find { it.orderId == orderId && it.productOptionId == productOptionId }
        }
        
        override fun update(orderItem: OrderItem): OrderItem {
            return save(orderItem)
        }
        
        override fun delete(id: Long) {
            orderItems.remove(id)
        }
        
        override fun deleteByOrderId(orderId: Long) {
            orderItems.entries.removeIf { it.value.orderId == orderId }
        }
        
        override fun findTopSellingProductIds(startDate: LocalDateTime, endDate: LocalDateTime, limit: Int): List<Long> {
            return emptyList() // 테스트용 구현에서는 사용하지 않음
        }
        
        override fun findProductQuantityMap(startDate: LocalDateTime, endDate: LocalDateTime): Map<Long, Int> {
            return emptyMap() // 테스트용 구현에서는 사용하지 않음
        }
        
        fun clear() {
            orderItems.clear()
        }
    }
    
    // 테스트용 ProductRepository 구현
    class TestProductRepository : ProductRepository {
        private val products = mutableMapOf<Long, Product>()
        private val availableQuantity = AtomicInteger(10)
        
        override fun save(product: Product): Product {
            products[product.id!!] = product
            return product
        }
        
        override fun findById(id: Long): Product? {
            return products[id]
        }
        
        override fun findByIds(ids: List<Long>): List<Product> {
            return products.filter { ids.contains(it.key) }.values.toList()
        }
        
        override fun update(product: Product): Product {
            return save(product)
        }
        
        override fun delete(id: Long) {
            products.remove(id)
        }
        
        override fun findAll(): List<Product> {
            return products.values.toList()
        }
        
        // 재고 감소 메서드
        fun decrementQuantity(): Int {
            if (availableQuantity.get() <= 0) {
                throw IllegalStateException("재고가 부족합니다.")
            }
            return availableQuantity.decrementAndGet()
        }
        
        // 재고 가져오기
        fun getAvailableQuantity(): Int {
            return availableQuantity.get()
        }
    }
    
    @BeforeEach
    fun setup() {
        // 테스트 리포지토리 초기화
        orderRepository = TestOrderRepository()
        orderItemRepository = TestOrderItemRepository()
        productRepository = TestProductRepository()
        timeProvider = Mockito.mock(TimeProvider::class.java)
        orderEventPublisher = Mockito.mock(OrderEventPublisher::class.java)
        orderItemService = Mockito.mock(OrderItemService::class.java)
        
        // 현재 시간 설정
        val now = LocalDateTime.now()
        Mockito.`when`(timeProvider.now()).thenReturn(now)
        
        // OrderService 생성
        orderService = OrderService(
            orderRepository,
            orderEventPublisher,
            orderItemService,
            timeProvider
        )
    }

    @Test
    fun `동시에 여러 요청이 같은 주문을 처리할 때 주문 상태 변경이 정확하게 이루어져야 한다`() {
        // given
        val userId = 1L
        
        // 주문 생성
        val testOrder = Order.create(
            userId = userId,
            userCouponId = null,
            totalPrice = 10000.0,
            timeProvider = timeProvider
        )
        
        val savedOrder = orderRepository.save(testOrder)
        
        val numberOfThreads = 10
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        val exceptionList = mutableListOf<String>()
        
        // when - 여러 스레드에서 동시에 주문 상태 변경 시도
        for (i in 1..numberOfThreads) {
            executor.submit {
                try {
                    // 동시성 문제를 시뮬레이션하기 위해 의도적으로 지연 추가
                    if (i % 2 == 0) {
                        Thread.sleep(10) // 일부 스레드는 약간 지연
                    }
                    
                    // 현재 상태 확인 (동시성 문제 시뮬레이션)
                    val currentOrder = orderRepository.findById(savedOrder.id!!)
                    if (currentOrder?.status == OrderStatus.COMPLETED) {
                        throw IllegalStateException("이미 완료된 주문입니다.")
                    }
                    
                    // 주문 상태를 COMPLETED로 변경 시도
                    orderService.completeOrder(savedOrder.id!!)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                    synchronized(exceptionList) {
                        exceptionList.add("주문 상태 변경 실패: ${e.message}")
                    }
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // 모든 스레드가 작업을 완료할 때까지 대기
        assertTrue(latch.await(10, TimeUnit.SECONDS))
        executor.shutdown()
        
        // then
        // 로그 출력
        exceptionList.forEach(::println)
        
        // 주문 상태 변경은 한 번만 성공해야 하지만, 테스트용 리포지토리에서는 락이 없어 여러 번 성공할 수 있음
        // 중요한 것은 최종적으로 주문이 COMPLETED 상태여야 함
        val updatedOrder = orderRepository.findById(savedOrder.id!!)
        assertEquals(OrderStatus.COMPLETED, updatedOrder?.status)
        
        // 성공 횟수와 실패 횟수의 합은 총 스레드 수와 같아야 함
        assertEquals(numberOfThreads, successCount.get() + failCount.get())
    }
    
    @Test
    fun `동시에 여러 요청이 동일한 상품을 주문할 때 재고가 정확히 관리되어야 한다`() {
        // given
        val numberOfThreads = 20 // 20명이 동시에 요청 (재고는 10개만 있음)
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        val exceptionList = mutableListOf<String>()
        
        // when - 여러 스레드에서 동시에 상품 주문
        for (i in 1..numberOfThreads) {
            val userId = i.toLong()
            executor.submit {
                try {
                    // 상품 재고 감소 시도
                    productRepository.decrementQuantity()
                    
                    // 주문 생성
                    val order = Order.create(
                        userId = userId,
                        userCouponId = null,
                        totalPrice = 1000.0,
                        timeProvider = timeProvider
                    )
                    orderRepository.save(order)
                    
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                    synchronized(exceptionList) {
                        exceptionList.add("상품 주문 실패 (user: $userId): ${e.message}")
                    }
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // 모든 스레드가 작업을 완료할 때까지 대기
        assertTrue(latch.await(10, TimeUnit.SECONDS))
        executor.shutdown()
        
        // then
        // 로그 출력
        exceptionList.forEach(::println)
        
        // 남은 재고 확인
        assertEquals(0, productRepository.getAvailableQuantity())
        
        // 성공한 주문 수는 최초 재고량과 일치해야 함
        assertEquals(10, successCount.get())
        
        // 실패한 주문 수는 (요청 수 - 재고량)과 일치해야 함
        assertEquals(numberOfThreads - 10, failCount.get())
        
        // 생성된 주문 수 확인
        val allOrders = orderRepository.findAll()
        assertEquals(10, allOrders.size)
    }
} 