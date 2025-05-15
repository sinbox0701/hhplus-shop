package kr.hhplus.be.server.shared.transaction

import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import java.util.function.Supplier

/**
 * 트랜잭션 관리를 위한 헬퍼 클래스
 * 트랜잭션 범위에서 코드 실행을 간소화하는 유틸리티 메소드 제공
 */
@Component
class TransactionHelper(private val transactionManager: PlatformTransactionManager) {

    /**
     * 기본 트랜잭션 설정으로 코드 블록을 실행합니다.
     *
     * @param action 트랜잭션 내에서 실행할 작업
     * @return 작업 실행 결과
     */
    fun <T> executeInTransaction(action: () -> T): T {
        val transactionTemplate = TransactionTemplate(transactionManager)
        return transactionTemplate.execute { action() }!!
    }

    /**
     * 읽기 전용 트랜잭션으로 코드 블록을 실행합니다.
     *
     * @param action 트랜잭션 내에서 실행할 작업
     * @return 작업 실행 결과
     */
    fun <T> executeInReadOnlyTransaction(action: () -> T): T {
        val transactionTemplate = TransactionTemplate(transactionManager)
        transactionTemplate.isReadOnly = true
        return transactionTemplate.execute { action() }!!
    }

    /**
     * 커스텀 트랜잭션 설정으로 코드 블록을 실행합니다.
     *
     * @param isolationLevel 트랜잭션 격리 수준 (기본값: 기본 설정 사용)
     * @param propagation 트랜잭션 전파 방식 (기본값: 기본 설정 사용)
     * @param timeout 트랜잭션 타임아웃 (기본값: 기본 설정 사용)
     * @param readOnly 읽기 전용 여부 (기본값: false)
     * @param action 트랜잭션 내에서 실행할 작업
     * @return 작업 실행 결과
     */
    fun <T> executeWithTransactionOptions(
        isolationLevel: Int = TransactionDefinition.ISOLATION_DEFAULT,
        propagation: Int = TransactionDefinition.PROPAGATION_REQUIRED,
        timeout: Int = TransactionDefinition.TIMEOUT_DEFAULT,
        readOnly: Boolean = false,
        action: () -> T
    ): T {
        val transactionTemplate = TransactionTemplate(transactionManager)
        transactionTemplate.isolationLevel = isolationLevel
        transactionTemplate.propagationBehavior = propagation
        transactionTemplate.timeout = timeout
        transactionTemplate.isReadOnly = readOnly
        
        return transactionTemplate.execute { action() }!!
    }
    
    /**
     * 새로운 트랜잭션으로 코드 블록을 실행합니다 (기존 트랜잭션과 독립적).
     *
     * @param action 트랜잭션 내에서 실행할 작업
     * @return 작업 실행 결과
     */
    fun <T> executeInNewTransaction(action: () -> T): T {
        val transactionTemplate = TransactionTemplate(transactionManager)
        transactionTemplate.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
        
        return transactionTemplate.execute { action() }!!
    }
} 