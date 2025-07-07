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
 * 对应绩效套标《10-核算业绩分》- 护理组
 * </p>
 *
 * @author author
 * @since 2023-11-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("nfjx_ads.ads_income_performance_score_nur")
@Schema(description="对应绩效套标《10-核算业绩分》- 护理组")
public class AdsIncomePerformanceScoreNur extends Model<AdsIncomePerformanceScoreNur> {

    private static final long serialVersionUID = 1L;

    @Schema(description= "绩效考核周期")
    private String accountPeriod;

    @Schema(description= "核算单元ID")
    private Long accountUnitId;

    @Schema(description= "核算单元名称")
    private String accountUnitName;

    @Schema(description= "核算单元分组")
    private String accountUnitGroup;

    @Schema(description= "门诊收入")
    private BigDecimal outpatient;

    @Schema(description= "住院收入")
    private BigDecimal inpatient;

    @Schema(description= "医保结余")
    private BigDecimal insurance;

    @Schema(description= "医保考核收入")
    private BigDecimal insuranceIncome;

    @Schema(description= "日间病房")
    private BigDecimal dayCare;

    @Schema(description= "转ICU治疗类收入")
    private BigDecimal turnIcu;

    @Schema(description= "急诊转送病人")
    private BigDecimal turnEmergency;

    @Schema(description= "门诊护士")
    private BigDecimal clinicnurse;

    @Schema(description= "补上月数据")
    private BigDecimal lastmonth;

    @Schema(description= "转入")
    private BigDecimal turnIn;

    @Schema(description= "转出")
    private BigDecimal turnOut;

    @Schema(description= "收入合计")
    private BigDecimal totalIncome;

    @Schema(description= "成本合计")
    private BigDecimal totalCost;

    @Schema(description= "总核算值")
    private BigDecimal performanceScore;


}
