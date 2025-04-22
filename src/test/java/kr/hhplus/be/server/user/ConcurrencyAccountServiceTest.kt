package kr.hhplus.be.server.user

import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.user.repository.AccountRepository
import kr.hhplus.be.server.domain.user.service.AccountCommand
import kr.hhplus.be.server.domain.user.service.AccountService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
@ActiveProfiles("test")
class ConcurrencyAccountServiceTest {

    @Autowired
    private lateinit var accountService: AccountService

    @Autowired
    private lateinit var accountRepository: AccountRepository

    private lateinit var testAccount: Account

    @BeforeEach
    fun setup() {
        // 테스트용 계좌 생성 (초기 잔액 10000)
        val command = AccountCommand.CreateAccountCommand(userId = 1L, amount = 10000.0)
        testAccount = accountService.create(command)
    }

    /**
     * 동시에 여러 스레드에서 계좌 잔액을 차감하는 테스트
     * 동시성 문제가 없다면 최종 잔액은 초기값 - (차감액 * 스레드수)와 일치해야 함
     */
    @Test
    fun `동시에 여러 요청이 계좌 잔액을 차감할 때 정확한 잔액이 유지되어야 한다`() {
        // given
        val threadCount = 10
        val withdrawalAmount = 100.0
        val expectedFinalAmount = testAccount.amount - (withdrawalAmount * threadCount)
        
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // when
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    val command = AccountCommand.UpdateAccountCommand(
                        id = testAccount.id!!,
                        amount = withdrawalAmount
                    )
                    accountService.withdraw(command)
                } catch (e: Exception) {
                    // 실패한 경우도 카운트 감소
                    println("Error in thread $i: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 스레드 완료 대기
        latch.await()
        executor.shutdown()

        // then
        val updatedAccount = accountService.findById(testAccount.id!!)
        assertEquals(expectedFinalAmount, updatedAccount.amount, 
            "동시 출금 후 계좌 잔액이 예상값과 일치해야 함")
    }

    /**
     * 동시에 여러 스레드에서 계좌 잔액을 충전하는 테스트
     * 동시성 문제가 없다면 최종 잔액은 초기값 + (충전액 * 스레드수)와 일치해야 함
     */
    @Test
    fun `동시에 여러 요청이 계좌 잔액을 충전할 때 정확한 잔액이 유지되어야 한다`() {
        // given
        val threadCount = 10
        val chargeAmount = 100.0
        val expectedFinalAmount = testAccount.amount + (chargeAmount * threadCount)
        
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // when
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    val command = AccountCommand.UpdateAccountCommand(
                        id = testAccount.id!!,
                        amount = chargeAmount
                    )
                    accountService.charge(command)
                } catch (e: Exception) {
                    println("Error in thread $i: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 스레드 완료 대기
        latch.await()
        executor.shutdown()

        // then
        val updatedAccount = accountService.findById(testAccount.id!!)
        assertEquals(expectedFinalAmount, updatedAccount.amount, 
            "동시 충전 후 계좌 잔액이 예상값과 일치해야 함")
    }
} 