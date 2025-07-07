package com.hscloud.hs.cost.account.model.dto;

import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskIndividualPost;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Schema(description = "分配个人岗位绩效Dto")
public class SecondDistributionIndividualPostDto {

    @Schema(description = "关联任务个人岗位绩效列表")
    private List<SecondDistributionTaskIndividualPost> taskIndividualPostDtoList;

    @Schema(description = "关联任务id")
    @NotNull(message = "关联任务id不能为空")
    private Long taskUnitRelateId;

    @Schema(description = "核算方案id")
    @NotNull(message = "核算方案id不能为空")
    private Long planId;


    @Schema(description = "核算指标id")
    @NotNull(message = "核算指标id不能为空")
    private Long indexId;

}
