package com.hscloud.hs.cost.account.model.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 10:38]
 */
@Data
@Schema(description = "医院奖惩明细结果Vo")
@Builder
public class MetaDataBySqlVo {

    @Schema(description = "字段名")
    private String fieldName;

    @Schema(description = "字段文本")
    private String fieldText;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "字段类型")
    private String fieldType;

}
