package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author 小小w
 * @date 2023/11/20 9:40
 */
@Data
@Schema(description = "一次分配任务分组")
public class DistributionTaskGroupQueryDto extends PageDto {

    @Schema(description = "分组名称")
    private String name;

    @Schema(description = "任务类型")
    private String type;

    @Schema(description = "状态")
    private String status;
}
