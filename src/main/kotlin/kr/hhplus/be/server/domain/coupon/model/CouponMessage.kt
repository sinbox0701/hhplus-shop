package kr.hhplus.be.server.domain.coupon.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 쿠폰 발급 요청 메시지
 */
data class CouponPublishRequestMessage @JsonCreator constructor(
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("couponCode") val couponCode: String,
    @JsonProperty("requestId") val requestId: String
) 