package com.hscloud.hs.cost.account.model.vo.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 监测动态监测值测试数据vo
 * @author  lian
 * @date  2023-09-18 11:04
 * 
 */
@Data
@Schema(description = "监测动态监测值测试数据vo")
public class CostMonitorDataVo {


    @Schema(description = "核算单元id")
    private String unitId;


    @Schema(description = "核算项id")
    private String itemId;

    @Schema(description = "监测日期")
    private String monitorDate;

    @Schema(description = "监测值")
    private BigDecimal monitorValue;

    @Schema(description = "当月累计监测总值")
    private BigDecimal monitorValueMonth;

    @Schema(description = "最新预警时间")
    private String warnTime;

}
