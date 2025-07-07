package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Classname KpiUserFactorAddDto
 * @Description TODO
 * @Date 2025/4/16 18:31
 * @Created by sch
 */
@Data
public class KpiUserFactorAddDto {


//    @Schema(description = "user,office,coefficient,subsidy 人员、职务这类字典、系数、补贴")
//    private String type;

    @Schema(description = "对应系统字段的dicType")
    private String dictType;

//    @Schema(description = "对应系统字段的二级字典code ")
//    private String itemCode;

    @Schema(description = "系数value ")
    private BigDecimal factorValue;

    @Schema(description = "补贴value ")
    private BigDecimal subsidyValue;

    @Schema(description = "目前不使用 ")
    private Long deptId;

    @Schema(description = "对应系统字段的二级字典code 字典类型使用")
    private List<String> itemCodes;
}
