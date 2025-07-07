package com.hscloud.hs.cost.account.model.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.dom4j.rule.Mode;

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
@TableName("nfjx_ads.ads_income_performance_logistics")
@Schema( description="")
public class AdsIncomePerformanceLogistics extends Model<AdsIncomePerformanceLogistics> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "绩效考核周期")
    private String accountPeriod;

    @Schema(description = "核算单元ID")
    private Long accountUnitId;

    @Schema(description = "核算单元名称")
    private String accountUnitName;

    @Schema(description = "行政级别")
    private String xzjb;

    @Schema(description = "群体")
    private String qt;

    @Schema(description = "标准")
    private String bz;

    @Schema(description = "岗位系数")
    private String gwxs;

    @Schema(description = "参照水平")
    private Double czsp;

    @Schema(description = "核算人数")
    private Double hsrs;

    @Schema(description = "出勤天数")
    private Double cqts;

    @Schema(description = "出勤系数")
    private Double cqxs;

    @Schema(description = "科室绩效")
    private Double ksjx;

    @Schema(description = "门诊工作量绩效")
    private String mzgzljx;

    @Schema(description = "访视绩效")
    private String fsjx;

    @Schema(description = "考核得分")
    private String khdf;

    @Schema(description = "医院奖罚")
    private String yyjf;

    @Schema(description = "绩效工资合计")
    private Double jxgzhj;

    @Schema(description = "人均绩效")
    private Double rjjx;


}
