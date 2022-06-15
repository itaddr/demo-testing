package com.itaddr.demo.testing;

import com.alibaba.excel.EasyExcel;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EasyExcelTest {

    @Test
    public void test01() throws IOException {
        String filePath = "D:\\easy-excel-test.xlsx";
        try (OutputStream os = new FileOutputStream(filePath)) {
            EasyExcel.write(os, DemoBean.class).sheet("模板").doWrite(getData());
        }
    }

    private List<DemoBean> getData() {
        List<DemoBean> users = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            DemoBean bean = new DemoBean();
            bean.setBirthday(new Date());
            bean.setName("user_" + i);
            bean.setSalary(1.285 * i);
            bean.setTelphone("1888888888" + i);
            users.add(bean);
        }
        return users;
    }

}
