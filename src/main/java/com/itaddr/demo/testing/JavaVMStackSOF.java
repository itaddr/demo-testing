package com.itaddr.demo.testing;

/**
 * VM Args: -Xss128k
 * @Author 马嘉祺
 * @Date 2021/3/2 0002 10 05
 * @Description <p></p>
 */
public class JavaVMStackSOF {
    
    private int stackLength = 1;
    
    public void stackLeak() {
        stackLength++;
        stackLeak();
    }
    
    public static void main(String[] args) throws Throwable {
        JavaVMStackSOF oom = new JavaVMStackSOF();
        try {
            oom.stackLeak();
        } catch (Throwable e) {
            System.out.println("stack length:" + oom.stackLength);
            throw e;
        }
    }
    
}
