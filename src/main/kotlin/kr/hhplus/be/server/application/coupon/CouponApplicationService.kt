package kr.hhplus.be.server.application.coupon

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.config.KafkaConfig
import kr.hhplus.be.server.domain.coupon.model.CouponPublishRequestMessage
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.coupon.service.CouponService
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CouponApplicationService(
    private val couponService: CouponService,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    /**
     * 선착순 쿠폰 발급 요청
     * 카프카를 통해 메시지를 발행하여 비동기적으로 처리
     * 메시지의 키는 쿠폰 코드로 설정하여 같은 쿠폰에 대한 요청은 동일한 파티션에서 처리되도록 함
     */
    fun requestFirstComeFirstServedCoupon(userId: Long, couponCode: String): String {
        // 쿠폰이 존재하는지 확인
        val coupon = couponService.findByCode(couponCode)
        
        // 유효한 쿠폰인지 확인
        if (!couponService.isValidCoupon(coupon.id!!)) {
            throw IllegalStateException("유효하지 않은 쿠폰입니다: $couponCode")
        }
        
        // 요청 ID 생성
        val requestId = UUID.randomUUID().toString()
        
        // 메시지 생성
        val message = CouponPublishRequestMessage(userId, couponCode, requestId)
        
        // 메시지 발행 (키는 쿠폰 코드로 설정하여 동일 쿠폰에 대한 요청은 같은 파티션에 할당되도록 함)
        kafkaTemplate.send(
            KafkaConfig.COUPON_PUBLISH_REQUEST_TOPIC,
            couponCode, // 키를 쿠폰 코드로 설정
            objectMapper.writeValueAsString(message)
        )
        
        return requestId
    }
    
    /**
     * 사용자의 쿠폰 목록 조회
     */
    fun getUserCoupons(userId: Long): List<UserCoupon> {
        return couponService.findUserCouponsByUserId(userId)
    }
} 