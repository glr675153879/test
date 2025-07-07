package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "二次分配任务结果平均绩效vo")
public class SecondTaskAverageVo {
    @Schema(description = "平均绩效金额")
    private BigDecimal totalAmount;

    @Schema(description = "详细数据")
    private List<SecondAverage> individualPostList;

    @Schema(description = "公式")
    private String expression;

    @Data
    @Schema(description = "详细数据Vo")
    public static class SecondAverage {
        @Schema(description = "平均绩效指标名称")
        private String name;

        @Schema(description = "指标的key")
        private String key;

        @Schema(description = "平均绩效金额")
        private BigDecimal amount;
    }
}
