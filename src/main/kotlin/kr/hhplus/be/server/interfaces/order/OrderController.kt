package kr.hhplus.be.server.interfaces.order

import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.application.order.OrderItemRequest
import kr.hhplus.be.server.application.order.OrderWithItems
import kr.hhplus.be.server.interfaces.order.api.OrderApi
import kr.hhplus.be.server.interfaces.order.OrderRequest
import kr.hhplus.be.server.interfaces.order.OrderResponse   
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.validation.annotation.Validated
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/orders")
@Validated
class OrderController(
    private val orderFacade: OrderFacade
) : OrderApi {

    override fun createOrder(@Valid orderCreateRequest: OrderRequest.OrderCreateRequest): ResponseEntity<OrderResponse.Response> {
        try {
            // OrderItemRequest로 변환
            val orderItems = orderCreateRequest.orderItems.map {
                OrderItemRequest(
                    productId = it.productId,
                    productOptionId = it.productOptionId,
                    quantity = it.quantity
                )
            }
            
            // 주문 생성
            val result = orderFacade.createOrder(
                accountId = orderCreateRequest.accountId,
                items = orderItems,
                accountCouponId = orderCreateRequest.accountCouponId,
                couponDiscountRate = orderCreateRequest.couponDiscountRate
            )
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrderResponse.Response.of(result.order, result.items))
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    override fun processPayment(orderId: Long, @Valid orderPaymentRequest: OrderRequest.OrderPaymentRequest): ResponseEntity<OrderResponse.Response> {
        try {
            // 결제 처리
            val order = orderFacade.processPayment(orderId, orderPaymentRequest.accountId)
            
            // 주문 상세 정보 조회
            val result = orderFacade.getOrderWithItems(orderId, orderPaymentRequest.accountId)
            
            return ResponseEntity.ok(OrderResponse.Response.of(result.order, result.items))
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
        } catch (e: IllegalStateException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    override fun getOrdersByAccountId(accountId: Long): ResponseEntity<List<OrderResponse.Response>> {
        try {
            // 사용자의 주문 목록 조회
            val orders = orderFacade.getOrdersByAccountId(accountId)
            
            // 각 주문별로 상세 정보 조회하여 응답 생성
            val response = orders.map { order -> 
                val result = orderFacade.getOrderWithItems(order.id, accountId)
                OrderResponse.Response.of(result.order, result.items)
            }
            
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    override fun getOrder(orderId: Long, accountId: Long): ResponseEntity<OrderResponse.Response> {
        try {
            // 주문 상세 정보 조회
            val result = orderFacade.getOrderWithItems(orderId, accountId)
            
            return ResponseEntity.ok(OrderResponse.Response.of(result.order, result.items))
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    override fun cancelOrder(orderId: Long, accountId: Long): ResponseEntity<OrderResponse.Response> {
        try {
            // 주문 취소
            val order = orderFacade.cancelOrder(orderId, accountId)
            
            // 주문 상세 정보 조회
            val result = orderFacade.getOrderWithItems(orderId, accountId)
            
            return ResponseEntity.ok(OrderResponse.Response.of(result.order, result.items))
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
        } catch (e: IllegalStateException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }
}
