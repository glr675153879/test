package com.hscloud.hs.cost.account.service.impl.imputation;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.constant.DeptOrUserConstant;
import com.hscloud.hs.cost.account.constant.enums.imputation.ImputationType;
import com.hscloud.hs.cost.account.model.dto.imputation.NonAndSpecialDelDTO;
import com.hscloud.hs.cost.account.model.dto.imputation.SpecialPersonIndexOrUnitDTO;
import com.hscloud.hs.cost.account.model.entity.base.Entity;
import com.hscloud.hs.cost.account.model.entity.imputation.Imputation;
import com.hscloud.hs.cost.account.service.imputation.IImputationService;
import com.hscloud.hs.cost.account.model.entity.imputation.NonIncomePerson;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.hscloud.hs.cost.account.utils.OperationUtil;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.imputation.SpecialImputationPersonMapper;
import com.hscloud.hs.cost.account.model.entity.imputation.SpecialImputationPerson;
import com.hscloud.hs.cost.account.service.imputation.ISpecialImputationPersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 特殊归集人员 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpecialImputationPersonService extends ServiceImpl<SpecialImputationPersonMapper, SpecialImputationPerson> implements ISpecialImputationPersonService {

    @Lazy
    @Autowired
    private ImputationDeptUnitService imputationDeptUnitService;

    private final IImputationService imputationService;

    private final OperationUtil operationUtil;

    @Override
    public List<Attendance> listByPId(Long imputationId, Long accountUnitId, Long indexId, List<SpecialImputationPerson> allList) {
        List<SpecialImputationPerson> specialImputationPersonList = new ArrayList<>();
        if (allList == null) {
            List<SpecialImputationPerson> list = this.list(Wrappers.<SpecialImputationPerson>lambdaQuery()
                    .eq(SpecialImputationPerson::getImputationId, imputationId)
                    .like(accountUnitId != null, SpecialImputationPerson::getAccountUnitIds, accountUnitId + "")
                    .like(indexId != null, SpecialImputationPerson::getImputationIndexIds, indexId + ""));
            specialImputationPersonList.addAll(list);
        } else {
            specialImputationPersonList = allList.stream()
                    .filter(item -> item.getImputationIndexIds() != null && item.getAccountUnitIds() != null && item.getImputationIndexIds().contains(indexId + "") && item.getAccountUnitIds().contains(accountUnitId + ""))
                    .collect(Collectors.toList());
        }
        return specialImputationPersonList.stream().map(item -> new Attendance().setUserId(item.getUserId()).setEmpName(item.getUserName())).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveOrEditSpecialImputationPerson(SpecialImputationPerson specialImputationPerson) {
        imputationService.setImputation(specialImputationPerson, specialImputationPerson.getImputationId());
        specialImputationPerson.setUserId(Long.valueOf(Objects.requireNonNull(CommonUtils.getValueFromUserObj(specialImputationPerson.getLeaderUser(), "id", DeptOrUserConstant.USER_LIST))));
        specialImputationPerson.setUserName(CommonUtils.getValueFromUserObj(specialImputationPerson.getLeaderUser(), "name", DeptOrUserConstant.USER_LIST));

        if (Objects.equals(specialImputationPerson.getImputationCode(), ImputationType.WORKLOAD_DATA_IMPUTATION.toString())) {
            //工作量归集特殊归集人员
            SpecialImputationPerson s = getOne(Wrappers.<SpecialImputationPerson>lambdaQuery()
                    .eq(SpecialImputationPerson::getImputationId, specialImputationPerson.getImputationId())
                    .eq(SpecialImputationPerson::getUserId, specialImputationPerson.getUserId()));
            if (ObjectUtils.isNotNull(specialImputationPerson.getId())) {
                if (ObjectUtils.isNotNull(s) && !Objects.equals(s.getId(), specialImputationPerson.getId())) {
                    log.error("工作量数据归集特殊归集人员编辑，月份为{}的{}人员{}已经在已匹配列表中", specialImputationPerson.getImputationCycle(), specialImputationPerson.getImputationName(), specialImputationPerson.getUserName());
                    throw new BizException("此人员已经存在，不可重复！");
                }
            } else {
                if (ObjectUtils.isNotNull(s)) {
                    log.error("工作量数据归集特殊归集人员新增，月份为{}的{}人员{}已经在已匹配列表中", specialImputationPerson.getImputationCycle(), specialImputationPerson.getImputationName(), specialImputationPerson.getUserName());
                    throw new BizException("此人员已经存在，不可重复！");
                }
            }

        } else if (Objects.equals(specialImputationPerson.getImputationCode(), ImputationType.INCOME_DATA_IMPUTATION.toString())) {
            List<SpecialImputationPerson> list = list(Wrappers.<SpecialImputationPerson>lambdaQuery()
                    .eq(SpecialImputationPerson::getImputationId, specialImputationPerson.getImputationId())
                    .eq(SpecialImputationPerson::getUserId, specialImputationPerson.getUserId()));
            //校验 同一个人不能有重复的归集指标
            if (CollectionUtils.isNotEmpty(list)) {
                if (ObjectUtils.isNotNull(specialImputationPerson.getId())) {
                    list.removeIf(item -> item.getId().equals(specialImputationPerson.getId()));
                }
                checkHaveSameIndex(list, specialImputationPerson.getImputationIndexList());
            }

        }
        fillAccountUnit(specialImputationPerson);
        saveOrUpdate(specialImputationPerson);

        if (Objects.equals(specialImputationPerson.getImputationCode(), ImputationType.INCOME_DATA_IMPUTATION.toString())) {
            imputationDeptUnitService.setIncomePerson(specialImputationPerson.getImputationCycle());
        }

        return true;
    }

    @Override
    public IPage<SpecialImputationPerson> pageSpecialImputationPerson(Page<SpecialImputationPerson> page, QueryWrapper<SpecialImputationPerson> wrapper, Long imputationId) {
        log.info("分页查询归集人员，主档ID入参{}", imputationId);
        Page<SpecialImputationPerson> specialImputationPersonPage = page(page, wrapper.eq("imputation_id", imputationId).orderByDesc("create_time"));
        List<SpecialImputationPerson> records = specialImputationPersonPage.getRecords();

        if (CollectionUtils.isNotEmpty(records)) {
            Map<Long, String> userMap = operationUtil.getUserMap(records);

            records.forEach(item ->
            {
                item.setImputationIndexList(CommonUtils.getIndexOrUnitList(item.getImputationIndexIds(), item.getImputationIndexNames()));
                item.setLeaderDept(CommonUtils.getUserObj(item.getAccountUnitIds(), item.getAccountUnitNames(), DeptOrUserConstant.DEPT_LIST));
                item.setLeaderUser(CommonUtils.getUserObj(item.getUserId() + "", item.getUserName(), DeptOrUserConstant.USER_LIST));

                //操作人，操作时间
                item.setOperationTime(operationUtil.getOperationTime(item));
                item.setOperationName(operationUtil.getOperationName(userMap, item));
            });
        }
        return specialImputationPersonPage;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean removeSpecialImputationPerson(NonAndSpecialDelDTO nonAndSpecialDelDTO) {
        removeById(nonAndSpecialDelDTO.getId());

        Long imputationId = nonAndSpecialDelDTO.getImputationId();
        Imputation imputation = imputationService.getById(imputationId);
        if (Objects.nonNull(imputation)) {
            if (ImputationType.INCOME_DATA_IMPUTATION.toString().equals(imputation.getImputationCode())) {
                log.info("重新计算归集人员，归集类型为{}", imputation.getImputationCode());
                imputationDeptUnitService.setIncomePerson(nonAndSpecialDelDTO.getCycle());
            }
        }
        return true;
    }

    @Override
    public List<Attendance> listAllByPId(Long imputationId, Long indexId) {
        LambdaQueryWrapper<SpecialImputationPerson> wrapper = Wrappers.<SpecialImputationPerson>lambdaQuery().eq(SpecialImputationPerson::getImputationId, imputationId).like(SpecialImputationPerson::getImputationIndexIds, String.valueOf(indexId));
        List<SpecialImputationPerson> specialImputationPersonList = list(wrapper);
        return specialImputationPersonList.stream().map(item -> {
            Attendance attendance = new Attendance();
            attendance.setUserId(item.getUserId());
            attendance.setEmpName(item.getUserName());
            return attendance;
        }).collect(Collectors.toList());
    }

    private void checkHaveSameIndex(List<SpecialImputationPerson> list, List<SpecialPersonIndexOrUnitDTO> imputationIndexList) {
        List<SpecialPersonIndexOrUnitDTO> oldList = new ArrayList<>();
        for (SpecialImputationPerson specialImputationPerson : list) {
            oldList.addAll(CommonUtils.getIndexOrUnitList(specialImputationPerson.getImputationIndexIds(), specialImputationPerson.getImputationIndexNames()));
        }
        //两个集合有相同元素则有重复
        if (CollectionUtils.isNotEmpty(oldList) && CollectionUtils.isNotEmpty(imputationIndexList)) {
            List<String> oldIds = oldList.stream().map(SpecialPersonIndexOrUnitDTO::getId).collect(Collectors.toList());
            List<String> newIds = imputationIndexList.stream().map(SpecialPersonIndexOrUnitDTO::getId).collect(Collectors.toList());
            for (String id : newIds) {
                if (oldIds.contains(id)) {
                    throw new BizException("归集指标有重复，同一个人的归集指标不可重复！");
                }
            }

        }
    }

    private void fillAccountUnit(SpecialImputationPerson specialImputationPerson) {
        Map<String, Object> leaderDept = specialImputationPerson.getLeaderDept();
        if (MapUtil.isNotEmpty(leaderDept)) {
            specialImputationPerson.setAccountUnitIds(CommonUtils.getValueFromUserObj(leaderDept, "id", DeptOrUserConstant.DEPT_LIST));
            specialImputationPerson.setAccountUnitNames(CommonUtils.getValueFromUserObj(leaderDept, "name", DeptOrUserConstant.DEPT_LIST));
        }
        List<SpecialPersonIndexOrUnitDTO> imputationIndexList = specialImputationPerson.getImputationIndexList();
        if (CollectionUtils.isNotEmpty(imputationIndexList)) {
            String ids = imputationIndexList.stream().map(SpecialPersonIndexOrUnitDTO::getId).collect(Collectors.joining(","));
            String names = imputationIndexList.stream().map(SpecialPersonIndexOrUnitDTO::getName).collect(Collectors.joining(","));
            specialImputationPerson.setImputationIndexIds(ids);
            specialImputationPerson.setImputationIndexNames(names);
        }
    }


}
