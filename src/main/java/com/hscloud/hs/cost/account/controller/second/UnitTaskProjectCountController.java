package com.hscloud.hs.cost.account.controller.second;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProject;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProjectCount;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskProjectCountVo;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskProjectDetailVo;
import com.hscloud.hs.cost.account.service.second.IUnitTaskProjectCountService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskProjectService;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* 核算指标分配结果按人汇总
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/unitTaskProjectCount")
@Tag(name = "unitTaskProjectCount", description = "核算指标分配结果按人汇总")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class UnitTaskProjectCountController {

    private final IUnitTaskProjectCountService unitTaskProjectCountService;
    private final IUnitTaskProjectService unitTaskProjectService;

    @SysLog("核算指标分配结果按人汇总info")
    @GetMapping("/info/{id}")
    @Operation(summary = "核算指标分配结果按人汇总info")
    public R<UnitTaskProjectCount> info(@PathVariable Long id) {
        return R.ok(unitTaskProjectCountService.getById(id));
    }

    @SysLog("核算指标分配结果按人汇总page")
    @GetMapping("/page")
    @Operation(summary = "核算指标分配结果按人汇总page")
    public R<IPage<UnitTaskProjectCount>> page(PageRequest<UnitTaskProjectCount> pr) {
        return R.ok(unitTaskProjectCountService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("核算指标分配结果按人汇总userList")
    @GetMapping("/userList")
    @Operation(summary = "核算指标分配结果按人汇总userList")
    public R<List<UnitTaskProjectCountVo>> userList(Long unitTaskId) {
        return R.ok(unitTaskProjectCountService.userList(unitTaskId));
    }

    @SysLog("核算指标分配结果按人汇总list")
    @GetMapping("/list")
    @Operation(summary = "核算指标分配结果按人汇总list")
    public R<List<UnitTaskProjectCount>> list(PageRequest<UnitTaskProjectCount> pr) {
        List<UnitTaskProjectCount> list = unitTaskProjectCountService.list(pr.getWrapper().orderByAsc("sort_num"));
        List<Long> projectIds = list.stream().map(UnitTaskProjectCount::getProjectId).collect(Collectors.toList());
        if(!projectIds.isEmpty()){
            List<UnitTaskProject> projectList = unitTaskProjectService.list(Wrappers.<UnitTaskProject>lambdaQuery().in(UnitTaskProject::getId,projectIds));
            Map<Long,UnitTaskProject> projectMap = projectList.stream().collect(Collectors.toMap(UnitTaskProject::getId, item->item,(k1,k2)->k1));
            for (UnitTaskProjectCount projectCount : list){
                Long projectId = projectCount.getProjectId();
                UnitTaskProject project = projectMap.get(projectId);
                if(project != null){
                    int scale = project.getReservedDecimal();
                    RoundingMode roundingMode = CommonUtils.getCarryRule(project.getCarryRule());
                    BigDecimal amt = projectCount.getAmt();
                    amt = amt.setScale(scale,roundingMode);
                    projectCount.setSegment(amt+"");
                }
            }
        }
        return R.ok(list);
    }

    @SysLog("核算指标分配结果按人汇总add")
    @PostMapping("/add")
    @Operation(summary = "核算指标分配结果按人汇总add")
    public R add(@RequestBody UnitTaskProjectCount unitTaskProjectCount)  {
        return R.ok(unitTaskProjectCountService.save(unitTaskProjectCount));
    }

    @SysLog("核算指标分配结果按人汇总edit")
    @PostMapping("/edit")
    @Operation(summary = "核算指标分配结果按人汇总edit")
    public R edit(@RequestBody UnitTaskProjectCount unitTaskProjectCount)  {
        return R.ok(unitTaskProjectCountService.updateById(unitTaskProjectCount));
    }

    @SysLog("核算指标分配结果按人汇总del")
    @PostMapping("/del/{id}")
    @Operation(summary = "核算指标分配结果按人汇总del")
    public R del(@PathVariable Long id)  {
        return R.ok(unitTaskProjectCountService.removeById(id));
    }

    @SysLog("核算指标分配结果按人汇总delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "核算指标分配结果按人汇总delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(unitTaskProjectCountService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}