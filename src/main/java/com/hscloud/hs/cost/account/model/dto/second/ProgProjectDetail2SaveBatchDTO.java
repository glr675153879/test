package com.hscloud.hs.cost.account.model.dto.second;

import com.hscloud.hs.cost.account.model.entity.second.ProgProjectDetail;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProjectDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "发放单元方案核算指标 保存dto 科室二次绩效")
public class ProgProjectDetail2SaveBatchDTO {

    @Schema(description = "方案核算指标")
    private List<UnitTaskProjectDetail> detailList;

}
