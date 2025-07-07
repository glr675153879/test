package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Classname KpiCoefficientDto
 * @Description TODO
 * @Date 2024/11/27 13:41
 * @Created by sch
 */
@Data
public class KpiValueAdjustSearchDto extends PageDto {

    @Schema(description = "类型")
    private String type;

    @Schema(description = "code")
    private String code;

    @Schema(description = "codeName")
    private String codeName;

    @Schema(description = "用户Id")
    private Long userId;

    @Schema(description = "用户姓名")
    private String userName;

    @Schema(description = "科室id")
    private Long accountUnit;

    @Schema(description = "科室名称")
    private String  accountUnitName;

    private String busiType;

    private String period;

    private String remark;

}
