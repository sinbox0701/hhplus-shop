package kr.hhplus.be.server.domain.product.service

import kr.hhplus.be.server.domain.product.model.ProductDailySales
import kr.hhplus.be.server.domain.product.repository.ProductDailySalesRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * 상품 판매 정보 관련 서비스
 */
@Service
class ProductSalesService(
    private val productDailySalesRepository: ProductDailySalesRepository
) {
    /**
     * 판매량 기준 인기 상품 ID 조회
     * 
     * @param startDate 조회 시작일
     * @param limit 조회할 상품 수
     * @return 판매량 기준 내림차순으로 정렬된 상품 ID 목록
     */
    @Transactional(readOnly = true)
    fun getTopSellingProductIds(startDate: LocalDate, limit: Int): List<Long> {
        return productDailySalesRepository.findTopSellingProducts(startDate, limit)
    }
    
    /**
     * 특정 날짜의 판매 데이터 조회
     * 
     * @param date 조회할 날짜
     * @return 해당 날짜의 판매 데이터 목록
     */
    @Transactional(readOnly = true)
    fun getSalesByDate(date: LocalDate): List<ProductDailySales> {
        return productDailySalesRepository.findBySaleDate(date)
    }
    
    /**
     * 판매 데이터 저장
     * 
     * @param productDailySales 저장할 판매 데이터
     * @return 저장된 판매 데이터
     */
    @Transactional
    fun saveSales(productDailySales: ProductDailySales): ProductDailySales {
        return productDailySalesRepository.save(productDailySales)
    }
    
    /**
     * 판매 데이터 일괄 저장
     * 
     * @param salesList 저장할 판매 데이터 목록
     * @return 저장된 판매 데이터 목록
     */
    @Transactional
    fun saveAllSales(salesList: List<ProductDailySales>): List<ProductDailySales> {
        return productDailySalesRepository.saveAll(salesList)
    }
} 