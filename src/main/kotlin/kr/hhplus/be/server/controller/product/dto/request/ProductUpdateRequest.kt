package kr.hhplus.be.server.controller.product.dto.request

import java.math.BigDecimal
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin

data class ProductUpdateRequest(
    @field:Size(min = 3, max = 20, message = "상품명은 3자 이상 20자 이하여야 합니다")
    val name: String? = null,
    
    @field:Size(min = 10, max = 1000, message = "상품 설명은 10자 이상 1000자 이하여야 합니다")
    val description: String? = null,
    
    @field:DecimalMin(value = "1", message = "가격은 1원 이상이어야 합니다")
    @field:DecimalMax(value = "1000000", message = "가격은 100만원 이하여야 합니다")
    val price: BigDecimal? = null,
    
    @field:Size(max = 20, message = "상품 옵션은 최대 20개까지 등록 가능합니다")
    @field:Valid
    val options: List<ProductOptionUpdateRequest>? = null
)

data class ProductOptionUpdateRequest(
    val optionId: Int,
    
    @field:Size(min = 1, max = 10, message = "옵션명은 1자 이상 10자 이하여야 합니다")
    val name: String? = null,
    
    @field:DecimalMin(value = "0", message = "추가 가격은 0원 이상이어야 합니다")
    val additionalPrice: BigDecimal? = null,
    
    @field:Min(value = 0, message = "재고 수량은 0개 이상이어야 합니다")
    @field:jakarta.validation.constraints.Max(value = 1000, message = "재고 수량은 1000개 이하여야 합니다")
    val availableQuantity: Int? = null
) 