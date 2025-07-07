package com.hscloud.hs.cost.account.model.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.math.BigDecimal;

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
@TableName("nfjx_ads.ads_hospital_rewards_punishments_details")
@Schema(description="医院单项奖惩绩效")
public class AdsHospitalRewardsPunishmentsDetails extends Model<AdsHospitalRewardsPunishmentsDetails> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "核算周期")
    private String accountPeriod;

    @Schema(description = "核算单元id")
    private Long accountUnitId;

    @Schema(description = "核算单元医生组")
    private String accountUnitDoc;

    @Schema(description = "科室合计")
    private BigDecimal deptTotal;

    @Schema(description = "管理层")
    private BigDecimal managerTotal;

    @Schema(description = "(发热门诊)支援科主任")
    private BigDecimal supportDirector;

    @Schema(description = "(发热门诊)支援医生")
    private BigDecimal supportDoc;

    @Schema(description = "(发热门诊)支援护士长")
    private BigDecimal supportNurHead;

    @Schema(description = "(发热门诊)支援护士")
    private BigDecimal supportNur;

    @Schema(description = "(发热门诊)支援科室人员")
    private BigDecimal supportEmp;

    @Schema(description = "(规培/进修/下乡/借调/120/援疆)其他科主任")
    private BigDecimal otherDirector;

    @Schema(description = "(规培/进修/下乡/借调/120/援疆)其他医生")
    private BigDecimal otherDoc;

    @Schema(description = "(规培/进修/下乡/借调/120/援疆)其他护士长")
    private BigDecimal otherNurHead;

    @Schema(description = "(规培/进修/下乡/借调/120/援疆)其他护士")
    private BigDecimal otherNur;

    @Schema(description = "(规培/进修/下乡/借调/120/援疆)其他科室人员")
    private BigDecimal otherEmp;

    @Schema(description = "电子病例")
    private BigDecimal elecMedicalRecord;

    @Schema(description = "体检转病人")
    private BigDecimal examTransferPatient;

    @Schema(description = "制剂奖励")
    private BigDecimal zjReward;

    @Schema(description = "其他1")
    @TableField("other_1")
    private BigDecimal other1;

    @Schema(description = "其他")
    private BigDecimal other;

    @Schema(description = "医院单项奖罚")
    private BigDecimal rewardPunish;

    @Schema(description = "合计")
    private BigDecimal total;


}
