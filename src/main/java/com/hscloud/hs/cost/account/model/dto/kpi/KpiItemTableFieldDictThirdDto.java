package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "核算项基础表字段字典映射")
public class KpiItemTableFieldDictThirdDto extends PageDto {

    private Long id;

    @Schema(description = "字典编码")
    @NotNull
    private String dictCode;

    @Schema(description = "字典值编码")
    @NotNull
    private String itemCode;

    @Schema(description = "第三方字典值编码")
    @NotNull
    private String thirdItemCode;

    @Schema(description = "第三方字典值名称")
    @NotNull
    private String thirdItemName;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;
}
