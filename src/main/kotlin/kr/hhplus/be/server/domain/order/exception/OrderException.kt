package kr.hhplus.be.server.domain.order.exception

import kr.hhplus.be.server.domain.common.exception.BusinessException
import kr.hhplus.be.server.domain.common.exception.ErrorCode
import org.springframework.http.HttpStatus

/**
 * 주문 관련 에러 코드
 */
enum class OrderErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String
) : ErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문을 찾을 수 없습니다"),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "O002", "유효하지 않은 주문 상태입니다"),
    ORDER_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "O003", "주문이 이미 완료되었습니다"),
    ORDER_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "O004", "주문이 이미 취소되었습니다"),
    CANNOT_CANCEL_COMPLETED_ORDER(HttpStatus.BAD_REQUEST, "O005", "완료된 주문은 취소할 수 없습니다"),
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "O006", "주문 항목을 찾을 수 없습니다"),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "O007", "상품 재고가 부족합니다"),
    UNAUTHORIZED_ORDER_ACCESS(HttpStatus.FORBIDDEN, "O008", "주문에 접근할 권한이 없습니다"),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "O009", "결제에 실패했습니다"),
}

/**
 * 주문을 찾을 수 없을 때 발생하는 예외
 */
class OrderNotFoundException(
    orderId: Long
) : BusinessException(OrderErrorCode.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다: $orderId")

/**
 * 주문 상태가 유효하지 않을 때 발생하는 예외
 */
class InvalidOrderStatusException(
    message: String
) : BusinessException(OrderErrorCode.INVALID_ORDER_STATUS, message)

/**
 * 주문이 이미 완료되었을 때 발생하는 예외
 */
class OrderAlreadyCompletedException(
    orderId: Long
) : BusinessException(OrderErrorCode.ORDER_ALREADY_COMPLETED, "주문이 이미 완료되었습니다: $orderId")

/**
 * 주문이 이미 취소되었을 때 발생하는 예외
 */
class OrderAlreadyCancelledException(
    orderId: Long
) : BusinessException(OrderErrorCode.ORDER_ALREADY_CANCELLED, "주문이 이미 취소되었습니다: $orderId")

/**
 * 완료된 주문을 취소할 수 없을 때 발생하는 예외
 */
class CannotCancelCompletedOrderException(
    orderId: Long
) : BusinessException(OrderErrorCode.CANNOT_CANCEL_COMPLETED_ORDER, "완료된 주문은 취소할 수 없습니다: $orderId")

/**
 * 주문 항목을 찾을 수 없을 때 발생하는 예외
 */
class OrderItemNotFoundException(
    orderItemId: Long
) : BusinessException(OrderErrorCode.ORDER_ITEM_NOT_FOUND, "주문 항목을 찾을 수 없습니다: $orderItemId")

/**
 * 재고가 부족할 때 발생하는 예외
 */
class InsufficientStockException(
    productId: Long,
    optionId: Long,
    requiredQuantity: Int,
    availableQuantity: Int
) : BusinessException(OrderErrorCode.INSUFFICIENT_STOCK, 
    "상품 재고가 부족합니다. 상품 ID: $productId, 옵션 ID: $optionId, 필요 수량: $requiredQuantity, 가용 수량: $availableQuantity")

/**
 * 주문에 접근할 권한이 없을 때 발생하는 예외
 */
class UnauthorizedOrderAccessException(
    orderId: Long,
    userId: Long
) : BusinessException(OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS, "주문에 접근할 권한이 없습니다. 주문 ID: $orderId, 사용자 ID: $userId")

/**
 * 결제에 실패했을 때 발생하는 예외
 */
class PaymentFailedException(
    message: String
) : BusinessException(OrderErrorCode.PAYMENT_FAILED, "결제에 실패했습니다: $message") 