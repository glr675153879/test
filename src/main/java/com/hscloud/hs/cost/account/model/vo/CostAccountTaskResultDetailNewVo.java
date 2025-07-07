package com.hscloud.hs.cost.account.model.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hscloud.hs.cost.account.model.pojo.AdsIncomePerformanceScoreNur;
import com.hscloud.hs.cost.account.model.pojo.UnitAccountInfo;
import com.hscloud.hs.cost.account.model.pojo.WholeAccountInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author 小小w
 * @date 2023/11/29 14:25
 */
@Data
@Schema(description = "核算任务结果Vo(新)")
public class CostAccountTaskResultDetailNewVo {
    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "核算开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime accountStartTime;

    @Schema(description = "核算结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime accountEndTime;

    @Schema(description = "总核算值")
    private BigDecimal totalCost;

    @Schema(description = "核算单元核算信息")
    private Object unitAccountInfoList;

    @Schema(description = "总的核算信息")
    private WholeAccountInfo wholeAccountInfo;

}
