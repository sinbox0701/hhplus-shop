package kr.hhplus.be.server.domain.product.model

import java.time.LocalDateTime

data class ProductOption private constructor(
    val id: Long? = null, // 데이터베이스가 자동 생성하므로 null 허용
    val productId: Long,
    val name: String,
    val availableQuantity: Int,
    val additionalPrice: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        private const val MIN_NAME_LENGTH = 1
        private const val MAX_NAME_LENGTH = 10

        private const val MIN_AVAILABLE_QUANTITY = 0
        private const val MAX_AVAILABLE_QUANTITY = 1000

        fun create(productId: Long, name: String, availableQuantity: Int, additionalPrice: Double): ProductOption {
            require(name.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
                "Name must be between $MIN_NAME_LENGTH and $MAX_NAME_LENGTH characters"
            }
            require(availableQuantity in MIN_AVAILABLE_QUANTITY..MAX_AVAILABLE_QUANTITY) {
                "Available quantity must be between $MIN_AVAILABLE_QUANTITY and $MAX_AVAILABLE_QUANTITY"
            }
            return ProductOption(productId=productId, name=name, availableQuantity=availableQuantity, additionalPrice=additionalPrice, createdAt=LocalDateTime.now(), updatedAt=LocalDateTime.now())
        }
    }

    fun update(name: String? = null, additionalPrice: Double? = null): ProductOption {
        // 변경할 필드가 없으면 현재 객체 그대로 반환
        if (name == null && additionalPrice == null) {
            return this
        }

        val updatedName = name?.also {
            require(it.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
                "Name must be between $MIN_NAME_LENGTH and $MAX_NAME_LENGTH characters"
            }
        } ?: this.name

        val updatedAdditionalPrice = additionalPrice ?: this.additionalPrice

        return ProductOption(
            id = this.id,
            productId = this.productId,
            name = updatedName,
            availableQuantity = this.availableQuantity,
            additionalPrice = updatedAdditionalPrice,
            createdAt = this.createdAt,
            updatedAt = LocalDateTime.now()
        )
    }

    fun add(quantity: Int): ProductOption {
        require(quantity >= 0) {
            "Available quantity must be between $MIN_AVAILABLE_QUANTITY and $MAX_AVAILABLE_QUANTITY"
        }
        
        val newQuantity = this.availableQuantity + quantity
        require(newQuantity in MIN_AVAILABLE_QUANTITY..MAX_AVAILABLE_QUANTITY) {
            "Available quantity must be between $MIN_AVAILABLE_QUANTITY and $MAX_AVAILABLE_QUANTITY"
        }
        
        return ProductOption(
            id = this.id,
            productId = this.productId,
            name = this.name,
            availableQuantity = newQuantity,
            additionalPrice = this.additionalPrice,
            createdAt = this.createdAt,
            updatedAt = LocalDateTime.now()
        )
    }

    fun subtract(quantity: Int): ProductOption {
        require(quantity >= 0 && quantity <= MAX_AVAILABLE_QUANTITY) {
            "Available quantity must be between $MIN_AVAILABLE_QUANTITY and $MAX_AVAILABLE_QUANTITY"
        }
        
        if (this.availableQuantity < quantity) {
            throw IllegalStateException("Not enough stock available")
        }
        
        return ProductOption(
            id = this.id,
            productId = this.productId,
            name = this.name,
            availableQuantity = this.availableQuantity - quantity,
            additionalPrice = this.additionalPrice,
            createdAt = this.createdAt,
            updatedAt = LocalDateTime.now()
        )
    }
}
