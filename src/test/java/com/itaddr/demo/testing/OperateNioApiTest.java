package com.itaddr.demo.testing;

import com.itaddr.common.tools.utils.ByteUtil;
import org.junit.Test;
import sun.misc.Cleaner;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.StandardOpenOption;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author 马嘉祺
 * @Date 2020/2/10 0010 17 31
 * @Description <p></p>
 */
public class OperateNioApiTest {
    
    @Test
    public void fileChannelWrite() throws IOException {
        long beginTimeMs, endTimeMs;
        
        // 初始化资源
        beginTimeMs = System.currentTimeMillis();
        String fileName = "D:\\_TEST_NIO_API_FILE";
        
        int truncateSize = 1024 * 1024 * 1024;
        // FileChannel channel = FileChannel.open(new File(fileName).toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        // channel.position(truncateSize - 1).write(ByteBuffer.allocate(1));
        // channel.position(0);
        RandomAccessFile accessFile = new RandomAccessFile(fileName, "rw");
        accessFile.seek(truncateSize - 1);
        accessFile.writeByte(0);
        accessFile.seek(0);
        FileChannel channel = accessFile.getChannel();
        
        int count = 10000;
        int eachSize = truncateSize / count;
        ByteBuffer byteBuffer = ByteBuffer.allocate(eachSize);
        byteBuffer.put((byte) 0x12);
        byteBuffer.put(eachSize - 1, (byte) 0x34);
        byteBuffer.flip();
        endTimeMs = System.currentTimeMillis();
        System.out.printf("init resource: time=%dms\n", (endTimeMs - beginTimeMs));
        
        // 写数据测试
        beginTimeMs = System.currentTimeMillis();
        // channel.transferFrom(channel, 1024 * 1024, 100 * 2014 * 1024);
        for (int i = 0; i < count; ++i) {
            channel.write(byteBuffer);
            byteBuffer.clear();
        }
        channel.force(false);
        endTimeMs = System.currentTimeMillis();
        System.out.printf("write record: count=%d, eachSize=%d, time=%dms\n", count, eachSize, endTimeMs - beginTimeMs);
        
        // 释放资源测试
        beginTimeMs = System.currentTimeMillis();
        accessFile.close();
        endTimeMs = System.currentTimeMillis();
        System.out.printf("release resource: time=%dms\n", endTimeMs - beginTimeMs);
    }
    
    @Test
    public void mmapBufferWrite() throws IOException {
        long beginTimeMs, endTimeMs;
        
        // 初始化资源
        beginTimeMs = System.currentTimeMillis();
        String fileName = "D:\\_TEST_NIO_API_FILE";
        
        int mmapSize = 1024 * 1024 * 1024;
        // FileChannel channel = FileChannel.open(new File(fileName).toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        RandomAccessFile accessFile = new RandomAccessFile(fileName, "rw");
        accessFile.seek(mmapSize - 1);
        accessFile.writeByte(0);
        accessFile.seek(0);
        FileChannel channel = accessFile.getChannel();
        
        MappedByteBuffer mmapBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, mmapSize).load();
        accessFile.close();
        int count = 10000;
        int eachSize = mmapSize / count;
        byte[] recordValue = new byte[eachSize];
        recordValue[0] = 0x12;
        recordValue[count - 1] = 0x34;
        endTimeMs = System.currentTimeMillis();
        System.out.printf("init resource: time=%dms\n", (endTimeMs - beginTimeMs));
        
        // 写数据测试
        beginTimeMs = System.currentTimeMillis();
        for (int i = 0; i < count; ++i) {
            mmapBuffer.put(recordValue);
        }
        mmapBuffer.force();
        endTimeMs = System.currentTimeMillis();
        System.out.printf("write record: count=%d, eachSize=%d, time=%dms\n", count, eachSize, endTimeMs - beginTimeMs);
        
