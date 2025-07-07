package com.hscloud.hs.cost.account.controller;

import com.hscloud.hs.cost.account.model.dto.TaskResultQueryDto;
import com.hscloud.hs.cost.account.service.IDistributionTaskGroupService;
import com.hscloud.hs.cost.account.service.IDistributionTaskService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/distribution/task")
@Tag(description = "distributionTask", name = "绩效任务")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@RequiredArgsConstructor
public class DistributionTaskController {

    private final IDistributionTaskService distributionTaskService;

    private final IDistributionTaskGroupService distributionTaskGroupService;

    /**
     * 查询所有任务分组名称
     * @return
     */
    @GetMapping("/group/names")
    @Operation(description = "核算任务分组名称")
    public R getGroupNames() {
        return R.ok(distributionTaskGroupService.getTaskGroupNames());
    }

    @GetMapping("/result/unit")
    @Operation(description = "任务结果", summary = "任务结果")
    public R getResult(TaskResultQueryDto queryDto) {
        return R.ok(distributionTaskService.listResult(queryDto));
    }
}
