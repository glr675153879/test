package com.hscloud.hs.cost.account.controller;

import com.hscloud.hs.cost.account.model.dto.CostDataCollectionDto;
import com.hscloud.hs.cost.account.service.ICostDataCollectionService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author banana
 * @create 2023-09-20 13:57
 */
@RestController
@Tag(name = "数据采集中心/调度任务监控", description = "dataCollection")
@RequestMapping("/data/collection")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostDataCollectionController {

    @Autowired
    private ICostDataCollectionService iCostDataCollectionService;

    @PostMapping("/getData")
    @Operation(summary = "根据API名称获取数据")
    public R getDataByAppName(@Validated @RequestBody CostDataCollectionDto input){
        return R.ok(iCostDataCollectionService.getDataByAppName(input));
    }

}
