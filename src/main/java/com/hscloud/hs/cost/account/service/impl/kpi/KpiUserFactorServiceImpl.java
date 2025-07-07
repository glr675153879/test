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
package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bestvike.linq.Linq;
import com.google.common.collect.ImmutableList;
import com.hscloud.hs.cost.account.constant.enums.kpi.UserFactorCodeEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiDictItemMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiDictMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiUserFactorMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDict;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDictItem;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserFactor;
import com.hscloud.hs.cost.account.model.vo.userAttendance.export.ImportErrListVO;
import com.hscloud.hs.cost.account.model.vo.userAttendance.export.ImportErrVo;
import com.hscloud.hs.cost.account.service.kpi.KpiUserFactorService;
import com.hscloud.hs.cost.account.utils.kpi.TreeUtilExtend;
import com.hscloud.hs.cost.account.utils.kpi.excel.CommonUtil;
import com.hscloud.hs.cost.account.utils.kpi.excel.EasyExcelUtils;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.javers.common.collections.Arrays;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 字典表
 *
 * @author lengleng
 * @date 2019/03/19
 */
@Service
@AllArgsConstructor
public class KpiUserFactorServiceImpl extends ServiceImpl<KpiUserFactorMapper, KpiUserFactor> implements KpiUserFactorService {


    private final KpiUserFactorMapper kpiUserFactorMapper;


    private final KpiDictItemMapper kpiItemMapper;

    private final KpiDictMapper kpiDictMapper;

