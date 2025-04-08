package kr.hhplus.be.server.domain.product

import java.time.LocalDateTime

data class ProductOption private constructor(
    val id: Long,
    val productId: Long,
    var name: String,
    var additionalPrice: Double,
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
) {
    companion object {
        private const val MIN_NAME_LENGTH = 1
        private const val MAX_NAME_LENGTH = 10

        fun create(id: Long, productId: Long, name: String, additionalPrice: Double, productPrice: Double): ProductOption {
            require(name.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
                "Name must be between $MIN_NAME_LENGTH and $MAX_NAME_LENGTH characters"
            }
            require(additionalPrice <= productPrice) {
                "Additional price must not exceed product price"
            }
            return ProductOption(id, productId, name, additionalPrice, LocalDateTime.now(), LocalDateTime.now())
        }
    }

    fun update(name: String? = null, additionalPrice: Double? = null, productPrice: Double): ProductOption {
        name?.let {
            require(it.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
                "Name must be between $MIN_NAME_LENGTH and $MAX_NAME_LENGTH characters"
            }
            this.name = it
            this.updatedAt = LocalDateTime.now()
        }
        additionalPrice?.let {
            require(it <= productPrice) {
                "Additional price must not exceed product price"
            }
            this.additionalPrice = it
            this.updatedAt = LocalDateTime.now()
        }
        return this
    }
}
