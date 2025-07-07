package com.hscloud.hs.cost.account.model.vo.second;

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
public class UnitTaskCountVo extends UnitTaskUser {

    @Schema(description = "绩效金额")
    private BigDecimal amt;
}
