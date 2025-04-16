package kr.hhplus.be.server.domain.product.service

import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.repository.ProductOptionRepository
import kr.hhplus.be.server.domain.product.repository.ProductRepository
import org.springframework.stereotype.Service

@Service
class ProductOptionService(
    private val productOptionRepository: ProductOptionRepository,
    private val productRepository: ProductRepository
) {
    fun create(command: ProductOptionCommand.CreateProductOptionCommand): ProductOption {
        // 상품이 존재하는지 확인
        val product = productRepository.findById(command.productId) 
            ?: throw IllegalArgumentException("Product not found with id: ${command.productId}")
        
        val productOption = ProductOption.create(product, command.name, command.availableQuantity, command.additionalPrice)
        return productOptionRepository.save(productOption)
    }

    fun createAll(
        commands: List<ProductOptionCommand.CreateProductOptionCommand>
    ): List<ProductOption> {
        // 상품이 존재하는지 확인
        val product = productRepository.findById(commands[0].productId)
            ?: throw IllegalArgumentException("Product not found with id: ${commands[0].productId}")
        
        return commands.map { command ->
            val productOption = ProductOption.create(
                product, 
                command.name, 
                command.availableQuantity, 
                command.additionalPrice
            )
            productOptionRepository.save(productOption)
        }
    }

    fun get(id: Long): ProductOption {
        return productOptionRepository.findById(id) 
            ?: throw IllegalArgumentException("Product option not found with id: $id")
    }

    fun getByProductIdAndId(productId: Long, id: Long): ProductOption {
        return productOptionRepository.findByProductIdAndId(productId, id)
            ?: throw IllegalArgumentException("Product option not found with id: $id")
    }

    fun getAllByProductId(productId: Long): List<ProductOption> {
        // 상품이 존재하는지 확인
        productRepository.findById(productId) 
            ?: throw IllegalArgumentException("Product not found with id: $productId")
        
        return productOptionRepository.findByProductId(productId)
    }

    fun update(command: ProductOptionCommand.UpdateProductOptionCommand): ProductOption {
        val productOption = get(command.id)
        val updatedOption = productOption.update(command.name, command.additionalPrice)
        return productOptionRepository.update(updatedOption)
    }

    fun updateAll(commands: List<ProductOptionCommand.UpdateProductOptionCommand>): List<ProductOption> {
        return commands.map { command ->
            val productOption = get(command.id)
            val updatedOption = productOption.update(command.name, command.additionalPrice)
            productOptionRepository.update(updatedOption)
        }
    }

    fun updateQuantity(command: ProductOptionCommand.UpdateQuantityCommand): ProductOption {
        val productOption = get(command.id)
        val updatedOption = productOption.add(command.quantity)
        return productOptionRepository.update(updatedOption)
    }

    fun subtractQuantity(command: ProductOptionCommand.UpdateQuantityCommand): ProductOption {
        val productOption = get(command.id)
        val updatedOption = productOption.subtract(command.quantity)
        return productOptionRepository.update(updatedOption)
    }

    fun delete(id: Long) {
        get(id) // 옵션이 존재하는지 확인
        productOptionRepository.delete(id)
    }
    
    fun deleteAll(productId: Long) {
        // 상품이 존재하는지 확인
        productRepository.findById(productId)
            ?: throw IllegalArgumentException("Product not found with id: $productId")
            
        val options = productOptionRepository.findByProductId(productId)
        options.forEach { option ->
            productOptionRepository.delete(option.id!!)
        }
    }
}