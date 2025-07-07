package com.hscloud.hs.cost.account.model.vo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.hscloud.hs.cost.account.model.pojo.MonthRatio;
import com.hscloud.hs.cost.account.model.pojo.YearRatio;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.ehcache.shadow.org.terracotta.offheapstore.paging.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "结果报表Vo")
public class CostResultStatementVo {

    @Schema(description = "核算任务id")
    private Long id;

    @Schema(description = "核算任务名称")
    private String accountTaskName;

    @Schema(description = "核算单元id")
    private Long unitId;

    @Schema(description = "核算单元名称")
    private String unitName;

    @Schema(description = "核算分组")
    private String groupId;

    @Schema(description = "指标报表id")
    private Long indexResultId;

    @Schema(description = "核算业务开始周期")
    private LocalDateTime accountStartTime;

    @Schema(description = "核算业务结束周期")
    private LocalDateTime accountEndTime;

    @Schema(description = "统计维度")
    private String dimension;

    @Schema(description = "总核算值")
    private String detailDim;

    @Schema(description = "总核算值")
    private BigDecimal totalCount=BigDecimal.ZERO;

    @Schema(description = "同比")
    private YearRatio yearRatio;

    @Schema(description = "环比")
    private MonthRatio monthRatio;

    @Schema(description = "计量单位")
    private String measureUnit;

    @Schema(description = "核算指标id")
    private Long indexId;

    @Schema(description = "核算指标名")
    private String indexName;

    @Schema(description = "父级核算指标id")
    private Long parentId;

    @Schema(description = "路径")
    private String path;

}
