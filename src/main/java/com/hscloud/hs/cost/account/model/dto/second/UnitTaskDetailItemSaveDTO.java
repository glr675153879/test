package com.hscloud.hs.cost.account.model.dto.second;

import com.hscloud.hs.cost.account.model.vo.second.UnitTaskDetailItemVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "任务科室二次分配明细大项值 保存dto")
public class UnitTaskDetailItemSaveDTO {

    @Schema(description = "核算指标明细id")
    private Long projectDetailId;

    @Schema(description = "任务科室二次分配明细大项值")
    private List<UnitTaskDetailItemVo> userList;

}
