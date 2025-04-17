package kr.hhplus.be.server.domain.coupon.model

import java.time.LocalDateTime

enum class CouponType {
    DISCOUNT_PRODUCT, // 상품 할인
    DISCOUNT_ORDER, // 주문 할인
}

data class Coupon private constructor(
    val id: Long? = null,
    val couponType: CouponType,
    val code: String,
    val discountRate: Double,
    val description: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val quantity: Int,
    val remainingQuantity: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        const val MIN_DISCOUNT_RATE = 1.0
        const val MAX_DISCOUNT_RATE = 100.0

        const val MIN_QUANTITY = 1
        const val MAX_QUANTITY = 100

        const val CODE_LENGTH = 6
        private val CODE_PATTERN = Regex("^[A-Z]{6}$")

        const val MIN_DESCRIPTION_LENGTH = 2
        const val MAX_DESCRIPTION_LENGTH = 30
        
        fun create(
            code: String,
            discountRate: Double,
            description: String,
            startDate: LocalDateTime,
            endDate: LocalDateTime,
            quantity: Int,
            couponType: CouponType
        ): Coupon {
            require(CODE_PATTERN.matches(code)) { "쿠폰 코드는 대문자 영어 6자리여야 합니다." }
            require(discountRate in MIN_DISCOUNT_RATE..MAX_DISCOUNT_RATE) { "할인율은 $MIN_DISCOUNT_RATE 부터 $MAX_DISCOUNT_RATE 사이여야 합니다." }
            require(description.length in MIN_DESCRIPTION_LENGTH..MAX_DESCRIPTION_LENGTH) { "설명은 $MIN_DESCRIPTION_LENGTH 부터 $MAX_DESCRIPTION_LENGTH 사이여야 합니다." }
            require(startDate.isBefore(endDate)) { "시작일은 종료일보다 이전이어야 합니다." }
            require(quantity in MIN_QUANTITY..MAX_QUANTITY) { "쿠폰 수량은 $MIN_QUANTITY 부터 $MAX_QUANTITY 사이여야 합니다." }
            
            val now = LocalDateTime.now()
            return Coupon(
                code = code,
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
        val updatedDiscountRate = discountRate?.also {
            require(it in MIN_DISCOUNT_RATE..MAX_DISCOUNT_RATE) { "할인율은 $MIN_DISCOUNT_RATE 부터 $MAX_DISCOUNT_RATE 사이여야 합니다." }
        } ?: this.discountRate
        
        val updatedDescription = description?.also {
            require(it.length in MIN_DESCRIPTION_LENGTH..MAX_DESCRIPTION_LENGTH) { "설명은 $MIN_DESCRIPTION_LENGTH 부터 $MAX_DESCRIPTION_LENGTH 사이여야 합니다." }
        } ?: this.description
        
        val updatedStartDate = startDate ?: this.startDate
        val updatedEndDate = endDate ?: this.endDate
        
        // 시작일과 종료일 검증
        require(updatedStartDate.isBefore(updatedEndDate)) { "시작일은 종료일보다 이전이어야 합니다." }
        
        var updatedRemainingQuantity = this.remainingQuantity
        val updatedQuantity = quantity?.let { newQuantity ->
            require(newQuantity in MIN_QUANTITY..MAX_QUANTITY) { "쿠폰 수량은 $MIN_QUANTITY 부터 $MAX_QUANTITY 사이여야 합니다." }
            val diff = newQuantity - this.quantity
            updatedRemainingQuantity += diff
            require(updatedRemainingQuantity >= 0) { "남은 쿠폰 수량은 0보다 작을 수 없습니다." }
            newQuantity
        } ?: this.quantity
        
        return Coupon(
            id = this.id,
            couponType = this.couponType,
            code = this.code,
            discountRate = updatedDiscountRate,
            description = updatedDescription,
            startDate = updatedStartDate,
            endDate = updatedEndDate,
            quantity = updatedQuantity,
            remainingQuantity = updatedRemainingQuantity,
            createdAt = this.createdAt,
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun decreaseQuantity(count: Int): Coupon {
        require(remainingQuantity > 0) { "남은 쿠폰이 없습니다." }
        require(count > 0) { "쿠폰 수량은 0보다 크게 감소할 수 없습니다." }
        
        return Coupon(
            id = this.id,
            couponType = this.couponType,
            code = this.code,
            discountRate = this.discountRate,
            description = this.description,
            startDate = this.startDate,
            endDate = this.endDate,
            quantity = this.quantity,
            remainingQuantity = this.remainingQuantity - count,
            createdAt = this.createdAt,
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun hasRemainingQuantity(): Boolean = remainingQuantity > 0
    
    fun isValid(): Boolean {
        val now = LocalDateTime.now()
        return now.isAfter(startDate) && now.isBefore(endDate) && remainingQuantity > 0
    }
}
