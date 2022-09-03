package com.itaddr.demo.testing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @ClassName GZipSupport
 * @Description TODO
 * @Author MyPC
 * @Date 2022/8/25 11:48
 * @Version 1.0
 */
public class GZipSupport {

    public static void main(String[] args) {
//        if (args == null || args.length == 0) {
//            return;
//        }
//        final String bodyBase64Str = args[0];
        final byte[] resultBytes;

        final String bodyBase64Str = "eyJyb2xlaWQiOiIxMjEzMiIsInNlcnZlcmlkIjoiZmRhc3RyZSIsImdhbWVpZCI6MSwidGltZSI6MTY2MTQwOTA2ODkwMCwiY2hhbmdldGltZSI6MTY2MTQwOTA2ODkwMH0=";
        final byte[] bodyBytes = Base64.getDecoder().decode(bodyBase64Str);

        final ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out1)) {
            gzip.write(bodyBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        resultBytes = out1.toByteArray();
        System.out.println(Arrays.toString(resultBytes));

        final byte[] buffer = new byte[256];
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (GZIPInputStream input = new GZIPInputStream(new ByteArrayInputStream(resultBytes))) {
            int len;
            while ((len = input.read(buffer)) >= 0) {
                output.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(Arrays.toString(output.toByteArray()));
    }

}
