package kr.hhplus.be.server.controller.order

import kr.hhplus.be.server.controller.order.api.OrderApi
import kr.hhplus.be.server.controller.order.dto.request.OrderCreateRequest
import kr.hhplus.be.server.controller.order.dto.request.OrderStatusUpdateRequest
import kr.hhplus.be.server.controller.order.dto.response.OrderResponse
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderItem
import kr.hhplus.be.server.domain.order.OrderStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.validation.annotation.Validated
import jakarta.validation.Valid
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@RestController
@RequestMapping("/api/orders")
@Validated
class OrderController : OrderApi {

    // 임시 데이터 저장소 (실제로는 repository를 사용해야 함)
    private val orders = ConcurrentHashMap<Long, Order>()
    private val orderItems = ConcurrentHashMap<Long, MutableList<OrderItem>>()
    private val nextOrderId = AtomicLong(1)
    private val nextOrderItemId = AtomicLong(1)

    override fun createOrder(@Valid orderCreateRequest: OrderCreateRequest): ResponseEntity<OrderResponse> {
        val orderId = nextOrderId.getAndIncrement()
        
        // 주문 상품 생성
        val items = orderCreateRequest.orderItems.map { item ->
            val orderItemId = nextOrderItemId.getAndIncrement()
            OrderItem.create(
                id = orderItemId,
                orderId = orderId,
                productId = item.productId,
                quantity = item.quantity,
                price = item.price,
                productOptionId = item.productOptionId
            )
        }
        
        // 총 가격 계산
        val totalPrice = items.fold(0.0) { acc, item -> 
            acc + (item.price * item.quantity)
        }
        
        // 주문 생성
        val order = Order.create(
            id = orderId,
            accountId = orderCreateRequest.accountId,
            accountCouponId = orderCreateRequest.accountCouponId,
            totalPrice = totalPrice,
            status = OrderStatus.PENDING
        )
        
        // 저장
        orders[orderId] = order
        orderItems[orderId] = items.toMutableList()
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(OrderResponse.of(order, items))
    }

    override fun getOrdersByAccountId(accountId: Long): ResponseEntity<List<OrderResponse>> {
        val accountOrders = orders.values
            .filter { it.accountId == accountId }
            .map { order ->
                val items = orderItems[order.id] ?: emptyList()
                OrderResponse.of(order, items)
            }
        
        return ResponseEntity.ok(accountOrders)
    }

    override fun getOrder(orderId: Long): ResponseEntity<OrderResponse> {
        val order = orders[orderId] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다.")
        val items = orderItems[orderId] ?: emptyList()
        
        return ResponseEntity.ok(OrderResponse.of(order, items))
    }

    override fun updateOrderStatus(
        orderId: Long,
        @Valid orderStatusUpdateRequest: OrderStatusUpdateRequest
    ): ResponseEntity<OrderResponse> {
        val order = orders[orderId] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다.")
        
        // 주문 상태 업데이트
        order.updateStatus(orderStatusUpdateRequest.status)
        
        val items = orderItems[orderId] ?: emptyList()
        return ResponseEntity.ok(OrderResponse.of(order, items))
    }

    override fun cancelOrder(orderId: Long): ResponseEntity<OrderResponse> {
        val order = orders[orderId] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다.")
        
        // 이미 완료된 주문은 취소할 수 없음
        if (!order.isCancellable()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 완료된 주문은 취소할 수 없습니다.")
        }
        
        // 주문 상태를 취소로 변경
        order.updateStatus(OrderStatus.CANCELLED)
        
        val items = orderItems[orderId] ?: emptyList()
        return ResponseEntity.ok(OrderResponse.of(order, items))
    }
}
