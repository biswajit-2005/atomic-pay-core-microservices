package com.atomicpay.transaction.service;

import com.atomicpay.transaction.domain.IdempotencyStatus;
import com.atomicpay.transaction.entity.IdempotencyRecordEntity;
import com.atomicpay.transaction.exception.ErrorCode;
import com.atomicpay.transaction.exception.TransactionException;
import com.atomicpay.transaction.repository.IdempotencyRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);
    private static final String REDIS_IDEMPOTENCY_PREFIX = "idempotency:";

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final StringRedisTemplate redisTemplate;

    public IdempotencyService(IdempotencyRecordRepository idempotencyRecordRepository, StringRedisTemplate redisTemplate) {
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.redisTemplate = redisTemplate;
    }

    public String calculatePayloadHash(Object payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    @Transactional
    public Optional<IdempotencyRecordEntity> checkExistingRecord(UUID userId, String idempotencyKey, String currentPayloadHash) {
        Optional<IdempotencyRecordEntity> existingInDb = idempotencyRecordRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);

        if (existingInDb.isPresent()) {
            IdempotencyRecordEntity record = existingInDb.get();
            if (!record.getRequestHash().equals(currentPayloadHash)) {
                log.warn("Idempotency key reuse detected for user {} and key {}: stored hash {} != current hash {}",
                        userId, idempotencyKey, record.getRequestHash(), currentPayloadHash);
                throw new TransactionException(ErrorCode.IDEMPOTENCY_KEY_REUSED);
            }
            log.info("Idempotency match found for user {} and key {}", userId, idempotencyKey);
            return Optional.of(record);
        }

        return Optional.empty();
    }

    @Transactional
    public IdempotencyRecordEntity createRecord(UUID userId, String idempotencyKey, String payloadHash) {
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(1);

        IdempotencyRecordEntity record = IdempotencyRecordEntity.builder()
                .userId(userId)
                .idempotencyKey(idempotencyKey)
                .requestHash(payloadHash)
                .status(IdempotencyStatus.PROCESSING)
                .expiresAt(expiresAt)
                .build();

        IdempotencyRecordEntity saved = idempotencyRecordRepository.save(record);

        // Redis Fast-path
        try {
            String redisKey = REDIS_IDEMPOTENCY_PREFIX + userId + ":" + idempotencyKey;
            redisTemplate.opsForValue().set(redisKey, payloadHash, Duration.ofHours(24));
        } catch (Exception e) {
            log.warn("Failed to write fast-path idempotency record to Redis: {}", e.getMessage());
        }

        return saved;
    }

    @Transactional
    public void markCompleted(IdempotencyRecordEntity record, UUID transactionId, int responseStatus, String responseBody) {
        record.setTransactionId(transactionId);
        record.setResponseStatus(responseStatus);
        record.setResponseBody(responseBody);
        record.setStatus(IdempotencyStatus.COMPLETED);
        idempotencyRecordRepository.save(record);
    }
}
