package com.itaddr.demo.testing;

import com.itaddr.common.tools.beans.TimeStamp;
import com.itaddr.common.tools.utils.ThreadUtil;
import org.junit.Test;

import java.lang.ref.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author 马嘉祺
 * @Date 2021/1/6 0006 09 26
 * @Description <p></p>
 */
public class ReferenceTest {
    
    @Test
    public void test01() {
        
        ReferenceQueue<TimeStamp> queue = new ReferenceQueue<>();
        
        Reference<TimeStamp> reference = new WeakReference<>(new TimeStamp(), queue);
//        weak.enqueue();
        System.gc();
        ThreadUtil.sleepUninterruptibly(3, TimeUnit.SECONDS);
        
        Reference<? extends TimeStamp> poll = queue.poll();
        if (null == poll) {
            System.out.println("没有元素");
            return;
        }
        System.out.println(poll.get());
    }
    
    @Test
    public void test02() {
//        TimeStamp stamp = new TimeStamp();
        ThreadLocal<TimeStamp> tl = new ThreadLocal<>();
        tl.set(new TimeStamp());
    
        System.gc();
        ThreadUtil.sleepUninterruptibly(3, TimeUnit.SECONDS);
        
        System.out.println(tl.get());
    }
    
}
