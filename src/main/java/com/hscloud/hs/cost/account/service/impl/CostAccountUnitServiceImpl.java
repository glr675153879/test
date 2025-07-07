package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.hscloud.hs.cost.account.mapper.CostAccountUnitMapper;
import com.hscloud.hs.cost.account.mapper.CostUnitExcludedInfoMapper;
import com.hscloud.hs.cost.account.model.dto.*;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.CostUnitExcludedInfo;
import com.hscloud.hs.cost.account.model.entity.CostUnitRelateInfo;
import com.hscloud.hs.cost.account.model.vo.CostAccountUnitExcelVO;
import com.hscloud.hs.cost.account.service.CostAccountUnitService;
import com.hscloud.hs.cost.account.utils.LocalCacheUtils;
import com.hscloud.hs.cost.account.utils.OperationUtil;
import com.hscloud.hs.cost.account.utils.SqlUtil;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.excel.vo.ErrorMessage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author YJM
 * @date 2023-09-05 09:47
 */
@Service
@RequiredArgsConstructor
public class CostAccountUnitServiceImpl extends ServiceImpl<CostAccountUnitMapper, CostAccountUnit> implements CostAccountUnitService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CostAccountUnitMapper costAccountUnitMapper;

    private final CostUnitExcludedInfoMapper costUnitExcludedInfoMapper;

    private final SqlUtil sqlUtil;

    private final LocalCacheUtils cacheUtils;

    private final RemoteUserService remoteUserService;

    private final OperationUtil operationUtil;

    @Override
    public IPage<CostAccountUnit> listUnit(CostAccountUnitQueryDtoNew input) {
        Page<CostAccountUnit> accountUnitPage = new Page<>(input.getCurrent(), input.getSize());
        IPage<CostAccountUnit> resultPage = costAccountUnitMapper.listByQueryDto(accountUnitPage, input);

        List<CostAccountUnit> units = resultPage.getRecords();

        Map<Long, String> userMap = operationUtil.getUserMap(units);
        //获取责任人信息
        for (CostAccountUnit unit : units) {
            CommonDTO commonDTO = new CommonDTO();
            commonDTO.setId(unit.getResponsiblePersonId());
            commonDTO.setName(unit.getResponsiblePersonName());
            commonDTO.setType(unit.getResponsiblePersonType());
            List<CostUnitRelateInfoDto> relateInfoDtos = costAccountUnitMapper.selectRelateInfoByUnitId(unit.getId());
            if (!relateInfoDtos.isEmpty()) {
                unit.setCostUnitRelateInfo(relateInfoDtos);
            }
            unit.setResponsiblePerson(commonDTO);

            //操作人、操作时间
            unit.setOperationTime(operationUtil.getOperationTime(unit));
            unit.setOperationName(operationUtil.getOperationName(userMap, unit));
        }
        resultPage.setRecords(units);

        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveUnit(CostAccountUnitDto dto) {

        CostAccountUnit costAccountUnit = BeanUtil.copyProperties(dto, CostAccountUnit.class);
        costAccountUnit.setResponsiblePersonId(dto.getResponsiblePerson().getId());
        costAccountUnit.setResponsiblePersonName(dto.getResponsiblePerson().getName());
        costAccountUnit.setResponsiblePersonType(dto.getResponsiblePerson().getType());
        save(costAccountUnit);
        cacheUtils.setAccountUnitMap(costAccountUnit);
        //保存核算科室/人 信息到relate info表
        dto.getCostUnitRelateInfo().forEach(info -> {
            costAccountUnitMapper.insertRelateInfo(costAccountUnit.getId(), info.getName(), info.getId(), info.getType(), info.getCode());
        });

        return 0L;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateUnit(CostAccountUnitDto dto) {
        /*String status=costAccountUnitMapper.selectStatusById(dto.getId());
        //更新状态 status
        if(!isEmpty(status)&&!status.equals(dto.getStatus())){
            costAccountUnitMapper.updateStatusById(dto.getId(), dto.getStatus());
            return dto.getId();
        }
*/
        //update
        if (StrUtil.isEmpty(dto.getName())
        ) {
            throw new BizException("必填参数不能为空");
        }
        costAccountUnitMapper.removeRelateInfoByUnitId(dto.getId());
        costAccountUnitMapper.removeExcludedInfoByUnitId(dto.getId());
        CostAccountUnit unit = BeanUtil.copyProperties(dto, CostAccountUnit.class);
        //if(!StringUtils.isEmpty(dto.getResponsiblePerson().getId()))
        unit.setId(dto.getId());
        unit.setResponsiblePersonId(dto.getResponsiblePerson().getId());
        unit.setResponsiblePersonName(dto.getResponsiblePerson().getName());
        unit.setResponsiblePersonType(dto.getResponsiblePerson().getType());
        updateById(unit);

        //更新relate info表
        dto.getCostUnitRelateInfo().forEach(info -> {
            costAccountUnitMapper.insertRelateInfo(unit.getId(), info.getName(), info.getId(), info.getType(), info.getCode());
        });

        return 0L;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUnitById(Long id) {
        costAccountUnitMapper.removeRelateInfoByUnitId(id);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchUnit(CostAccountUnitDto dto) {
        CostAccountUnit d = costAccountUnitMapper.selectById(dto.getId());
        if (ObjectUtils.isEmpty(d)) {
            throw new BizException("不存在记录");
        }
        d.setStatus(dto.getStatus());
        updateById(d);
        //saveApply(dto.getId(),"status",dto);
    }

    @Override
    public R importUnit(List<CostAccountUnitExcelVO> excelVOList, BindingResult bindingResult) {
        List<ErrorMessage> errorMessageList = (List<ErrorMessage>) bindingResult.getTarget();
        Set<String> unitNameSet = new HashSet<>();
        List<CostAccountUnit> costAccountUnits = this.list();
        for (CostAccountUnitExcelVO item : excelVOList) {
            Set<String> errorMsg = new HashSet<>();
            //校验科室单元名称是否存在
            boolean existUnitName = costAccountUnits.stream().anyMatch(costAccountUnit -> item.getUnitName().equals(costAccountUnit.getName()));
            if (existUnitName || unitNameSet.contains(item.getUnitName())) {
                errorMsg.add("科室单元名称已存在");
            } else {
                unitNameSet.add(item.getUnitName());
            }
            Map<String, String> map = new HashMap<>(8);
            map.put("医生组", "HSDX001");
            map.put("护理组", "HSDX002");
            map.put("行政组", "HSDX003");
            map.put("医技组", "HSDX004");
            map.put("后勤组", "HSDX005");
            map.put("药剂组", "HSDX006");
            if (CollUtil.isEmpty(errorMsg)) {
                CostAccountUnit costAccountUnit = new CostAccountUnit();
                costAccountUnit.setName(item.getUnitName());
                DictDto dictDto = DictDto.builder().label(item.getGroupName()).value(map.get(item.getGroupName())).build();
                costAccountUnit.setAccountGroupCode(JSON.toJSONString(dictDto));
                baseMapper.insert(costAccountUnit);
                CostUnitRelateInfo costUnitRelateInfo = new CostUnitRelateInfo();
                costUnitRelateInfo.setAccountUnitId(costAccountUnit.getId());
                if (item.getUnitName().equals(item.getUserName())) {
                    costUnitRelateInfo.setType("user");
                    String sql = "select user_id from `hsx`.`sys_user` where name = '" + item.getUserName() + "'";
                    try {
                        String s = jdbcTemplate.queryForObject(sql, String.class);
                        costUnitRelateInfo.setRelateId(s);
                        costUnitRelateInfo.setName(item.getUserName());
                    } catch (Exception e) {
                        log.error("用户不存在", e);
                        errorMessageList.add(new ErrorMessage(item.getLineNum(), Collections.singleton("用户不存在")));
                    }
                } else {
                    costUnitRelateInfo.setType("dept");
                    try {
                        String sql = "select dept_id from `hsx`.`sys_dept` where name = '" + item.getDeptName() + "'";
                        String s = jdbcTemplate.queryForObject(sql, String.class);
                        costUnitRelateInfo.setRelateId(s);
                        costUnitRelateInfo.setName(item.getDeptName());
                    } catch (Exception e) {
                        log.error("科室不存在", e);
                        errorMessageList.add(new ErrorMessage(item.getLineNum(), Collections.singleton("科室不存在")));
                    }

                }
                costUnitRelateInfo.insert();
            } else {
                // 数据不合法情况
                errorMessageList.add(new ErrorMessage(item.getLineNum(), errorMsg));
            }
        }
        if (CollUtil.isNotEmpty(errorMessageList)) {
            return R.failed(errorMessageList);
        }
        return R.ok(null, "科室单元导入成功");
    }


    @Override
    public List<SysUser> listUser(Long unitId) {
        //根据科室单元id查询对应的关联信息
        List<CostUnitRelateInfo> costUnitRelateInfos = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery().eq(CostUnitRelateInfo::getAccountUnitId, unitId));
        Set<Long> userIds = new HashSet<>();
        Set<Long> deptIds = new HashSet<>();
        costUnitRelateInfos.forEach(costUnitRelateInfo -> {
            if (costUnitRelateInfo.getType().equals("user")) {
                userIds.add(Long.valueOf(costUnitRelateInfo.getRelateId()));
            } else if (costUnitRelateInfo.getType().equals("dept")) {
                deptIds.add(Long.valueOf(costUnitRelateInfo.getRelateId()));
            }
        });


        //根据deptId获取用户id
        if (CollUtil.isNotEmpty(deptIds)) {
            List<Long> userIdsByDeptIds = sqlUtil.getUserIdsByDeptIds(Lists.newArrayList(deptIds));
            userIds.addAll(userIdsByDeptIds);
        }
        //根据科室单元id查询不参与核算人员
        List<CostUnitExcludedInfo> costUnitExcludedInfos = new CostUnitExcludedInfo().selectList(Wrappers.<CostUnitExcludedInfo>lambdaQuery().eq(CostUnitExcludedInfo::getAccountUnitId, unitId));
        List<String> excludeUserIds = costUnitExcludedInfos.stream().map(CostUnitExcludedInfo::getRelateId).collect(Collectors.toList());
        userIds.removeAll(excludeUserIds);
        if (CollUtil.isNotEmpty(userIds)) {
            com.pig4cloud.pigx.common.core.util.R<List<SysUser>> userList = remoteUserService.getUserList(Lists.newArrayList(userIds));
            return userList.getData();
        }
        return new ArrayList<>();
    }


}
