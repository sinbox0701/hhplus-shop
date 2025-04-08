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
    fun createProductOption(
        productId: Long, 
        name: String, 
        availableQuantity: Int, 
        additionalPrice: Double
    ): ProductOption {
        // 상품이 존재하는지 확인
        productRepository.findById(productId) 
            ?: throw IllegalArgumentException("Product not found with id: $productId")
        
        val id = System.currentTimeMillis() // 임시 ID 생성 방식
        val productOption = ProductOption.create(id, productId, name, availableQuantity, additionalPrice)
        return productOptionRepository.save(productOption)
    }

    fun getProductOption(id: Long): ProductOption {
        return productOptionRepository.findById(id) 
            ?: throw IllegalArgumentException("Product option not found with id: $id")
    }

    fun getProductOptionByProductIdAndId(productId: Long, id: Long): ProductOption {
        return productOptionRepository.findByProductIdAndId(productId, id)
            ?: throw IllegalArgumentException("Product option not found with id: $id")
    }

    fun getProductOptionsByProductId(productId: Long): List<ProductOption> {
        // 상품이 존재하는지 확인
        productRepository.findById(productId) 
            ?: throw IllegalArgumentException("Product not found with id: $productId")
        
        return productOptionRepository.findByProductId(productId)
    }

    fun updateProductOption(
        id: Long,
        name: String? = null,
        additionalPrice: Double? = null
    ): ProductOption {
        val productOption = getProductOption(id)
        val updatedOption = productOption.update(name, additionalPrice)
        return productOptionRepository.update(updatedOption)
    }

    fun addQuantity(id: Long, quantity: Int): ProductOption {
        val productOption = getProductOption(id)
        val updatedOption = productOption.add(quantity)
        return productOptionRepository.update(updatedOption)
    }

    fun subtractQuantity(id: Long, quantity: Int): ProductOption {
        val productOption = getProductOption(id)
        val updatedOption = productOption.subtract(quantity)
        return productOptionRepository.update(updatedOption)
    }

    fun deleteProductOption(id: Long) {
        getProductOption(id) // 옵션이 존재하는지 확인
        productOptionRepository.delete(id)
    }
} 