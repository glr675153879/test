package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分配管理平均绩效")
public class SecondDistributionTaskAverageDto {

    @Schema(description = "关联任务id")
    private Long taskId;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "工号")
    private Long jobNumber;

    @Schema(description = "平均绩效金额")
    private String amount;

    @Schema(description = "计算单位")
    private String unit;

    @Schema(description = "租户id")
    private Long tenantId;

}
