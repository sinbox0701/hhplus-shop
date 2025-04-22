package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.model.ProductDailySales
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "product_daily_sales",
    indexes = [
        Index(name = "idx_product_daily_sales_date", columnList = "sale_date"),
        Index(name = "idx_product_daily_sales_product_id", columnList = "product_id")
    ]
)
class ProductDailySalesEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "product_id", nullable = false)
    val productId: Long,
    
    @Column(name = "sale_date", nullable = false)
    val saleDate: LocalDate,
    
    @Column(name = "quantity_sold", nullable = false)
    val quantitySold: Int,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime,
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime
) {
    fun toProductDailySales(): ProductDailySales {
        return ProductDailySales(
            id = id,
            productId = productId,
            salesDate = saleDate,
            quantitySold = quantitySold,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        fun fromProductDailySales(dailySales: ProductDailySales): ProductDailySalesEntity {
            return ProductDailySalesEntity(
                id = dailySales.id,
                productId = dailySales.productId,
                saleDate = dailySales.salesDate,
                quantitySold = dailySales.quantitySold,
                createdAt = dailySales.createdAt,
                updatedAt = dailySales.updatedAt
            )
        }
    }
}
