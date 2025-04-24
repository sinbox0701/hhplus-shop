package kr.hhplus.be.server.domain.coupon.exception

import kr.hhplus.be.server.domain.common.exception.BusinessException
import kr.hhplus.be.server.domain.common.exception.ErrorCode
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

/**
 * 쿠폰 관련 에러 코드
 */
enum class CouponErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String
) : ErrorCode {
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "C001", "쿠폰을 찾을 수 없습니다"),
    USER_COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "사용자 쿠폰을 찾을 수 없습니다"),
    COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST, "C003", "이미 사용된 쿠폰입니다"),
    COUPON_ALREADY_ISSUED(HttpStatus.CONFLICT, "C004", "이미 발급된 쿠폰입니다"),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "C005", "만료된 쿠폰입니다"),
    COUPON_NOT_STARTED(HttpStatus.BAD_REQUEST, "C006", "아직 사용할 수 없는 쿠폰입니다"),
    COUPON_QUANTITY_EXCEEDED(HttpStatus.BAD_REQUEST, "C007", "쿠폰 수량이 초과되었습니다"),
    INVALID_COUPON_CODE(HttpStatus.BAD_REQUEST, "C008", "유효하지 않은 쿠폰 코드입니다"),
    COUPON_NOT_APPLICABLE(HttpStatus.BAD_REQUEST, "C009", "적용할 수 없는 쿠폰입니다"),
    COUPON_ISSUE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "C010", "쿠폰 발급에 실패했습니다"),
}

/**
 * 쿠폰을 찾을 수 없을 때 발생하는 예외
 */
class CouponNotFoundException(
    couponId: Long? = null,
    couponCode: String? = null
) : BusinessException(CouponErrorCode.COUPON_NOT_FOUND, 
    when {
        couponId != null -> "쿠폰을 찾을 수 없습니다. 쿠폰 ID: $couponId"
        couponCode != null -> "쿠폰을 찾을 수 없습니다. 쿠폰 코드: $couponCode"
        else -> "쿠폰을 찾을 수 없습니다."
    }
)

/**
 * 사용자 쿠폰을 찾을 수 없을 때 발생하는 예외
 */
class UserCouponNotFoundException(
    userCouponId: Long? = null,
    userId: Long? = null,
    couponId: Long? = null
) : BusinessException(CouponErrorCode.USER_COUPON_NOT_FOUND, 
    when {
        userCouponId != null -> "사용자 쿠폰을 찾을 수 없습니다. 사용자 쿠폰 ID: $userCouponId"
        userId != null && couponId != null -> "사용자 쿠폰을 찾을 수 없습니다. 사용자 ID: $userId, 쿠폰 ID: $couponId"
        userId != null -> "사용자의 쿠폰을 찾을 수 없습니다. 사용자 ID: $userId"
        else -> "사용자 쿠폰을 찾을 수 없습니다."
    }
)

/**
 * 이미 사용된 쿠폰일 때 발생하는 예외
 */
class CouponAlreadyUsedException(
    userCouponId: Long
) : BusinessException(CouponErrorCode.COUPON_ALREADY_USED, "이미 사용된 쿠폰입니다. 사용자 쿠폰 ID: $userCouponId")

/**
 * 이미 발급된 쿠폰일 때 발생하는 예외
 */
class CouponAlreadyIssuedException(
    userId: Long,
    couponId: Long
) : BusinessException(CouponErrorCode.COUPON_ALREADY_ISSUED, "이미 발급된 쿠폰입니다. 사용자 ID: $userId, 쿠폰 ID: $couponId")

/**
 * 만료된 쿠폰일 때 발생하는 예외
 */
class CouponExpiredException(
    couponId: Long,
    endDate: LocalDateTime
) : BusinessException(CouponErrorCode.COUPON_EXPIRED, "만료된 쿠폰입니다. 쿠폰 ID: $couponId, 만료일: $endDate")

/**
 * 아직 사용할 수 없는 쿠폰일 때 발생하는 예외
 */
class CouponNotStartedException(
    couponId: Long,
    startDate: LocalDateTime
) : BusinessException(CouponErrorCode.COUPON_NOT_STARTED, "아직 사용할 수 없는 쿠폰입니다. 쿠폰 ID: $couponId, 시작일: $startDate")

/**
 * 쿠폰 수량이 초과되었을 때 발생하는 예외
 */
class CouponQuantityExceededException(
    couponId: Long,
    quantity: Int
) : BusinessException(CouponErrorCode.COUPON_QUANTITY_EXCEEDED, "쿠폰 수량이 초과되었습니다. 쿠폰 ID: $couponId, 최대 수량: $quantity")

/**
 * 유효하지 않은 쿠폰 코드일 때 발생하는 예외
 */
class InvalidCouponCodeException(
    couponCode: String
) : BusinessException(CouponErrorCode.INVALID_COUPON_CODE, "유효하지 않은 쿠폰 코드입니다: $couponCode")

/**
 * 적용할 수 없는 쿠폰일 때 발생하는 예외
 */
class CouponNotApplicableException(
    couponId: Long,
    reason: String
) : BusinessException(CouponErrorCode.COUPON_NOT_APPLICABLE, "적용할 수 없는 쿠폰입니다. 쿠폰 ID: $couponId, 이유: $reason")

/**
 * 쿠폰 발급에 실패했을 때 발생하는 예외
 */
class CouponIssueFailedException(
    couponId: Long,
    userId: Long,
    reason: String
) : BusinessException(CouponErrorCode.COUPON_ISSUE_FAILED, "쿠폰 발급에 실패했습니다. 쿠폰 ID: $couponId, 사용자 ID: $userId, 이유: $reason") 