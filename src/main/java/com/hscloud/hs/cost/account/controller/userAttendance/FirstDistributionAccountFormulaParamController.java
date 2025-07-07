package com.hscloud.hs.cost.account.controller.userAttendance;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.userAttendance.FirstDistributionAccountFormulaParamDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.userAttendance.FirstDistributionAccountFormulaParam;
import com.hscloud.hs.cost.account.service.userAttendance.IFirstDistributionAccountFormulaParamService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 一次分配考勤公式参数
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/userAttendance/firstDistributionAccountFormulaParam")
@Tag(name = "firstDistributionAccountFormulaParam", description = "一次分配考勤公式参数")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class FirstDistributionAccountFormulaParamController {

    private final IFirstDistributionAccountFormulaParamService firstDistributionAccountFormulaParamService;

    @SysLog("一次分配考勤公式参数info")
    @GetMapping("/info/{id}")
    @Operation(summary = "一次分配考勤公式参数info")
    public R<FirstDistributionAccountFormulaParam> info(@PathVariable Long id) {
        return R.ok(firstDistributionAccountFormulaParamService.getById(id));
    }

    @SysLog("一次分配考勤公式参数page")
    @GetMapping("/page")
    @Operation(summary = "一次分配考勤公式参数page")
    public R<IPage<FirstDistributionAccountFormulaParam>> page(PageRequest<FirstDistributionAccountFormulaParam> pr) {
        return R.ok(firstDistributionAccountFormulaParamService.page(pr.getPage(), pr.getWrapper()));
    }

    @SysLog("一次分配考勤公式参数list")
    @GetMapping("/list")
    @Operation(summary = "一次分配考勤公式参数list")
    public R<List<FirstDistributionAccountFormulaParam>> list(PageRequest<FirstDistributionAccountFormulaParam> pr) {
        return R.ok(firstDistributionAccountFormulaParamService.list(pr.getWrapper()));
    }

    @SysLog("一次分配考勤公式参数add")
    @PostMapping("/add")
    @Operation(summary = "一次分配考勤公式参数add")
    public R add(@RequestBody FirstDistributionAccountFormulaParam firstDistributionAccountFormulaParam) {
        return R.ok(firstDistributionAccountFormulaParamService.save(firstDistributionAccountFormulaParam));
    }

    @SysLog("一次分配考勤公式参数edit")
    @PostMapping("/edit")
    @Operation(summary = "一次分配考勤公式参数edit")
    public R edit(@RequestBody FirstDistributionAccountFormulaParam firstDistributionAccountFormulaParam) {
        return R.ok(firstDistributionAccountFormulaParamService.updateById(firstDistributionAccountFormulaParam));
    }

    @SysLog("一次分配考勤公式参数del")
    @PostMapping("/del/{id}")
    @Operation(summary = "一次分配考勤公式参数del")
    public R del(@PathVariable Long id) {
        return R.ok(firstDistributionAccountFormulaParamService.removeById(id));
    }

    @SysLog("一次分配考勤公式参数delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "一次分配考勤公式参数delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(firstDistributionAccountFormulaParamService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @SysLog("一次分配考勤公式所有参数列表")
    @GetMapping("/listParam")
    @Operation(summary = "一次分配考勤公式所有参数列表")
    public R<List<FirstDistributionAccountFormulaParamDto>> listParam(PageRequest<FirstDistributionAccountFormulaParam> pr) {
        return R.ok(firstDistributionAccountFormulaParamService.listParam(pr));
    }
}