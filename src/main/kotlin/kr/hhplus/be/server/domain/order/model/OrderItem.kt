package kr.hhplus.be.server.domain.order.model

import java.time.LocalDateTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType

import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption

@Entity
@Table(name = "order_items")
data class OrderItem private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null, // 데이터베이스가 자동 생성하므로 null 허용
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    val order: Order,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    val product: Product,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id")
    val productOption: ProductOption,
    
    @Column(nullable = true)
    val accountCouponId: Long?,
    
    @Column(nullable = false)
    var quantity: Int,
    
    @Column(nullable = false)
    var price: Double,
    
    @Column(nullable = false)
    var createdAt: LocalDateTime,
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime
){
    companion object {
        const val MIN_QUANTITY = 1
        const val MAX_QUANTITY = 100
        val MIN_PRICE = 100.0

        fun create(order: Order, product: Product, productOption: ProductOption, 
                   quantity: Int, accountCouponId: Long?, discountRate: Double?): OrderItem {
            require(quantity in MIN_QUANTITY..MAX_QUANTITY) { "수량은 $MIN_QUANTITY 부터 $MAX_QUANTITY 사이여야 합니다." }
            val basePrice = (product.price + productOption.additionalPrice) * quantity
            val finalPrice = discountRate?.let { basePrice * (1 - it / 100) } ?: basePrice
            require(finalPrice >= MIN_PRICE) { "가격은 $MIN_PRICE 이상이어야 합니다." }
            return OrderItem(order=order, product=product, productOption=productOption, accountCouponId=accountCouponId, quantity=quantity, price=finalPrice, createdAt=LocalDateTime.now(), updatedAt=LocalDateTime.now())
        }
    }

    fun update(quantity: Int?, productPrice: Double?): OrderItem {
        quantity?.let {
            require(it in MIN_QUANTITY..MAX_QUANTITY) { "수량은 $MIN_QUANTITY 부터 $MAX_QUANTITY 사이여야 합니다." }
            this.quantity = it
            this.updatedAt = LocalDateTime.now()
        }
        productPrice?.let {
            this.price = this.quantity * it
            require(this.price >= MIN_PRICE) { "가격은 $MIN_PRICE 이상이어야 합니다." }
            this.updatedAt = LocalDateTime.now()
        }
        return this
    }

    fun updatePrice(discountRate: Double?): OrderItem {
        val newPrice = discountRate?.let { price * (1 - it / 100) } ?: price
        require(newPrice >= MIN_PRICE) { "가격은 $MIN_PRICE 이상이어야 합니다." }
        price = newPrice
        updatedAt = LocalDateTime.now()
        return this
    }
}

