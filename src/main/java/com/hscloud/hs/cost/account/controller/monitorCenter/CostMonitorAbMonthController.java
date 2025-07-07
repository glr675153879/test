package com.hscloud.hs.cost.account.controller.monitorCenter;

import com.hscloud.hs.cost.account.model.dto.monitorCenter.CostMonitorAbMonthQueryDto;
import com.hscloud.hs.cost.account.model.dto.monitorCenter.CostMonitorAbnormalMonTestCreateDto;
import com.hscloud.hs.cost.account.model.entity.CostMonitorAbMonth;
import com.hscloud.hs.cost.account.service.monitorCenter.CostMonitorAbMonthService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 年度异常月份数据入口
 * @author  lian
 * @date  2023-09-19 10:58
 *
 */
@RestController
@RequestMapping("account/monitorAbMonth")
@Tag(name = "年度异常月份", description = "monitorAbMonth")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostMonitorAbMonthController {

    @Autowired
    private CostMonitorAbMonthService costMonitorAbMonthService;


    /**
     * 查询监测值
     */
 /*   @GetMapping("/list")
    public R list(@Validated Page page,CostMonitorSetQueryDto setQueryDto) {
        return R.ok(costMonitorSetService.queryListAll(page,setQueryDto));
    }*/

    /**
     * 保存异常月份记录
     */
    @PostMapping("/save")
    public R add(@RequestBody CostMonitorAbMonth costMonitorAbMonth) {
        return R.ok(costMonitorAbMonthService.save(costMonitorAbMonth));
    }

    /**
     * 批量保存异常月份记录测试
     */
    @PostMapping("/batchSave")
    public R add(@RequestBody CostMonitorAbnormalMonTestCreateDto costMonitorAbMonths) {
        List<CostMonitorAbMonth> costMonitorAbMonthList = costMonitorAbMonths.getCostMonitorAbMonthList();
        costMonitorAbMonthList.forEach(costMonitorAbMonth -> costMonitorAbMonthService.save(costMonitorAbMonth));
        return R.ok();
    }

    /**
     * 定时器生成本月是否为为异常月
     * @author  lian
     * @date  2023-09-22 11:24
     *
     */
    @PostMapping("/generateCurrentMonth")
    public R generateCurrentMonth(@RequestBody CostMonitorAbMonthQueryDto costMonitorAbMonths) {
        costMonitorAbMonthService.generateCurrentMonth(costMonitorAbMonths);
        return R.ok();
    }

}
