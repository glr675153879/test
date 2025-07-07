package com.hscloud.hs.cost.account.model.vo.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 监测值趋势详情vo
 *
 * @author lian
 * @date 2023-09-21 6:53
 */
@Data
@Schema(description = "监测值趋势详情vo")
public class CostMonitorValTrendVo {

    @Schema(description = "核算单元id")
    private String unitId;

    @Schema(description = "核算单元名称")
    private String unitName;

    @Schema(description = "核算项id")
    private String itemId;

    @Schema(description = "核算项名称")
    private String itemName;

    @Schema(description = "监测日期")
    private String monitorDate;

    @Schema(description = "值")
    private BigDecimal monitorValue;

    @Schema(description = "当月累计监测总值")
    private BigDecimal monitorValueMonth;

    @Schema(description = "年度平均值")
    private BigDecimal yearAvg;

    @Schema(description = "目标值")
    private String targetValue;

    @Schema(description = "单位")
    private String measureUnit;

    @Schema(description = "警戒值")
    private BigDecimal warnValue;

    @Schema(description = "警戒状态 0正常 1超出 2低于")
    private String status = "0";

    @Schema(description = "同比")
    private String sequentialGrowth;

    @Schema(description = "环比")
    private String yearOnYearGrowth;

    public void returnCostMonitorValTrendVo(CostMonitorValTrendVo vo,CostMonitorValTrendVo paramVo) {
        vo.unitId = paramVo.unitId;
        vo.unitName = paramVo.unitName;
        vo.itemId = paramVo.itemId;
        vo.itemName = paramVo.itemName;
        vo.targetValue = paramVo.targetValue;
        vo.measureUnit = paramVo.measureUnit;
    }
}
