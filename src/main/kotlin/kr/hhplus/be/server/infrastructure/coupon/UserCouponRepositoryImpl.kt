package kr.hhplus.be.server.infrastructure.coupon

import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.coupon.repository.UserCouponRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class UserCouponRepositoryImpl(
    private val jpaUserCouponRepository: JpaUserCouponRepository
) : UserCouponRepository {
    
    override fun save(userCoupon: UserCoupon): UserCoupon {
        val userCouponEntity = UserCouponEntity.fromUserCoupon(userCoupon)
        val savedEntity = jpaUserCouponRepository.save(userCouponEntity)
        return savedEntity.toUserCoupon()
    }
    
    override fun findById(id: Long): UserCoupon? {
        return jpaUserCouponRepository.findByIdOrNull(id)?.toUserCoupon()
    }
    
    override fun findByUserId(userId: Long): List<UserCoupon> {
        return jpaUserCouponRepository.findByUserId(userId).map { it.toUserCoupon() }
    }
    
    override fun findByCouponId(couponId: Long): List<UserCoupon> {
        return jpaUserCouponRepository.findByCouponId(couponId).map { it.toUserCoupon() }
    }
    
    override fun findByUserIdAndCouponId(userId: Long, couponId: Long): UserCoupon? {
        return jpaUserCouponRepository.findByUserIdAndCouponId(userId, couponId)?.toUserCoupon()
    }
    
    override fun update(userCoupon: UserCoupon): UserCoupon {
        val userCouponEntity = UserCouponEntity.fromUserCoupon(userCoupon)
        val savedEntity = jpaUserCouponRepository.save(userCouponEntity)
        return savedEntity.toUserCoupon()
    }
    
    override fun delete(id: Long) {
        jpaUserCouponRepository.deleteById(id)
    }
    
    override fun deleteByUserIdAndCouponId(userId: Long, couponId: Long) {
        jpaUserCouponRepository.deleteByUserIdAndCouponId(userId, couponId)
    }
} 