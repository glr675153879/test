package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "二次分配任务结果单项绩效vo")
public class SecondTaskSingleVo {
    @Schema(description = "单项绩效金额")
    private BigDecimal totalAmount;

    @Schema(description = "详细数据")
    private List<SecondSingle> singleList;


    @Data
    @Schema(description = "详细数据Vo")
    @Getter
    public static class SecondSingle {
        @Schema(description = "单项名称")
        private String name;

        @Schema(description = "单项金额")
        private BigDecimal amount;
    }
}
