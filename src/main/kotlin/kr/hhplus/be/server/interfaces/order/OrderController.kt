package kr.hhplus.be.server.interfaces.order

import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.application.order.OrderCriteria
import kr.hhplus.be.server.interfaces.order.api.OrderApi
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
            // 요청을 Criteria로 변환
            val criteria = orderCreateRequest.toCriteria()
            
            // 주문 생성
            val result = orderFacade.createOrder(criteria)
            
            // OrderResponse.Response 객체 생성
            val response = OrderResponse.Response(
                id = result.order.id!!,
                userId = result.order.userId!!,
                userCouponId = result.order.userCouponId,
                totalPrice = result.order.totalPrice,
                status = result.order.status,
                orderDate = result.order.orderDate,
                createdAt = result.order.createdAt,
                updatedAt = result.order.updatedAt,
                items = result.items.map { item ->
                    OrderResponse.OrderItemResponse(
                        id = item.id!!,
                        orderId = item.orderId!!,
                        productId = item.productId!!,
                        productOptionId = item.productOptionId!!,
                        userCouponId = item.userCouponId,
                        quantity = item.quantity,
                        price = item.price,
                        createdAt = item.createdAt,
                        updatedAt = item.updatedAt
                    )
                }
            )
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    override fun processPayment(orderId: Long, @Valid orderPaymentRequest: OrderRequest.OrderPaymentRequest): ResponseEntity<OrderResponse.Response> {
        try {
            // 요청을 Criteria로 변환
            val criteria = orderPaymentRequest.toCriteria(orderId)
            
            // 결제 처리
            val result = orderFacade.processPayment(criteria)
            
            // 주문 상세 정보 조회
            val orderWithItems = orderFacade.getOrderWithItems(orderId, criteria.userId)
            
            // OrderResponse.Response 객체 생성
            val response = OrderResponse.Response(
                id = orderWithItems.order.id!!,
                userId = orderWithItems.order.userId!!,
                userCouponId = orderWithItems.order.userCouponId,
                totalPrice = orderWithItems.order.totalPrice,
                status = orderWithItems.order.status,
                orderDate = orderWithItems.order.orderDate,
                createdAt = orderWithItems.order.createdAt,
                updatedAt = orderWithItems.order.updatedAt,
                items = orderWithItems.items.map { item ->
                    OrderResponse.OrderItemResponse(
                        id = item.id!!,
                        orderId = item.orderId!!,
                        productId = item.productId!!,
                        productOptionId = item.productOptionId!!,
                        userCouponId = item.userCouponId,
                        quantity = item.quantity,
                        price = item.price,
                        createdAt = item.createdAt,
                        updatedAt = item.updatedAt
                    )
                }
            )
            
            return ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
        } catch (e: IllegalStateException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    override fun getOrdersByAccountId(userId: Long): ResponseEntity<List<OrderResponse.Response>> {
        try {
            // 사용자의 주문 목록 조회
            val ordersWithItems = orderFacade.getAllOrdersByUserId(userId)
            
            // 각 주문별로 상세 정보 조회하여 응답 생성
            val response = ordersWithItems.map { orderWithItems -> 
                OrderResponse.Response(
                    id = orderWithItems.order.id!!,
                    userId = orderWithItems.order.userId!!,
                    userCouponId = orderWithItems.order.userCouponId,
                    totalPrice = orderWithItems.order.totalPrice,
                    status = orderWithItems.order.status,
                    orderDate = orderWithItems.order.orderDate,
                    createdAt = orderWithItems.order.createdAt,
                    updatedAt = orderWithItems.order.updatedAt,
                    items = orderWithItems.items.map { item ->
                        OrderResponse.OrderItemResponse(
                            id = item.id!!,
                            orderId = item.orderId!!,
                            productId = item.productId!!,
                            productOptionId = item.productOptionId!!,
                            userCouponId = item.userCouponId,
                            quantity = item.quantity,
                            price = item.price,
                            createdAt = item.createdAt,
                            updatedAt = item.updatedAt
                        )
                    }
                )
            }
            
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    override fun getOrder(orderId: Long, accountId: Long): ResponseEntity<OrderResponse.Response> {
        try {
            // 주문 상세 정보 조회
            val orderWithItems = orderFacade.getOrderWithItems(orderId, accountId)
            
            // OrderResponse.Response 객체 생성
            val response = OrderResponse.Response(
                id = orderWithItems.order.id!!,
                userId = orderWithItems.order.userId!!,
                userCouponId = orderWithItems.order.userCouponId,
                totalPrice = orderWithItems.order.totalPrice,
                status = orderWithItems.order.status,
                orderDate = orderWithItems.order.orderDate,
                createdAt = orderWithItems.order.createdAt,
                updatedAt = orderWithItems.order.updatedAt,
                items = orderWithItems.items.map { item ->
                    OrderResponse.OrderItemResponse(
                        id = item.id!!,
                        orderId = item.orderId!!,
                        productId = item.productId!!,
                        productOptionId = item.productOptionId!!,
                        userCouponId = item.userCouponId,
                        quantity = item.quantity,
                        price = item.price,
                        createdAt = item.createdAt,
                        updatedAt = item.updatedAt
                    )
                }
            )
            
            return ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    override fun cancelOrder(orderId: Long, accountId: Long): ResponseEntity<OrderResponse.Response> {
        try {
            // 주문 취소
            val orderWithItems = orderFacade.cancelOrder(orderId, accountId)
            
            // OrderResponse.Response 객체 생성
            val response = OrderResponse.Response(
                id = orderWithItems.order.id!!,
                userId = orderWithItems.order.userId!!,
                userCouponId = orderWithItems.order.userCouponId,
                totalPrice = orderWithItems.order.totalPrice,
                status = orderWithItems.order.status,
                orderDate = orderWithItems.order.orderDate,
                createdAt = orderWithItems.order.createdAt,
                updatedAt = orderWithItems.order.updatedAt,
                items = orderWithItems.items.map { item ->
                    OrderResponse.OrderItemResponse(
                        id = item.id!!,
                        orderId = item.orderId!!,
                        productId = item.productId!!,
                        productOptionId = item.productOptionId!!,
                        userCouponId = item.userCouponId,
                        quantity = item.quantity,
                        price = item.price,
                        createdAt = item.createdAt,
                        updatedAt = item.updatedAt
                    )
                }
            )
            
            return ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
        } catch (e: IllegalStateException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }
}
