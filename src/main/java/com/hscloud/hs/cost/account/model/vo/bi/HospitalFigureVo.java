package com.hscloud.hs.cost.account.model.vo.bi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医院图 VO
 *
 * @author : [pc]
 * @date 2024/05/28
 */
@Data
@Schema(description = "饼图数据")
public class HospitalFigureVo {

    @Schema(description = "医生组")
    private Detail amountDoc = new Detail();

    @Schema(description = "护理组")
    private Detail amountNur = new Detail();

    @Schema(description = "医技组")
    private Detail amountDocTec = new Detail();

    @Schema(description = "药剂组")
    private Detail amountMed = new Detail();

    @Schema(description = "行政组")
    private Detail amountAdm = new Detail();

    @Data
    @Schema(description = "明细")
    public static class Detail {

        private BigDecimal value;

        private BigDecimal qoq;

    }

}
