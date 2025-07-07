package com.hscloud.hs.cost.account.service.impl.imputation;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.constant.DeptOrUserConstant;
import com.hscloud.hs.cost.account.model.dto.imputation.NonAndSpecialDelDTO;
import com.hscloud.hs.cost.account.constant.DeptOrUserConstant;
import com.hscloud.hs.cost.account.model.dto.imputation.SpecialPersonIndexOrUnitDTO;
import com.hscloud.hs.cost.account.model.entity.imputation.Imputation;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import com.hscloud.hs.cost.account.service.imputation.IImputationService;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.common.core.constant.SecurityConstants;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.imputation.NonIncomePersonMapper;
import com.hscloud.hs.cost.account.model.entity.imputation.NonIncomePerson;
import com.hscloud.hs.cost.account.service.imputation.INonIncomePersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 不计收入人员 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NonIncomePersonService extends ServiceImpl<NonIncomePersonMapper, NonIncomePerson> implements INonIncomePersonService {

    @Lazy
    @Autowired
    private ImputationDeptUnitService imputationDeptUnitService;
    @Resource
    private RemoteUserService remoteUserService;

    private final IImputationService imputationService;

    @Override
    public List<Attendance> listByPId(Long imputationId, Long indexId, List<NonIncomePerson> allList) {
        List<Attendance> rtnList = new ArrayList<>();
        List<NonIncomePerson> nonIncomePersonList = new ArrayList<>();
        if (allList == null) {
            List<NonIncomePerson> list = this.list(Wrappers.<NonIncomePerson>lambdaQuery()
                    .eq(NonIncomePerson::getImputationId, imputationId)
                    .like(NonIncomePerson::getImputationIndexIds, indexId));
            nonIncomePersonList.addAll(list);
        } else {
            nonIncomePersonList = allList.stream().filter(nonIncomePerson -> nonIncomePerson.getImputationIndexIds().contains(indexId + "")).collect(Collectors.toList());
        }

        for (NonIncomePerson nonIncomePerson : nonIncomePersonList) {
            String type = nonIncomePerson.getDeptOrUserType();
            if (Objects.equals(type, DeptOrUserConstant.USER)) {
                rtnList.add(new Attendance().setUserId(nonIncomePerson.getDeptOrUserId()).setEmpName(nonIncomePerson.getDeptOrUserName()));
            } else {
                Long deptId = nonIncomePerson.getDeptOrUserId();
                String needIncomePersonIds = nonIncomePerson.getNeedIncomePersonIds();
                List<Long> needIncomePersonIdList = new ArrayList<>();
                if (StringUtils.isNotBlank(needIncomePersonIds)) {
                    needIncomePersonIdList = Arrays.stream(needIncomePersonIds.split(","))
                            .map(Long::valueOf)
                            .collect(Collectors.toList());
                }
                //获取科室下的所有人员, 并剔除需要计入收入人员
                R<List<SysUser>> listR = remoteUserService.listByDept(deptId, SecurityConstants.FROM_IN);
                if (listR.getCode() == 0) {
                    List<SysUser> userList = listR.getData();
                    for (SysUser sysUser : userList) {
                        if (!needIncomePersonIdList.contains(sysUser.getUserId())) {
                            rtnList.add(new Attendance().setUserId(sysUser.getUserId()).setEmpName(sysUser.getUsername()));
                        }
                    }
                }
            }
        }
        return rtnList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveOrUpdateNonIncomePerson(NonIncomePerson nonIncomePerson) {
        log.info("新增或编辑不计收入人员，入参{}", nonIncomePerson);
        imputationService.setImputation(nonIncomePerson, nonIncomePerson.getImputationId());
        Map<String, Object> leaderDept = nonIncomePerson.getLeaderDept();
        Map<String, Object> leaderUser = nonIncomePerson.getLeaderUser();
        if (MapUtil.isNotEmpty(leaderDept)) {
            nonIncomePerson.setDeptOrUserType(DeptOrUserConstant.DEPT);
            nonIncomePerson.setDeptOrUserName(CommonUtils.getValueFromUserObj(leaderDept, "name", DeptOrUserConstant.DEPT_LIST));
            nonIncomePerson.setDeptOrUserId(Long.parseLong(Objects.requireNonNull(CommonUtils.getValueFromUserObj(leaderDept, "id", DeptOrUserConstant.DEPT_LIST))));
            if (MapUtil.isNotEmpty(leaderUser)) {
                nonIncomePerson.setNeedIncomePersonIds(CommonUtils.getValueFromUserObj(leaderUser, "id", DeptOrUserConstant.USER_LIST));
                nonIncomePerson.setNeedIncomePersons(CommonUtils.getValueFromUserObj(leaderUser, "name", DeptOrUserConstant.USER_LIST));
            } else {
                nonIncomePerson.setNeedIncomePersonIds("");
                nonIncomePerson.setNeedIncomePersons("");
            }
        } else {
            if (MapUtil.isNotEmpty(leaderUser)) {
                nonIncomePerson.setDeptOrUserType(DeptOrUserConstant.USER);
                nonIncomePerson.setDeptOrUserId(Long.parseLong(Objects.requireNonNull(CommonUtils.getValueFromUserObj(leaderUser, "id", DeptOrUserConstant.USER_LIST))));
                nonIncomePerson.setDeptOrUserName(CommonUtils.getValueFromUserObj(leaderUser, "name", DeptOrUserConstant.USER_LIST));
            }
        }
        List<SpecialPersonIndexOrUnitDTO> imputationIndexList = nonIncomePerson.getImputationIndexList();
        if (CollectionUtils.isNotEmpty(imputationIndexList)) {
            nonIncomePerson.setImputationIndexIds(imputationIndexList.stream().map(SpecialPersonIndexOrUnitDTO::getId).collect(Collectors.joining(",")));
            nonIncomePerson.setImputationIndexNames(imputationIndexList.stream().map(SpecialPersonIndexOrUnitDTO::getName).collect(Collectors.joining(",")));
        }
        saveOrUpdate(nonIncomePerson);

        imputationDeptUnitService.setIncomePerson(nonIncomePerson.getImputationCycle());
        return true;
    }

    @Override
    public IPage<NonIncomePerson> pageNonIncomePerson(Page<NonIncomePerson> page, QueryWrapper<NonIncomePerson> wrapper, Long imputationId) {
        log.info("分页查询不计收入人员，主档ID入参：{}", imputationId);
        Page<NonIncomePerson> nonIncomePersonPage = page(page, wrapper.eq("imputation_id", imputationId));
        List<NonIncomePerson> records = nonIncomePersonPage.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(item -> {
                item.setImputationIndexList(CommonUtils.getIndexOrUnitList(item.getImputationIndexIds(), item.getImputationIndexNames()));
                if (Objects.equals(item.getDeptOrUserType(), DeptOrUserConstant.DEPT)) {
                    item.setLeaderDept(CommonUtils.getUserObj(item.getDeptOrUserId() + "", item.getDeptOrUserName(), DeptOrUserConstant.DEPT_LIST));
                    if (StringUtils.isNotBlank(item.getNeedIncomePersonIds()) && StringUtils.isNotBlank(item.getNeedIncomePersons())) {
                        item.setLeaderUser(CommonUtils.getUserObj(item.getNeedIncomePersonIds(), item.getNeedIncomePersons(), DeptOrUserConstant.USER_LIST));
                    }
                } else {
                    item.setLeaderUser(CommonUtils.getUserObj(item.getDeptOrUserId() + "", item.getDeptOrUserName(), DeptOrUserConstant.USER_LIST));
                }

            });
        }
        return nonIncomePersonPage;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean removeNonIncomePerson(NonAndSpecialDelDTO nonAndSpecialDelDTO) {
        removeById(nonAndSpecialDelDTO.getId());
        imputationDeptUnitService.setIncomePerson(nonAndSpecialDelDTO.getCycle());
        return true;
    }
}
