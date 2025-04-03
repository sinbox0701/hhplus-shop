package kr.hhplus.be.server.domain.product

import java.math.BigDecimal
import java.time.LocalDateTime

data class Product private constructor(
    val productId: Int,
    var name: String,
    var description: String,
    var price: BigDecimal,
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
){
    companion object {
        private val MIN_PRICE = BigDecimal("1")
        private val MAX_PRICE = BigDecimal("1000000")

        private val MIN_NAME_LENGTH = 3
        private val MAX_NAME_LENGTH = 20

        fun create(productId: Int, name: String, description: String, price: BigDecimal): Product {
            require(price >= MIN_PRICE && price <= MAX_PRICE) {
                "Initial amount must be between $MIN_PRICE and $MAX_PRICE"
            }
            return Product(productId, name, description, price, LocalDateTime.now(), LocalDateTime.now())
        }
    }

    fun update(name: String? = null, description: String? = null, price: BigDecimal? = null): Product {
        name?.let {
            require(it.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) { "Name must be between 3 and 20 characters" }
            this.name = it
            this.updatedAt = LocalDateTime.now()
        }
        description?.let {
            this.description = it
            this.updatedAt = LocalDateTime.now()
        }
        price?.let {
            require(it >= MIN_PRICE && it <= MAX_PRICE) { "Price must be between $MIN_PRICE and $MAX_PRICE" }
            this.price = it
            this.updatedAt = LocalDateTime.now()
        }
        return this
    }
}
