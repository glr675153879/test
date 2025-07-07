package com.hscloud.hs.cost.account.utils.kpi;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 导入导出EXCEL
 */
public class ExcelUtil extends AbstractColumnWidthStyleStrategy {

    public static void writeExcel(HttpServletResponse response, List<? extends Object> data, String fileName, String sheetName, Class clazz) throws Exception {
        //表头样式
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        //设置表头居中对齐
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //内容样式
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        //设置内容靠左对齐
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        //字体大小
        WriteFont writeFont = new WriteFont();
        writeFont.setFontHeightInPoints((short) 14);
        contentWriteCellStyle.setWriteFont(writeFont);

        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
        EasyExcel.write(getOutputStream(fileName, response), clazz)
                .excelType(ExcelTypeEnum.XLSX).sheet(sheetName).registerWriteHandler(horizontalCellStyleStrategy).doWrite(data);
    }

    public static List<List<String>> simpleHeader(List<String> header){
        List<List<String>> result = new ArrayList<>();
        for (String s : header) {
            List<String> t = ListUtil.of(s);
            result.add(t);
        }
        return result;
    }

    public static List<List<String>> getHeader(){
        List<String> h1 = ListUtil.of("tou1");
        List<String> h2 = ListUtil.of("tou2");
        List<List<String>> hz = new ArrayList<>();
        hz.add(h1);
        hz.add(h2);
        return hz;
    }

    /**
     * 导出
     *
     * @param response
     * @param data
     * @param fileName
     * @param sheetName
     * @param clazz
     * @throws Exception
     */
    public static void writeExcel_merge(HttpServletResponse response, List<? extends Object> data, String fileName, String sheetName, Class clazz) throws Exception {
        //表头样式
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        //设置表头居中对齐
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //内容样式
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        //设置内容靠左对齐
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        //字体大小
        WriteFont writeFont = new WriteFont();
        writeFont.setFontHeightInPoints((short) 14);
        contentWriteCellStyle.setWriteFont(writeFont);

        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
        EasyExcel.write(getOutputStream(fileName, response), clazz).excelType(ExcelTypeEnum.XLSX).sheet(sheetName).registerWriteHandler(horizontalCellStyleStrategy).doWrite(data);
    }

    private static OutputStream getOutputStream(String fileName, HttpServletResponse response) throws Exception {
        fileName = URLEncoder.encode(fileName, "UTF-8");
        response.setContentType("application/octet-stream");
//        response.setCharacterEncoding("utf8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        return response.getOutputStream();
    }

    public static void main(String[] args) {
        String qmc = "区名称";
        List<Integer> 大类1 = ListUtil.of(1,2);
        List<Integer> 大类2 = ListUtil.of(3,4);
        List<List<String>> list = new ArrayList<List<String>>();
        List<String> head0 = new ArrayList<String>();

    }


    // 入参说明：第一个参数，T，是有一个实体类的，在这个实体类里面需要有一些注解。上面有说明
// 第二个参数是指，你想给生成的这个文件存放在那个位置，
    public static void  easyTemplateUtil(List<? extends Object> T, String path, Class clazz,ExcelTypeEnum excelType){

        // 头的策略
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        // 内容的策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        //设置边框
        contentWriteCellStyle.setBorderTop(BorderStyle.THIN);
        contentWriteCellStyle.setBorderRight(BorderStyle.THIN);
        contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);
        //设置自动换行
        contentWriteCellStyle.setWrapped(true);
        //水平居中
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //垂直居中
        contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //内容字体
        WriteFont contentFont = new WriteFont();
        contentFont.setFontHeightInPoints((short) 11);
        contentWriteCellStyle.setWriteFont(contentFont);
        //头部字体
        WriteFont headFont = new WriteFont();
        headFont.setFontHeightInPoints((short) 11);//字体大小
        headWriteCellStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());//背景颜色
        headWriteCellStyle.setWriteFont(headFont);
        // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
        try {
            //   ApplyExcelMailVo.class 指的是 内容部分
            ExcelWriter excelWriter = EasyExcel.write(new FileOutputStream(path), clazz)
                     .excelType(excelType)
                    .charset(Charset.forName("GBK"))
                    .registerWriteHandler(horizontalCellStyleStrategy)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()).build();
            //  ApplyExcelMailVo.class 指的是 列表部分
            WriteSheet writeSheet1 = EasyExcel.writerSheet(0, "sheet1").head(clazz).build();
            excelWriter.write(T, writeSheet1);
            excelWriter.finish();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

    }


}
