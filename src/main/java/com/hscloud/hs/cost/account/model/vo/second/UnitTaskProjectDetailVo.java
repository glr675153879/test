package com.hscloud.hs.cost.account.model.vo.second;

import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProjectDetail;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "任务核算指标明细值")
public class UnitTaskProjectDetailVo extends UnitTaskUser {

    @Schema(description = "任务核算指标明细值")
    private List<UnitTaskProjectDetail> detailList;
}
