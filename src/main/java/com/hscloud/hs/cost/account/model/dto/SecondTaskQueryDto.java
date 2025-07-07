package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "二次分配任务查询dto")
@EqualsAndHashCode(callSuper = true)
public class SecondTaskQueryDto  extends PageDto {

    @Schema(description = "任务id")
    private Long taskUnitInfoId;

}
