package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.domain.order.service.OrderItemService
import kr.hhplus.be.server.domain.order.service.OrderService
import kr.hhplus.be.server.domain.product.service.ProductService
import kr.hhplus.be.server.domain.product.service.ProductOptionService
import kr.hhplus.be.server.domain.user.service.AccountService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderFacade(
    private val orderService: OrderService,
    private val orderItemService: OrderItemService,
    private val productService: ProductService,
    private val productOptionService: ProductOptionService,
    private val accountService: AccountService
) {
    /**
     * 주문 생성 (장바구니 아이템을 주문으로 변환)
     */
    @Transactional
    fun createOrder(criteria: OrderCriteria.OrderCreateCriteria): OrderResult.OrderWithItems {
        // 1. 주문 생성
        val account = accountService.findById(criteria.accountId)
        val order = orderService.createOrder(criteria.toOrderCommand(account))
        
        // 2. 주문 상품 생성 및 총 가격 계산
        var totalPrice = 0.0
        val orderItems = criteria.orderItems.map { item -> 
            // 상품 및 옵션 정보 조회
            val product = productService.get(item.productId)
            val productOption = productOptionService.get(item.productOptionId)
            
            // 주문 상품 생성
            val orderItemCommand = item.toOrderItemCommand(order, product, productOption)
            val orderItem = orderItemService.create(orderItemCommand)
            
            totalPrice += orderItem.price
            orderItem
        }
        
        // 3. 쿠폰 할인 적용 (전체 주문에 대한 할인)
        val finalPrice = criteria.accountCouponId?.let { totalPrice * (1 - it / 100) } ?: totalPrice
        
        // 4. 주문 총 가격 업데이트
        orderService.updateOrderTotalPrice(criteria.toOrderTotalPriceUpdateCommand(order.id!!, finalPrice))
        
        return OrderResult.OrderWithItems(order, orderItems)
    }
    
    /**
     * 주문 결제 처리
     */
    @Transactional
    fun processPayment(criteria: OrderCriteria.OrderPaymentCriteria): Order {
        // 1. 주문 조회
        val order = orderService.getOrder(criteria.orderId)
        
        // 2. 계정 확인
        val account = accountService.findById(criteria.accountId)
        if (order.account.id != criteria.accountId) {
            throw IllegalArgumentException("해당 주문의 소유자가 아닙니다")
        }
        
        // 3. 결제 모듈 호출 (외부 결제 API 호출을 대신하여 계좌 잔액 차감으로 대체)
        val paymentResult = processExternalPayment(order)
        
        if (paymentResult) {
            try {
                // 4. 계좌에서 금액 차감
                accountService.withdraw(criteria.toOrderPaymentCommand(order.totalPrice))
                
                // 5. 주문 상태 완료로 변경
                return orderService.completeOrder(order.id!!)
            } catch (e: Exception) {
                // 결제는 성공했으나 내부 처리 실패 시 환불 처리 (실제로는 더 복잡한 로직이 필요)
                reversePayment(order)
                throw e
            }
        } else {
            throw IllegalStateException("결제 처리에 실패했습니다")
        }
    }
    
    /**
     * 외부 결제 모듈 호출 (가상의 메서드)
     */
    private fun processExternalPayment(order: Order): Boolean {
        // 실제로는 외부 결제 API를 호출
        // 여기서는 항상 성공한다고 가정
        return true
    }
    
    /**
     * 결제 취소 처리 (가상의 메서드)
     */
    private fun reversePayment(order: Order): Boolean {
        // 실제로는 외부 결제 API를 호출하여 결제 취소
        // 여기서는 항상 성공한다고 가정
        return true
    }
    
    /**
     * 주문 취소
     */
    @Transactional
    fun cancelOrder(criteria: OrderCriteria.OrderPaymentCriteria): Order {
        // 1. 주문 조회
        val order = orderService.getOrder(criteria.orderId)
        
        // 2. 주문 소유자 확인
        val account = accountService.findById(criteria.accountId)
        if (order.account.id != criteria.accountId)  {
            throw IllegalArgumentException("해당 주문의 소유자가 아닙니다")
        }
        
        // 3. 주문 취소 가능 여부 확인
        if (!order.isCancellable()) {
            throw IllegalStateException("취소할 수 없는 주문 상태입니다: ${order.status}")
        }
        
        // 4. 주문이 이미 결제 완료 상태라면 환불 처리
        if (order.status == OrderStatus.COMPLETED) {
            // 환불 처리 (외부 결제 API 호출 대신 계좌에 금액 환불)
            accountService.charge(criteria.toOrderPaymentCommand(order.totalPrice))
        }
        
        // 5. 주문 상태 취소로 변경
        return orderService.cancelOrder(order.id!!)
    }
    
    /**
     * 주문 정보와 주문 상품 정보 조회
     */
    @Transactional(readOnly = true)
    fun getOrderWithItems(criteria: OrderCriteria.OrderPaymentCriteria): OrderResult.OrderWithItems {
        // 1. 주문 조회
        val order = orderService.getOrder(criteria.orderId)
        
        // 2. 주문 소유자 확인
        val account = accountService.findById(criteria.accountId)
        if (order.account.id != criteria.accountId) {
            throw IllegalArgumentException("해당 주문의 소유자가 아닙니다")
        }
        
        // 3. 주문 상품 조회
        val orderItems = orderItemService.getByOrderId(order.id!!)
        
        return OrderResult.OrderWithItems(order, orderItems)
    }
    
    /**
     * 사용자의 주문 목록 조회
     */
    @Transactional(readOnly = true)
    fun getOrdersByAccountId(accountId: Long): List<Order> {
        return orderService.getOrdersByAccountId(accountId)
    }
}