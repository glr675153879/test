package com.hscloud.hs.cost.account.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "报表查询dto")
public class CostAccountStatementQueryDto extends PageDto{


    private Long taskId;

    @Schema(description = "核算业务开始周期")
    private LocalDateTime startTime;

    @Schema(description = "核算业务结束周期")
    private LocalDateTime endTime;

    @Schema(description = "同比 RISE上升 FALL下降 不限 空")
    private String yearOverYear;

    @Schema(description = "环比 RISE上升 FALL下降 不限 空")
    private String monthOverMonth;

    @Schema(description = "维度")
    private String dimension;

    @Schema(description = "核算分组")
    private String accountGroupCode;

    @Schema(description = "开始总核算值")
    private BigDecimal totalCountStart;

    @Schema(description = "结束总核算值")
    private BigDecimal totalCountEnd;

    @Schema(description = "核算指标id")
    private Long indexId;

    @Schema(description = "指标报表id")
    private Long indexResultId;

    @Schema(description = "核算单元id")
    private Long unitId;

    @Schema(description = "核算单元名称")
    private String unitName;

    @Schema(description = "核算分组id")
    private String  groupId;

    @Schema(description = "核算分组名称")
    private String groupName;

}
