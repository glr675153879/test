package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "报表趋势详情查询dto")
public class AccountStatementDetailQueryDto {

    @Schema(description = "核算业务id")
    private Long taskId;

    @Schema(description = "分组id")
    private String groupId;

    @Schema(description = "分组id")
    private Long unitId;

    @Schema(description = "指标报表id")
    private Long indexResultId;

    @Schema(description = "指标id")
    private Long indexId;

    @Schema(description = "父指标id")
    private Long parentId;

    @Schema(description = "开始统计维度")
    private String detailDimStart;

    @Schema(description = "结束统计维度")
    private String detailDimEnd;

    @Schema(description = "核算维度")
    private String dimension;

}
