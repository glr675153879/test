package com.hscloud.hs.cost.account.model.vo.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexPlanMemberDto;
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
@Schema(description = "指标公式对应方案")
public class KpiIndexFormulaPlanVO{

    @Schema(description = "")
    private Long id;

    @Schema(description = "指标code")
    private String indexCode;

    @Schema(description = "方案分组id")
    private Long planId;

    @Schema(description = "方案分组编码")
    private String planCode;

    @Schema(description = "方案分组名称")
    private String planName;

    @Schema(description = "口径颗粒度()")
    private String caliber;

    @Schema(description = "公式")
    private String formula;

    @Schema(description = "是否下转展示")
    private String showFlag;

    @Schema(description = "是否校验")
    private String checkFlag;

    @Schema(description = "创建人")
    private Long createdId;

    @Schema(description = "创建时间")
    private Date createdDate;

    @Schema(description = "更新人")
    private Long updatedId;

    @Schema(description = "更新时间")
    private Date updatedDate;

    @Schema(description = "租户号")
    private Long tenantId;

    @Schema(description = "指标，分摊，指标项合集，json存储")
    private String memberCodes;

    @Schema(description = "人/科室编码")
    private String memberIds;

    private String excludePerson;

    private String excludeDept;

    private List<KpiIndexPlanMemberDto> excludeMembers;

    private List<KpiIndexPlanMemberDto> excludeDepts;


    @Schema(description = "人/科室")
    private List<KpiIndexPlanMemberDto> planMembers;


}