package com.hscloud.hs.cost.account.model.pojo;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 住院收费30d汇总表
 * </p>
 *
 * @author author
 * @since 2023-12-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("nfjx_dw.dws_finance_inpat_fee_30d")
@Schema(description="住院收费30d汇总表")
public class DwsFinanceInpatFee30d extends Model<DwsFinanceInpatFee30d> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "业务发生日期")
    private String dt;

    @Schema(description = "核算收入(医生组)")
    private BigDecimal totalIncomeDoc;

    @Schema(description = "核算单元ID（医生组）")
    private Long accountUnitDocId;

    @Schema(description = "核算单元名称（医生组）")
    private String accountUnitDocName;

    @Schema(description = "人员工号")
    private String empId;

    @Schema(description = "人员姓名")
    private String empName;

    @Schema(description = "执行科室编号")
    private String deptId;

    @Schema(description = "执行科室名称")
    private String deptName;

    @Schema(description = "西药费")
    private BigDecimal amountWesMed=BigDecimal.ZERO;

    @Schema(description = "中成药")
    private BigDecimal amountPatMed=BigDecimal.ZERO;

    @Schema(description = "中药费")
    private BigDecimal amountChiMed=BigDecimal.ZERO;

    @Schema(description = "治疗费")
    private BigDecimal amountTreat=BigDecimal.ZERO;

    @Schema(description = "放射费")
    private BigDecimal amountRadiology=BigDecimal.ZERO;

    @Schema(description = "B超")
    private BigDecimal amountB=BigDecimal.ZERO;

    @Schema(description = "检查费")
    private BigDecimal amountCheck=BigDecimal.ZERO;

    @Schema(description = "针灸费")
    private BigDecimal amountAcupuncture=BigDecimal.ZERO;

    @Schema(description = "推拿费")
    private BigDecimal amountMassage=BigDecimal.ZERO;

    @Schema(description = "护理费")
    private BigDecimal amountNurse=BigDecimal.ZERO;

    @Schema(description = "煎药费")
    private BigDecimal amountDecoct=BigDecimal.ZERO;

    @Schema(description = "注射费")
    private BigDecimal amountInjection=BigDecimal.ZERO;

    @Schema(description = "理疗费")
    private BigDecimal amountPhysio=BigDecimal.ZERO;

    @Schema(description = "化验费")
    private BigDecimal amountTest=BigDecimal.ZERO;

    @Schema(description = "放 射 费")
    private BigDecimal amountRadiology2=BigDecimal.ZERO;

    @Schema(description = "输血费")
    private BigDecimal amountTransfuse=BigDecimal.ZERO;

    @Schema(description = "输氧费")
    private BigDecimal amountOxygenate=BigDecimal.ZERO;

    @Schema(description = "诊疗费")
    private BigDecimal amountTreatment=BigDecimal.ZERO;

    @Schema(description = "床位费")
    private BigDecimal amountBed=BigDecimal.ZERO;

    @Schema(description = "其他费")
    private BigDecimal amountOther=BigDecimal.ZERO;

    @Schema(description = "材料费")
    private BigDecimal amountMaterial=BigDecimal.ZERO;

    @Schema(description = "手术费")
    private BigDecimal amountSurgery=BigDecimal.ZERO;

    @Schema(description = "体检费")
    private BigDecimal amountCheckup=BigDecimal.ZERO;

    @Schema(description = "挂号费")
    private BigDecimal amountRegister=BigDecimal.ZERO;

    @Schema(description = "磁卡费")
    private BigDecimal amountCard=BigDecimal.ZERO;

    @Schema(description = "病历卡费")
    private BigDecimal amountRecord=BigDecimal.ZERO;

    @Schema(description = "内窥镜费")
    private BigDecimal amountEndoscope=BigDecimal.ZERO;

    @Schema(description = "麻醉费")
    private BigDecimal amountNarcotism=BigDecimal.ZERO;

    @Schema(description = "院前急救")
    private BigDecimal amountEmergency=BigDecimal.ZERO;

    @Schema(description = "伙食费")
    private BigDecimal amountFood=BigDecimal.ZERO;

    @Schema(description = "药耗收入")
    private BigDecimal amountMed=BigDecimal.ZERO;

    @Schema(description = "非药耗收入")
    private BigDecimal amountNoneMed=BigDecimal.ZERO;

    @Schema(description = "总收入")
    private BigDecimal total=BigDecimal.ZERO;

    @Schema(description = "创建日期")
    private LocalDateTime createTime;


}
