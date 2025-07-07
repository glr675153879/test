package com.hscloud.hs.cost.account.utils;

import com.alibaba.excel.EasyExcel;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class EasyExcelUtil {

    /**
     * 单行表头excel导出
     *
     * @param response 请求回复
     * @param fileName 文件名称
     * @param heads 列头
     * @param dataList 数据
     */
    public static void importExcel(HttpServletResponse response, String fileName, List<String> heads, List<List<String>> dataList) throws IOException {
        fileName = URLEncoder.encode(fileName, "UTF-8");
        response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xlsx");
        response.setContentType("application/octet-stream");

        EasyExcel.write(response.getOutputStream()).head(getHead(heads)).sheet().doWrite(getBody(dataList));
    }

    private static List<List<String>> getHead(List<String> heads){
        List<List<String>> head = new ArrayList<>();
        for (String vo:heads){
            List<String> vos = Collections.singletonList(vo);
            head.add(vos);
        }
        return head;
    }

    /**
     * @param dataList 列数据
     * @return 行数据
     */
    private static List<List<String>> getBody(List<List<String>> dataList){
        List<List<String>> body = new ArrayList<>();
        for (int i=0;i<dataList.get(0).size();i++){
            List<String> values = new ArrayList<>();
            for (List<String> data:dataList){
                values.add(data.get(i));
            }
            body.add(values);
        }
        return body;
    }
}
