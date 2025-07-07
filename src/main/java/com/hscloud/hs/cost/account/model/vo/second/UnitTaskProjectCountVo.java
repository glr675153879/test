package com.hscloud.hs.cost.account.model.vo.second;

import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProjectCount;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProjectDetail;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "核算指标分配结果按人汇总")
public class UnitTaskProjectCountVo extends UnitTaskUser {

    @Schema(description = "发放单元")
    private BigDecimal taskCount;

    @Schema(description = "核算指标分配结果按人汇总")
    private List<UnitTaskProjectCount> projectCountList;
}
