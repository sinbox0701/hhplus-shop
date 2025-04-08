package kr.hhplus.be.server.controller.coupon.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.controller.coupon.dto.request.CouponCreateRequest
import kr.hhplus.be.server.controller.coupon.dto.request.CouponIssueRequest
import kr.hhplus.be.server.controller.coupon.dto.request.CouponUpdateRequest
import kr.hhplus.be.server.controller.coupon.dto.response.AccountCouponResponse
import kr.hhplus.be.server.controller.coupon.dto.response.CouponResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@Tag(name = "쿠폰", description = "쿠폰 관련 API")
interface CouponApi {
    
    @Operation(summary = "전체 쿠폰 목록 조회", description = "모든 쿠폰 목록을 조회합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = CouponResponse::class))]
        )
    )
    @GetMapping
    fun getAllCoupons(): ResponseEntity<List<CouponResponse>>
    
    @Operation(summary = "쿠폰 상세 조회", description = "특정 쿠폰의 상세 정보를 조회합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = CouponResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "쿠폰을 찾을 수 없음",
            content = [Content()]
        )
    )
    @GetMapping("/{couponId}")
    fun getCouponById(
        @Parameter(description = "쿠폰 ID", required = true)
        @PathVariable couponId: Long
    ): ResponseEntity<CouponResponse>
    
    @Operation(summary = "쿠폰 생성", description = "새로운 쿠폰을 생성합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "생성 성공",
            content = [Content(schema = Schema(implementation = CouponResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [Content()]
        )
    )
    @PostMapping
    fun createCoupon(
        @Parameter(description = "쿠폰 생성 정보", required = true)
        @Valid @RequestBody request: CouponCreateRequest
    ): ResponseEntity<CouponResponse>
    
    @Operation(summary = "쿠폰 수정", description = "기존 쿠폰 정보를 수정합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "수정 성공",
            content = [Content(schema = Schema(implementation = CouponResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [Content()]
        ),
        ApiResponse(
            responseCode = "404",
            description = "쿠폰을 찾을 수 없음",
            content = [Content()]
        )
    )
    @PutMapping("/{couponId}")
    fun updateCoupon(
        @Parameter(description = "쿠폰 ID", required = true)
        @PathVariable couponId: Long,
        
        @Parameter(description = "쿠폰 수정 정보", required = true)
        @Valid @RequestBody request: CouponUpdateRequest
    ): ResponseEntity<CouponResponse>
    
    @Operation(summary = "쿠폰 삭제", description = "쿠폰을 삭제합니다. 이미 발급된 쿠폰은 삭제할 수 없습니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "204",
            description = "삭제 성공",
            content = [Content()]
        ),
        ApiResponse(
            responseCode = "400",
            description = "이미 발급된 쿠폰은 삭제할 수 없음",
            content = [Content()]
        ),
        ApiResponse(
            responseCode = "404",
            description = "쿠폰을 찾을 수 없음",
            content = [Content()]
        )
    )
    @DeleteMapping("/{couponId}")
    fun deleteCoupon(
        @Parameter(description = "쿠폰 ID", required = true)
        @PathVariable couponId: Long
    ): ResponseEntity<Void>
    
    @Operation(summary = "쿠폰 발급", description = "사용자에게 쿠폰을 발급합니다. 쿠폰은 선착순으로 제한된 수량만큼만 발급됩니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "발급 성공",
            content = [Content(schema = Schema(implementation = AccountCouponResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 또는 쿠폰 발급 가능 수량 초과",
            content = [Content()]
        ),
        ApiResponse(
            responseCode = "404",
            description = "쿠폰 또는 사용자를 찾을 수 없음",
            content = [Content()]
        )
    )
    @PostMapping("/{couponId}/issue")
    fun issueCoupon(
        @Parameter(description = "쿠폰 ID", required = true)
        @PathVariable couponId: Long,
        
        @Parameter(description = "쿠폰 발급 정보", required = true)
        @Valid @RequestBody request: CouponIssueRequest
    ): ResponseEntity<AccountCouponResponse>
    
    @Operation(summary = "쿠폰 사용", description = "사용자의 쿠폰을 사용 처리합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "쿠폰 사용 성공",
            content = [Content(schema = Schema(implementation = AccountCouponResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 또는 이미 사용된 쿠폰",
            content = [Content()]
        ),
        ApiResponse(
            responseCode = "404",
            description = "쿠폰 또는 사용자를 찾을 수 없음",
            content = [Content()]
        )
    )
    @PostMapping("/account/{accountId}/coupons/{accountCouponId}/use")
    fun useCoupon(
        @Parameter(description = "계정 ID", required = true)
        @PathVariable accountId: Long,
        
        @Parameter(description = "계정 쿠폰 ID", required = true)
        @PathVariable accountCouponId: Long
    ): ResponseEntity<AccountCouponResponse>
} 