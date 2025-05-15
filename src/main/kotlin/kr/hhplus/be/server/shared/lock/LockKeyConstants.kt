package kr.hhplus.be.server.shared.lock

/**
 * 분산 락 키 관련 상수 정의
 * 모든 락 키 생성에 사용되는 일관된 상수를 제공합니다.
 */
object LockKeyConstants {
    // 락 키 구분자
    const val SEPARATOR = ":"
    const val RESOURCE_SEPARATOR = "-"
    
    // 타임아웃 상수 (초 단위)
    const val DEFAULT_TIMEOUT = 10L
    const val EXTENDED_TIMEOUT = 30L
    
    // 도메인 프리픽스
    const val ORDER_PREFIX = "order"
    const val PRODUCT_PREFIX = "product"
    const val USER_PREFIX = "user"
    const val USER_POINT_PREFIX = "user-point"
    const val COUPON_PREFIX = "coupon"
    const val COUPON_USER_PREFIX = "coupon-user"
    const val ORDER_ITEM_RANK_PREFIX = "order-item-rank"
    
    // 리소스 타입
    const val RESOURCE_ID = "id"
    const val RESOURCE_USER = "user"
    const val RESOURCE_STOCK = "stock"
    const val RESOURCE_STATUS = "status"
    const val RESOURCE_PAYMENT = "payment"
    const val RESOURCE_POINT = "point"
    const val RESOURCE_EVENT = "event"
} 