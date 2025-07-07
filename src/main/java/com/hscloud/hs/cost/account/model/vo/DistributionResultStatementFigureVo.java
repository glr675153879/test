package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 小小w
 * @date 2023/12/2 17:35
 */
@Data
@Schema(description = "绩效饼状图vo")
public class DistributionResultStatementFigureVo {

    @Schema(description = "名称")
    private String name;

    @Schema(description = "值")
    private BigDecimal value;
}
