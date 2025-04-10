package kr.hhplus.be.server.domain.coupon.service

import kr.hhplus.be.server.domain.coupon.model.AccountCoupon
import kr.hhplus.be.server.domain.coupon.repository.AccountCouponRepository
import kr.hhplus.be.server.domain.coupon.service.AccountCouponCommand
import org.springframework.stereotype.Service

@Service
class AccountCouponService(private val accountCouponRepository: AccountCouponRepository) {
    fun findById(id: Long): AccountCoupon {
        return accountCouponRepository.findById(id)
            ?: throw IllegalArgumentException("AccountCoupon not found with id: $id")
    }

    fun findByAccountId(accountId: Long): List<AccountCoupon> {
        return accountCouponRepository.findByAccountId(accountId)
    }

    fun findByCouponId(couponId: Long): List<AccountCoupon> {
        return accountCouponRepository.findByCouponId(couponId)
    }

    fun findByAccountIdAndCouponId(accountId: Long, couponId: Long): AccountCoupon? {
        return accountCouponRepository.findByAccountIdAndCouponId(accountId, couponId)
    }

    fun issue(command: AccountCouponCommand.IssueCouponCommand) {
        val accountCoupon = findById(command.id)
        accountCoupon.issue(command.couponStartDate, command.couponEndDate)
        accountCouponRepository.save(accountCoupon)
    }

    fun use(id: Long) {
        val accountCoupon = findById(id)
        accountCoupon.use()
        accountCouponRepository.save(accountCoupon)
    }

    fun delete(id: Long) {
        accountCouponRepository.delete(id)
    }

    fun deleteAllByAccountId(accountId: Long) {
        val accountCoupons = accountCouponRepository.findByAccountId(accountId)
        accountCoupons.forEach { accountCoupon ->
            accountCouponRepository.delete(accountCoupon.id!!)
        }
    }

    fun deleteAllByCouponId(couponId: Long) {
        val accountCoupons = accountCouponRepository.findByCouponId(couponId)
        accountCoupons.forEach { accountCoupon ->
            accountCouponRepository.delete(accountCoupon.id!!)
        }
    }
}