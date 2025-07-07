package com.hscloud.hs.cost.account.model.dto.second;

import com.hscloud.hs.oa.workflow.api.dto.ProcessInstanceCreateDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 小小w
 * @date 2024/3/9 12:06
 */
@Data
@Schema(description = "任务提交传输对象")
public class TaskSubmitDto extends ProcessInstanceCreateDto {

    @Schema(description = "任务id")
    private Long taskId;

}
