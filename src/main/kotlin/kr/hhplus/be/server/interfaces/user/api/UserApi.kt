package kr.hhplus.be.server.interfaces.user.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.server.interfaces.user.UserRequest
import kr.hhplus.be.server.interfaces.user.UserResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@Tag(name = "사용자", description = "사용자 및 계좌 관련 API")
interface UserApi {
    
    @Operation(summary = "전체 사용자 목록 조회", description = "모든 사용자 목록을 조회합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = UserResponse.Response::class))]
        )
    )
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserResponse.Response>>
    
    @Operation(summary = "사용자 상세 조회", description = "특정 사용자의 상세 정보와 계좌 정보를 조회합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = UserResponse.DetailResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = [Content()]
        )
    )
    @GetMapping("/{id}")
    fun getUserById(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<UserResponse.DetailResponse>
    
    @Operation(summary = "사용자 등록", description = "새로운 사용자를 등록합니다. 계좌가 자동으로 생성됩니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "등록 성공",
            content = [Content(schema = Schema(implementation = UserResponse.DetailResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [Content()]
        )
    )
    @PostMapping
    fun createUser(
        @Parameter(description = "사용자 생성 정보", required = true)
        @Valid @RequestBody request: UserRequest.CreateRequest
    ): ResponseEntity<UserResponse.DetailResponse>
    
    @Operation(summary = "사용자 정보 수정", description = "기존 사용자 정보를 수정합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "수정 성공",
            content = [Content(schema = Schema(implementation = UserResponse.Response::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [Content()]
        ),
        ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = [Content()]
        )
    )
    @PutMapping("/{id}")
    fun updateUser(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable id: Long,
        
        @Parameter(description = "사용자 수정 정보", required = true)
        @Valid @RequestBody request: UserRequest.UpdateRequest
    ): ResponseEntity<UserResponse.Response>
    
    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "204",
            description = "삭제 성공",
            content = [Content()]
        ),
        ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = [Content()]
        )
    )
    @DeleteMapping("/{id}")
    fun deleteUser(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<Void>
    
    @Operation(summary = "계좌 입금", description = "사용자 계좌에 금액을 입금합니다.")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "입금 성공",
            content = [Content(schema = Schema(implementation = UserResponse.AccountResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [Content()]
        ),
        ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = [Content()]
        )
    )
    @PostMapping("/{id}/deposit")
    fun depositBalance(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable id: Long,
        
        @Parameter(description = "입금 정보", required = true)
        @Valid @RequestBody request: UserRequest.AccountDepositRequest
    ): ResponseEntity<UserResponse.AccountResponse>
} 