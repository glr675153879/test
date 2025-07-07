package com.hscloud.hs.cost.account.model.dto.second;

import com.hscloud.hs.cost.account.model.entity.second.ProgDetailItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "发放单元方案核算指标 保存dto 科室二次分配（系数分配）")
public class ProgDetailItemSave1DTO {

    @Schema(description = "任务核算指标明细detailid")
    private Long unitTaskProjectDetailId;

    @Schema(description = "方案大项Id")
    private Long id;

    @Schema(description = "方案大项名称")
    private String itemName;

    @Schema(description = "方案字项list")
    private List<ProgDetailItem> childItemList;

}
