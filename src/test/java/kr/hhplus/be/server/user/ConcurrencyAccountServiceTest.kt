package kr.hhplus.be.server.user

import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.user.repository.AccountRepository
import kr.hhplus.be.server.domain.user.service.AccountCommand
import kr.hhplus.be.server.domain.user.service.AccountService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

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
    
    /**
     * 잔액 부족 상황에서 동시에 여러 출금 요청이 발생할 때 이중 인출 방지 테스트
     * 동시성 처리가 올바르다면 잔액 이상의 출금은 발생하지 않아야 함
     */
    @Test
    fun `잔액 부족 상황에서 동시에 여러 요청이 계좌 잔액을 차감할 때 이중 인출이 방지되어야 한다`() {
        // given
        // 정확한 출금 가능 횟수 계산
        val initialBalance = testAccount.amount
        val withdrawalAmount = 1000.0
        val maxWithdrawals = (initialBalance / withdrawalAmount).toInt()
        
        // 최대 출금 가능 횟수보다 많은 스레드로 테스트
        val threadCount = maxWithdrawals + 5
        val successCount = AtomicInteger(0)
        
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
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    // 잔액 부족 예외는 정상적으로 처리
                    println("Expected error in thread $i: ${e.message}")
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
        
        // 성공한 출금 횟수는 최대 출금 가능 횟수를 초과하지 않아야 함
        assertEquals(maxWithdrawals, successCount.get(), 
            "성공한 출금 횟수는 최대 출금 가능 횟수와 일치해야 함")
        
        // 계좌 잔액은 출금 후 0에 가까워야 함 (약간의 소수점 오차 허용)
        assertTrue(updatedAccount.amount < withdrawalAmount, 
            "계좌 잔액은 출금 단위보다 작아야 함")
    }
    
    /**
     * 동시에 입금과 출금이 발생할 때 정확한 잔액 관리 테스트
     */
    @Test
    fun `동시에 입금과 출금이 발생할 때 정확한 잔액이 유지되어야 한다`() {
        // given
        val chargeThreadCount = 5
        val withdrawThreadCount = 5
        val chargeAmount = 200.0
        val withdrawAmount = 100.0
        
        // 예상되는 최종 잔액 계산
        val expectedBalance = testAccount.amount + 
                             (chargeAmount * chargeThreadCount) -
                             (withdrawAmount * withdrawThreadCount)
        
        val executor = Executors.newFixedThreadPool(chargeThreadCount + withdrawThreadCount)
        val latch = CountDownLatch(chargeThreadCount + withdrawThreadCount)

        // when
        // 입금 스레드
        for (i in 0 until chargeThreadCount) {
            executor.submit {
                try {
                    val command = AccountCommand.UpdateAccountCommand(
                        id = testAccount.id!!,
                        amount = chargeAmount
                    )
                    accountService.charge(command)
                } catch (e: Exception) {
                    println("Charge error in thread $i: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // 출금 스레드
        for (i in 0 until withdrawThreadCount) {
            executor.submit {
                try {
                    val command = AccountCommand.UpdateAccountCommand(
                        id = testAccount.id!!,
                        amount = withdrawAmount
                    )
                    accountService.withdraw(command)
                } catch (e: Exception) {
                    println("Withdraw error in thread $i: ${e.message}")
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
        assertEquals(expectedBalance, updatedAccount.amount, 0.001,
            "동시 입출금 후 계좌 잔액이 예상값과 일치해야 함")
    }
} 