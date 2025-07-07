package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "归集单元dto")
public class CostCollectionUnitDto {

    @Schema(description = "归集单元id")
    private Long id;

    @Schema(description = "归集单元名称")
    private String collectionName;

    @Schema(description = "归集核算科室")
    private AccountDepartmentDto collectionAccountDepartment;

    @Schema(description = "分摊科室单元")
    private List<Long> accountUnitIds;

    @Schema(description = "状态：0：启用  1:停用")
    private String status;


}
