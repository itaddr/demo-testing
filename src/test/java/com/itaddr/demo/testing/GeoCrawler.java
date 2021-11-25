package com.itaddr.demo.testing;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;

/**
 * @Title null.java
 * @Package com.itaddr.demo.testing
 * @Author 马嘉祺
 * @Date 2021/10/27 16:09
 * @Description
 */
public class GeoCrawler {

    private static final String[] UAS = {
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 BIDUBrowser/8.3 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 Core/1.47.277.400 QQBrowser/9.4.7658.400",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 UBrowser/5.6.12150.8 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36 SE 2.X MetaSr 1.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36 TheWorld 7",
            "Mozilla/5.0 (Windows NT 6.1; W…) Gecko/20100101 Firefox/60.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36 OPR/37.0.2178.32",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.57.2 (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586",
            "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko",
            "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)",
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)",
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0)",};

    private static int NUM = new Random().nextInt(UAS.length - 1);

    private static String getUserAgent() {
        if (NUM == UAS.length) {
            NUM = 0;
        }
        return UAS[NUM++];
    }

    /**
     * 如果出现（Unexpected end of file from server）异常，可以适当将Thread.sleep改大.
     */
    public static Document httpGet(String url) throws IOException {
        Connection con = Jsoup.connect(url);
        con.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        con.header("Accept-Encoding", "gzip, deflate");
        con.header("Accept-Language", "zh-CN,zh;q=0.9");
        con.header("Cache-Control", "max-age=0");
        con.header("Connection", "keep-alive");
        con.header("Host", "www.stats.gov.cn");
        String ua = getUserAgent();
//        System.out.println(ua);
        con.header("User-Agent", ua);
        con.timeout(60000);

        Document doc;
        for (int i = 0; ; ++i) {
            try {
                doc = con.get();
            } catch (SocketTimeoutException e) {
                if (i < 3) {
                    System.err.println("请求超时" + i + "次");
                    continue;
                } else {
                    throw e;
                }
            }
            break;
        }

        LockSupport.parkNanos(GeoCrawler.class, 1000000000);

        return doc;
    }

    public static List<Geo> getProvinceList(String year) throws IOException {
        String url = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/" + year + "/index.html";
        System.out.println(url);
        Document document = httpGet(url);
        Elements trs = document.body().getElementsByAttributeValue("class", "provincetr");
        List<Geo> provinceList = new ArrayList<>();
        for (Element tr : trs) {
            Elements tds = tr.children();
            for (Element td : tds) {
                Elements a = td.select("a");
                if (a.isEmpty()) {
                    continue;
                }
                Integer code = Integer.parseInt(a.attr("href").replace(".html", ""));
                String name = a.text().replace("<br>", "");
                provinceList.add(new Geo().setLevel(1).setParentCode(0).setCode(code).setName(name));

//                System.out.println(code + "  " + name);
            }
        }
        return provinceList;
    }

    private static List<Geo> getCityList(String year, Integer provinceCode) throws IOException {
        String url = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/" + year + "/" + provinceCode + ".html";
        System.out.println(url);
        Document document = httpGet(url);
        Elements trs = document.body().getElementsByAttributeValue("class", "citytr");
        List<Geo> cityList = new ArrayList<>();
        for (Element tr : trs) {
            Elements tds = tr.children();
            Elements codeA = tds.get(0).select("a");
            Elements nameA = tds.get(1).select("a");
            if (codeA.isEmpty() || nameA.isEmpty()) {
                continue;
            }
            Integer code = Integer.parseInt(codeA.text().substring(0, 4));
            String name = nameA.text();
            cityList.add(new Geo().setLevel(2).setParentCode(provinceCode).setCode(code).setName(name));

//            System.out.println(code + "  " + name);
        }
        return cityList;
    }

    private static List<Geo> getAreaList(String year, Integer provinceCode, Integer cityCode) throws IOException {
        String url = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/" + year + "/" + provinceCode + "/" + cityCode + ".html";
        System.out.println(url);
        Document document = httpGet(url);
        Elements trs = document.body().getElementsByAttributeValue("class", "countytr");
        List<Geo> areaList = new ArrayList<>();
        for (Element tr : trs) {
            Elements tds = tr.children();
            Elements codeA = tds.get(0).select("a");
            Elements nameA = tds.get(1).select("a");
            if (codeA.isEmpty() || nameA.isEmpty()) {
                continue;
            }
            Integer code = Integer.parseInt(codeA.text().substring(0, 6));
            String name = nameA.text();
            areaList.add(new Geo().setLevel(3).setParentCode(cityCode).setCode(code).setName(name));

//            System.out.println(code + "  " + name);
        }
        return areaList;
    }

