package kr.hhplus.be.server.domain.product.service

import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.repository.ProductRepository
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository
) {
    fun create(command: ProductCommand.CreateProductCommand): Product {
        val product = Product.create(command.name, command.description, command.price)
        return productRepository.save(product)
    }
    
    fun get(id: Long): Product {
        return productRepository.findById(id) ?: throw IllegalArgumentException("Product not found with id: $id")
    }

    fun getAll(): List<Product> {
        return productRepository.findAll()
    }

    fun update(command: ProductCommand.UpdateProductCommand): Product {
        val product = get(command.id)
        val updatedProduct = product.update(command.name, command.description, command.price)
        return productRepository.save(updatedProduct)
    }

    fun delete(id: Long) {
        get(id) // 제품이 존재하는지 확인
        productRepository.delete(id)
    }
} 