package com.hscloud.hs.cost.account.model.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class GroupExcelVo extends BaseStatementExcelVo implements Serializable {

    private static final long serialVersionUID = 1L;


    @ExcelProperty("核算分组")
    private String groupId;

    @ExcelProperty("任务名称")
    private String accountTaskName;

}
