package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.domain.order.service.OrderCommand
import kr.hhplus.be.server.domain.order.service.OrderItemCommand
import kr.hhplus.be.server.domain.order.service.OrderItemService
import kr.hhplus.be.server.domain.order.service.OrderService
import kr.hhplus.be.server.domain.product.service.ProductOptionCommand
import kr.hhplus.be.server.domain.product.service.ProductOptionService
import kr.hhplus.be.server.domain.product.service.ProductService
import kr.hhplus.be.server.domain.user.service.AccountCommand
import kr.hhplus.be.server.domain.user.service.AccountService
import kr.hhplus.be.server.domain.user.service.UserService
import kr.hhplus.be.server.domain.coupon.service.CouponService
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.shared.lock.DistributedLock
import kr.hhplus.be.server.shared.lock.LockKeyConstants
import kr.hhplus.be.server.shared.transaction.TransactionHelper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class OrderFacade(
    private val orderService: OrderService,
    private val orderItemService: OrderItemService,
    private val productService: ProductService,
    private val productOptionService: ProductOptionService,
    private val userService: UserService,
    private val couponService: CouponService,
    private val accountService: AccountService,
    private val transactionHelper: TransactionHelper
) {
    /**
     * 주문 생성 (장바구니 아이템을 주문으로 변환)
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
            val couponInfo = criteria.userCouponId?.let { couponId ->
                val userCoupon = couponService.findUserCouponById(couponId)
                val coupon = couponService.findById(userCoupon.couponId)
                
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

            // 3. 상품 및 옵션 검증 및 가격 계산
            var totalPrice = 0.0
            val orderItemCommands = criteria.orderItems.map { item -> 
                // 상품 및 옵션 정보 조회
                val product = productService.get(item.productId)
                val productOption = productOptionService.get(item.productOptionId)
                
                // 재고 확인
                if (productOption.availableQuantity < item.quantity) {
                    throw IllegalStateException("상품 옵션의 재고가 부족합니다: ${productOption.name}")
                }
                
                // 상품 가격 계산
                val basePrice = product.price + productOption.additionalPrice
                val itemPrice = basePrice * item.quantity
                
                totalPrice += itemPrice
                
                // 주문 상품 생성 명령 준비
                OrderItemCommand.CreateOrderItemCommand(
                    orderId = 0, // 임시 값, 실제 orderId는 주문 생성 후 설정
                    productId = product.id!!,
                    productOptionId = productOption.id!!,
                    userCouponId = couponInfo?.first?.id,
                    quantity = item.quantity,
                    discountRate = null
                ) to Pair(product, productOption)
            }
            
            // 4. 쿠폰 할인 적용
            val finalPrice = if (couponInfo != null) {
                val (userCoupon, coupon) = couponInfo
                // 쿠폰 종류에 따라 할인 적용 방식 다르게 처리
                when (coupon.couponType) {
                    CouponType.DISCOUNT_ORDER -> totalPrice * (1 - coupon.discountRate / 100) // 주문 전체 할인
                    CouponType.DISCOUNT_PRODUCT -> totalPrice * (1 - coupon.discountRate / 100) // 상품 할인도 동일하게 적용
                }
            } else {
                totalPrice
            }
            
            // 5. 주문 생성 (재고 확인 후 실행)
            val order = orderService.createOrder(OrderCommand.CreateOrderCommand(
                userId = user.id!!,
                userCouponId = couponInfo?.first?.id,
                totalPrice = 0.0 // 임시 가격 (나중에 업데이트)
            ))
            
            // 6. 주문 상품 생성 및 재고 감소
            val orderItems = orderItemCommands.map { (itemCommand, productPair) -> 
                val (product, productOption) = productPair
                
                // 주문 상품 생성
                val orderItem = orderItemService.create(itemCommand.copy(orderId = order.id!!))
                
                // 재고 감소
                productOptionService.subtractQuantity(ProductOptionCommand.UpdateQuantityCommand(
                    id = productOption.id!!,
                    quantity = itemCommand.quantity
                ))
                
                orderItem
            }
            
            // 7. 주문 총 가격 업데이트
            val updatedOrder = orderService.updateOrderTotalPrice(OrderCommand.UpdateOrderTotalPriceCommand(
                id = order.id!!,
                totalPrice = finalPrice
            ))
            
            OrderResult.OrderWithItems(updatedOrder, orderItems)
        }
    }
    
    /**
     * 주문 결제 처리
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
            
            // 2. 사용자 및 계좌 확인
            val user = userService.findById(criteria.userId)
            if (order.userId != user.id) {
                throw IllegalArgumentException("해당 주문의 소유자가 아닙니다")
            }
            
            val account = accountService.findByUserId(user.id!!)
            
            // 3. 잔액 확인
            if (account.amount < order.totalPrice) {
                throw IllegalStateException("계좌 잔액이 부족합니다")
            }
            
            try {
                // 4. 주문 상태 확인
                if (order.status != OrderStatus.PENDING) {
                    throw IllegalStateException("결제할 수 없는 주문 상태입니다: ${order.status}")
                }
                
                // 5. 계좌에서 금액 차감
                accountService.withdraw(AccountCommand.UpdateAccountCommand(
                    id = account.id!!,
                    amount = order.totalPrice
                ))
                
                // 6. 쿠폰 사용 처리
                order.userCouponId?.let {
                    couponService.useUserCoupon(it)
                }
                
                // 7. 주문 상태 완료로 변경
                val completedOrder = orderService.completeOrder(order.id!!)
                
                // 8. 주문 아이템 조회
                val orderItems = orderItemService.getByOrderId(order.id!!)
                
                OrderResult.OrderWithItems(completedOrder, orderItems)
                
            } catch (e: Exception) {
                // 오류 발생 시 롤백은 transactionHelper에 의해 자동으로 처리됨
                throw e
            }
        }
    }
    
    /**
     * 주문 취소
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
            
            // 3. 주문 취소 가능 여부 확인
            if (!order.isCancellable()) {
                throw IllegalStateException("취소할 수 없는 주문 상태입니다: ${order.status}")
            }
            
            // 4. 주문 아이템 조회
            val orderItems = orderItemService.getByOrderId(order.id!!)
            
            // 5. 재고 복구
            orderItems.forEach { orderItem ->
                productOptionService.updateQuantity(ProductOptionCommand.UpdateQuantityCommand(
                    id = orderItem.productOptionId,
                    quantity = orderItem.quantity
                ))
            }
            
            // 6. 주문이 이미 결제 완료 상태라면 환불 처리
            if (order.status == OrderStatus.COMPLETED) {
                // 계좌 조회
                val account = accountService.findByUserId(userId)
                
                // 환불 처리 (계좌에 금액 환불)
                accountService.charge(AccountCommand.UpdateAccountCommand(
                    id = account.id!!,
                    amount = order.totalPrice
                ))
                
                // 쿠폰 사용 취소 처리
                order.userCouponId?.let { couponId ->
                    // 실제로는 쿠폰 사용 취소 메소드 호출 필요
                    // couponService.cancelUsedCoupon(couponId)
                }
            }
            
            // 7. 주문 상태 취소로 변경
            val canceledOrder = orderService.cancelOrder(order.id!!)
            
            OrderResult.OrderWithItems(canceledOrder, orderItems)
        }
    }
    
    /**
     * 부분 주문 취소 (일부 상품만 취소)
     */
    @DistributedLock(
        domain = LockKeyConstants.ORDER_PREFIX,
        resourceType = LockKeyConstants.RESOURCE_ID,
        resourceIdExpression = "orderId",
        timeout = LockKeyConstants.EXTENDED_TIMEOUT
    )
    fun cancelOrderItem(orderId: Long, orderItemId: Long, userId: Long): OrderResult.OrderWithItems {
        return transactionHelper.executeInTransaction {
            // 1. 주문 및 주문 아이템 조회
            val order = orderService.getOrder(orderId)
            val orderItem = orderItemService.getById(orderItemId)
            
            // 2. 주문 소유자 확인
            val user = userService.findById(userId)
            if (order.userId != user.id) {
                throw IllegalArgumentException("해당 주문의 소유자가 아닙니다")
            }
            
            // 3. 주문 아이템이 해당 주문에 포함되는지 확인
            if (orderItem.orderId != order.id) {
                throw IllegalArgumentException("해당 주문에 포함된 상품이 아닙니다")
            }
            
            // 4. 주문 취소 가능 여부 확인
            if (!order.isCancellable()) {
                throw IllegalStateException("취소할 수 없는 주문 상태입니다: ${order.status}")
            }
            
            // 5. 재고 복구
            productOptionService.updateQuantity(ProductOptionCommand.UpdateQuantityCommand(
                id = orderItem.productOptionId,
                quantity = orderItem.quantity
            ))
            
            // 6. 주문 상품 삭제
            orderItemService.deleteById(orderItem.id!!)
            
            // 7. 주문의 총 가격 재계산
            val remainingItems = orderItemService.getByOrderId(order.id!!)
            val newTotalPrice = orderItemService.calculateTotalPrice(remainingItems)
            
            // 8. 주문이 이미 결제 완료 상태라면 부분 환불 처리
            if (order.status == OrderStatus.COMPLETED) {
                // 계좌 조회
                val account = accountService.findByUserId(userId)
                
                // 환불 처리 (계좌에 해당 상품 금액만 환불)
                accountService.charge(AccountCommand.UpdateAccountCommand(
                    id = account.id!!,
                    amount = orderItem.price
                ))
            }
            
            // 9. 주문 총 가격 업데이트
            val updatedOrder = orderService.updateOrderTotalPrice(OrderCommand.UpdateOrderTotalPriceCommand(
                id = order.id!!,
                totalPrice = newTotalPrice
            ))
            
            // 10. 모든 상품이 취소되었는지 확인
            if (remainingItems.isEmpty()) {
                // 주문 자체를 취소 상태로 변경
                orderService.cancelOrder(order.id!!)
                return@executeInTransaction OrderResult.OrderWithItems(orderService.getOrder(order.id!!), emptyList())
            }
            
            OrderResult.OrderWithItems(updatedOrder, remainingItems)
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
}