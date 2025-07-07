package com.hscloud.hs.cost.account.model.vo.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class KpiItemEquivalentConfigVO {
    private Long id;

    @Schema(description = "核算项id")
    private Long itemId;

    @Schema(description = "核算项code")
    private String itemCode;

    @Schema(description = "核算项名称")
    private String itemName;

    @Schema(description = "项目分类")
    private String proCategoryName;

    @Schema(description = "科室id")
    private Long accountUnitId;

    @Schema(description = "科室名称")
    private String accountUnitName;

    @Schema(description = "标化当量")
    private BigDecimal stdEquivalent;

    @Schema(description = "是否继承 0-否 1-是")
    private String inheritFlag;

    @Schema(description = "排序号")
    private Integer seq;

    @Schema(description = "核算项状态 0启用，1停用")
    private String itemStatus;

    @Schema(description = "核算项删除标记 0-未删除 1-已删除")
    private String itemDelFlag;
}
