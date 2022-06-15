package com.itaddr.demo.testing;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadTest {

    @Test
    public void test01() throws InterruptedException {
        final AtomicInteger indxer = new AtomicInteger(NumberUtils.INTEGER_ZERO);
        final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1, run -> new Thread(run, String.format("thread-test-%03d", indxer.getAndIncrement())), (r, executor) -> r.run());

        executorService.schedule(() -> System.out.println(LocalDateTime.now()), 5, TimeUnit.SECONDS);

        executorService.shutdown();
        if (!executorService.awaitTermination(120, TimeUnit.SECONDS)) {
            System.err.println("线程池未正确关闭");
        }
    }

}
