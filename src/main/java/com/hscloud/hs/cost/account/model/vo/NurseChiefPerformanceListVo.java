package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author banana
 * @create 2023-12-06 16:01
 */
@Data
public class NurseChiefPerformanceListVo {

    @Schema(description = "核算周期")
    private Integer accountPeriod;

    @Schema(description = "人员类型")
    private String rylx;

    @Schema(description = "核算单元类型")
    private String hsdylx;

    @Schema(description = "核算单元id")
    private Long accountUnitId;

    @Schema(description = "核算单元")
    private String accountUnitName;

    @Schema(description = "人员工号(hsx.sys_user.user_id)")
    private String empId;

    @Schema(description = "护士长姓名")
    private String empName;

    @Schema(description = "职务")
    private String zw;

    @Schema(description = "岗位系数")
    private String gwxs;

    @Schema(description = "出勤系数")
    private String cqxs;

    @Schema(description = "考核前科室总绩效")
    private String khqkszjx;

    @Schema(description = "护士人数（在册）")
    private String zchsrs;

    @Schema(description = "护士人数（出勤）")
    private String cqhsrs;

    @Schema(description = "基础绩效")
    private String jcjx;

    @Schema(description = "效率绩效")
    private String xljx;

    @Schema(description = "规模绩效")
    private String gmjx;

    @Schema(description = "考核得分")
    private String khdf;

    @Schema(description = "医院奖罚")
    private String yyjf;

    @Schema(description = "绩效工资合计")
    private String jxgzhj;

    @Schema(description = "管理绩效（医院发放）")
    private String gljx;

    @Schema(description = "管理绩效80%（医院发放）")
    private String bs;

    @Schema(description = "管理绩效20%（医院发放）")
    private String es;

    @Schema(description = "护士长/护士")
    private String hszhs;

    @Schema(description = "绩效工资明细_扶持绩效")
    private Double supportPerf;

    @Schema(description = "绩效工资明细_业绩绩效")
    private Double perforPerf;

    @Schema(description = "绩效工资明细_工作量绩效")
    private Double workloadPerf;

    @Schema(description = "绩效工资明细_护理垂直绩效")
    private Double nurVerticalPerf;

}
