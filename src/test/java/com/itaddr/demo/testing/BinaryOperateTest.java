package com.itaddr.demo.testing;

import com.itaddr.common.tools.utils.ByteUtil;
import org.junit.Test;

/**
 * @Author 马嘉祺
 * @Date 2020/10/23 0023 09 10
 * @Description <p></p>
 */
public class BinaryOperateTest {
    
    @Test
    public void test01() {
        System.out.println(-0b00000000_00000000_00000000_00000011);
        System.out.println(0b10000000_00000000_00000000_00000011);
        System.out.println(" " + -0b10000000_00000000_00000000_00000011);
        System.out.println();
        int i1 = -0b00000000_00000000_00000000_00000011;
        int i2 = ~i1 + 1;
        System.out.println(i1);
        System.out.println(Integer.toBinaryString(i1));
        System.out.println(Integer.toBinaryString(i2));
    }
    
    @Test
    public void test02() {
        /*String vs = "00000000";*/
        /*int value = 11101111111011111110111111011111;*/
        /*int value = 0b11101111111000000000111111011111;*/
        /*int value = 0b00000000011000000000111111011111;*/
        /*int value = 0;
        System.out.println(ByteUtil.toBinaryString(ByteUtil.readBytes(value)));*/
        long value = 0b0111111111111111111111111000000000000000011111111111111111111111L;
        System.out.println(ByteUtil.toBinaryString(ByteUtil.readBytes(value)));
        
        int idx = 0;
        byte[] temps = new byte[10];
        for (; (value & 0xffffffffffffff80L) != 0; value >>>= 7, ++idx) {
            temps[idx] = (byte) (value & 0x7fL | 0x80L);
        }
        temps[idx] = (byte) (value & 0x7fL);
        byte[] result = new byte[idx + 1];
        System.arraycopy(temps, 0, result, 0, result.length);
        
        for (int i = 0; i < result.length; ++i) {
            System.out.print(ByteUtil.toBinaryString(result[i]) + " ");
        }
        System.out.println();
    }
    
    @Test
    public void test03() {
        byte[] buffer = {(byte) 0b1_1010101, (byte) 0b1_1101101, (byte) 0b1_1110111, (byte) 0b1_1101011, (byte) 0b0_1001001};
        
        int offset = 2;
        int result = 0;
        for (int shift = 0; offset < buffer.length && shift < 32; ++offset, shift += 7) {
            int var = buffer[offset] & 0xff, flag = var & 0x80, value = var & 0x7f;
            
            System.out.println(ByteUtil.toBinaryString(ByteUtil.readBytes(value << shift)));
            
            result |= value << shift;
            if (0 == flag) {
                break;
            }
        }
        System.out.println();
        System.out.println(ByteUtil.toBinaryString(ByteUtil.readBytes(result)));
    }
    
    @Test
    public void test04() {
        int value = 0b00000000011000000000111111011111;
        System.out.println(ByteUtil.toBinaryString(value));
        
        int flag = value >>> 31;
        if (flag != 0) {
            value = ~value;
        }
        value = value << 1 | flag;
        
        System.out.println(ByteUtil.toBinaryString(value));
    }
    
    static final int val = 32767;
    
    char c;
    
    @Test
    public void test05() {
        /*byte value1 = -128;
        byte value2 = Short.MAX_VALUE - 32640;
        
        final int value4 = 0b100;
        short value5 = val;
        
        float MIN_VALUE = 0x1.fffffeP+127f;
        float MAX_VALUE = 0x1.0p-126f;
        
        byte value6 = -0b10000000;
        System.out.println(value6);
        
        int value = Byte.MIN_VALUE;
        System.out.println(ByteUtil.toBinaryString(new byte[]{value1}));*/
        
        int value1 = 0b10000000_00000000_00000000_01010111;
        int value2 = 0b00000000_00000000_00000000_01101100;
        
        /*
        被除数：01000  除数：10101  余数：符号不等 01000 + 10101 = 11101
        
        余数   除数
        11101  10101  符号相等 商为1 余数为(11101 << 1 = 11010) - 10101 = 00101
        00101  10101
        */
        
        System.out.println(ByteUtil.toBinaryString(value1 / value2));
        System.out.println(ByteUtil.toBinaryString(39 / -13));
    
    
        System.out.println();
        System.out.println(ByteUtil.toBinaryString(39));
        System.out.println(ByteUtil.toBinaryString(-13));
        System.out.println(ByteUtil.toBinaryString(13));
    
        System.out.println();
        System.out.println(36 & 31);
    }
    
}
