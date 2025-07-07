package com.hscloud.hs.cost.account.model.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@TableName("nfjx_ads.ads_income_performance_score_nur_head")
@Schema(description="")
public class AdsIncomePerformanceScoreNurHead extends Model<AdsIncomePerformanceScoreNurHead> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "核算周期")
    private String accountPeriod;

    @Schema(description = "人员类型")
    private String rylx;

    @Schema(description = "人员工号(hsx.sys_user.user_id)")
    private String empId;

    @Schema(description = "核算单元类型")
    private String hsdylx;

    @Schema(description = "核算单元id")
    private Long accountUnitId;

    @Schema(description = "核算单元")
    private String accountUnitName;

    @Schema(description = "护士长姓名")
    private String empName;

    @Schema(description = "职务")
    private String zw;

    @Schema(description = "岗位系数")
    private BigDecimal gwxs=BigDecimal.ZERO;

    @Schema(description = "出勤系数")
    private BigDecimal cqxs=BigDecimal.ZERO;

    @Schema(description = "考核前科室总绩效")
    private BigDecimal khqkszjx=BigDecimal.ZERO;

    @Schema(description = "护士人数（在册）")
    private BigDecimal zchsrs=BigDecimal.ZERO;

    @Schema(description = "护士人数（出勤）")
    private BigDecimal cqhsrs=BigDecimal.ZERO;

    @Schema(description = "基础绩效")
    private BigDecimal jcjx=BigDecimal.ZERO;

    @Schema(description = "效率绩效")
    private BigDecimal xljx=BigDecimal.ZERO;

    @Schema(description = "规模绩效")
    private BigDecimal gmjx=BigDecimal.ZERO;

    @Schema(description = "考核得分")
    private BigDecimal khdf=BigDecimal.ZERO;

    @Schema(description = "医院奖罚")
    private BigDecimal yyjf=BigDecimal.ZERO;

    @Schema(description = "绩效工资合计")
    private BigDecimal jxgzhj=BigDecimal.ZERO;

    @Schema(description = "管理绩效（医院发放）")
    private BigDecimal gljx=BigDecimal.ZERO;

    @Schema(description = "管理绩效80%（医院发放）")
    private BigDecimal bs=BigDecimal.ZERO;

    @Schema(description = "管理绩效20%（医院发放）")
    private BigDecimal es=BigDecimal.ZERO;

    @Schema(description = "护士长/护士")
    private BigDecimal hszhs=BigDecimal.ZERO;


}
