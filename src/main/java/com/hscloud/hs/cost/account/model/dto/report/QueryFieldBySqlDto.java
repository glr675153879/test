package com.hscloud.hs.cost.account.model.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 10:35]
 */
@Data
@Schema(description = "根据sql查询字段dto")
public class QueryFieldBySqlDto {

    @Schema(description = "sql")
    private String sql;

}
