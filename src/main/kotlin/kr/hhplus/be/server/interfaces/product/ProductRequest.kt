package kr.hhplus.be.server.interfaces.product

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

class ProductRequest {
    data class CreateRequest(
        @field:NotBlank(message = "상품명은 필수입니다.")
        @field:Size(min = 1, max = 100, message = "상품명은 1~100자 사이여야 합니다.")
        val name: String,
        
        @field:NotBlank(message = "설명은 필수입니다.")
        @field:Size(min = 1, max = 1000, message = "설명은 1~1000자 사이여야 합니다.")
        val description: String,
        
        @field:DecimalMin(value = "0.0", message = "가격은 0 이상이어야 합니다.")
        val price: Double,
        
        @field:Valid
        val options: List<CreateProductOptionRequest>
    )
    
    data class CreateProductRequest(
        @field:NotBlank(message = "상품명은 필수입니다.")
        @field:Size(min = 1, max = 100, message = "상품명은 1~100자 사이여야 합니다.")
        val name: String,
        
        @field:NotBlank(message = "설명은 필수입니다.")
        @field:Size(min = 1, max = 1000, message = "설명은 1~1000자 사이여야 합니다.")
        val description: String,
        
        @field:DecimalMin(value = "0.0", message = "가격은 0 이상이어야 합니다.")
        val price: Double
    )
    
    data class UpdateProductRequest(
        val id: Long,
        
        @field:Size(min = 1, max = 100, message = "상품명은 1~100자 사이여야 합니다.")
        val name: String?,
        
        @field:Size(min = 1, max = 1000, message = "설명은 1~1000자 사이여야 합니다.")
        val description: String?,
        
        @field:DecimalMin(value = "0.0", message = "가격은 0 이상이어야 합니다.")
        val price: Double?
    )
    
    data class CreateProductOptionRequest(
        @field:NotBlank(message = "옵션명은 필수입니다.")
        @field:Size(min = 1, max = 50, message = "옵션명은 1~50자 사이여야 합니다.")
        val name: String,
        
        @field:Min(value = 0, message = "재고는 0 이상이어야 합니다.")
        val availableQuantity: Int,
        
        @field:DecimalMin(value = "0.0", message = "추가 가격은 0 이상이어야 합니다.")
        val additionalPrice: Double
    )
    
    data class UpdateProductOptionRequest(
        val optionId: Long?,
        
        @field:Size(min = 1, max = 50, message = "옵션명은 1~50자 사이여야 합니다.")
        val name: String?,
        
        @field:Min(value = 0, message = "재고는 0 이상이어야 합니다.")
        val availableQuantity: Int?,
        
        @field:DecimalMin(value = "0.0", message = "추가 가격은 0 이상이어야 합니다.")
        val additionalPrice: Double?
    )
    
    data class UpdateProductOptionQuantityRequest(
        @field:Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        val quantity: Int
    )
    
    data class UpdateRequest(
        @field:Size(min = 1, max = 100, message = "상품명은 1~100자 사이여야 합니다.")
        val name: String?,
        
        @field:Size(min = 1, max = 1000, message = "설명은 1~1000자 사이여야 합니다.")
        val description: String?,
        
        @field:DecimalMin(value = "0.0", message = "가격은 0 이상이어야 합니다.")
        val price: Double?,
        
        val options: List<UpdateProductOptionRequest>?,
        
        val newOptions: List<CreateProductOptionRequest>?,
        
        val removeOptionIds: List<Long>?
    )
    
    data class UpdateOptionsRequest(
        @field:Valid
        val options: List<UpdateProductOptionRequest>
    )
    
    data class AddOptionsRequest(
        @field:Valid
        val options: List<CreateProductOptionRequest>
    )
    
    data class DeleteOptionsRequest(
        val optionIds: List<Long>
    )
} 