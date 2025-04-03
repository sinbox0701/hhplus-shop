package kr.hhplus.be.server.controller.product.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.controller.product.dto.request.ProductCreateRequest
import kr.hhplus.be.server.controller.product.dto.request.ProductUpdateRequest
import kr.hhplus.be.server.controller.product.dto.response.ProductDetailResponse
import kr.hhplus.be.server.controller.product.dto.response.ProductResponse
import kr.hhplus.be.server.controller.product.dto.response.TopSellingProductsResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@Tag(name = "상품", description = "상품 관련 API")
interface ProductApi {
    
    @Operation(summary = "전체 상품 목록 조회", description = "모든 상품 목록을 조회합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = ProductResponse::class))]
        )
    )
    @GetMapping
    fun getAllProducts(): ResponseEntity<List<ProductResponse>>
    
    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = ProductDetailResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "상품을 찾을 수 없음",
            content = [Content()]
        )
    )
    @GetMapping("/{id}")
    fun getProductById(
        @Parameter(description = "상품 ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<ProductDetailResponse>
    
    @Operation(summary = "인기 상품 조회", description = "최근 3일 동안 가장 많이 팔린 상품 5개를 조회합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = TopSellingProductsResponse::class))]
        )
    )
    @GetMapping("/top-selling")
    fun getTopSellingProducts(): ResponseEntity<TopSellingProductsResponse>
    
    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "등록 성공",
            content = [Content(schema = Schema(implementation = ProductResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [Content()]
        )
    )
    @PostMapping
    fun createProduct(
        @Parameter(description = "상품 생성 정보", required = true)
        @Valid @RequestBody request: ProductCreateRequest
    ): ResponseEntity<ProductResponse>
    
    @Operation(summary = "상품 수정", description = "기존 상품 정보를 수정합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "수정 성공",
            content = [Content(schema = Schema(implementation = ProductResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [Content()]
        ),
        ApiResponse(
            responseCode = "404",
            description = "상품을 찾을 수 없음",
            content = [Content()]
        )
    )
    @PutMapping("/{id}")
    fun updateProduct(
        @Parameter(description = "상품 ID", required = true)
        @PathVariable id: Long,
        
        @Parameter(description = "상품 수정 정보", required = true)
        @Valid @RequestBody request: ProductUpdateRequest
    ): ResponseEntity<ProductResponse>
    
    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "204",
            description = "삭제 성공",
            content = [Content()]
        ),
        ApiResponse(
            responseCode = "404",
            description = "상품을 찾을 수 없음",
            content = [Content()]
        )
    )
    @DeleteMapping("/{id}")
    fun deleteProduct(
        @Parameter(description = "상품 ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<Void>
} 