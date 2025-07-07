package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Classname KpiUserFactorDeptDto
 * @Description TODO
 * @Date 2025/5/7 08:33
 * @Created by sch
 */
@Data
public class KpiUserFactorDeptDto {

    private String name;

    private Long userId;

    @Schema(description = "科室名称")
    private String unitName;

    private Long deptId;

    private BigDecimal factor;

    private String isSystem = "0";
}
