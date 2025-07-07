package com.hscloud.hs.cost.account.controller.second;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.constant.enums.SecondDistributionTaskStatusEnum;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.ProgProjectDetail;
import com.hscloud.hs.cost.account.model.entity.second.UnitTask;
import com.hscloud.hs.cost.account.service.second.IProgProjectDetailService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskProjectDetailService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskProjectService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskService;
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
import java.util.Objects;

/**
* 方案核算指标明细
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/progProjectDetail")
@Tag(name = "progProjectDetail", description = "方案核算指标明细")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ProgProjectDetailController {

    private final IProgProjectDetailService progProjectDetailService;
    private final IUnitTaskService unitTaskService;
    private final IUnitTaskProjectDetailService unitTaskProjectDetailService;

    @SysLog("方案核算指标明细info")
    @GetMapping("/info/{id}")
    @Operation(summary = "方案核算指标明细info")
    public R<ProgProjectDetail> info(@PathVariable Long id) {
        return R.ok(progProjectDetailService.getById(id));
    }

    @SysLog("方案核算指标明细page")
    @GetMapping("/page")
    @Operation(summary = "方案核算指标明细page")
    public R<IPage<ProgProjectDetail>> page(PageRequest<ProgProjectDetail> pr) {
        return R.ok(progProjectDetailService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("方案核算指标明细list")
    @GetMapping("/list")
    @Operation(summary = "方案核算指标明细list")
    public R<List<ProgProjectDetail>> list(Long unitTaskId,Long unitProjectId,PageRequest<ProgProjectDetail> pr) {
        if (unitTaskId != null && unitProjectId!= null){//已完成的 从任务中查
            UnitTask unitTask = unitTaskService.getById(unitTaskId);
            if (unitTask != null && Objects.equals(unitTask.getStatus(), SecondDistributionTaskStatusEnum.APPROVAL_APPROVED.getCode())){
                return R.ok(unitTaskProjectDetailService.getProgDetailList(unitTaskId,unitProjectId));
            }

        }
        return R.ok(progProjectDetailService.list(pr.getWrapper().lambda().orderByAsc(ProgProjectDetail::getSortNum, ProgProjectDetail::getId)));
    }

    @SysLog("方案核算指标明细add")
    @PostMapping("/add")
    @Operation(summary = "方案核算指标明细add")
    public R add(@RequestBody ProgProjectDetail progProjectDetail)  {
        return R.ok(progProjectDetailService.save(progProjectDetail));
    }

//    @SysLog("方案核算指标明细save")
//    @PostMapping("/save")
//    @Operation(summary = "方案核算指标明细save")
//    public R save(@RequestBody ProgProjectDetailSaveDTO progProjectDetailSaveDTO)  {
//        progProjectDetailService.saveByUnitTaskProject(progProjectDetailSaveDTO);
//        return R.ok();
//    }

    @SysLog("方案核算指标明细edit")
    @PostMapping("/edit")
    @Operation(summary = "方案核算指标明细edit")
    public R edit(@RequestBody ProgProjectDetail progProjectDetail)  {
        return R.ok(progProjectDetailService.updateById(progProjectDetail));
    }

    @SysLog("方案核算指标明细del")
    @PostMapping("/del/{id}")
    @Operation(summary = "方案核算指标明细del")
    public R del(@PathVariable Long id)  {
        return R.ok(progProjectDetailService.removeById(id));
    }

    @SysLog("方案核算指标明细delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "方案核算指标明细delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(progProjectDetailService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}