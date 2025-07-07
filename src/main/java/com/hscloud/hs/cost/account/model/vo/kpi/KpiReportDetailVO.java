package com.hscloud.hs.cost.account.model.vo.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class KpiReportDetailVO {

    @Schema(description="")
    private Long id;

    @Schema(description="出参编码")
    private String code;

    @Schema(description="出参名称")
    private String name;

    @Schema(description="口径颗粒度 1人2科室3归集4固定值")
    private String caliber;

    @Schema(description="创建人")
    private Long createdId;

    @Schema(description="创建时间")
    private Date createdDate;

    @Schema(description="更新人")
    private Long updatedId;

    @Schema(description="更新时间")
    private Date updatedDate;

    @Schema(description="租户号")
    private Long tenantId;

    @Schema(description="指标code")
    private String indexCode;

    @Schema(description="接口CODE")
    private String reportCode;
}
