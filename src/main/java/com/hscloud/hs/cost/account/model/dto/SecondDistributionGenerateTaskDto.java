package com.hscloud.hs.cost.account.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "根据一次分配生成二次分配任务接口")
public class SecondDistributionGenerateTaskDto {

    @Schema(description = "任务周期")
    private String taskPeriod;
}
