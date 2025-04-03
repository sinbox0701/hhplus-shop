package kr.hhplus.be.server.domain.order

import java.time.LocalDateTime

data class OrderItem private constructor(
    val id: Long,
    val orderId: Long,
    val productId: Long,
    val productOptionId: Long?,
    var quantity: Int,
    var price: Double,
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
){
    companion object {
        const val MIN_QUANTITY = 1
        const val MAX_QUANTITY = 100
        val MIN_PRICE = 100.0

        fun create(id: Long, orderId: Long, productId: Long, quantity: Int, price: Double, productOptionId: Long? = null): OrderItem {
            require(quantity in MIN_QUANTITY..MAX_QUANTITY) { "수량은 $MIN_QUANTITY 부터 $MAX_QUANTITY 사이여야 합니다." }
            require(price >= MIN_PRICE) { "가격은 $MIN_PRICE 이상이어야 합니다." }
            return OrderItem(id, orderId, productId, productOptionId, quantity, price, LocalDateTime.now(), LocalDateTime.now())
        }
    }

    fun update(quantity: Int, price: Double) {
        require(quantity in MIN_QUANTITY..MAX_QUANTITY) { "수량은 $MIN_QUANTITY 부터 $MAX_QUANTITY 사이여야 합니다." }
        require(price >= MIN_PRICE) { "가격은 $MIN_PRICE 이상이어야 합니다." }
        
        this.quantity = quantity
        this.price = price
        this.updatedAt = LocalDateTime.now()
    }
}

