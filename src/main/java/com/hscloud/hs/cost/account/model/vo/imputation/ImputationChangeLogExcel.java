package com.hscloud.hs.cost.account.model.vo.imputation;

import com.alibaba.excel.annotation.ExcelProperty;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/29 16:33
 */
@Data
public class ImputationChangeLogExcel {

    @ExcelProperty("应用")
    private String changeModel;

    @ExcelProperty("操作类型")
    private String changeType;

    @ExcelProperty("操作项")
    private String changeItem;

    @ExcelProperty("操作时间")
    private String changeTime;


    @ExcelProperty("操作人")
    private String changeUserName;

    @ExcelProperty("描述")
    private String changeDesc;
}
