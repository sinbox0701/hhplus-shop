package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.order.service.OrderItemService
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.repository.ProductDailySalesRepository
import kr.hhplus.be.server.domain.product.service.ProductOptionService
import kr.hhplus.be.server.domain.product.service.ProductSalesService
import kr.hhplus.be.server.domain.product.service.ProductService
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import kr.hhplus.be.server.shared.lock.CompositeLock
import kr.hhplus.be.server.shared.lock.DistributedLock
import kr.hhplus.be.server.shared.lock.LockKeyConstants
import kr.hhplus.be.server.domain.product.service.ProductCommand

@Service
class ProductFacade(
    private val productService: ProductService,
    private val productOptionService: ProductOptionService,
    private val orderItemService: OrderItemService,
    private val productSalesService: ProductSalesService,
    private val cacheManager: CacheManager
) {
    /**
     * 모든 상품 목록과 옵션을 조회하는 메서드
     */
    @Cacheable(value = ["products"], key = "'all'")
    @Transactional(readOnly = true)
    fun getAllProductsWithOptions(): List<ProductResult.ProductWithOptions> {
        // 1. 모든 상품 조회
        val products = productService.getAll()
        
        // 2. 상품이 없으면 빈 리스트 반환
        if (products.isEmpty()) return emptyList()
        
        // 3. 모든 상품 ID 추출
        val productIds = products.mapNotNull { it.id }
        
        // 4. 한 번의 쿼리로 모든 상품의 옵션 조회
        val allOptions = productOptionService.getAllByProductIds(productIds)
        
        // 5. 옵션을 상품별로 그룹화 (메모리에서 처리)
        val optionsByProductId = allOptions.groupBy { it.productId }
        
        // 6. 결과 조합
        return products.map { product ->
            ProductResult.ProductWithOptions(
                product, 
                optionsByProductId[product.id] ?: emptyList()
            )
        }
    }
    
    /**
     * 상품과 옵션을 함께 생성하는 메서드
     */
    @CachePut(value = ["products"], key = "#result.product.id")
    @CacheEvict(value = ["products"], key = "'all'")
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
    @Cacheable(value = ["products"], key = "#productId")
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
     * - 집계 테이블을 활용하여 성능 최적화
     */
    @Cacheable(value = ["bestSellers"], key = "#days + '_' + #limit")
    @Transactional(readOnly = true)
    fun getTopSellingProducts(days: Int = 3, limit: Int = 5): List<Product> {
        // 현재 날짜 기준 조회 기간 계산
        val startDate = LocalDate.now().minusDays(days.toLong())
        
        // 집계 테이블에서 상위 상품 ID 조회
        val topSellingProductIds = productSalesService.getTopSellingProductIds(startDate, limit)
        
        // ID로 상품 정보 조회
        if (topSellingProductIds.isEmpty()) {
            // 판매 데이터가 없는 경우 일반 상품 목록 반환
            return productService.getAll().take(limit)
        }
        
        // 모든 상품 ID를 한 번에 조회
        val products = productService.getByIds(topSellingProductIds)
        
        // 상품 ID로 매핑하여 O(1) 조회 가능하게 함
        val productsMap = products.associateBy { it.id }
        
        // 판매량 순서대로 상품 정보 정렬하여 반환 (O(n) 시간 복잡도)
        return topSellingProductIds.mapNotNull { productsMap[it] }
    }

     /**
     * 상품과 옵션을 함께 업데이트하는 메서드
     */
    @CachePut(value = ["products"], key = "#criteria.id")
    @CacheEvict(value = ["products"], key = "'all'")
    @Transactional
    fun updateProductWithOptions(criteria: ProductCriteria.UpdateProductCriteria): ProductResult.ProductWithOptions {
        // 1. 상품 업데이트
        val updatedProduct = productService.update(criteria.toCommand())
        
        // 2. 기존 옵션 업데이트
        if (criteria.optionsToUpdate?.isNotEmpty() == true) {
            // 모든 옵션이 현재 상품에 속하는지 확인
            criteria.optionsToUpdate.forEach { option ->
                val productOption = productOptionService.get(option.id)
                if (productOption.productId != updatedProduct.id) {
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
                if (option.productId != updatedProduct.id) {
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
    @CacheEvict(value = ["products"], allEntries = true)
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
    @CacheEvict(value = ["products"], key = "#criteria.id")
    @Transactional
    fun removeOptionsFromProduct(criteria: ProductCriteria.UpdateProductCriteria) {
        // 1. 상품 검증
        val product = productService.get(criteria.id)
        
        // 2. 각 옵션이 해당 상품의 것인지 확인 후 삭제
        criteria.optionsToRemove?.forEach { optionId ->
            val option = productOptionService.get(optionId)
            if (option.productId != product.id) {
                throw IllegalArgumentException("Option with id $optionId does not belong to product with id ${product.id}")
            }
            productOptionService.delete(optionId)
        }
    }

    /**
     * 상품과 해당 상품의 모든 옵션을 함께 삭제하는 메서드
     */
    @Caching(evict = [
        CacheEvict(value = ["products"], key = "#productId"),
        CacheEvict(value = ["products"], key = "'all'"),
        CacheEvict(value = ["bestSellers"], allEntries = true)
    ])
    @Transactional
    fun deleteProductWithOptions(productId: Long) {
        // 1. 상품 검증
        productService.get(productId)
        
        // 2. 모든 옵션 삭제
        productOptionService.deleteAll(productId)
        
        // 3. 상품 삭제
        productService.delete(productId)
    }

    /**
     * 상품 재고를 증가시키는 메서드
     */
    @DistributedLock(
        domain = LockKeyConstants.PRODUCT_PREFIX,
        resourceType = LockKeyConstants.RESOURCE_STOCK,
        resourceIdExpression = "criteria.productId"
    )
    @CachePut(value = ["products"], key = "#criteria.productId")
    @CacheEvict(value = ["products"], key = "'all'")
    @Transactional
    fun increaseStock(criteria: ProductCriteria.UpdateStockCriteria): ProductResult.Single {
        val product = productService.increaseStock(criteria.toCommand())
        return ProductResult.Single.from(product)
    }
    
    /**
     * 상품 재고를 감소시키는 메서드
     */
    @DistributedLock(
        domain = LockKeyConstants.PRODUCT_PREFIX,
        resourceType = LockKeyConstants.RESOURCE_STOCK,
        resourceIdExpression = "criteria.productId"
    )
    @CachePut(value = ["products"], key = "#criteria.productId")
    @CacheEvict(value = ["products"], key = "'all'")
    @Transactional
    fun decreaseStock(criteria: ProductCriteria.UpdateStockCriteria): ProductResult.Single {
        val product = productService.decreaseStock(criteria.toCommand())
        return ProductResult.Single.from(product)
    }
    
    /**
     * 여러 상품의 재고를 한 번에 감소시키는 메서드 (주문 처리 시 사용)
     */
    @CompositeLock(
        locks = [
            DistributedLock(
                domain = LockKeyConstants.PRODUCT_PREFIX,
                resourceType = LockKeyConstants.RESOURCE_STOCK,
                resourceIdExpression = "criteria.items[0].productId",
                timeout = LockKeyConstants.EXTENDED_TIMEOUT
            ),
            DistributedLock(
                domain = LockKeyConstants.PRODUCT_PREFIX,
                resourceType = LockKeyConstants.RESOURCE_STOCK,
                resourceIdExpression = "criteria.items.size > 1 ? criteria.items[1].productId : criteria.items[0].productId"
            )
        ],
        ordered = true
    )
    @Transactional
    fun decreaseStockBatch(criteria: ProductCriteria.BatchUpdateStockCriteria): List<ProductResult.Single> {
        // 결과 저장 리스트
        val results = mutableListOf<ProductResult.Single>()
        
        // 각 상품별 재고 차감 처리
        criteria.items.forEach { stockItem ->
            val updateCommand = ProductCommand.UpdateStockCommand(
                productId = stockItem.productId,
                quantity = stockItem.quantity
            )
            
            val product = productService.decreaseStock(updateCommand)
            results.add(ProductResult.Single.from(product))
            
            // 캐시 갱신
            cacheManager.getCache("products")?.evict(stockItem.productId)
        }
        
        // 전체 상품 리스트 캐시 갱신
        cacheManager.getCache("products")?.evict("all")
        
        return results
    }

    /**
     * 상품 ID 목록으로 상품과 옵션 정보를 함께 조회하는 메서드
     */
    @Transactional(readOnly = true)
    fun getProductsWithOptionsByIds(productIds: List<Long>): List<ProductResult.ProductWithOptions> {
        if (productIds.isEmpty()) return emptyList()
        
        // 1. 상품 정보 조회
        val products = productService.getByIds(productIds)
        if (products.isEmpty()) return emptyList()
        
        // 2. 모든 상품의 옵션 한 번에 조회
        val allOptions = productOptionService.getAllByProductIds(productIds)
        
        // 3. 옵션을 상품별로 그룹화
        val optionsByProductId = allOptions.groupBy { it.productId }
        
        // 4. 결과 조합 (순서 유지를 위해 productIds 순서대로 맵핑)
        val productsMap = products.associateBy { it.id }
        return productIds
            .mapNotNull { productsMap[it] }
            .map { product ->
                ProductResult.ProductWithOptions(
                    product, 
                    optionsByProductId[product.id] ?: emptyList()
                )
            }
    }
}
