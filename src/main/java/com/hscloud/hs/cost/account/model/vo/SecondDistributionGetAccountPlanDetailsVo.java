package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * @author banana
 * @create 2023-11-28 12:54
 */
@Data
public class SecondDistributionGetAccountPlanDetailsVo {

    @Schema(description = "方案配置表id")
    private Long planId;

    @Schema(description = "核算指标配置信息")
    List<SecondDistributionAccountIndexInfoVo> accountIndexInfoList = new ArrayList<>();

    @Schema(description = "总分配公式")
    SecondDistributionPlanConfigFormulaVo  configFormula;

}
