package com.hscloud.hs.cost.account.model.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
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
@TableName("nfjx_ads.ads_income_performance_clinical_nur")
@Schema( description="")
public class AdsIncomePerformanceClinicalNur extends Model<AdsIncomePerformanceClinicalNur> {

    private static final long serialVersionUID = 1L;

    private String accountPeriod;
    private String employeeType;
    private String accountUnitType;
    private Long accountUnitId;
    private String accountUnitNur;
    private BigDecimal depPerfScore;
    private BigDecimal registNumNur;
    private BigDecimal attendanceNumNur;
    private BigDecimal perAttPerfScore;
    private BigDecimal perfPoint;
    private BigDecimal supportPerf;
    private BigDecimal perforPerf;
    private BigDecimal workloadPerf;
    private BigDecimal nurVerticalPerf;
    private BigDecimal rewardPunish;
    private BigDecimal assessmentScore;
    private BigDecimal perfTotal;
    @TableField("perf_80")
    private BigDecimal perf80;
    @TableField("perf_20")
    private BigDecimal perf20;
    private BigDecimal perfPerReg;
    private BigDecimal perfPerAtt;

}
