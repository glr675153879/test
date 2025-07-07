package com.hscloud.hs.cost.account.model.dto.monitorCenter;

import com.hscloud.hs.cost.account.model.entity.CostMonitorAbMonth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 异常月份查看参数
 * @author  lian
 * @date  2023-09-19 16:08
 * 
 */
@Data
@Schema(description = "异常月份查看参数")
public class CostMonitorAbnormalMonTestCreateDto {

    @Schema(description = "核算项id")
    private List<CostMonitorAbMonth> costMonitorAbMonthList;
}
