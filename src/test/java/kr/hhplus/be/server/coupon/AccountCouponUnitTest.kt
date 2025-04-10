package kr.hhplus.be.server.coupon

import kr.hhplus.be.server.domain.coupon.model.AccountCoupon
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.user.model.Account
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class AccountCouponUnitTest {
    
    @Test
    @DisplayName("계정 쿠폰 생성 성공")
    fun createAccountCoupon() {
        // given
        val account = createAccount()
        val coupon = createCoupon()
        
        // when
        val accountCoupon = AccountCoupon.create(account, coupon)
        
        // then
        assertEquals(account, accountCoupon.account)
        assertEquals(coupon, accountCoupon.coupon)
        assertEquals(LocalDateTime.MIN, accountCoupon.issueDate)
        assertFalse(accountCoupon.issued)
        assertFalse(accountCoupon.used)
    }
    
    @Test
    @DisplayName("쿠폰 발행 성공")
    fun issueCouponSuccess() {
        // given
        val account = createAccount()
        val coupon = createCoupon()
        val accountCoupon = AccountCoupon.create(account, coupon)
        
        // when
        val issuedCoupon = accountCoupon.issue(coupon.startDate, coupon.endDate)
        
        // then
        assertTrue(issuedCoupon.isIssued())
        assertFalse(issuedCoupon.isUsed())
        assertTrue(issuedCoupon.issueDate.isAfter(LocalDateTime.MIN))
    }
    
    @Test
    @DisplayName("이미 발행된 쿠폰 재발행 시도 시 예외 발생")
    fun issueAlreadyIssuedCoupon() {
        // given
        val account = createAccount()
        val coupon = createCoupon()
        val accountCoupon = AccountCoupon.create(account, coupon)
        
        // 쿠폰 발행
        accountCoupon.issue(coupon.startDate, coupon.endDate)
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountCoupon.issue(coupon.startDate, coupon.endDate)
        }
        
        assertTrue(exception.message!!.contains("이미 발행된 쿠폰입니다"))
    }
    
    @Test
    @DisplayName("쿠폰 유효기간 외 발행 시도 시 예외 발생")
    fun issueCouponOutOfValidPeriod() {
        // given
        val account = createAccount()
        val futureCoupon = Coupon.create(
            code = "ABCDEF",
            discountRate = 10.0,
            description = "미래 쿠폰",
            startDate = LocalDateTime.now().plusDays(10),
            endDate = LocalDateTime.now().plusDays(20),
            quantity = 50,
            couponType = CouponType.DISCOUNT_PRODUCT
        )
        val accountCoupon = AccountCoupon.create(account, futureCoupon)
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountCoupon.issue(futureCoupon.startDate, futureCoupon.endDate)
        }
        
        assertTrue(exception.message!!.contains("쿠폰 유효 기간이 아닙니다"))
    }
    
    @Test
    @DisplayName("쿠폰 사용 성공")
    fun useCouponSuccess() {
        // given
        val account = createAccount()
        val coupon = createCoupon()
        val accountCoupon = AccountCoupon.create(account, coupon)
        
        // 쿠폰 발행
        accountCoupon.issue(coupon.startDate, coupon.endDate)
        
        // when
        val usedCoupon = accountCoupon.use()
        
        // then
        assertTrue(usedCoupon.isIssued())
        assertTrue(usedCoupon.isUsed())
    }
    
    @Test
    @DisplayName("발행되지 않은 쿠폰 사용 시도 시 예외 발생")
    fun useNotIssuedCoupon() {
        // given
        val account = createAccount()
        val coupon = createCoupon()
        val accountCoupon = AccountCoupon.create(account, coupon)
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountCoupon.use()
        }
        
        assertTrue(exception.message!!.contains("발행되지 않은 쿠폰은 사용할 수 없습니다"))
    }
    
    @Test
    @DisplayName("이미 사용한 쿠폰 재사용 시도 시 예외 발생")
    fun useAlreadyUsedCoupon() {
        // given
        val account = createAccount()
        val coupon = createCoupon()
        val accountCoupon = AccountCoupon.create(account, coupon)
        
        // 쿠폰 발행 및 사용
        accountCoupon.issue(coupon.startDate, coupon.endDate)
        accountCoupon.use()
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            accountCoupon.use()
        }
        
        assertTrue(exception.message!!.contains("이미 사용된 쿠폰입니다"))
    }
    
    private fun createAccount(): Account {
        return Account(
            id = 1L,
            email = "test@example.com",
            password = "password",
            username = "테스트계정",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    private fun createCoupon(): Coupon {
        return Coupon.create(
            code = "ABCDEF",
            discountRate = 10.0,
            description = "테스트 쿠폰",
            startDate = LocalDateTime.now().minusDays(1),
            endDate = LocalDateTime.now().plusDays(10),
            quantity = 50,
            couponType = CouponType.DISCOUNT_PRODUCT
        )
    }
}
