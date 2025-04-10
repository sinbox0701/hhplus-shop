package kr.hhplus.be.server.domain.product.model

import java.time.LocalDateTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "products")
data class Product private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null, // 데이터베이스가 자동 생성하므로 null 허용
    
    @Column(nullable = false, length = MAX_NAME_LENGTH)
    var name: String,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,
    
    @Column(nullable = false)
    var price: Double,
    
    @Column(nullable = false)
    var createdAt: LocalDateTime,
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime
){
    companion object {
        const val MIN_PRICE = 1.0
        const val MAX_PRICE = 1000000.0

        const val MIN_NAME_LENGTH = 3
        const val MAX_NAME_LENGTH = 20

        fun create(name: String, description: String, price: Double): Product {
            require(price >= MIN_PRICE && price <= MAX_PRICE) {
                "Initial amount must be between $MIN_PRICE and $MAX_PRICE"
            }
            return Product(name=name, description=description, price=price, createdAt=LocalDateTime.now(), updatedAt=LocalDateTime.now())
        }
    }

    fun update(name: String? = null, description: String? = null, price: Double? = null): Product {
        name?.let {
            require(it.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) { "Name must be between 3 and 20 characters" }
            this.name = it
            this.updatedAt = LocalDateTime.now()
        }
        description?.let {
            this.description = it
            this.updatedAt = LocalDateTime.now()
        }
        price?.let {
            require(it >= MIN_PRICE && it <= MAX_PRICE) { "Price must be between $MIN_PRICE and $MAX_PRICE" }
            this.price = it
            this.updatedAt = LocalDateTime.now()
        }
        return this
    }
}
