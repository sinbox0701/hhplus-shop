package kr.hhplus.be.server.domain.user.exception

import kr.hhplus.be.server.domain.common.exception.BusinessException
import kr.hhplus.be.server.domain.common.exception.ErrorCode
import org.springframework.http.HttpStatus

/**
 * 사용자 관련 에러 코드
 */
enum class UserErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String
) : ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다"),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "U002", "계좌를 찾을 수 없습니다"),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "U003", "중복된 로그인 ID입니다"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U004", "중복된 이메일입니다"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "U005", "인증 정보가 올바르지 않습니다"),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "U006", "잔액이 부족합니다"),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "U007", "유효하지 않은 금액입니다"),
    USER_ACCOUNT_MISMATCH(HttpStatus.FORBIDDEN, "U008", "계좌의 소유자가 아닙니다"),
    WITHDRAWAL_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "U009", "출금 한도를 초과했습니다"),
    OPTIMISTIC_LOCK_EXCEPTION(HttpStatus.CONFLICT, "U010", "동시 접근으로 인한 충돌이 발생했습니다"),
}

/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 */
class UserNotFoundException(
    userId: Long
) : BusinessException(UserErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: $userId")

/**
 * 계좌를 찾을 수 없을 때 발생하는 예외
 */
class AccountNotFoundException(
    userId: Long? = null,
    accountId: Long? = null
) : BusinessException(UserErrorCode.ACCOUNT_NOT_FOUND, 
    when {
        userId != null && accountId != null -> "계좌를 찾을 수 없습니다. 사용자 ID: $userId, 계좌 ID: $accountId"
        userId != null -> "사용자의 계좌를 찾을 수 없습니다. 사용자 ID: $userId"
        accountId != null -> "계좌를 찾을 수 없습니다. 계좌 ID: $accountId"
        else -> "계좌를 찾을 수 없습니다."
    }
)

/**
 * 중복된 로그인 ID일 때 발생하는 예외
 */
class DuplicateLoginIdException(
    loginId: String
) : BusinessException(UserErrorCode.DUPLICATE_LOGIN_ID, "중복된 로그인 ID입니다: $loginId")

/**
 * 중복된 이메일일 때 발생하는 예외
 */
class DuplicateEmailException(
    email: String
) : BusinessException(UserErrorCode.DUPLICATE_EMAIL, "중복된 이메일입니다: $email")

/**
 * 인증 정보가 올바르지 않을 때 발생하는 예외
 */
class InvalidCredentialsException(
    message: String = UserErrorCode.INVALID_CREDENTIALS.message
) : BusinessException(UserErrorCode.INVALID_CREDENTIALS, message)

/**
 * 잔액이 부족할 때 발생하는 예외
 */
class InsufficientBalanceException(
    accountId: Long,
    currentBalance: Double,
    requiredAmount: Double
) : BusinessException(UserErrorCode.INSUFFICIENT_BALANCE, 
    "잔액이 부족합니다. 계좌 ID: $accountId, 현재 잔액: $currentBalance, 필요 금액: $requiredAmount")

/**
 * 유효하지 않은 금액일 때 발생하는 예외
 */
class InvalidAmountException(
    amount: Double
) : BusinessException(UserErrorCode.INVALID_AMOUNT, "유효하지 않은 금액입니다: $amount")

/**
 * 계좌의 소유자가 아닐 때 발생하는 예외
 */
class UserAccountMismatchException(
    accountId: Long,
    userId: Long
) : BusinessException(UserErrorCode.USER_ACCOUNT_MISMATCH, 
    "계좌의 소유자가 아닙니다. 계좌 ID: $accountId, 사용자 ID: $userId")

/**
 * 출금 한도를 초과했을 때 발생하는 예외
 */
class WithdrawalLimitExceededException(
    userId: Long,
    amount: Double,
    dailyLimit: Double
) : BusinessException(UserErrorCode.WITHDRAWAL_LIMIT_EXCEEDED, 
    "출금 한도를 초과했습니다. 사용자 ID: $userId, 요청 금액: $amount, 일일 한도: $dailyLimit")

/**
 * 동시 접근으로 인한 충돌이 발생했을 때 발생하는 예외
 */
class OptimisticLockException(
    accountId: Long
) : BusinessException(UserErrorCode.OPTIMISTIC_LOCK_EXCEPTION, 
    "동시 접근으로 인한 충돌이 발생했습니다. 계좌 ID: $accountId") 