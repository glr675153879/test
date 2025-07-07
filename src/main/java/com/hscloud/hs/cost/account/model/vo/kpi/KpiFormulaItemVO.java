package com.hscloud.hs.cost.account.model.vo.kpi;

import com.hscloud.hs.cost.account.model.dto.DictDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "公式项属性")
public class KpiFormulaItemVO {
    @Schema(description = "变量编码")
    private String code;
    @Schema(description = "字段类型")
    private String fieldType;
    @Schema(description = "字段编码")
    private String fieldCode;
    @Schema(description = "字段值")
    private String fieldValue;
    @Schema(description = "指标口径")
    private String caliber;
    @Schema(description = "口径剔除值")
    private List<DictDto> paramExcludes;
    @Schema(description = "字段描述")
    private String fieldDesc;
    private String fieldName;
    @Schema(description = "字段范围大类")
    private String paramCate;
    @Schema(description = "字段范围类型")
    private String paramType;
    @Schema(description = "字段范围值")
    private List<DictDto> paramValues;
    @Schema(description = "字段范围描述")
    private String paramDesc;
    @Schema(description = "")
    private String imputation;

    @Schema(description = "")
    private String formulaShow;
    @Schema(description = "")
    private String formulaOrigin;
    @Schema(description = "")
    private String indexCate;

    @Schema(description = "是否需要展开Y是N否")
    private String expand="N";

    private String delFlag;

    private String status;

    private KpiPlanConfigVO kpiPlanConfigVO;
}
