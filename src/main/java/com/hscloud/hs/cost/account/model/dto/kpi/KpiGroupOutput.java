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
public class KpiGroupOutput {

    @Schema(description = "唯一标识")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "分组名称")
    @JsonProperty("name")
    private String name;

//    @Schema(description = "角色列表")
//    @JsonProperty("role_list")
//    private List<SysRoleOutput> roleList;
}
