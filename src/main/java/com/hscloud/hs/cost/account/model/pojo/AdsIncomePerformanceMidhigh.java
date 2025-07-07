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
@TableName("nfjx_ads.ads_income_performance_midhigh")
@Schema( description="")
public class AdsIncomePerformanceMidhigh extends Model<AdsIncomePerformanceMidhigh> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "时间")
    private  String accountPeriod;

    @Schema(description = "人员类别")
    private String rylb;

    @Schema(description = "部门")
    private String bm;

    @Schema(description = "核算单元id")
    private Long accountUnitId;

    @Schema(description = "核算单元名称")
    private String accountUnitName;

    @Schema(description = "姓名")
    private String empName;

    @Schema(description = "人员工号(hsx.sys_user.user_id)")
    private String empId;

    @Schema(description = "群体")
    private String qt;

    @Schema(description = "标准")
    private BigDecimal bz=BigDecimal.ZERO;

    @Schema(description = "岗位系数")
    private BigDecimal gwxs=BigDecimal.ZERO;

    @Schema(description = "出勤天数")
    private BigDecimal cqts=BigDecimal.ZERO;

    @Schema(description = "出勤系数")
    private BigDecimal cqxs=BigDecimal.ZERO;

    @Schema(description = "岗位绩效")
    private BigDecimal gwjx=BigDecimal.ZERO;

    @Schema(description = "门诊工作量")
    private BigDecimal mzgzl=BigDecimal.ZERO;

    @Schema(description = "访问绩效50")
    private BigDecimal fwjx50=BigDecimal.ZERO;

    @Schema(description = "考核分数")
    private BigDecimal khfs=BigDecimal.ZERO;

    @Schema(description = "奖罚")
    private BigDecimal jf=BigDecimal.ZERO;

    @Schema(description = "体检奖励")
    private BigDecimal tjjl=BigDecimal.ZERO;

    @Schema(description = "绩效工资合计")
    private BigDecimal jxgzhj=BigDecimal.ZERO;


}
