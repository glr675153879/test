package com.hscloud.hs.cost.account.model.vo.second;

import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProject;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProjectCount;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "核算指标分配结果按人汇总")
public class SecondDetailCountVo extends UnitTaskUser {

    @Schema(description = "核算指标分配结果按人汇总")
    private List<UnitTaskProject> unitTaskProjectList;

    @Schema(description = "任务总金额")
    private BigDecimal totalAmt = BigDecimal.ZERO;
}
