package com.hscloud.hs.cost.account.model.dto.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 异常信息统计查询参数
 * @author  lian
 * @date  2023-09-19 16:08
 * 
 */
@Data
@Schema(description = "异常信息统计查询参数")
public class CostMonitorAbnormalCountQueryDto {

    @Schema(description = "月份")
    private String month;

}
