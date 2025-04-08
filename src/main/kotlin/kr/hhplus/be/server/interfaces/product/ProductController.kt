package kr.hhplus.be.server.interfaces.product

import kr.hhplus.be.server.interfaces.product.api.ProductApi
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

    override fun getAllProducts(): ResponseEntity<List<ProductResponse.Response>> {
        // Mock API response
        val now = LocalDateTime.now()
        val products = listOf(
            ProductResponse.Response(
                productId = 1L,
                name = "청바지",
                description = "편안한 청바지입니다.",
                price = 29000.0,
                options = listOf(
                    ProductResponse.ProductOptionResponse(1, "S 사이즈", 0.0, 10),
                    ProductResponse.ProductOptionResponse(2, "M 사이즈", 0.0, 20),
                    ProductResponse.ProductOptionResponse(3, "L 사이즈", 0.0, 15)
                ),
                createdAt = now,
                updatedAt = now
            ),
            ProductResponse.Response(
                productId = 2L,
                name = "티셔츠",
                description = "시원한 여름용 티셔츠입니다.",
                price = 15000.0,
                options = listOf(
                    ProductResponse.ProductOptionResponse(4, "화이트 S", 0.0, 5),
                    ProductResponse.ProductOptionResponse(5, "화이트 M", 0.0, 10),
                    ProductResponse.ProductOptionResponse(6, "블랙 S", 2000.0, 3),
                    ProductResponse.ProductOptionResponse(7, "블랙 M", 2000.0, 8)
                ),
                createdAt = now,
                updatedAt = now
            )
        )
        return ResponseEntity.ok(products)
    }

    override fun getProductById(@PathVariable id: Long): ResponseEntity<ProductResponse.DetailResponse> {
        // Mock API response
        val now = LocalDateTime.now()
        val productId = id.toLong()
        val product = ProductResponse.DetailResponse(
            productId = productId,
            name = "상품 $productId",
            description = "상품 상세 설명입니다.",
            price = 29000.0,
            options = listOf(
                ProductResponse.ProductOptionResponse(productId * 10 + 1, "옵션 1", 0.0, 10),
                ProductResponse.ProductOptionResponse(productId * 10 + 2, "옵션 2", 5000.0, 5)
            ),
            createdAt = now,
            updatedAt = now
        )
        return ResponseEntity.ok(product)
    }

    override fun getTopSellingProducts(): ResponseEntity<ProductResponse.TopSellingProductsResponse> {
        // 현재 시간 기준 최근 3일을 계산
        val now = LocalDateTime.now()
        val threeDaysAgo = now.minusDays(3)
        
        // Mock API response - 실제로는 OrderRepository를 통해 데이터를 가져와야 함
        val topSellingProducts = listOf(
            ProductResponse.TopSellingProductResponse(
                productId = 1L,
                name = "청바지",
                description = "편안한 청바지입니다.",
                price = 29000.0,
                totalSoldQuantity = 120,
                createdAt = now.minusDays(10),
                updatedAt = now.minusDays(1)
            ),
            ProductResponse.TopSellingProductResponse(
                productId = 5L,
                name = "가죽자켓",
                description = "고급 가죽 재질의 자켓입니다.",
                price = 89000.0,
                totalSoldQuantity = 87,
                createdAt = now.minusDays(15),
                updatedAt = now.minusDays(2)
            ),
            ProductResponse.TopSellingProductResponse(
                productId = 2L,
                name = "티셔츠",
                description = "시원한 여름용 티셔츠입니다.",
                price = 15000.0,
                totalSoldQuantity = 75,
                createdAt = now.minusDays(20),
                updatedAt = now.minusDays(3)
            ),
            ProductResponse.TopSellingProductResponse(
                productId = 8L,
                name = "운동화",
                description = "편안한 운동화입니다.",
                price = 55000.0,
                totalSoldQuantity = 63,
                createdAt = now.minusDays(12),
                updatedAt = now.minusDays(1)
            ),
            ProductResponse.TopSellingProductResponse(
                productId = 3L,
                name = "모자",
                description = "스타일리시한 모자입니다.",
                price = 12000.0,
                totalSoldQuantity = 58,
                createdAt = now.minusDays(30),
                updatedAt = now.minusDays(5)
            )
        )
        
        return ResponseEntity.ok(
            ProductResponse.TopSellingProductsResponse(
                products = topSellingProducts,
                startDate = threeDaysAgo,
                endDate = now
            )
        )
    }

    override fun createProduct(@Valid @RequestBody request: ProductRequest.CreateRequest): ResponseEntity<ProductResponse.Response> {
        // Mock API response
        val now = LocalDateTime.now()
        val createdProduct = ProductResponse.Response(
            productId = 1,
            name = request.name,
            description = request.description,
            price = request.price,
            options = request.options.mapIndexed { index, option ->
                ProductResponse.ProductOptionResponse(
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
        @Valid @RequestBody request: ProductRequest.UpdateRequest
    ): ResponseEntity<ProductResponse.Response> {
        // Mock API response
        val now = LocalDateTime.now()
        val productId = id.toLong()
        
        // 기본 값 설정
        val name = request.name ?: "상품 $productId"
        val description = request.description ?: "상품 설명입니다."
        val price = request.price ?: 29000.0
        
        // 옵션 처리
        val options = request.options?.map {
            ProductResponse.ProductOptionResponse(
                optionId = it.optionId.toLong(),
                name = it.name ?: "옵션 ${it.optionId}",
                additionalPrice = it.additionalPrice ?: 0.0,
                availableQuantity = it.availableQuantity ?: 10
            )
        } ?: listOf(
            ProductResponse.ProductOptionResponse(productId * 10 + 1, "기본 옵션 1", 0.0, 10),
            ProductResponse.ProductOptionResponse(productId * 10 + 2, "기본 옵션 2", 0.0, 5)
        )
        
        val updatedProduct = ProductResponse.Response(
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