package kr.hhplus.be.server.service.product

import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.repository.product.ProductRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ProductService (
    private val productRepository: ProductRepository
) {
    fun save(productId: Int, name: String, description: String, price: BigDecimal): Product {
        val newProduct = Product.create(productId, name, description, price)
        return productRepository.save(newProduct)
    }

    fun getById(productId: Int): Product {
        return productRepository.findById(productId) ?: throw IllegalArgumentException("Product not found for productId: $productId")
    }

    fun update(productId: Int, name: String?, description: String?, price: BigDecimal?): Product {
        // 기존 상품 조회 (존재하지 않으면 예외 발생)
        val product = getById(productId)
        // 도메인 객체의 update 함수로 필드 업데이트 (옵션 값이 null이 아니면 해당 필드 업데이트)
        product.update(name, description, price)
        // 변경된 상품을 저장
        return productRepository.update(product)
    }

    fun delete(productId: Int) {
        // 존재 여부 확인 후 삭제 (없으면 getById에서 예외 발생)
        getById(productId)
        productRepository.delete(productId)
    }
}