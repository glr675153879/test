package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Administrator
 */
@Data
@Schema(description = "基础idDTO")
public class BaseIdDTO {
    @Schema(description = "ID")
    @NotNull(message = "ID不能为空")
    private Long id;
}
