package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author Administrator
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "转科数据明细查询参数")
public class KpiTransferInfoDTO extends PageDto {
    @Schema(description = "是否处理 0-未处理 1-已处理")
    @NotEmpty(message = "处理状态不能为空")
    private String status;

    @Schema(description = "人员id")
    @NotNull(message = "人员id不能为空")
    private Long userId;

    @Schema(description = "周期")
    @NotEmpty(message = "周期不能为空")
    private String period;

    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType = "1";
}
