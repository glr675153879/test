package com.hscloud.hs.cost.account.controller.monitorCenter;


import com.hscloud.hs.cost.account.model.entity.CostMonitorData;
import com.hscloud.hs.cost.account.service.monitorCenter.CostMonitorDataService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * 监测动态监测值测试数据
 * @author  lian
 * @date  2023-09-19 10:58
 *
 */
@RestController
@RequestMapping("account/monitorData")
@Tag(name = "监测动态监测值测试数据", description = "monitorData")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostMonitorDataController {
    @Autowired
    private CostMonitorDataService costMonitorDataService;

    /**
     * 新增测试数据
     */
    @PostMapping("/add")
    public R add(@Validated @RequestBody CostMonitorData costMonitorData) {
        return R.ok(costMonitorDataService.save(costMonitorData));
    }

    /**
     * 批量测试数据
     */
    @PostMapping("/batchTestValue")
    public R batchTestValue(@Validated @RequestBody CostMonitorData costMonitorData) {
        return R.ok(costMonitorDataService.batchTestValue(costMonitorData));
    }



}
