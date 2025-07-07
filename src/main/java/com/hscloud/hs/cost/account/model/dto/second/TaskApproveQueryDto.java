package com.hscloud.hs.cost.account.model.dto.second;

import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 小小w
 * @date 2024/3/9 13:16
 */
@Data
@Schema(description = "二次分配审核传输对象")
@EqualsAndHashCode(callSuper = true)
public class TaskApproveQueryDto extends PageDto {

    @Schema(description = "任务id")
    private Long taskId;

    @Schema(description = "部门名称")
    private String unitName;
}
