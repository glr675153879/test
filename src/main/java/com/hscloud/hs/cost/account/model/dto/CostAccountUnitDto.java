package com.hscloud.hs.cost.account.model.dto;

import com.hscloud.hs.cost.account.model.entity.CostUnitExcludedInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author YJM
 * @date 2023-09-05 14:11
 */
@Data
@Schema(description = "核算单元")
public class CostAccountUnitDto {
    @Schema(description = "科室单元ID")
    private Long id;

    @Schema(description = "科室单元名称")
    @NotBlank(message = "科室单元名称不能为空")
    private String name;

    @Schema(description = "核算科室/人列表")
    private List<CostUnitRelateInfoDto> costUnitRelateInfo;

    @Schema(description = "核算分组")
    private String accountGroupCode;

    @Schema(description = "负责人 ")
    private CommonDTO responsiblePerson;

    @Schema(description = "启停用标记，0启用，1停用")
    private String status;

    @Schema(description = "删除标记，0未删除，1已删除")
    private String delFlag;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
