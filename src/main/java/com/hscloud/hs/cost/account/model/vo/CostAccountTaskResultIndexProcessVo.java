package com.hscloud.hs.cost.account.model.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "核算结果指标Vo")
public class CostAccountTaskResultIndexProcessVo {

    @Schema(description = "指标项id")
    private Long indexId;

    @Schema(description = "配置项指标名称")
    private String configIndexName;

    @Schema(description = "配置项key")
    private String configKey;

    @Schema(description = "指标公式")
    private String indexFormula;

    @Schema(description = "核算指标原本的值")
    private BigDecimal noExtraIndexCount;

    @Schema(description = "核算指标总值")
    private BigDecimal indexTotalValue;

    @Schema(description = "核算指标")
    private List<CostAccountTaskResultIndexProcessVo> configIndexList;

    @Schema(description = "核算项")
    private List<CostAccountTaskItemVo> configItemList;

    @Schema(description = "医护分摊对象")
    private List<CostAccountTaskMedicalAllocationVo> medicalAllocation;

    @Schema(description = "借床被分摊核算值")
    private BigDecimal bedBorrow;

    @Schema(description = "病区被分摊值")
    private BigDecimal endemicArea;

    @Schema(description = "门诊共用分摊值")
    private BigDecimal outpatientShard;
}
