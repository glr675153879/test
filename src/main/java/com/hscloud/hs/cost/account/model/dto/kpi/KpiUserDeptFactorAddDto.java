package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Classname KpiUserFactorAddListDto
 * @Description TODO
 * @Date 2025/4/23 16:51
 * @Created by sch
 */
@Data
public class KpiUserDeptFactorAddDto {

    private Long userId;

    @Schema(description = "系数value ")
    private BigDecimal value;

    @Schema(description = " ")
    private Long deptId;
}
