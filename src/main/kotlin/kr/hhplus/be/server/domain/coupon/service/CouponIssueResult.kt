package kr.hhplus.be.server.domain.coupon.service

/**
 * 쿠폰 발급 결과를 나타내는 클래스
 */
sealed class CouponIssueResult {
    /**
     * 발급 성공
     * @param couponId 발급된 쿠폰 ID
     */
    data class Success(val couponId: String) : CouponIssueResult()
    
    /**
     * 발급 실패
     * @param reason 실패 이유
     */
    data class Failure(val reason: String) : CouponIssueResult()
} 