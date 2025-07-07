package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Admin
 */
@Schema(description = "启停用对象")
@Data
public class EnableDto {

    @Schema(description = "id")
    @NotBlank(message = "id不能为空")
    private String id;

    @Schema(description = "状态")
    @NotBlank(message = "状态不能为空")
    private String status;
}
