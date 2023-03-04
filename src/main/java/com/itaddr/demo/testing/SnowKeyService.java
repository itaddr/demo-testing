package com.itaddr.demo.testing;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class SnowKeyService {

    private static final int ID_EXPIRE_SEC = 33;
    private static final int CONTINUE_INTERVAL_SEC = 10;

    private final AtomicBoolean stopped = new AtomicBoolean(Boolean.FALSE);
    private final String sessionId = UUID.randomUUID().toString();
    private final ConcurrentMap<String, KeyGenerator> keyGeneratorTables = new ConcurrentHashMap<>();

    private final IdOperator idOperator;
    private ScheduledExecutorService housekeeperService;

    public SnowKeyService(IdOperator idOperator) {
        this.idOperator = idOperator;
        this.housekeeperService = new ScheduledThreadPoolExecutor(NumberUtils.INTEGER_ONE, (run) -> new Thread(run, "housekeeperService"));
        log.info("初始化GenerateKeyService成功");
    }

    /**
     * 生成分布式ID
     *
     * @param namespace
     * @return
     */
    public long key(String namespace) {
        return this.key(namespace, 6, 15);
    }

    /**
     * 生成分布式ID
     *
     * @param namespace
     * @param serialNoBits
     * @param idBits
     * @return
     */
    public long key(String namespace, int idBits, int serialNoBits) {
        if (stopped.get()) { // 生成器已被释放
            throw new IllegalStateException("分布式Key生成器已被释放");
        }
        final int maxSerialNo = ~(-1 << serialNoBits), maxSymbol = ~(-1 << idBits);

        final KeyGenerator keyGen = this.getKeyGenerator(namespace, maxSymbol);
        synchronized (keyGen.namespace) {

            long currentTimeMs = System.currentTimeMillis(), currentTimeSec = currentTimeMs / 1000L;
            if (keyGen.serialNo >= maxSerialNo) {
                while (keyGen.beforeSeconds == currentTimeSec) {
                    currentTimeSec = this.nextSecondTimeMs(currentTimeMs) / 1000L;
                }
                keyGen.serialNo = 0;
            } else {
                ++keyGen.serialNo;
            }
            keyGen.beforeSeconds = currentTimeSec;

            return currentTimeSec << serialNoBits + idBits | (long) keyGen.id << serialNoBits | keyGen.serialNo;
        }
    }

    private long nextSecondTimeMs(long currentTimeMs) {
        final long timeoutMs = 1000L - currentTimeMs % 1000L;
        try {
            Thread.sleep(timeoutMs);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
        return System.currentTimeMillis();
    }

    private KeyGenerator getKeyGenerator(String namespace, int maxId) {
        final long currentEpoch = System.currentTimeMillis();
        final ConcurrentMap<String, KeyGenerator> keyGeneratorTables = this.keyGeneratorTables;
        return keyGeneratorTables.compute(namespace, (key, value) -> {
            if (null == value) {
                return this.createKeyGenerator(namespace, maxId, currentEpoch);
            } else if (currentEpoch - value.epoch > 30000L) {
                log.warn("[sessionId={}, namespace={}]下的[id={}]超过30秒未续期，已过期", this.sessionId, namespace, value.id);
                return this.createKeyGenerator(namespace, maxId, currentEpoch);
            } else {
                return value;
            }
        });
    }

    private KeyGenerator createKeyGenerator(String namespace, int maxId, long epoch) {
        final String sessionId = this.sessionId;
        for (int id = 1; ; ++id) {
            if (idOperator.trySetId(namespace, id, sessionId, ID_EXPIRE_SEC)) {
                final KeyGenerator keyGen = new KeyGenerator(id, namespace, epoch);
                housekeeperService.schedule(() -> this.continueKeyGenerator(namespace), CONTINUE_INTERVAL_SEC, TimeUnit.SECONDS);
                log.info("成功创建[sessionId={}, namespace={}]下的[id={}]", this.sessionId, namespace, keyGen.id);
                return keyGen;
            } else if (id == maxId) {
                throw new IllegalStateException("[keyGenerator.id]已被耗尽，最大等待[" + ID_EXPIRE_SEC + "s]之后，无效的[keyGenerator.id]可能会被释放");
            }
        }
    }

    private void continueKeyGenerator(String namespace) {
        if (stopped.get()) {
            return;
        }
        try {
            final ConcurrentMap<String, KeyGenerator> keyGeneratorTables = this.keyGeneratorTables;
            final String sessionId = this.sessionId;
            final long currentTimeMs = System.currentTimeMillis();

            final KeyGenerator keyGen = keyGeneratorTables.get(namespace);
            if (null == keyGen) { // keyGenerator不存在，直接退出并不再续期
                return;
            } else if (currentTimeMs - keyGen.epoch > 30000L) { // keyGenerator已超时，直接退出并不再续期
                log.warn("[sessionId={}, namespace={}]下的[id={}]超过30秒未续期，已过期", this.sessionId, namespace, keyGen.id);
                return;
            } else if (currentTimeMs / 1000L - keyGen.beforeSeconds > 2 * 60 * 60L) { // keyGenerator已超过2h未被使用，直接退出并不再续期
                log.warn("[sessionId={}, namespace={}]下的[id={}]已超过2小时未使用，不再续期", this.sessionId, namespace, keyGen.id);
                return;
            }

            if (!idOperator.isOwnedId(namespace, keyGen.id, sessionId)) { // keyGenerator.id不存在或者sessionId失效，直接退出并不再续期
                return;
            }

            idOperator.continueId(namespace, keyGen.id, ID_EXPIRE_SEC); // 开始keyGenerator续期操作
            keyGen.epoch = currentTimeMs;
            log.info("[sessionId={}, namespace={}]下的[id={}]已成功续期", this.sessionId, namespace, keyGen.id);
        } catch (Exception e) {
            log.error(String.format("[sessionId=%s, namespace=%s]下的[id=Unknown]续期失败", this.sessionId, namespace), e);
        }
        housekeeperService.schedule(() -> this.continueKeyGenerator(namespace), CONTINUE_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    public void destroy() throws InterruptedException {
        if (stopped.compareAndSet(Boolean.FALSE, Boolean.TRUE)) {
            // 清除当前进程占用的keyGenerator.id
            final String sessionId = this.sessionId;
            long currentEpoch = System.currentTimeMillis();

            keyGeneratorTables.forEach((namespace, keyGen) -> {
                synchronized (keyGen.namespace) {
                    if (idOperator.isOwnedId(namespace, keyGen.id, sessionId) && currentEpoch - keyGen.epoch <= 30000) {
                        idOperator.removeId(namespace, keyGen.id);
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

        final int id;
        final String namespace;
        volatile long epoch;

        volatile long beforeSeconds;
        volatile int serialNo;

        KeyGenerator(int id, String namespace, long epoch) {
            this.id = id;
            this.namespace = namespace;
            this.epoch = epoch;
        }

    }

    public interface IdOperator {

        boolean trySetId(String namespace, int id, String sessionId, long expireSec);

        boolean isOwnedId(String namespace, int id, String sessionId);

        void continueId(String namespace, int id, long expireSec);

        void removeId(String namespace, int id);

    }

}
