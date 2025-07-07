package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "指标分组对象")
public class CostCommentGroupDto {


    @Schema(description = "分组id")
    private Long id;

    @NotBlank(message = "分组名称不能为空")
    @Schema(description = "分组名称")
    private String name;

    @Schema(description = "父级分组id")
    private Long parentId;

    @Schema(description = "启停用标识 0-正常，1-停用")
    private String status;


    @NotBlank(message = "分组类型不能为空")
    @Schema(description = "分组类型")
    private String typeGroup;

    @Schema(description = "备用字段：1科室成本分摊项 存财务科目；")
    private String extra;

}
