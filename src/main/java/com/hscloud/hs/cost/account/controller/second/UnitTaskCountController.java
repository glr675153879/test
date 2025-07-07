package com.hscloud.hs.cost.account.controller.second;

import cn.hutool.core.comparator.CompareUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.second.UnitTaskCountEditBatchDTO;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskCount;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskUser;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskCountVo;
import com.hscloud.hs.cost.account.model.vo.second.importXls.ImportResultVo;
import com.hscloud.hs.cost.account.service.second.IUnitTaskCountService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskUserService;
import com.hscloud.hs.cost.account.utils.ExcelUtil;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* 发放单元分配结果按人汇总
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/unitTaskCount")
@Tag(name = "unitTaskCount", description = "发放单元分配结果按人汇总")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class UnitTaskCountController {

    private final IUnitTaskCountService unitTaskCountService;
    private final IUnitTaskUserService unitTaskUserService;


    @SysLog("发放单元分配结果按人汇总info")
    @GetMapping("/info/{id}")
    @Operation(summary = "发放单元分配结果按人汇总info")
    public R<UnitTaskCount> info(@PathVariable Long id) {
        return R.ok(unitTaskCountService.getById(id));
    }

    @SysLog("发放单元分配结果按人汇总page")
    @GetMapping("/page")
    @Operation(summary = "发放单元分配结果按人汇总page")
    public R<IPage<UnitTaskCount>> page(PageRequest<UnitTaskCount> pr) {
        return R.ok(unitTaskCountService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("发放单元分配结果按人汇总list")
    @GetMapping("/list")
    @Operation(summary = "发放单元分配结果按人汇总list")
    public R<List<UnitTaskCount>> list(PageRequest<UnitTaskCount> pr) {
        List<UnitTaskCount> list = unitTaskCountService.list(pr.getWrapper());
        Long unitTaskId = Long.valueOf((String) pr.getQ().get("unitTaskId"));
        List<UnitTaskUser> unitTaskUsers = unitTaskUserService.listByTaskId(unitTaskId);
        Map<String, Float> collect = unitTaskUsers.stream().collect(Collectors.toMap(UnitTaskUser::getEmpCode, UnitTaskUser::getSortNum, (v1, v2) -> v1));
        list.sort((o1, o2) -> {
            Float sortNum1 = collect.get(o1.getEmpCode());
            Float sortNum2 = collect.get(o2.getEmpCode());
            return CompareUtil.compare(sortNum1, sortNum2);
        });
        return R.ok(list);
    }

    @SysLog("发放单元任务 按人汇总userList")
    @GetMapping("/userList")
    @Operation(summary = "发放单元任务 按人汇总userList")
    public R<List<UnitTaskCountVo>> userList(Long unitTaskId) {
        return R.ok(unitTaskCountService.userList(unitTaskId));
    }

    @SysLog("发放单元分配结果按人汇总add")
    @PostMapping("/add")
    @Operation(summary = "发放单元分配结果按人汇总add")
    public R add(@RequestBody UnitTaskCount unitTaskCount)  {
        return R.ok(unitTaskCountService.save(unitTaskCount));
    }

    @SysLog("发放单元分配结果按人汇总edit")
    @PostMapping("/edit")
    @Operation(summary = "发放单元分配结果按人汇总edit")
    public R edit(@RequestBody UnitTaskCount unitTaskCount)  {
        return R.ok(unitTaskCountService.updateById(unitTaskCount));
    }

    @SysLog("发放单元任务人员 editBatch")
    @PostMapping("/editBatch")
    @Operation(summary = "发放单元任务人员 editBatch")
    public R editBatch(@RequestBody @Validated  UnitTaskCountEditBatchDTO unitTaskCountEditBatchDTO)  {
        unitTaskCountService.editBatch(unitTaskCountEditBatchDTO);
        return R.ok();
    }

    @SysLog("发放单元分配结果按人汇总del")
    @PostMapping("/del/{id}")
    @Operation(summary = "发放单元分配结果按人汇总del")
    public R del(@PathVariable Long id)  {
        return R.ok(unitTaskCountService.removeById(id));
    }

    @SysLog("发放单元分配结果按人汇总delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "发放单元分配结果按人汇总delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(unitTaskCountService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }


    @GetMapping("/export")
    @Operation(summary = "线下统计导出")
    public void exportCount(Long unitTaskId, HttpServletResponse response) {
        unitTaskCountService.exportCount(unitTaskId,response);
    }

    @PostMapping("/importCount/{unitTaskId}")
    @Operation(summary = "线下统计导入")
    public R<ImportResultVo> importCount(@PathVariable Long unitTaskId, @RequestPart("file") MultipartFile file) throws Exception {
        String[][] xlsDataArr = ExcelUtil.doExcelH(file,1);
        return R.ok(unitTaskCountService.importCount(unitTaskId,xlsDataArr));
    }

}