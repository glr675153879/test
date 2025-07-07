package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 物资核算状态
 * @author  lian
 * @date  2024/6/2 22:49
 *
 */

@Data
@Schema(description = "物资核算状态状态dto")
public class MaterialChargeUpdateDto {

    @Schema(description = "id")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "状态：N：否  Y:是  ")
    @NotBlank(message = "是否收费不能为空")
    private String isCharge;
}
