package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "二次分配任务结果管理绩效vo")
public class SecondTaskManagementVo {

    @Schema(description = "管理绩效金额")
    private BigDecimal amount;

    @Schema(description = "类型：一次分配，二次分配")
    private String type;
}
