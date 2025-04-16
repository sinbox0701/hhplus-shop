package kr.hhplus.be.server.domain.product.model

import java.time.LocalDateTime

data class ProductOption private constructor(
    val id: Long? = null, // 데이터베이스가 자동 생성하므로 null 허용
    val productId: Long,
    var name: String,
    var availableQuantity: Int,
    var additionalPrice: Double,
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
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
        name?.let {
            require(it.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
                "Name must be between $MIN_NAME_LENGTH and $MAX_NAME_LENGTH characters"
            }
            this.name = it
            this.updatedAt = LocalDateTime.now()
        }
        additionalPrice?.let {
            this.additionalPrice = it
            this.updatedAt = LocalDateTime.now()
        }
        return this
    }

    fun add(quantity: Int): ProductOption {
        require(quantity >= 0) {
            "Available quantity must be between $MIN_AVAILABLE_QUANTITY and $MAX_AVAILABLE_QUANTITY"
        }
        
        val newQuantity = this.availableQuantity + quantity
        require(newQuantity in MIN_AVAILABLE_QUANTITY..MAX_AVAILABLE_QUANTITY) {
            "Available quantity must be between $MIN_AVAILABLE_QUANTITY and $MAX_AVAILABLE_QUANTITY"
        }
        
        this.availableQuantity = newQuantity
        this.updatedAt = LocalDateTime.now()
        return this
    }

    fun subtract(quantity: Int): ProductOption {
        require(quantity >= 0 && quantity <= MAX_AVAILABLE_QUANTITY) {
            "Available quantity must be between $MIN_AVAILABLE_QUANTITY and $MAX_AVAILABLE_QUANTITY"
        }
        
        if (this.availableQuantity < quantity) {
            throw IllegalStateException("Not enough stock available")
        }
        
        this.availableQuantity -= quantity
        this.updatedAt = LocalDateTime.now()
        return this
    }
}
