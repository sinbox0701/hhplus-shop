package kr.hhplus.be.server.controller.product

import kr.hhplus.be.server.controller.product.dto.request.ProductCreateRequest
import kr.hhplus.be.server.controller.product.dto.request.ProductUpdateRequest
import kr.hhplus.be.server.controller.product.dto.response.ProductDetailResponse
import kr.hhplus.be.server.controller.product.dto.response.ProductOptionResponse
import kr.hhplus.be.server.controller.product.dto.response.ProductResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDateTime
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/products")
@Validated
class ProductController() {

    @GetMapping
    fun getAllProducts(): ResponseEntity<List<ProductResponse>> {
        // Mock API response
        val now = LocalDateTime.now()
        val products = listOf(
            ProductResponse(
                productId = 1,
                name = "청바지",
                description = "편안한 청바지입니다.",
                price = BigDecimal("29000"),
                options = listOf(
                    ProductOptionResponse(1, "S 사이즈", BigDecimal.ZERO, 10),
                    ProductOptionResponse(2, "M 사이즈", BigDecimal.ZERO, 20),
                    ProductOptionResponse(3, "L 사이즈", BigDecimal.ZERO, 15)
                ),
                createdAt = now,
                updatedAt = now
            ),
            ProductResponse(
                productId = 2,
                name = "티셔츠",
                description = "시원한 여름용 티셔츠입니다.",
                price = BigDecimal("15000"),
                options = listOf(
                    ProductOptionResponse(4, "화이트 S", BigDecimal.ZERO, 5),
                    ProductOptionResponse(5, "화이트 M", BigDecimal.ZERO, 10),
                    ProductOptionResponse(6, "블랙 S", BigDecimal("2000"), 3),
                    ProductOptionResponse(7, "블랙 M", BigDecimal("2000"), 8)
                ),
                createdAt = now,
                updatedAt = now
            )
        )
        return ResponseEntity.ok(products)
    }

    @GetMapping("/{productId}")
    fun getProductById(@PathVariable productId: Int): ResponseEntity<ProductDetailResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        val product = ProductDetailResponse(
            productId = productId,
            name = "상품 $productId",
            description = "상품 상세 설명입니다.",
            price = BigDecimal("29000"),
            options = listOf(
                ProductOptionResponse(productId * 10 + 1, "옵션 1", BigDecimal.ZERO, 10),
                ProductOptionResponse(productId * 10 + 2, "옵션 2", BigDecimal("5000"), 5)
            ),
            createdAt = now,
            updatedAt = now
        )
        return ResponseEntity.ok(product)
    }

    @PostMapping
    fun createProduct(@Valid @RequestBody request: ProductCreateRequest): ResponseEntity<ProductResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        val createdProduct = ProductResponse(
            productId = 1,
            name = request.name,
            description = request.description,
            price = request.price,
            options = request.options.mapIndexed { index, option ->
                ProductOptionResponse(
                    optionId = index + 1,
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

    @PutMapping("/{productId}")
    fun updateProduct(
        @PathVariable productId: Int,
        @Valid @RequestBody request: ProductUpdateRequest
    ): ResponseEntity<ProductResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        
        // 기본 값 설정
        val name = request.name ?: "상품 $productId"
        val description = request.description ?: "상품 설명입니다."
        val price = request.price ?: BigDecimal("29000")
        
        // 옵션 처리
        val options = request.options?.map {
            ProductOptionResponse(
                optionId = it.optionId,
                name = it.name ?: "옵션 ${it.optionId}",
                additionalPrice = it.additionalPrice ?: BigDecimal.ZERO,
                availableQuantity = it.availableQuantity ?: 10
            )
        } ?: listOf(
            ProductOptionResponse(productId * 10 + 1, "기본 옵션 1", BigDecimal.ZERO, 10),
            ProductOptionResponse(productId * 10 + 2, "기본 옵션 2", BigDecimal.ZERO, 5)
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

    @DeleteMapping("/{productId}")
    fun deleteProduct(@PathVariable productId: Int): ResponseEntity<Void> {
        // Mock deletion
        return ResponseEntity.noContent().build()
    }
} 