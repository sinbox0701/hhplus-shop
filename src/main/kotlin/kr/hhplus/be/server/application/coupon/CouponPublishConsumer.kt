package kr.hhplus.be.server.application.coupon

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.config.KafkaConfig
import kr.hhplus.be.server.domain.coupon.model.CouponPublishRequestMessage
import kr.hhplus.be.server.domain.coupon.service.CouponService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class CouponPublishConsumer(
    private val couponService: CouponService,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * 쿠폰 발급 요청 메시지 소비
     * 같은 쿠폰 코드에 대한 요청은 동일한 파티션에서 처리됨으로써 순서가 보장됨
     */
    @KafkaListener(
        topics = [KafkaConfig.COUPON_PUBLISH_REQUEST_TOPIC],
        groupId = KafkaConfig.COUPON_CONSUMER_GROUP_ID
    )
    fun consume(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_KEY) key: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int
    ) {
        try {
            logger.info("쿠폰 발급 요청 메시지 수신 - 파티션: $partition, 키: $key, 메시지: $message")
            
            // 메시지 역직렬화
            val couponPublishRequest = objectMapper.readValue(message, CouponPublishRequestMessage::class.java)
            
            // 쿠폰 발급 처리
            processFirstComeFirstServedCoupon(couponPublishRequest, partition)
            
        } catch (e: Exception) {
            logger.error("쿠폰 발급 처리 중 오류 발생", e)
        }
    }
    
    /**
     * 선착순 쿠폰 발급 처리
     */
    private fun processFirstComeFirstServedCoupon(request: CouponPublishRequestMessage, partition: Int) {
        try {
            logger.info("선착순 쿠폰 발급 처리 - 사용자: ${request.userId}, 쿠폰: ${request.couponCode}, 요청ID: ${request.requestId}, 파티션: $partition")
            
            // 쿠폰 발급 (트랜잭션 및 비관적 락을 통한 동시성 제어)
            val userCoupon = couponService.issueFirstComeFirstServedCoupon(request.userId, request.couponCode)
            
            logger.info("쿠폰 발급 성공 - 사용자: ${request.userId}, 쿠폰: ${request.couponCode}, 쿠폰ID: ${userCoupon.id}, 요청ID: ${request.requestId}")
            
        } catch (e: Exception) {
            logger.error("쿠폰 발급 실패 - 사용자: ${request.userId}, 쿠폰: ${request.couponCode}, 요청ID: ${request.requestId}", e)
            // 여기서 필요에 따라 실패 이벤트를 발행하거나 별도 처리를 할 수 있음
        }
    }
} 