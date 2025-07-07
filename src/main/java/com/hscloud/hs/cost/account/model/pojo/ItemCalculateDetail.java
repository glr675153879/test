package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Admin
 */
@Data
@Schema(description = "核算项计算明细")
public class ItemCalculateDetail {

    @Schema(description = "业务相关 科室code 人员id")
    private String bizId;

    @Schema(description = "业务相关 科室名称 人员名称")
    private String bizName;

    @Schema(description = "计算值")
    private BigDecimal calculatedValue;
}
