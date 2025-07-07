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
//列表展示
public class KpiUserFactorTableDto {

    @Schema(description = "对应系统字段的dicType")
    private String dictType;

    @Schema(description = "系数value ")
    private ValueOrSystem factorValue;

    @Schema(description = "补贴value ")
    private ValueOrSystem subsidyValue;

    @Schema(description = " \"对应系统字段的二级字典code ")
    private List<KpiDicItemDto> itemCode;

}
