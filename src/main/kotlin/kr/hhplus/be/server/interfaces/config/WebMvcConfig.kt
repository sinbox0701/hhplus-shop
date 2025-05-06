package kr.hhplus.be.server.interfaces.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * 웹 MVC 설정 클래스
 */
@Configuration
class WebMvcConfig(
    private val loggingInterceptor: LoggingInterceptor
) : WebMvcConfigurer {
    
    /**
     * 인터셉터 등록
     */
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(loggingInterceptor)
            .addPathPatterns("/api/**") // API 호출 패턴에만 적용
    }
} 