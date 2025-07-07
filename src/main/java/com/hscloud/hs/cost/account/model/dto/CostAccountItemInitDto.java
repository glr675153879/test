package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Admin
 */
@Data
@Schema(description = "成本核算项初始化对象")
public class CostAccountItemInitDto {

    @Schema(description = "成本核算项分组id")
    @NotNull(message = "成本核算项分组id不能为空")
    private Long groupId;


    @Schema(description = "成本核算项类型")
    @NotBlank(message = "成本核算项类型不能为空")
    private String type;
}
