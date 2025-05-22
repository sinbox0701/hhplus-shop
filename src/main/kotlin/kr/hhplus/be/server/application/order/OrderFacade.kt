package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.domain.order.service.OrderCommand
import kr.hhplus.be.server.domain.order.service.OrderItemCommand
import kr.hhplus.be.server.domain.order.service.OrderItemService
import kr.hhplus.be.server.domain.order.service.OrderService
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.service.ProductOptionCommand
import kr.hhplus.be.server.domain.product.service.ProductOptionService
import kr.hhplus.be.server.domain.product.service.ProductService
import kr.hhplus.be.server.domain.ranking.service.ProductRankingService
import kr.hhplus.be.server.domain.user.service.AccountCommand
import kr.hhplus.be.server.domain.user.service.AccountService
import kr.hhplus.be.server.domain.user.service.UserService
import kr.hhplus.be.server.domain.coupon.service.CouponService
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.shared.lock.DistributedLock
import kr.hhplus.be.server.shared.lock.LockKeyConstants
import kr.hhplus.be.server.shared.transaction.TransactionHelper
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 주문 Facade
 * - 주문 도메인 서비스와 연결하는 역할
 * - 이벤트 기반 아키텍처로 각 서비스 간 의존성 제거
 */
@Service
class OrderFacade(
    private val orderService: OrderService,
    private val orderItemService: OrderItemService,
    private val productService: ProductService,
    private val productOptionService: ProductOptionService,
    private val userService: UserService,
    private val couponService: CouponService,
    private val transactionHelper: TransactionHelper
) {
    /**
     * 상품 정보 캐시로 조회
     */
    @Cacheable(value = ["orderProducts"], key = "'product_' + #productId")
    fun getProductWithCache(productId: Long): kr.hhplus.be.server.domain.product.model.Product {
        return productService.get(productId)
    }
    
    /**
     * 상품 옵션 캐시로 조회
     */
    @Cacheable(value = ["orderProducts"], key = "'option_' + #optionId")
    fun getProductOptionWithCache(optionId: Long): kr.hhplus.be.server.domain.product.model.ProductOption {
        return productOptionService.get(optionId)
    }
    
    /**
     * 주문 생성 (장바구니 아이템을 주문으로 변환)
     * - 주문 도메인 로직만 처리하고 나머지는 이벤트로 처리
     */
    @DistributedLock(
        domain = LockKeyConstants.ORDER_PREFIX,
        resourceType = LockKeyConstants.RESOURCE_USER,
        resourceIdExpression = "criteria.userId",
        timeout = LockKeyConstants.EXTENDED_TIMEOUT
    )
    fun createOrder(criteria: OrderCriteria.OrderCreateCriteria): OrderResult.OrderWithItems {
        return transactionHelper.executeInTransaction {
            // 1. 사용자 확인
            val user = userService.findById(criteria.userId)
            
            // 2. 쿠폰 유효성 검증
            val couponInfo = validateAndGetCoupon(criteria.userCouponId, criteria.userId)

            // 3. 상품 및 옵션 검증 및 가격 계산
            val (orderItemCommands, totalPrice) = validateProductsAndCalculatePrice(criteria.orderItems)
            
            // 4. 쿠폰 할인 적용
            val finalPrice = applyDiscount(totalPrice, couponInfo?.second)
            
            // 5. 주문 생성
            val order = orderService.createOrder(OrderCommand.CreateOrderCommand(
                userId = user.id!!,
                userCouponId = couponInfo?.first?.id,
                totalPrice = finalPrice
            ))
            
            // 6. 주문 상품 생성
            val orderItems = createOrderItems(orderItemCommands, order.id!!)
            
            // 7. 주문 이벤트 발행 (이벤트를 통해 재고 감소 등 부가 작업 처리)
            val completedOrder = orderService.createOrderAndPublishEvent(
                OrderCommand.CreateOrderCommand(
                    userId = order.userId,
                    userCouponId = order.userCouponId,
                    totalPrice = finalPrice
                ),
                orderItems
            )
            
            OrderResult.OrderWithItems(completedOrder, orderItems)
        }
    }
    
    /**
     * 주문 결제 처리
     * - 결제 완료 후 이벤트를 발행하여 쿠폰 적용, 계좌 차감 등 부가 작업 처리
     */
    @DistributedLock(
        domain = LockKeyConstants.ORDER_PREFIX,
        resourceType = LockKeyConstants.RESOURCE_ID,
        resourceIdExpression = "criteria.orderId",
        timeout = LockKeyConstants.EXTENDED_TIMEOUT
    )
    fun processPayment(criteria: OrderCriteria.OrderPaymentCriteria): OrderResult.OrderWithItems {
        return transactionHelper.executeInTransaction {
            // 1. 주문 조회
            val order = orderService.getOrder(criteria.orderId)
            
            // 2. 사용자 확인
            val user = userService.findById(criteria.userId)
            if (order.userId != user.id) {
                throw IllegalArgumentException("해당 주문의 소유자가 아닙니다")
            }
            
            // 3. 주문 상태 확인
            if (order.status != OrderStatus.PENDING) {
                throw IllegalStateException("결제할 수 없는 주문 상태입니다: ${order.status}")
            }
            
            try {
                // 4. 주문 완료 처리 (이벤트를 통해 계좌 차감, 쿠폰 사용 등 처리)
                val completedOrder = orderService.completeOrder(order.id!!)
                
                // 5. 주문 아이템 조회
                val orderItems = orderItemService.getByOrderId(order.id!!)
                
                OrderResult.OrderWithItems(completedOrder, orderItems)
            } catch (e: Exception) {
                throw e
            }
        }
    }
    
    /**
     * 주문 취소
     * - 취소 후 이벤트를 발행하여 재고 복구, 계좌 환불 등 처리
     */
    @DistributedLock(
        domain = LockKeyConstants.ORDER_PREFIX,
        resourceType = LockKeyConstants.RESOURCE_ID,
        resourceIdExpression = "orderId",
        timeout = LockKeyConstants.EXTENDED_TIMEOUT
    )
    fun cancelOrder(orderId: Long, userId: Long): OrderResult.OrderWithItems {
        return transactionHelper.executeInTransaction {
            // 1. 주문 조회
            val order = orderService.getOrder(orderId)
            
            // 2. 주문 소유자 확인
            val user = userService.findById(userId)
            if (order.userId != user.id) {
                throw IllegalArgumentException("해당 주문의 소유자가 아닙니다")
            }
            
            // 3. 주문 취소 처리 (이벤트를 통해 재고 복구, 환불 등 처리)
            val cancelledOrder = orderService.cancelOrder(order.id!!)
            
            // 4. 주문 아이템 조회
            val orderItems = orderItemService.getByOrderId(order.id!!)
            
            OrderResult.OrderWithItems(cancelledOrder, orderItems)
        }
    }
    
    /**
     * 주문 정보와 주문 상품 정보 조회
     */
    @Transactional(readOnly = true)
    fun getOrderWithItems(orderId: Long, userId: Long): OrderResult.OrderWithItems {
        // 1. 주문 조회
        val order = orderService.getOrder(orderId)
        
        // 2. 주문 소유자 확인
        val user = userService.findById(userId)
        if (order.userId != user.id) {
            throw IllegalArgumentException("해당 주문의 소유자가 아닙니다")
        }
        
        // 3. 주문 상품 조회
        val orderItems = orderItemService.getByOrderId(order.id!!)
        
        return OrderResult.OrderWithItems(order, orderItems)
    }
    
    /**
     * 사용자의 모든 주문 목록 조회
     */
    @Transactional(readOnly = true)
    fun getAllOrdersByUserId(userId: Long): List<OrderResult.OrderWithItems> {
        // 1. 사용자 확인
        userService.findById(userId)
        
        // 2. 사용자의 모든 주문 조회
        val orders = orderService.getOrdersByUserId(userId)
        
        // 3. 각 주문의 상품 정보 함께 조회
        return orders.map { order ->
            val orderItems = orderItemService.getByOrderId(order.id!!)
            OrderResult.OrderWithItems(order, orderItems)
        }
    }
    
    /**
     * 특정 상태의 주문 목록 조회
     */
    @Transactional(readOnly = true)
    fun getOrdersByUserIdAndStatus(userId: Long, status: OrderStatus): List<OrderResult.OrderWithItems> {
        // 1. 사용자 확인
        userService.findById(userId)
        
        // 2. 특정 상태의 주문 조회
        val orders = orderService.getOrdersByUserIdAndStatus(userId, status)
        
        // 3. 각 주문의 상품 정보 함께 조회
        return orders.map { order ->
            val orderItems = orderItemService.getByOrderId(order.id!!)
            OrderResult.OrderWithItems(order, orderItems)
        }
    }
    
    // 내부 헬퍼 메소드

    /**
     * 쿠폰 유효성 검증 및 조회
     */
    private fun validateAndGetCoupon(userCouponId: Long?, userId: Long): Pair<kr.hhplus.be.server.domain.coupon.model.UserCoupon, kr.hhplus.be.server.domain.coupon.model.Coupon>? {
        return userCouponId?.let { couponId ->
            val userCoupon = couponService.findUserCouponById(couponId)
            val coupon = couponService.findById(userCoupon.couponId)
            
            // 쿠폰 소유자 확인
            if (userCoupon.userId != userId) {
                throw IllegalArgumentException("해당 쿠폰의 소유자가 아닙니다")
            }
            
            // 쿠폰 유효기간 검증
            val now = LocalDateTime.now()
            if (now.isBefore(coupon.startDate) || now.isAfter(coupon.endDate)) {
                throw IllegalStateException("쿠폰 유효기간이 아닙니다")
            }
            
            // 쿠폰 사용 가능 여부 검증
            if (userCoupon.isIssued() && userCoupon.isUsed()) {
                throw IllegalStateException("이미 사용된 쿠폰입니다")
            }
            
            Pair(userCoupon, coupon)
        }
    }
    
    /**
     * 상품 및 옵션 검증 및 가격 계산
     */
    private fun validateProductsAndCalculatePrice(orderItems: List<OrderCriteria.OrderItemRequest>): Pair<List<Pair<OrderItemCommand.CreateOrderItemCommand, Pair<kr.hhplus.be.server.domain.product.model.Product, kr.hhplus.be.server.domain.product.model.ProductOption>>>, Double> {
        var totalPrice = 0.0
        val orderItemCommands = orderItems.map { item -> 
            // 상품 및 옵션 정보 조회 (캐시된 값 사용)
            val product = getProductWithCache(item.productId)
            val productOption = getProductOptionWithCache(item.productOptionId)
            
            // 재고 확인 - 재고는 항상 최신 정보를 사용
            if (productOption.availableQuantity < item.quantity) {
                throw IllegalStateException("상품 옵션의 재고가 부족합니다: ${productOption.name}")
            }
            
            // 상품 가격 계산
            val basePrice = product.price + productOption.additionalPrice
            val itemPrice = basePrice * item.quantity
            
            totalPrice += itemPrice
            
            // 주문 상품 생성 명령 준비
            Pair(OrderItemCommand.CreateOrderItemCommand(
                orderId = 0, // 임시 값, 실제 orderId는 주문 생성 후 설정
                productId = product.id!!,
                productOptionId = productOption.id!!,
                userCouponId = null,
                quantity = item.quantity,
                discountRate = null
            ), Pair(product, productOption))
        }
        
        return Pair(orderItemCommands, totalPrice)
    }
    
    /**
     * 쿠폰 할인 적용
     */
    private fun applyDiscount(totalPrice: Double, coupon: kr.hhplus.be.server.domain.coupon.model.Coupon?): Double {
        return if (coupon != null) {
            // 쿠폰 종류에 따라 할인 적용 방식 다르게 처리
            when (coupon.couponType) {
                kr.hhplus.be.server.domain.coupon.model.CouponType.DISCOUNT_ORDER -> totalPrice * (1 - coupon.discountRate / 100) // 주문 전체 할인
                kr.hhplus.be.server.domain.coupon.model.CouponType.DISCOUNT_PRODUCT -> totalPrice * (1 - coupon.discountRate / 100) // 상품 할인도 동일하게 적용
            }
        } else {
            totalPrice
        }
    }
    
    /**
     * 주문 상품 생성
     */
    private fun createOrderItems(orderItemCommands: List<Pair<OrderItemCommand.CreateOrderItemCommand, Pair<kr.hhplus.be.server.domain.product.model.Product, kr.hhplus.be.server.domain.product.model.ProductOption>>>, orderId: Long): List<OrderItem> {
        return orderItemCommands.map { (itemCommand, _) -> 
            // 주문 상품 생성
            orderItemService.create(itemCommand.copy(orderId = orderId))
        }
    }
}