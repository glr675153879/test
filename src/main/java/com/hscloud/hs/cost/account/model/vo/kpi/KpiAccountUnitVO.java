package com.hscloud.hs.cost.account.model.vo.kpi;

import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * @author Administrator
 */
@Data
@Schema(description = "科室单元列表记录VO")
public class KpiAccountUnitVO {
    @Schema(description = "核算单元ID")
    private Long id;

    @Schema(description = "科室单元名称")
    private String name;

    @Schema(description = "核算分组代码")
    private String categoryCode;

    @Schema(description = "三方编码")
    private String thirdCode;

    @Schema(description = "核算分组名称")
    private String categoryName;

    @Schema(description = "核算类型")
    private String accountTypeCode;

    @Schema(description = "核算类型名称")
    private String accountTypeName;

    @Schema(description = "负责人id，多人逗号隔开")
    private String responsiblePersonId;

    @Schema(description = "负责人姓名，多人逗号隔开")
    private String responsiblePersonName;

    @Schema(description = "负责人类型dept | user | role")
    private String responsiblePersonType;

    @Schema(description = "启停用标记，0启用，1停用")
    private String status;

    @Schema(description = "科室人员类型")
    private String accountUserCode;
    private String accountUserName;

    @Schema(description = "分组code")
    private String groupCode;

    @Schema(description = "科室编码")
    private String unitCode;

    @Schema(description = "分组名称")
    private String groupCodeName;

    @Schema(description = "核算组别")
    private String accountGroup;

    @Schema(description = "科别 1门诊 2病区")
    private String deptType;

    private BigDecimal factor;

    @Schema(description = "当量数量")
    private Integer equivalentNum;

    @Schema(description = "当量配置数量")
    private Integer equivalentConfigNum;

    @Schema(description = "总当量")
    private BigDecimal totalEquivalent;

    public static KpiAccountUnitVO changeToVo(KpiAccountUnit unit){
        KpiAccountUnitVO vo = new KpiAccountUnitVO();
        BeanUtils.copyProperties(unit, vo);
        return vo;
    }
}
