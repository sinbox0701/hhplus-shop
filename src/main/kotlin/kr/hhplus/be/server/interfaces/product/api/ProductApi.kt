package kr.hhplus.be.server.interfaces.product.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.interfaces.product.ProductRequest
import kr.hhplus.be.server.interfaces.product.ProductResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@Tag(name = "상품", description = "상품 관련 API")
interface ProductApi {
    
    // 생성(Create) 관련 API
    
    @Operation(summary = "상품 및 옵션 생성", description = "새로운 상품과 옵션을 함께 등록합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "생성 성공",
            content = [Content(schema = Schema(implementation = ProductResponse.Response::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [Content()]
        )
    )
    @PostMapping
    fun createProductWithOptions(
        @Parameter(description = "상품 생성 정보", required = true)
        @Valid @RequestBody request: ProductRequest.CreateRequest
    ): ResponseEntity<ProductResponse.Response>
    
    @Operation(summary = "상품 옵션 추가", description = "기존 상품에 새로운 옵션을 추가합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "추가 성공",
            content = [Content(schema = Schema(implementation = ProductResponse.Response::class))]
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
    @PostMapping("/{id}/options")
    fun addProductOptions(
        @Parameter(description = "상품 ID", required = true)
        @PathVariable id: Long,
        
        @Parameter(description = "추가할 옵션 정보", required = true)
        @Valid @RequestBody request: ProductRequest.AddOptionsRequest
    ): ResponseEntity<ProductResponse.Response>
    
    // 조회(Read) 관련 API
    
    @Operation(summary = "전체 상품 목록 조회 (옵션 포함)", description = "모든 상품과 각 상품의 옵션 목록을 조회합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = ProductResponse.Response::class))]
        )
    )
    @GetMapping
    fun getAllProductsWithOptions(): ResponseEntity<List<ProductResponse.Response>>
    
    @Operation(summary = "전체 상품 목록 조회 (옵션 미포함)", description = "모든 상품 정보만 조회합니다 (옵션 정보 제외).")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = ProductResponse.SimpleResponse::class))]
        )
    )
    @GetMapping("/simple")
    fun getAllProducts(): ResponseEntity<List<ProductResponse.SimpleResponse>>
    
    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보와 옵션을 조회합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = ProductResponse.DetailResponse::class))]
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
    ): ResponseEntity<ProductResponse.DetailResponse>
    
    @Operation(summary = "인기 상품 조회", description = "최근 3일 동안 가장 많이 팔린 상품 5개를 조회합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = ProductResponse.TopSellingProductsResponse::class))]
        )
    )
    @GetMapping("/top-selling")
    fun getTopSellingProducts(): ResponseEntity<ProductResponse.TopSellingProductsResponse>
    
    // 수정(Update) 관련 API
    
    @Operation(summary = "상품 수정", description = "기존 상품 정보를 수정합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "수정 성공",
            content = [Content(schema = Schema(implementation = ProductResponse.Response::class))]
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
        @Valid @RequestBody request: ProductRequest.UpdateRequest
    ): ResponseEntity<ProductResponse.Response>
    
    @Operation(summary = "상품 옵션 수정", description = "기존 상품의 옵션 정보를 수정합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "수정 성공",
            content = [Content(schema = Schema(implementation = ProductResponse.Response::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [Content()]
        ),
        ApiResponse(
            responseCode = "404",
            description = "상품 또는 옵션을 찾을 수 없음",
            content = [Content()]
        )
    )
    @PutMapping("/{id}/options")
    fun updateProductOptions(
        @Parameter(description = "상품 ID", required = true)
        @PathVariable id: Long,
        
        @Parameter(description = "옵션 수정 정보", required = true)
        @Valid @RequestBody request: ProductRequest.UpdateOptionsRequest
    ): ResponseEntity<ProductResponse.Response>
    
    // 삭제(Delete) 관련 API
    
    @Operation(summary = "상품 삭제", description = "상품과 해당 상품의 모든 옵션을 함께 삭제합니다.")
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
    
    @Operation(summary = "상품 옵션 삭제", description = "특정 상품의 선택된 옵션을 삭제합니다. 최소 하나의 옵션은 유지되어야 합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "삭제 성공",
            content = [Content(schema = Schema(implementation = ProductResponse.Response::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (옵션이 하나만 있는 경우)",
            content = [Content()]
        ),
        ApiResponse(
            responseCode = "404",
            description = "상품 또는 옵션을 찾을 수 없음",
            content = [Content()]
        )
    )
    @DeleteMapping("/{id}/options")
    fun deleteProductOptions(
        @Parameter(description = "상품 ID", required = true)
        @PathVariable id: Long,
        
        @Parameter(description = "삭제할 옵션 ID 목록", required = true)
        @Valid @RequestBody request: ProductRequest.DeleteOptionsRequest
    ): ResponseEntity<ProductResponse.Response>
} 