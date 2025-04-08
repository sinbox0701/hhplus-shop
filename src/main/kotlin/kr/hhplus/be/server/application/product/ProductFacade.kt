package kr.hhplus.be.server.domain.product.service

import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductFacade(
    private val productService: ProductService,
    private val productOptionService: ProductOptionService
) {
    /**
     * 상품과 옵션을 함께 생성하는 메서드
     */
    @Transactional
    fun createProductWithOptions(
        name: String,
        description: String,
        price: Double,
        options: List<ProductOptionRequest>
    ): ProductWithOptions {
        // 1. 상품 생성
        val product = productService.createProduct(name, description, price)
        
        // 2. 옵션 생성
        val productOptions = if (options.isNotEmpty()) {
            productOptionService.createAll(product.id, options)
        } else {
            emptyList()
        }
        
        return ProductWithOptions(product, productOptions)
    }
    
    /**
     * 상품 정보와 모든 옵션 정보를 함께 조회하는 메서드
     */
    @Transactional(readOnly = true)
    fun getProductWithOptions(productId: Long): ProductWithOptions {
        // 1. 상품 조회
        val product = productService.getProduct(productId)
        
        // 2. 상품의 모든 옵션 조회
        val options = productOptionService.getProductOptionsByProductId(productId)
        
        return ProductWithOptions(product, options)
    }

     /**
     * 상품과 옵션을 함께 업데이트하는 메서드
     */
    @Transactional
    fun updateProductWithOptions(
        productId: Long,
        name: String? = null,
        description: String? = null,
        price: Double? = null,
        optionsToUpdate: Map<Long, ProductOptionUpdateRequest> = emptyMap(),
        optionsToAdd: List<ProductOptionRequest> = emptyList(),
        optionsToRemove: List<Long> = emptyList()
    ): ProductWithOptions {
        // 1. 상품 업데이트
        val updatedProduct = productService.updateProduct(productId, name, description, price)
        
        // 2. 기존 옵션 업데이트
        optionsToUpdate.forEach { (optionId, updateRequest) ->
            val option = productOptionService.getProductOption(optionId)
            if (option.productId != productId) {
                throw IllegalArgumentException("Option with id $optionId does not belong to product with id $productId")
            }
            productOptionService.updateProductOption(optionId, updateRequest.name, updateRequest.additionalPrice)
        }
        
        // 3. 새 옵션 추가
        if (optionsToAdd.isNotEmpty()) {
            productOptionService.createAll(productId, optionsToAdd)
        }
        
        // 4. 옵션 삭제
        if (optionsToRemove.isNotEmpty()) {
            removeOptionsFromProduct(productId, optionsToRemove)
        }
        
        // 5. 최종 결과 조회
        return getProductWithOptions(productId)
    }

    /**
     * 상품에 옵션을 추가하는 메서드
     */
    @Transactional
    fun addOptionsToProduct(
        productId: Long,
        options: List<ProductOptionRequest>
    ): List<ProductOption> {
        // 1. 상품 검증
        productService.getProduct(productId)
        
        // 2. 옵션 추가
        return productOptionService.createAll(productId, options)
    }
    
    /**
     * 상품의 특정 옵션을 삭제하는 메서드
     */
    @Transactional
    fun removeOptionsFromProduct(productId: Long, optionIds: List<Long>) {
        // 1. 상품 검증
        productService.getProduct(productId)
        
        // 2. 각 옵션이 해당 상품의 것인지 확인 후 삭제
        optionIds.forEach { optionId ->
            val option = productOptionService.getProductOption(optionId)
            if (option.productId != productId) {
                throw IllegalArgumentException("Option with id $optionId does not belong to product with id $productId")
            }
            productOptionService.deleteProductOption(optionId)
        }
    }

    /**
     * 상품과 해당 상품의 모든 옵션을 함께 삭제하는 메서드
     */
    @Transactional
    fun deleteProductWithOptions(productId: Long) {
        // 1. 상품 검증
        productService.getProduct(productId)
        
        // 2. 모든 옵션 삭제
        productOptionService.deleteAll(productId)
        
        // 3. 상품 삭제
        productService.deleteProduct(productId)
    }
}

/**
 * 상품과 옵션 정보를 함께 담는 데이터 클래스
 */
data class ProductWithOptions(
    val product: Product,
    val options: List<ProductOption>
) 

data class ProductOptionUpdateRequest(
    val name: String? = null,
    val additionalPrice: Double? = null
) 