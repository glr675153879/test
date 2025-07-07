package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author Admin
 */
@Data
@Schema(description = "校验器类型")
public class ValidatorType {

    @Schema(description = "校验器类型")
    @NotBlank(message = "校验器类型不能为空")
    private String type;
}
