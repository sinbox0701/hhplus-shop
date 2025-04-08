package kr.hhplus.be.server.controller.product.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin

data class ProductCreateRequest(
    @field:NotBlank(message = "상품명은 필수입니다")
    @field:Size(min = 3, max = 20, message = "상품명은 3자 이상 20자 이하여야 합니다")
    val name: String,
    
    @field:NotBlank(message = "상품 설명은 필수입니다")
    @field:Size(min = 10, max = 1000, message = "상품 설명은 10자 이상 1000자 이하여야 합니다")
    val description: String,
    
    @field:NotNull(message = "가격은 필수입니다")
    @field:DecimalMin(value = "1", message = "가격은 1원 이상이어야 합니다")
    @field:DecimalMax(value = "1000000", message = "가격은 100만원 이하여야 합니다")
    val price: Double,
    
    @field:NotEmpty(message = "최소 1개 이상의 상품 옵션이 필요합니다")
    @field:Size(max = 20, message = "상품 옵션은 최대 20개까지 등록 가능합니다")
    @field:Valid
    val options: List<ProductOptionCreateRequest>
)

data class ProductOptionCreateRequest(
    @field:NotBlank(message = "옵션명은 필수입니다")
    @field:Size(min = 1, max = 10, message = "옵션명은 1자 이상 10자 이하여야 합니다")
    val name: String,
    
    @field:NotNull(message = "추가 가격은 필수입니다")
    @field:DecimalMin(value = "0", message = "추가 가격은 0원 이상이어야 합니다")
    val additionalPrice: Double,
    
    @field:NotNull(message = "재고 수량은 필수입니다")
    @field:Min(value = 0, message = "재고 수량은 0개 이상이어야 합니다")
    @field:jakarta.validation.constraints.Max(value = 1000, message = "재고 수량은 1000개 이하여야 합니다")
    val availableQuantity: Int
) 