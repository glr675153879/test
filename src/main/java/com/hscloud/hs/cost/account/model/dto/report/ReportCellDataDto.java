package com.hscloud.hs.cost.account.model.dto.report;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 10:35]
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "单元格数据dto")
public class ReportCellDataDto extends Page<ReportCellDataDto> {

    private static final long serialVersionUID = -1347038663872959367L;
    @NotBlank
    @Schema(description = "reportCode")
    private String reportCode;

    @Schema(description = "入参")
    private List<ParamDto> params;

    @NotEmpty
    @Schema(description = "需获取数据")
    private List<String> fieldNameList;

}
