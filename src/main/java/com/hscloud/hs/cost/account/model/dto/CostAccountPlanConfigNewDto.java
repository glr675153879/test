package com.hscloud.hs.cost.account.model.dto;

import com.hscloud.hs.cost.account.model.pojo.CalculationIndex;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Admin
 */
@Data
@Schema(description = "核算方案配置的核算指标")
public class CostAccountPlanConfigNewDto {


    @Schema(description = "核算方案配置表的主键id")
    private Long id;

    @Schema(description = "核算方案id")
    @NotNull(message = "核算方案id不能为空")
    private Long planId;

    @Schema(description = "核算指标id")
    private Long indexId;

    @Schema(description = "是否是关联指标  0：否  1：是")
    private String isRelevance;

    @Schema(description = "关联指标核算范围")
    private String relevanceAccountRange;

    @Schema(description = "核算对象")
    private String accountRange;

    @Schema(description = "自定义科室单元id")
    private List<Long> customUnitIdList;

    @Schema(description = "关联指标自定义科室单元id")
    private List<distributionCustomUnitId> distributionCustomUnitIdList;

    @Schema(description = "排序")
    private Integer req;

    @Schema(description = "方案对应的核算指标配置项key")
    private String configKey;

    @Schema(description = "核算指标配置")
    @NotNull(message = "核算指标配置不能为空")
    private CalculationIndex calculationIndex;

    /**
     * 公式描述
     */
    @Schema(description = "公式描述")
    private String configDesc;

    @Data
    @Schema(description = "关联分组参数")
    public static class distributionCustomUnitId {
        @Schema(description = "id")
        private Long id;

        @Schema(description = "名称")
        private String name;

    }
}
