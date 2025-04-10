package kr.hhplus.be.server.interfaces.product

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

class ProductRequest {
    data class CreateRequest(
        @field:NotBlank(message = "이름은 필수입니다.")
        val name: String,
        
        @field:NotBlank(message = "설명은 필수입니다.")
        val description: String,
        
        @field:NotNull(message = "가격은 필수입니다.")
        @field:Min(value = 0, message = "가격은 0 이상이어야 합니다.")
        val price: Double,
        
        @field:NotNull(message = "옵션은 최소 1개 이상이어야 합니다.")
        @field:Size(min = 1, message = "옵션은 최소 1개 이상이어야 합니다.")
        @field:Valid
        val options: List<CreateOptionRequest>
    )
    
    data class CreateOptionRequest(
        @field:NotBlank(message = "옵션 이름은 필수입니다.")
        val name: String,
        
        @field:NotNull(message = "추가 가격은 필수입니다.")
        @field:Min(value = 0, message = "추가 가격은 0 이상이어야 합니다.")
        val additionalPrice: Double,
        
        @field:NotNull(message = "수량은 필수입니다.")
        @field:Min(value = 0, message = "수량은 0 이상이어야 합니다.")
        val availableQuantity: Int
    )
    
    data class AddOptionsRequest(
        @field:NotNull(message = "옵션은 최소 1개 이상이어야 합니다.")
        @field:Size(min = 1, message = "옵션은 최소 1개 이상이어야 합니다.")
        @field:Valid
        val options: List<CreateOptionRequest>
    )
    
    data class UpdateRequest(
        val name: String? = null,
        val description: String? = null,
        val price: Double? = null,
        val options: List<UpdateOptionRequest>? = null,
        val newOptions: List<CreateOptionRequest>? = null,
        val removeOptionIds: List<Long>? = null
    )
    
    data class UpdateOptionsRequest(
        @field:NotEmpty(message = "수정할 옵션이 최소 1개 이상이어야 합니다.")
        @field:Valid
        val options: List<UpdateOptionRequest>
    )
    
    data class DeleteOptionsRequest(
        @field:NotEmpty(message = "삭제할 옵션 ID는 최소 1개 이상이어야 합니다.")
        val optionIds: List<Long>
    )
    
    data class UpdateOptionRequest(
        val optionId: Long? = null,
        val name: String? = null,
        val additionalPrice: Double? = null,
        val availableQuantity: Int? = null
    )
} 