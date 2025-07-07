package com.hscloud.hs.cost.account.utils.kpi.excel;

/**
 * @Classname EasyExcelUtils
 * @Description TODO
 * @Date 2024-01-10 11:16
 * @Created by sch
 */

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import com.alibaba.excel.write.style.row.SimpleRowHeightStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.hscloud.hs.cost.account.model.dto.kpi.NoModelWriteData;
import com.hscloud.hs.cost.account.utils.kpi.TimestampStringConverter;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

@Component
public class EasyExcelUtils {


    //不创建对象的导出
    public void noModleWrite(NoModelWriteData data, HttpServletResponse response) throws IOException {
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        try {
//            response.setContentType("application/vnd.ms-excel");
            //response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode(data.getFileName(), "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            response.setContentType("application/vnd.ms-excel");
            // 这里需要设置不关闭流
            EasyExcel.write(response.getOutputStream()).head(head(data.getHeadMap())).
                    registerConverter(new TimestampStringConverter())
                    // 简单的行高策略：头行高，内容行高
                    .registerWriteHandler(new SimpleRowHeightStyleStrategy((short) 25, (short) 20))
                    // 简单的列宽策略，列宽20
                    .registerWriteHandler(new SimpleColumnWidthStyleStrategy(35))
                    .registerWriteHandler(new ExcelCellStyleStrategy())
                    .sheet(data.getFileName()).doWrite(dataList(data.getDataList(), data.getDataStrMap()));
        } catch (Exception e) {
            // 重置response
            //response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, String> map = new HashMap<String, String>();
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }

    public void noModleWrite_CSV(NoModelWriteData data, HttpServletResponse response) throws IOException {
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        try {
//            response.setContentType("application/vnd.ms-excel");
            //response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode(data.getFileName(), "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".csv");
            response.setContentType("application/vnd.ms-excel");
            // 这里需要设置不关闭流
            EasyExcel.write(response.getOutputStream()).head(head(data.getHeadMap())).excelType(ExcelTypeEnum.CSV).
                    registerConverter(new TimestampStringConverter())
                    // 简单的行高策略：头行高，内容行高
                    .registerWriteHandler(new SimpleRowHeightStyleStrategy((short) 25, (short) 20))
                    // 简单的列宽策略，列宽20
                    .registerWriteHandler(new SimpleColumnWidthStyleStrategy(35))
                    .registerWriteHandler(new ExcelCellStyleStrategy())
                    .sheet(data.getFileName()).doWrite(dataList(data.getDataList(), data.getDataStrMap()));
        } catch (Exception e) {
            // 重置response
            //response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, String> map = new HashMap<String, String>();
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }


    //设置表头
    private List<List<String>> head(String[] headMap) {
        List<List<String>> list = new ArrayList<List<String>>();

        for (String head : headMap) {
            List<String> headList = new ArrayList<String>();
            headList.add(head);
            list.add(headList);
        }
        return list;
    }

    //设置导出的数据内容
    private List<List<Object>> dataList(List<LinkedHashMap<String, Object>> dataList, String[] dataStrMap) {
        List<List<Object>> list = new ArrayList<List<Object>>();
        for (Map<String, Object> map : dataList) {
            List<Object> data = new ArrayList<Object>();
            for (int i = 0; i < dataStrMap.length; i++) {
                data.add(map.get(dataStrMap[i]));
            }
            list.add(data);
        }
        return list;
    }
}
