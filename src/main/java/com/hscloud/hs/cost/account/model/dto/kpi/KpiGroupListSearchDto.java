package com.hscloud.hs.cost.account.model.dto.kpi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Classname KpiGroupRoleOutput
 * @Description TODO
 * @Date 2024-09-10 9:52
 * @Created by sch
 */
@Data
public class KpiGroupListSearchDto {

    @Schema(description = "枚举  user_group 人员分组  item_group  核算项分组  后续自补")
    @JsonProperty("type")
    private String type ;

    @Schema(description = "状态;0启用 1停用")
    private Long status;

    @Schema(description = "分组名称")
    @JsonProperty("name")
    private String name;


    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType;

//    @Schema(description = "角色列表")
//    @JsonProperty("role_list")
//    private List<SysRoleOutput> roleList;
}
