package kr.hhplus.be.server.controller.product

import kr.hhplus.be.server.controller.product.api.ProductApi
import kr.hhplus.be.server.controller.product.dto.request.ProductCreateRequest
import kr.hhplus.be.server.controller.product.dto.request.ProductUpdateRequest
import kr.hhplus.be.server.controller.product.dto.response.ProductDetailResponse
import kr.hhplus.be.server.controller.product.dto.response.ProductOptionResponse
import kr.hhplus.be.server.controller.product.dto.response.ProductResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/products")
@Validated
class ProductController : ProductApi {

    override fun getAllProducts(): ResponseEntity<List<ProductResponse>> {
        // Mock API response
        val now = LocalDateTime.now()
        val products = listOf(
            ProductResponse(
                productId = 1L,
                name = "청바지",
                description = "편안한 청바지입니다.",
                price = 29000.0,
                options = listOf(
                    ProductOptionResponse(1, "S 사이즈", 0.0, 10),
                    ProductOptionResponse(2, "M 사이즈", 0.0, 20),
                    ProductOptionResponse(3, "L 사이즈", 0.0, 15)
                ),
                createdAt = now,
                updatedAt = now
            ),
            ProductResponse(
                productId = 2L,
                name = "티셔츠",
                description = "시원한 여름용 티셔츠입니다.",
                price = 15000.0,
                options = listOf(
                    ProductOptionResponse(4, "화이트 S", 0.0, 5),
                    ProductOptionResponse(5, "화이트 M", 0.0, 10),
                    ProductOptionResponse(6, "블랙 S", 2000.0, 3),
                    ProductOptionResponse(7, "블랙 M", 2000.0, 8)
                ),
                createdAt = now,
                updatedAt = now
            )
        )
        return ResponseEntity.ok(products)
    }

    override fun getProductById(@PathVariable id: Long): ResponseEntity<ProductDetailResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        val productId = id.toLong()
        val product = ProductDetailResponse(
            productId = productId,
            name = "상품 $productId",
            description = "상품 상세 설명입니다.",
            price = 29000.0,
            options = listOf(
                ProductOptionResponse(productId * 10 + 1, "옵션 1", 0.0, 10),
                ProductOptionResponse(productId * 10 + 2, "옵션 2", 5000.0, 5)
            ),
            createdAt = now,
            updatedAt = now
        )
        return ResponseEntity.ok(product)
    }

    override fun createProduct(@Valid @RequestBody request: ProductCreateRequest): ResponseEntity<ProductResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        val createdProduct = ProductResponse(
            productId = 1,
            name = request.name,
            description = request.description,
            price = request.price,
            options = request.options.mapIndexed { index, option ->
                ProductOptionResponse(
                    optionId = (index + 1).toLong(),
                    name = option.name,
                    additionalPrice = option.additionalPrice,
                    availableQuantity = option.availableQuantity
                )
            },
            createdAt = now,
            updatedAt = now
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct)
    }

    override fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody request: ProductUpdateRequest
    ): ResponseEntity<ProductResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        val productId = id.toLong()
        
        // 기본 값 설정
        val name = request.name ?: "상품 $productId"
        val description = request.description ?: "상품 설명입니다."
        val price = request.price ?: 29000.0
        
        // 옵션 처리
        val options = request.options?.map {
            ProductOptionResponse(
                optionId = it.optionId.toLong(),
                name = it.name ?: "옵션 ${it.optionId}",
                additionalPrice = it.additionalPrice ?: 0.0,
                availableQuantity = it.availableQuantity ?: 10
            )
        } ?: listOf(
            ProductOptionResponse(productId * 10 + 1, "기본 옵션 1", 0.0, 10),
            ProductOptionResponse(productId * 10 + 2, "기본 옵션 2", 0.0, 5)
        )
        
        val updatedProduct = ProductResponse(
            productId = productId,
            name = name,
            description = description,
            price = price,
            options = options,
            createdAt = now.minusDays(1),
            updatedAt = now
        )
        return ResponseEntity.ok(updatedProduct)
    }

    override fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        // Mock deletion
        return ResponseEntity.noContent().build()
    }
} 