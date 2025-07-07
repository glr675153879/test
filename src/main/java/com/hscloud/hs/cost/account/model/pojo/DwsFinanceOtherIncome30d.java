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
 * 门诊收费30d汇总表
 * </p>
 *
 * @author author
 * @since 2023-12-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("nfjx_dw.dws_finance_other_income_30d")
@Schema(description="门诊收费30d汇总表")
public class DwsFinanceOtherIncome30d extends Model<DwsFinanceOtherIncome30d> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "业务发生日期")
    private String dt;

    @Schema(description = "核算单元ID（医生组）")
    private Long accountUnitDocId;

    @Schema(description = "核算单元名称（医生组）")
    private String accountUnitDocName;

    @Schema(description = "核算收入(医生组)")
    private BigDecimal totalIncomeDoc;

    @Schema(description = "核算单元ID（护理组）")
    private Long accountUnitNurId;

    @Schema(description = "核算单元名称（护理组）")
    private String accountUnitNurName;

    @Schema(description = "核算收入（护理组）")
    private BigDecimal totalIncomeNur;

    @Schema(description = "核算单元ID（医技组）")
    private Long accountUnitTecId;

    @Schema(description = "核算单元名称（医技组）")
    private String accountUnitTecName;

    @Schema(description = "核算收入(医技组)")
    private BigDecimal accountIncomeTech;

    @Schema(description = "收入分类-套表")
    private String incomeType;

    @Schema(description = "收入类型编码")
    private String incomeTypeCode;

    @Schema(description = "收入类型名称")
    private String incomeTypeName;

    @Schema(description = "院区编码")
    private String branchCode;

    @Schema(description = "院区名称")
    private String branchName;

    @Schema(description = "科室编号")
    private String deptCode;

    @Schema(description = "科室名称")
    private String deptName;

    @Schema(description = "西药费")
    private BigDecimal amountWesMed;

    @Schema(description = "中成药")
    private BigDecimal amountPatMed;

    @Schema(description = "中药费")
    private BigDecimal amountChiMed;

    @Schema(description = "治疗费")
    private BigDecimal amountTreat;

    @Schema(description = "中医辨证论治费")
    private BigDecimal amountChiMedi;

    @Schema(description = "放射费")
    private BigDecimal amountRadiology;

    @Schema(description = "B超")
    private BigDecimal amountB;

    @Schema(description = "检查费")
    private BigDecimal amountCheck;

    @Schema(description = "针灸费")
    private BigDecimal amountAcupuncture;

    @Schema(description = "推拿费")
    private BigDecimal amountMassage;

    @Schema(description = "护理费")
    private BigDecimal amountNurse;

    @Schema(description = "煎药费")
    private BigDecimal amountDecoct;

    @Schema(description = "注射费")
    private BigDecimal amountInjection;

    @Schema(description = "理疗费")
    private BigDecimal amountPhysio;

    @Schema(description = "化验费")
    private BigDecimal amountTest;

    @Schema(description = "放 射 费")
    private BigDecimal amountRadiology2;

    @Schema(description = "输血费")
    private BigDecimal amountTransfuse;

    @Schema(description = "输氧费")
    private BigDecimal amountOxygenate;

    @Schema(description = "诊疗费")
    private BigDecimal amountTreatment;

    @Schema(description = "床位费")
    private BigDecimal amountBed;

    @Schema(description = "其他费")
    private BigDecimal amountOther;

    @Schema(description = "材料费")
    private BigDecimal amountMaterial;

    @Schema(description = "手术费")
    private BigDecimal amountSurgery;

    @Schema(description = "体检费")
    private BigDecimal amountCheckup;

    @Schema(description = "挂号费")
    private BigDecimal amountRegister;

    @Schema(description = "磁卡费")
    private BigDecimal amountCard;

    @Schema(description = "病历卡费")
    private BigDecimal amountRecord;

    @Schema(description = "内窥镜费")
    private BigDecimal amountEndoscope;

    @Schema(description = "麻醉费")
    private BigDecimal amountNarcotism;

    @Schema(description = "院前急救")
    private BigDecimal amountEmergency;

    @Schema(description = "伙食费")
    private BigDecimal amountFood;

    @Schema(description = "心电")
    private BigDecimal amountElectro;

    @Schema(description = "总计")
    private BigDecimal total;

    @Schema(description = "创建日期")
    private LocalDateTime createTime;


}
