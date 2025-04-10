package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.service.ProductCommand
import kr.hhplus.be.server.domain.product.service.ProductOptionCommand

class ProductCriteria {
    /**
     * 상품 생성 Criteria
     */
    data class CreateProductOptionCriteria(
        val name: String,
        val price: Double,
        val availableQuantity: Int
    )
    
    data class CreateProductCriteria(
        val name: String,
        val description: String,
        val price: Double,
        val options: List<CreateProductOptionCriteria>
    ) {
        fun toProductCommand(): ProductCommand.CreateProductCommand {
            return ProductCommand.CreateProductCommand(
                name = name,
                description = description,
                price = price
            )
        }

        fun toOptionCommands(productId: Long): List<ProductOptionCommand.CreateProductOptionCommand> {
            return options.map { option ->
                ProductOptionCommand.CreateProductOptionCommand(
                    productId = productId,
                    name = option.name,
                    availableQuantity = option.availableQuantity,
                    additionalPrice = option.price
                )
            }
        }
    }

    /**
     * 상품 업데이트 Criteria
     */
    data class UpdateProductOptionCriteria(
        val id: Long,
        val name: String?,
        val availableQuantity: Int?,
        val additionalPrice: Double?
    )

    data class UpdateProductCriteria(
        val id: Long,
        val name: String? = null,
        val description: String? = null,
        val price: Double? = null,
        val optionsToUpdate: List<UpdateProductOptionCriteria>? = null,
        val optionsToAdd: List<CreateProductOptionCriteria>? = null,
        val optionsToRemove: List<Long>? = null
    ) {
        fun toCommand(): ProductCommand.UpdateProductCommand {
            return ProductCommand.UpdateProductCommand(
                id = id,
                name = name,
                description = description,
                price = price
            )
        }

        fun toOptionCreateCommands(productId: Long): List<ProductOptionCommand.CreateProductOptionCommand> {
            return optionsToAdd?.map { option ->
                ProductOptionCommand.CreateProductOptionCommand(
                    productId = productId,
                    name = option.name,
                    availableQuantity = option.availableQuantity,
                    additionalPrice = option.price
                )
            } ?: emptyList()
        }
        
        fun toOptionUpdateCommands(): List<ProductOptionCommand.UpdateProductOptionCommand> {
            return optionsToUpdate?.map { option ->
                ProductOptionCommand.UpdateProductOptionCommand(
                    id = option.id,
                    name = option.name,
                    availableQuantity = option.availableQuantity,
                    additionalPrice = option.additionalPrice
                )
            } ?: emptyList()
        }
        
    }
}