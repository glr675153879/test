package com.hscloud.hs.cost.account.model.vo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.hscloud.hs.cost.account.constant.enums.StatisticalPeriodEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class IndexPreviewVo {


    @Schema(description = "核算指标id")
    private Long id;

    @Schema(description = "核算指标名称")
    private String name;

    @Schema(description = "指标单位")
    private String indexUnit;

    @Schema(description = "指标公式")
    private String indexFormula;

    @Schema(description = "进位规则id")
    private String carryRule;

    @Schema(description = "指标保留小数")
    private Integer reservedDecimal;
}
