package com.hscloud.hs.cost.account.model.dto.second;

import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProject;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProjectDetail;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskProjectDetailVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Schema(description = "任务核算指标 科室二次分配 保存dto")
public class UnitTaskProjectDetailSave2DTO {

    @Schema(description = "任务核算指标明细值")
    private Long projectId;

    @Schema(description = "任务核算指标明细值")
    private List<UnitTaskProjectDetail> detailList;

}
