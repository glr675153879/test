package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Admin
 */
@Data
@Schema(description = "核算任务计算过程Dto")
public class CostAccountTaskCalculateProcessDto {

    @Schema(description = "核算任务id")
    @NotNull(message = "核算任务id不能为空")
    private Long taskId;

    @Schema(description = "核算单元id")
    @NotNull(message = "核算单元id不能为空")
    private Long accountUnitId;

    @Schema(description = "类型 plan-核算方案 index-核算指标 item-核算项")
    @NotBlank(message = "类型不能为空")
    private String type;

    @Schema(description = "业务id,根据类型不同，对应的id不同")
    @NotNull(message = "业务id不能为空")
    private Long bizId;

    @Schema(description = "指标父级id")
    private Long parentId;

    @Schema(description = "核算项路径，是该核算项所有的父级指标key的路径，用,分割拼接")
    private String path;

    @Schema(description = "当前核算项的key")
    private String configKey;
}
