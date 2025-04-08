package kr.hhplus.be.server.controller.order.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.controller.order.dto.request.OrderCreateRequest
import kr.hhplus.be.server.controller.order.dto.request.OrderStatusUpdateRequest
import kr.hhplus.be.server.controller.order.dto.response.OrderResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@Tag(name = "주문 API", description = "주문 관련 API")
interface OrderApi {

    @Operation(
        summary = "주문 생성",
        description = "새로운 주문을 생성합니다.",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "주문 생성 성공",
                content = [Content(schema = Schema(implementation = OrderResponse::class))]
            )
        ]
    )
    @PostMapping
    fun createOrder(
        @Valid @RequestBody orderCreateRequest: OrderCreateRequest
    ): ResponseEntity<OrderResponse>

    @Operation(
        summary = "주문 내역 조회",
        description = "사용자의 모든 주문 내역을 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "주문 내역 조회 성공",
                content = [Content(schema = Schema(implementation = OrderResponse::class))]
            )
        ]
    )
    @GetMapping("/account/{accountId}")
    fun getOrdersByAccountId(
        @Parameter(description = "사용자 ID", example = "1")
        @PathVariable accountId: Long
    ): ResponseEntity<List<OrderResponse>>

    @Operation(
        summary = "주문 상세 조회",
        description = "주문 ID로 단일 주문의 상세 정보를 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "주문 상세 조회 성공",
                content = [Content(schema = Schema(implementation = OrderResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "주문을 찾을 수 없음"
            )
        ]
    )
    @GetMapping("/{orderId}")
    fun getOrder(
        @Parameter(description = "주문 ID", example = "1")
        @PathVariable orderId: Long
    ): ResponseEntity<OrderResponse>

    @Operation(
        summary = "주문 상태 업데이트",
        description = "주문 상태를 업데이트합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "주문 상태 업데이트 성공",
                content = [Content(schema = Schema(implementation = OrderResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "주문을 찾을 수 없음"
            )
        ]
    )
    @PatchMapping("/{orderId}/status")
    fun updateOrderStatus(
        @Parameter(description = "주문 ID", example = "1")
        @PathVariable orderId: Long,
        @Valid @RequestBody orderStatusUpdateRequest: OrderStatusUpdateRequest
    ): ResponseEntity<OrderResponse>

    @Operation(
        summary = "주문 취소",
        description = "주문을 취소합니다. 주문 상태를 CANCELLED로 변경합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "주문 취소 성공",
                content = [Content(schema = Schema(implementation = OrderResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "주문을 찾을 수 없음"
            ),
            ApiResponse(
                responseCode = "400",
                description = "이미 완료된 주문은 취소할 수 없습니다."
            )
        ]
    )
    @DeleteMapping("/{orderId}")
    fun cancelOrder(
        @Parameter(description = "주문 ID", example = "1")
        @PathVariable orderId: Long
    ): ResponseEntity<OrderResponse>
}
