package com.hscloud.hs.cost.account.model.vo.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 监测趋势月份统计
 * @author  lian
 * @date  2023-09-18 11:04
 * 
 */
@Data
@Schema(description = "监测趋势月份统计")
public class CostMonitorCenterMonthVo {

    @Schema(description = "核算单元id")
    private String unitId;

    @Schema(description = "核算项id")
    private String itemId;

    @Schema(description = "月份")
    private String month;

    @Schema(description = "该月累计监测总值")
    private BigDecimal monitorValueMonth;

    @Schema(description = "状态 0正常 1异常")
    private String status;
}
