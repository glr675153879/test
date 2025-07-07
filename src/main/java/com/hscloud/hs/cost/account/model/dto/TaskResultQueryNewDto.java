package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author 小小w
 * @date 2023/11/29 14:35
 */
@Data
@Schema(description = "核算结果查询对象(新)")
public class TaskResultQueryNewDto extends PageDto {

    @Schema(description = "核算任务名称")
    private String accountTaskName;

    @Schema(description = "核算时间")
    private String accountTime;

    @Schema(description = "核算单元")
    private String accountUnitName;

    @Schema(description = "核算任务id")
    private Long accountTaskId;

    @Schema(description = "核算任务分组id")
    private Long accountTaskGroupId;

    @Schema(description = "核算分组")
    private String accountGroupId;

    @Schema(description = "核算单元id")
    private Long accountUnitId;

}
