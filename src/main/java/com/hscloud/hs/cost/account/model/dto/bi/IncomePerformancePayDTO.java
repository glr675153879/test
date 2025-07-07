package com.hscloud.hs.cost.account.model.dto.bi;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "护理绩效")
public class IncomePerformancePayDTO {

    @Schema(description = "周期")
    private String cycle;

    @Schema(description = "全院绩效总值")
    private BigDecimal qyTotal;

    @Schema(description = "全院人均绩效")
    private BigDecimal qyAvg;

    @Schema(description = "绩效工资与医疗收入占比")
    private BigDecimal performanceIncomeRatio;

    @Schema(description = "当年所有月份累计之和")
    private BigDecimal yearTotal;

    @Schema(description = "全院绩效总值环比")
    private BigDecimal qyTotalQoqRate;

    @Schema(description = "全院人均绩效环比")
    private BigDecimal qyAvgQoqRate;

}
