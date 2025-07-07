package com.hscloud.hs.cost.account.service.impl.second;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.model.entity.second.ProgProjectDetail;
import com.hscloud.hs.cost.account.model.entity.second.Programme;
import com.hscloud.hs.cost.account.service.impl.second.async.SecRedisService;
import com.hscloud.hs.cost.account.service.second.IProgProjectDetailService;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.second.ProgProjectMapper;
import com.hscloud.hs.cost.account.model.entity.second.ProgProject;
import com.hscloud.hs.cost.account.service.second.IProgProjectService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* 核算指标 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgProjectService extends ServiceImpl<ProgProjectMapper, ProgProject> implements IProgProjectService {

    private final IProgProjectDetailService progProjectDetailService;
    private final SecRedisService secRedisService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncByProgramme(Programme programme) {
        //comProject 和 progProject 对比，根据增删改分类
        Long comProgrammeId = programme.getParentId();
        Long programmeId = programme.getId();

        List<ProgProject> comProjectList = this.listByProgId(comProgrammeId);
        List<ProgProject> progProjectList = this.listByProgId(programmeId);

        //新增的集合
        List<ProgProject> addList = comProjectList.stream()
                .filter(comProject -> progProjectList.stream().map(ProgProject::getCommonId).noneMatch(commonId -> Objects.equals(commonId, comProject.getId())))
                .collect(Collectors.toList());
        this.createByComProjectList(programme, addList);

        //删除的集合
        List<ProgProject> delList = progProjectList.stream()
                .filter(progProject -> comProjectList.stream().map(ProgProject::getId).noneMatch(id -> Objects.equals(id,progProject.getCommonId())))
                .collect(Collectors.toList());
        this.delByProjectList(delList);

        //修改的集合 往下处理 detail
        List<ProgProject> updateList = progProjectList.stream()
                .filter(progProject -> delList.stream().noneMatch(delProject -> Objects.equals(delProject.getId(), progProject.getId())))
                .collect(Collectors.toList());
        this.updateByComProjectList(comProjectList,updateList);
        updateList.forEach(progProjectDetailService::syncByProject);

        //重新查询 progProject，根据 comProject 进行排序
        List<ProgProject> progProjectListNew = this.listByProgId(programmeId);
        this.orderByProgPrject(comProjectList,progProjectListNew);
    }

    private void updateByComProjectList(List<ProgProject> comProjectList, List<ProgProject> updateList) {
        Map<Long,ProgProject> comProjectMap = comProjectList.stream().collect(Collectors.toMap(ProgProject::getId,item->item, (v1, v2) -> v2));
        for (ProgProject progProject : updateList){
            Long commonId = progProject.getCommonId();
            ProgProject comProject = comProjectMap.get(commonId);
            progProject.setSortNum(comProject.getSortNum());
            progProject.setName(comProject.getName());
            progProject.setReservedDecimal(comProject.getReservedDecimal());
            progProject.setCarryRule(comProject.getCarryRule());
        }
        if(!updateList.isEmpty()){
            this.updateBatchById(updateList);
        }
    }

    private void orderByProgPrject(List<ProgProject> comProjectList, List<ProgProject> progProjectListNew) {
        //方案里的 id 对应排序号
        Map<Long,Integer> idSortNumMap = comProjectList.stream().collect(Collectors.toMap(ProgProject::getId,ProgProject::getSortNum, (v1, v2) -> v2));
        progProjectListNew.forEach(item->item.setSortNum(idSortNumMap.get(item.getCommonId())));
        this.updateBatchById(progProjectListNew);
    }

    @Override
    public void delByProjectList(List<ProgProject> delList) {
        if(delList == null || delList.isEmpty()){
            return;
        }
        List<Long> delIds = delList.stream().map(ProgProject::getId).collect(Collectors.toList());
        this.remove(Wrappers.<ProgProject>lambdaQuery().in(ProgProject::getId,delIds));

        List<ProgProjectDetail> progProjectDetailList = progProjectDetailService.list(Wrappers.<ProgProjectDetail>lambdaQuery()
                .in(ProgProjectDetail::getProgProjectId,delIds));
        progProjectDetailService.delByDetailList(progProjectDetailList);
    }

    @Override
    public List<ProgProject> listByPidCache(String cycle, Long programmeId) {
        List<ProgProject> progProjectAll = secRedisService.projectList(cycle);
        return progProjectAll.stream()
                .filter(progProject -> progProject.getProgrammeId().equals(programmeId))
                .sorted(Comparator.nullsFirst(Comparator.comparing(ProgProject::getSortNum)))
                .collect(Collectors.toList());
    }

    private void createByComProjectList(Programme programme, List<ProgProject> comProjectList) {
        if(comProjectList == null || comProjectList.isEmpty()){
            return;
        }
        Long programmeId = programme.getId();

        for (ProgProject comProject : comProjectList){
            ProgProject progProject = new ProgProject();
            BeanUtils.copyProperties(comProject, progProject);

            progProject.setId(null);
            progProject.setProgrammeId(programmeId);
            progProject.setCommonId(comProject.getId());
            this.save(progProject);

            progProjectDetailService.createByProject(progProject,comProject);

        }
    }

    private List<ProgProject> listByProgId(Long programmeId) {
        return this.list(Wrappers.<ProgProject>lambdaQuery().eq(ProgProject::getProgrammeId,programmeId));
    }
}
