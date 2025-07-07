package com.hscloud.hs.cost.account.service.impl.second;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.constant.enums.second.ActionType;
import com.hscloud.hs.cost.account.constant.enums.second.ProjectType;
import com.hscloud.hs.cost.account.mapper.second.*;
import com.hscloud.hs.cost.account.model.dto.second.ProgProjectDetailSaveDTO;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.service.impl.second.async.SecRedisService;
import com.hscloud.hs.cost.account.service.second.IProgDetailItemService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskDetailItemService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskProjectDetailService;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.service.second.IProgProjectDetailService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* 核算指标明细 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgProjectDetailService extends ServiceImpl<ProgProjectDetailMapper, ProgProjectDetail> implements IProgProjectDetailService {

    private final UnitTaskProjectMapper unitTaskProjectMapper;
    private final UnitTaskMapper unitTaskMapper;
    private final UnitTaskUserMapper unitTaskUserMapper;
    private final UnitTaskProjectDetailMapper unitTaskProjectDetailMapper;
    @Lazy
    @Autowired
    private IUnitTaskProjectDetailService unitTaskProjectDetailService;

    private final IProgDetailItemService progDetailItemService;
    private final SecRedisService secRedisService;

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void saveByUnitTaskProject(ProgProjectDetailSaveDTO progProjectDetailSaveDTO) {
//        Long unitTaskProjectId = progProjectDetailSaveDTO.getUnitTaskProjectId();
//        UnitTaskProject unitTaskProject = unitTaskProjectMapper.selectById(unitTaskProjectId);
//        Long unitTaskId = unitTaskProject.getUnitTaskId();
//        UnitTask unitTask = unitTaskMapper.selectById(unitTaskId);
//
//        List<ProgProjectDetail> delList = new ArrayList<>();
//        List<ProgProjectDetail> addList = new ArrayList<>();
//        for (ProgProjectDetail progProjectDetail : progProjectDetailSaveDTO.getDetailList()) {
//            String actionType = progProjectDetail.getActionType();
//            //新增的项目
//            if(ActionType.add.toString().equals(actionType)){
//                addList.add(progProjectDetail);
//            }else if(ActionType.edit.toString().equals(actionType)){//修改的项目
//                addList.add(progProjectDetail);
//                delList.add(progProjectDetail);
//            }else  if(ActionType.del.toString().equals(actionType)){//删除的项目
//                delList.add(progProjectDetail);
//            }
//        }
//
//        //先处理删除
//        if(!delList.isEmpty()) {
//            this.delByUnitTaskProject(unitTask, unitTaskProject, delList);
//        }
//        //处理新增
//        if(!addList.isEmpty()) {
//            this.addByUnitTaskProject(unitTask, unitTaskProject, addList);
//        }
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delErciById(Long progDetailtId) {
        this.removeById(progDetailtId);
        progDetailItemService.remove(Wrappers.<ProgDetailItem>lambdaQuery().eq(ProgDetailItem::getProgProjectDetailId,progDetailtId));
    }

    @Override
    public void syncByProject(ProgProject progProject) {
        //comDetail 和 progDetail 对比，根据增删改分类
        Long comProjectId = progProject.getCommonId();
        Long progProjectId = progProject.getId();
        List<ProgProjectDetail> comDetailList = this.listByProjectId(comProjectId);
        List<ProgProjectDetail> progDetailList = this.listByProjectId(progProjectId);

        //新增的集合
        List<ProgProjectDetail> addList = comDetailList.stream()
                .filter(comDetail -> progDetailList.stream().map(ProgProjectDetail::getCommonId).noneMatch(commonId -> Objects.equals(commonId,comDetail.getId())))
                .collect(Collectors.toList());
        this.createByComDetailList(progProject, addList);

        //删除的集合
        List<ProgProjectDetail> delList = progDetailList.stream()
                .filter(progDetail -> comDetailList.stream().map(ProgProjectDetail::getId).noneMatch(id -> Objects.equals(id,progDetail.getCommonId())))
                .collect(Collectors.toList());
        this.delByDetailList(delList);

        //修改的集合 往下处理 detail
        List<ProgProjectDetail> updateList = progDetailList.stream()
                .filter(progDetail -> delList.stream().noneMatch(delDetail -> Objects.equals(delDetail.getId(), progDetail.getId())))
                .collect(Collectors.toList());
        this.updateByComDetailList(comDetailList,updateList);
        updateList.forEach(progDetailItemService::syncByDetail);
    }

    private void updateByComDetailList(List<ProgProjectDetail> comList, List<ProgProjectDetail> updateList) {
        Map<Long,ProgProjectDetail> comMap = comList.stream().collect(Collectors.toMap(ProgProjectDetail::getId, item->item, (v1, v2) -> v2));
        for (ProgProjectDetail detail : updateList){
            Long commonId = detail.getCommonId();
            ProgProjectDetail comDetail = comMap.get(commonId);
            detail.setName(comDetail.getName());
            detail.setModeType(comDetail.getModeType());
            detail.setPriceValue(comDetail.getPriceValue());
            detail.setInputType(comDetail.getInputType());
            detail.setAccountItemId(comDetail.getAccountItemId());
            detail.setAccountItemType(comDetail.getAccountItemType());
            detail.setAccountItemCode(comDetail.getAccountItemCode());
            detail.setAccountItemName(comDetail.getAccountItemName());
            detail.setIfExtendLast(comDetail.getIfExtendLast());
            detail.setErciRate(comDetail.getErciRate());
            detail.setIfCareWorkdays(comDetail.getIfCareWorkdays());
            detail.setIfParentItemValueAdd(comDetail.getIfParentItemValueAdd());
            detail.setIfItemValueAdd(comDetail.getIfItemValueAdd());
            detail.setSortNum(comDetail.getSortNum());
        }
        if(!updateList.isEmpty()){
            this.updateBatchById(updateList);
        }
    }

    private void createByComDetailList(ProgProject progProject, List<ProgProjectDetail> comProjectList) {
        Long progProjectId = progProject.getId();
        String projectType = CommonUtils.getDicVal(progProject.getProjectType());
        this.createByProgDetailList(progProjectId,comProjectList,projectType);
    }

    private List<ProgProjectDetail> listByProjectId(Long progProjectId) {
        return this.list(Wrappers.<ProgProjectDetail>lambdaQuery().eq(ProgProjectDetail::getProgProjectId,progProjectId));
    }

    @Override
    public void createByProject(ProgProject progProject, ProgProject comProject) {
        Long progProjectId = progProject.getId();
        List<ProgProjectDetail> comProjectDetailList = this.list(Wrappers.<ProgProjectDetail>lambdaQuery().eq(ProgProjectDetail::getProgProjectId, comProject.getId()));

        String projectType = CommonUtils.getDicVal(progProject.getProjectType());
        this.createByProgDetailList(progProjectId,comProjectDetailList,projectType);

    }

    @Override
    public void delByDetailList(List<ProgProjectDetail> delList) {
        if(delList == null || delList.isEmpty()){
            return;
        }
        List<Long> delIds = delList.stream().map(ProgProjectDetail::getId).collect(Collectors.toList());
        this.remove(Wrappers.<ProgProjectDetail>lambdaQuery().in(ProgProjectDetail::getId,delIds));

        List<ProgDetailItem> progDetailItemList = progDetailItemService.list(Wrappers.<ProgDetailItem>lambdaQuery()
                .in(ProgDetailItem::getProgProjectDetailId,delIds));
        progDetailItemService.delByItemList(progDetailItemList);
    }

    @Override
    public List<ProgProjectDetail> listByPidCache(String cycle, Long projectId) {
        List<ProgProjectDetail> progProjectDetailAll = secRedisService.detailList(cycle);
        return progProjectDetailAll.stream().filter(progProjectDetail -> progProjectDetail.getProgProjectId().equals(projectId)).collect(Collectors.toList());
    }

    private void createByProgDetailList(Long progProjectId, List<ProgProjectDetail> comProjectDetailList, String projectType) {
        for (ProgProjectDetail comProjectDetail : comProjectDetailList) {
            ProgProjectDetail progProjectDetail = new ProgProjectDetail();
            BeanUtils.copyProperties(comProjectDetail, progProjectDetail);

            progProjectDetail.setId(null);
            progProjectDetail.setProgProjectId(progProjectId);
            progProjectDetail.setCommonId(comProjectDetail.getId());
            this.save(progProjectDetail);

            if (ProjectType.erci.toString().equals(projectType)) { //科室二次分配新增 为progProjectDetail 增出 detailItem
                progDetailItemService.createByDetail(progProjectDetail, comProjectDetail);
            }
        }
    }

    private void addByUnitTaskProject(UnitTask unitTask, UnitTaskProject unitTaskProject, List<ProgProjectDetail> addList) {
        Long unitTaskId = unitTask.getId();
        //新增方案detail
        this.saveBatch(addList);

        //新增任务detail
        List<UnitTaskUser> userList = unitTaskUserMapper.selectList(Wrappers.<UnitTaskUser>lambdaQuery().eq(UnitTaskUser::getUnitTaskId,unitTaskId));
        unitTaskProjectDetailService.addByProgDetailList(unitTaskProject,userList,addList);

    }

    private void delByUnitTaskProject(UnitTask unitTask, UnitTaskProject unitTaskProject, List<ProgProjectDetail> progDelList) {
        Long unitTaskProjectId = unitTaskProject.getId();
        //删除任务detail
        List<Long> progDetailIdList = progDelList.stream()
                .map(ProgProjectDetail::getId)
                .collect(Collectors.toList());
        unitTaskProjectDetailMapper.delete(Wrappers.<UnitTaskProjectDetail>lambdaQuery().eq(UnitTaskProjectDetail::getUnitTaskProjectId,unitTaskProjectId).in(UnitTaskProjectDetail::getProgProjectDetailId,progDetailIdList));

        //删除方案detail
        this.removeBatchByIds(progDelList);
    }
}
