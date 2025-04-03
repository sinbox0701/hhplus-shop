package kr.hhplus.be.server.domain.product

import java.math.BigDecimal
import java.time.LocalDateTime

data class ProductOptionInventory private constructor(
    val inventoryId: Int,
    val optionId: Int,
    var availableQuantity: Int,
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
) {
    companion object {
        private const val MIN_AVAILABLE_QUANTITY = 0
        private const val MAX_AVAILABLE_QUANTITY = 1000

        fun create(inventoryId: Int, optionId: Int, availableQuantity: Int): ProductOptionInventory {
            require(availableQuantity in MIN_AVAILABLE_QUANTITY..MAX_AVAILABLE_QUANTITY) {
                "Available quantity must be between $MIN_AVAILABLE_QUANTITY and $MAX_AVAILABLE_QUANTITY"
            }
            return ProductOptionInventory(inventoryId, optionId, availableQuantity, LocalDateTime.now(), LocalDateTime.now())
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