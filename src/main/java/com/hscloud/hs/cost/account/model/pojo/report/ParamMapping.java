package com.hscloud.hs.cost.account.model.pojo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 14:10]
 */
@Data
@Schema(description = "参数映射")
public class ParamMapping {

    @Schema(description = "父表字段变量名")
    private String parentCode;

    @Schema(description = "子表入参英文名")
    private String childCode;

    @Schema(description = "父表字段名称")
    private String parentText;

    @Schema(description = "子表入参名称")
    private String childText;


}
