package com.hscloud.hs.cost.account.model.vo.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 异常月份查看Vo
 *
 * @author lian
 * @date 2023-09-21 6:53
 */
@Data
@Schema(description = "异常月份vo")
public class CostMonitorAbnormalMonVo {

    @Schema(description = "核算单元id")
    private String unitId;

    @Schema(description = "核算项id")
    private String itemId;

    @Schema(description = "月份")
    private String month;

    @Schema(description = "当月累计监测总值")
    private BigDecimal monitorValueMonth;

    @Schema(description = "警戒值")
    private BigDecimal warnValue;

    @Schema(description = "目标值")
    private String targetValue;

    @Schema(description = "单位")
    private String measureUnit;

    @Schema(description = "警戒状态 0正常 1超出 2低于")
    private String status = "0";

    @Schema(description = "同比")
    private String sequentialGrowth;

    @Schema(description = "环比")
    private String yearOnYearGrowth;

    @Schema(description = "异常月份数")
    private Integer abnormalMonthCount;

}
