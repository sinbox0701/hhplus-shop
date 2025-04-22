package kr.hhplus.be.server.domain.order.model

import java.time.LocalDateTime

data class OrderItem private constructor(
    val id: Long? = null, // 데이터베이스가 자동 생성하므로 null 허용
    val orderId: Long,
    val productId: Long,
    val productOptionId: Long,
    val userCouponId: Long?,
    val quantity: Int,
    val price: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
){
    companion object {
        const val MIN_QUANTITY = 1
        const val MAX_QUANTITY = 100
        val MIN_PRICE = 100.0

        fun create(orderId: Long, productId: Long, productOptionId: Long, userCouponId: Long?, quantity: Int, price: Double): OrderItem {
            require(quantity in MIN_QUANTITY..MAX_QUANTITY) { "수량은 $MIN_QUANTITY 부터 $MAX_QUANTITY 사이여야 합니다." }
            require(price >= MIN_PRICE) { "가격은 $MIN_PRICE 이상이어야 합니다." }
            return OrderItem(orderId=orderId, productId=productId, productOptionId=productOptionId, userCouponId=userCouponId, quantity=quantity, price=price, createdAt=LocalDateTime.now(), updatedAt=LocalDateTime.now())
        }
    }

    fun update(quantity: Int?, productPrice: Double?): OrderItem {
        // 변경할 필드가 없으면 현재 객체 그대로 반환
        if (quantity == null && productPrice == null) {
            return this
        }

        val updatedQuantity = quantity?.also {
            require(it in MIN_QUANTITY..MAX_QUANTITY) { "수량은 $MIN_QUANTITY 부터 $MAX_QUANTITY 사이여야 합니다." }
        } ?: this.quantity

        val updatedPrice = if (productPrice != null) {
            val newPrice = updatedQuantity * productPrice
            require(newPrice >= MIN_PRICE) { "가격은 $MIN_PRICE 이상이어야 합니다." }
            newPrice
        } else {
            this.price
        }

        return OrderItem(
            id = this.id,
            orderId = this.orderId,
            productId = this.productId,
            productOptionId = this.productOptionId,
            userCouponId = this.userCouponId,
            quantity = updatedQuantity,
            price = updatedPrice,
            createdAt = this.createdAt,
            updatedAt = LocalDateTime.now()
        )
    }

    fun updatePrice(discountRate: Double?): OrderItem {
        val newPrice = discountRate?.let { price * (1 - it / 100) } ?: price
        require(newPrice >= MIN_PRICE) { "가격은 $MIN_PRICE 이상이어야 합니다." }
        
        return OrderItem(
            id = this.id,
            orderId = this.orderId,
            productId = this.productId,
            productOptionId = this.productOptionId,
            userCouponId = this.userCouponId,
            quantity = this.quantity,
            price = newPrice,
            createdAt = this.createdAt,
            updatedAt = LocalDateTime.now()
        )
    }
}

