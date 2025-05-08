package kr.hhplus.be.server.domain.product.service

import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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

    /**
     * 여러 상품 ID로 상품 정보를 한 번에 조회
     */
    @Transactional(readOnly = true)
    fun getByIds(ids: List<Long>): List<Product> {
        return productRepository.findByIds(ids)
    }

    /**
     * 상품 재고 증가
     */
    @Transactional
    fun increaseStock(command: ProductCommand.UpdateStockCommand): Product {
        // 실제 구현에서는 상품의 재고 필드를 직접 수정하거나
        // 관련 옵션의 재고를 수정하는 로직이 필요합니다.
        // 현재 모델에서는 Product에 재고 필드가 없어 임시 구현만 제공합니다.
        val product = get(command.productId)
        return product
    }
    
    /**
     * 상품 재고 감소
     */
    @Transactional
    fun decreaseStock(command: ProductCommand.UpdateStockCommand): Product {
        // 실제 구현에서는 상품의 재고 필드를 직접 수정하거나
        // 관련 옵션의 재고를 수정하는 로직이 필요합니다.
        // 현재 모델에서는 Product에 재고 필드가 없어 임시 구현만 제공합니다.
        val product = get(command.productId)
        return product
    }
} 