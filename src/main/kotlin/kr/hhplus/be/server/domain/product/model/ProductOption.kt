package kr.hhplus.be.server.domain.product.model

import java.time.LocalDateTime

data class ProductOption private constructor(
    val id: Long,
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

        fun create(id: Long, productId: Long, name: String, availableQuantity: Int, additionalPrice: Double): ProductOption {
            require(name.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
                "Name must be between $MIN_NAME_LENGTH and $MAX_NAME_LENGTH characters"
            }
            require(availableQuantity in MIN_AVAILABLE_QUANTITY..MAX_AVAILABLE_QUANTITY) {
                "Available quantity must be between $MIN_AVAILABLE_QUANTITY and $MAX_AVAILABLE_QUANTITY"
            }
            return ProductOption(id, productId, name, availableQuantity, additionalPrice, LocalDateTime.now(), LocalDateTime.now())
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

    fun add(availableQuantity: Int): ProductOption {
        require(availableQuantity in MIN_AVAILABLE_QUANTITY..MAX_AVAILABLE_QUANTITY) {
            "Available quantity must be between $MIN_AVAILABLE_QUANTITY and $MAX_AVAILABLE_QUANTITY"
        }
        this.availableQuantity += availableQuantity
        this.updatedAt = LocalDateTime.now()
        return this
    }

    fun subtract(availableQuantity: Int): ProductOption {
        require(availableQuantity in MIN_AVAILABLE_QUANTITY..MAX_AVAILABLE_QUANTITY) {
            "Available quantity must be between $MIN_AVAILABLE_QUANTITY and $MAX_AVAILABLE_QUANTITY"
        }
        this.availableQuantity -= availableQuantity
        this.updatedAt = LocalDateTime.now()
        return this
    }
}
