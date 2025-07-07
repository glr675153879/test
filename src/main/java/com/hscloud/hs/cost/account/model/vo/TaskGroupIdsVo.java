package com.hscloud.hs.cost.account.model.vo;

import com.hscloud.hs.cost.account.model.dto.CostAccountTaskNewDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author 小小w
 * @date 2023/11/28 20:01
 */
@Data
public class TaskGroupIdsVo {
    @Schema(description = "核算单元名称")
    private Long taskGroupInfoId;

    @Schema(description = "核算单元名称")
    private Long taskGroupId;
}
