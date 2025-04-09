package kr.hhplus.be.server.domain.order.model

import java.time.LocalDateTime

data class OrderItem private constructor(
    val id: Long,
    val orderId: Long,
    val productId: Long,
    val productOptionId: Long,
    val accountCouponId: Long?,
    var quantity: Int,
    var price: Double,
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
){
    companion object {
        const val MIN_QUANTITY = 1
        const val MAX_QUANTITY = 100
        val MIN_PRICE = 100.0

        fun create(id: Long, orderId: Long, productId: Long, productOptionId: Long, quantity: Int, productPrice: Double, accountCouponId: Long?, discountRate: Double?): OrderItem {
            require(quantity in MIN_QUANTITY..MAX_QUANTITY) { "수량은 $MIN_QUANTITY 부터 $MAX_QUANTITY 사이여야 합니다." }
            val basePrice = productPrice * quantity
            val finalPrice = discountRate?.let { basePrice * (1 - it / 100) } ?: basePrice
            require(finalPrice >= MIN_PRICE) { "가격은 $MIN_PRICE 이상이어야 합니다." }
            return OrderItem(id, orderId, productId, productOptionId, accountCouponId, quantity, finalPrice, LocalDateTime.now(), LocalDateTime.now())
        }
    }

    fun update(quantity: Int?, productPrice: Double?): OrderItem {
        quantity?.let {
            require(it in MIN_QUANTITY..MAX_QUANTITY) { "수량은 $MIN_QUANTITY 부터 $MAX_QUANTITY 사이여야 합니다." }
            this.quantity = it
            this.updatedAt = LocalDateTime.now()
        }
        productPrice?.let {
            this.price = this.quantity * it
            require(this.price >= MIN_PRICE) { "가격은 $MIN_PRICE 이상이어야 합니다." }
            this.updatedAt = LocalDateTime.now()
        }
        return this
    }

    fun updatePrice(discountRate: Double?): OrderItem {
        val newPrice = discountRate?.let { price * (1 - it / 100) } ?: price
        require(newPrice >= MIN_PRICE) { "가격은 $MIN_PRICE 이상이어야 합니다." }
        price = newPrice
        updatedAt = LocalDateTime.now()
        return this
    }
}

