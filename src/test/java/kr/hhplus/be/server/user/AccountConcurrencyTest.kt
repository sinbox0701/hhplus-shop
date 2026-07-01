package kr.hhplus.be.server.user

import kr.hhplus.be.server.application.user.UserAccountFacade
import kr.hhplus.be.server.application.user.UserCriteria
import kr.hhplus.be.server.domain.user.service.AccountCommand
import kr.hhplus.be.server.domain.user.service.AccountService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 실제 Spring 컨텍스트를 사용한 계좌 충전/출금 동시성 테스트.
 *
 * 실제 UserAccountFacade / AccountService + 실제 JPA 리포지토리(AccountRepositoryImpl)를 사용하여
 * H2(MODE=MySQL) 위에서 여러 스레드가 동시에 같은 계좌에 대해 충전/출금을 시도한다.
 *
 * 프로덕션 가드는 AccountEntity의 @Version 필드를 이용한 낙관적 락이며,
 * AccountService.chargeWithOptimisticLock / withdrawWithOptimisticLock 이 충돌 시
 * OptimisticLockingFailureException 을 잡아 재조회 후 재시도한다.
 *
 * 만약 @Version(낙관적 락)이 제거되면 "읽기 -> 계산 -> 쓰기" 사이의 경쟁 조건으로 인해
 * lost update가 발생하여 최종 잔액이 기대값보다 작게 나온다. 이 테스트는 최종 잔액이
 * 정확히 기대값과 일치할 것을 요구하므로, 락이 제거되면 실패한다.
 */
@SpringBootTest
@ActiveProfiles("test")
class AccountConcurrencyTest {

    @Autowired
    private lateinit var userAccountFacade: UserAccountFacade

    @Autowired
    private lateinit var accountService: AccountService

    private var userId: Long = 0L
    private var accountId: Long = 0L

    @BeforeEach
    fun setup() {
        val unique = System.nanoTime() % 1_000_000
        val criteria = UserCriteria.CreateUserCriteria(
            name = "동시성 테스트 사용자",
            email = "concurrency-test-$unique@test.com",
            loginId = "cc${unique.toString().takeLast(6)}",
            password = "test1234"
        )
        val user = userAccountFacade.createUserWithAccount(criteria)
        userId = user.id!!
        val account = accountService.findByUserId(userId)
        accountId = account.id!!

        // 계좌는 0원으로 생성되므로, 동시성 테스트를 위한 초기 잔액을 충전해둔다.
        accountService.chargeWithOptimisticLock(
            AccountCommand.UpdateAccountCommand(id = accountId, amount = BigDecimal("10000"))
        )
    }

    @Test
    fun `동시에 여러 스레드에서 충전 요청 시 낙관적 락으로 데이터 일관성이 유지되어야 한다`() {
        // given
        val numberOfThreads = 10
        val chargeAmount = BigDecimal("1000")
        val initialBalance = accountService.findById(accountId).amount
        val expectedFinalBalance = initialBalance.add(chargeAmount.multiply(BigDecimal(numberOfThreads)))

        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val exceptionList = mutableListOf<String>()

        // when
        for (i in 1..numberOfThreads) {
            executor.submit {
                try {
                    accountService.chargeWithOptimisticLock(
                        AccountCommand.UpdateAccountCommand(id = accountId, amount = chargeAmount)
                    )
                } catch (e: Exception) {
                    synchronized(exceptionList) {
                        exceptionList.add("충전 실패: ${e.message}")
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS))
        executor.shutdown()

        // then
        exceptionList.forEach(::println)

        val finalAccount = accountService.findById(accountId)
        // 낙관적 락 + 재시도 덕분에 lost update 없이 정확히 10번의 충전이 모두 반영되어야 함
        assertEquals(0, expectedFinalBalance.compareTo(finalAccount.amount)) {
            "예상 잔액($expectedFinalBalance)과 실제 잔액(${finalAccount.amount})이 일치해야 합니다"
        }
    }

    @Test
    fun `동시에 여러 스레드에서 출금 요청 시 낙관적 락으로 데이터 일관성이 유지되어야 한다`() {
        // given
        val numberOfThreads = 5
        val withdrawAmount = BigDecimal("1000")
        val initialBalance = accountService.findById(accountId).amount
        val expectedFinalBalance = initialBalance.subtract(withdrawAmount.multiply(BigDecimal(numberOfThreads)))

        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val exceptionList = mutableListOf<String>()

        // when
        for (i in 1..numberOfThreads) {
            executor.submit {
                try {
                    accountService.withdrawWithOptimisticLock(
                        AccountCommand.UpdateAccountCommand(id = accountId, amount = withdrawAmount)
                    )
                } catch (e: Exception) {
                    synchronized(exceptionList) {
                        exceptionList.add("출금 실패: ${e.message}")
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS))
        executor.shutdown()

        // then
        exceptionList.forEach(::println)

        val finalAccount = accountService.findById(accountId)
        // 낙관적 락 + 재시도 덕분에 lost update 없이 정확히 5번의 출금이 모두 반영되어야 함
        assertEquals(0, expectedFinalBalance.compareTo(finalAccount.amount)) {
            "예상 잔액($expectedFinalBalance)과 실제 잔액(${finalAccount.amount})이 일치해야 합니다"
        }
    }

    @Test
    fun `잔액보다 많은 금액을 동시에 출금 시도할 경우 잔액이 음수가 되지 않아야 한다`() {
        // given: 잔액 10000원, 12번의 1000원 출금을 동시에 시도 (최대 10번만 성공 가능)
        val numberOfThreads = 12
        val withdrawAmount = BigDecimal("1000")

        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        // when
        for (i in 1..numberOfThreads) {
            executor.submit {
                try {
                    accountService.withdrawWithOptimisticLock(
                        AccountCommand.UpdateAccountCommand(id = accountId, amount = withdrawAmount)
                    )
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS))
        executor.shutdown()

        // then
        val finalAccount = accountService.findById(accountId)
        assertEquals(10, successCount.get())
        assertEquals(2, failCount.get())
        assertTrue(finalAccount.amount.compareTo(BigDecimal.ZERO) >= 0) {
            "최종 잔액은 0 이상이어야 합니다 (실제: ${finalAccount.amount})"
        }
        assertEquals(0, BigDecimal.ZERO.compareTo(finalAccount.amount)) {
            "정확히 10번만 성공했으므로 잔액은 0이어야 합니다 (실제: ${finalAccount.amount})"
        }
    }
}
