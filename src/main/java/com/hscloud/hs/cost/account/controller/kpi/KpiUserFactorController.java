/*
 *
 *      Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the hscloud.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: lengleng (wangiegie@gmail.com)
 *
 */

package com.hscloud.hs.cost.account.controller.kpi;

import cn.hutool.core.lang.tree.Tree;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserFactor;
import com.hscloud.hs.cost.account.model.vo.userAttendance.export.ImportErrVo;
import com.hscloud.hs.cost.account.service.kpi.KpiUserFactorService;
import com.hscloud.hs.cost.account.utils.kpi.excel.EasyExcelUtils;
import com.pig4cloud.pigx.common.core.constant.CacheConstants;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * <p>
 * 人员系数 前端控制器
 * </p>
 *
 * @author shiiic
 * @since 2019-03-19
 */
@RestController
@AllArgsConstructor
@RequestMapping("/kpi/factor")
@Tag(description = "人员系数", name = "factor")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiUserFactorController {

    private final KpiUserFactorService kpiUserFactorService;


    /**
     * 通过ID查询字典信息
     *
     * @param id ID
     * @return 字典信息
     */
    @GetMapping("/details/{id}")
    @Operation(summary = "详情")
    public R<KpiUserFactor> getById(@PathVariable Long id) {
        return R.ok(kpiUserFactorService.getById(id));
    }

//    /**
//     * 查询字典信息
//     *
//     * @param query 查询信息
//     * @return 字典信息
//     */
//    @GetMapping("/details")
//    public R getDetails(@ParameterObject KpiUserFactor query) {
//        return R.ok(kpiUserFactorService.getOne(Wrappers.query(query), false));
//    }

    /**
     * 分页查询人员系数
     *
     * @param searchDto
     * @return 分页对象
     */
    @GetMapping("/page")
    @Operation(summary = "列表分页表体")
    public R<IPage<JSONObject>> getFactorPage(KpiUserFactorSearchDto searchDto) {
        return R.ok(kpiUserFactorService.pageUserFactor(searchDto));
    }

    @GetMapping("/page2")
    @Operation(summary = "列表分页表体2")
    public R<List<JSONObject>> getFactorPage2(KpiUserFactorSearchDto searchDto) {
        return R.ok(kpiUserFactorService.getBodyList2(searchDto));
    }


    @GetMapping("/getTable")
    @Operation(summary = "动态列表整表")
    public R<KpiUserFactorPageDto> getTable(KpiUserFactorSearchDto searchDto) {
        return R.ok(kpiUserFactorService.getTable(searchDto));
    }

    @Operation(summary = "列表导出")
    @GetMapping(value = "/getTable_export")
    public void getJcq_export(KpiUserFactorSearchDto input, HttpServletResponse response) throws IOException {
        KpiUserFactorPageDto dto = kpiUserFactorService.getTable(input);
        //toIndex不包括
        List<KeyValueDTO> headList = dto.getHead_list().subList(5, dto.getHead_list().size());
        List<JSONObject> bodyList = dto.getBody_list();
        //移除头两个元素
        bodyList.forEach(item -> {
            item.remove("id");
            item.remove("deptId");
            item.remove("userId");
            item.remove("userStatus");
            item.remove("systemValueType");
        });
        List<String> list = Linq.of(headList).select(KeyValueDTO::getKey).toList();
        String[] dataStrMap = list.toArray(new String[list.size()]);
        List<String> list2 = Linq.of(headList).select(KeyValueDTO::getValue).toList();
        String[] headMap = list2.toArray(new String[list2.size()]);
        List<LinkedHashMap<String, Object>> listDatas = new ArrayList<LinkedHashMap<String, Object>>();
        for (int i = 0; i < bodyList.size(); i++) {
            LinkedHashMap<String, Object> o = JSONObject.parseObject(bodyList.get(i).toJSONString(), new TypeReference<LinkedHashMap<String, Object>>() {
            });
            listDatas.add(o);
        }
        NoModelWriteData d = new NoModelWriteData();
        d.setFileName("列表导出");
        d.setHeadMap(headMap);
        d.setDataStrMap(dataStrMap);
        d.setDataList(listDatas);
        EasyExcelUtils easyExcelUtils = new EasyExcelUtils();
        easyExcelUtils.noModleWrite(d, response);
    }

    @Operation(summary = "人员系数导入模板")
    @GetMapping(value = "/factor_export")
    public void rksModelExport(HttpServletResponse response) {
        try {
            //ExcelUtil.writeExcel(response, null, fileName, "人员系数导入模板", ExcelKeyUserFactorImportDto.class);
            kpiUserFactorService.exportFactorModelExcel(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Operation(summary = "人员系数导入 overwriteFlag 1整体覆盖，2部分覆盖")
    @PostMapping(value = "/factor_import")
    public R<ImportErrVo> importFile(@RequestParam("file") MultipartFile file, String overwriteFlag) throws Exception {
        if (file.isEmpty()) {
            return R.failed("请上传有效文件");
        } else {
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (suffix.equals(".xls") || suffix.equals(".xlsx")) {
                ImportErrVo importErrVo = kpiUserFactorService.importFile(file, overwriteFlag);
                return R.ok(importErrVo);
            } else {
                return R.failed("请上传Excel文件");
            }
        }
        //return R.ok(null, "导入成功,请2分钟后查看");
    }


    /**
     * 添加系数
     *
     * @param userFactor 字典信息
     * @return success、false
     */
    @SysLog("添加系数")
    @PostMapping("/save_factor")
    @Operation(summary = "添加系数")
    public R save(@Valid @RequestBody KpiUserFactorAddListDto userFactor) {
        kpiUserFactorService.saveFactor(userFactor);
        return R.ok();
    }

    /**
     * 删除该人员系数
     *
     * @param ids ID
     * @return R
     */
    @SysLog("删除该人员系数")
    @Operation(summary = "删除该人员系数")
    @PostMapping("del")
    @CacheEvict(value = CacheConstants.DICT_DETAILS, allEntries = true)
    public R<Boolean> removeById(@RequestBody Long[] ids) {
        return R.ok(kpiUserFactorService.removeByIds(Arrays.asList(ids)));
    }


    /**
     * 人员系数根据字典分组
     *
     * @param
     * @param
     * @return 人员系数根据字典分组
     */
    @GetMapping(value = "/tree")
    @SysLog("人员系数根据字典分组")
    @Operation(summary = "人员系数根据字典分组")
    public R<List<Tree<String>>> getTree(String dicType) {
        return R.ok(kpiUserFactorService.getTree(dicType));
    }


    @SysLog("添加科室系数")
    @PostMapping("/saveDeptfactor")
    @Operation(summary = "添加科室系数")
    public R saveDeptfactor(@Valid @RequestBody KpiUserDeptFactorAddDto userFactor) {
        kpiUserFactorService.saveDeptfactor(userFactor);
        return R.ok();
    }


    @GetMapping(value = "/deptValueList")
    @SysLog("科室系数列表")
    @Operation(summary = "科室系数列表")
    public R<List<KpiUserFactorDeptDto>> deptValueList(KpiUserFactorDeptSearchDto dto) {
        return R.ok(kpiUserFactorService.deptValueList(dto));
    }


}
