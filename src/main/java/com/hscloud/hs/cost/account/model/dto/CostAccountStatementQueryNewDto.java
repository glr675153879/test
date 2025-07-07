package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 小小w
 * @date 2023/11/30 9:36
 */
@Data
@Schema(description = "报表查询dto(新一次分配)")
public class CostAccountStatementQueryNewDto extends PageDto {

    @Schema(description = "核算任务id")
    private Long taskId;

    @Schema(description = "核算业务开始周期")
    private String startTime;

    @Schema(description = "核算业务结束周期")
    private String endTime;

    @Schema(description = "同比 RISE上升 FALL下降 不限 空")
    private String yearOverYear;

    @Schema(description = "环比 RISE上升 FALL下降 不限 空")
    private String monthOverMonth;

    @Schema(description = "核算分组")
    private String accountGroupCode;

    @Schema(description = "开始总核算值")
    private BigDecimal totalCountStart;

    @Schema(description = "结束总核算值")
    private BigDecimal totalCountEnd;

    @Schema(description = "核算单元id")
    private Long unitId;

    @Schema(description = "核算单元名称")
    private String unitName;

    @Schema(description = "核算单元id")
    private List<Long> otherUnitIds;

}
