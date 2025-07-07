package com.hscloud.hs.cost.account.model.vo.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 异常信息统计
 * @author  lian
 * @date  2023-09-18 11:04
 * 
 */
@Data
@Schema(description = "异常信息统计")
public class CostMonitorAbnormalCountVo {
    @Schema(description = "异常核算数")
    private Integer abnormalItem = 0;

    @Schema(description = "异常科室单元数")
    private Integer abnormalUnit = 0;
}
