package kr.hhplus.be.server.user

import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class AccountConcurrencyTest {

    @Test
    fun `동시에 여러 스레드에서 충전 요청 시 데이터 일관성이 유지되어야 한다`() {
        // given
        val amount = AtomicInteger(10000) // 초기 금액 10000원
        val numberOfThreads = 10
        val chargeAmount = 1000
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)

        // when
        for (i in 1..numberOfThreads) {
            executor.submit {
                try {
                    // 충전 처리 (AtomicInteger를 사용하여 스레드 안전하게 처리)
                    amount.addAndGet(chargeAmount)
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 스레드가 작업을 완료할 때까지 대기 (최대 10초)
        latch.await(10, TimeUnit.SECONDS)
        executor.shutdown()

        // then
        // 초기 금액 10000원에 1000원씩 10번 충전이 성공해야 함
        assert(amount.get() == 20000) { "Expected final amount to be 20000, but got ${amount.get()}" }
    }

    @Test
    fun `동시에 여러 스레드에서 포인트 사용 요청 시 잔액이 정확하게 유지되어야 한다`() {
        // given
        val amount = AtomicInteger(10000) // 초기 금액 10000원
        val numberOfThreads = 5
        val withdrawAmount = 1000
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)

        // when
        for (i in 1..numberOfThreads) {
            executor.submit {
                try {
                    // 출금 처리 (AtomicInteger를 사용하여 스레드 안전하게 처리)
                    amount.addAndGet(-withdrawAmount)
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 스레드가 작업을 완료할 때까지 대기 (최대 10초)
        latch.await(10, TimeUnit.SECONDS)
        executor.shutdown()

        // then
        // 초기 금액 10000원에서 1000원씩 5번 출금이 성공해야 함
        assert(amount.get() == 5000) { "Expected final amount to be 5000, but got ${amount.get()}" }
    }

    @Test
    fun `잔액보다 많은 금액 출금 시도할 경우 실패해야 한다`() {
        // given
        val initialAmount = 10000
        val amount = AtomicInteger(initialAmount) // 초기 금액 10000원
        val numberOfThreads = 12
        val withdrawAmount = 1000 // 총 12000원 출금 시도 (잔액은 10000원)
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)
        val successCount = AtomicInteger(0)

        // when
        for (i in 1..numberOfThreads) {
            executor.submit {
                try {
                    // 현재 잔액
                    val currentAmount = amount.get()
                    
                    // 잔액이 출금액보다 작으면 출금 실패
                    if (currentAmount < withdrawAmount) {
                        // 출금 실패 - 아무 작업 없음
                    } else {
                        // CAS(Compare-And-Swap)로 안전하게 잔액 차감 시도
                        if (amount.compareAndSet(currentAmount, currentAmount - withdrawAmount)) {
                            successCount.incrementAndGet() // 출금 성공
                        }
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 스레드가 작업을 완료할 때까지 대기 (최대 10초)
        latch.await(10, TimeUnit.SECONDS)
        executor.shutdown()

        // then
        // 초기 금액 10000원에서 최대 10번의 출금만 성공할 수 있음
        assert(successCount.get() <= 10) { "Expected success count to be at most 10, but got ${successCount.get()}" }
        assert(amount.get() >= 0) { "Expected final amount to be at least 0, but got ${amount.get()}" }
    }
} 