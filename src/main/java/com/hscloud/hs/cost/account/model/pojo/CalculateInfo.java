package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Admin
 */
@Data
@Schema(description = "核算信息")
public class CalculateInfo {

    @Schema(description = "总核算值")
    private BigDecimal totalCost;

    @Schema(description = "核算名称")
    private String name;

    @Schema(description = "核算id")
    private Long id;

    @Schema(description = "核算类型")
    private String type;

    @Schema(description = "配置指标的configKey")
    private String configKey;
    
    @Schema(description = "分摊成本")
    private SharedCost sharedCost;

    @Schema(description = "子核算计算信息")
    private List<CalculateInfo> children;

}
