package com.hscloud.hs.cost.account.service.impl.second;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskDetailItemWorkMapper;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskMapper;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskProjectDetailMapper;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.service.second.IGrantUnitService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskDetailItemWorkService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务科室二次分配工作量系数 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnitTaskDetailItemWorkService extends ServiceImpl<UnitTaskDetailItemWorkMapper, UnitTaskDetailItemWork> implements IUnitTaskDetailItemWorkService {

    private final IUnitTaskUserService unitTaskUserService;
    private final UnitTaskMapper unitTaskMapper;
    private final IGrantUnitService grantUnitService;
    private final UnitTaskProjectDetailMapper unitTaskProjectDetailMapper;
    @Lazy
    @Autowired
    private UnitTaskProjectService unitTaskProjectService;


    @Override
    public List<UnitTaskDetailItemWork> listByDetailId(Long detailId) {
        return super.list(Wrappers.<UnitTaskDetailItemWork>lambdaQuery().eq(UnitTaskDetailItemWork::getUnitTaskProjectDetailId, detailId));
    }

    @Override
    public void delByProjectId(List<Long> projectIds) {
        super.remove(Wrappers.<UnitTaskDetailItemWork>lambdaQuery().in(UnitTaskDetailItemWork::getUnitTaskProjectId, projectIds));
    }

    @Override
    public void initUserData(Long unitTaskId, UnitTaskProjectDetail unitTaskProjectDetail, List<UnitTaskUser> userList) {
        // 人员list
        if (userList == null) {
            userList = unitTaskUserService.listByTaskId(unitTaskId);
        }
        Long projectId = unitTaskProjectDetail.getUnitTaskProjectId();
        UnitTaskProject unitTaskProject = unitTaskProjectService.getById(projectId);

        this.addByProgDetail(unitTaskProject, unitTaskProjectDetail, userList);
    }

    @Override
    public void delByDetailId(List<Long> detailIds) {
        if (CollUtil.isNotEmpty(detailIds)) {
            this.remove(Wrappers.<UnitTaskDetailItemWork>lambdaQuery().in(UnitTaskDetailItemWork::getUnitTaskProjectDetailId, detailIds));
        }
    }

    private void addByProgDetail(UnitTaskProject unitTaskProject, UnitTaskProjectDetail unitTaskProjectDetail, List<UnitTaskUser> userList) {
        List<UnitTaskDetailItemWork> addList = new ArrayList<>();
        String userIds = userList.stream().map(UnitTaskUser::getUserId).collect(Collectors.joining(","));

        UnitTask unitTask = unitTaskMapper.selectById(unitTaskProject.getUnitTaskId());
        String cycle = unitTask.getCycle();
        GrantUnit grantUnit = grantUnitService.getById(unitTask.getGrantUnitId());
        String deptIds = grantUnit.getKsUnitIds();

        // 继承上一次
        List<UnitTaskDetailItemWork> userItemValueList = this.getLastItemValue(unitTaskProjectDetail.getId(), unitTaskProjectDetail.getProgProjectDetailId());
        Map<String, UnitTaskDetailItemWork> lastItemValueMap = userItemValueList.stream().collect(Collectors.toMap(UnitTaskDetailItemWork::getEmpCode, item -> item, (v1, v2) -> v2));

        for (UnitTaskUser unitTaskUser : userList) {// 循环人员
            String empCode = unitTaskUser.getEmpCode();
            String userId = unitTaskUser.getUserId();
            UnitTaskDetailItemWork unitTaskDetailItemWork = new UnitTaskDetailItemWork();// 每人一笔
            BeanUtils.copyProperties(unitTaskProjectDetail, unitTaskDetailItemWork);

            unitTaskDetailItemWork.setId(null);
            unitTaskDetailItemWork.setEmpCode(unitTaskUser.getEmpCode());
            unitTaskDetailItemWork.setUnitTaskProjectDetailId(unitTaskProjectDetail.getId());
            unitTaskDetailItemWork.setUnitTaskProjectId(unitTaskProject.getId());
            unitTaskDetailItemWork.setUnitTaskId(unitTaskProject.getUnitTaskId());

            UnitTaskDetailItemWork lastItem = lastItemValueMap.get(empCode);
            if (lastItem != null) {
                unitTaskDetailItemWork.setExamPoint(lastItem.getExamPoint());
                unitTaskDetailItemWork.setWorkRate(lastItem.getWorkRate());
            }
            addList.add(unitTaskDetailItemWork);
        }
        if (!addList.isEmpty()) {
            this.saveBatch(addList);
        }
        if (!addList.isEmpty()) {
            this.updateBatchById(addList);
        }

    }

    private List<UnitTaskDetailItemWork> getLastItemValue(Long detailId, Long progProjectDetailId) {
        // 查询最近一笔 progDetailId 所对应的 unitDetailId
        UnitTaskProjectDetail unitTaskProjectDetail = unitTaskProjectDetailMapper.selectOne(Wrappers.<UnitTaskProjectDetail>lambdaQuery()
                .eq(UnitTaskProjectDetail::getProgProjectDetailId, progProjectDetailId)
                .ne(UnitTaskProjectDetail::getId, detailId)
                .orderByDesc(UnitTaskProjectDetail::getCreateTime)
                .last("limit 1"));
        // 查询 unitDetailId 下的 所有 progDetailItem 对应的unitItem的数据
        if (unitTaskProjectDetail != null) {
            return this.list(Wrappers.<UnitTaskDetailItemWork>lambdaQuery()
                    .eq(UnitTaskDetailItemWork::getUnitTaskProjectDetailId, unitTaskProjectDetail.getId()));
        }
        return new ArrayList<>();
    }
}
