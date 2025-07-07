package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author banana
 * @create 2023-09-11 15:04
 */
@Data
@Schema(description = "绑定医护科室单元id入参")
public class BindDocNDto{

    @NotNull(message = "医生组科室单元id不为空")
    @Schema(description = "医生组科室单元id")
    private Long docAccountGroupId;

    @Schema(description = "护士组科室单元id")
    private List<CommonDTO> nurseAccountGroupList;
}
