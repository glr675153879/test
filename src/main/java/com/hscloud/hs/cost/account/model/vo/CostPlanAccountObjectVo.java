package com.hscloud.hs.cost.account.model.vo;

import com.hscloud.hs.cost.account.model.pojo.CostFormulaInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Admin
 */
@Schema(description = "方案核算对象")
@Data
public class CostPlanAccountObjectVo {

    @Schema(description = "方案公式表的主键id")
    private Long id;


    @Schema(description = "核算对象id")
    private String accountObjectId;

    @Schema(description = "自定义科室单元id")
    private Long customUnitId;

    @Schema(description = "核算对象名称")
    private String accountObjectName;

    @Schema(description = "对象公式")
    private String formulaExpression;

    @Schema(description = "进位规则")
    private String carryRule;

    @Schema(description = "保留小数位数")
    private Integer reservedDecimal;

    @Schema(description = "核算对象公式")
    private List<CostFormulaInfo> formulaInfoList;




}
