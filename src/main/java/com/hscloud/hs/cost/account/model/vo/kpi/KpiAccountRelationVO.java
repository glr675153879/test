package com.hscloud.hs.cost.account.model.vo.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 */
@Data
@Schema(description = "科室单元关系VO")
public class KpiAccountRelationVO {
    @Schema(description = "医生组科室单元")
    private Long docAccountId;

    @Schema(description = "医生组科室单元名称")
    private String docAccountName;

    @Schema(description = "护士组科室单元列表")
    private List<KpiAccountDocNurseVO> nurseAccountList;

    private Long status;
}
