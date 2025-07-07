package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "二次分配审核列表查询dto")
public class SecondTaskApprovingRecordQueryDto extends PageDto {

    @Schema(description = "任务名称")
    private String taskName;


    @Schema(description = "任务周期")
    private String taskPeriod;

    @Schema(description = "状态：进行中：UNDERWAY、已完成：FINISHED")
    private String status;
}
