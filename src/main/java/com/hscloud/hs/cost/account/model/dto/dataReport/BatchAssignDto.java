package com.hscloud.hs.cost.account.model.dto.dataReport;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "参数")
public class BatchAssignDto implements Serializable {

    @NotEmpty
    @Schema(description = "任务ID列表")
    private List<Long> taskIds;
    @NotBlank
    @Schema(description = "下发周期")
    private String calculateCircle;

}
