package kr.hhplus.be.server.service.product

import kr.hhplus.be.server.domain.product.ProductOption
import kr.hhplus.be.server.repository.product.ProductOptionRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ProductOptionService(
    private val productOptionRepository: ProductOptionRepository,
    private val productService: ProductService
) {
    fun create(optionId: Int, productId: Int, name: String, additionalPrice: BigDecimal): ProductOption {
        // Get product to validate additionalPrice
        val product = productService.getById(productId)
        
        // Create domain object
        val productOption = ProductOption.create(
            optionId = optionId,
            productId = productId,
            name = name,
            additionalPrice = additionalPrice,
            productPrice = product.price
        )
        
        // Save to repository
        return productOptionRepository.save(productOption)
    }
    
    fun getById(optionId: Int): ProductOption {
        return productOptionRepository.findById(optionId) ?: 
            throw IllegalArgumentException("Product option not found for optionId: $optionId")
    }
    
    fun getByProductId(productId: Int): List<ProductOption> {
        return productOptionRepository.findByProductId(productId)
    }
    
    fun update(optionId: Int, name: String?, additionalPrice: BigDecimal?): ProductOption {
        // Get existing option
        val productOption = getById(optionId)
        
        // Get product to validate additionalPrice
        val product = productService.getById(productOption.productId)
        
        // Update domain object
        productOption.update(
            name = name,
            additionalPrice = additionalPrice,
            productPrice = product.price
        )
        
        // Save updated option
        return productOptionRepository.update(productOption)
    }
    
    fun delete(optionId: Int) {
        // Check if option exists
        getById(optionId)
        
        // Delete from repository
        productOptionRepository.delete(optionId)
    }
} 