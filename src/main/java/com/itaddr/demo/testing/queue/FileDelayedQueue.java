package com.itaddr.demo.testing.queue;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName FileDelayedQueue
 * @Description TODO
 * @Author MyPC
 * @Date 2022/12/23 18:04
 * @Version 1.0
 */
public class FileDelayedQueue<E extends Delayed> implements DelayedQueue<E> {

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public DelayedQueue<E> offer(E... elements) {
        return null;
    }

    @Override
    public DelayedQueue<E> offer(Collection<E> elements) {
        return null;
    }

    @Override
    public E poll() throws InterruptedException {
        return null;
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public Collection<E> polls() throws InterruptedException {
        return null;
    }

    @Override
    public Collection<E> polls(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public E peek() {
        return null;
    }

    @Override
    public Long peekTargetTime() {
        return null;
    }

    @Override
    public DelayedQueue<E> destroy() {
        return null;
    }

}
