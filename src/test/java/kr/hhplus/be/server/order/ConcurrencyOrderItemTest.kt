package kr.hhplus.be.server.order

import kr.hhplus.be.server.domain.common.TimeProvider
import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.repository.ProductOptionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class ConcurrencyOrderItemTest {

    private lateinit var orderItemRepository: TestOrderItemRepository
    private lateinit var productOptionRepository: TestProductOptionRepository
    private lateinit var timeProvider: TimeProvider

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
    }
    
    // 테스트용 ProductOptionRepository 구현
    class TestProductOptionRepository {
        private val productOptions = mutableMapOf<Long, ProductOption>()
        private val quantityMap = java.util.concurrent.ConcurrentHashMap<Long, AtomicInteger>()
        
        fun save(productOption: ProductOption): ProductOption {
            productOptions[productOption.id!!] = productOption
            // computeIfAbsent 대신 putIfAbsent로 변경
            val existingCounter = quantityMap.putIfAbsent(productOption.id!!, AtomicInteger(productOption.availableQuantity))
            if (existingCounter == null) {
                // 새로 생성된 경우, 할 일 없음
            } else {
                // 이미 존재하는 경우, 값 설정
                existingCounter.set(productOption.availableQuantity)
            }
            return productOption
        }
        
        fun findById(id: Long): ProductOption? {
            return productOptions[id]
        }
        
        fun decrementQuantity(optionId: Long): Int {
            // synchronized 블록으로 동시 접근 제어
            synchronized(this) {
                val counter = quantityMap[optionId] ?: throw IllegalArgumentException("상품 옵션을 찾을 수 없습니다: $optionId")
                if (counter.get() <= 0) {
                    throw IllegalStateException("재고가 부족합니다.")
                }
                return counter.decrementAndGet()
            }
        }
        
        fun getAvailableQuantity(optionId: Long): Int {
            return quantityMap[optionId]?.get() ?: 0
        }
    }
    
    @BeforeEach
    fun setup() {
        // 테스트 리포지토리 초기화
        orderItemRepository = TestOrderItemRepository()
        productOptionRepository = TestProductOptionRepository()
        timeProvider = Mockito.mock(TimeProvider::class.java)
        
        // 현재 시간 설정
        val now = LocalDateTime.now()
        Mockito.`when`(timeProvider.now()).thenReturn(now)
        Mockito.`when`(timeProvider.today()).thenReturn(LocalDate.now())
    }
    
    @Test
    fun `동시에 여러 요청이 동일한 상품 옵션을 주문할 때 재고가 정확히 관리되어야 한다`() {
        // given
        val productId = 1L
        val productOptionId = 1L
        val initialQuantity = 10
        
        // 가상의 테스트용 ProductOption 생성 (ID가 이미 설정되어 있다고 가정)
        val productOption = Mockito.mock(ProductOption::class.java)
        Mockito.`when`(productOption.id).thenReturn(productOptionId)
        Mockito.`when`(productOption.availableQuantity).thenReturn(initialQuantity)
        
        // ProductOptionRepository에 저장
        productOptionRepository.save(productOption)
        
        val numberOfThreads = 20 // 20명이 동시에 요청 (재고는 10개만 있음)
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        val exceptionList = mutableListOf<String>()
        
        // when - 여러 스레드에서 동시에 상품 주문
        for (i in 1..numberOfThreads) {
            val orderId = i.toLong()
            executor.submit {
                try {
                    // 동시성 문제를 시뮬레이션하기 위해 의도적으로 지연 추가
                    if (i % 2 == 0) {
                        Thread.sleep(5) // 일부 스레드는 약간 지연
                    }
                    
                    // 상품 재고 감소 시도
                    productOptionRepository.decrementQuantity(productOptionId)
                    
                    // OrderItem 생성
                    val orderItem = OrderItem.create(
                        orderId = orderId,
                        productId = productId,
                        productOptionId = productOptionId,
                        userCouponId = null,
                        quantity = 1,
                        price = 1000.0
                    )
                    orderItemRepository.save(orderItem)
                    
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                    synchronized(exceptionList) {
                        exceptionList.add("상품 주문 실패 (order: $orderId): ${e.message}")
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
        
        // 테스트 결과 검증
        // 1. 주문 항목 생성 수를 검증
        val allOrderItems = orderItemRepository.findByProductId(productId)
        println("생성된 주문 항목 수: ${allOrderItems.size}")
        println("성공 카운트: ${successCount.get()}")
        println("실패 카운트: ${failCount.get()}")
        println("초기 재고량: $initialQuantity")
        
        // 2. 주문 성공 횟수와 실패 횟수의 합이 요청 횟수와 같아야 함
        assertEquals(numberOfThreads, successCount.get() + failCount.get())
        
        // 3. 주문 항목 수와 성공 횟수가 같아야 함 - 이 부분은 allOrderItems.size가 성공 횟수와 같아야 함을 검증
        assertEquals(successCount.get(), allOrderItems.size)
        
        // 4. 초기 재고와 성공 횟수가 같아야 함 - 재고가 0이 될 때까지만 성공해야 함
        assertEquals(initialQuantity, successCount.get())
        
        // 5. 남은 재고는 0이어야 함
        assertEquals(0, productOptionRepository.getAvailableQuantity(productOptionId))
    }
} 