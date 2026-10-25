package com.app.gym.otp;

import java.sql.Timestamp;
import java.time.Instant;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GymAuthRateLimiter {

    private final JdbcTemplate jdbc;

    public GymAuthRateLimiter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /*
     * Uses a separate transaction so the request count survives
     * even when the surrounding authentication operation fails.
     *
     * GymOtpFailure must not roll back the recorded attempt.
     */
    @Transactional(
        propagation = Propagation.REQUIRES_NEW,
        noRollbackFor = GymOtpFailure.class
    )
    public void check(
            String operation,
            String remoteAddress,
            int limit,
            int seconds) {

        long bucket =
                Instant.now().getEpochSecond() / seconds;

        String bucketKey = GymOtpSecrets.hash(
                operation
                        + ":"
                        + remoteAddress
                        + ":"
                        + bucket
        );

        Timestamp expiresAt = Timestamp.from(
                Instant.ofEpochSecond(
                        (bucket + 2) * seconds
                )
        );

        try {

            jdbc.update(
                    "INSERT INTO GYM_AUTH_RATE_BUCKET "
                            + "(BUCKET_KEY, HITS, EXPIRES_AT) "
                            + "VALUES (?, 1, ?)",
                    bucketKey,
                    expiresAt
            );

        } catch (DuplicateKeyException exception) {

            /*
             * Another request has already created this bucket.
             * Increment its count atomically.
             */
            jdbc.update(
                    "UPDATE GYM_AUTH_RATE_BUCKET "
                            + "SET HITS = HITS + 1 "
                            + "WHERE BUCKET_KEY = ?",
                    bucketKey
            );
        }

        Integer hits = jdbc.queryForObject(
                "SELECT HITS "
                        + "FROM GYM_AUTH_RATE_BUCKET "
                        + "WHERE BUCKET_KEY = ?",
                Integer.class,
                bucketKey
        );

        if (hits != null && hits > limit) {

            throw new GymOtpFailure(
                    429,
                    "Too many requests. Please try later.",
                    "RETRY_LATER"
            );
        }
    }
}