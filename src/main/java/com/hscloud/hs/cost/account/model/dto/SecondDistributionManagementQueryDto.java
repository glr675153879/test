package com.hscloud.hs.cost.account.model.dto;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "任务管理列表查询dto")
public class SecondDistributionManagementQueryDto extends PageDto {

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "科室单元id")
    private Long unitId;

    @Schema(description = "状态：进行中：UNDERWAY、已完成：FINISHED")
    private String status;
}
