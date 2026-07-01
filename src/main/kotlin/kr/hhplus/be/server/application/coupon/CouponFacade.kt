package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.application.coupon.CouponCriteria
import kr.hhplus.be.server.application.coupon.CouponResult
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.coupon.service.CouponService
import kr.hhplus.be.server.domain.user.service.UserService
import kr.hhplus.be.server.shared.lock.DistributedLock
import kr.hhplus.be.server.shared.lock.LockKeyConstants
import kr.hhplus.be.server.shared.transaction.TransactionHelper
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 쿠폰 발급 유스케이스.
 *
 * 동시성 제어 계층(권위 계층, authoritative layer)
 * -------------------------------------------------
 * 이 프로젝트의 쿠폰 흐름은 하나의 계층이 "권위"를 갖도록 단일 전략으로 정리한다.
 * 서로 다른 계층을 무분별하게 중첩(Redisson + 비관적 락 + @Version)하지 않는다.
 *
 * - [create] (사용자별 쿠폰 발급): **Redisson 분산 락**이 권위 계층이다.
 *   여러 인스턴스가 떠 있어도 `coupon:user:{userId}` 키로 직렬화되어 한 사용자의
 *   중복 발급을 막는다. 정합성의 최종 방어선은 `user_coupon`의 (userId, couponId)
 *   유니크 제약이다. 단일 노드 DB 락을 여기서 추가로 걸지 않는다(분산 환경에서
 *   무의미하고 락 계층만 흐려지기 때문).
 * - 선착순 한정 수량 차감([CouponService.issueFirstComeFirstServedCoupon]):
 *   공유 자원인 "쿠폰 재고 row" 자체를 다투므로 **DB 비관적 락(PESSIMISTIC_WRITE)**
 *   이 권위 계층이다. 여기서는 Redisson 락을 중첩하지 않는다.
 *
 * 즉, "인스턴스 간 사용자 단위 직렬화 = Redisson", "단일 공유 row 정합성 = DB 락"으로
 * 역할을 분리하고, 계좌 잔액만 낙관적 락(@Version)을 사용한다.
 */
@Service
class CouponFacade(
    private val couponService: CouponService,
    private val userService: UserService,
    private val transactionHelper: TransactionHelper
) {
    /**
     * 사용자별 쿠폰 발급. 권위 계층은 Redisson 분산 락(`coupon:user:{userId}`)이며,
     * 최종 정합성은 `user_coupon` 유니크 제약이 보장한다.
     */
    @CacheEvict(value = ["coupons"], key = "#criteria.userId")
    @DistributedLock(
        domain = LockKeyConstants.COUPON_PREFIX,
        resourceType = LockKeyConstants.RESOURCE_USER,
        resourceIdExpression = "criteria.userId",
        timeout = LockKeyConstants.EXTENDED_TIMEOUT
    )
    fun create(criteria: CouponCriteria.CreateUserCouponCommand): UserCoupon {
        return transactionHelper.executeInTransaction {
            val user = userService.findById(criteria.userId)
            val coupon = couponService.create(criteria.toCreateCouponCommand())
            val userCoupon = couponService.createUserCoupon(criteria.toCreateUserCouponCommand(user.id!!, coupon.id!!))
            couponService.updateRemainingQuantity(criteria.toUpdateCouponRemainingQuantityCommand(coupon.id, 1))
            userCoupon
        }
    }

    @Cacheable(value = ["coupons"], key = "#userId")
    @Transactional(readOnly = true)
    fun findByUserId(userId: Long): List<CouponResult.UserCouponResult>{
        val user = userService.findById(userId)
        val userCoupons = couponService.findUserCouponsByUserId(userId)
        val couponIds = userCoupons.mapNotNull { it.couponId }
        val coupons = couponService.findAllByIds(couponIds)
        return userCoupons.mapNotNull { userCoupon ->
            val coupon = coupons.find { coupon -> coupon.id == userCoupon.couponId }
            coupon?.let { CouponResult.UserCouponResult.from(userCoupon, it) }
        }
    }

    @Cacheable(value = ["coupons"], key = "#userId + '_' + #couponId")
    @Transactional(readOnly = true)
    fun findByUserIdAndCouponId(userId: Long, couponId: Long): CouponResult.UserCouponResult{
        val coupon = couponService.findById(couponId)
        val userCoupon = couponService.findUserCouponByUserIdAndCouponId(userId, couponId)
        return CouponResult.UserCouponResult.from(userCoupon, coupon)
    }

    @CacheEvict(value = ["coupons"], key = "#criteria.userId")
    @Transactional()
    fun issue(criteria: CouponCriteria.UpdateCouponCommand) {
        val coupon = couponService.findById(criteria.couponId)
        val userCoupon = couponService.findUserCouponByUserIdAndCouponId(criteria.userId, criteria.couponId)
        couponService.issueUserCoupon(criteria.toIssueCouponCommand(userCoupon.id!!, coupon.startDate, coupon.endDate))
    }

    @CacheEvict(value = ["coupons"], key = "#criteria.userId")
    @Transactional()
    fun use(criteria: CouponCriteria.UpdateCouponCommand) {
        userService.findById(criteria.userId)
        couponService.findById(criteria.couponId)
        val userCoupon = couponService.findUserCouponByUserIdAndCouponId(criteria.userId, criteria.couponId)
        couponService.useUserCoupon(userCoupon.id!!)
    }

    @CacheEvict(value = ["coupons"], key = "#criteria.userId")
    @Transactional()
    fun deleteByUserIdAndCouponId(criteria: CouponCriteria.UpdateCouponCommand) {
        userService.findById(criteria.userId)
        couponService.findById(criteria.couponId)
        couponService.deleteUserCouponByUserIdAndCouponId(criteria.userId, criteria.couponId)
        couponService.delete(criteria.couponId)
    }

    @CacheEvict(value = ["coupons"], key = "#userId")
    @Transactional()
    fun deleteAllByUserId(userId: Long) {
        userService.findById(userId)
        val couponIds = couponService.findUserCouponsByUserId(userId).mapNotNull { it.couponId }
        couponService.deleteAll(couponIds)
        couponService.deleteAllUserCouponByUserId(userId)
    }
}
