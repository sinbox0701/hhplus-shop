package kr.hhplus.be.server.shared.lock

import java.util.concurrent.TimeUnit

/**
 * 분산 락 관련 유틸리티 함수들을 제공하는 객체
 */
object DistributedLockUtils {

    /**
     * 여러 락을 안전하게 순서대로 획득합니다.
     * 데드락 방지를 위해 모든 락 획득 요청에서 동일한 락 순서를 사용해야 합니다.
     *
     * @param lockManager 분산 락 관리자
     * @param keys 획득할 락 키 목록 (이미 정렬된 상태여야 함)
     * @param timeouts 각 락의 타임아웃 (기본값: 10초)
     * @param timeUnit 타임아웃 시간 단위 (기본값: 초)
     * @param action 모든 락을 획득한 후 실행할 액션
     * @return 액션의 실행 결과
     */
    fun <T> withOrderedLocks(
        lockManager: DistributedLockManager,
        keys: List<String>,
        timeouts: List<Long> = List(keys.size) { 10L },
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        action: () -> T
    ): T {
        require(keys.isNotEmpty()) { "락 키 목록은 비어있을 수 없습니다." }
        require(keys.size == timeouts.size) { "락 키 목록과 타임아웃 목록의 크기가 일치해야 합니다." }
        
        return acquireLocksRecursively(lockManager, keys, timeouts, timeUnit, 0, action)
    }
    
    /**
     * 재귀적으로 지정된 순서대로 락을 획득합니다.
     *
     * @param lockManager 분산 락 관리자
     * @param keys 획득할 락 키 목록
     * @param timeouts 각 락의 타임아웃
     * @param timeUnit 타임아웃 시간 단위
     * @param index 현재 처리할 키 인덱스
     * @param action 모든 락이 획득된 후 실행할 액션
     * @return 액션의 실행 결과
     */
    private fun <T> acquireLocksRecursively(
        lockManager: DistributedLockManager,
        keys: List<String>,
        timeouts: List<Long>,
        timeUnit: TimeUnit,
        index: Int,
        action: () -> T
    ): T {
        // 모든 락을 획득했다면 액션 실행
        if (index >= keys.size) {
            return action()
        }
        
        // 현재 키로 락 획득
        return lockManager.executeWithLock(keys[index], timeouts[index], timeUnit) {
            // 다음 락 획득으로 재귀 호출
            acquireLocksRecursively(lockManager, keys, timeouts, timeUnit, index + 1, action)
        }
    }
    
    /**
     * 데드락 방지를 위해 여러 리소스에 대한 락 키를 일관된 순서로 정렬합니다.
     *
     * @param keys 정렬이 필요한 락 키 목록
     * @return 정렬된 락 키 목록
     */
    fun sortLockKeys(keys: List<String>): List<String> {
        return keys.sorted()
    }
}