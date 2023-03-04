package com.itaddr.demo.testing.queue;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName DelayedQueue
 * @Description TODO
 * @Author MyPC
 * @Date 2022/12/23 17:51
 * @Version 1.0
 */
public interface DelayedQueue<E extends Delayed> {

    int size();

    boolean isEmpty();

    DelayedQueue<E> offer(E... elements);

    DelayedQueue<E> offer(Collection<E> elements);

    E poll() throws InterruptedException;

    E poll(long timeout, TimeUnit unit) throws InterruptedException;

    Collection<E> polls() throws InterruptedException;

    Collection<E> polls(long timeout, TimeUnit unit) throws InterruptedException;

    E peek();

    Long peekTargetTime();

    DelayedQueue<E> destroy();

}
