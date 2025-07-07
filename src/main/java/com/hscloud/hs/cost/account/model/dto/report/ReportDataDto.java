package com.hscloud.hs.cost.account.model.dto.report;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 10:35]
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "报表数据dto")
public class ReportDataDto extends Page<ReportDataDto> {

    @NotBlank
    @Schema(description = "reportCode")
    private String reportCode;

    @Schema(description = "入参")
    private List<ParamDto> params;

    @Schema(description = "自定义查询参数")
    private List<CustomParamDto> customParams;

    @Schema(description = "是否预览 1是 0否 默认0")
    private String isPreview = "0";

    @Schema(description = "null 原生，其他，二次")
    private Long baseReportId;
}
