package kr.hhplus.be.server.shared.lock

/**
 * 분산 락에서 사용되는 락 키의 상수 값들을 관리하는 클래스
 * 
 * 모든 도메인 및 리소스 타입에 대한 상수를 중앙에서 관리하여
 * 일관된 락 키 생성 패턴을 보장합니다.
 */
object LockKeyConstants {
    // 도메인 프리픽스 정의
    const val ORDER_PREFIX = "order"
    const val PRODUCT_PREFIX = "product"
    const val USER_PREFIX = "user"
    const val COUPON_USER_PREFIX = "coupon-user"
    const val COUPON_EVENT_PREFIX = "coupon-event"
    const val USER_POINT_PREFIX = "user-point"
    const val ORDER_ITEM_RANK_PREFIX = "order-item-rank"
    
    // 리소스 타입 정의
    const val RESOURCE_ID = "id"
    const val RESOURCE_USER = "user"
    const val RESOURCE_STOCK = "stock"
    const val RESOURCE_POINT = "point"
    const val RESOURCE_EVENT = "event"
    const val RESOURCE_PAYMENT = "payment"
    const val RESOURCE_STATUS = "status"
    
    // 구분자
    const val SEPARATOR = ":"
    const val RESOURCE_SEPARATOR = "-"
    
    // 타임아웃 기본값 (초 단위)
    const val DEFAULT_TIMEOUT = 10L
    const val EXTENDED_TIMEOUT = 30L
} 