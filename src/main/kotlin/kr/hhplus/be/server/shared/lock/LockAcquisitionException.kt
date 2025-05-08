package kr.hhplus.be.server.shared.lock

/**
 * 분산 락 획득 실패 예외
 * 락을 획득하지 못했을 때 발생하는 예외
 */
class LockAcquisitionException(message: String) : RuntimeException(message) 