        beginTimeMs = System.currentTimeMillis();
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                Method cleanerMethod = mmapBuffer.getClass().getMethod("cleaner");
                cleanerMethod.setAccessible(true);
                Cleaner cleaner = (Cleaner) cleanerMethod.invoke(mmapBuffer);
                cleaner.clean();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
        endTimeMs = System.currentTimeMillis();
        System.out.printf("release resource: time=%dms\n", endTimeMs - beginTimeMs);
    }
    
    @Test
    public void test01() throws IOException {
        String fileName = "_TEST_NIO_API_FILE";
        
        FileChannel channel = FileChannel.open(new File(fileName).toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 1024 * 1024 * 1024).load();
        channel.close();
        
        byteBuffer.put(new byte[]{0x01, 0x02, 0x03, 0x04});
        
        byteBuffer.force();
        
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                Method getCleanerMethod = byteBuffer.getClass().getMethod("cleaner");
                getCleanerMethod.setAccessible(true);
                Cleaner cleaner = (Cleaner) getCleanerMethod.invoke(byteBuffer);
                cleaner.clean();
            } catch (Exception ignored) {
            }
            return null;
        });
    }
    
    @Test
    public void test02() throws IOException {
        String fileName = "_TEST_NIO_API_FILE";
        ByteBuffer buffer = ByteBuffer.allocate(4);
        
        FileChannel channel = FileChannel.open(new File(fileName).toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        channel.read(buffer);
        buffer.flip();
        
        System.out.println(ByteUtil.toLowerHexString(buffer.array()));
        
        channel.close();
    }
    
    @Test
    public void test03() throws IOException {
        String fileName = "D:\\_TEST_NIO_API_FILE";
        File file = new File(fileName);
        RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
        
        ByteBuffer buffer = ByteBuffer.allocate(4);
        
        FileChannel channel = accessFile.getChannel();
        MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 32 * 1024 * 1024).load();
        
        int idx = 0;
        for (int i = 0; i < 50; ++i) {
            byteBuffer.putInt(idx, i);
            idx += 4;
            
            channel.read(buffer);
            buffer.flip();
            System.out.print(buffer.getInt() + " ");
            buffer.clear();
        }
        
        channel.position(0);
        System.out.println("\n=======================================================");
        for (int i = 0; i < 50; ++i) {
            channel.read(buffer);
            buffer.flip();
            System.out.print(buffer.getInt() + " ");
            buffer.clear();
        }
        
        accessFile.seek(0);
        System.out.println("\n=======================================================");
        for (int i = 0; i < 50; ++i) {
            System.out.print(accessFile.readInt() + " ");
        }
        System.out.println();
        
        byteBuffer.force();
        channel.force(true);
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                Method getCleanerMethod = byteBuffer.getClass().getMethod("cleaner");
                getCleanerMethod.setAccessible(true);
                Cleaner cleaner = (Cleaner) getCleanerMethod.invoke(byteBuffer);
                cleaner.clean();
            } catch (Exception ignored) {
            }
            return null;
        });
        channel.close();
        accessFile.close();
    }
    
    @Test
    public void test04() throws IOException {
        String fileName = "D:\\_TEST_NIO_API_FILE";
        FileChannel channel = FileChannel.open(new File(fileName).toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 32 * 1024 * 1024).load();
        MappedByteBuffer duplicate = (MappedByteBuffer) byteBuffer.duplicate();
        channel.close();
        
        byteBuffer.putLong(10);
        System.out.println(duplicate.getLong(100));
        
        byteBuffer.force();
        
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                Method cleanerMethod = byteBuffer.getClass().getMethod("cleaner");
                cleanerMethod.setAccessible(true);
                Cleaner cleaner = (Cleaner) cleanerMethod.invoke(byteBuffer);
                cleaner.clean();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
        
        File file = new File(fileName);
        if (!file.delete()) {
            System.out.println("删除失败");
        } else {
            System.out.println("删除成功");
        }
    }
    
    @Test
    public void test05() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
        String fileName = "D:\\_TEST_NIO_API_FILE";
        FileChannel channel = FileChannel.open(new File(fileName).toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        MappedByteBuffer byteBuffer1 = channel.map(FileChannel.MapMode.READ_WRITE, 0, 1024 * 1024).load();
        MappedByteBuffer byteBuffer2 = channel.map(FileChannel.MapMode.READ_WRITE, 1024 * 1024, 1024 * 1024).load();
        MappedByteBuffer byteBuffer3 = channel.map(FileChannel.MapMode.READ_WRITE, 0, 2 * 1024 * 1024).load();
        
        channel.close();
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                Method cleanerMethod = byteBuffer1.getClass().getMethod("cleaner");
                cleanerMethod.setAccessible(true);
                Cleaner cleaner = (Cleaner) cleanerMethod.invoke(byteBuffer1);
                cleaner.clean();
                
                cleanerMethod = byteBuffer2.getClass().getMethod("cleaner");
                cleanerMethod.setAccessible(true);
                cleaner = (Cleaner) cleanerMethod.invoke(byteBuffer2);
                cleaner.clean();
                
                cleanerMethod = byteBuffer3.getClass().getMethod("cleaner");
                cleanerMethod.setAccessible(true);
                cleaner = (Cleaner) cleanerMethod.invoke(byteBuffer3);
                cleaner.clean();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
        /*Thread.sleep(3000);
        Method unmapMethod = FileChannelImpl.class.getDeclaredMethod("unmap", MappedByteBuffer.class);
        unmapMethod.setAccessible(true);
        unmapMethod.invoke(FileChannelImpl.class, byteBuffer1);
        unmapMethod.invoke(FileChannelImpl.class, byteBuffer2);
        unmapMethod.invoke(FileChannelImpl.class, byteBuffer3);
        Thread.sleep(5000);*/
    }
    
    @Test
    public void test06() throws IOException {
        String fileName = "D:\\_TEST_NIO_API_FILE";
        FileChannel channel = FileChannel.open(new File(fileName).toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        channel.write(ByteBuffer.wrap(new byte[]{0}), 1024 * 1024);
        
        ReadAndWrite raw = new ReadAndWrite();
        
        long transferFrom = channel.transferFrom(raw, 0, 1026 * 100);
        System.out.println("transferFrom: " + transferFrom);
        
        System.out.println("=======================================================");
        System.out.println("=======================================================");
        
        long transferTo = channel.transferTo(0, 1026 * 100, raw);
        System.out.println("transferTo: " + transferTo);
        
        raw.close();
        channel.close();
        
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(32);
        byteBuffer.flip();
        
        
    }
    
    public static class ReadAndWrite implements ReadableByteChannel, WritableByteChannel {
        
        private boolean open = true;
        
        private ByteBuffer byteBuffer;
        
        private boolean isNewBuffer;
        
        @Override
        public int read(ByteBuffer dst) throws IOException {
            if (null != byteBuffer && !isNewBuffer) {
                isNewBuffer = byteBuffer == dst;
                System.out.println("byteBuffer == dst: " + isNewBuffer);
            }
            this.byteBuffer = dst;
            System.out.printf("read: dst.capacity=%d, dst.limit=%d\n", dst.capacity(), dst.limit());
            
            // 必须保证 dst.position == result 才会进行下一次读取
            dst.position(dst.limit() - 4);
            dst.putInt(ThreadLocalRandom.current().nextInt(1000));
            
            // 返回读取的字节数，可能为0，如果通道已到达文件末尾，则为-1
            return dst.position();
        }
        
        @Override
        public int write(ByteBuffer src) throws IOException {
            if (null != byteBuffer && !isNewBuffer) {
                isNewBuffer = byteBuffer == src;
                System.out.println("byteBuffer == src: " + isNewBuffer);
            }
            this.byteBuffer = src;
            int lastInt = src.getInt(src.limit() - 4);
            System.out.printf("write: src.capacity=%d, src.limit=%d, lastInt=%d\n", src.capacity(), src.limit(), lastInt);
            
            // 返回写入的字节数，可能为0
            return src.limit();
        }
        
        @Override
        public boolean isOpen() {
            return open;
        }
        
        @Override
        public void close() throws IOException {
            this.open = false;
        }
        
    }
    
}
