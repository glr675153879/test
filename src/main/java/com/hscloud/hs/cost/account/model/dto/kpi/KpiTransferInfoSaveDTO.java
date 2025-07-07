package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.vo.kpi.KpiTransferInfoVO2;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Administrator
 */
@Data
@Schema(description = "转科数据一键保存")
public class KpiTransferInfoSaveDTO {
    @Schema(description = "是否处理 0-未处理 1-已处理")
    @NotEmpty(message = "处理状态不能为空")
    private String status;

    @Schema(description = "科室id")
    private Long deptId;

    @Schema(description = "人员id")
    private Long userId;

    @Schema(description = "人员ids")
    private String userIds;

    @Schema(description = "周期")
    @NotEmpty(message = "周期不能为空")
    private String period;

    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType = "1";

    @Schema(description = "saveOneTouch2用 需要/transfer/list出参id和数据发生科室")
    private List<KpiTransferInfoVO2> list;
}
