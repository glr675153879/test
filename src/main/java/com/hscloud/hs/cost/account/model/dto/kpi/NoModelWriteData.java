package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Classname NoModelWriteData
 * @Description TODO
 * @Date 2024-01-10 11:15
 * @Created by sch
 */
@Data
public class NoModelWriteData {
    private String fileName;//文件名
    private String[] headMap;//表头数组
    private String[] dataStrMap;//对应数据字段数组
    private List<LinkedHashMap<String, Object>> dataList;//数据集合
}
