package kr.hhplus.be.server.domain.product.service

import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.repository.ProductRepository
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository
) {
    fun createProduct(name: String, description: String, price: Double): Product {
        val id = System.currentTimeMillis() // 임시 ID 생성 방식
        val product = Product.create(id, name, description, price)
        return productRepository.save(product)
    }
    
    fun getProduct(id: Long): Product {
        return productRepository.findById(id) ?: throw IllegalArgumentException("Product not found with id: $id")
    }


    fun updateProduct(id: Long, name: String?, description: String?, price: Double?): Product {
        val product = getProduct(id)
        val updatedProduct = product.update(name, description, price)
        return productRepository.update(updatedProduct)
    }

    fun deleteProduct(id: Long) {
        getProduct(id) // 제품이 존재하는지 확인
        productRepository.delete(id)
    }
} 