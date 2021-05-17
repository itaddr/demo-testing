package com.lbole.demo.testing;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileContext;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsCreateModes;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author 马嘉祺
 * @Date 2020/8/26 0026 11 00
 * @Description <p></p>
 */
public class OperateHDFSTest {
    
    @Test
    public void exists() throws IOException {
        System.getProperties().setProperty("HADOOP_USER_NAME", "hdfs");
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://10.112.16.14:8020");
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            
            System.out.println("============================================================================================================");
            boolean exists = fs.exists(new Path("/user"));
            System.out.println("exists=" + exists);
            System.out.println("============================================================================================================");
            
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
    }
    
    @Test
    public void mkdirs() throws IOException {
        System.getProperties().setProperty("HADOOP_USER_NAME", "hdfs");
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://10.112.16.14:8020");
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            
            System.out.println("============================================================================================================");
            boolean mkdirs = fs.mkdirs(new Path("/test/directory"), FsCreateModes.getDirDefault());
            System.out.println("mkdirs=" + mkdirs);
            System.out.println("============================================================================================================");
            
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
    }
    
    @Test
    public void delete() throws IOException {
        System.getProperties().setProperty("HADOOP_USER_NAME", "hdfs");
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://10.112.16.14:8020");
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            
            System.out.println("============================================================================================================");
            boolean delete = fs.delete(new Path("/test/file"), true);
            System.out.println("mkdirs=" + delete);
            System.out.println("============================================================================================================");
            
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
    }
    
    @Test
    public void rename() throws IOException {
        System.getProperties().setProperty("HADOOP_USER_NAME", "hdfs");
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://10.112.16.14:8020");
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            
            System.out.println("============================================================================================================");
            boolean rename = fs.rename(new Path("/test/oldFile"), new Path("/test/newFile"));
            System.out.println("rename=" + rename);
            System.out.println("============================================================================================================");
            
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
    }
    
    @Test
    public void isFileOrIsDirectory() throws IOException {
    
    }
    
    @Test
    public void listStatus() throws IOException {
        System.getProperties().setProperty("HADOOP_USER_NAME", "hdfs");
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://10.112.16.14:8020");
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            
            System.out.println("============================================================================================================");
            FileStatus[] listStatus = fs.listStatus(new Path("/user"));
            for (FileStatus state : listStatus) {
                System.out.println(state);
            }
            System.out.println("============================================================================================================");
            
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
    }
    
    @Test
    public void copy() throws IOException {
        System.getProperties().setProperty("HADOOP_USER_NAME", "hdfs");
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://10.112.16.14:8020");
        FileContext fc = null;
        try {
            fc = FileContext.getFileContext(conf);
            
            System.out.println("============================================================================================================");
            FileContext.Util util = fc.util();
            boolean copy = util.copy(new Path("/test/srcFile"), new Path("/test/tarFile"));
            System.out.println("copy=" + copy);
            System.out.println("============================================================================================================");
            
        } finally {
            if (fc != null) {
            
            }
        }
    }
    
    @Test
    public void copyFromLocalFile() throws IOException {
        System.getProperties().setProperty("HADOOP_USER_NAME", "hdfs");
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://192.168.1.3:8020");
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            
            System.out.println("============================================================================================================");
            fs.copyFromLocalFile(new Path("D:\\localFile"), new Path("/test/remoteFile"));
            fs.moveFromLocalFile(new Path("D:\\localFile"), new Path("/test/remoteFile"));
            fs.copyFromLocalFile(false, new Path("D:\\localFile"), new Path("/test/remoteFile"));
            fs.copyFromLocalFile(false, true, new Path("D:\\localFile"), new Path("/test/remoteFile"));
            System.out.println("============================================================================================================");
            
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
    }
    
    @Test
    public void copyToLocalFile() throws IOException {
        System.getProperties().setProperty("HADOOP_USER_NAME", "hdfs");
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://192.168.1.3:8020");
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            
            System.out.println("============================================================================================================");
            fs.copyToLocalFile(new Path("D:\\remoteFile"), new Path("/test/localFile"));
            fs.moveToLocalFile(new Path("D:\\remoteFile"), new Path("/test/localFile"));
            fs.copyToLocalFile(false, new Path("D:\\remoteFile"), new Path("/test/localFile"));
            fs.copyToLocalFile(false, new Path("D:\\remoteFile"), new Path("/test/localFile"), true);
            System.out.println("============================================================================================================");
            
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
    }
    
}
