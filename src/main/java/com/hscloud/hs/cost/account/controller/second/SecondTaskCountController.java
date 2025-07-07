package com.hscloud.hs.cost.account.controller.second;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.model.dto.second.SecondTaskCountExportDTO;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.SecondTaskCount;
import com.hscloud.hs.cost.account.model.vo.second.ProgrammeInfoVo;
import com.hscloud.hs.cost.account.model.vo.second.SecondDetailCountVo;
import com.hscloud.hs.cost.account.model.vo.second.count.PerformanceDetailsVO;
import com.hscloud.hs.cost.account.service.impl.second.PerformanceDetailsService;
import com.hscloud.hs.cost.account.service.second.ISecondTaskCountService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.excel.annotation.ResponseExcel;
import com.pig4cloud.pigx.common.excel.annotation.Sheet;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* 二次分配结果按人汇总
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/secondTaskCount")
@Tag(name = "secondTaskCount", description = "二次分配结果按人汇总")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class SecondTaskCountController {

    private final ISecondTaskCountService secondTaskCountService;
    private final PerformanceDetailsService performanceDetailsService;

    @SysLog("二次分配结果按人汇总info")
    @GetMapping("/info/{id}")
    @Operation(summary = "二次分配结果按人汇总info")
    public R<SecondTaskCount> info(@PathVariable Long id) {
        return R.ok(secondTaskCountService.getById(id));
    }

    @SysLog("二次分配结果按人汇总page")
    @GetMapping("/page")
    @Operation(summary = "二次分配结果按人汇总page")
    public R<IPage<SecondTaskCount>> page(PageRequest<SecondTaskCount> pr) {
        return R.ok(secondTaskCountService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("二次分配结果按人汇总list")
    @GetMapping("/list")
    @Operation(summary = "二次分配结果按人汇总list")
    public R<List<SecondTaskCount>> list(PageRequest<SecondTaskCount> pr) {
        return R.ok(secondTaskCountService.list(pr.getWrapper()));
    }

    @SysLog("二次分配结果按人汇总add")
    @PostMapping("/add")
    @Operation(summary = "二次分配结果按人汇总add")
    public R add(@RequestBody SecondTaskCount secondTaskCount)  {
        return R.ok(secondTaskCountService.save(secondTaskCount));
    }

    @SysLog("二次分配结果按人汇总edit")
    @PostMapping("/edit")
    @Operation(summary = "二次分配结果按人汇总edit")
    public R edit(@RequestBody SecondTaskCount secondTaskCount)  {
        return R.ok(secondTaskCountService.updateById(secondTaskCount));
    }

    @SysLog("二次分配结果按人汇总del")
    @PostMapping("/del/{id}")
    @Operation(summary = "二次分配结果按人汇总del")
    public R del(@PathVariable Long id)  {
        return R.ok(secondTaskCountService.removeById(id));
    }

    @SysLog("二次分配结果按人汇总delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "二次分配结果按人汇总delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(secondTaskCountService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @SysLog("分配结果明细 按人汇总userList")
    @GetMapping("/detailUserList")
    @Operation(summary = "分配结果明细 按人汇总userList")
    public R<List<SecondDetailCountVo>> detailUserList(Long secondTaskId,Long unitTaskId, String empCode) {
        return R.ok(secondTaskCountService.detailUserList(secondTaskId,unitTaskId,empCode));
    }

    @SysLog("分配结果明细 按人汇总detailTitleList")
    @GetMapping("/detailTitleList")
    @Operation(summary = "分配结果明细 按人汇总detailTitleList")
    public R<List<ProgrammeInfoVo>> detailTitleList(Long secondTaskId,Long unitTaskId, String empCode) {
        return R.ok(secondTaskCountService.detailTitleList(secondTaskId,unitTaskId,empCode));
    }

    @ResponseExcel(sheets = @Sheet(sheetName = "导出 总任务分配结果"))
    @GetMapping("/export")
    @Operation(summary = "导出 总任务分配结果")
    public List<SecondTaskCountExportDTO> export(Long id) {
        List<SecondTaskCountExportDTO> rtnLit = new ArrayList<>();
        List<SecondTaskCount> secondTaskCountList = secondTaskCountService.list(Wrappers.<SecondTaskCount>lambdaQuery().eq(SecondTaskCount::getSecondTaskId,id));
        for (SecondTaskCount secondTaskCount : secondTaskCountList){
            SecondTaskCountExportDTO dto = new SecondTaskCountExportDTO();
            dto.setEmpCode(secondTaskCount.getEmpCode());
            dto.setEmpName(secondTaskCount.getEmpName());
            dto.setGrantUnitNames(secondTaskCount.getGrantUnitNames());
            dto.setAmt(secondTaskCount.getAmt()==null?"":secondTaskCount.getAmt().setScale(2, BigDecimal.ROUND_HALF_UP)+"");
            rtnLit.add(dto);
        }
        return rtnLit;
    }

    @SysLog("绩效明细")
    @GetMapping("/performanceDetails")
    @Operation(summary = "绩效明细")
    public R<PerformanceDetailsVO> performanceDetails(Long secondTaskId, Long unitTaskId, String empCode) {
        return R.ok(performanceDetailsService.performanceDetails(secondTaskId, unitTaskId));
    }

    @SysLog("二次分配结果按人汇总add")
    @GetMapping("/patch")
    public R patch(Long secondTaskId, Long unitTaskId)  {
        if(unitTaskId == null) {
            secondTaskCountService.doCountAll(secondTaskId);
        }else{
            secondTaskCountService.doCount(secondTaskId,unitTaskId);
        }
        return R.ok();
    }

}