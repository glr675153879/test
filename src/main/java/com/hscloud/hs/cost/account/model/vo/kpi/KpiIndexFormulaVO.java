package com.hscloud.hs.cost.account.model.vo.kpi;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.hscloud.hs.cost.account.model.dto.DictDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexPlanMemberDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndexFormula;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
* 指标公式Model
* @author you
* @since 2024-09-09
*/

@Data
@Schema(description = "指标公式表")
public class KpiIndexFormulaVO{
    @Schema(description = "")
    private Long id;

    @Schema(description = "指标code")
    private String indexCode;

    @Schema(description = "方案编码")
    private String planCode;

    @Schema(description = "公式")
    private String formula;

    @Schema(description = "条件指标公式")
    private List<String> formulas;

    @TableField(value = "member_codes")
    @Schema(description = "指标，分摊，指标项合集，json存储")
    private String memberCodes;

    private String memberCodesJson;

    @TableField(value = "member_ids")
    @Schema(description = "人/科室编码")
    private String memberIds;

    private Integer formulaGroup=1;

    private List<DictDto> memberList;

    private List<KpiIndexPlanMemberDto> planMembers;

    private List<MemberCodeObj> memberCodeObjs;

    private String excludePerson;

    private String excludeDept;

    private List<KpiIndexPlanMemberDto> excludeMembers;

    public KpiIndexFormulaVO convertByKpiIndexFormula(KpiIndexFormula kpiIndexFormula){
        return BeanUtil.copyProperties(kpiIndexFormula, KpiIndexFormulaVO.class);
    }

    @Data
    public static class MemberCodeObj{
        private String fieldCode;
        private String fieldType;
    }
}