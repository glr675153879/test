package com.hscloud.hs.cost.account.model.dto;

import com.hscloud.hs.oa.workflow.api.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "二次分配审核传输对象")
@EqualsAndHashCode(callSuper = true)
public class SecondDistributionTaskApproveQueryDto extends PageDto {

    @Schema(description = "任务id")
    private Long taskId;

    @Schema(description = "部门名称")
    private String unitName;
}
