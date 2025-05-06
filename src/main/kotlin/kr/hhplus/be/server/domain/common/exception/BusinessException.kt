package kr.hhplus.be.server.domain.common.exception

import org.springframework.http.HttpStatus

/**
 * 비즈니스 예외의 기본 클래스
 */
abstract class BusinessException(
    val errorCode: ErrorCode,
    override val message: String? = errorCode.message
) : RuntimeException(message)

/**
 * 에러 코드 인터페이스
 */
interface ErrorCode {
    val status: HttpStatus
    val code: String
    val message: String
}

/**
 * 기본 에러 코드 열거형 클래스
 */
enum class CommonErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String
) : ErrorCode {
    // 일반 오류
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C002", "인증이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C003", "접근 권한이 없습니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "리소스를 찾을 수 없습니다"),
    CONFLICT(HttpStatus.CONFLICT, "C005", "충돌이 발생했습니다"),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C006", "서버 오류가 발생했습니다"),
    
    // 공통 비즈니스 오류
    INVALID_STATE(HttpStatus.BAD_REQUEST, "C010", "유효하지 않은 상태입니다"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C011", "리소스를 찾을 수 없습니다"),
    RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "C012", "리소스가 이미 존재합니다"),
    OPERATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "C013", "작업이 허용되지 않습니다"),
}

/**
 * 리소스를 찾지 못했을 때 발생하는 예외
 */
class ResourceNotFoundException(
    message: String? = CommonErrorCode.RESOURCE_NOT_FOUND.message,
    val resourceName: String? = null,
    val fieldName: String? = null,
    val fieldValue: Any? = null
) : BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, 
    message ?: "${resourceName ?: "리소스"}를 찾을 수 없습니다. ${fieldName ?: ""}: ${fieldValue ?: ""}")

/**
 * 리소스가 이미 존재할 때 발생하는 예외
 */
class ResourceAlreadyExistsException(
    message: String? = CommonErrorCode.RESOURCE_ALREADY_EXISTS.message,
    val resourceName: String? = null,
    val fieldName: String? = null,
    val fieldValue: Any? = null
) : BusinessException(CommonErrorCode.RESOURCE_ALREADY_EXISTS,
    message ?: "${resourceName ?: "리소스"}가 이미 존재합니다. ${fieldName ?: ""}: ${fieldValue ?: ""}")

/**
 * 잘못된 상태 예외
 */
class InvalidStateException(
    message: String
) : BusinessException(CommonErrorCode.INVALID_STATE, message)

/**
 * 작업이 허용되지 않는 예외
 */
class OperationNotAllowedException(
    message: String
) : BusinessException(CommonErrorCode.OPERATION_NOT_ALLOWED, message) 