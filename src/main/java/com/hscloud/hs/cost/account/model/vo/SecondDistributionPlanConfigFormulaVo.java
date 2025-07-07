package com.hscloud.hs.cost.account.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.hscloud.hs.cost.account.model.pojo.SecondDistributionFormula;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author banana
 * @create 2023-11-28 14:37
 */
@Schema
@Data
public class SecondDistributionPlanConfigFormulaVo {

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "保留小数")
    private Integer reservedDecimal;

    @Schema(description = "进位规则")
    private String carryRule;

    @Schema(description = "指标公式")
    private SecondDistributionFormula otherFormula = new SecondDistributionFormula();
}
