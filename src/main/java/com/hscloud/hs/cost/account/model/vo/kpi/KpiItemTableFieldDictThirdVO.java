package com.hscloud.hs.cost.account.model.vo.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "核算项基础表字段字典映射")
public class KpiItemTableFieldDictThirdVO {

    private Long id;

    @Schema(description = "字典编码")
    private String dictCode;

    @Schema(description = "字典值编码")
    private String itemCode;

    @Schema(description = "第三方字典值编码")
    private String thirdItemCode;

    @Schema(description = "第三方字典值名称")
    private String thirdItemName;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "删除标记，0未删除，1已删除")
    private String delFlag;

    @Schema(description = "创建人")
    private Long createdId;

    @Schema(description = "创建时间")
    private Date createdDate;

    @Schema(description = "更新人")
    private Long updatedId;

    @Schema(description = "更新时间")
    private Date updatedDate;

    @Schema(description = "租户号")
    private Long tenantId;
}
