package com.hscloud.hs.cost.account.service.impl.second;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.mapper.second.SecondTaskMapper;
import com.hscloud.hs.cost.account.model.dto.second.SecondTaskCreateDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountTaskNew;
import com.hscloud.hs.cost.account.model.entity.base.Entity;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountTask;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.service.ICostAccountTaskNewService;
import com.hscloud.hs.cost.account.service.kpi.IKpiAccountTaskService;
import com.hscloud.hs.cost.account.service.second.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* 二次分配总任务 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SecondTaskService extends ServiceImpl<SecondTaskMapper, SecondTask> implements ISecondTaskService {

    private final IUnitTaskService unitTaskService;
    private final ICostAccountTaskNewService costAccountTaskNewService;
    private final IUnitTaskCountService unitTaskCountService;
    private final IUnitTaskProjectService unitTaskProjectService;
    private final IUnitTaskProjectDetailService unitTaskProjectDetailService;
    private final IKpiAccountTaskService kpiAccountTaskService;
    @Override
    @Transactional
    public void create(SecondTaskCreateDto taskCreateDto) {
//        SecondTask secondTaskDB = this.getByFirstId(taskCreateDto.getFirstId());
//        if(secondTaskDB != null){
//            //throw new BizException("二次分配任务已生成，请勿重复生成");
//        }

        KpiAccountTask firstTask = kpiAccountTaskService.getById(taskCreateDto.getFirstId());
        if (firstTask == null){
            throw new RuntimeException("未找到对应一次分配任务");
        }
        //需要可以重复下发
        firstTask.setSendFlag("Y");
        firstTask.setSendDate(new Date());
        firstTask.setSendLog("");
        firstTask.setSendGrantUnitIds(taskCreateDto.getGrantUnitIds());
        firstTask.setSendGrantUnitNames(taskCreateDto.getGrantUnitNames());
        kpiAccountTaskService.updateById(firstTask);

        String cycle = firstTask.getPeriod()+"";
        cycle = cycle.substring(0,4)+"-"+ cycle.substring(4, 6);

        //删除同一周期的二次分配总任务、科室任务,相关发放单元下的任务
        this.delByCycle(cycle,taskCreateDto.getGrantUnitIds());

        //生成二次分配总任务 todo 数据来自一次分配
        SecondTask secondTask = new SecondTask();
        secondTask.setName(firstTask.getAccountTaskName()+" 二次分配");
        secondTask.setFirstTaskId(taskCreateDto.getFirstId());
        secondTask.setStartTime(LocalDateTime.now());
        secondTask.setStatus("UNDERWAY");
        secondTask.setCycle(cycle);

        SecondTask secondTaskDB = this.getOne(Wrappers.<SecondTask>lambdaQuery().eq(SecondTask::getCycle,cycle));
        if(Objects.nonNull(secondTaskDB)){
            secondTask.setId(secondTaskDB.getId());
            this.updateById(secondTask);
        }else {
            this.save(secondTask);
        }
        //按发放单元生成各 发放单元任务
        unitTaskService.createBySecondTaskId(secondTask,taskCreateDto.getGrantUnitIds());

    }

    private void delByCycle(String cycle,String grantUnitIds) {
        List<SecondTask> secondTaskList = this.list(Wrappers.<SecondTask>lambdaQuery().eq(SecondTask::getCycle,cycle));
        List<String> grantUnitIdList = Arrays.asList(grantUnitIds.split(","));
        //删发放单元任务,删除指定发放单元信息
        for (SecondTask secondTask : secondTaskList){
            List<UnitTask> taskList = unitTaskService.list(Wrappers.<UnitTask>lambdaQuery().eq(UnitTask::getSecondTaskId, secondTask.getId())
                    .in(!CollectionUtils.isEmpty(grantUnitIdList), UnitTask::getGrantUnitId, grantUnitIdList));
            if(CollectionUtils.isEmpty(taskList)){
                continue;
            }
            List<Long> taskIds = taskList.stream().map(UnitTask::getId).collect(Collectors.toList());
            //删除unitTask
            unitTaskService.removeBatchByIds(taskIds);
            //删除unitTaskProject
            List<UnitTaskProject> unitTaskProjects = unitTaskProjectService.listByUnitTaskIds(taskIds);
            if(CollUtil.isNotEmpty(unitTaskProjects)){
                List<Long> collect = unitTaskProjects.stream().map(UnitTaskProject::getId).collect(Collectors.toList());
                unitTaskProjectService.removeBatchByIds(collect);
                unitTaskProjectDetailService.removeByTaskProjectIds(collect);
            }
            //删除taskCount
            unitTaskCountService.remove(Wrappers.<UnitTaskCount>lambdaQuery().in(UnitTaskCount::getUnitTaskId,taskIds));

        }
        //删二次分配总任务
        //this.removeBatchByIds(secondTaskList);
    }

    @Override
    @Transactional
    public void finishCheck(Long secondTaskId) {
        boolean exist = unitTaskService.exists(Wrappers.<UnitTask>lambdaQuery().eq(UnitTask::getSecondTaskId,secondTaskId)
                .ne(UnitTask::getIfFinish,"1"));
        if(!exist){
           this.update(null,Wrappers.<SecondTask>lambdaUpdate().eq(SecondTask::getId,secondTaskId)
                           .set(SecondTask::getEndTime,LocalDateTime.now())
                   .set(SecondTask::getStatus,"FINISHED"));
        }
    }

    @Override
    public Boolean ifPublished(Long firstId) {
        CostAccountTaskNew firstTask = costAccountTaskNewService.getById(firstId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        String cycle = firstTask.getAccountStartTime().format(formatter);
        if(this.exists(Wrappers.<SecondTask>lambdaQuery().eq(SecondTask::getCycle,cycle))){
            return true;
        }
        return false;
    }

    @Override
    public IPage<SecondTask> taskPage(PageRequest<SecondTask> pr)
    {
        Page<SecondTask> rt = this.page(pr.getPage(), pr.getWrapper().orderByDesc("start_time"));
        List<Long> list = Linq.of(rt.getRecords()).select(Entity::getId).toList();
        if(!list.isEmpty())
        {
            List<SecondTask> secondTasks = this.baseMapper.sumTask(list);
            Linq.of(rt.getRecords()).forEach(t -> {
                SecondTask secondTask = Linq.of(secondTasks).firstOrDefault(t1 -> t1.getId().equals(t.getId()));
                if(secondTask != null)
                {
                    t.setKsAmt(secondTask.getKsAmt());
                }
            });
        }
        return rt;
    }

    private SecondTask getByFirstId(Long firstId) {
        return this.getOne(Wrappers.<SecondTask>lambdaQuery().eq(SecondTask::getFirstTaskId,firstId));
    }
}
