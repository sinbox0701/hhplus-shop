package kr.hhplus.be.server.domain.product.service

import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.repository.ProductOptionRepository
import kr.hhplus.be.server.domain.product.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
@Service
class ProductOptionService(
    private val productOptionRepository: ProductOptionRepository,
) {
    fun create(command: ProductOptionCommand.CreateProductOptionCommand): ProductOption {
        val productOption = ProductOption.create(command.productId, command.name, command.availableQuantity, command.additionalPrice)
        return productOptionRepository.save(productOption)
    }

    fun createAll(
        commands: List<ProductOptionCommand.CreateProductOptionCommand>
    ): List<ProductOption> {
        return commands.map { command ->
            val productOption = ProductOption.create(
                command.productId, 
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
        return productOptionRepository.findByProductId(productId)
    }

    @Transactional(readOnly = true)
    fun getAllByProductIds(productIds: List<Long>): List<ProductOption> {
        return productOptionRepository.findAllByProductIds(productIds)
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
        val options = productOptionRepository.findByProductId(productId)
        options.forEach { option ->
            productOptionRepository.delete(option.id!!)
        }
    }

    /**
     * 비관적 락을 사용하여 재고 차감
     */
    @Transactional
    fun subtractQuantityWithPessimisticLock(id: Long, quantity: Int): ProductOption {
        return productOptionRepository.updateWithPessimisticLock(id) { productOption ->
            productOption.subtract(quantity)
        }
    }
    
    /**
     * 비관적 락을 사용하여 재고 증가
     */
    @Transactional
    fun addQuantityWithPessimisticLock(id: Long, quantity: Int): ProductOption {
        return productOptionRepository.updateWithPessimisticLock(id) { productOption ->
            productOption.add(quantity)
        }
    }
    
    /**
     * 비관적 락을 사용하여 재고 복원
     */
    @Transactional
    fun restoreQuantityWithPessimisticLock(id: Long, quantity: Int): ProductOption {
        return productOptionRepository.updateWithPessimisticLock(id) { productOption ->
            productOption.add(quantity)
        }
    }
}