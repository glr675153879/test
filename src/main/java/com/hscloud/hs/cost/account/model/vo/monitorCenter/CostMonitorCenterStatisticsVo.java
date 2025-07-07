package com.hscloud.hs.cost.account.model.vo.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
/**
 * 监测统计vo
 * @author  lian
 * @date  2023-09-18 11:31
 *
 */
@Data
@Schema(description = "监测统计vo")
public class CostMonitorCenterStatisticsVo {
    @Schema(description = "收入异常数")
    private Integer incomeAbnormal;

    @Schema(description = "成本异常数")
    private Integer costAbnormal;

    @Schema(description = "收入监测总项")
    private Integer incomeCount;

    @Schema(description = "成本监测总项")
    private Integer costCount;
}
