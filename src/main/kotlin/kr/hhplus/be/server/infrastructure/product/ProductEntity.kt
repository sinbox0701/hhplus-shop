package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.model.Product
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "products")
class ProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, length = 20)
    val name: String,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val description: String,
    
    @Column(nullable = false)
    val price: Double,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime,
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime
) {
    fun toProduct(): Product {
        // Product의 private 생성자 접근을 위한 리플렉션 사용
        val productClass = Product::class.java
        val constructor = productClass.getDeclaredConstructor(
            Long::class.java, String::class.java, String::class.java,
            Double::class.java, LocalDateTime::class.java, LocalDateTime::class.java
        )
        constructor.isAccessible = true
        
        return constructor.newInstance(
            id, name, description, price, createdAt, updatedAt
        )
    }
    
    companion object {
        fun fromProduct(product: Product): ProductEntity {
            // Product의 private 필드 접근을 위한 리플렉션 사용
            val productClass = Product::class.java
            
            val idField = productClass.getDeclaredField("id")
            val nameField = productClass.getDeclaredField("name")
            val descriptionField = productClass.getDeclaredField("description")
            val priceField = productClass.getDeclaredField("price")
            val createdAtField = productClass.getDeclaredField("createdAt")
            val updatedAtField = productClass.getDeclaredField("updatedAt")
            
            idField.isAccessible = true
            nameField.isAccessible = true
            descriptionField.isAccessible = true
            priceField.isAccessible = true
            createdAtField.isAccessible = true
            updatedAtField.isAccessible = true
            
            return ProductEntity(
                id = idField.get(product) as? Long,
                name = nameField.get(product) as String,
                description = descriptionField.get(product) as String,
                price = priceField.get(product) as Double,
                createdAt = createdAtField.get(product) as LocalDateTime,
                updatedAt = updatedAtField.get(product) as LocalDateTime
            )
        }
    }
} 