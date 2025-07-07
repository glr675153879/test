package com.hscloud.hs.cost.account.utils;

import java.util.concurrent.TimeUnit;


/**
 * @author YJM
 * @date 2023-09-05 17:16
 */
public class SnowflakeGenerator {
    private static long workerId = 1;
    private static long sequence = 0L;
    private static long lastTimestamp = -1L;

    private static final long START_TIME = TimeUnit.MILLISECONDS.toMillis(1420070400000L); // 2015-01-01

    // Number of bits to represent the worker ID and the sequence number respectively
    private static final long WORKER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    // Maximum possible worker ID and sequence number based on the number of bits
    private static final long MAX_WORKER_ID = ~(~0L << WORKER_ID_BITS);
    private static final long MAX_SEQUENCE = ~(~0L << SEQUENCE_BITS);

    // Shift lengths for worker ID and sequence
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = WORKER_ID_BITS + SEQUENCE_BITS;

    public static synchronized long ID() {
        long currentTimestamp = timestamp();

        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("Invalid system clock");
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        long nextId = ((currentTimestamp - START_TIME) << TIMESTAMP_LEFT_SHIFT) |
                (workerId << WORKER_ID_SHIFT) |
                sequence;
        return nextId;
    }

    private static long waitNextMillis(long currentTimestamp) {
        long timestamp = timestamp();
        while (timestamp <= currentTimestamp) {
            timestamp = timestamp();
        }
        return timestamp;
    }

    private static long timestamp() {
        return System.currentTimeMillis();
    }
}
