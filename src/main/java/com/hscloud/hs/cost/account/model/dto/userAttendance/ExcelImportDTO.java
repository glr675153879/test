package com.hscloud.hs.cost.account.model.dto.userAttendance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author pc
 * @date 2024/8/6
 */
@Data
public class ExcelImportDTO {

    @Schema(description = "错误处理方式：1继续，2终止")
    private String continueFlag;

    @Schema(description = "导入模式：1覆盖，2增量导入")
    private String overwriteFlag;

    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType = "1";
}
