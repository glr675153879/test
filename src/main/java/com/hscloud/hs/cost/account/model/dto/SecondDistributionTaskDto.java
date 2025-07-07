package com.hscloud.hs.cost.account.model.dto;


import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "分配管理任务Dto")
public class SecondDistributionTaskDto {

    @Schema(description = "管理绩效")
    private List<SecondDistributionTaskManagementDto> taskManagementDtoList;

    @Schema(description = "单项绩效")
    private List<SecondDistributionTaskSingleDto> taskSingleDtoList;

    @Schema(description = "个人岗位绩效")
    private List<SecondDistributionTaskIndividualPostDto> taskIndividualPostDtoList;

    @Schema(description = "工作量绩效")
    private List<SecondDistributionTaskWorkloadDto> taskWorkloadDtoList;

    @Schema(description = "平均绩效")
    private List<SecondDistributionTaskAverageDto> taskAverageDtoList;

    @Schema(description = "分配概要")
    private SecondDistributionTaskSummary taskSummary;

    @Schema(description = "标识：1是最终保存，0是暂存")
    private String flag;

}
