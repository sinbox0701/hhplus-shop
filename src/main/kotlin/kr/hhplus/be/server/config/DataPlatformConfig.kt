package kr.hhplus.be.server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync

/**
 * 데이터 플랫폼 관련 설정
 * - application.yml에서 dataPlatform.enabled=true 설정 시 활성화
 */
@Configuration
@EnableAsync
@ConfigurationProperties(prefix = "data-platform")
class DataPlatformConfig {
    /**
     * 데이터 플랫폼 전송 활성화 여부
     * - 기본값: false
     */
    var enabled: Boolean = false
    
    /**
     * 데이터 플랫폼 API 엔드포인트
     */
    var apiEndpoint: String = "http://localhost:8080/api/data-platform"
} 