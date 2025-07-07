package com.hscloud.hs.cost.account.model.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class IndexExcelVo extends BaseStatementExcelVo implements Serializable{

    private static final long serialVersionUID = 1L;


    @ExcelProperty("核算指标")
    private String indexName;


    @ExcelProperty("核算单元")
    private String unitName;

    @ExcelProperty("核算分组")
    private String groupId;

}
