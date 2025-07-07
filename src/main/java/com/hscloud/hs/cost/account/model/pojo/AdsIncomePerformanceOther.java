package com.hscloud.hs.cost.account.model.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.math.BigDecimal;

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
 * @author author
 * @since 2023-12-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("nfjx_ads.ads_income_performance_other")
@Schema(description = "其他核算绩效")
public class AdsIncomePerformanceOther extends Model<AdsIncomePerformanceOther> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "核算时间")
    private String accountPeriod;

    @Schema(description = "人员类型")
    private String rylx;

    @Schema(description = "核算单元id")
    private Long accountUnitId;

    @Schema(description = "科室")
    private String ks;

    @Schema(description = "科室人数在册")
    private BigDecimal ksrsZc=BigDecimal.ZERO;

    @Schema(description = "科室人数出勤")
    private BigDecimal ksrsCq=BigDecimal.ZERO;

    @Schema(description = "考核项目")
    private String khxm;

    @Schema(description = "数量")
    private BigDecimal sl=BigDecimal.ZERO;

    @Schema(description = "标准")
    private BigDecimal bz=BigDecimal.ZERO;

    @Schema(description = "医院奖惩")
    private BigDecimal yyjc=BigDecimal.ZERO;

    @Schema(description = "考核得分")
    private BigDecimal khdf=BigDecimal.ZERO;

    @Schema(description = "小计")
    private BigDecimal xj=BigDecimal.ZERO;

//    @Schema(description = "小计80%")
//    @TableField("xj_0.8")
//    private Integer xjEight;
//
//    @Schema(description = "小计20%")
//    @TableField("xj_0.2")
//    private Integer xj0Two;

    @Schema(description = "出勤人均")
    private BigDecimal cqrj=BigDecimal.ZERO;

    @Schema(description = "在册人均")
    private BigDecimal zcrj=BigDecimal.ZERO;


}
