package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

/**
 * @author Administrator
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "核算单元关系查询DTO")
public class KpiAccountRelationQueryDTO extends PageDto {
    @Schema(description = "分组code")
    @NotEmpty(message = "分组code为空")
    private String categoryCode;

    @Schema(description = "医生组科室单元名称")
    private String docAccountName;

    @Schema(description = "护士组科室单元名称")
    private String nurseAccountName;

    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType = "1";
}
