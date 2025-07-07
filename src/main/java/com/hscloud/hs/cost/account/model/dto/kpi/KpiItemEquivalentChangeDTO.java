package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "当量校准dto")
public class KpiItemEquivalentChangeDTO {

    @Schema(description = "核验任务id")
    private Long taskId = 0L;

    @Schema(description = "周期")
    @NotNull(message = "周期不能为空")
    private Long period;

    @Schema(description = "核算项id")
    @NotNull(message = "核算项id不能为空")
    private Long itemId;

    @Schema(description = "科室id")
    @NotNull(message = "科室id不能为空")
    private Long accountUnitId;

    @Schema(description = "修改者类型 0-科室 1-绩效办")
    @NotNull(message = "修改类型不能为空")
    @Pattern(regexp = "^[01]$", message = "修改类型只能为0或1")
    private String changeFlag;

    @Schema(description = "当量id")
    @NotNull(message = "当量id不能为空")
    private Long equivalentId;

    @Schema(description = "操作符 add-加，sub-减，mul-乘，eq-等于")
    @NotBlank(message = "操作符不能为空")
    @Pattern(regexp = "^(add|sub|mul|eq)$", message = "操作符只能为add、sub、mul或eq")
    private String operators = "eq";

    @Schema(description = "调整值")
    @NotNull(message = "调整值不能为空")
    private BigDecimal changeValue;

    @Schema(description = "调整原因")
    private String reason;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件地址")
    private String fileUrl;

    @Schema(description = "状态 -1:驳回 0:未提交 10:待审核 20:通过")
    private String status;

    @Schema(description = "人员当量调整list，科室核算项自定义分配使用")
    List<KpiItemEquivalentChangeDTO> childRecords;
}
