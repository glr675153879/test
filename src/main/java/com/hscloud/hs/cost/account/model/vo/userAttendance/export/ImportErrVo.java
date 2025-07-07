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
public class ImportErrVo implements Serializable {

    private static final long serialVersionUID = -114493644421103785L;

    @Schema(description = "成功条数")
    private Integer successCount;

    @Schema(description = "失败条数")
    private Integer failCount;

    @Schema(description = "表头")
    private List<List<String>> head;

    @Schema(description = "错误信息列表")
    private List<ImportErrListVO> details;

}
