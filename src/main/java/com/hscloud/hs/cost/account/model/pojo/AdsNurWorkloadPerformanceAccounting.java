package com.hscloud.hs.cost.account.model.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * <p>
 * 8-护理组工作量绩效
 * </p>
 *
 * @author
 * @since 2023-12-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("nfjx_ads.ads_nur_workload_performance_accounting")
@Schema(description = "8-护理组工作量绩效")
public class AdsNurWorkloadPerformanceAccounting extends Model<AdsNurWorkloadPerformanceAccounting> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "时间id")
    private String accountPeriod;

    @Schema(description = "核算单元id")
    private Long accountUnitId;

    @Schema(description = "核算单元名称")
    private String accountUnitName;

    @Schema(description = "病例总点数")
    private BigDecimal casesTotalPoints;

    @Schema(description = "非药耗比")
    private BigDecimal nonDrugRatio;

    @Schema(description = "小计")
    private BigDecimal subtotal;

    @Schema(description = "高病种工作量绩效")
    @TableField("high_disease_work_performance_100")
    private BigDecimal highDiseaseWorkPerformance100;

    @Schema(description = "高病种工作量绩效小计")
    private BigDecimal highDiseaseWorkPerformanceTotal;

    @Schema(description = "住院护理特色中医治疗")
    private BigDecimal inpatNursingChiMedTreat;

    @Schema(description = "门诊护理特色中医治疗")
    private BigDecimal outNursingChiMedTreat;

    @Schema(description = "四级手术")
    @TableField("fourth_surgery_1")
    private BigDecimal fourthSurgery1;

    @Schema(description = "碳14")
    private BigDecimal c14;

    @Schema(description = "胃镜")
    private BigDecimal gastroscope;

    @Schema(description = "支气管镜")
    private BigDecimal Bronchoscopy;

    @Schema(description = "肠镜")
    private BigDecimal colonoscopy;

    @Schema(description = "体检人次")
    private BigDecimal physicalNumber;

    @Schema(description = "护理组工作量绩效合计")
    private BigDecimal workloadPerformanceTotal;


}
