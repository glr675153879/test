package com.hscloud.hs.cost.account.model.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import java.math.BigDecimal;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2023-12-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("nfjx_ads.ads_income_performance_clinical_doc")
@Schema( description="")
public class AdsIncomePerformanceClinicalDoc extends Model<AdsIncomePerformanceClinicalDoc> {

    private static final long serialVersionUID = 1L;

    private String accountPeriod;
    private String employeeType;
    private String accountUnitType;
    private Long accountUnitId;
    private String accountUnitDoc;
    private BigDecimal depPerfScore;
    private BigDecimal inpatientPerfScore;
    private BigDecimal registNumDoc;
    private BigDecimal attendanceNumDoc;
    private BigDecimal perAttPerfScore;
    private BigDecimal depPerfPoint;
    private BigDecimal inpatPerfPoint;
    private BigDecimal supportPerf;
    private BigDecimal perforPerf;
    private BigDecimal workloadPerf;
    private BigDecimal rewardPunish;
    private BigDecimal assessmentScore;
    private BigDecimal perfTotal;
    @TableField("perf_80")
    private BigDecimal perf80;
    @TableField("perf_20")
    private BigDecimal perf20;
    private BigDecimal perfPerReg;
    private BigDecimal perfPerAtt;
    private BigDecimal perfNurDoc;


}
