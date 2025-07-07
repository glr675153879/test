package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

/**
 * 转科数据查询参数
 * @author Administrator
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "转科数据查询参数")
public class KpiTransferListDTO extends PageDto {
    @Schema(description = "是否处理 0-未处理 1-已处理")
    @NotEmpty(message = "处理状态不能为空")
    private String status;

    @Schema(description = "周期")
    private String period;

    @Schema(description = "人员id")
    private Long userId;

    @Schema(description = "人员姓名")
    private String userName;

    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType = "1";
}
