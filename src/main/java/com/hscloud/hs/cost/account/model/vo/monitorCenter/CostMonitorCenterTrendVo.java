package com.hscloud.hs.cost.account.model.vo.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 监测值趋势vo
 * @author  lian
 * @date  2023-09-20 11:12
 *
 */
@Data
@Schema(description = "趋势vo")
public class CostMonitorCenterTrendVo {
    @Schema(description = "监测日期")
    private String monitorDate;

    @Schema(description = "值")
    private BigDecimal monitorValue;

    @Schema(description = "值统计")
    private BigDecimal monitorValueCount;

    @Schema(description = "状态 0正常 1异常")
    private String status;
}
