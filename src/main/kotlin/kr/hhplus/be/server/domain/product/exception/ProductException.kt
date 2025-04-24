package kr.hhplus.be.server.domain.product.exception

import kr.hhplus.be.server.domain.common.exception.BusinessException
import kr.hhplus.be.server.domain.common.exception.ErrorCode
import org.springframework.http.HttpStatus

/**
 * 상품 관련 에러 코드
 */
enum class ProductErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String
) : ErrorCode {
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "상품을 찾을 수 없습니다"),
    PRODUCT_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "P002", "상품 옵션을 찾을 수 없습니다"),
    INVALID_PRODUCT_PRICE(HttpStatus.BAD_REQUEST, "P003", "유효하지 않은 상품 가격입니다"),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "P004", "상품 재고가 부족합니다"),
    PRODUCT_ALREADY_EXISTS(HttpStatus.CONFLICT, "P005", "이미 존재하는 상품입니다"),
    OPTION_BELONGS_TO_DIFFERENT_PRODUCT(HttpStatus.BAD_REQUEST, "P006", "옵션이 다른 상품에 속해 있습니다"),
    PRODUCT_DELETION_FAILED(HttpStatus.BAD_REQUEST, "P007", "상품 삭제에 실패했습니다"),
    PRODUCT_UPDATE_FAILED(HttpStatus.BAD_REQUEST, "P008", "상품 업데이트에 실패했습니다"),
    MINIMUM_ONE_OPTION_REQUIRED(HttpStatus.BAD_REQUEST, "P009", "최소 하나의 옵션이 필요합니다"),
}

/**
 * 상품을 찾을 수 없을 때 발생하는 예외
 */
class ProductNotFoundException(
    productId: Long
) : BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND, "상품을 찾을 수 없습니다: $productId")

/**
 * 상품 옵션을 찾을 수 없을 때 발생하는 예외
 */
class ProductOptionNotFoundException(
    optionId: Long
) : BusinessException(ProductErrorCode.PRODUCT_OPTION_NOT_FOUND, "상품 옵션을 찾을 수 없습니다: $optionId")

/**
 * 유효하지 않은 상품 가격일 때 발생하는 예외
 */
class InvalidProductPriceException(
    price: Double
) : BusinessException(ProductErrorCode.INVALID_PRODUCT_PRICE, "유효하지 않은 상품 가격입니다: $price")

/**
 * 상품 재고가 부족할 때 발생하는 예외
 */
class ProductInsufficientStockException(
    productId: Long,
    optionId: Long,
    requiredQuantity: Int,
    availableQuantity: Int
) : BusinessException(ProductErrorCode.INSUFFICIENT_STOCK, 
    "상품 재고가 부족합니다. 상품 ID: $productId, 옵션 ID: $optionId, 필요 수량: $requiredQuantity, 가용 수량: $availableQuantity")

/**
 * 상품이 이미 존재할 때 발생하는 예외
 */
class ProductAlreadyExistsException(
    productName: String
) : BusinessException(ProductErrorCode.PRODUCT_ALREADY_EXISTS, "이미 존재하는 상품입니다: $productName")

/**
 * 옵션이 다른 상품에 속해 있을 때 발생하는 예외
 */
class OptionBelongsToDifferentProductException(
    optionId: Long,
    currentProductId: Long,
    requestedProductId: Long
) : BusinessException(ProductErrorCode.OPTION_BELONGS_TO_DIFFERENT_PRODUCT, 
    "옵션이 다른 상품에 속해 있습니다. 옵션 ID: $optionId, 현재 상품 ID: $currentProductId, 요청 상품 ID: $requestedProductId")

/**
 * 상품 삭제에 실패했을 때 발생하는 예외
 */
class ProductDeletionFailedException(
    productId: Long,
    reason: String
) : BusinessException(ProductErrorCode.PRODUCT_DELETION_FAILED, "상품 삭제에 실패했습니다. 상품 ID: $productId, 이유: $reason")

/**
 * 상품 업데이트에 실패했을 때 발생하는 예외
 */
class ProductUpdateFailedException(
    productId: Long,
    reason: String
) : BusinessException(ProductErrorCode.PRODUCT_UPDATE_FAILED, "상품 업데이트에 실패했습니다. 상품 ID: $productId, 이유: $reason")

/**
 * 최소 하나의 옵션이 필요할 때 발생하는 예외
 */
class MinimumOneOptionRequiredException(
    productId: Long
) : BusinessException(ProductErrorCode.MINIMUM_ONE_OPTION_REQUIRED, "최소 하나의 옵션이 필요합니다. 상품 ID: $productId") 