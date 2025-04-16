package kr.hhplus.be.server.domain.product.model

import java.time.LocalDateTime

data class Product private constructor(
    val id: Long? = null, // 데이터베이스가 자동 생성하므로 null 허용
    val name: String,
    val description: String,
    val price: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
){
    companion object {
        const val MIN_PRICE = 1.0
        const val MAX_PRICE = 1000000.0

        const val MIN_NAME_LENGTH = 3
        const val MAX_NAME_LENGTH = 20

        fun create(name: String, description: String, price: Double): Product {
            require(name.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
                "Name must be between $MIN_NAME_LENGTH and $MAX_NAME_LENGTH characters"
            }
            require(price >= MIN_PRICE && price <= MAX_PRICE) {
                "Initial amount must be between $MIN_PRICE and $MAX_PRICE"
            }
            return Product(name=name, description=description, price=price, createdAt=LocalDateTime.now(), updatedAt=LocalDateTime.now())
        }
    }
    fun update(name: String? = null, description: String? = null, price: Double? = null): Product {
        // 변경할 필드가 없으면 현재 객체 그대로 반환
        if (name == null && description == null && price == null) {
            return this
        }

        val updatedName = name?.also {
            require(it.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) { 
                "Name must be between $MIN_NAME_LENGTH and $MAX_NAME_LENGTH characters" 
            }
        } ?: this.name

        val updatedDescription = description ?: this.description

        val updatedPrice = price?.also {
            require(it >= MIN_PRICE && it <= MAX_PRICE) { 
                "Price must be between $MIN_PRICE and $MAX_PRICE" 
            }
        } ?: this.price

        return Product(
            id = this.id,
            name = updatedName,
            description = updatedDescription,
            price = updatedPrice,
            createdAt = this.createdAt,
            updatedAt = LocalDateTime.now()
        )
    }
}