    private static List<Geo> getStreetList(String year, Integer provinceCode, Integer cityCode, Integer areaCode) throws IOException {
        String url = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/" + year + "/" + provinceCode + "/" + String.format("%02d", cityCode % 100) + "/" + areaCode + ".html";
        System.out.println(url);
        Document document = httpGet(url);
        Elements trs = document.body().getElementsByAttributeValue("class", "towntr");
        List<Geo> streetList = new ArrayList<>();
        for (Element tr : trs) {
            Elements tds = tr.children();
            Elements codeA = tds.get(0).select("a");
            Elements nameA = tds.get(1).select("a");
            if (codeA.isEmpty() || nameA.isEmpty()) {
                continue;
            }
            Integer code = Integer.parseInt(codeA.text().substring(0, 9));
            String name = nameA.text();
            streetList.add(new Geo().setLevel(3).setParentCode(cityCode).setCode(code).setName(name));

//            System.out.println(code + "  " + name);
        }
        return streetList;
    }

    @Test
    public void writeProvinceTest() throws IOException {
        String year = "2020";
        File provinceFile = new File("E:\\provinceFile.txt");
        File provinceSqlFile = new File("E:\\province.sql");
        if (!provinceFile.exists()) {
            provinceFile.createNewFile();
        }
        if (!provinceSqlFile.exists()) {
            provinceSqlFile.createNewFile();
        }
        String url = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/" + year + "/index.html";
        try (PrintWriter filePrint = new PrintWriter(new FileOutputStream(provinceFile, false)); PrintWriter sqlPrint = new PrintWriter(new FileOutputStream(provinceSqlFile, false))) {
            List<Geo> provinceList = getProvinceList(year);
            for (Geo province : provinceList) {
                filePrint.println(province.getCode() + "," + province.getName() + "," + url);
                String sqlLine = String.format("INSERT INTO `pig`.`hb_config_regions` (`region_type`, `parent_code`, `region_code`, `region_name`, `create_id`, `update_id`) VALUES (2, null, '%d', '%s', 1, 1) -- %s", province.getCode(), province.getName(), url);
                sqlPrint.println(sqlLine);
            }
        }
    }

    @Test
    public void writeCityTest() throws IOException {
        String year = "2020";
        File cityFile = new File("E:\\cityFile.txt");
        File citySqlFile = new File("E:\\city.sql");
        if (!cityFile.exists()) {
            cityFile.createNewFile();
        }
        if (!citySqlFile.exists()) {
            citySqlFile.createNewFile();
        }
        try (
                LineNumberReader reader = new LineNumberReader(new FileReader("E:\\provinceFile.txt"));
                PrintWriter filePrint = new PrintWriter(new FileOutputStream(cityFile, false));
                PrintWriter sqlPrint = new PrintWriter(new FileOutputStream(citySqlFile, false))
        ) {
            String line;
            while (null != (line = reader.readLine())) {
                String[] split = line.split(",");
                int provinceCode = Integer.parseInt(split[0]);
                String provinceName = split[1];
                String url = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/" + year + "/" + provinceCode + ".html";

                List<Geo> cityList = getCityList(year, provinceCode);
                for (Geo city : cityList) {
                    filePrint.println(provinceCode + "," + provinceName + "," + city.getCode() + "," + city.getName() + "," + url);
                    String sqlLine = String.format("INSERT INTO `pig`.`hb_config_regions` (`region_type`, `parent_code`, `region_code`, `region_name`, `create_id`, `update_id`) VALUES (3, '%s', '%d', '%s', 1, 1) -- %s", provinceCode, city.getCode(), city.getName(), url);
                    sqlPrint.println(sqlLine);
                }
            }
        }
    }

    @Test
    public void writeAreaTest() throws IOException {
        String year = "2020";
        File areaFile = new File("E:\\areaFile.txt");
        File areaSqlFile = new File("E:\\area.sql");
        if (!areaFile.exists()) {
            areaFile.createNewFile();
        }
        if (!areaSqlFile.exists()) {
            areaSqlFile.createNewFile();
        }
        try (
                LineNumberReader reader = new LineNumberReader(new FileReader("E:\\cityFile.txt"));
                PrintWriter filePrint = new PrintWriter(new FileOutputStream(areaFile, false));
                PrintWriter sqlPrint = new PrintWriter(new FileOutputStream(areaSqlFile, false))
        ) {
            String line;
            while (null != (line = reader.readLine())) {
                String[] split = line.split(",");
                int provinceCode = Integer.parseInt(split[0]), cityCode = Integer.parseInt(split[2]);
                String provinceName = split[1], cityName = split[3];
                String url = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/" + year + "/" + provinceCode + "/" + cityCode + ".html";

                List<Geo> areaList = getAreaList(year, provinceCode, cityCode);
                for (Geo area : areaList) {
                    filePrint.println(provinceCode + "," + provinceName + "," + cityCode + "," + cityName + "," + area.getCode() + "," + area.getName() + "," + url);
                    String sqlLine = String.format("INSERT INTO `pig`.`hb_config_regions` (`region_type`, `parent_code`, `region_code`, `region_name`, `create_id`, `update_id`) VALUES (4, '%s', '%d', '%s', 1, 1) -- %s", cityCode, area.getCode(), area.getName(), url);
                    sqlPrint.println(sqlLine);
                }
            }
        }
    }

