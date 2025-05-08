package kr.hhplus.be.server.shared.lock

/**
 * 일관된 락 키 생성을 위한 유틸리티 클래스
 * 
 * 모든 파사드 레이어에서 이 클래스를 통해 락 키를 생성하여
 * 일관성 있고 체계적인 락 키 관리가 가능하도록 합니다.
 */
object LockKeyGenerator {
    
    /**
     * 도메인과 리소스 타입, 리소스 ID를 조합하여 락 키를 생성합니다.
     * 
     * @param domain 도메인 프리픽스 (예: order, product 등)
     * @param resourceType 리소스 타입 (예: id, user, stock 등)
     * @param resourceId 리소스의 고유 식별자
     * @return 생성된 락 키 (예: "order-user:123")
     */
    fun generate(domain: String, resourceType: String, resourceId: String): String {
        return "$domain${LockKeyConstants.RESOURCE_SEPARATOR}$resourceType${LockKeyConstants.SEPARATOR}$resourceId"
    }
    
    /**
     * 주문 관련 락 키 생성
     */
    object Order {
        /**
         * 사용자 기반 주문 락
         * 주로 사용자별 주문 생성 시 사용
         */
        fun userLock(userId: String) = generate(
            LockKeyConstants.ORDER_PREFIX,
            LockKeyConstants.RESOURCE_USER,
            userId
        )
        
        /**
         * 주문 ID 기반 주문 락
         * 주로 주문 상태 변경이나 상세 정보 수정 시 사용
         */
        fun idLock(orderId: String) = generate(
            LockKeyConstants.ORDER_PREFIX,
            LockKeyConstants.RESOURCE_ID,
            orderId
        )
        
        /**
         * 주문 결제 락
         * 결제 처리 시 사용
         */
        fun paymentLock(orderId: String) = generate(
            LockKeyConstants.ORDER_PREFIX,
            LockKeyConstants.RESOURCE_PAYMENT,
            orderId
        )
        
        /**
         * 주문 상태 변경 락
         * 주문 상태 업데이트 시 사용
         */
        fun statusLock(orderId: String) = generate(
            LockKeyConstants.ORDER_PREFIX,
            LockKeyConstants.RESOURCE_STATUS,
            orderId
        )
    }
    
    /**
     * 상품 관련 락 키 생성
     */
    object Product {
        /**
         * 상품 재고 락
         * 재고 수량 변경 시 사용
         */
        fun stockLock(productId: String) = generate(
            LockKeyConstants.PRODUCT_PREFIX,
            LockKeyConstants.RESOURCE_STOCK,
            productId
        )
        
        /**
         * 상품 ID 기반 락
         * 상품 정보 수정 시 사용
         */
        fun idLock(productId: String) = generate(
            LockKeyConstants.PRODUCT_PREFIX,
            LockKeyConstants.RESOURCE_ID,
            productId
        )
    }
    
    /**
     * 사용자 관련 락 키 생성
     */
    object User {
        /**
         * 사용자 ID 기반 락
         * 사용자 정보 수정 시 사용
         */
        fun idLock(userId: String) = generate(
            LockKeyConstants.USER_PREFIX,
            LockKeyConstants.RESOURCE_ID,
            userId
        )
    }
    
    /**
     * 쿠폰 사용자 관련 락 키 생성
     */
    object CouponUser {
        /**
         * 쿠폰 사용자 ID 기반 락
         * 쿠폰 사용 처리 시 사용
         */
        fun idLock(couponUserId: String) = generate(
            LockKeyConstants.COUPON_USER_PREFIX,
            LockKeyConstants.RESOURCE_ID,
            couponUserId
        )
        
        /**
         * 사용자별 쿠폰 락
         * 특정 사용자의 쿠폰 관련 처리 시 사용
         */
        fun userLock(userId: String) = generate(
            LockKeyConstants.COUPON_USER_PREFIX,
            LockKeyConstants.RESOURCE_USER,
            userId
        )
    }
    
    /**
     * 쿠폰 이벤트 관련 락 키 생성
     */
    object CouponEvent {
        /**
         * 쿠폰 이벤트 ID 기반 락
         * 이벤트 정보 수정 시 사용
         */
        fun idLock(couponEventId: String) = generate(
            LockKeyConstants.COUPON_PREFIX,
            LockKeyConstants.RESOURCE_ID,
            couponEventId
        )
        
        /**
         * 쿠폰 이벤트 참여 락
         * 이벤트 참여 처리 시 사용
         */
        fun eventLock(couponEventId: String) = generate(
            LockKeyConstants.COUPON_PREFIX,
            LockKeyConstants.RESOURCE_EVENT,
            couponEventId
        )
    }
    
    /**
     * 사용자 포인트 관련 락 키 생성
     */
    object UserPoint {
        /**
         * 사용자별 포인트 락
         * 포인트 증감 처리 시 사용
         */
        fun userLock(userId: String) = generate(
            LockKeyConstants.USER_POINT_PREFIX,
            LockKeyConstants.RESOURCE_USER,
            userId
        )
        
        /**
         * 포인트 ID 기반 락
         */
        fun idLock(pointId: String) = generate(
            LockKeyConstants.USER_POINT_PREFIX,
            LockKeyConstants.RESOURCE_ID,
            pointId
        )
        
        /**
         * 포인트 증감 락
         */
        fun pointLock(userId: String) = generate(
            LockKeyConstants.USER_POINT_PREFIX,
            LockKeyConstants.RESOURCE_POINT,
            userId
        )
    }
    
    /**
     * 주문 아이템 랭킹 관련 락 키 생성
     */
    object OrderItemRank {
        /**
         * 랭킹 업데이트 락
         * 랭킹 갱신 처리 시 사용
         */
        fun updateLock(rankType: String) = generate(
            LockKeyConstants.ORDER_ITEM_RANK_PREFIX,
            LockKeyConstants.RESOURCE_ID,
            rankType
        )
    }
} 