package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author banana
 * @create 2023-09-13 19:08
 */

@Data
@Schema(description = "获取核算比例列表入参")
public class CostAccountProportionListDto extends PageDto {

    @NotBlank(message = "核算项id不能为空")
    @Schema(description = "核算项id")
    private String id;

    // 科室单元
    @Schema(description = "科室单元 （自定义/固定科室单元相关入参）")
    private String accountUnit;

    @Schema(description = "核算分组 （自定义/固定科室单元相关入参）")
    private String typeGroup;

    // 科室
    @Schema(description = "核算科室 （自定义科室相关入参）")
    private String dept;

    @Schema(description = "科室性质 （自定义科室相关入参）")
    private String deptType;

    // 员工
    @Schema(description = "姓名 （自定义人员相关入参）")
    private String name;

    @Schema(description = "工号 （自定义人员相关入参）")
    private String jobNumber;

}
