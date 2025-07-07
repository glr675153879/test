package com.hscloud.hs.cost.account.model.dto.second;

import com.hscloud.hs.cost.account.model.entity.second.ProgDetailItem;
import com.hscloud.hs.cost.account.model.entity.second.ProgProjectDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "发放单元方案核算指标 保存dto 科室二次分配（工作量）")
public class ProgDetailItemSave2DTO {

    @Schema(description = "任务核算指标明细detailid")
    private Long unitTaskProjectDetailId;

    @Schema(description = "方案大项")
    private List<ProgDetailItem> itemList;

}
