package com.hscloud.hs.cost.account.model.dto.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 年度异常月份查询参数 trendList用到
 * @author  lian
 * @date  2023-09-19 16:08
 * 
 */
@Data
@Schema(description = "年度异常月份查询参数 trendList用到")
public class CostMonitorAbnormalMonthQueryDto {

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份 生成异常月份记录入库用")
    private String month;

    @Schema(description = "核算项id")
    private String itemId;

    @Schema(description = "科室单元id")
    private String unitId;

}
