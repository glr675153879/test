package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "返回异动列表对象")
public class CostDataChangeRecordVo {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "业务id")
    private Long bizId;

    @Schema(description = "业务名称")
    private String bizName;

    @Schema(description = "变更项")
    private String changeItem;

    @Schema(description = "变更时间")
    private LocalDateTime changeTime;

    @Schema(description = "变更类型")
    private String changeType;

    @Schema(description = "变更描述")
    private String changeDesc;

    @Schema(description = "创建人")
    private String createBy;

}
