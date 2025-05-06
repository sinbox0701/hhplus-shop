package kr.hhplus.be.server.domain.common

import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 시간 관련 의존성을 추상화하기 위한 인터페이스
 * 테스트에서는 고정된 시간을 제공하는 구현체를 주입하여 사용할 수 있습니다.
 */
interface TimeProvider {
    fun now(): LocalDateTime
    fun today(): LocalDate
}

/**
 * 실제 시스템 시간을 제공하는 기본 구현체
 */
@Component
class DefaultTimeProvider : TimeProvider {
    override fun now(): LocalDateTime = LocalDateTime.now()
    override fun today(): LocalDate = LocalDate.now()
}

/**
 * 테스트용 고정 시간 제공 구현체
 */
class FixedTimeProvider(
    private val fixedDateTime: LocalDateTime
) : TimeProvider {
    override fun now(): LocalDateTime = fixedDateTime
    override fun today(): LocalDate = fixedDateTime.toLocalDate()
} 