    @NotNull
    private static List<KeyValueDTO> getTitleNameList(List<KpiDict> dic) {
        List<KeyValueDTO> headTitleList = new ArrayList<>();
        for (int i = 0; i < dic.size(); i++) {
            KpiDict kpiDict = dic.get(i);
            KeyValueDTO origin = new KeyValueDTO();
            origin.setKey(kpiDict.getDictType());
            origin.setValue(kpiDict.getDescription());
            headTitleList.add(origin);
            //headTitleList.add(kpiDict.getDescription());
            if (kpiDict.getPerformanceSubsidy().equals("1")) {
                KeyValueDTO subsidy = new KeyValueDTO();
                //配置了绩效补贴
                subsidy.setKey(kpiDict.getDictType() + "_subsidy");
                subsidy.setValue(kpiDict.getDescription() + "补贴");
                headTitleList.add(subsidy);
            }
            if (kpiDict.getPersonnelFactor().equals("1")) {
                KeyValueDTO factor = new KeyValueDTO();
                //配置了人员系数
                factor.setKey(kpiDict.getDictType() + "_factor");
                factor.setValue(kpiDict.getDescription() + "系数");
                headTitleList.add(factor);

            }
        }
        return headTitleList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFactor(KpiUserFactorAddListDto userFactorDto) {
        if (userFactorDto.getUserId() == null) {
            return;
        }
        //根据userId 加人
        kpiUserFactorMapper.delete(new LambdaQueryWrapper<KpiUserFactor>()
                .eq(KpiUserFactor::getUserId, userFactorDto.getUserId())
                .eq(KpiUserFactor::getType, UserFactorCodeEnum.USER.getCode()));
        KpiUserFactor kpiUserFactor = new KpiUserFactor();
        kpiUserFactor.setUserId(userFactorDto.getUserId());
        kpiUserFactor.setType(UserFactorCodeEnum.USER.getCode());
        kpiUserFactor.setCreateTime(new Date());
        kpiUserFactor.insert();
        //对其他的进行处理
        List<KpiUserFactorAddDto> list = userFactorDto.getList();
        if (CollectionUtils.isNotEmpty(list)) {
            for (KpiUserFactorAddDto kpiUserFactorAddDto : list) {
                if (CollectionUtils.isNotEmpty(kpiUserFactorAddDto.getItemCodes())) {
                    //修改对应二级字典
                    kpiUserFactorMapper.delete(new LambdaQueryWrapper<KpiUserFactor>()
                            .eq(KpiUserFactor::getUserId, userFactorDto.getUserId())
                            .eq(KpiUserFactor::getType, UserFactorCodeEnum.OFFICE.getCode())
                            .eq(KpiUserFactor::getDictType, kpiUserFactorAddDto.getDictType()));
                    for (String item_code : kpiUserFactorAddDto.getItemCodes()) {
                        KpiUserFactor user = new KpiUserFactor();
                        user.setUserId(userFactorDto.getUserId());
                        user.setDictType(kpiUserFactorAddDto.getDictType());
                        user.setType(UserFactorCodeEnum.OFFICE.getCode());
                        user.setCreateTime(new Date());
                        user.setItemCode(item_code);
                        user.insert();
                    }
                } else {
                    //修改对应二级字典
                    kpiUserFactorMapper.delete(new LambdaQueryWrapper<KpiUserFactor>()
                            .eq(KpiUserFactor::getUserId, userFactorDto.getUserId())
                            .eq(KpiUserFactor::getType, UserFactorCodeEnum.OFFICE.getCode())
                            .eq(KpiUserFactor::getDictType, kpiUserFactorAddDto.getDictType()));
                }
                if (kpiUserFactorAddDto.getFactorValue() != null) {
                    //修改系数
                    kpiUserFactorMapper.delete(new LambdaQueryWrapper<KpiUserFactor>()
                            .eq(KpiUserFactor::getUserId, userFactorDto.getUserId())
                            .eq(KpiUserFactor::getType, UserFactorCodeEnum.COEFFICIENT.getCode())
                            .eq(KpiUserFactor::getDictType, kpiUserFactorAddDto.getDictType()));
                    KpiUserFactor user = new KpiUserFactor();
                    user.setUserId(userFactorDto.getUserId());
                    user.setType(UserFactorCodeEnum.COEFFICIENT.getCode());
                    user.setDictType(kpiUserFactorAddDto.getDictType());
                    user.setCreateTime(new Date());
                    user.setValue(kpiUserFactorAddDto.getFactorValue());
                    user.insert();
                } else {
                    kpiUserFactorMapper.delete(new LambdaQueryWrapper<KpiUserFactor>()
                            .eq(KpiUserFactor::getUserId, userFactorDto.getUserId())
                            .eq(KpiUserFactor::getType, UserFactorCodeEnum.COEFFICIENT.getCode())
                            .eq(KpiUserFactor::getDictType, kpiUserFactorAddDto.getDictType()));
                }
                if (kpiUserFactorAddDto.getSubsidyValue() != null) {
                    //修改系数
                    kpiUserFactorMapper.delete(new LambdaQueryWrapper<KpiUserFactor>()
                            .eq(KpiUserFactor::getUserId, userFactorDto.getUserId())
                            .eq(KpiUserFactor::getType, UserFactorCodeEnum.SUBSIDY.getCode())
                            .eq(KpiUserFactor::getDictType, kpiUserFactorAddDto.getDictType()));
                    KpiUserFactor user = new KpiUserFactor();
                    user.setUserId(userFactorDto.getUserId());
                    user.setType(UserFactorCodeEnum.SUBSIDY.getCode());
                    user.setCreateTime(new Date());
                    user.setDictType(kpiUserFactorAddDto.getDictType());
                    user.setValue(kpiUserFactorAddDto.getSubsidyValue());
                    user.insert();
                } else {
                    kpiUserFactorMapper.delete(new LambdaQueryWrapper<KpiUserFactor>()
                            .eq(KpiUserFactor::getUserId, userFactorDto.getUserId())
                            .eq(KpiUserFactor::getType, UserFactorCodeEnum.SUBSIDY.getCode())
                            .eq(KpiUserFactor::getDictType, kpiUserFactorAddDto.getDictType()));
                }
            }
        }
//        Map<String, List<KpiUserFactorAddDto>> bankCardMap = list.stream().collect(Collectors.groupingBy(KpiUserFactorAddDto::getType));
//        for (Map.Entry<String, List<KpiUserFactorAddDto>> entry : bankCardMap.entrySet()) {
//            List<KpiUserFactorAddDto> value = entry.getValue();
//            if (CollectionUtils.isNotEmpty(value)) {
//                if (value.get(0).getType().equals("office")) {
//                    //针对office的修改
//                    kpiUserFactorMapper.delete(new LambdaQueryWrapper<KpiUserFactor>()
//                            .eq(KpiUserFactor::getDelFlag, "0")
//                            .eq(KpiUserFactor::getUserId, value.get(0).getUserId())
//                            .eq(KpiUserFactor::getType, value.get(0).getType())
//                            .eq(KpiUserFactor::getDictType, value.get(0).getDictType()));
//                } else {
//                    //补贴和系数
//                    kpiUserFactorMapper.delete(new LambdaQueryWrapper<KpiUserFactor>()
//                            .eq(KpiUserFactor::getDelFlag, "0")
//                            .eq(KpiUserFactor::getUserId, value.get(0).getUserId())
//                            .eq(KpiUserFactor::getType, value.get(0).getType())
//                            .eq(value.get(0).getDeptId() != null, KpiUserFactor::getDeptId, value.get(0).getDeptId())
//                            .eq(KpiUserFactor::getDictType, value.get(0).getDictType()));
//                }
//                for (KpiUserFactorAddDto kpiUserFactorAddDto : value) {
//                    KpiUserFactor kpiUserFactor = new KpiUserFactor();
//                    BeanUtils.copyProperties(kpiUserFactorAddDto, kpiUserFactor);
//                    kpiUserFactor.setUserId(Long.valueOf(kpiUserFactorAddDto.getUserId()));
//                    kpiUserFactor.setCreateTime(new Date());
//                    kpiUserFactor.insert();
//                }
//            }

    }

    /**
     * 赋值表头
     *
     * @param list
     * @return
     */
    private List<KeyValueDTO> processTableHeadList(List<KeyValueDTO> list) {
        List<KeyValueDTO> ret = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                KeyValueDTO keyValueDTO = new KeyValueDTO();
                keyValueDTO.setKey(list.get(i).getKey());
                keyValueDTO.setValue(list.get(i).getValue());
                ret.add(keyValueDTO);
            }
        }
        return ret;
    }

    //表头
    private List<KeyValueDTO> getHeadTitleList() {
        //表头多出来的列
        List<KpiDict> dic = kpiUserFactorMapper.getDic();
        List<KeyValueDTO> headTitleList = getTitleNameList(dic);
        //得到表头
        List<KeyValueDTO> keyValueDTOS = new ArrayList<>();
        keyValueDTOS.add(new KeyValueDTO().setKey("id").setValue("id"));
        keyValueDTOS.add(new KeyValueDTO().setKey("deptId").setValue("科室单元id"));
        keyValueDTOS.add(new KeyValueDTO().setKey("userId").setValue("用户id"));
        keyValueDTOS.add(new KeyValueDTO().setKey("userStatus").setValue("用户启用状态 0启用"));
        keyValueDTOS.add(new KeyValueDTO().setKey("systemValueType").setValue("系统数值type集合"));
        keyValueDTOS.add(new KeyValueDTO().setKey("deptName").setValue("科室单元名称"));
        keyValueDTOS.add(new KeyValueDTO().setKey("name").setValue("姓名"));
        keyValueDTOS.addAll(this.processTableHeadList(headTitleList));
        return keyValueDTOS;
    }

    public boolean areAllElementsEmptyApache(List<BigDecimal> list) {
        return CollectionUtils.isEmpty(list) || list.stream().allMatch(Objects::isNull);
    }


    //表体
    private List<JSONObject> getBodyList(KpiUserFactorSearchDto searchDto, List<KeyValueDTO> headTitleList) {
        //List<KpiUserFactor> list = kpiUserFactorMapper.getList(searchDto);
        //表体初始化
        List<JSONObject> retjson = new ArrayList<>();
        //所有的数据
        List<KpiUserFactor> list = kpiUserFactorMapper.selectList(new LambdaQueryWrapper<KpiUserFactor>()
                .eq(KpiUserFactor::getDelFlag, "0")
                .eq(searchDto != null && StringUtils.isNotBlank(searchDto.getItemCode()), KpiUserFactor::getItemCode, searchDto.getItemCode()));
        //字典项表
        List<KpiDictItem> kpiDictItems = kpiItemMapper.selectList(new LambdaQueryWrapper<KpiDictItem>().eq(KpiDictItem::getDelFlag, "0").eq(KpiDictItem::getStatus, "0"));
        //职务等一级字典名称
        List<KpiUserFactor> office_list = Linq.of(list).where(t -> t.getType().equals("office")).toList();
        //系数字段
        List<KpiUserFactor> coefficient_list = Linq.of(list).where(t -> t.getType().equals("coefficient")).toList();
        //补贴字段
        List<KpiUserFactor> subsidy_list = Linq.of(list).where(t -> t.getType().equals("subsidy")).toList();
        //先拼接出人员科室 type = user
        List<KpiUserFactorBeforeDto> userDept = kpiUserFactorMapper.getUserDept(searchDto);
        List<String> systemValueType = new ArrayList<>();
        for (KpiUserFactorBeforeDto item : userDept) {
            JSONObject jsonObject = new JSONObject(new LinkedHashMap<>());
            jsonObject.put("id", item.getId());
            jsonObject.put("deptId", item.getDeptId());
            jsonObject.put("userId", item.getUserId());
            jsonObject.put("userStatus", item.getUserStatus());
            jsonObject.put("systemValueType", systemValueType);
            jsonObject.put("deptName", item.getDeptName());
            jsonObject.put("name", item.getName());
            for (int j = 7; j < headTitleList.size(); j++) {
                //开始遍历 拿到key名称
                String key = headTitleList.get(j).getKey();
                if (key.contains("_subsidy")) {
                    //补贴
                    List<KpiUserFactor> subsidy = Linq.of(subsidy_list).where(t -> t.getUserId().equals(item.getUserId())
                            && t.getDictType().equals(key.split("_")[0])).toList();
                    if (CollectionUtils.isNotEmpty(subsidy)) {
                        subsidy.sort(Comparator.comparing(KpiUserFactor::getValue).reversed());
                        //找到最大值
                        jsonObject.put(key, subsidy.get(0).getValue());
                    } else {
                        //找到该dicItem的补贴值
                        List<KpiUserFactor> office = Linq.of(office_list).where(t -> t.getUserId().equals(item.getUserId())
                                && t.getDictType().equals(key.split("_")[0])).toList();
                        if (CollectionUtils.isNotEmpty(office)) {
                            List<String> item_codes = Linq.of(office).select(KpiUserFactor::getItemCode).toList();
                            //一个人可能有多个职务默认
                            List<BigDecimal> performanceSubsidyValue = Linq.of(kpiDictItems).where(t -> t.getDictType().equals(office.get(0).getDictType())
                                            && item_codes.contains(t.getItemCode())).select(KpiDictItem::getPerformanceSubsidyValue)
                                    .toList();
                            //找到list的最大值
                            if (!areAllElementsEmptyApache(performanceSubsidyValue)) {
                                BigDecimal max = Collections.max(performanceSubsidyValue);
                                jsonObject.put(key, max);
                            } else {
                                jsonObject.put(key, "");
                            }
                        } else {
                            jsonObject.put(key, "");
                        }
                        //系统数值集合添加key
                        systemValueType.add(key);
                    }
                } else if (key.contains("_factor")) {
                    //系数
                    List<KpiUserFactor> coefficient = Linq.of(coefficient_list).where(t -> t.getUserId().equals(item.getUserId())
                            && t.getDictType().equals(key.split("_")[0])).toList();
                    if (CollectionUtils.isNotEmpty(coefficient)) {
                        coefficient.sort(Comparator.comparing(KpiUserFactor::getValue).reversed());
                        //找是否有科室综合系数
                        List<KpiUserFactor> deptId = Linq.of(coefficient).where(t -> t.getDeptId() != null).toList();
                        if (CollectionUtils.isNotEmpty(deptId)) {
                            //科室综合系数取当条
                            KpiUserFactor kpiUserFactor = Linq.of(deptId).where(t -> t.getDeptId().equals(item.getUnitId())).firstOrDefault();
                            if (kpiUserFactor != null) {
                                jsonObject.put(key, kpiUserFactor.getValue());
                            } else {
                                jsonObject.put(key, "");
                            }
                        } else {
                            //普通的取最大的
                            jsonObject.put(key, coefficient.get(0).getValue());
                        }
                    } else {
                        //找到该dicItem的系数值
                        List<KpiUserFactor> office = Linq.of(office_list).where(t -> t.getUserId().equals(item.getUserId())
                                && t.getDictType().equals(key.split("_")[0])).toList();
                        if (CollectionUtils.isNotEmpty(office)) {
                            List<String> item_codes = Linq.of(office).select(KpiUserFactor::getItemCode).toList();
                            List<BigDecimal> personnelFactoryValue = Linq.of(kpiDictItems).where(t -> t.getDictType().equals(office.get(0).getDictType())
                                    && item_codes.contains(t.getItemCode())).select(KpiDictItem::getPersonnelFactorValue).toList();
                            //找到list的最大值
                            if (!areAllElementsEmptyApache(personnelFactoryValue)) {
                                BigDecimal max = Collections.max(personnelFactoryValue);
                                jsonObject.put(key, max);
                            } else {
                                jsonObject.put(key, "");
                            }
                        } else {
                            jsonObject.put(key, "");
                        }
                        //系统数值集合添加key
                        systemValueType.add(key);
                    }
                } else {
                    //普通一级字典 职务等 可能有多条
                    List<KpiUserFactor> office = Linq.of(office_list).where(t -> t.getUserId().equals(item.getUserId())
                            && t.getDictType().equals(key.split("_")[0])).toList();
                    if (CollectionUtils.isNotEmpty(office)) {
                        List<String> split = Linq.of(office).select(KpiUserFactor::getItemCode).toList();
                        //赋对应字典中文值
                        List<String> description = Linq.of(kpiDictItems).where(t -> t.getDictType().equals(office.get(0).getDictType())
                                && split.contains(t.getItemCode())).select(KpiDictItem::getLabel).toList();
                        jsonObject.put(key, String.join(",", description));
                    } else {
                        jsonObject.put(key, "");
                    }
                }
            }
            jsonObject.put("systemValueType", systemValueType.stream().distinct().collect(Collectors.toList()));
            retjson.add(jsonObject);
        }
        //做筛选
        if (searchDto != null) {
            if (StringUtils.isNotBlank(searchDto.getDeptName())) {
                retjson = Linq.of(retjson).where(t -> t.get("deptName").toString().contains(searchDto.getDeptName())).toList();
            }
            if (StringUtils.isNotBlank(searchDto.getName())) {
                retjson = Linq.of(retjson).where(t -> t.get("name").toString().contains(searchDto.getName())).toList();
            }
            if (StringUtils.isNotBlank(searchDto.getZwName())) {
                retjson = Linq.of(retjson).where(t -> t.get(searchDto.getDictType()).toString().contains(searchDto.getZwName())).toList();
            }
        }
        //按科室名称排序
        retjson = Linq.of(retjson).orderBy(t -> t.get("deptName")).toList();
        return retjson;
    }


    /**
     * 得到列表所有开启的字典key
     *
     * @return
     */
    public List<KpiUserFactorTableDto> getEnableDicKey() {
        List<KpiUserFactorTableDto> key = new ArrayList<>();
        //启用的字典
        List<KpiDictItemOutDto> kpiDicts = kpiUserFactorMapper.getDictKey();
        Map<String, List<KpiDictItemOutDto>> dic = kpiDicts.stream().collect(Collectors.groupingBy(KpiDictItemOutDto::getDictType));
        for (Map.Entry<String, List<KpiDictItemOutDto>> entry : dic.entrySet()) {
            List<KpiDictItemOutDto> value = entry.getValue();
            KpiUserFactorTableDto dto = new KpiUserFactorTableDto();
            dto.setDictType(entry.getKey());
            if (value.get(0).getPerformanceSubsidy().equals("1")) {
                //配置了绩效补贴
                ValueOrSystem subsidyValue = new ValueOrSystem();
//                //先取默认值
//                subsidyValue.setIsSystem("0");
//                List<BigDecimal> performanceSubsidyValue = Linq.of(value).where(t -> t.getPerformanceSubsidyValue() != null)
//                        .select(KpiDictItemOutDto::getPerformanceSubsidyValue).toList();
//                if (CollectionUtils.isNotEmpty(performanceSubsidyValue)) {
//                    BigDecimal max = Collections.max(performanceSubsidyValue);
//                    subsidyValue.setValue(max);
//                }
                dto.setSubsidyValue(subsidyValue);
            }
            if (value.get(0).getPersonnelFactor().equals("1")) {
                ValueOrSystem factorValue = new ValueOrSystem();
//                //先取默认值
//                factorValue.setIsSystem("0");
//                List<BigDecimal> personnelFactorValue = Linq.of(value).where(t -> t.getPersonnelFactorValue() != null).select(KpiDictItemOutDto::getPersonnelFactorValue).toList();
//                if (CollectionUtils.isNotEmpty(personnelFactorValue)) {
//                    BigDecimal max = Collections.max(personnelFactorValue);
//                    factorValue.setValue(max);
//                }
                dto.setFactorValue(factorValue);
            }
            key.add(dto);
        }
        return key;
    }


    //表体
    public List<JSONObject> getBodyList2(KpiUserFactorSearchDto searchDto) {
        //List<KpiUserFactor> list = kpiUserFactorMapper.getList(searchDto);
        //表体初始化
        List<JSONObject> retjson = new ArrayList<>();
        //所有的数据
        List<KpiUserFactor> list = kpiUserFactorMapper.selectList(new LambdaQueryWrapper<KpiUserFactor>()
                .eq(KpiUserFactor::getDelFlag, "0")
                .eq(searchDto != null && StringUtils.isNotBlank(searchDto.getItemCode()), KpiUserFactor::getItemCode, searchDto.getItemCode()));
        list = Linq.of(list).where(t -> t.getDeptId() == null).toList();
        //字典项表
        List<KpiDictItem> kpiDictItems = kpiItemMapper.selectList(new LambdaQueryWrapper<KpiDictItem>().eq(KpiDictItem::getDelFlag, "0").eq(KpiDictItem::getStatus, "0"));
        //职务等一级字典名称
        List<KpiUserFactor> office_list = Linq.of(list).where(t -> t.getType().equals("office")).toList();
        //系数字段
        List<KpiUserFactor> coefficient_list = Linq.of(list).where(t -> t.getType().equals("coefficient")).toList();
        //补贴字段
        List<KpiUserFactor> subsidy_list = Linq.of(list).where(t -> t.getType().equals("subsidy")).toList();
        //先拼接出人员科室 type = user
        List<KpiUserFactorBeforeDto> userDept = kpiUserFactorMapper.getUserDept(searchDto);
        List<KpiUserFactorBeforeDto> after = new ArrayList<>();
        Map<Long, List<KpiUserFactorBeforeDto>> before = userDept.stream().collect(Collectors.groupingBy(KpiUserFactorBeforeDto::getUserId));
        for (Map.Entry<Long, List<KpiUserFactorBeforeDto>> entry : before.entrySet()) {
            List<KpiUserFactorBeforeDto> value = entry.getValue();
            KpiUserFactorBeforeDto dto = new KpiUserFactorBeforeDto();
            BeanUtils.copyProperties(value.get(0), dto);
            //多科室综合一条
//            List<String> deptIds = Linq.of(value).select(KpiUserFactorBeforeDto::getDeptId).toList();
//            dto.setDeptId(String.join(",", deptIds));
            List<String> deptNames = Linq.of(value).where(t -> StringUtils.isNotBlank(t.getDeptName()))
                    .select(KpiUserFactorBeforeDto::getDeptName).distinct().toList();
            dto.setDeptName(String.join(",", deptNames));
            after.add(dto);
        }
        userDept = after;
        List<KpiUserFactorTableDto> basicDicKey = getEnableDicKey();
        for (KpiUserFactorBeforeDto item : userDept) {
            List<KpiUserFactorTableDto> finalList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(new LinkedHashMap<>());
            jsonObject.put("id", item.getId());
            jsonObject.put("deptId", item.getDeptId());
            jsonObject.put("userId", item.getUserId());
            jsonObject.put("userStatus", item.getUserStatus());
            jsonObject.put("deptName", item.getDeptName());
            jsonObject.put("name", item.getName());
            for (int j = 0; j < basicDicKey.size(); j++) {
                List<KpiDicItemDto> itemCode = new ArrayList<>();
                //开始遍历 拿到key名称
                KpiUserFactorTableDto key = basicDicKey.get(j);
                KpiUserFactorTableDto one = new KpiUserFactorTableDto();
                BeanUtils.copyProperties(key, one);
                if (key.getSubsidyValue() != null) {
                    //补贴
                    List<KpiUserFactor> subsidy = Linq.of(subsidy_list).where(t -> t.getUserId().equals(item.getUserId())
                            && t.getDictType().equals(key.getDictType())).toList();
                    if (CollectionUtils.isNotEmpty(subsidy)) {
                        subsidy.sort(Comparator.comparing(KpiUserFactor::getValue).reversed());
                        //找到最大值
                        ValueOrSystem subsidyValue = new ValueOrSystem(subsidy.get(0).getValue(), "1");
                        one.setSubsidyValue(subsidyValue);
                    } else {
                        //找到该dicItem的补贴值
                        List<KpiUserFactor> office = Linq.of(office_list).where(t -> t.getUserId().equals(item.getUserId())
                                && t.getDictType().equals(key.getDictType())).toList();
                        if (CollectionUtils.isNotEmpty(office)) {
                            List<String> item_codes = Linq.of(office).select(KpiUserFactor::getItemCode).toList();
                            //一个人可能有多个职务默认
                            List<BigDecimal> performanceSubsidyValue = Linq.of(kpiDictItems).where(t -> t.getDictType().equals(office.get(0).getDictType())
                                            && item_codes.contains(t.getItemCode()) && t.getPerformanceSubsidyValue() != null).select(KpiDictItem::getPerformanceSubsidyValue)
                                    .toList();
                            //找到list的最大值
                            if (!areAllElementsEmptyApache(performanceSubsidyValue)) {
                                BigDecimal max = Collections.max(performanceSubsidyValue);
                                ValueOrSystem subsidyValue = new ValueOrSystem(max, "0");
                                one.setSubsidyValue(subsidyValue);
                            }
                        } else {
                            //没字典值
                            ValueOrSystem subsidyValue = new ValueOrSystem();
                            one.setSubsidyValue(subsidyValue);
                        }
                        //系统数值集合添加key
                        //systemValueType.add(key);
                    }
                }
                if (key.getFactorValue() != null) {
                    //系数
                    List<KpiUserFactor> coefficient = Linq.of(coefficient_list).where(t -> t.getUserId().equals(item.getUserId())
                            && t.getDictType().equals(key.getDictType())).toList();
                    if (CollectionUtils.isNotEmpty(coefficient)) {
                        //科室系数去除
                        coefficient = Linq.of(coefficient).where(t -> t.getDeptId() == null).toList();
                        coefficient.sort(Comparator.comparing(KpiUserFactor::getValue).reversed());
//                        //找是否有科室综合系数
//                        List<KpiUserFactor> deptId = Linq.of(coefficient).where(t -> t.getDeptId() != null).toList();
//                        if (CollectionUtils.isNotEmpty(deptId)) {
//                            //科室综合系数取当条
//                            KpiUserFactor kpiUserFactor = Linq.of(deptId).where(t -> t.getDeptId().equals(item.getUnitId())).firstOrDefault();
//                            if (kpiUserFactor != null) {
//                                //找到最大值
//                                ValueOrSystem factorValue = new ValueOrSystem(kpiUserFactor.getValue(), "1");
//                                one.setFactorValue(factorValue);
//                            }
//                        } else {
                        //普通的取最大的
                        ValueOrSystem factorValue = new ValueOrSystem(coefficient.get(0).getValue(), "1");
                        one.setFactorValue(factorValue);
                        // }
                    } else {
                        //找到该dicItem的系数值
                        List<KpiUserFactor> office = Linq.of(office_list).where(t -> t.getUserId().equals(item.getUserId())
                                && t.getDictType().equals(key.getDictType())).toList();
                        if (CollectionUtils.isNotEmpty(office)) {
                            List<String> item_codes = Linq.of(office).select(KpiUserFactor::getItemCode).toList();
                            List<BigDecimal> personnelFactoryValue = Linq.of(kpiDictItems)
                                    .where(t -> t.getDictType().equals(office.get(0).getDictType())
                                            && item_codes.contains(t.getItemCode()) && t.getPersonnelFactorValue() != null)
                                    .select(KpiDictItem::getPersonnelFactorValue).toList();
                            //找到list的最大值
                            if (!areAllElementsEmptyApache(personnelFactoryValue)) {
                                BigDecimal max = Collections.max(personnelFactoryValue);
                                ValueOrSystem factorValue = new ValueOrSystem(max, "0");
                                one.setFactorValue(factorValue);
                            }
                        } else {
                            //没字典值
                            ValueOrSystem factorValue = new ValueOrSystem();
                            one.setFactorValue(factorValue);
                        }
                        //系统数值集合添加key
                        // systemValueType.add(key);
                    }
                }
                //普通一级字典 职务等 可能有多条
                List<KpiUserFactor> office = Linq.of(office_list).where(t -> t.getUserId().equals(item.getUserId())
                        && t.getDictType().equals(key.getDictType())).toList();
                if (CollectionUtils.isNotEmpty(office)) {
                    List<String> split = Linq.of(office).select(KpiUserFactor::getItemCode).toList();
                    //赋对应字典中文值
                    List<KpiDictItem> description = Linq.of(kpiDictItems).where(t -> t.getDictType().equals(office.get(0).getDictType())
                            && split.contains(t.getItemCode())).toList();
                    for (KpiDictItem dicItem : description) {
                        KpiDicItemDto dto = new KpiDicItemDto();
                        BeanUtils.copyProperties(dicItem, dto);
                        itemCode.add(dto);
                    }
                    one.setItemCode(itemCode);
                }
                finalList.add(one);
                //jsonObject.put("dic", finalList.stream().distinct().collect(Collectors.toList()));
            }
            jsonObject.put("dic", finalList);
            retjson.add(jsonObject);
        }
        //做筛选
        if (searchDto != null) {
            if (StringUtils.isNotBlank(searchDto.getDeptName())) {
                retjson = Linq.of(retjson).where(t -> t.get("deptName").toString().contains(searchDto.getDeptName())).toList();
            }
            if (StringUtils.isNotBlank(searchDto.getName())) {
                retjson = Linq.of(retjson).where(t -> t.get("name").toString().contains(searchDto.getName())).toList();
            }
            if (StringUtils.isNotBlank(searchDto.getZwName())) {
                retjson = Linq.of(retjson).where(t -> t.get(searchDto.getDictType()).toString().contains(searchDto.getZwName())).toList();
            }
        }
        retjson = Linq.of(retjson).orderBy(t -> t.get("deptName")).thenBy(t -> t.get("name")).toList();
        retjson = Linq.of(retjson).orderBy(t -> t.get("userStatus")).toList();
        return retjson;
    }


    public KpiUserFactorPageDto getTable(KpiUserFactorSearchDto searchDto) {
        KpiUserFactorPageDto ta = new KpiUserFactorPageDto();
        //TODO 生成表头
        List<KeyValueDTO> headTitleList = getHeadTitleList();
        ta.setHead_list(headTitleList);
        //TODO 生成表体
        List<JSONObject> bodyList = getBodyList(searchDto, headTitleList);
        ta.setBody_list(bodyList);
        return ta;
    }


    public IPage<JSONObject> pageUserFactor(KpiUserFactorSearchDto searchDto) {
        IPage<JSONObject> page = new Page<>(searchDto.getCurrent(), searchDto.getSize());
        List<KeyValueDTO> headTitleList = getHeadTitleList();
        List<JSONObject> bodyList = getBodyList(searchDto, headTitleList);
        int total = bodyList.size();
        if (total > searchDto.getSize()) {
            long toIndex = searchDto.getSize() * searchDto.getCurrent();
            if (toIndex > total) {
                toIndex = total;
            }
            bodyList = bodyList.subList((int) (searchDto.getSize() * (searchDto.getCurrent() - 1)), (int) toIndex);
        }
        page.setRecords(bodyList);
        page.setPages((total + searchDto.getSize() - 1) / searchDto.getSize());
        page.setTotal(total);
        return page;
    }


    public List<Tree<String>> getTree(String dicType) {
        //字典项表
        List<KpiDictItem> kpiDictItems = kpiItemMapper.selectList(new LambdaQueryWrapper<KpiDictItem>()
                .eq(KpiDictItem::getDelFlag, "0").eq(KpiDictItem::getStatus, "0")
                .eq(KpiDictItem::getDictType, dicType));
        //人员
        //List<SysUser> user = kpiUserFactorMapper.getUser();
        //先拼接出人员科室 type = user
        List<KpiUserFactorBeforeDto> userDept = kpiUserFactorMapper.getUserDept(null);
        //先找到人
        List<KpiPersonnelFactorList> before = new ArrayList<>();
        List<KpiUserFactor> kpiUserFactors = kpiUserFactorMapper.selectList(new LambdaQueryWrapper<KpiUserFactor>()
                .eq(KpiUserFactor::getDictType, dicType)
                .eq(KpiUserFactor::getDelFlag, "0"));
        //一类字典列表
        List<KpiUserFactor> officeList = Linq.of(kpiUserFactors).where(t -> t.getType().equals(UserFactorCodeEnum.OFFICE.getCode())).toList();
        //系数列表
        List<KpiUserFactor> coefficientList = Linq.of(kpiUserFactors).where(t -> t.getType().equals(UserFactorCodeEnum.COEFFICIENT.getCode())).toList();
        coefficientList = Linq.of(coefficientList).where(t -> t.getDeptId() == null).toList();
        //补贴列表
        List<KpiUserFactor> subsidyList = Linq.of(kpiUserFactors).where(t -> t.getType().equals(UserFactorCodeEnum.SUBSIDY.getCode())).toList();
        for (KpiUserFactor kpiUserFactor : officeList) {
            //根据职务这类一类字典去分组二类
//            String[] split = kpiUserFactor.getItemCode().split(",");
//            for (String a : split) {
            KpiPersonnelFactorList kpiPersonnelFactorList = new KpiPersonnelFactorList();
            kpiPersonnelFactorList.setItemCode(kpiUserFactor.getItemCode());
            String name = Linq.of(userDept).where(t -> t.getUserId().equals(kpiUserFactor.getUserId())).select(KpiUserFactorBeforeDto::getName).firstOrDefault();
            if (StringUtils.isNotBlank(name)) {
                kpiPersonnelFactorList.setName(name);
            }
            String deptName = Linq.of(userDept).where(t -> t.getUserId().equals(kpiUserFactor.getUserId())).select(KpiUserFactorBeforeDto::getDeptName).firstOrDefault();
            if (StringUtils.isNotBlank(deptName)) {
                kpiPersonnelFactorList.setDeptName(deptName);
            }
            kpiPersonnelFactorList.setUserId(kpiUserFactor.getUserId());
            KpiDictItem kpiDictItem = Linq.of(kpiDictItems).where(t -> t.getItemCode().equals(kpiUserFactor.getItemCode())).firstOrDefault();
            if (kpiDictItem == null) {
                continue;
            }
            kpiPersonnelFactorList.setLabel(kpiDictItem.getLabel());
            kpiPersonnelFactorList.setParentCode(kpiDictItem.getParentCode());
            if (kpiDictItem.getPersonnelFactorValue() != null) {
                kpiPersonnelFactorList.setPersonnelFactoryValue(kpiDictItem.getPersonnelFactorValue());
            }
            if (kpiDictItem.getPerformanceSubsidyValue() != null) {
                kpiPersonnelFactorList.setPerformanceSubsidyValue(kpiDictItem.getPerformanceSubsidyValue());
            }
            //默认系统类型
            kpiPersonnelFactorList.setType("系统规则");
            kpiPersonnelFactorList.setValue(kpiDictItem.getPersonnelFactorValue());
            before.add(kpiPersonnelFactorList);
//            }
        }
        //拼接id 以及类型及系数
        for (KpiPersonnelFactorList b : before) {
//            KpiUserFactor kpiUserFactor = Linq.of(coefficientList).where(t -> t.getItemCode().contains(b.getItemCode())
//                    && t.getUserId().equals(b.getUserId())).firstOrDefault();
            List<KpiUserFactor> kpiUserFactor = Linq.of(coefficientList).where(t -> t.getDictType().equals(dicType)
                    && t.getUserId().equals(b.getUserId())).toList();
            List<KpiUserFactor> kpiUserSubsidy = Linq.of(subsidyList).where(t -> t.getDictType().equals(dicType)
                    && t.getUserId().equals(b.getUserId())).toList();
            if (CollUtil.isNotEmpty(kpiUserFactor)) {
                kpiUserFactor.sort(Comparator.comparing(KpiUserFactor::getValue).reversed());
                b.setId(kpiUserFactor.get(0).getId());
                b.setType("自定义规则");
                b.setValue(kpiUserFactor.get(0).getValue());
            }
            if (CollUtil.isNotEmpty(kpiUserSubsidy)) {
                kpiUserSubsidy.sort(Comparator.comparing(KpiUserFactor::getValue).reversed());
                b.setId(kpiUserSubsidy.get(0).getId());
                b.setSubsidyType("自定义规则");
                b.setSubsidyValue(kpiUserSubsidy.get(0).getValue());
            }
        }
        //补充上级
        List<KpiPersonnelFactorList> kpiPersonnelFactorLists = new ArrayList<>(new HashSet<>(before));
        for (KpiPersonnelFactorList b : before) {
            getUpperDicOne(kpiPersonnelFactorLists, b);
        }
        //根据userId和itemCode去重
        Map<String, KpiPersonnelFactorList> map = new HashMap<>();
        for (KpiPersonnelFactorList a : kpiPersonnelFactorLists) {
            map.put(a.getUserId() + "_" + a.getItemCode(), a);
        }
        kpiPersonnelFactorLists = new ArrayList<>(map.values());
        kpiPersonnelFactorLists = Linq.of(kpiPersonnelFactorLists).orderBy(KpiPersonnelFactorList::getItemCode).toList();
        //变树
        List<TreeNode<String>> collect = kpiPersonnelFactorLists.stream().map(getNodeFunction()).collect(Collectors.toList());
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        treeNodeConfig.setNameKey("codeName");
        treeNodeConfig.setParentIdKey("parentCode");
        return TreeUtilExtend.build2(collect, "-1", treeNodeConfig);
    }


    //从人员出发找到二类字典向上补全
    public List<KpiPersonnelFactorList> getUpperDicOne(List<KpiPersonnelFactorList> diclist, KpiPersonnelFactorList dic) {
        if (dic != null && dic.getParentCode() != null) {
            KpiPersonnelFactorList factor = new KpiPersonnelFactorList();
            KpiDictItem parent_dic = kpiItemMapper.selectOne(new LambdaQueryWrapper<KpiDictItem>()
                    .eq(KpiDictItem::getItemCode, dic.getParentCode()));
            if (parent_dic == null) {
                return diclist;
            }
            int count = Linq.of(diclist).where(t -> t.getItemCode().equals(parent_dic.getItemCode())).count();
            if (count == 0) {
                factor.setItemCode(parent_dic.getItemCode());
                factor.setLabel(parent_dic.getLabel());
                factor.setUserId(0L);
                factor.setParentCode(parent_dic.getParentCode());
                factor.setPersonnelFactoryValue(parent_dic.getPersonnelFactorValue());
                diclist.add(factor);
            }
            getUpperDicOne(diclist, factor);
        }
        return diclist;
    }

    @javax.validation.constraints.NotNull
    private Function<KpiPersonnelFactorList, TreeNode<String>> getNodeFunction() {
        return factor -> {
            TreeNode<String> node = new TreeNode<>();
            node.setId(factor.getItemCode());
            node.setName(factor.getLabel());
            node.setParentId(factor.getParentCode());
            node.setWeight(null);

            // 扩展属性
            Map<String, Object> extra = new HashMap<>();
            extra.put("userName", factor.getName());
            extra.put("userId", factor.getUserId());
            extra.put("deptName", factor.getDeptName());
            extra.put("itemCode", factor.getItemCode());
            extra.put("type", factor.getType());
            extra.put("value", factor.getValue());
            extra.put("subsidyType", factor.getSubsidyType());
            extra.put("subsidyValue", factor.getSubsidyValue());
            extra.put("personnelFactoryValue", factor.getPersonnelFactoryValue());
            extra.put("performanceSubsidyValue", factor.getPerformanceSubsidyValue());
            //extra.put("parentCode", factor.getParentCode());
            node.setExtra(extra);
            return node;
        };
    }

    public ImportErrVo importFile(MultipartFile uploadFile, String overwriteFlag) {
        try {
            String[] headMap = getHead();
            //校验表头是否与数据库实时一致
            Result result = CommonUtil.checkExcel(uploadFile, headMap);
            if (result != null) {
                throw new IOException("上传清单模板与系统不一致，请重新下载模板！" + result.getData());
            }
            String[][] data = CommonUtil.getExcelData(uploadFile);
            for (int i = 1; i < data.length; i++) {
                if (data[0].length != headMap.length
                        || data[i].length != headMap.length) {
                    throw new IOException("上传系数模板与系统不一致，请重新下载模板！");
                }
            }
            return doImport(data, overwriteFlag);
        } catch (Exception e) {
            throw new BizException("导入数据失败：" + e);
        }
    }

    public List<List<String>> getErrorHead() {
        List<List<String>> errorHead = new ArrayList<>();
        errorHead.add(ImmutableList.of("行号"));
        errorHead.add(ImmutableList.of("姓名"));
        errorHead.add(ImmutableList.of("错误说明"));
        return errorHead;
    }

    /*
     * 数据入库
     *
     */
    private ImportErrVo doImport(String[][] data, String overwriteFlag) {
        List<List<String>> head = getErrorHead();
        List<ImportErrListVO> details = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        List<LinkedHashMap<String, Object>> positons = CommonUtil.excelCellToSqlTypes(data, getHeadMap());
        //找到所有字典项
        List<KpiDictItem> kpiDictItems = kpiItemMapper.selectList(null);
        List<SysUser> user = kpiUserFactorMapper.getUser();
        List<KpiUserFactor> before = new ArrayList<>();
        //TODO 开始做数据转换落库
        if (CollectionUtils.isNotEmpty(positons)) {
            // 将data进行处理
            for (int i = 0; i < positons.size(); i++) {
                // 错误说明
                List<String> contentList = new ArrayList<>();
                LinkedHashMap<String, Object> stringObjectLinkedHashMap = positons.get(i);
                String userName = stringObjectLinkedHashMap.get("userName").toString().trim();
                for (Map.Entry<String, Object> entry : stringObjectLinkedHashMap.entrySet()) {
                    try {
                        Long userId = null;
                        try {
                            userId = Linq.of(user).where(t -> t.getUsername().trim().equals(userName.trim())).select(SysUser::getUserId).firstOrDefault();
                        } catch (Exception e) {
                            throw new Exception(userName.trim() + "该用户不存在");
                        }
                        if(userId==null)
                        {
                            throw new Exception("该用户不存在");
                        }
                        //遍历循环map
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        if (key.equals("userName")) {
                            //存人 直接加
                            KpiUserFactor factor = new KpiUserFactor();
                            factor.setUserId(userId);
                            factor.setType(UserFactorCodeEnum.USER.getCode());
                            before.add(factor);
                        } else {
                            //去字典匹配找
                            if (StringUtils.isNotBlank(value.toString())) {
                                //可能为多条数据
                                String[] split = value.toString().split(",");
                                List<Object> value_list = Arrays.asList(split);
                                //对应分类下的所有字典项
                                List<String> code_list = Linq.of(kpiDictItems).where(t -> t.getDictType().equals(key)
                                        && value_list.contains(t.getLabel())).select(KpiDictItem::getItemCode).toList();
                                //存对应字典
//                            KpiUserFactor factor = new KpiUserFactor();
//                            factor.setUserId(userId);
//                            factor.setType(UserFactorCodeEnum.OFFICE.getCode());
//                            factor.setDictType(key);
//                            factor.setItemCode(String.join(",", code_list));
//                            before.add(factor);
                                for (String code : code_list) {
                                    KpiUserFactor factor = new KpiUserFactor();
                                    factor.setUserId(userId);
                                    factor.setType(UserFactorCodeEnum.OFFICE.getCode());
                                    factor.setDictType(key);
                                    factor.setItemCode(code);
                                    before.add(factor);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("导入报错", e);
                        String message = e.getMessage();
                        contentList.add(message);
                    }
                }
                if (CollUtil.isNotEmpty(contentList)) {
                    // 生成错误说明
                    failCount++;
                    ImportErrListVO build = ImportErrListVO.builder().lineNum(i + 1)
                            .data(ImmutableList.of(userName)).contentList(contentList).content(StrUtil.join(";",
                                    contentList)).build();
                    details.add(build);
                } else {
                    successCount++;
                }
            }
        }
        if (!before.isEmpty()) {
            before.forEach(t -> {
                t.setTenantId(SecurityUtils.getUser().getTenantId());
                t.setCreateTime(new Date());
            });
            List<List<KpiUserFactor>> partition = ListUtils.partition(before, 1000);
            //找到数据库种原有的userId数据 删除
            if (overwriteFlag.equals("1")) {
                kpiUserFactorMapper.delete(null);
            } else if (overwriteFlag.equals("2")) {
                List<Long> importUserIds = Linq.of(before).select(KpiUserFactor::getUserId).distinct().toList();
                kpiUserFactorMapper.delete(new LambdaQueryWrapper<KpiUserFactor>().in(KpiUserFactor::getUserId, importUserIds));
            }
            partition.forEach(kpiUserFactorMapper::insertBatchSomeColumn);
        }
        return ImportErrVo.builder().details(details).successCount(successCount).failCount(failCount).head(head).build();
    }


    //拿中文名
    public String[] getHead() {
        List<KpiDict> kpiDicts = kpiDictMapper.selectList(new LambdaQueryWrapper<KpiDict>().eq(KpiDict::getStatus, "0"));
        List<String> dic_names = Linq.of(kpiDicts).select(KpiDict::getDescription).toList();
        dic_names.add(0, "用户姓名");
        return dic_names.toArray(new String[dic_names.size()]);
    }

    /**
     * key dic_type value 中文
     *
     * @return
     */
    public List<LinkedHashMap<String, Object>> getHeadMap() {
        List<LinkedHashMap<String, Object>> final_list = new ArrayList<>();
        List<KpiDict> kpiDicts = kpiDictMapper.selectList(new LambdaQueryWrapper<KpiDict>().eq(KpiDict::getStatus, "0"));
        LinkedHashMap<String, Object> map_before = new LinkedHashMap<>();
        map_before.put("userName", "用户姓名");
        final_list.add(map_before);
        for (KpiDict kpiDict : kpiDicts) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put(kpiDict.getDictType(), kpiDict.getDescription());
            final_list.add(map);
        }
        return final_list;
    }


    public void exportFactorModelExcel(HttpServletResponse response) throws IOException {
        String[] headMap = getHead();
        List<LinkedHashMap<String, Object>> listDatas = new ArrayList<LinkedHashMap<String, Object>>();
        String[] dataStrMap = new String[headMap.length];
        NoModelWriteData d = new NoModelWriteData();
        d.setFileName("人员系数导入模板");
        d.setHeadMap(headMap);
        d.setDataStrMap(dataStrMap);
        d.setDataList(listDatas);
        EasyExcelUtils easyExcelUtils = new EasyExcelUtils();
        easyExcelUtils.noModleWrite(d, response);
    }


    public List<KpiUserFactorDeptDto> deptValueList(KpiUserFactorDeptSearchDto dto) {
        List<KpiUserFactorDeptDto> kpiUserFactorDeptDtos = kpiUserFactorMapper.deptValueList(dto);
        List<KpiUserFactor> kpiUserFactors = kpiUserFactorMapper.selectList(new LambdaQueryWrapper<KpiUserFactor>()
                .eq(KpiUserFactor::getType, UserFactorCodeEnum.OFFICE.getCode())
                .eq(KpiUserFactor::getDelFlag, "0"));
        kpiUserFactors = Linq.of(kpiUserFactors).where(t -> t.getDeptId() != null).toList();
        for (KpiUserFactorDeptDto a : kpiUserFactorDeptDtos) {
            KpiUserFactor kpiUserFactor = Linq.of(kpiUserFactors)
                    .where(t -> t.getUserId().equals(a.getUserId())
                            && t.getDeptId().equals(a.getDeptId())).firstOrDefault();
            if (kpiUserFactor != null) {
                a.setFactor(kpiUserFactor.getValue());
                a.setIsSystem("1");
            }
        }
        return kpiUserFactorDeptDtos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDeptfactor(KpiUserDeptFactorAddDto userFactorDto) {
        if (userFactorDto.getUserId() == null) {
            return;
        }
        if (userFactorDto.getValue() != null) {
            //修改系数
            kpiUserFactorMapper.delete(new LambdaQueryWrapper<KpiUserFactor>()
                    .eq(KpiUserFactor::getUserId, userFactorDto.getUserId())
                    .eq(KpiUserFactor::getType, UserFactorCodeEnum.OFFICE.getCode())
                    .eq(KpiUserFactor::getDeptId, userFactorDto.getDeptId()));
            KpiUserFactor user = new KpiUserFactor();
            user.setUserId(userFactorDto.getUserId());
            user.setType(UserFactorCodeEnum.OFFICE.getCode());
            user.setDeptId(userFactorDto.getDeptId());
            user.setCreateTime(new Date());
            user.setValue(userFactorDto.getValue());
            user.insert();
        } else {
            kpiUserFactorMapper.delete(new LambdaQueryWrapper<KpiUserFactor>()
                    .eq(KpiUserFactor::getUserId, userFactorDto.getUserId())
                    .eq(KpiUserFactor::getType, UserFactorCodeEnum.OFFICE.getCode())
                    .eq(KpiUserFactor::getDeptId, userFactorDto.getDeptId()));
        }
    }
}