    @Test
    public void writeStreetTest() throws IOException {
        String year = "2020";
        File streetFile = new File("E:\\streetFile.txt");
        File streetSqlFile = new File("E:\\street.sql");
        if (!streetFile.exists()) {
            streetFile.createNewFile();
        }
        if (!streetSqlFile.exists()) {
            streetSqlFile.createNewFile();
        }
        try (
                LineNumberReader reader = new LineNumberReader(new FileReader("E:\\areaFile.txt"));
                PrintWriter filePrint = new PrintWriter(new FileOutputStream(streetFile, false));
                PrintWriter sqlPrint = new PrintWriter(new FileOutputStream(streetSqlFile, false))
        ) {
            String line;
            while (null != (line = reader.readLine())) {
                String[] split = line.split(",");
                int provinceCode = Integer.parseInt(split[0]), cityCode = Integer.parseInt(split[2]), areaCode = Integer.parseInt(split[4]);
                String provinceName = split[1], cityName = split[3], areaName = split[5];
                String url = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/" + year + "/" + provinceCode + "/" + String.format("%02d", cityCode % 100) + "/" + areaCode + ".html";

                List<Geo> streetList = getStreetList(year, provinceCode, cityCode, areaCode);
                for (Geo street : streetList) {
                    filePrint.println(provinceCode + "," + provinceName + "," + cityCode + "," + cityName + "," + areaCode + "," + areaName + "," + street.getCode() + "," + street.getName() + "," + url);
                    String sqlLine = String.format("INSERT INTO `pig`.`hb_config_regions` (`region_type`, `parent_code`, `region_code`, `region_name`, `create_id`, `update_id`) VALUES (5, '%s', '%d', '%s', 1, 1) -- %s", areaCode, street.getCode(), street.getName(), url);
                    sqlPrint.println(sqlLine);
                }
            }
        }
    }

    /**
     * 创建文件
     */
//    public static boolean createFile(File fileName) throws Exception {
//        boolean flag = false;
//        try {
//            if (!fileName.exists()) {
//                fileName.createNewFile();
//                flag = true;
//            } else {
//                fileName.delete();
//                fileName.createNewFile();
//                flag = true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return flag;
//    }

//    public static boolean writeTxtFile(String content, File fileName) throws Exception {
//        RandomAccessFile mm = null;
//        boolean flag = false;
//        FileOutputStream o = null;
//        try {
//            o = new FileOutputStream(fileName);
//            o.write(content.getBytes("UTF-8"));
//            o.close();
//            flag = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (mm != null) {
//                mm.close();
//            }
//        }
//        return flag;
//    }
    @Test
    public void test() throws Exception {
        String year = "2020";
        String exportPath = "E:\\china-geo.txt";

        List<Geo> provinceList = getProvinceList(year);
        for (Geo province : provinceList) {
            List<Geo> cityList = getCityList(year, province.getCode());

            for (Geo city : cityList) {
                List<Geo> areaList = getAreaList(year, province.getCode(), city.getCode());

                for (Geo area : areaList) {
                    List<Geo> streetList = getStreetList(year, province.getCode(), city.getCode(), area.getCode());

//                    System.out.println("--------------------------------");
                }
//                System.out.println("--------------------------------");
//                System.out.println("--------------------------------");
            }
//            System.out.println("--------------------------------");
//            System.out.println("--------------------------------");
//            System.out.println("--------------------------------");
        }


//        List<Geo> citys = new ArrayList<Geo>();
//        for (int i = 0; i < provinceList.size(); i++) {
//            Geo province = provinceList.get(i);
//            citys.addAll(getCityList(year, province.getCode()));
//        }
//
//        List<Geo> areas = new ArrayList<Geo>();
//        for (int i = 0; i < citys.size(); i++) {
//            Geo city = citys.get(i);
//            areas.addAll(getAreaList(year, city.getParentCode(), city.getCode()));
//        }
//
//        List<Geo> streets = new ArrayList<Geo>();
//        for (int i = 0; i < citys.size(); i++) {
//            Geo area = areas.get(i);
//            streets.addAll(getStreetList(year, area.getParentCode(), area.getCode()));
//        }


//        File file = new File(exportPath);
//        createFile(file);

//        StringBuffer stringBuffer = new StringBuffer();
//        for (int i = 0; i < provinces.size(); i++) {
//            Geo province = provinces.get(i);
//            stringBuffer.append(province.getLevel() + "\t" + province.getParentCode() + "\t" + province.getCode() + "\t" + province.getName() + "\n");
//        }
//        for (int i = 0; i < citys.size(); i++) {
//            Geo city = citys.get(i);
//            stringBuffer.append(city.getLevel() + "\t" + city.getParentCode() + "\t" + city.getCode() + "\t" + city.getName() + "\n");
//        }
//        for (int i = 0; i < areas.size(); i++) {
//            Geo area = areas.get(i);
//            stringBuffer.append(area.getLevel() + "\t" + area.getParentCode() + "\t" + area.getCode() + "\t" + area.getName() + "\n");
//        }
//        writeTxtFile(stringBuffer.toString(), file);
    }

}
