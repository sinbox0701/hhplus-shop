package kr.hhplus.be.server.domain.product

import java.math.BigDecimal

data class ProductOption private constructor(
    val optionId: Int,
    val productId: Int,
    var name: String,
    var additionalPrice: BigDecimal
) {
    companion object {
        private const val MIN_NAME_LENGTH = 1
        private const val MAX_NAME_LENGTH = 10

        fun create(optionId: Int, productId: Int, name: String, additionalPrice: BigDecimal, productPrice: BigDecimal): ProductOption {
            require(name.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
                "Name must be between $MIN_NAME_LENGTH and $MAX_NAME_LENGTH characters"
            }
            require(additionalPrice <= productPrice) {
                "Additional price must not exceed product price"
            }
            return ProductOption(optionId, productId, name, additionalPrice)
        }
    }

    fun update(name: String? = null, additionalPrice: BigDecimal? = null, productPrice: BigDecimal): ProductOption {
        name?.let {
            require(it.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
                "Name must be between $MIN_NAME_LENGTH and $MAX_NAME_LENGTH characters"
            }
            this.name = it
        }
        additionalPrice?.let {
            require(it <= productPrice) {
                "Additional price must not exceed product price"
            }
            this.additionalPrice = it
        }
        return this
    }
}
