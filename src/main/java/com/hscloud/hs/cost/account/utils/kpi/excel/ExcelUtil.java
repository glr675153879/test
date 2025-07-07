package com.hscloud.hs.cost.account.utils.kpi.excel;

/**
 * @Classname ExcelUtil
 * @Description TODO
 * @Date 2025/4/16 14:00
 * @Created by sch
 */

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelUtil {
    public static final Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

    /**
     * 读取Excel的内容，第一维数组存储的是一行中格列的值，二维数组存储的是第几行
     *
     * @param in         读取数据的源Excel
     * @param ignoreRows 读取数据忽略的行数，比喻行头不需要读入 忽略的行数为1
     * @return 读出的Excel中数据的内容
     * @throws IOException 异常
     */
    public static String[][] getXlsxData(InputStream in, int ignoreRows)
            throws IOException {
        List<String[]> result = new ArrayList<String[]>();
        int rowSize = 0;
        XSSFWorkbook wb = new XSSFWorkbook(in);
        XSSFCell cell = null;
        for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
            XSSFSheet st = wb.getSheetAt(sheetIndex);
            // 第一行为标题，取标题值
            for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
                XSSFRow row = st.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                int tempRowSize = row.getLastCellNum();
                if (tempRowSize > rowSize) {
                    rowSize = tempRowSize;
                }
                String[] values = new String[rowSize];
                Arrays.fill(values, "");
                boolean hasValue = false;
                for (short columnIndex = 0; columnIndex < row.getLastCellNum(); columnIndex++) {
                    String value = "";
                    cell = row.getCell(columnIndex);
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING:
                                value = cell.getStringCellValue().trim();
                                break;
                            case NUMERIC:
                                value = getString(DateUtil.isCellDateFormatted(cell), cell.getDateCellValue(), cell
                                        .getNumericCellValue());
                                break;
                            case FORMULA:
                                // 导入时如果为公式生成的数据则无值
                                if (!"".equals(cell.getStringCellValue())) {
                                    value = cell.getStringCellValue();
                                } else {
                                    value = cell.getNumericCellValue() + "";
                                }
                                break;
                            case BLANK:
                                break;
                            case ERROR:
                                value = "";
                                break;
                            case BOOLEAN:
                                value = (cell.getBooleanCellValue() ? "Y" : "N");
                                break;
                            default:
                                value = "";
                        }
                    }
                    if (columnIndex == 0 && "".equals(value.trim())) {
                        break;
                    }
                    value = value.trim();

                    values[columnIndex] = rightTrim(value);
                    hasValue = true;
                    //System.out.println(value + ":" + columnIndex);
                }

                if (hasValue) {
                    result.add(values);
                }
            }

        }
        return getStrings(in, result, rowSize);
    }

    public static String[][] getXlsData(InputStream in, int ignoreRows) throws IOException {

        List<String[]> result = new ArrayList<String[]>();
        int rowSize = 0;
        // 打开HSSFWorkbook
        POIFSFileSystem fs = new POIFSFileSystem(in);
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFCell cell;
        for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
            HSSFSheet st = wb.getSheetAt(sheetIndex);
            // 第一行为标题，不取
            for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
                HSSFRow row = st.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                int tempRowSize = row.getLastCellNum();
                if (tempRowSize > rowSize) {
                    rowSize = tempRowSize;
                }
                String[] values = new String[rowSize];
                Arrays.fill(values, "");
                boolean hasValue = false;
                for (short columnIndex = 0; columnIndex < row.getLastCellNum(); columnIndex++) {
                    String value = "";
                    cell = row.getCell(columnIndex);
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING:
                                value = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                value = getString(DateUtil.isCellDateFormatted(cell), cell.getDateCellValue(), cell
                                        .getNumericCellValue());
                                break;
                            case FORMULA:
                                // 导入时如果为公式生成的数据则无值
                                if (!cell.getStringCellValue().equals("")) {
                                    value = cell.getStringCellValue();
                                } else {
                                    value = cell.getNumericCellValue() + "";
                                }
                                break;
                            case BLANK:
                                break;
                            case ERROR:
                                value = "";
                                System.out.println(value + ":" + columnIndex);
                                break;
                            case BOOLEAN:
                                value = (cell.getBooleanCellValue() == true ? "Y" : "N");
                                break;
                            default:
                                value = "";
                        }
                    }
                    if (columnIndex == 0 && "".equals(value.trim())) {
                        break;
                    }
                    value = value.trim();
                    values[columnIndex] = rightTrim(value);
                    hasValue = true;
                }

                if (hasValue) {
                    result.add(values);
                }
            }
        }
        return getStrings(in, result, rowSize);
    }

    public static StringBuilder getStringMessage(List<String> list) {
        StringBuilder message = new StringBuilder();
        for (int i = 0, listSize = list.size(); i < listSize; i++) {
            String str = list.get(i);
            if (i == listSize - 1 && listSize > 1) {
                message.append("[").append(str).append("]字段!");
            } else if (i == 0 && listSize > 1) {
                message.append("Excel表头无[").append(str).append("],");
            } else if (i == 0) {
                message.append("Excel表头无[").append(str).append("]字段!");
            } else {
                message.append("[").append(str).append("],");
            }
        }
        return message;
    }

    private static String rightTrim(String str) {
        if (str == null) {
            return "";
        }
        int length = str.length();
        for (int i = length - 1; i >= 0; i--) {
            if (str.charAt(i) != 0x20) {
                break;
            }
            length--;
        }
        return str.substring(0, length);
    }

    private static String[][] getStrings(InputStream in,
                                         List<String[]> result, int rowSize) throws IOException {
        in.close();
        String[][] returnArray = new String[result.size()][rowSize];
        for (int i = 0; i < returnArray.length; i++) {
            returnArray[i] = result.get(i);
        }
        return returnArray;
    }

    private static String getString(boolean cellDateFormatted,
                                    Date dateCellValue, double numericCellValue) {
        String value;
        if (cellDateFormatted) {
            Date date = dateCellValue;
            if (date != null) {
                value = new SimpleDateFormat("yyyy-MM-dd")
                        .format(date);
            } else {
                value = "";
            }
        } else {
            value = new DecimalFormat("#.#").format(numericCellValue);
        }
        return value;
    }

    public static void downExcel(HttpServletResponse response, String fileName,
                                 List<List<String>> headers, String sheetName,
                                 List<List<String>> datas) throws IOException {
        //设置MIME类型，用于指定文件的内容类型，具体来说，这个类型是用于表示EXCEL电子表格文件
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");// 设置字符编码
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx"); // 设置响应头

        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).head(headers)
                .registerWriteHandler(getCellStyle())
                .autoCloseStream(Boolean.TRUE)
                .build();
        int sheetNum = 0;
        WriteSheet writeSheet = EasyExcel.writerSheet(sheetNum, sheetName).build();
        // 写入文件
        excelWriter.write(datas, writeSheet);
        // 关闭文件流
        excelWriter.finish();

    }


    private static HorizontalCellStyleStrategy getCellStyle() {
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        //垂直居中,水平居中
        contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        contentWriteCellStyle.setBorderTop(BorderStyle.THIN);
        contentWriteCellStyle.setBorderRight(BorderStyle.THIN);
        contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);
        contentWriteCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        //背景设置白色
        contentWriteCellStyle.setFillForegroundColor(IndexedColors.WHITE1.getIndex());
        //设置 自动换行
        contentWriteCellStyle.setWrapped(true);
        // 字体策略
        WriteFont contentWriteFont = new WriteFont();
        // 字体大小
        contentWriteFont.setFontHeightInPoints((short) 12);
        contentWriteFont.setColor(IndexedColors.GREY_25_PERCENT.getIndex());
        contentWriteCellStyle.setWriteFont(contentWriteFont);

        // 头样式
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        headWriteCellStyle.setFillForegroundColor(IndexedColors.YELLOW1.index);
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setBold(true);
        headWriteFont.setFontName("宋体");
        headWriteFont.setFontHeightInPoints((short) 11);
        headWriteCellStyle.setWriteFont(headWriteFont);
        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
    }

    /**
     * 处理字段的表头值
     *
     * @param headMap
     * @return
     */
    public static List<String> createdHead(String[] headMap) {
        List<String> list = new ArrayList<String>();
        for (String head : headMap) {
            list.add(head);
        }
        return list;
    }

    public static void uploadExcel(HttpServletResponse response, String fileName,
                                   List<String> columnList, List<List<Object>> dataList) throws Exception {
        //声明输出流
        OutputStream os = null;
        //设置响应头
        setResponseHeader(response, fileName);
        try {
            //获取输出流
            os = response.getOutputStream();
            //内存中保留1000条数据，以免内存溢出，其余写入硬盘
            SXSSFWorkbook wb = new SXSSFWorkbook(1000);
            //获取该工作区的第一个sheet
            Sheet sheet1 = wb.createSheet(fileName);
            int excelRow = 0;
            //创建标题行
            Row titleRow = sheet1.createRow(excelRow++);
            for (int i = 0; i < columnList.size(); i++) {
                //创建该行下的每一列，并写入标题数据
                Cell cell = titleRow.createCell(i);
                cell.setCellValue(columnList.get(i));
            }
            //设置内容行
            if (dataList != null && dataList.size() > 0) {
                //序号是从1开始的
                int count = 1;
                //外层for循环创建行
                for (int i = 0; i < dataList.size(); i++) {
                    Row dataRow = sheet1.createRow(excelRow++);
                    //内层for循环创建每行对应的列，并赋值
                    for (int j = 0; j < dataList.get(0).size(); j++) {//由于多了一列序号列所以内层循环从-1开始
                        Cell cell = dataRow.createCell(j);
                        if (dataList.get(i).get(j) != null) {
                            cell.setCellValue(dataList.get(i).get(j).toString());
                        }
                    }
                }
            }
            //将整理好的excel数据写入流中
            wb.write(os);
        } catch (IOException e) {
            logger.error("===上传清单方法（uploadChecklist） is error===:" + e.getMessage());
            throw e;
        } finally {
            try {
                // 关闭输出流
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                logger.error("===上传清单方法（uploadChecklist） is error===:" + e.getMessage());
                throw e;
            }
        }
    }

    /*
       设置浏览器下载响应头
    */
    private static void setResponseHeader(HttpServletResponse response, String fileName) throws Exception {
        try {
            fileName = new String(fileName.getBytes("UTF-8"), "UTF-8");
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
            response.addHeader("Pargam", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
        } catch (Exception ex) {
            logger.error("===上传清单方法（uploadChecklist） is error===:" + ex.getMessage());
            throw ex;
        }
    }



}

