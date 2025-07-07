package com.hscloud.hs.cost.account.model.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "护理业绩绩效列表展示出参")
public class NurseAchievementVo {

    @Schema(description = "核算单元id")
    private Long accountUnitId;

    @Schema(description = "核算单元护理组")
    private String accountUnitNur;

    @Schema(description = "收入合计")
    private BigDecimal totalIncome;

    @Schema(description = "成本合计")
    private BigDecimal totalCost;

    @Schema(description = "科室核算单元核算业绩分")
    private BigDecimal performanceScore;

    @Schema(description = "绩效点值")
    private Double perfPoint;

}
