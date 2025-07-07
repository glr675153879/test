package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "核算结果查询对象")
public class TaskResultQueryDto extends PageDto {

    @Schema(description = "核算任务id")
    @NotNull
    private Long taskId;

    @Schema(description = "核算单元id")
    private String accountUnitId;

    @Schema(description = "核算分组id")
    private String accountGroupId;

    @Schema(description = "核算分组id")
    private String taskGroupId;
}
