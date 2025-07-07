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
 * 核算业绩分-医生组
 * </p>
 *
 * @author author
 * @since 2023-12-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("nfjx_ads.ads_income_performance_score_doc")
@Schema(description="核算业绩分-医生组")
public class AdsIncomePerformanceScoreDoc extends Model<AdsIncomePerformanceScoreDoc> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "时间id")
    private String accountPeriod;

    @Schema(description = "医生医技组核算单元id")
    private Long accountDocId;

    @Schema(description = "核算单元名称")
    private String accountDocUnitName;

    @Schema(description = "类型组id")
    private Long accountNurId;

    @Schema(description = "类型名称")
    private String accountNurUnitName;

    @Schema(description = "住院收入")
    private BigDecimal inpatIncome;

    @Schema(description = "科室cmi")
    private BigDecimal deptCmi;

    @Schema(description = "医保结余")
    private BigDecimal insurance;

    @Schema(description = "医保考核收入")
    private BigDecimal insuranceIncome;

    @Schema(description = "住院考核指标")
    private BigDecimal inpatExamIndex;

    @Schema(description = "住院核算收入")
    private BigDecimal inpatAccountIncome;

    @Schema(description = "门诊收入")
    private BigDecimal mzAccountFee;

    @Schema(description = "名医馆收入")
    private BigDecimal mygAccountFee;

    @Schema(description = "鄞州门诊收入")
    private BigDecimal yzmzAccountFee;

    @Schema(description = "医技收入")
    private BigDecimal techAccountFee;

    @Schema(description = "转icu")
    private BigDecimal icuFee;

    @Schema(description = "科间转诊")
    private BigDecimal tranferFee;

    @Schema(description = "急诊科转送病人")
    private BigDecimal noMedFee;

    @Schema(description = "转针灸科/推拿科针灸费/推拿费")
    private BigDecimal tranferAcupFee;

    @Schema(description = "外检费用")
    private BigDecimal outInspectFee;

    @Schema(description = "体检收入")
    private BigDecimal examinationIncome;

    @Schema(description = "康复跨科")
    private BigDecimal recoveryInterdis;

    @Schema(description = "介入收入")
    private BigDecimal interveneIncome;

    @Schema(description = "补上月数据")
    private BigDecimal lastmonth;

    @Schema(description = "肠镜执行")
    private BigDecimal colonExec;

    @Schema(description = "转入")
    private BigDecimal tranferIn;

    @Schema(description = "转出")
    private BigDecimal tranferOut;

    @Schema(description = "收入合计")
    private BigDecimal incomeTotal;

    @Schema(description = "成本合计")
    private BigDecimal costTotal;

    @Schema(description = "科室核算单元核算业绩分")
    private BigDecimal performanceScore;


}
