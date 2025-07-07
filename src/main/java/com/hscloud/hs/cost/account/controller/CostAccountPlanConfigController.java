package com.hscloud.hs.cost.account.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.CostAccountPlanConfigDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountPlanConfigNewDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountPlanConfigQueryDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountPlanFormulaVerificationDto;
import com.hscloud.hs.cost.account.model.vo.CostAccountPlanConfigVo;
import com.hscloud.hs.cost.account.service.CostAccountPlanConfigService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account/plan/config")
@Tag(name = "account_plan_config", description = "核算方案配置")
public class CostAccountPlanConfigController {

    @Autowired
    private CostAccountPlanConfigService costAccountPlanConfigService;

    @PostMapping("/add")
    public R addConfig(@RequestBody CostAccountPlanConfigDto costAccountPlanConfigDto) {
        costAccountPlanConfigService.saveConfig(costAccountPlanConfigDto);
        return R.ok();
    }


    @PostMapping("/addNew")
    public R addConfigNew(@RequestBody @Validated CostAccountPlanConfigNewDto costAccountPlanConfigNewDto) {
        return R.ok(costAccountPlanConfigService.saveConfigNew(costAccountPlanConfigNewDto));
    }


    @PutMapping("/update")
    public R updateConfig(@RequestBody CostAccountPlanConfigDto costAccountPlanConfigDto) {
        costAccountPlanConfigService.updateConfig(costAccountPlanConfigDto);
        return R.ok();
    }

    @GetMapping("/list")
    public R<IPage<CostAccountPlanConfigVo>> listConfig(CostAccountPlanConfigQueryDto costAccountPlanConfigQueryDto) {
        return R.ok(costAccountPlanConfigService.newListConfig(costAccountPlanConfigQueryDto));
    }




    @DeleteMapping("/delete/{id}")
    public R deleteConfig(@PathVariable Long id) {
        return R.ok(costAccountPlanConfigService.deleteConfig(id));
    }


    @GetMapping("/listName/{planId}")
    public R listConfigName(@PathVariable Long planId) {

        return R.ok(costAccountPlanConfigService.listConfigName(planId));
    }

    /**
     * * 方案总公式校验
     *
     * @param dto
     * @return
     */
    @PostMapping("/verification")
    public R verificationCostFormula(@RequestBody CostAccountPlanFormulaVerificationDto dto) {
        return R.ok(costAccountPlanConfigService.verificationCostFormula(dto));
    }
}
