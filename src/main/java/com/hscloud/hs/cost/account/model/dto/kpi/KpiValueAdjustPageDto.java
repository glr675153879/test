package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Classname KpiCoefficientDto
 * @Description TODO
 * @Date 2024/11/27 13:41
 * @Created by sch
 */
@Data
public class KpiValueAdjustPageDto {
    private String carryRule;
    private String remark;
    private Long id;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "code")
    private String code;

    @Schema(description = "code_name")
    private String codeName;

    @Schema(description = "符号")
    private String operation;

    @Schema(description = "修改系数")
    private String value;

    @Schema(description = "用户Id")
    private Long userId;

    @Schema(description = "科室id")
    private Long accountUnit;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "科室名称")
    private String accountUnitName;

    private String caliber;

    private String retainDecimal;
}
