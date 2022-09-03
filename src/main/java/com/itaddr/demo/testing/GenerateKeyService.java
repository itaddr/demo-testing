package com.itaddr.demo.testing;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class GenerateKeyService {

    private static final String GENERATE_KEY_PREFIX = "distributed:key:";

    private static final int SYMBOL_EXPIRE_SEC = 33;
    private static final int CONTINUE_INTERVAL_SEC = 10;

    private final AtomicBoolean stopped = new AtomicBoolean(Boolean.FALSE);
    private final String sessionId = UUID.randomUUID().toString();
    private final ConcurrentMap<String, KeyGenerator> keyGeneratorTables = new ConcurrentHashMap<>();

    private final RedissonClient redissonClient;
    private ScheduledExecutorService housekeeperService;

    public GenerateKeyService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.housekeeperService = new ScheduledThreadPoolExecutor(NumberUtils.INTEGER_ONE, (run) -> new Thread(run, "housekeeperService"));
        log.info("初始化GenerateKeyService成功");
    }

    /**
     * 生成分布式ID
     *
     * @param namespace
     * @param serialNoBits
     * @param symbolBits
     * @return
     */
    public long key(String namespace, int symbolBits, int serialNoBits) {
        if (stopped.get()) { // 生成器已被释放
            throw new IllegalStateException("分布式Key生成器已被释放");
        }
        final int maxSerialNo = ~(-1 << serialNoBits), maxSymbol = ~(-1 << symbolBits);

        synchronized (namespace = namespace.intern()) {
            final KeyGenerator keyGen = this.getKeyGenerator(namespace, maxSymbol);

            long currentSeconds = System.currentTimeMillis() / 1000L;
            if (keyGen.serialNo >= maxSerialNo) {
                while (keyGen.beforeSeconds == currentSeconds) {
                    currentSeconds = this.currentTimeMillisAfterBlock() / 1000L;
                }
                keyGen.serialNo = 0;
            } else {
                ++keyGen.serialNo;
            }
            keyGen.beforeSeconds = currentSeconds;

            return currentSeconds << serialNoBits + symbolBits | (long) keyGen.symbol << serialNoBits | keyGen.serialNo;
        }
    }

    private long currentTimeMillisAfterBlock() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
        return System.currentTimeMillis();
    }

    private KeyGenerator getKeyGenerator(String namespace, int maxSymbol) {
        final long currentEpoch = System.currentTimeMillis();
        final ConcurrentMap<String, KeyGenerator> keyGeneratorTables = this.keyGeneratorTables;

        KeyGenerator keyGen = keyGeneratorTables.get(namespace);
        if (null == keyGen) {
            keyGen = this.createKeyGenerator(namespace, maxSymbol, currentEpoch);
            keyGeneratorTables.put(namespace, keyGen);
        } else if (currentEpoch - keyGen.epoch > 30000) {
            log.warn("[sessionId={}, namespace={}]下的符号[symbol={}]超过30秒为续期，已过期", this.sessionId, namespace, keyGen.namespace);
            keyGen = this.createKeyGenerator(namespace, maxSymbol, currentEpoch);
            keyGeneratorTables.put(namespace, keyGen);
        }

        return keyGen;
    }

    private KeyGenerator createKeyGenerator(String namespace, int maxSymbol, long epoch) {
        final String sessionId = this.sessionId;
        for (int symbol = 1; ; ++symbol) {
            final String key = GENERATE_KEY_PREFIX + namespace + ':' + symbol;
            final RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);
            if (bucket.trySet(sessionId, SYMBOL_EXPIRE_SEC, TimeUnit.SECONDS)) {
                final KeyGenerator keyGen = new KeyGenerator(symbol, namespace, epoch);
                housekeeperService.schedule(() -> this.continueKeyGenerator(namespace), CONTINUE_INTERVAL_SEC, TimeUnit.SECONDS);
                log.info("成功创建[sessionId={}, namespace={}]下的符号[symbol={}]", this.sessionId, namespace, keyGen.namespace);
                return keyGen;
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
            final long currentTimeMillis = System.currentTimeMillis();

            final KeyGenerator keyGen = keyGeneratorTables.get(namespace);
            if (null == keyGen) { // keyGenerator不存在，直接退出并不再续期
                return;
            } else if (currentTimeMillis - keyGen.epoch > 3000) { // keyGenerator已超时，直接退出并不再续期
                log.warn("[sessionId={}, namespace={}]下的符号[symbol={}]超过30秒为续期，已过期", this.sessionId, namespace, keyGen.namespace);
                return;
            } else if (currentTimeMillis / 1000L - keyGen.beforeSeconds > 2 * 60 * 60L) { // keyGenerator已超过2h未被使用，直接退出并不再续期
                log.warn("[sessionId={}, namespace={}]下的符号[symbol={}]已超过2小时未使用，不在续期", this.sessionId, namespace, keyGen.namespace);
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
            keyGen.epoch = currentTimeMillis;
        } catch (Exception ignored) {
        }
        housekeeperService.schedule(() -> this.continueKeyGenerator(namespace), CONTINUE_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    public void destroy() throws InterruptedException {
        if (stopped.compareAndSet(false, true)) {
            // 清除当前进程占用的machineId
            final String sessionId = this.sessionId;
            long currentEpoch = System.currentTimeMillis();

            keyGeneratorTables.forEach((namespace, keyGen) -> {
                synchronized (keyGen.namespace) {
                    final String key = GENERATE_KEY_PREFIX + namespace + ':' + keyGen.symbol;
                    final RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);
                    if (bucket.isExists() && bucket.get().equals(sessionId) && currentEpoch - keyGen.epoch <= 30000) {
                        bucket.delete();
                    }
                }
            });
            keyGeneratorTables.clear();

            // 释放定时任务执行器
            this.housekeeperService.shutdown();
            if (this.housekeeperService.awaitTermination(60, TimeUnit.SECONDS)) {
                this.housekeeperService = null;
            }
            log.info("释放GenerateKeyService成功");
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
