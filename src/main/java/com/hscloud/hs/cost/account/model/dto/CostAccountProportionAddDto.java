package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author banana
 * @create 2023-09-13 15:17
 */
@Data
@Schema(description = "新增核算比例入参")
public class CostAccountProportionAddDto {

    @NotBlank(message = "核算项不能为空")
    @Schema(description = "核算项 格式：核算项id")
    private String costAccountItemId;

    @NotBlank(message = "分组不能为空")
    @Schema(description = "分组 格式：分组id")
    private String typeGroupId;

    @NotBlank(message = "核算范围不能为空")
    @Schema(description = "核算范围 格式:{label:,name:} json字符串")
    private String accountObject;

    @Schema(description = "用来传递自定义选择核算对象的数据：" +
            "固定科室单元: 空" +
            "自定义科室单元：{科室单元id 科室单元名称}" +
            "自定义科室：{科室id，科室名称}" +
            "自定义人员：{人员id，人员名称}")
    private List<CommonDTO> customInput;
}

