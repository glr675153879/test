package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "返回分组列表对象")
public class CostGroupListVo {
    @Schema(description = "分组id")
    private Long id;

    @Schema(description = "分组名称")
    private String name;

    @Schema(description = "分组对象")
    private List<CostGroupListVo> groupList = new ArrayList<>();

    @Schema(description = "父级分组id")
    private Long parentId;

    @Schema(description = "启停用标识 0-正常，1-停用")
    private String status;

    @Schema(description = "是否为数据上报核算项")
    private Boolean isReport;

    @Schema(description = "是否为数据上报核算项")
    private String isSystem;

    @Schema(description = "分组类型")
    private String typeGroup;

    @Schema(description = "备用字段：1科室成本分摊项 存财务科目；")
    private String extra;
}
