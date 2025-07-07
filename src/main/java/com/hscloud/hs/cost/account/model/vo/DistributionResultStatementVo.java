package com.hscloud.hs.cost.account.model.vo;

import com.hscloud.hs.cost.account.model.pojo.MonthRatio;
import com.hscloud.hs.cost.account.model.pojo.YearRatio;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author 小小w
 * @date 2023/11/30 16:10
 */

@Data
@Schema(description = "结果报表Vo")
public class DistributionResultStatementVo {

    @Schema(description = "核算周期")
    private String detailDim;

    @Schema(description = "核算单元id")
    private Long unitId;

    @Schema(description = "核算单元名称")
    private String unitName;

    @Schema(description = "总核算值")
    private BigDecimal totalCount=BigDecimal.ZERO;

    @Schema(description = "警戒值")
    private BigDecimal warnValue;

    @Schema(description = "警戒状态 0正常 1超出 2低于")
    private String warnStatus = "0";

    @Schema(description = "目标值")
    private String targetValue;

    @Schema(description = "同比")
    private YearRatio yearRatio;

    @Schema(description = "环比")
    private MonthRatio monthRatio;

}
