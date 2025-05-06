package kr.hhplus.be.server.interfaces.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * API 요청 및 응답을 로깅하기 위한 인터셉터
 */
@Component
class LoggingInterceptor : HandlerInterceptor {
    
    private val log = LoggerFactory.getLogger(LoggingInterceptor::class.java)
    
    companion object {
        private const val START_TIME_ATTRIBUTE = "requestStartTime"
    }
    
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // 요청 시작 시간 저장
        request.setAttribute(START_TIME_ATTRIBUTE, LocalDateTime.now())
        
        // 요청 정보 로깅
        log.info(
            "Request: {} {} (User-Agent: {}, Client IP: {})",
            request.method,
            request.requestURI,
            request.getHeader("User-Agent"),
            request.remoteAddr
        )
        
        return true
    }
    
    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        // 컨트롤러 메서드 실행 후 호출됨
    }
    
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        // 요청 처리 완료 후 호출됨
        val startTime = request.getAttribute(START_TIME_ATTRIBUTE) as LocalDateTime
        val endTime = LocalDateTime.now()
        val executionTime = ChronoUnit.MILLIS.between(startTime, endTime)
        
        if (ex == null) {
            // 정상 응답 로깅
            log.info(
                "Response: {} {} - {} ({} ms)",
                request.method,
                request.requestURI,
                response.status,
                executionTime
            )
        } else {
            // 예외 발생 시 로깅
            log.error(
                "Exception: {} {} - {} ({} ms): {}",
                request.method,
                request.requestURI,
                response.status,
                executionTime,
                ex.message
            )
        }
    }
} 