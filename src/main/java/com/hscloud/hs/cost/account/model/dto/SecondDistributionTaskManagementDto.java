package com.hscloud.hs.cost.account.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "任务管理dto")
public class SecondDistributionTaskManagementDto {

    @Schema(description = "关联任务id")
    private Long taskId;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "工号")
    private Long jobNumber;

    @Schema(description = "管理岗位")
    private String position;

    @Schema(description = "管理绩效金额")
    private String amount;

    @Schema(description = "计算单位")
    private String unit;

    @Schema(description = "类型：一次分配、二次分配")
    private String type;

}
