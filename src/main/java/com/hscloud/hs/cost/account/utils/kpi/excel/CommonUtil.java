package com.hscloud.hs.cost.account.utils.kpi.excel;

/**
 * @Classname CommonUtil
 * @Description TODO
 * @Date 2025/4/16 13:54
 * @Created by sch
 */

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.nacos.api.model.v2.Result;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.google.common.io.ByteStreams.copy;

public class CommonUtil {
    // linux 下
    public static final String XX_PATH = "listdata" + File.separator;

    // windows下
    // public static final String XX_PATH = "listdata/";
    /*
     * 自定义：自增流水号工具类
     *
     * @param prefix
     * @param id
     */
    private static final Integer ONE = 1;
    private static final Integer TWO = 2;
    private static final Integer THREE = 3;

    /**
     * 获取两个数组的不同元素
     *
     * @param t1
     * @param t2
     * @return
     */
    public static <T> List<T> compare(T[] t1, T[] t2) {
        //将t1数组转成list数组
        List<T> list1 = Arrays.asList(t1);

        for (int i = 0; i < list1.size(); i++) {
            String header = list1.get(i).toString().replace("\n", "");
            list1.set(i, (T) header);
        }
        //用来存放2个数组中不相同的元素
        List<T> list2 = new ArrayList<T>();
        for (T t : t2) {
            if (!list1.contains(t)) {
                list2.add(t);
            }
        }
        return list2;
    }

    /**
     * 获取模板表头信息
     *
     * @param headers
     * @return
     */
    public static List<List<String>> getTemplateHeaders(List<Map<String, Object>> headers) {
        List<List<String>> cells = new ArrayList<List<String>>();
        for (Map<String, Object> header : headers) {
            List<String> headerList = new ArrayList<String>();
            headerList.add(header.get("headerName").toString());
            cells.add(headerList);
        }
        return cells;
    }

    /**
     * 清单上传校验
     *
     * @param multipartFile
     * @param
     * @return
     * @throws Exception
     */
    public static Result checkExcel(MultipartFile multipartFile,
                                    String[] headers) throws Exception {
        String fileName = multipartFile.getOriginalFilename();
        // 校验后缀
        if (!"xls".equals(fileName.split("\\.")[1]) &&
                !"xlsx".equals(fileName.split("\\.")[1])) {
            return Result.failure("上传文件格式不正确，请检查");
        }
        String[][] data = getExcelData(multipartFile);
        // 对excel表头校验
        String[] dataArray = data[0];
        List<String> list = CommonUtil.compare(dataArray, headers);
        if (list != null && list.size() > 0) {
            return Result.failure(ExcelUtil.getStringMessage(list).toString());
        }
        return null;

    }

    /**
     * 读取excel数据
     *
     * @param multipartFile
     * @return
     * @throws Exception
     */
    public static String[][] getExcelData(MultipartFile multipartFile) throws Exception {
        String[][] data;
        String fileName = multipartFile.getOriginalFilename();
        InputStream inputStream = multipartFile.getInputStream();
        String flag = fileName.substring(fileName.lastIndexOf("."));
        if (".xlsx".equals(flag)) {
            data = ExcelUtil.getXlsxData(inputStream, 0);
        } else {
            data = ExcelUtil.getXlsData(inputStream, 0);
        }
        return data;
    }

    /**
     * excel表头转换成sql中的字段
     *
     * @param data
     * @param headers
     * @return
     */
    public static List<Map<String, String>> excelCellToSqlType(
            String[][] data, List<Map<String, Object>> headers) {
        List<Map<String, String>> dataPositons = new ArrayList<Map<String, String>>();
        for (int i = 1; i < data.length; i++) {
            Map<String, String> map = new HashMap<String, String>();
            for (int columnNo = 0; columnNo < data[i].length; columnNo++) {
                String columnName = data[0][columnNo].replace("\n", "");
                String columnValue = data[i][columnNo];
                map.put(columnName, columnValue);
            }
            dataPositons.add(map);
        }
        dataPositons = changeColumnName(dataPositons, headers);
        return dataPositons;
    }

    // excel表头转换成sql中的字段2
    public static List<LinkedHashMap<String, Object>> excelCellToSqlTypes(
            String[][] data, List<LinkedHashMap<String, Object>> headers) {
        List<LinkedHashMap<String, Object>> dataPositons = new ArrayList<LinkedHashMap<String, Object>>();
        for (int i = 1; i < data.length; i++) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            for (int columnNo = 0; columnNo < data[i].length; columnNo++) {
                String columnName = data[0][columnNo].replace("\n", "");
                String columnValue = data[i][columnNo];
                map.put(columnName, columnValue);
            }
            dataPositons.add(map);
        }
        dataPositons = changeColumnNames(dataPositons, headers);
        return dataPositons;
    }

    /**
     * 属性名与属性转换
     *
     * @param dataPositons
     * @param headers
     * @return
     */
    private static List<Map<String, String>> changeColumnName(
            List<Map<String, String>> dataPositons,
            List<Map<String, Object>> headers) {
        List<Map<String, String>> dataPositonsNews = new ArrayList<Map<String, String>>();
        for (Map<String, String> xxPostion : dataPositons) {
            Map<String, String> keyPositionNew = new HashMap<String, String>();
            for (Map<String, Object> header : headers) {
                if (!StringUtils.isEmpty(xxPostion.get(header.get("headerName")))) {
                    keyPositionNew.put(header.get("headerField").toString(),
                            xxPostion.get(header.get("headerName")));
                }
            }
            dataPositonsNews.add(keyPositionNew);
        }
        return dataPositonsNews;
    }

    // 属性名与属性转换2
    private static List<LinkedHashMap<String, Object>> changeColumnNames(
            List<LinkedHashMap<String, Object>> dataPositons,
            List<LinkedHashMap<String, Object>> headers) {
        List<LinkedHashMap<String, Object>> dataPositonsNews = new ArrayList<LinkedHashMap<String, Object>>();
        for (LinkedHashMap<String, Object> xxPostion : dataPositons) {
            LinkedHashMap<String, Object> keyPositionNew = new LinkedHashMap<String, Object>();
            for (LinkedHashMap<String, Object> header : headers) {
                Object o = xxPostion.get(header.entrySet().iterator().next().getValue());
                if (o != null) {
                    keyPositionNew.put(header.entrySet().iterator().next().getKey(),
                            o);
                }
            }
            dataPositonsNews.add(keyPositionNew);
        }
        return dataPositonsNews;
    }

    public static String getObjectKey(String filePath) {
        return XX_PATH + filePath;
    }

    public static byte[] inputStreamToByte(InputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    /**
     * 由年月日时分秒毫秒  生成流水号
     *
     * @return
     */
    public static String getSerialNumber(String strFlag) {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String dateString = formatter.format(currentTime);
        return strFlag + dateString;
    }

    public static String getId(String prefix, Integer id) {
        //判断位数
        String s = id + "";
        int count = s.length();
        String producerNum = prefix;
        if (ONE == count) {
            producerNum += "000" + String.valueOf(id);
        } else if (TWO == count) {
            producerNum += "00" + String.valueOf(id);
        } else if (THREE == count) {
            producerNum += "0" + String.valueOf(id);
        } else {
            producerNum += String.valueOf(id);
        }
        return producerNum;
    }

}

