package com.hscloud.hs.cost.account.controller.dataReport;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostClusterUnit;
import com.hscloud.hs.cost.account.model.vo.MappingBaseVO;
import com.hscloud.hs.cost.account.service.dataReport.ICostClusterUnitService;
import com.pig4cloud.pigx.admin.api.entity.mapping.MappingBase;
import com.pig4cloud.pigx.admin.api.feign.RemoteMappingBaseService;
import com.pig4cloud.pigx.admin.api.vo.mapping.InnerAllDataVO;
import com.pig4cloud.pigx.common.core.constant.SecurityConstants;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* 归集单元
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/dataReport/costClusterUnit")
@Tag(name = "costClusterUnit", description = "归集单元")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostClusterUnitController {

    private final ICostClusterUnitService costClusterUnitService;

    // private final RemoteThirdAccountUnitService remoteThirdAccountUnitService;

    private final RemoteMappingBaseService remoteMappingBaseService;
    
    @SysLog("归集单元info")
    @GetMapping("/info/{id}")
    @Operation(summary = "归集单元info")
    public R<CostClusterUnit> info(@PathVariable Long id) {
        return R.ok(costClusterUnitService.getById(id));
    }
    
    @SysLog("归集单元page")
    @GetMapping("/page")
    @Operation(summary = "归集单元page")
    public R<IPage<CostClusterUnit>> page(PageRequest<CostClusterUnit> pr) {
        return R.ok(costClusterUnitService.pageClusterUnit(pr.getPage(),pr.getWrapper()));
    }
    
    @SysLog("归集单元list")
    @GetMapping("/list")
    @Operation(summary = "归集单元list")
    public R<List<CostClusterUnit>> list(PageRequest<CostClusterUnit> pr) {
        return R.ok(costClusterUnitService.list(pr.getWrapper()));
    }


    @PreAuthorize("@pms.hasPermission('kpi_unit_imputation_add')")
    @SysLog("归集单元add")
    @PostMapping("/add")
    @Operation(summary = "归集单元add")
    public R add(@RequestBody CostClusterUnit costClusterUnit)  {
        return R.ok(costClusterUnitService.saveData(costClusterUnit));
    }

    @PreAuthorize("@pms.hasPermission('kpi_unit_imputation_edit')")
    @SysLog("归集单元edit")
    @PostMapping("/edit")
    @Operation(summary = "归集单元edit")
    public R edit(@RequestBody CostClusterUnit costClusterUnit)  {
        return R.ok(costClusterUnitService.editData(costClusterUnit));
    }

    @PreAuthorize("@pms.hasPermission('kpi_unit_imputation_del')")
    @SysLog("归集单元del")
    @PostMapping("/del/{id}")
    @Operation(summary = "归集单元del")
    public R del(@PathVariable Long id)  {
        return R.ok(costClusterUnitService.del(id));
    }
    
    @SysLog("归集单元delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "归集单元delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(costClusterUnitService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @SysLog("归集单元activate")
    @PostMapping("/activate")
    @Operation(summary = "归集单元activate")
    public R activate(@RequestBody CostClusterUnit costClusterUnit)  {
        return R.ok(costClusterUnitService.activate(costClusterUnit));
    }

    @SysLog("归集单元initiate")
    @PostMapping("/initiate")
    @Operation(summary = "归集单元initiate")
    public R initiate()  {
        return R.ok(costClusterUnitService.initiate());
    }

    @SysLog("科室映射page")
    @PostMapping("/cluster/unit/page")
    @Operation(summary = "科室映射page")
    public R<List<MappingBaseVO>> edit(@RequestParam(name = "attribute") String attribute,
                                       @RequestParam(name = "purpose") String purpose) {
        InnerAllDataVO data = remoteMappingBaseService.allData(attribute, purpose, SecurityConstants.FROM_IN).getData();
        List<MappingBase> mappingBaseList = data.getMappingBaseList();
        List<MappingBaseVO> mappingBaseVOList = new ArrayList<>();
        for (MappingBase mappingBase : mappingBaseList) {
            MappingBaseVO mappingBaseVO = new MappingBaseVO();
            BeanUtils.copyProperties(mappingBase, mappingBaseVO);
            Long unitId = mappingBase.getId();
            LambdaQueryWrapper<CostClusterUnit> qr = new LambdaQueryWrapper<>();
            qr.in(CostClusterUnit::getUnitList, unitId);
            List<CostClusterUnit> list2 = costClusterUnitService.list(qr);
            if (CollectionUtil.isNotEmpty(list2)) {
                mappingBaseVO.setIsUsed("1");
            } else {
                mappingBaseVO.setIsUsed("0");
            }
            mappingBaseVOList.add(mappingBaseVO);
        }
        return R.ok(mappingBaseVOList);
    }

    @SysLog("科室设为/取消归集单元")
    @GetMapping("/setClusterUnit")
    @Operation(summary = "科室设为/取消归集单元")
    public R setClusterUnit(@RequestParam("id") Long unitId)  {
        // return R.ok(costClusterUnitService.setClusterUnit(unitId));
        return R.ok();
    }

}