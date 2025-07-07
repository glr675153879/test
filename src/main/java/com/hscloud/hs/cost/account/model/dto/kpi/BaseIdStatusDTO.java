package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Administrator
 */
@Data
@Schema(description = "基础状态修改DTO")
public class BaseIdStatusDTO {
    @Schema(description = "ID")
    @NotNull(message = "ID不能为空")
    private Long id;

    @Schema(description = "状态 0-启用 1-停用")
    @NotBlank(message = "状态不能为空")
    private String status;
}
