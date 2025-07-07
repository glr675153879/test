package com.hscloud.hs.cost.account.model.pojo;

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
 * 对应套表：14-发放审批表
 * </p>
 *
 * @author author
 * @since 2023-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("nfjx_ads.ads_income_performance_pay")
@Schema(description = "对应套表：14-发放审批表")
public class AdsIncomePerformancePay extends Model<AdsIncomePerformancePay> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "核算周期")
    private String accountPeriod;

    @Schema(description = "核算单元ID")
    private Long accountUnitId;

    @Schema(description = "核算单元")
    private String accountUnitName;

    @Schema(description = "核算分组")
    private String accountGroupCode;

    @Schema(description = "小组绩效")
    private BigDecimal amountTeam=BigDecimal.ZERO;

    @Schema(description = "科主任/护士长绩效")
    private BigDecimal amountHead=BigDecimal.ZERO;

    @Schema(description = "合计")
    private BigDecimal amountTotal=BigDecimal.ZERO;


}
