package com.hscloud.hs.cost.account.model.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 10:35]
 */
@Data
@Schema(description = "复制报表")
public class ReportCopyDto {

    @Schema(description = "被复制报表id")
    private Long sourceReportId;

    @Schema(description = "报表名称")
    private String reportName;

    @Schema(description = "所在分组")
    private Long groupId;

}
