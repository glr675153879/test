package com.hscloud.hs.cost.account.model.dto.imputation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/23 9:37
 */
@Data
@Schema(description = "归集管理删除DTO")
public class ImputationDeptUnitDelDTO {
    @Schema(description = "主档科室ID")
    private Long imputationDeptUnitId;

    @Schema(description = "人员明细ID列表")
    private List<Long> imputationDetailsIds;
}
