package com.itaddr.demo.testing;

import org.apache.commons.lang3.math.NumberUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DistributedKey {

    private static final String GENERATE_KEY_PREFIX = "distributed:key:";

    private static final int SYMBOL_EXPIRE_SEC = 33;
    private static final int CONTINUE_INTERVAL_SEC = 10;

    private final AtomicBoolean stopped = new AtomicBoolean(Boolean.FALSE);
    private final String sessionId = UUID.randomUUID().toString();
    private final ConcurrentMap<String, KeyGenerator> keyGeneratorTables = new ConcurrentHashMap<>();

    private final RedissonClient redissonClient;
    private ScheduledExecutorService housekeeperService;

    public DistributedKey(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.housekeeperService = new ScheduledThreadPoolExecutor(NumberUtils.INTEGER_ONE, (run) -> new Thread(run, "housekeeperService"));
    }

    /**
     * 生成分布式ID
     *
     * @param namespace
     * @param serialNoBits
     * @param symbolBits
     * @return
     */
    public long key(String namespace, int serialNoBits, int symbolBits) {
        if (stopped.get()) { // 生成器已被释放
            throw new IllegalStateException("分布式Key生成器已被释放");
        }
        final int maxSerialNo = ~(-1 << serialNoBits), maxSymbol = ~(-1 << symbolBits);
//        namespace = String.format("%s-%02dbit-%02dbit", namespace, serialNoBits, symbolBits);
        long currentSeconds = System.currentTimeMillis() / 1000L;
        final KeyGenerator keyGen = this.getKeyGenerator(namespace, maxSymbol, currentSeconds);

        int serialNo;
        synchronized (keyGen.namespace) {
            if (keyGen.beforeSeconds == currentSeconds) {
                if (keyGen.serialNo >= maxSerialNo) {
                    while (keyGen.beforeSeconds == currentSeconds) {
                        currentSeconds = this.currentTimeMillisAfterBlock() / 1000L;
                    }
                    serialNo = keyGen.serialNo = 0;
                } else {
                    serialNo = keyGen.serialNo = ++keyGen.serialNo;
                }
            } else {
                serialNo = keyGen.serialNo = 0;
            }
            keyGen.beforeSeconds = currentSeconds;
        }

        return currentSeconds << serialNoBits + symbolBits | (long) serialNo << symbolBits | keyGen.symbol;
    }

    private long currentTimeMillisAfterBlock() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
        return System.currentTimeMillis();
    }

    private KeyGenerator getKeyGenerator(String namespace, int maxSymbol, long epoch) {
        final ConcurrentMap<String, KeyGenerator> keyGeneratorTables = this.keyGeneratorTables;
        return keyGeneratorTables.compute(namespace, (key, value) -> null != value && epoch - value.epoch <= 30 ? value : this.createKeyGenerator(namespace, maxSymbol, epoch));
    }

    private KeyGenerator createKeyGenerator(String namespace, int maxSymbol, long epoch) {
        final String sessionId = this.sessionId;
        for (int symbol = 1; ; ++symbol) {
            final String key = GENERATE_KEY_PREFIX + namespace + ':' + symbol;
            final RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);
            if (bucket.trySet(sessionId, SYMBOL_EXPIRE_SEC, TimeUnit.SECONDS)) {
                final KeyGenerator keyGenerator = new KeyGenerator(symbol, namespace, epoch);
                housekeeperService.schedule(() -> this.continueKeyGenerator(namespace), CONTINUE_INTERVAL_SEC, TimeUnit.SECONDS);
                return keyGenerator;
            } else if (symbol == maxSymbol) {
                throw new IllegalStateException("keyGeneratorSymbol已被耗尽，最大等待[" + SYMBOL_EXPIRE_SEC + "s]之后，无效的keyGeneratorSymbol会被释放");
            }
        }
    }

    private void continueKeyGenerator(String namespace) {
        if (stopped.get()) {
            return;
        }
        try {
            final ConcurrentMap<String, KeyGenerator> keyGeneratorTables = this.keyGeneratorTables;
            final long currentSeconds = System.currentTimeMillis() / 1000L;

            final KeyGenerator keyGen = keyGeneratorTables.get(namespace);
            if (null == keyGen) { // keyGenerator不存在，直接退出并不再续期
                return;
            } else if (currentSeconds - keyGen.epoch > 30) { // keyGenerator已超时，直接退出并不再续期
                return;
            } else if (currentSeconds - keyGen.beforeSeconds > 2 * 60 * 60L) { // keyGenerator已超过2h未被使用，直接退出并不再续期
                return;
            }

            final String key = GENERATE_KEY_PREFIX + namespace + ':' + keyGen.symbol;
            final RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);
            if (!bucket.isExists()) { // keyGenerator.symbol不存在，直接退出并不再续期
                return;
            }

            final String sessionId = this.sessionId;
            if (!bucket.get().equals(sessionId)) { // sessionId失效，直接退出并不再续期
                return;
            }

            // 开始keyGenerator续期操作
            bucket.expire(SYMBOL_EXPIRE_SEC, TimeUnit.SECONDS);
            keyGen.epoch = currentSeconds;
        } catch (Exception ignored) {
        }
        housekeeperService.schedule(() -> this.continueKeyGenerator(namespace), CONTINUE_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    public void destroy() throws InterruptedException {
        if (stopped.compareAndSet(false, true)) {
            // 清除当前进程占用的machineId
            final String sessionId = this.sessionId;
            long epoch = System.currentTimeMillis() / 1000L;
            for (Map.Entry<String, KeyGenerator> entry : keyGeneratorTables.entrySet()) {
                final String namespace = entry.getKey();
                final KeyGenerator keyGen = entry.getValue();
                final String key = GENERATE_KEY_PREFIX + namespace + ':' + keyGen.symbol;
                final RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);
                if (bucket.isExists() && bucket.get().equals(sessionId) && epoch - keyGen.epoch <= 30) {
                    bucket.delete();
                }
            }
            keyGeneratorTables.clear();

            // 释放定时任务执行器
            this.housekeeperService.shutdown();
            if (this.housekeeperService.awaitTermination(15, TimeUnit.SECONDS)) {
                this.housekeeperService = null;
            }
        }
    }

    static class KeyGenerator {

        final int symbol;
        final String namespace;
        volatile long epoch;

        volatile long beforeSeconds;
        volatile int serialNo;

        KeyGenerator(int symbol, String namespace, long epoch) {
            this.symbol = symbol;
            this.namespace = namespace;
            this.epoch = epoch;
        }

    }

}
