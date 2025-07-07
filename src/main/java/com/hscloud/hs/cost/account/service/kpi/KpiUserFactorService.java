/*
 *    Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the hscloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: lengleng (wangiegie@gmail.com)
 */
package com.hscloud.hs.cost.account.service.kpi;

import cn.hutool.core.lang.tree.Tree;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserFactor;
import com.hscloud.hs.cost.account.model.vo.userAttendance.export.ImportErrVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 字典表
 *
 * @author lengleng
 * @date 2019/03/19
 */
public interface KpiUserFactorService extends IService<KpiUserFactor> {


    void saveFactor(KpiUserFactorAddListDto userFactor);


    IPage<JSONObject> pageUserFactor(KpiUserFactorSearchDto searchDto);


    List<JSONObject> getBodyList2(KpiUserFactorSearchDto searchDto);


    KpiUserFactorPageDto getTable(KpiUserFactorSearchDto searchDto);

    List<Tree<String>> getTree(String dicType);


    ImportErrVo  importFile(MultipartFile uploadFile, String overwriteFlag);


    void exportFactorModelExcel(HttpServletResponse response) throws IOException;


    List<KpiUserFactorDeptDto> deptValueList(KpiUserFactorDeptSearchDto dto);

    void saveDeptfactor(KpiUserDeptFactorAddDto userFactorDto);
}
