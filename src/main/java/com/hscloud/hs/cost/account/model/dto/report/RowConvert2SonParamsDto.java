package com.hscloud.hs.cost.account.model.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 10:35]
 */
@Data
@Schema(description = "入参")
public class RowConvert2SonParamsDto {

    @Schema(description = "父报表行数据 key为'变量名称'")
    private Map<String, Object> rowData;

    @Schema(description = "字段id")
    private Long fieldId;

}
