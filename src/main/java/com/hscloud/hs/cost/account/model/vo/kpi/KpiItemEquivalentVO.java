package com.hscloud.hs.cost.account.model.vo.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class KpiItemEquivalentVO {

    private Long id;

    @Schema(description = "核算项id")
    private Long itemId;

    @Schema(description = "code用56转，带上前缀，核算项X_，分摊指标F_，核算指标Z_")
    private String code;

    @Schema(description = "核算项名称")
    private String itemName;

    @Schema(description = "核算项口径颗粒度 1人2科室3归集4固定值5多条件")
    private String caliber;

    @Schema(description = "采集方式,1,sql,2手工")
    private String acqMethod;

    @Schema(description = "周期")
    private Long period;

    @Schema(description = "是否当量分配 0-否 1-是")
    private String assignFlag;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "科室id")
    private Long accountUnitId;

    @Schema(description = "科室名称")
    private String accountUnitName;

    @Schema(description = "当量类型 1-人 2-科室")
    private String equivalentType;

    @Schema(description = "标化当量")
    private BigDecimal stdEquivalent;

    @Schema(description = "总工作量（原始值）")
    private BigDecimal totalWorkload;

    @Schema(description = "总工作量（校准值，审核通过）")
    private BigDecimal totalWorkloadAdmin;

    @Schema(description = "总工作量（修改值）")
    private BigDecimal newTotalWorkload;

    @Schema(description = "总工作当量")
    private BigDecimal totalEquivalent;

    @Schema(description = "分配方式 0-平均分配，1-系数分配，2-自定义分配")
    private String distributeType;

    @Schema(description = "状态 -1:驳回 0:未提交（重新调取） 10:待审核 20:通过")
    private String status;

    @Schema(description = "人员当量")
    private List<KpiItemEquivalentChildVO> childVOList;
}