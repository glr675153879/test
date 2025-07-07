package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "当量配置")
public class KpiItemEquivalentConfigDTO extends PageDto {

    private Long id;

    private Long itemId;

    @Schema(description = "核算项code")
    @NotBlank(message = "核算项code不能为空")
    private String itemCode;

    @Schema(description = "科室id")
    @NotNull(message = "科室id不能为空")
    private Long accountUnitId;

    @Schema(description = "标化当量")
    @NotNull(message = "标化当量不能为空")
    private BigDecimal stdEquivalent;

    @Schema(description = "是否继承 0-否 1-是")
    private String inheritFlag;

    @Schema(description = "排序号")
    private Integer seq;
}
