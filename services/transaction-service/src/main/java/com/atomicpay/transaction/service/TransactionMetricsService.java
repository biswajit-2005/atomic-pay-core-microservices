package com.atomicpay.transaction.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TransactionMetricsService {

    private final Counter transactionsTotal;
    private final Counter transactionsCompletedTotal;
    private final Counter transactionsFailedTotal;
    private final Counter transactionsPendingTotal;
    private final Counter idempotencyHitsTotal;
    private final Counter sagaCompensationTotal;
    private final Counter reconciliationIssuesTotal;
    private final Counter kafkaEventsPublishedTotal;
    private final Timer transactionProcessingTimer;

    public TransactionMetricsService(MeterRegistry registry) {
        this.transactionsTotal = Counter.builder("transactions_total")
                .description("Total number of financial transfer attempts")
                .register(registry);

        this.transactionsCompletedTotal = Counter.builder("transactions_completed_total")
                .description("Total number of successfully completed transfers")
                .register(registry);

        this.transactionsFailedTotal = Counter.builder("transactions_failed_total")
                .description("Total number of failed transfers")
                .register(registry);

        this.transactionsPendingTotal = Counter.builder("transactions_pending_total")
                .description("Total number of pending/reconciliation-required transfers")
                .register(registry);

        this.idempotencyHitsTotal = Counter.builder("idempotency_hits_total")
                .description("Total number of duplicate idempotency key hits")
                .register(registry);

        this.sagaCompensationTotal = Counter.builder("saga_compensation_total")
                .description("Total number of Saga backward compensations executed")
                .register(registry);

        this.reconciliationIssuesTotal = Counter.builder("reconciliation_issues_total")
                .description("Total number of reconciliation issues detected")
                .register(registry);

        this.kafkaEventsPublishedTotal = Counter.builder("kafka_events_published_total")
                .description("Total number of Kafka domain events published")
                .register(registry);

        this.transactionProcessingTimer = Timer.builder("transaction_processing_seconds")
                .description("Execution latency for financial transactions")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    public void incrementTransactionsTotal() { transactionsTotal.increment(); }
    public void incrementTransactionsCompleted() { transactionsCompletedTotal.increment(); }
    public void incrementTransactionsFailed() { transactionsFailedTotal.increment(); }
    public void incrementTransactionsPending() { transactionsPendingTotal.increment(); }
    public void incrementIdempotencyHits() { idempotencyHitsTotal.increment(); }
    public void incrementSagaCompensation() { sagaCompensationTotal.increment(); }
    public void incrementReconciliationIssues() { reconciliationIssuesTotal.increment(); }
    public void incrementKafkaEventsPublished() { kafkaEventsPublishedTotal.increment(); }

    public void recordTransactionLatency(long durationMillis) {
        transactionProcessingTimer.record(durationMillis, TimeUnit.MILLISECONDS);
    }
}
