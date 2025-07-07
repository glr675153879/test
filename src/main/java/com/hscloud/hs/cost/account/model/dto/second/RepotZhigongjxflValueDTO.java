package com.hscloud.hs.cost.account.model.dto.second;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "职工绩效分类")
@HeadRowHeight(20)
@ColumnWidth(20)
public class RepotZhigongjxflValueDTO {
    @ExcelIgnore
    @Schema(description = "周期")
    private String cycle;

    @ExcelIgnore
    @Schema(description = "userId")
    private String userId;

    @ExcelProperty("职工姓名")
    private String userName;


    @ExcelProperty("管理绩效")
    private String guanlijx;

    @ExcelProperty("门诊绩效")
    private String menzhenjx;

    @ExcelIgnore
    @ExcelProperty("二次分配总金额")
    @Deprecated
    private BigDecimal secondAmt;

    @ExcelProperty("科室绩效")
    //绩效二次分配中的绩效结果（不含鄞州门诊医生组发放单元分配的钱）
    private BigDecimal secondAmtWithoutYz;

    @ExcelProperty("鄞州门诊")
    //：绩效二次分配中鄞州门诊医生组发放单元分配的钱
    private BigDecimal secondYzAmt;

    @ExcelProperty("明湖院区")
    //：绩效二次分配中鄞州门诊医生组发放单元分配的钱
    private BigDecimal secondYhAmt;

    @ExcelProperty("合计")
    private BigDecimal totalAmt = BigDecimal.ZERO;

    @ExcelIgnore
    @Schema(description = "考勤组")
    private String attendanceGroup;


}
