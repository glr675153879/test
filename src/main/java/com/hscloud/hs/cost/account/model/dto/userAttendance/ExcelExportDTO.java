package com.hscloud.hs.cost.account.model.dto.userAttendance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author pc
 * @date 2024/8/6
 */
@Data
public class ExcelExportDTO {



    @Schema(description = "周期")
    private Long period;

    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType = "1";
}
