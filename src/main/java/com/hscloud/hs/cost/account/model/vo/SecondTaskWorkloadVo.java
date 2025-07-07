package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "二次分配任务结果工作量绩效vo")
public class SecondTaskWorkloadVo {

    @Schema(description = "工作量绩效金额")
    private BigDecimal totalAmount;

    @Schema(description = "详细数据")
    private List<SecondWorkload> workloadList;


    @Data
    @Schema(description = "详细数据Vo")
    public static class SecondWorkload {
        @Schema(description = "单项名称")
        private String name;

        @Schema(description = "单项金额")
        private BigDecimal amount;
    }
}
