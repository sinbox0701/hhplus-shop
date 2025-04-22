package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.repository.ProductRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class ProductRepositoryImpl(
    private val jpaProductRepository: JpaProductRepository
) : ProductRepository {
    
    override fun save(product: Product): Product {
        val productEntity = ProductEntity.fromProduct(product)
        val savedEntity = jpaProductRepository.save(productEntity)
        return savedEntity.toProduct()
    }
    
    override fun findById(id: Long): Product? {
        return jpaProductRepository.findByIdOrNull(id)?.toProduct()
    }
    
    override fun findAll(): List<Product> {
        return jpaProductRepository.findAll().map { it.toProduct() }
    }
    
    override fun findByIds(ids: List<Long>): List<Product> {
        if (ids.isEmpty()) return emptyList()
        return jpaProductRepository.findAllByIdIn(ids).map { it.toProduct() }
    }
    
    override fun update(product: Product): Product {
        // 엔티티를 저장할 때 이미 있는 ID라면 JPA는 업데이트를 수행함
        val productEntity = ProductEntity.fromProduct(product)
        val savedEntity = jpaProductRepository.save(productEntity)
        return savedEntity.toProduct()
    }
    
    override fun delete(id: Long) {
        jpaProductRepository.deleteById(id)
    }
} 