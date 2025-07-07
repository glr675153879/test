package com.hscloud.hs.cost.account.model.dto.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 异常月份查看参数
 * @author  lian
 * @date  2023-09-19 16:08
 * 
 */
@Data
@Schema(description = "异常月份查看参数")
public class CostMonitorAbMonthQueryDto {

    @Schema(description = "开始月份")
    private String startMonth;

    @Schema(description = "结束月份")
    private String endMonth;
}
