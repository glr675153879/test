package com.hscloud.hs.cost.account.model.vo.monitorCenter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 校验监测值是否在对应目标值范围内vo
 * @author  lian
 * @date  2023-09-18 11:04
 * 
 */
@Data
@Schema(description = "监测动态监测值测试数据vo")
public class CostMonitorInRangeVo {


    @Schema(description = "警戒值")
    private BigDecimal warnValue;

    @Schema(description = "警戒状态 0正常 1超出 2低于")
    private String warnStatus = "0";


}
