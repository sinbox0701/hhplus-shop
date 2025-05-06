package kr.hhplus.be.server.interfaces.product

import kr.hhplus.be.server.application.product.ProductCriteria
import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.interfaces.product.ProductResponse
import kr.hhplus.be.server.interfaces.product.ProductRequest
import kr.hhplus.be.server.domain.product.service.ProductService
import kr.hhplus.be.server.interfaces.product.api.ProductApi
import kr.hhplus.be.server.domain.product.service.ProductCommand
import kr.hhplus.be.server.domain.product.service.ProductOptionCommand
import kr.hhplus.be.server.domain.product.service.ProductOptionService
import kr.hhplus.be.server.interfaces.product.ProductRequest.CreateProductRequest
import kr.hhplus.be.server.interfaces.product.ProductRequest.UpdateProductRequest
import kr.hhplus.be.server.interfaces.product.ProductRequest.CreateProductOptionRequest
import kr.hhplus.be.server.interfaces.product.ProductRequest.UpdateProductOptionRequest
import kr.hhplus.be.server.interfaces.product.ProductRequest.UpdateProductOptionQuantityRequest
import kr.hhplus.be.server.interfaces.product.ProductResponse.ProductDetailResponse
import kr.hhplus.be.server.interfaces.product.ProductResponse.ProductOptionResponse
import kr.hhplus.be.server.interfaces.product.ProductResponse.ProductListResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/products")
@Validated
class ProductController(
    private val productFacade: ProductFacade,
    private val productService: ProductService,
    private val productOptionService: ProductOptionService
) : ProductApi {

    // Create 작업
    override fun createProductWithOptions(@Valid @RequestBody request: ProductRequest.CreateRequest): ResponseEntity<ProductResponse.Response> {
        val criteria = ProductCriteria.CreateProductCriteria(
            name = request.name,
            description = request.description,
            price = request.price,
            options = request.options.map {
                ProductCriteria.CreateProductOptionCriteria(
                    name = it.name,
                    price = it.additionalPrice,
                    availableQuantity = it.availableQuantity
                )
            }
        )
        
        val createdProduct = productFacade.createProductWithOptions(criteria)
        val response = ProductResponse.from(createdProduct)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    override fun addProductOptions(
        @PathVariable id: Long,
        @Valid @RequestBody request: ProductRequest.AddOptionsRequest
    ): ResponseEntity<ProductResponse.Response> {
        val criteria = ProductCriteria.UpdateProductCriteria(
            id = id,
            optionsToAdd = request.options.map {
                ProductCriteria.CreateProductOptionCriteria(
                    name = it.name,
                    price = it.additionalPrice,
                    availableQuantity = it.availableQuantity
                )
            }
        )
        
        val options = productFacade.addOptionsToProduct(criteria)
        val productWithOptions = productFacade.getProductWithOptions(id)
        val response = ProductResponse.from(productWithOptions)
        
        return ResponseEntity.ok(response)
    }
    
    // Read 작업
    override fun getAllProductsWithOptions(): ResponseEntity<List<ProductResponse.Response>> {
        val products = productFacade.getAllProductsWithOptions().map { ProductResponse.from(it) }
        return ResponseEntity.ok(products)
    }
    
    override fun getAllProducts(): ResponseEntity<List<ProductResponse.SimpleResponse>> {
        val products = productService.getAll().map { ProductResponse.simpleFrom(it) }
        return ResponseEntity.ok(products)
    }

    override fun getProductById(@PathVariable id: Long): ResponseEntity<ProductResponse.DetailResponse> {
        val productWithOptions = productFacade.getProductWithOptions(id)
        val response = ProductResponse.detailFrom(productWithOptions)
        return ResponseEntity.ok(response)
    }

    override fun getTopSellingProducts(): ResponseEntity<ProductResponse.TopSellingProductsResponse> {
        val topSellingProducts = productFacade.getTopSellingProducts()
        val response = ProductResponse.topSellingFrom(topSellingProducts)
        return ResponseEntity.ok(response)
    }
    
    // Update 작업
    override fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody request: ProductRequest.UpdateRequest
    ): ResponseEntity<ProductResponse.Response> {
        val criteria = ProductCriteria.UpdateProductCriteria(
            id = id,
            name = request.name,
            description = request.description,
            price = request.price,
            optionsToUpdate = request.options?.filter { it.optionId != null }?.map {
                ProductCriteria.UpdateProductOptionCriteria(
                    id = it.optionId!!,
                    name = it.name,
                    availableQuantity = it.availableQuantity,
                    additionalPrice = it.additionalPrice
                )
            },
            optionsToAdd = request.newOptions?.map {
                ProductCriteria.CreateProductOptionCriteria(
                    name = it.name,
                    price = it.additionalPrice,
                    availableQuantity = it.availableQuantity
                )
            },
            optionsToRemove = request.removeOptionIds
        )
        
        val updatedProduct = productFacade.updateProductWithOptions(criteria)
        val response = ProductResponse.from(updatedProduct)
        
        return ResponseEntity.ok(response)
    }
    
    override fun updateProductOptions(
        @PathVariable id: Long,
        @Valid @RequestBody request: ProductRequest.UpdateOptionsRequest
    ): ResponseEntity<ProductResponse.Response> {
        // 옵션 ID가 모두 존재하는지 확인
        if (request.options.any { it.optionId == null }) {
            throw IllegalArgumentException("옵션 ID는 필수입니다.")
        }
        
        val criteria = ProductCriteria.UpdateProductCriteria(
            id = id,
            optionsToUpdate = request.options.map {
                ProductCriteria.UpdateProductOptionCriteria(
                    id = it.optionId!!,
                    name = it.name,
                    availableQuantity = it.availableQuantity,
                    additionalPrice = it.additionalPrice
                )
            }
        )
        
        val updatedProduct = productFacade.updateProductWithOptions(criteria)
        val response = ProductResponse.from(updatedProduct)
        
        return ResponseEntity.ok(response)
    }
    
    // Delete 작업
    override fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        productFacade.deleteProductWithOptions(id)
        return ResponseEntity.noContent().build()
    }
    
    override fun deleteProductOptions(
        @PathVariable id: Long,
        @Valid @RequestBody request: ProductRequest.DeleteOptionsRequest
    ): ResponseEntity<ProductResponse.Response> {
        // 상품에 속한 모든 옵션 조회
        val product = productFacade.getProductWithOptions(id)
        
        // 최소 하나의 옵션은 남겨야 함 - 현재 옵션 수와 삭제할 옵션 수 비교
        if (product.options.size <= request.optionIds.size) {
            throw IllegalArgumentException("최소 하나의 옵션은 남겨야 합니다.")
        }
        
        val criteria = ProductCriteria.UpdateProductCriteria(
            id = id,
            optionsToRemove = request.optionIds
        )
        
        productFacade.removeOptionsFromProduct(criteria)
        
        // 결과 조회 및 반환
        val updatedProduct = productFacade.getProductWithOptions(id)
        val response = ProductResponse.from(updatedProduct)
        
        return ResponseEntity.ok(response)
    }

    /**
     * 비관적 락을 사용하여 상품 옵션 재고 차감
     */
    @PostMapping("/{productId}/options/{optionId}/subtract-inventory-with-lock")
    fun subtractOptionInventoryWithLock(
        @PathVariable productId: Long,
        @PathVariable optionId: Long,
        @RequestBody request: UpdateProductOptionQuantityRequest
    ): ProductOptionResponse {
        val product = productOptionService.subtractQuantityWithPessimisticLock(optionId, request.quantity)
        return ProductOptionResponse.from(product)
    }
    
    /**
     * 비관적 락을 사용하여 상품 옵션 재고 복원
     */
    @PostMapping("/{productId}/options/{optionId}/restore-inventory-with-lock")
    fun restoreOptionInventoryWithLock(
        @PathVariable productId: Long,
        @PathVariable optionId: Long,
        @RequestBody request: UpdateProductOptionQuantityRequest
    ): ProductOptionResponse {
        val product = productOptionService.restoreQuantityWithPessimisticLock(optionId, request.quantity)
        return ProductOptionResponse.from(product)
    }
} 