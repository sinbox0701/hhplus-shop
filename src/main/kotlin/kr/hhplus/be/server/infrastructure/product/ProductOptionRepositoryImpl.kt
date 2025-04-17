package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.repository.ProductOptionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
class ProductOptionRepositoryImpl(
    private val jpaProductOptionRepository: JpaProductOptionRepository
) : ProductOptionRepository {
    
    override fun save(productOption: ProductOption): ProductOption {
        val productOptionEntity = ProductOptionEntity.fromProductOption(productOption)
        val savedEntity = jpaProductOptionRepository.save(productOptionEntity)
        return savedEntity.toProductOption()
    }
    
    override fun findById(id: Long): ProductOption? {
        return jpaProductOptionRepository.findByIdOrNull(id)?.toProductOption()
    }
    
    override fun findByProductId(productId: Long): List<ProductOption> {
        return jpaProductOptionRepository.findByProductId(productId).map { it.toProductOption() }
    }
    
    override fun findByProductIdAndId(productId: Long, id: Long): ProductOption? {
        return jpaProductOptionRepository.findByProductIdAndId(productId, id)?.toProductOption()
    }
    
    override fun update(productOption: ProductOption): ProductOption {
        val productOptionEntity = ProductOptionEntity.fromProductOption(productOption)
        val savedEntity = jpaProductOptionRepository.save(productOptionEntity)
        return savedEntity.toProductOption()
    }
    
    override fun updateQuantity(id: Long, quantity: Int): ProductOption {
        // 먼저 엔티티를 조회
        val productOptionEntity = jpaProductOptionRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("상품 옵션을 찾을 수 없습니다: $id")
        
        // 수량 업데이트 쿼리 실행
        jpaProductOptionRepository.updateQuantity(id, quantity)
        
        // 업데이트된 엔티티 다시 조회하여 반환
        return jpaProductOptionRepository.findByIdOrNull(id)!!.toProductOption()
    }
    
    override fun delete(id: Long) {
        jpaProductOptionRepository.deleteById(id)
    }
} 