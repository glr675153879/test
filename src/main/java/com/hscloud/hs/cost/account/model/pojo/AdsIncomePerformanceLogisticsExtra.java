package com.hscloud.hs.cost.account.model.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
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
 * @author 
 * @since 2023-12-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("nfjx_ads.ads_income_performance_logistics_extra")
@Schema( description="")
public class AdsIncomePerformanceLogisticsExtra extends Model<AdsIncomePerformanceLogisticsExtra> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "核算周期")
    private String accountPeriod;

    @Schema(description = "核算单元ID")
    private Long accountUnitId;

    @Schema(description = "人员类别")
    private String rylb;

    @Schema(description = "核算单元名称")
    private String accountUnitName;

    @Schema(description = "部门")
    private String bm;

    @Schema(description = "人员工号")
    private String empId;

    @Schema(description = "姓名")
    private String empName;

    @Schema(description = "群体")
    private String qt;

    @Schema(description = "标准")
    private Double bz;

    @Schema(description = "出勤天数")
    private String cqts;

    @Schema(description = "出勤系数")
    private Double cqxs;

    @Schema(description = "岗位绩效")
    private Double gwjx;

    @Schema(description = "系数")
    private String xs;

    @Schema(description = "考核得分")
    private String khdf;

    @Schema(description = "医院奖罚")
    private String yyjf;

    @Schema(description = "绩效工资合计")
    private Double jxgzhj;

    @Schema(description = "八十")
    private Double bs;

    @Schema(description = "二十")
    private Double es;


}
