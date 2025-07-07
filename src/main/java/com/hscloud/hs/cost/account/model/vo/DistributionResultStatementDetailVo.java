package com.hscloud.hs.cost.account.model.vo;

import com.hscloud.hs.cost.account.model.pojo.AdsIncomePerformancePay;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 小小w
 * @date 2023/12/4 9:03
 */

@Data
@Schema(description = "结果报表详情Vo")
public class DistributionResultStatementDetailVo {

    @Schema(description = "核算总值")
    private BigDecimal totalValue=BigDecimal.ZERO;

    @Schema(description = "科室绩效总值")
    private BigDecimal unitTotalValue=BigDecimal.ZERO;

    @Schema(description = "管理绩效总值")
    private BigDecimal manageTotalValue=BigDecimal.ZERO;

    @Schema(description = "明细")
    private List<detail> detailList;

    @Data
    @Schema(description = "明细")
    public static class detail {

        @Schema(description = "科室")
        private String name;

        @Schema(description = "医生组")
        private BigDecimal amountDoc=BigDecimal.ZERO;

        @Schema(description = "护理组")
        private BigDecimal amountNur=BigDecimal.ZERO;

        @Schema(description = "医技组")
        private BigDecimal amountDocTec=BigDecimal.ZERO;

        @Schema(description = "药剂组")
        private BigDecimal amountMed=BigDecimal.ZERO;

        @Schema(description = "行政组")
        private BigDecimal amountAdm=BigDecimal.ZERO;

        @Schema(description = "科主任")
        private BigDecimal amountDocHead=BigDecimal.ZERO;

        @Schema(description = "护士长")
        private BigDecimal amountNurHead=BigDecimal.ZERO;

        @Schema(description = "合计")
        private BigDecimal total=BigDecimal.ZERO;
    }
}
