package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Classname KpiDictItemOutDto
 * @Description TODO
 * @Date 2025/4/28 09:29
 * @Created by sch
 */
@Data
public class KpiDictItemOutDto {

    private Long id;

    private String dictType;

    private String description;

    private String performanceSubsidy;

    private String personnelFactor;

    private BigDecimal performanceSubsidyValue;

    private BigDecimal personnelFactorValue;

    private String itemCode;

    private String label;
}
