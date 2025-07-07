package com.hscloud.hs.cost.account.model.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 11:30]
 */
@Data
@Schema(description = "参数")
public class SonReportParamVo {

    @Schema(description = "reportCode")
    private String reportCode;

    @Schema(description = "reportId")
    private Long reportId;

    @Schema(description = "子报表参数")
    private List<ParamVo> params;

}
