package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 小小w
 * @date 2023/12/5 16:39
 */
@Data
@Schema(description = "医院奖惩明细结果Vo")
public class AdsHospitalRewardsPunishmentsDetailsVo {

    @Schema(description = "时间")
    private Integer dt;

    @Schema(description = "核算单元id")
    private Long accountUnitId;

    @Schema(description = "核算单元医生组")
    private String accountUnitDoc;

    @Schema(description = "总核算值")
    private BigDecimal deptTotal=BigDecimal.ZERO;

    @Schema(description = "支援")
    private BigDecimal support=BigDecimal.ZERO;

    @Schema(description = "规培/进修/下乡/借调/120/援疆")
    private BigDecimal training=BigDecimal.ZERO;

    @Schema(description = "电子病例")
    private BigDecimal elecMedicalRecord=BigDecimal.ZERO;

    @Schema(description = "体检转病人")
    private BigDecimal examTransferPatient=BigDecimal.ZERO;

    @Schema(description = "制剂奖励")
    private BigDecimal zjReward=BigDecimal.ZERO;

    @Schema(description = "其他1")
    private BigDecimal other1=BigDecimal.ZERO;

    @Schema(description = "其他")
    private BigDecimal other=BigDecimal.ZERO;

    @Schema(description = "医院单项奖罚")
    private BigDecimal rewardPunish=BigDecimal.ZERO;

    @Schema(description = "合计")
    private BigDecimal total=BigDecimal.ZERO;
}
