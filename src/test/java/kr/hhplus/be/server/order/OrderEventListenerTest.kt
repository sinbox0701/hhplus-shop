package kr.hhplus.be.server.order

import io.mockk.*
import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.domain.order.event.OrderEvent
import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.domain.product.service.ProductOptionCommand
import kr.hhplus.be.server.domain.product.service.ProductOptionService
import kr.hhplus.be.server.interfaces.order.event.CompensationOrderEventListener
import kr.hhplus.be.server.interfaces.order.event.ProductOrderEventListener
import kr.hhplus.be.server.interfaces.order.event.ProductStockEvent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

/**
 * 이벤트 리스너 테스트 클래스
 */
class OrderEventListenerTest {

    private lateinit var productOptionService: ProductOptionService
    private lateinit var orderFacade: OrderFacade
    private lateinit var applicationEventPublisher: ApplicationEventPublisher
    private lateinit var productOrderEventListener: ProductOrderEventListener
    private lateinit var compensationOrderEventListener: CompensationOrderEventListener

    @BeforeEach
    fun setup() {
        productOptionService = mockk(relaxed = true)
        orderFacade = mockk(relaxed = true)
        applicationEventPublisher = mockk(relaxed = true)
        
        // ProductRankingService 목 객체 추가
        val productRankingService = mockk<kr.hhplus.be.server.domain.ranking.service.ProductRankingService>(relaxed = true)
        
        productOrderEventListener = ProductOrderEventListener(
            productOptionService,
            productRankingService,
            applicationEventPublisher
        )
        
        compensationOrderEventListener = CompensationOrderEventListener(
            orderFacade
        )
    }

    @Test
    @DisplayName("주문 생성 이벤트 처리 테스트 - 상품 재고 감소")
    fun `주문 생성 이벤트를 받으면 상품 재고가 정상적으로 감소해야 한다`() {
        // given
        val orderItem1 = mockk<OrderItem>()
        every { orderItem1.productOptionId } returns 1L
        every { orderItem1.quantity } returns 2
        
        val orderItem2 = mockk<OrderItem>()
        every { orderItem2.productOptionId } returns 2L
        every { orderItem2.quantity } returns 3
        
        val event = OrderEvent.Created(
            orderId = 1L,
            userId = 100L,
            userCouponId = null,
            orderItems = listOf(orderItem1, orderItem2),
            totalPrice = 10000.0,
            createdAt = LocalDateTime.now()
        )
        
        // when
        productOrderEventListener.handleOrderCreated(event)
        
        // then
        verify(exactly = 1) {
            productOptionService.subtractQuantity(
                ProductOptionCommand.UpdateQuantityCommand(
                    id = 1L,
                    quantity = 2
                )
            )
        }
        
        verify(exactly = 1) {
            productOptionService.subtractQuantity(
                ProductOptionCommand.UpdateQuantityCommand(
                    id = 2L,
                    quantity = 3
                )
            )
        }
    }
    
    @Test
    @DisplayName("상품 재고 감소 실패 시 보상 트랜잭션 테스트")
    fun `상품 재고 감소 실패 시 주문이 취소되어야 한다`() {
        // given
        val event = ProductStockEvent.DecreaseFailed(
            orderId = 1L,
            userId = 100L,
            reason = "재고 부족"
        )
        
        // when
        compensationOrderEventListener.handleProductStockDecreaseFailed(event)
        
        // then
        verify(exactly = 1) {
            orderFacade.cancelOrder(1L, 100L)
        }
    }
    
    @Test
    @DisplayName("주문 취소 이벤트 처리 테스트 - 상품 재고 복구")
    fun `주문 취소 이벤트를 받으면 상품 재고가 정상적으로 복구되어야 한다`() {
        // given
        val orderItem1 = mockk<OrderItem>()
        every { orderItem1.productOptionId } returns 1L
        every { orderItem1.quantity } returns 2
        
        val event = OrderEvent.Cancelled(
            orderId = 1L,
            userId = 100L,
            userCouponId = null,
            orderItems = listOf(orderItem1),
            totalPrice = 5000.0,
            previousStatus = OrderStatus.PENDING,
            cancelledAt = LocalDateTime.now()
        )
        
        // when
        productOrderEventListener.handleOrderCancelled(event)
        
        // then
        verify(exactly = 1) {
            productOptionService.updateQuantity(
                ProductOptionCommand.UpdateQuantityCommand(
                    id = 1L,
                    quantity = 2
                )
            )
        }
    }
} 