package com.pluto.pluto_interview.util;

import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdGenerator implements IdGenerator{
	private static final long EPOCH = 1704067200000L; // 1 January 2024

	private static final long WORKER_ID_BITS = 5L;
	private static final long DATACENTER_ID_BITS = 5L;
	private static final long SEQUENCE_BITS = 12L;

	private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
	private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
	private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

	private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
	private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
	private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

	private long workerId = 1L;
	private long datacenterId = 1L;

	private long sequence = 0L;
	private long lastTimestamp = -1L;

	@Override
	public long nextId() {
		long timestamp = timeGen();

		if (timestamp < lastTimestamp) {
			throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
		}

		if (lastTimestamp == timestamp) {
			sequence = (sequence + 1) & MAX_SEQUENCE;
			if (sequence == 0) {
				timestamp = tilNextMillis(lastTimestamp);
			}
		} else {
			sequence = 0L;
		}

		lastTimestamp = timestamp;

		return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT) |
			  (datacenterId << DATACENTER_ID_SHIFT) |
			  (workerId << WORKER_ID_SHIFT) |
			  sequence;
	}

	private long tilNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = timeGen();
		}
		return timestamp;
	}

	private long timeGen() {
		return System.currentTimeMillis();
	}
}
