package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.model.ProductOption
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(
    name = "product_options",
    indexes = [
        Index(name = "idx_product_option_product_id", columnList = "product_id")
    ]
)
class ProductOptionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "product_id", nullable = false)
    val productId: Long,
    
    @Column(nullable = false, length = 10)
    val name: String,
    
    @Column(nullable = false)
    val availableQuantity: Int,
    
    @Column(nullable = false)
    val additionalPrice: Double,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime,
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime
) {
    fun toProductOption(): ProductOption {
        // ProductOption의 private 생성자 접근을 위한 리플렉션 사용
        val productOptionClass = ProductOption::class.java
        val constructor = productOptionClass.getDeclaredConstructor(
            Long::class.java, Long::class.java, String::class.java,
            Int::class.java, Double::class.java, 
            LocalDateTime::class.java, LocalDateTime::class.java
        )
        constructor.isAccessible = true
        
        return constructor.newInstance(
            id, productId, name, availableQuantity, additionalPrice, 
            createdAt, updatedAt
        )
    }
    
    companion object {
        fun fromProductOption(productOption: ProductOption): ProductOptionEntity {
            // ProductOption의 private 필드 접근을 위한 리플렉션 사용
            val productOptionClass = ProductOption::class.java
            
            val idField = productOptionClass.getDeclaredField("id")
            val productIdField = productOptionClass.getDeclaredField("productId")
            val nameField = productOptionClass.getDeclaredField("name")
            val availableQuantityField = productOptionClass.getDeclaredField("availableQuantity")
            val additionalPriceField = productOptionClass.getDeclaredField("additionalPrice")
            val createdAtField = productOptionClass.getDeclaredField("createdAt")
            val updatedAtField = productOptionClass.getDeclaredField("updatedAt")
            
            idField.isAccessible = true
            productIdField.isAccessible = true
            nameField.isAccessible = true
            availableQuantityField.isAccessible = true
            additionalPriceField.isAccessible = true
            createdAtField.isAccessible = true
            updatedAtField.isAccessible = true
            
            return ProductOptionEntity(
                id = idField.get(productOption) as? Long,
                productId = productIdField.get(productOption) as Long,
                name = nameField.get(productOption) as String,
                availableQuantity = availableQuantityField.get(productOption) as Int,
                additionalPrice = additionalPriceField.get(productOption) as Double,
                createdAt = createdAtField.get(productOption) as LocalDateTime,
                updatedAt = updatedAtField.get(productOption) as LocalDateTime
            )
        }
    }
} 