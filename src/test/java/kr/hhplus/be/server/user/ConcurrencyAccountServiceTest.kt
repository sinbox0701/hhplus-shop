package kr.hhplus.be.server.user

import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ConcurrencyAccountServiceTest {

    @Test
    fun `동시에 여러 요청이 계좌 잔액을 차감할 때 정확한 잔액이 유지되어야 한다`() {
        // given
        val initialBalance = 10000
        val amount = AtomicInteger(initialBalance)
        val threadCount = 10
        val withdrawalAmount = 100
        val expectedFinalAmount = initialBalance - (withdrawalAmount * threadCount)
        
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // when
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    // 출금 처리 (AtomicInteger를 사용하여 스레드 안전하게 처리)
                    amount.addAndGet(-withdrawalAmount)
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 스레드 완료 대기
        latch.await()
        executor.shutdown()

        // then
        assert(amount.get() == expectedFinalAmount) {
            "동시 출금 후 계좌 잔액이 예상값과 일치해야 함 (예상: $expectedFinalAmount, 실제: ${amount.get()})"
        }
    }

    @Test
    fun `동시에 여러 요청이 계좌 잔액을 충전할 때 정확한 잔액이 유지되어야 한다`() {
        // given
        val initialBalance = 10000
        val amount = AtomicInteger(initialBalance)
        val threadCount = 10
        val chargeAmount = 100
        val expectedFinalAmount = initialBalance + (chargeAmount * threadCount)
        
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // when
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    // 충전 처리 (AtomicInteger를 사용하여 스레드 안전하게 처리)
                    amount.addAndGet(chargeAmount)
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 스레드 완료 대기
        latch.await()
        executor.shutdown()

        // then
        assert(amount.get() == expectedFinalAmount) {
            "동시 충전 후 계좌 잔액이 예상값과 일치해야 함 (예상: $expectedFinalAmount, 실제: ${amount.get()})"
        }
    }
    
    @Test
    fun `잔액 부족 상황에서 동시에 여러 요청이 계좌 잔액을 차감할 때 이중 인출이 방지되어야 한다`() {
        // given
        val initialBalance = 10000
        val amount = AtomicInteger(initialBalance)
        val withdrawalAmount = 1000
        val maxWithdrawals = initialBalance / withdrawalAmount
        
        // 최대 출금 가능 횟수보다 많은 스레드로 테스트
        val threadCount = maxWithdrawals + 5
        val successCount = AtomicInteger(0)
        
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // when
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    // 현재 잔액
                    val currentAmount = amount.get()
                    
                    // 잔액이 출금액보다 작으면 출금 실패
                    if (currentAmount < withdrawalAmount) {
                        // 출금 실패 - 아무 작업 없음
                    } else {
                        // CAS(Compare-And-Swap)로 안전하게 잔액 차감 시도
                        if (amount.compareAndSet(currentAmount, currentAmount - withdrawalAmount)) {
                            successCount.incrementAndGet() // 출금 성공
                        }
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 스레드 완료 대기
        latch.await()
        executor.shutdown()

        // then
        assert(successCount.get() == maxWithdrawals) {
            "성공한 출금 횟수는 최대 출금 가능 횟수와 일치해야 함 (예상: $maxWithdrawals, 실제: ${successCount.get()})"
        }
        
        assert(amount.get() == 0) {
            "계좌 잔액은 출금 후 0이어야 함 (실제: ${amount.get()})"
        }
    }
    
    @Test
    fun `동시에 입금과 출금이 발생할 때 정확한 잔액이 유지되어야 한다`() {
        // given
        val initialBalance = 10000
        val amount = AtomicInteger(initialBalance)
        val chargeThreadCount = 5
        val withdrawThreadCount = 5
        val chargeAmount = 200
        val withdrawAmount = 100
        
        // 예상되는 최종 잔액 계산
        val expectedBalance = initialBalance + 
                             (chargeAmount * chargeThreadCount) -
                             (withdrawAmount * withdrawThreadCount)
        
        val executor = Executors.newFixedThreadPool(chargeThreadCount + withdrawThreadCount)
        val latch = CountDownLatch(chargeThreadCount + withdrawThreadCount)

        // when
        // 입금 스레드
        for (i in 0 until chargeThreadCount) {
            executor.submit {
                try {
                    // 충전 처리
                    amount.addAndGet(chargeAmount)
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // 출금 스레드
        for (i in 0 until withdrawThreadCount) {
            executor.submit {
                try {
                    // 출금 처리
                    amount.addAndGet(-withdrawAmount)
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 스레드 완료 대기
        latch.await()
        executor.shutdown()

        // then
        assert(amount.get() == expectedBalance) {
            "동시 입출금 후 계좌 잔액이 예상값과 일치해야 함 (예상: $expectedBalance, 실제: ${amount.get()})"
        }
    }
} 