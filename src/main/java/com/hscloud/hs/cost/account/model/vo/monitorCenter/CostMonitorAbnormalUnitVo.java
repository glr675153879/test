package com.hscloud.hs.cost.account.model.vo.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 异常科室单元vo
 * @author  lian
 * @date  2023-09-18 11:04
 * 
 */
@Data
@Schema(description = "异常科室单元vo")
public class CostMonitorAbnormalUnitVo {

    @Schema(description = "核算单元id")
    private String unitId;

    @Schema(description = "核算单元名称")
    private String unitName;

    @Schema(description = "异常核算项数")
    private Integer abnormalCount;

    @Schema(description = "最新预警时间")
    private String warnTime;
}
