package kr.hhplus.be.server.domain.product.model

import java.time.LocalDateTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "product_options")
data class ProductOption private constructor(
    @Id
    val id: Long,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    val product: Product,
    
    @Column(nullable = false, length = MAX_NAME_LENGTH)
    var name: String,
    
    @Column(nullable = false)
    var availableQuantity: Int,
    
    @Column(nullable = false)
    var additionalPrice: Double,
    
    @Column(nullable = false)
    var createdAt: LocalDateTime,
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime
) {
    companion object {
        private const val MIN_NAME_LENGTH = 1
        private const val MAX_NAME_LENGTH = 10

        private const val MIN_AVAILABLE_QUANTITY = 0
        private const val MAX_AVAILABLE_QUANTITY = 1000

        fun create(id: Long, product: Product, name: String, availableQuantity: Int, additionalPrice: Double): ProductOption {
            require(name.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
                "Name must be between $MIN_NAME_LENGTH and $MAX_NAME_LENGTH characters"
            }
            require(availableQuantity in MIN_AVAILABLE_QUANTITY..MAX_AVAILABLE_QUANTITY) {
                "Available quantity must be between $MIN_AVAILABLE_QUANTITY and $MAX_AVAILABLE_QUANTITY"
            }
            return ProductOption(id, product, name, availableQuantity, additionalPrice, LocalDateTime.now(), LocalDateTime.now())
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
