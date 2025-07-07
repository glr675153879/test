package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Administrator
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "核算单元列表查询DTO")
public class KpiAccountUnitQueryDTO extends PageDto {
    @Schema(description = "科室单元名称")
    private String name;

    @Schema(description = "启停用标记，0启用，1停用")
    private String status;

    @Schema(description = "核算分组")
    private String categoryCode;

    @Schema(description = "核算类型")
    private String accountTypeCode;

    @Schema(description = "科室单元人员类型")
    private String accountUserCode;

    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType = "1";

    @Schema(description = "分组")
    private String groupCode;

    @Schema(description = "核算组别")
    private String accountGroup;

    @Schema(description = "1过滤虚拟核算单元 2不过滤虚拟核算单元")
    private String virtualFilter;

    @Schema(description = "科室负责人id")
    private String responsiblePersonId;

    @Schema(description = "科室负责人中文")
    private String responsiblePersonName;


    @Schema(description = "科别 1门诊 2病区")
    private String deptType;

    @Schema(description = "Y 已配置 N 未配置")
    private String isDisposition;
}
