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
 * 8-医生组工作量绩效
 * </p>
 *
 * @author
 * @since 2023-12-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("nfjx_ads.ads_doc_workload_performance_accounting")
@Schema(description = "8-医生组工作量绩效")
public class AdsDocWorkloadPerformanceAccounting extends Model<AdsDocWorkloadPerformanceAccounting> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "时间id")
    private String accountPeriod;

    @Schema(description = "核算单元id")
    private Long accountUnitId;

    @Schema(description = "核算单元名称")
    private String accountUnitName;

    @Schema(description = "门诊工作量绩效")
    private BigDecimal outWorkPerformance;

    @Schema(description = "病例总点数")
    private BigDecimal casesTotalPoints;

    @Schema(description = "非药耗比")
    private BigDecimal nonDrugRatio;

    @Schema(description = "拨付比")
    private BigDecimal disbursementRatio;

    @Schema(description = "小计")
    private BigDecimal subtotal;

    @Schema(description = "高病种工作量绩效")
    @TableField("high_disease_work_performance_100")
    private BigDecimal highDiseaseWorkPerformance100;

    @Schema(description = "高病种工作量绩效小计")
    private BigDecimal highDiseaseWorkPerformanceTotal;

    @Schema(description = "DRGS总费用")
    private BigDecimal drgsTotalCost;

    @Schema(description = "医保盈亏")
    private BigDecimal profitLoss;

    @Schema(description = "盈亏率")
    private BigDecimal profitLossRatio;

    @Schema(description = "病种运营绩效小计")
    private BigDecimal diseaseOperationTotal;

    @Schema(description = "入院访视")
    private BigDecimal admissionVisits;

    @Schema(description = "入院访视除行政/中高层")
    private BigDecimal admissionVisitsMidHigh;

    @Schema(description = "二级手术")
    private BigDecimal secondarySurgery;

    @Schema(description = "三级手术")
    private BigDecimal thirdLevelSurgery;

    @Schema(description = "四级手术")
    @TableField("fourth_surgery_1")
    private BigDecimal fourthSurgery1;

    @Schema(description = "介入治疗")
    private BigDecimal interventionalTherapy;

    @Schema(description = "日间手术")
    private BigDecimal daySurgery;

    @Schema(description = "治疗")
    private BigDecimal treatment;

    @Schema(description = "四级手术")
    @TableField("fourth_surgery_2")
    private BigDecimal fourthSurgery2;

    @Schema(description = "手术收入")
    private BigDecimal surgeryIncome;

    @Schema(description = "麻醉医生四级手术")
    @TableField("fourth_surgery_3")
    private BigDecimal fourthSurgery3;

    @Schema(description = "无痛工作量-开单")
    private BigDecimal painlessWorkloadOrder;

    @Schema(description = "无痛工作量-麻醉科")
    private BigDecimal painlessWorkloadNarcotism;

    @Schema(description = "院内会诊")
    private BigDecimal inHospitalConsultation;

    @Schema(description = "重大手术会诊")
    private BigDecimal majorSurgicalConsultation;

    @Schema(description = "多学科会诊")
    private BigDecimal multiConsultation;

    @Schema(description = "门诊工作量绩效")
    private BigDecimal nursingChiMedTreat;

    @Schema(description = "门诊中药帖数")
    private BigDecimal outChiMdiPosts;

    @Schema(description = "住院中药贴数")
    private BigDecimal inpatChiMdiPosts;

    @Schema(description = "冬病夏治")
    private BigDecimal winterDuseaseSummerTread;

    @Schema(description = "床旁B超和拍片")
    private BigDecimal bedisdeXRay;

    @Schema(description = "乳腺磁共振")
    private BigDecimal breastResonance;

    @Schema(description = "医生组工作量绩效合计")
    private BigDecimal workloadPerformanceTotal;


}
