package com.hscloud.hs.cost.account.service.impl.second;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.second.ProjectType;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskProjectMapper;
import com.hscloud.hs.cost.account.model.dto.second.SecStartDataDTO;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskDetailItemVo;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskProjectDetailVo;
import com.hscloud.hs.cost.account.service.impl.second.async.SecRedisService;
import com.hscloud.hs.cost.account.service.second.*;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
* 任务核算指标 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnitTaskProjectService extends ServiceImpl<UnitTaskProjectMapper, UnitTaskProject> implements IUnitTaskProjectService {

    private final IProgProjectService progProjectService;

    private final IUnitTaskProjectDetailService unitTaskProjectDetailService;

    private final IUnitTaskDetailItemService unitTaskDetailItemService;
    private final IUnitTaskDetailItemWorkService unitTaskDetailItemWorkService;

    private final IUnitTaskUserService unitTaskUserService;

    private final IUnitTaskProjectCountService unitTaskProjectCountService;
    private final IUnitTaskDetailCountService unitTaskDetailCountService;
    private final SecRedisService secRedisService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createByProg(UnitTask unitTask, Programme programme) {
        String cycle = unitTask.getCycle();
        Long unitTaskId = unitTask.getId();
        System.out.println("unitTaskId=="+unitTaskId);

        List<ProgProject> progProjectList = progProjectService.listByPidCache(cycle,programme.getId());


        this.createByProgProjectList(unitTask,progProjectList);
    }

    @Override
    public void initUserData(UnitTask unitTask, List<UnitTaskUser> userList) {
        Long unitTaskId = unitTask.getId();
        //查出 方案下的所有project
        List<UnitTaskProject> projectList = this.listByUnitTask(unitTask.getId());
        for (UnitTaskProject unitTaskProject : projectList){
            ProgProject progProject = progProjectService.getById(unitTaskProject.getProgProjectId());
            String projectType = CommonUtils.getDicVal(unitTaskProject.getProjectType());
            if(ProjectType.erci.toString().equals(projectType)){ //科室二次分配新增 projectDetailItem
                List<UnitTaskProjectDetail> detailList = unitTaskProjectDetailService.list(Wrappers.<UnitTaskProjectDetail>lambdaQuery().eq(UnitTaskProjectDetail::getUnitTaskProjectId,unitTaskProject.getId()));
                for (UnitTaskProjectDetail unitTaskProjectDetail : detailList){
                    unitTaskDetailItemService.initUserData(unitTaskId,unitTaskProjectDetail,userList);
                    unitTaskDetailItemWorkService.initUserData(unitTaskId, unitTaskProjectDetail, userList);
                }
            }else{//其他类型  为user增出 projectDetail
                unitTaskProjectDetailService.initUserData(unitTaskId,unitTaskProject,progProject,userList);
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncByUnitTask(UnitTask unitTask) {
        //progProject 和 taskProject 对比，根据增删改分类
        Long programmeId = unitTask.getProgrammeId();
        Long taskId = unitTask.getId();
        String cycle = unitTask.getCycle();
        List<ProgProject> progProjectList = progProjectService.list(Wrappers.<ProgProject>lambdaQuery().eq(ProgProject::getProgrammeId,programmeId));
        List<UnitTaskProject> taskProjectList = this.list(Wrappers.<UnitTaskProject>lambdaQuery().eq(UnitTaskProject::getUnitTaskId,taskId));

        //新增的集合
        List<ProgProject> addList = progProjectList.stream()
                .filter(progProject -> taskProjectList.stream().map(UnitTaskProject::getProgProjectId).noneMatch(progProjectId -> Objects.equals(progProjectId,progProject.getId())))
                .collect(Collectors.toList());
        this.createByProgProjectList(unitTask, addList);

        //删除的集合
        List<UnitTaskProject> delList = taskProjectList.stream()
                .filter(unitTaskProject -> progProjectList.stream().map(ProgProject::getId).noneMatch(id -> Objects.equals(id,unitTaskProject.getProgProjectId())))
                .collect(Collectors.toList());
        this.delByProjectId(delList);

        //修改的集合 往下处理 detail
        List<UnitTaskProject> updateList = taskProjectList.stream()
                .filter(unitTaskProject -> delList.stream().noneMatch(delProject -> Objects.equals(delProject.getId(), unitTaskProject.getId())))
                .collect(Collectors.toList());
        this.updateByProgProjectList(progProjectList,updateList);
        updateList.forEach(o->unitTaskProjectDetailService.syncByProject(cycle,o));

        //重新查询taskproject，根据ProgProject进行排序
        List<UnitTaskProject> taskProjectListNew = this.list(Wrappers.<UnitTaskProject>lambdaQuery().eq(UnitTaskProject::getUnitTaskId,taskId));
        this.orderByProgPrject(progProjectList,taskProjectListNew);
        // TODO paixu

        //project name和sortnum同步给count
        unitTaskProjectCountService.syncProjectName(taskProjectListNew);


        //重新算钱
        unitTaskProjectCountService.doCount(taskId);
    }

    private void updateByProgProjectList(List<ProgProject> progList, List<UnitTaskProject> updateList) {
        Map<Long,ProgProject> progProjectMap = progList.stream().collect(Collectors.toMap(ProgProject::getId,item->item, (v1, v2) -> v2));
        for (UnitTaskProject progProject : updateList){
            Long commonId = progProject.getProgProjectId();
            ProgProject comProject = progProjectMap.get(commonId);
            progProject.setName(comProject.getName());
            progProject.setSortNum(comProject.getSortNum());
            progProject.setReservedDecimal(comProject.getReservedDecimal());
            progProject.setCarryRule(comProject.getCarryRule());
        }
        if(!updateList.isEmpty()){
            this.updateBatchById(updateList);
        }
    }

    private void orderByProgPrject(List<ProgProject> progProjectList, List<UnitTaskProject> taskProjectListNew) {
        //方案里的 id 对应排序号
        Map<Long,Integer> idSortNumMap = progProjectList.stream().collect(Collectors.toMap(ProgProject::getId,ProgProject::getSortNum, (v1, v2) -> v2));
        taskProjectListNew.forEach(item->item.setSortNum(idSortNumMap.get(item.getProgProjectId())));
        this.updateBatchById(taskProjectListNew);
    }

    private void delByProjectId(List<UnitTaskProject> delList) {
        if(delList == null || delList.isEmpty()){
            return;
        }
        List<Long> projectIds = delList.stream().map(UnitTaskProject::getId).collect(Collectors.toList());
        this.remove(Wrappers.<UnitTaskProject>lambdaQuery().in(UnitTaskProject::getId,projectIds));
        unitTaskProjectDetailService.delByProjectId(projectIds);
    }

    @Override
    public void createByProgProjectList(UnitTask unitTask, List<ProgProject> progProjectList) {
        if(progProjectList == null || progProjectList.isEmpty()){
            return;
        }
        Long unitTaskId = unitTask.getId();
        String cycle = unitTask.getCycle();
        //Long nextProjectId = null;
        //为第一个project 设置beforeAmt
        int i = 0;
        for (ProgProject progProject : progProjectList){
            UnitTaskProject unitTaskProject = new UnitTaskProject();
            BeanUtils.copyProperties(progProject, unitTaskProject);

            if(i == 0){
                unitTaskProject.setBeforeAmt(unitTask.getKsAmt());
                i++;
            }
            unitTaskProject.setId(null);
            unitTaskProject.setUnitTaskId(unitTaskId);
            unitTaskProject.setProgProjectId(progProject.getId());
            //unitTaskProject.setNextProjectId(nextProjectId);
            this.save(unitTaskProject);
            //nextProjectId = unitTaskProject.getId();

            String projectType = CommonUtils.getDicVal(progProject.getProjectType());
            if(ProjectType.erci.toString().equals(projectType)){ //科室二次分配新增 projectDetail
                unitTaskProjectDetailService.createByProject(cycle,unitTaskProject,progProject);
            }else{//其他类型  为user增出 projectDetail
                unitTaskProjectDetailService.initUserData(unitTaskId,unitTaskProject,progProject,null);
            }

        }
    }

    @Override
    public List<UnitTaskProject> detailCountByUser(UnitTask unitTask, String empCode) {
        List<UnitTaskProject> projectListCache = this.listByUnitTask(unitTask.getId());
        List<UnitTaskProject> projectList = new ArrayList<>();
        for (UnitTaskProject project : projectListCache){
            UnitTaskProject project1 = new UnitTaskProject();
            BeanUtils.copyProperties(project,project1);
            projectList.add(project1);
        }

        for (UnitTaskProject project : projectList){
            String projectType = CommonUtils.getDicVal(project.getProjectType());
            if(ProjectType.danxiang.toString().equals(projectType)){
                List<UnitTaskProjectDetailVo> list = unitTaskProjectDetailService.userList(project.getId(),empCode);
                if(list != null && !list.isEmpty()){
                    project.setUnitTaskProjectDetailList(list.get(0).getDetailList());
                }
            }else if(ProjectType.pinjun.toString().equals(projectType)){

                //detail的备用字段里塞 人的detail金额
                UnitTaskProjectCount projectCount = unitTaskProjectCountService.getOne(Wrappers.<UnitTaskProjectCount>lambdaQuery()
                        .eq(UnitTaskProjectCount::getProjectId,project.getId())
                        .eq(UnitTaskProjectCount::getEmpCode,empCode));
                if(projectCount != null) {
                    project.setSegment(projectCount.getAmt());
                }
            }else if(ProjectType.erci.toString().equals(projectType)){
                List<UnitTaskProjectDetail> detailListCache = unitTaskProjectDetailService.list(Wrappers.<UnitTaskProjectDetail>lambdaQuery().eq(UnitTaskProjectDetail::getUnitTaskProjectId,project.getId()));
                List<UnitTaskProjectDetail> detailList = new ArrayList<>();
                for (UnitTaskProjectDetail detail : detailListCache){
                    UnitTaskProjectDetail detail1 = new UnitTaskProjectDetail();
                    BeanUtils.copyProperties(detail,detail1);
                    detailList.add(detail1);
                }

                project.setUnitTaskProjectDetailList(detailList);

                for (UnitTaskProjectDetail detail : detailList){
                    //detail的备用字段里塞 人的detail金额
                    UnitTaskDetailCount detailCount = unitTaskDetailCountService.getOne(Wrappers.<UnitTaskDetailCount>lambdaQuery()
                            .eq(UnitTaskDetailCount::getDetailId,detail.getId())
                            .eq(UnitTaskDetailCount::getEmpCode,empCode));

                    if(detailCount != null) {
                        System.out.println(detailCount.getAmt());
                        detail.setSegment(detailCount.getAmt());
                        System.out.println(detail.getSegment());
                    }
                    List<UnitTaskDetailItemVo> list = unitTaskDetailItemService.userList(detail.getId(),empCode);
                    if(list != null && !list.isEmpty()){
                        detail.setUnitTaskDetailItemList(list.get(0).getItemList());
                    }
                }
            }
        }
        return projectList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByUserId(UnitTaskUser unitTaskUser) {
        Long unitTaskId = unitTaskUser.getUnitTaskId();
        String empCode = unitTaskUser.getEmpCode();
        //Long userId = Long.parseLong(unitTaskUser.getUserId());

        //获得所有 detail
        List<UnitTaskProjectDetail> detailList = unitTaskProjectDetailService.listByUnitTask(unitTaskId);
        List<Long> detailIds = detailList.stream().map(UnitTaskProjectDetail::getId).collect(Collectors.toList());
        if (!detailIds.isEmpty()){
            //根据detail 删除item
            unitTaskDetailItemService.remove(Wrappers.<UnitTaskDetailItem>lambdaQuery()
                    .in(UnitTaskDetailItem::getUnitTaskProjectDetailId,detailIds)
                    .eq(UnitTaskDetailItem::getEmpCode,empCode));
            // 根据detail 删除item
            unitTaskDetailItemWorkService.remove(Wrappers.<UnitTaskDetailItemWork>lambdaQuery()
                    .in(UnitTaskDetailItemWork::getUnitTaskProjectDetailId, detailIds)
                    .eq(UnitTaskDetailItemWork::getEmpCode, empCode));
        }
        //删除detail
        List<UnitTaskProject> projectList = this.listByUnitTask(unitTaskId);
        if(projectList.isEmpty()){
            return;
        }
        List<Long> projectIds = projectList.stream().map(UnitTaskProject::getId).collect(Collectors.toList());
        if (!projectIds.isEmpty()) {
            unitTaskProjectDetailService.remove(Wrappers.<UnitTaskProjectDetail>lambdaQuery()
                    .in(UnitTaskProjectDetail::getUnitTaskProjectId, projectIds)
                    .eq(UnitTaskProjectDetail::getEmpCode, empCode));
        }

    }


    @Override
    public List<UnitTaskProject> listByUnitTask(Long unitTaskId) {
        return this.list(Wrappers.<UnitTaskProject>lambdaQuery().eq(UnitTaskProject::getUnitTaskId,unitTaskId).orderByAsc(UnitTaskProject::getSortNum));
    }

    @Override
    public List<UnitTaskProject> listByUnitTaskIds(List<Long> taskIds) {
        return this.list(Wrappers.<UnitTaskProject>lambdaQuery().in(UnitTaskProject::getUnitTaskId,taskIds).orderByAsc(UnitTaskProject::getSortNum));
    }
}
