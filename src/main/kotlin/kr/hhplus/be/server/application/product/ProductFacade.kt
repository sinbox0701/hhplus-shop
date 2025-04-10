package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.application.product.ProductResult
import kr.hhplus.be.server.application.product.ProductCriteria
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.service.ProductCommand
import kr.hhplus.be.server.domain.product.service.ProductOptionCommand
import kr.hhplus.be.server.domain.product.service.ProductOptionService
import kr.hhplus.be.server.domain.product.service.ProductService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ProductFacade(
    private val productService: ProductService,
    private val productOptionService: ProductOptionService
) {
    /**
     * 모든 상품 목록과 옵션을 조회하는 메서드
     */
    @Transactional(readOnly = true)
    fun getAllProductsWithOptions(): List<ProductResult.ProductWithOptions> {
        return productService.getAll().map { product ->
            ProductResult.ProductWithOptions(product, productOptionService.getAllByProductId(product.id!!))
        }
    }
    
    /**
     * 상품과 옵션을 함께 생성하는 메서드
     */
    @Transactional
    fun createProductWithOptions(
        criteria: ProductCriteria.CreateProductCriteria
    ): ProductResult.ProductWithOptions {
        // 1. 상품 생성
        val product = productService.create(criteria.toProductCommand())
        
        // 2. 옵션 생성
        val productOptions = productOptionService.createAll(criteria.toOptionCommands(product.id!!))
        
        return ProductResult.ProductWithOptions(product, productOptions)
    }
    
    /**
     * 상품 정보와 모든 옵션 정보를 함께 조회하는 메서드
     */
    @Transactional(readOnly = true)
    fun getProductWithOptions(productId: Long): ProductResult.ProductWithOptions {
        // 1. 상품 조회
        val product = productService.get(productId)
        
        // 2. 상품의 모든 옵션 조회
        val options = productOptionService.getAllByProductId(productId)
        
        return ProductResult.ProductWithOptions(product, options)
    }

    /**
     * 최근 3일 동안 가장 많이 팔린 상품 5개를 조회하는 메서드
     */
    @Transactional(readOnly = true)
    fun getTopSellingProducts(): List<Product> {
        // 현재 시간 기준 최근 3일 계산
        val now = LocalDateTime.now()
        val threeDaysAgo = now.minusDays(3)
        
        // 실제로는 OrderRepository에서 판매량 기준으로 데이터를 조회해야 함
        // 여기서는 임시로 전체 상품 중 최대 5개를 반환
        val allProducts = productService.getAll()
        return allProducts.take(5)
    }

     /**
     * 상품과 옵션을 함께 업데이트하는 메서드
     */
    @Transactional
    fun updateProductWithOptions(criteria: ProductCriteria.UpdateProductCriteria): ProductResult.ProductWithOptions {
        // 1. 상품 업데이트
        val updatedProduct = productService.update(criteria.toCommand())
        
        // 2. 기존 옵션 업데이트
        if (criteria.optionsToUpdate?.isNotEmpty() == true) {
            // 모든 옵션이 현재 상품에 속하는지 확인
            criteria.optionsToUpdate.forEach { option ->
                val productOption = productOptionService.get(option.id)
                if (productOption.product.id != updatedProduct.id) {
                    throw IllegalArgumentException("Option with id ${option.id} does not belong to product with id ${updatedProduct.id}")
                }
            }
            // 일괄 업데이트 수행
            productOptionService.updateAll(criteria.toOptionUpdateCommands())
        }
        
        // 3. 새 옵션 추가
        if (criteria.optionsToAdd?.isNotEmpty() == true) {
            productOptionService.createAll(criteria.toOptionCreateCommands(updatedProduct.id!!))
        }
        
        // 4. 옵션 삭제
        if (criteria.optionsToRemove?.isNotEmpty() == true) {
            // 모든 옵션이 현재 상품에 속하는지 확인
            val optionsToDelete = criteria.optionsToRemove.map { optionId ->
                val option = productOptionService.get(optionId)
                if (option.product.id != updatedProduct.id) {
                    throw IllegalArgumentException("Option with id $optionId does not belong to product with id ${updatedProduct.id}")
                }
                optionId
            }
            
            // 각 옵션 삭제 (ProductOptionService에 특정 ID 목록을 삭제하는 메서드가 없으므로 반복 사용)
            optionsToDelete.forEach { optionId ->
                productOptionService.delete(optionId)
            }
        }
        
        // 5. 최종 결과 조회
        return getProductWithOptions(updatedProduct.id!!)
    }

    /**
     * 상품에 옵션을 추가하는 메서드
     */
    @Transactional
    fun addOptionsToProduct(criteria: ProductCriteria.UpdateProductCriteria): List<ProductOption> {
        // 1. 상품 검증
        val product = productService.get(criteria.id)
        
        // 2. 옵션 추가
        return productOptionService.createAll(criteria.toOptionCreateCommands(product.id!!))
    }
    
    /**
     * 상품의 특정 옵션을 삭제하는 메서드
     */
    @Transactional
    fun removeOptionsFromProduct(criteria: ProductCriteria.UpdateProductCriteria) {
        // 1. 상품 검증
        val product = productService.get(criteria.id)
        
        // 2. 각 옵션이 해당 상품의 것인지 확인 후 삭제
        criteria.optionsToRemove?.forEach { optionId ->
            val option = productOptionService.get(optionId)
            if (option.product.id != product.id) {
                throw IllegalArgumentException("Option with id $optionId does not belong to product with id ${product.id}")
            }
            productOptionService.delete(optionId)
        }
    }

    /**
     * 상품과 해당 상품의 모든 옵션을 함께 삭제하는 메서드
     */
    @Transactional
    fun deleteProductWithOptions(productId: Long) {
        // 1. 상품 검증
        productService.get(productId)
        
        // 2. 모든 옵션 삭제
        productOptionService.deleteAll(productId)
        
        // 3. 상품 삭제
        productService.delete(productId)
    }
}
