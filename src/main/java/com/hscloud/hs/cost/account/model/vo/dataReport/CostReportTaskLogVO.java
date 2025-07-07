package com.hscloud.hs.cost.account.model.vo.dataReport;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Schema(description = "上报任务变更日志")
public class CostReportTaskLogVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "应用名称")
    private String name;

    @Schema(description = "操作类型")
    private String opsType;

    @Schema(description = "操作项")
    private String opsItem;

    @Schema(description = "操作人")
    private String opsBy;

    @Schema(description = "操作人id")
    private Long opsById;

    @Schema(description = "操作时间")
    private LocalDateTime opsTime;

    @Schema(description = "工号")
    private Long jobNumber;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "删除标记,1:已删除,0:正常")
    private String delFlag;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "修改人")
    private String updateBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;

    @Schema(description = "租户id")
    private Long tenantId;
}

