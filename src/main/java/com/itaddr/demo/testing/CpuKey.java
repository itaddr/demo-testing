package com.itaddr.demo.testing;

import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class CpuKey {

    /**
     * 获取当前系统CPU序列，可区分linux系统和windows系统
     */
    public static String getCpuId() throws Exception {
        final String cpuId;
        final String os = System.getProperty("os.name").toUpperCase();
        if ("LINUX".equals(os)) {
            cpuId = getLinuxCpuId();
        } else {
            cpuId = getWindowsCpuId();
        }
        return cpuId.toUpperCase().replace(" ", "");
    }

    /**
     * 获取linux系统CPU序列
     */
    public static String getLinuxCpuId() throws Exception {
        final Runtime run = Runtime.getRuntime();
        final Process process = run.exec(new String[]{"/bin/sh", "-c", "dmidecode -t processor | grep 'ID'"});
        boolean beginCharacter = false;
        try (final InputStream in = process.getInputStream()) {
            final StringBuilder out = new StringBuilder();
            byte[] b = new byte[128];
            for (int n; (n = in.read(b)) != -1; ) {
                for (int i = 0; i < n; ++i) {
                    char c = (char) (b[i] & 0xff);
                    if (!beginCharacter) {
                        beginCharacter = c == ':';
                        continue;
                    }
                    if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                        out.append(c);
                    } else if (c == '\n') {
                        return out.toString();
                    }
                }
            }
            return out.toString();
        } finally {
            if (null != process) {
                process.destroy();
            }
        }
    }

    /**
     * 获取windows系统CPU序列
     */
    public static String getWindowsCpuId() throws Exception {
        final Runtime run = Runtime.getRuntime();
        final Process process = run.exec(new String[]{"wmic", "cpu", "get", "ProcessorId"});
        boolean beginCharacter = false;
        try (final InputStream in = process.getInputStream()) {
            final StringBuilder out = new StringBuilder();
            final byte[] b = new byte[128];
            for (int n; (n = in.read(b)) != -1; ) {
                for (int i = 0; i < n; ++i) {
                    char c = (char) (b[i] & 0xff);
                    if (!beginCharacter) {
                        beginCharacter = c == '\n';
                        continue;
                    }
                    if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                        out.append(c);
                    } else if (c == '\n') {
                        return out.toString();
                    }
                }
            }
            return out.toString();
        } finally {
            if (null != process) {
                process.destroy();
            }
        }
    }

    /**
     * 获取进程号
     *
     * @return
     */
    public static int getProcessId() {
        final RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        return Integer.parseInt(runtime.getName().split("@")[0]);
    }

}
