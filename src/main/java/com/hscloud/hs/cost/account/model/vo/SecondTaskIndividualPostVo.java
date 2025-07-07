package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "二次分配任务结果个人职称绩效vo")
public class SecondTaskIndividualPostVo {
    @Schema(description = "个人职称绩效总金额")
    private BigDecimal totalAmount;

    @Schema(description = "详细数据")
    private List<SecondIndividualPost> individualPostList;

    @Schema(description = "公式")
    private String expression;

    @Data
    @Schema(description = "详细数据Vo")
    public static class SecondIndividualPost {
        @Schema(description = "个人职称指标名称")
        private String name;

        @Schema(description = "指标的key")
        private String key;

        @Schema(description = "单项金额")
        private BigDecimal amount;
    }
}
