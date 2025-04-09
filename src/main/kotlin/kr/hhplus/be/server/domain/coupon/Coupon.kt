package kr.hhplus.be.server.domain.coupon

import java.time.LocalDateTime

enum class CouponType {
    DISCOUNT_PRODUCT, // 상품 할인
    DISCOUNT_ORDER, // 주문 할인
}

data class Coupon private constructor(
    val id: Long,
    var couponType: CouponType,
    var discountRate: Double,
    var description: String,
    var startDate: LocalDateTime,
    var endDate: LocalDateTime,
    var quantity: Int,
    var remainingQuantity: Int,
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
) {
    companion object {
        const val MIN_DISCOUNT_RATE = 1.0
        const val MAX_DISCOUNT_RATE = 100.0

        const val MIN_QUANTITY = 1
        const val MAX_QUANTITY = 100

        const val MIN_DESCRIPTION_LENGTH = 2
        const val MAX_DESCRIPTION_LENGTH = 30
        
        fun create(
            id: Long,
            discountRate: Double,
            description: String,
            startDate: LocalDateTime,
            endDate: LocalDateTime,
            quantity: Int,
            couponType: CouponType
        ): Coupon {
            require(discountRate in MIN_DISCOUNT_RATE..MAX_DISCOUNT_RATE) { "할인율은 $MIN_DISCOUNT_RATE 부터 $MAX_DISCOUNT_RATE 사이여야 합니다." }
            require(description.length in MIN_DESCRIPTION_LENGTH..MAX_DESCRIPTION_LENGTH) { "설명은 $MIN_DESCRIPTION_LENGTH 부터 $MAX_DESCRIPTION_LENGTH 사이여야 합니다." }
            require(startDate.isBefore(endDate)) { "시작일은 종료일보다 이전이어야 합니다." }
            require(quantity in MIN_QUANTITY..MAX_QUANTITY) { "쿠폰 수량은 $MIN_QUANTITY 부터 $MAX_QUANTITY 사이여야 합니다." }
            
            val now = LocalDateTime.now()
            return Coupon(
                id = id,
                discountRate = discountRate,
                description = description,
                startDate = startDate,
                endDate = endDate,
                quantity = quantity,
                remainingQuantity = quantity,
                couponType = couponType,
                createdAt = now,
                updatedAt = now
            )
        }
    }
    
    fun update(
        discountRate: Double? = null,
        description: String? = null,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        quantity: Int? = null
    ): Coupon {
        discountRate?.let {
            require(it in MIN_DISCOUNT_RATE..MAX_DISCOUNT_RATE) { "할인율은 $MIN_DISCOUNT_RATE 부터 $MAX_DISCOUNT_RATE 사이여야 합니다." }
            this.discountRate = it
        }
        
        description?.let {
            require(it.length in MIN_DESCRIPTION_LENGTH..MAX_DESCRIPTION_LENGTH) { "설명은 $MIN_DESCRIPTION_LENGTH 부터 $MAX_DESCRIPTION_LENGTH 사이여야 합니다." }
            this.description = it
        }
        
        startDate?.let { newStartDate ->
            this.startDate = newStartDate
            require(this.startDate.isBefore(this.endDate)) { "시작일은 종료일보다 이전이어야 합니다." }
        }
        
        endDate?.let { newEndDate ->
            this.endDate = newEndDate
            require(this.startDate.isBefore(this.endDate)) { "시작일은 종료일보다 이전이어야 합니다." }
        }
        
        quantity?.let { newQuantity ->
            require(newQuantity in MIN_QUANTITY..MAX_QUANTITY) { "쿠폰 수량은 $MIN_QUANTITY 부터 $MAX_QUANTITY 사이여야 합니다." }
            val diff = newQuantity - this.quantity
            this.quantity = newQuantity
            this.remainingQuantity += diff
            require(this.remainingQuantity >= 0) { "남은 쿠폰 수량은 0보다 작을 수 없습니다." }
        }
        
        this.updatedAt = LocalDateTime.now()
        return this
    }
    
    fun decreaseQuantity(): Coupon {
        require(remainingQuantity > 0) { "남은 쿠폰이 없습니다." }
        remainingQuantity--
        this.updatedAt = LocalDateTime.now()
        return this
    }
    
    fun hasRemainingQuantity(): Boolean = remainingQuantity > 0
    
    fun isValid(): Boolean {
        val now = LocalDateTime.now()
        return now.isAfter(startDate) && now.isBefore(endDate) && remainingQuantity > 0
    }
}
