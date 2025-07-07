package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "方案成本公式查询对象")
public class CostAccountPlanCostQueryDto {

    @Schema(description = "方案总成本id")
    private 	  Long  id;

    @Schema(description = "所属方案id")
    private 	  Long  planId;

    @Schema(description = "总成本公式")
    private 	  FormulaDto  formulaDto;



   /* @Schema(description = "方案总成本")
    private 	  String  name;*/

    @Schema(description = "核算范围")
    private 	  String  accountProportionObject;

    @Schema(description = "保留小数")
    private 	  Long  reservedDecimal;

    @Schema(description = "进位规则")
    private 	  String  carryRule;

}
