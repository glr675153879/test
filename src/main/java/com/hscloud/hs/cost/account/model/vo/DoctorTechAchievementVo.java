package com.hscloud.hs.cost.account.model.vo;


import com.hscloud.hs.cost.account.model.pojo.AdsIncomePerformanceScoreDoc;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "医生医技业绩绩效列表展示出参")
public class DoctorTechAchievementVo {

    @Schema(description = "核算单元id")
    private Long accountUnitId;

    @Schema(description = "核算单元")
    private String accountUnitDoc;

    @Schema(description = "收入合计")
    private BigDecimal incomeTotal;

    @Schema(description = "成本合计")
    private BigDecimal costTotal;

    @Schema(description = "住院收入")
    private BigDecimal inpatIncome;

    @Schema(description = "医保考核收入")
    private BigDecimal insuranceIncome;

    @Schema(description = "住院考核指标")
    private BigDecimal inpatExamIndex;

    @Schema(description = "科室绩效点数")
    private Double depPerfPoint;

    @Schema(description = "住院绩效点数追加")
    private Double inpatPerfPoint;

    @Schema(description = "科室核算单元核算业绩分")
    private BigDecimal performanceScore;

}
