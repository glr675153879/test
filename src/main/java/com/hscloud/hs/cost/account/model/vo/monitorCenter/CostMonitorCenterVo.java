package com.hscloud.hs.cost.account.model.vo.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 监测趋势vo
 * @author  lian
 * @date  2023-09-18 11:04
 * 
 */
@Data
@Schema(description = "监测趋势")
public class CostMonitorCenterVo {
    @Schema(description = "监测值设置id")
    private Long id;

    @Schema(description = "核算单元id")
    private String unitId;

    @Schema(description = "核算单元名称")
    private String unitName;

    @Schema(description = "核算项id")
    private String itemId;

    @Schema(description = "核算项名称")
    private String itemName;

    @Schema(description = "趋势List")
    private List<CostMonitorCenterTrendVo> trendList;

    @Schema(description = "当日监测值")
    private BigDecimal monitorValue;

    @Schema(description = "当月累计监测总值")
    private BigDecimal monitorValueMonth;

    @Schema(description = "警戒值")
    private BigDecimal warnValue;

    @Schema(description = "警戒状态 0正常 1超出 2低于")
    private String status = "0";

    @Schema(description = "目标值")
    private String targetValue;

    @Schema(description = "单位")
    private String measureUnit;

    @Schema(description = "年度异常月份个数")
    private Integer abnormalMonth;

    @Schema(description = "最新预警时间")
    private String warnTime;
}
