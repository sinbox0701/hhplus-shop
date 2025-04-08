package kr.hhplus.be.server.domain.product

import java.time.LocalDateTime

data class ProductOptionInventory private constructor(
    val id: Long,
    val optionId: Long,
    var availableQuantity: Int,
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
) {
    companion object {
        private const val MIN_AVAILABLE_QUANTITY = 0
        private const val MAX_AVAILABLE_QUANTITY = 1000

        fun create(id: Long, optionId: Long, availableQuantity: Int): ProductOptionInventory {
            require(availableQuantity in MIN_AVAILABLE_QUANTITY..MAX_AVAILABLE_QUANTITY) {
                "Available quantity must be between $MIN_AVAILABLE_QUANTITY and $MAX_AVAILABLE_QUANTITY"
            }
            return ProductOptionInventory(id, optionId, availableQuantity, LocalDateTime.now(), LocalDateTime.now())
        }
    }

    fun add(availableQuantity: Int): ProductOptionInventory {
        require(availableQuantity in MIN_AVAILABLE_QUANTITY..MAX_AVAILABLE_QUANTITY) {
            "Available quantity must be between $MIN_AVAILABLE_QUANTITY and $MAX_AVAILABLE_QUANTITY"
        }
        this.availableQuantity += availableQuantity
        this.updatedAt = LocalDateTime.now()
        return this
    }

    fun subtract(availableQuantity: Int): ProductOptionInventory {
        require(availableQuantity in MIN_AVAILABLE_QUANTITY..MAX_AVAILABLE_QUANTITY) {
            "Available quantity must be between $MIN_AVAILABLE_QUANTITY and $MAX_AVAILABLE_QUANTITY"
        }
        this.availableQuantity -= availableQuantity
        this.updatedAt = LocalDateTime.now()
        return this
    }
}