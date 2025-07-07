package com.hscloud.hs.cost.account.controller.imputation;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.model.dto.imputation.ImputationDeptUnitDTO;
import com.hscloud.hs.cost.account.model.dto.imputation.ImputationDeptUnitDelDTO;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationDeptUnit;
import com.hscloud.hs.cost.account.model.vo.imputation.CostAccountUnitVO;
import com.hscloud.hs.cost.account.model.vo.imputation.ImputationDeptUnitVO;
import com.hscloud.hs.cost.account.service.imputation.IImputationDeptUnitService;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * 归集科室单元
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/imputation/imputationDeptUnit")
@Tag(name = "归集科室单元", description = "归集科室单元")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@Slf4j
public class ImputationDeptUnitController {

    private final IImputationDeptUnitService imputationDeptUnitService;
    private final RedisUtil redisUtil;

    @SysLog("归集科室单元page")
    @GetMapping("/page/{imputationId}")
    @Operation(summary = "归集科室单元page")
    public R<IPage<ImputationDeptUnitVO>> page(PageRequest<ImputationDeptUnit> pr, @PathVariable Long imputationId) {
        return R.ok(imputationDeptUnitService.pageImputationDeptUnit(pr.getPage(), pr.getWrapper(), imputationId));
    }


    @SysLog("点击匹配")
    @PostMapping("/match")
    @Operation(summary = "归集点击匹配和新增共用")
    public R match(@RequestBody ImputationDeptUnitDTO imputationDeptUnitDTO) {
        return R.ok(imputationDeptUnitService.match(imputationDeptUnitDTO));
    }

    @SysLog("归集科室单元edit")
    @PostMapping("/edit")
    @Operation(summary = "归集科室单元edit")
    public R edit(@RequestBody ImputationDeptUnitDTO imputationDeptUnitDTO) {
        return R.ok(imputationDeptUnitService.updateImputationDeptUnit(imputationDeptUnitDTO));
    }

    @SysLog("归集科室单元add")
    @PostMapping("/add")
    @Operation(summary = "归集科室单元add")
    public R add(@RequestBody ImputationDeptUnitDTO imputationDeptUnitDTO) {
        return R.ok(imputationDeptUnitService.addImputationDeptUnit(imputationDeptUnitDTO));
    }

    @SysLog("归集科室单元del")
    @PostMapping("/del")
    @Operation(summary = "归集科室单元del")
    public R del(@RequestBody ImputationDeptUnitDelDTO imputationDeptUnitDelDTO) {
        return R.ok(imputationDeptUnitService.removeImputationDeptUnit(imputationDeptUnitDelDTO));
    }

    @SysLog("是否已引入上个月")
    @GetMapping("/ifLastMonth")
    @Operation(summary = "是否已引入上个月")
    public R ifLastMonth(String imputationCycle) {
        return R.ok(imputationDeptUnitService.exists(Wrappers.<ImputationDeptUnit>lambdaQuery()
                .eq(ImputationDeptUnit::getImputationCycle, imputationCycle)
                .eq(ImputationDeptUnit::getIfLastMonth, "1"))
        );
    }

    @SysLog("引入上月科室生成数据")
    @PostMapping("/lastMonth")
    @Operation(summary = "引入上月科室生成数据")
    public R generateLastMonth(@RequestBody ImputationDeptUnit imputationDeptUnit) {
        String currentCycle = imputationDeptUnit.getImputationCycle();
        String key = CacheConstants.IMP_LASTMONTH + currentCycle;
        try {
            if (redisUtil.get(key) != null) {
                return R.failed("请勿重复操作");
            } else {
                redisUtil.setLock(key, 1, 30L, TimeUnit.SECONDS);
            }
            imputationDeptUnitService.generateLastMonth(currentCycle);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            redisUtil.unLock(key);
        }

        return R.ok();
    }

    @SysLog("分页获取未匹配科室单元")
    @GetMapping("/pageUnmatched/{imputationId}")
    @Operation(summary = "分页获取未匹配科室单元")
    public R<IPage<CostAccountUnitVO>> pageUnmatched(Page page,
                                                     @PathVariable Long imputationId,
                                                     @RequestParam(value = "accountUnitName", required = false) String accountUnitName,
                                                     @RequestParam(value = "accountGroupCode", required = false) String accountGroupCode) {
        return R.ok(imputationDeptUnitService.pageUnmatched(page, imputationId, accountUnitName, accountGroupCode));
    }
}