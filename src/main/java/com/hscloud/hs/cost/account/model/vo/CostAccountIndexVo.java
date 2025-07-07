package com.hscloud.hs.cost.account.model.vo;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.hscloud.hs.cost.account.model.entity.CostIndexConfigIndex;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "核算指标Vo")
public class  CostAccountIndexVo implements Serializable {
    @Schema(description = "核算指标id")
    private Long id;

    @Schema(description = "核算指标名称")
    private String name;

    @Schema(description = "指标单位")
    private String indexUnit;

    @Schema(description = "指标性质")
    private String indexProperty;

    @Schema(description = "统计周期")
    private String statisticalCycle;

    @Schema(description = "指标分组id")
    private Long indexGroupId;

    @Schema(description = "状态：0：启用  1:停用")
    private String status;

    @Schema(description = "进位规则id")
    private String carryRule;

    @Schema(description = "指标保留小数")
    private Integer reservedDecimal;

    @Schema(description = "指标公式")
    private String indexFormula;

    @Schema(description = "是否是系统指标  0 否  1 是")
    private String isSystemIndex;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "核算项是核算指标的")
    private List<CostIndexConfigIndex> costIndexConfigIndexList;

    @Schema(description = "核算项是核算项的")
    private List<CostIndexConfigItemVo> costIndexConfigItemList;
}
