package kr.hhplus.be.server.interfaces.coupon

import kr.hhplus.be.server.application.coupon.CouponCriteria
import kr.hhplus.be.server.application.coupon.CouponFacade
import kr.hhplus.be.server.application.coupon.CouponResult
import kr.hhplus.be.server.interfaces.coupon.api.CouponApi
import kr.hhplus.be.server.interfaces.coupon.CouponRequest
import kr.hhplus.be.server.interfaces.coupon.CouponResponse
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.coupon.service.CouponService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*
import jakarta.validation.Valid
import kr.hhplus.be.server.interfaces.coupon.CouponRequest.IssueFirstComeFirstServedCouponRequest
import kr.hhplus.be.server.interfaces.coupon.CouponResponse.UserCouponResponse

@RestController
@RequestMapping("/api/coupons")
@Validated
class CouponController(
    private val couponFacade: CouponFacade,
    private val couponService: CouponService,
) : CouponApi {

    @GetMapping
    override fun getAllCoupons(): ResponseEntity<List<CouponResponse.Response>> {
        val coupons = couponService.findAll()
        val responses = coupons.map { coupon -> CouponResponse.Response.from(coupon) }
        return ResponseEntity.ok(responses)
    }

    @GetMapping("/{couponId}")
    override fun getCouponById(
        @PathVariable couponId: Long
    ): ResponseEntity<CouponResponse.Response> {
        val coupon = couponService.findById(couponId)
        val response = CouponResponse.Response.from(coupon)
        return ResponseEntity.ok(response)
    }

    @PostMapping
    override fun createCoupon(
        @Valid @RequestBody request: CouponRequest.CreateCouponRequest
    ): ResponseEntity<CouponResponse.Response> {
        val criteria = CouponCriteria.CreateUserCouponCommand(
            userId = 0, // 쿠폰만 생성할 때는 사용자 ID가 필요 없음
            code = request.code,
            couponType = CouponType.DISCOUNT_ORDER,
            discountRate = request.discountRate,
            description = request.description,
            startDate = request.startDate,
            endDate = request.endDate,
            quantity = request.quantity,
            userCouponQuantity = 0 // 쿠폰만 생성할 때는 사용자 쿠폰 수량이 필요 없음
        )
        
        val createCommand = criteria.toCreateCouponCommand()
        val createdCoupon = couponService.create(createCommand)
        
        val response = CouponResponse.Response.from(createdCoupon)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{couponId}")
    override fun updateCoupon(
        @PathVariable couponId: Long,
        @Valid @RequestBody request: CouponRequest.UpdateCouponRequest
    ): ResponseEntity<CouponResponse.Response> {
        val coupon = couponService.findById(couponId)
        
        val updateCommand = kr.hhplus.be.server.domain.coupon.service.CouponCommand.UpdateCouponCommand(
            id = couponId,
            discountRate = request.discountRate,
            description = request.description,
            startDate = request.startDate,
            endDate = request.endDate,
            quantity = request.quantity
        )
        
        val updatedCoupon = couponService.update(updateCommand)
        
        val response = CouponResponse.Response.from(updatedCoupon)
        
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{couponId}")
    override fun deleteCoupon(
        @PathVariable couponId: Long
    ): ResponseEntity<Void> {
        // 해당 쿠폰이 발급된 적이 있는지 확인
        val userCoupons = couponService.findUserCouponByCouponId(couponId)
        
        if (userCoupons.isNotEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
        
        couponService.delete(couponId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{couponId}/issue")
    override fun issueCoupon(
        @PathVariable couponId: Long,
        @Valid @RequestBody request: CouponRequest.CouponIssueRequest
    ): ResponseEntity<CouponResponse.AccountCouponResponse> {
        val userId = request.accountId
        
        val updateCriteria = CouponCriteria.UpdateCouponCommand(
            userId = userId,
            couponId = couponId
        )
        
        val userCouponResult = try {
            // 이미 발급된 쿠폰이 있는지 확인
            couponFacade.findByUserIdAndCouponId(userId, couponId)
        } catch (e: IllegalArgumentException) {
            // 발급된 쿠폰이 없으면 새로 발급
            val criteria = CouponCriteria.CreateUserCouponCommand(
                userId = userId,
                code = UUID.randomUUID().toString(),
                couponType = CouponType.DISCOUNT_ORDER,
                discountRate = 0.0, // 실제 값은 쿠폰 서비스에서 설정
                description = "",  // 실제 값은 쿠폰 서비스에서 설정
                startDate = LocalDateTime.now(),
                endDate = LocalDateTime.now().plusDays(30),
                quantity = 1,
                userCouponQuantity = 1
            )
            
            val userCoupon = couponFacade.create(criteria)
            val coupon = couponService.findById(couponId)
            
            CouponResult.UserCouponResult.from(userCoupon, coupon)
        }
        
        // 쿠폰 발급 처리
        couponFacade.issue(updateCriteria)
        
        val response = CouponResponse.AccountCouponResponse.from(userCouponResult)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/use/account/{accountId}/coupons/{accountCouponId}")
    override fun useCoupon(
        @PathVariable accountId: Long,
        @PathVariable accountCouponId: Long
    ): ResponseEntity<CouponResponse.AccountCouponResponse> {
        val userCoupon = couponService.findUserCouponById(accountCouponId)
        
        // 이미 사용된 쿠폰인지 확인
        if (userCoupon.isUsed()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
        
        // 쿠폰이 해당 유저의 것인지 확인
        if (userCoupon.userId != accountId) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
        
        // 쿠폰 사용 처리
        couponService.useUserCoupon(accountCouponId)
        
        // 업데이트된 쿠폰 정보 조회
        val updatedUserCoupon = couponService.findUserCouponById(accountCouponId)
        val coupon = couponService.findById(updatedUserCoupon.couponId)
        
        // CouponFacade를 통해 UserCouponResult 획득
        val userCouponResult = couponFacade.findByUserIdAndCouponId(accountId, coupon.id!!)
        
        val response = CouponResponse.AccountCouponResponse.from(userCouponResult)
        
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/account/{accountId}/coupons")
    override fun getUserCoupons(
        @PathVariable accountId: Long
    ): ResponseEntity<List<CouponResponse.AccountCouponResponse>> {
        val userCouponResults = couponFacade.findByUserId(accountId)
        
        val responses = userCouponResults.map { result ->
            CouponResponse.AccountCouponResponse.from(result)
        }
        
        return ResponseEntity.ok(responses)
    }

    /**
     * 비관적 락을 사용한 선착순 쿠폰 발급 API
     */
    @PostMapping("/users/{userId}/first-come-first-served")
    fun issueFirstComeFirstServedCoupon(
        @PathVariable userId: Long,
        @RequestBody request: IssueFirstComeFirstServedCouponRequest
    ): UserCouponResponse {
        val userCoupon = couponService.issueFirstComeFirstServedCoupon(userId, request.couponCode)
        return UserCouponResponse.from(userCoupon)
    }
}
