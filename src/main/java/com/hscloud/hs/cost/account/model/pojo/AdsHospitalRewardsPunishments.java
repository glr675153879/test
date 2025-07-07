package com.hscloud.hs.cost.account.model.pojo;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author author
 * @since 2023-12-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("nfjx_ads.ads_hospital_rewards_punishments")
@Schema(description="医院单项奖惩")
public class AdsHospitalRewardsPunishments extends Model<AdsHospitalRewardsPunishments> {

    private static final long serialVersionUID = 1L;

   @Schema(description = "时间id")
    private String accountPeriod;

   @Schema(description = "核算单元id")
    private Long accountUnitId;

   @Schema(description = "核算单元名称")
    private String accountUnitName;

   @Schema(description = "制剂就诊")
    private BigDecimal preparationVisit;

   @Schema(description = "专利归属")
    private BigDecimal patentOwnership;

   @Schema(description = "西药占比")
    private BigDecimal westernProportion;

   @Schema(description = "临方加工")
    private String tempProcessing;

   @Schema(description = "精细化绩效")
    private BigDecimal refinePerformance;

   @Schema(description = "信访")
    private BigDecimal letterVisits;

   @Schema(description = "医保扣款")
    private BigDecimal medicalIssurance;

    @Schema(description = "膏方奖励")
    private BigDecimal amountHerbal;

   @Schema(description = "医疗纠纷扣罚")
    private BigDecimal medicalDispute;

   @Schema(description = "合计")
    private BigDecimal total;


}
