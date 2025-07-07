package com.hscloud.hs.cost.account.model.dto.second;

import com.hscloud.hs.cost.account.model.entity.second.ProgProjectDetail;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskProjectDetailVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "发放单元方案核算指标 保存dto 单项绩效")
public class ProgProjectDetailSaveDTO {

    @Schema(description = "任务核算指标id")
    private Long unitTaskProjectId;

    @Schema(description = "方案核算指标明细")
    private List<ProgProjectDetail> detailList;

}
