package com.hscloud.hs.cost.account.model.dto.monitorCenter;

import com.hscloud.hs.cost.account.model.vo.monitorCenter.CostMonitorAbnormalItemVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 异常科室单元查看参数
 * @author  lian
 * @date  2023-09-19 16:08
 * 
 */
@Data
@Schema(description = "异常科室单元查看参数")
public class CostMonitorAbnormalUnitQueryDto {

    @Schema(description = "月份")
    private String month;

    @Schema(description = "科室单元名称")
    private String unitName;

    @Schema(description = "异常核算项数量")
    private Integer itemNum;

    @Schema(description = "异常核算项List")
    List<CostMonitorAbnormalItemVo> resItemList;
}
