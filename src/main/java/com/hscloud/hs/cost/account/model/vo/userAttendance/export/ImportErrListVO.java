package com.hscloud.hs.cost.account.model.vo.userAttendance.export;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 导入错误日志
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportErrListVO implements Serializable {

    private static final long serialVersionUID = 4970889228615131379L;

    @Schema(description = "行数")
    private Integer lineNum;

    @Schema(description = "错误说明")
    private String content;

    @Schema(description = "错误说明")
    private List<String> contentList;

    @Schema(description = "行数据")
    private List<String> data;

}
