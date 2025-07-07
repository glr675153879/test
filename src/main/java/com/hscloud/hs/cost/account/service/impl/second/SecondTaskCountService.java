package com.hscloud.hs.cost.account.service.impl.second;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.constant.enums.SecondDistributionTaskStatusEnum;
import com.hscloud.hs.cost.account.mapper.second.*;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.model.vo.second.ProgrammeInfoVo;
import com.hscloud.hs.cost.account.model.vo.second.SecondDetailCountVo;
import com.hscloud.hs.cost.account.service.second.IProgrammeService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskProjectService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.service.second.ISecondTaskCountService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
* 二次分配结果按人汇总 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SecondTaskCountService extends ServiceImpl<SecondTaskCountMapper, SecondTaskCount> implements ISecondTaskCountService {
    private final UnitTaskCountMapper unitTaskCountMapper;
    private final UnitTaskUserMapper unitTaskUserMapper;
    private final UnitTaskMapper unitTaskMapper;
    private final IProgrammeService programmeService;
    @Lazy
    @Autowired
    private IUnitTaskProjectService unitTaskProjectService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doCount(Long secondTaskId, Long unitTaskId) {
        try{
            //改为 只汇总 提交 和 审批通过 的task
            if(secondTaskId == null || unitTaskId == null){
                return;
            }
            log.info("secondTaskCount开始:"+secondTaskId+","+unitTaskId);
            //unitTaskId 下的所有人
            List<UnitTaskCount> unitTaskCountList = unitTaskCountMapper.selectList(Wrappers.<UnitTaskCount>lambdaQuery()
                    .eq(UnitTaskCount::getSecondTaskId,secondTaskId)
                    .eq(UnitTaskCount::getUnitTaskId,unitTaskId)
            );
            log.info("unitTaskCountList:"+unitTaskCountList.size());
            Set<String> empCodes = unitTaskCountList.stream().map(UnitTaskCount::getEmpCode).collect(Collectors.toSet());
            //再获得 这些人的UnitTaskCount
            unitTaskCountList = unitTaskCountMapper.selectList(Wrappers.<UnitTaskCount>lambdaQuery()
                            .eq(UnitTaskCount::getSecondTaskId,secondTaskId)
                            .in(UnitTaskCount::getEmpCode,empCodes));
            log.info("unitTaskCountList2:"+unitTaskCountList.size());
            Set<Long> taskIds = unitTaskCountList.stream().map(UnitTaskCount::getUnitTaskId).collect(Collectors.toSet());
            List<UnitTask> unitTaskList = unitTaskMapper.selectBatchIds(taskIds);
            Map<Long,UnitTask> unitTaskMap = unitTaskList.stream().collect(Collectors.toMap(UnitTask::getId,Function.identity(),(v1, v2) -> v2));

            //所有SecondTaskCount
            List<SecondTaskCount> secondTaskCountList = this.list(Wrappers.<SecondTaskCount>lambdaQuery()
                    .eq(SecondTaskCount::getSecondTaskId,secondTaskId)
                    .in(SecondTaskCount::getEmpCode,empCodes)
            );
            Map<String, SecondTaskCount> secondTaskCountDBMap = secondTaskCountList.stream().collect(Collectors.toMap(SecondTaskCount::getEmpCode, Function.identity(), (v1, v2) -> v2));

            //每个人的 unitTaskCount合计,key:empcode
            Map<String, BigDecimal> unitTaskCountMap =  new HashMap<>();
            Map<String, Set<Long>> grantUnitIdsMap =  new HashMap<>();//每个工号的 grantUnitIds
            Map<String, Set<String>> grantUnitNamesMap =  new HashMap<>();

            List<SecondTaskCount> addList = new ArrayList<>();
            List<SecondTaskCount> editList = new ArrayList<>();
            Set<String> empCodeSet = new HashSet<>();//人员工号去重
            Map<String, SecondTaskCount> secondTaskCountAddMap = new HashMap<>();//新增的人员的基础信息
            for (UnitTaskCount unitTaskCount : unitTaskCountList){
                String empCode = unitTaskCount.getEmpCode();
                //Long userId = unitTaskCount.getUserId();
                UnitTask unitTask = unitTaskMap.get(unitTaskCount.getUnitTaskId());
                if(unitTask == null) continue;
                //改为 只汇总 提交 和 审批通过 的task
                if (!(SecondDistributionTaskStatusEnum.PENDING_APPROVAL.getCode().equals(unitTask.getStatus())
                        || SecondDistributionTaskStatusEnum.APPROVAL_APPROVED.getCode().equals(unitTask.getStatus()))) {
                    continue;
                }

                //金额
                BigDecimal count = unitTaskCountMap.computeIfAbsent(empCode, k -> BigDecimal.ZERO);
                count = count.add(unitTaskCount.getAmt());
                unitTaskCountMap.put(empCode,count);
                //发放单元
                Set<Long> grantUnitIdsSet = grantUnitIdsMap.computeIfAbsent(empCode, k -> new HashSet<>());
                grantUnitIdsSet.add(unitTask.getGrantUnitId());
                Set<String> grantUnitNameSet = grantUnitNamesMap.computeIfAbsent(empCode, k -> new HashSet<>());
                grantUnitNameSet.add(unitTask.getGrantUnitName());

                //记录去重工号
                empCodeSet.add(empCode);
                SecondTaskCount secondTaskCount = secondTaskCountDBMap.get(empCode);
                if(secondTaskCount == null){
                    secondTaskCount = new SecondTaskCount();
                    secondTaskCount.setSecondTaskId(unitTask.getSecondTaskId());
                    secondTaskCount.setUserId(unitTaskCount.getUserId());
                    secondTaskCount.setEmpCode(unitTaskCount.getEmpCode());
                    secondTaskCount.setEmpName(unitTaskCount.getEmpName());
                    secondTaskCountAddMap.put(empCode, secondTaskCount);
                }
            }

            for (String empCode : empCodeSet){
                SecondTaskCount secondTaskCount = secondTaskCountDBMap.get(empCode);
                if(secondTaskCount == null){
                    secondTaskCount = secondTaskCountAddMap.get(empCode);
                    addList.add(secondTaskCount);
                }else{
                    editList.add(secondTaskCount);
                }
            }

            for (SecondTaskCount secondTaskCount : addList){
                String empCode = secondTaskCount.getEmpCode();
                BigDecimal count = unitTaskCountMap.get(empCode);
                secondTaskCount.setAmt(count);
                if(grantUnitNamesMap.get(empCode) == null){
                    continue;
                }
                String grantUnitNams = String.join(",", grantUnitNamesMap.get(empCode));
                secondTaskCount.setGrantUnitNames(grantUnitNams);

                if(grantUnitIdsMap.get(empCode) != null) {
                    String grantUnitIds = String.join(",", grantUnitIdsMap.get(empCode) + "");
                    secondTaskCount.setGrantUnitIds(grantUnitIds);
                }
            }
            for (SecondTaskCount secondTaskCount : editList){
                String empCode = secondTaskCount.getEmpCode();
                BigDecimal count = unitTaskCountMap.get(empCode);
                secondTaskCount.setAmt(count);
                if(grantUnitNamesMap.get(empCode) == null){
                    continue;
                }
                String grantUnitNams = String.join(",", grantUnitNamesMap.get(empCode));
                secondTaskCount.setGrantUnitNames(grantUnitNams);

                if(grantUnitIdsMap.get(empCode) != null) {
                    String grantUnitIds = String.join(",", grantUnitIdsMap.get(empCode) + "");
                    secondTaskCount.setGrantUnitIds(grantUnitIds);
                }
            }
            if (!addList.isEmpty()){
                this.saveBatch(addList);
            }
            if (!editList.isEmpty()){
                this.updateBatchById(editList);
            }
            log.info("secondTaskCount完成:"+secondTaskId+","+unitTaskId+";addList="+addList.size()+";editList="+editList.size());
        }catch (Exception e){
            log.error("secondTaskCount异常:"+secondTaskId+","+unitTaskId,e);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doCountAll(Long secondTaskId) {
        log.info("汇总 SecondTaskCount================:"+secondTaskId);
        if(secondTaskId == null){
            return;
        }
        //所有UnitTaskCount
        List<UnitTaskCount> unitTaskCountList = unitTaskCountMapper.selectList(Wrappers.<UnitTaskCount>lambdaQuery()
                .eq(UnitTaskCount::getSecondTaskId,secondTaskId)
        );
        log.info("unitTaskCountList:"+unitTaskCountList.size());

        List<UnitTask> unitTaskList = unitTaskMapper.selectList(Wrappers.<UnitTask>lambdaQuery()
                .eq(UnitTask::getSecondTaskId,secondTaskId)
        );
        Map<Long,UnitTask> unitTaskMap = unitTaskList.stream().collect(Collectors.toMap(UnitTask::getId,Function.identity(),(v1, v2) -> v2));

        //所有SecondTaskCount
        List<SecondTaskCount> secondTaskCountList = this.list(Wrappers.<SecondTaskCount>lambdaQuery()
                .eq(SecondTaskCount::getSecondTaskId,secondTaskId)
        );
        Map<Long, SecondTaskCount> secondTaskCountMap = secondTaskCountList.stream().collect(Collectors.toMap(SecondTaskCount::getUserId, Function.identity(), (v1, v2) -> v2));

        //UnitTask unitTask = unitTaskMapper.selectById(unitTaskId);

        //每个人的 unitTaskCount合计,key:empcode
        Map<String, BigDecimal> unitTaskCountMap =  new HashMap<>();
        Map<String, Set<Long>> grantUnitIdsMap =  new HashMap<>();
        Map<String, Set<String>> grantUnitNamesMap =  new HashMap<>();

        for (UnitTaskCount unitTaskCount : unitTaskCountList){
            String empCode = unitTaskCount.getEmpCode();
            Long userId = unitTaskCount.getUserId();
            UnitTask unitTask = unitTaskMap.get(unitTaskCount.getUnitTaskId());
            //if(unitTask == null) continue;
            //金额
            BigDecimal count = unitTaskCountMap.computeIfAbsent(empCode, k -> BigDecimal.ZERO);
            count = count.add(unitTaskCount.getAmt());
            unitTaskCountMap.put(empCode,count);
            //发放单元
            Set<Long> grantUnitIdsSet = grantUnitIdsMap.computeIfAbsent(empCode, k -> new HashSet<>());
            grantUnitIdsSet.add(unitTask.getGrantUnitId());
            Set<String> grantUnitNameSet = grantUnitNamesMap.computeIfAbsent(empCode, k -> new HashSet<>());
            grantUnitNameSet.add(unitTask.getGrantUnitName());

            SecondTaskCount secondTaskCount = secondTaskCountMap.get(userId);
            if(secondTaskCount == null){
                secondTaskCount = new SecondTaskCount();
                secondTaskCount.setSecondTaskId(unitTask.getSecondTaskId());
                secondTaskCount.setUserId(unitTaskCount.getUserId());
                secondTaskCount.setEmpCode(unitTaskCount.getEmpCode());
                secondTaskCount.setEmpName(unitTaskCount.getEmpName());
            }
            secondTaskCountList.add(secondTaskCount);
        }

        for (SecondTaskCount secondTaskCount : secondTaskCountList){
            String empCode = secondTaskCount.getEmpCode();
            BigDecimal count = unitTaskCountMap.get(empCode);
            secondTaskCount.setAmt(count);
            if(grantUnitNamesMap.get(empCode) == null){
                continue;
            }
            String grantUnitNams = String.join(",", grantUnitNamesMap.get(empCode));
            secondTaskCount.setGrantUnitNames(grantUnitNams);

            if(grantUnitIdsMap.get(empCode) != null) {
                String grantUnitIds = String.join(",", grantUnitIdsMap.get(empCode) + "");
                secondTaskCount.setGrantUnitIds(grantUnitIds);
            }
        }
        this.saveOrUpdateBatch(secondTaskCountList);
    }

    private SecondTaskCount getByUserId(Long secondTaskId, Long userId) {
        return this.getOne(Wrappers.<SecondTaskCount>lambdaQuery()
                .eq(SecondTaskCount::getSecondTaskId,secondTaskId)
                .eq(SecondTaskCount::getUserId,userId),false);
    }

    private SecondTaskCount getByEmpCode(Long secondTaskId, String empCode) {
        return this.getOne(Wrappers.<SecondTaskCount>lambdaQuery()
                .eq(SecondTaskCount::getSecondTaskId,secondTaskId)
                .eq(SecondTaskCount::getEmpCode,empCode),false);
    }

    @Override
    public List<SecondDetailCountVo> detailUserList(Long secondTaskId,Long unitTaskId, String empCode) {
        //查出所有任务
        List<UnitTaskCount> unitTaskCountList = unitTaskCountMapper.selectList(Wrappers.<UnitTaskCount>lambdaQuery()
                .eq(secondTaskId!=null,UnitTaskCount::getSecondTaskId,secondTaskId)
                .eq(unitTaskId != null,UnitTaskCount::getUnitTaskId,unitTaskId)
                .eq(StringUtils.isNotBlank(empCode),UnitTaskCount::getEmpCode,empCode));

        List<SecondDetailCountVo> rtnList = new ArrayList<>();
        for (UnitTaskCount unitTaskCount : unitTaskCountList){
            UnitTask unitTask = unitTaskMapper.selectById(unitTaskCount.getUnitTaskId());
            if (!(Objects.equals(unitTask.getId(), unitTaskId)
                    || SecondDistributionTaskStatusEnum.PENDING_APPROVAL.getCode().equals(unitTask.getStatus())
                    || SecondDistributionTaskStatusEnum.APPROVAL_APPROVED.getCode().equals(unitTask.getStatus()))) {
                continue;
            }
            String ifUpload = unitTask.getIfUpload();
            UnitTaskUser unitTaskUser = unitTaskUserMapper.selectOne(Wrappers.<UnitTaskUser>lambdaQuery()
                    .eq(UnitTaskUser::getUnitTaskId,unitTask.getId())
                    .eq(UnitTaskUser::getEmpCode,unitTaskCount.getEmpCode()));
            SecondDetailCountVo vo = new SecondDetailCountVo();
            BeanUtil.copyProperties(unitTaskUser, vo);
            if (Objects.equals(ifUpload, "1")){
                List<UnitTaskProject> unitTaskProjectList = unitTaskProjectService.detailCountByUser(unitTask,unitTaskCount.getEmpCode());
                vo.setUnitTaskProjectList(unitTaskProjectList);
            }
            vo.setTotalAmt(unitTaskCount.getAmt());
            rtnList.add(vo);
        }
        return rtnList;
    }

    @Override
    public List<ProgrammeInfoVo> detailTitleList(Long secondTaskId,Long unitTaskId, String empCode) {
        //查出所有任务
        List<UnitTaskCount> unitTaskCountList = unitTaskCountMapper.selectList(Wrappers.<UnitTaskCount>lambdaQuery()
                .eq(secondTaskId!=null,UnitTaskCount::getSecondTaskId,secondTaskId)
                .eq(unitTaskId != null,UnitTaskCount::getUnitTaskId,unitTaskId)
                .eq(StringUtils.isNotBlank(empCode),UnitTaskCount::getEmpCode,empCode)
                .last(StringUtils.isBlank(empCode),"limit 1"));

        List<ProgrammeInfoVo> rtnList = new ArrayList<>();
        for (UnitTaskCount unitTaskCount : unitTaskCountList){
            UnitTask unitTask = unitTaskMapper.selectById(unitTaskCount.getUnitTaskId());
            ProgrammeInfoVo programmeInfoVo = programmeService.getProgrammeInfo(unitTask.getProgrammeId());
            rtnList.add(programmeInfoVo);
        }
        return rtnList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doCountByEmpCode(Long secondTaskId, String empCode) {
        SecondTaskCount secondTaskCount = this.getByEmpCode(secondTaskId, empCode);
        if (secondTaskCount != null){
            List<UnitTaskCount>  unitTaskCountList = unitTaskCountMapper.selectList(Wrappers.<UnitTaskCount>lambdaQuery()
                    .eq(UnitTaskCount::getSecondTaskId,secondTaskId)
                    .eq(UnitTaskCount::getEmpCode,empCode));

            if (unitTaskCountList != null && !unitTaskCountList.isEmpty()){
                List<UnitTask> unitTaskList = unitTaskMapper.selectList(Wrappers.<UnitTask>lambdaQuery()
                        .eq(UnitTask::getSecondTaskId,secondTaskId)
                );
                Map<Long,UnitTask> unitTaskMap = unitTaskList.stream().collect(Collectors.toMap(UnitTask::getId,Function.identity(),(v1, v2) -> v2));
                //获取grantUnitIdsSet和grantUnitNameSet
                BigDecimal amt = BigDecimal.ZERO;
                Set<Long> grantUnitIdsSet = new HashSet<>();
                Set<String> grantUnitNameSet = new HashSet<>();
                for (UnitTaskCount unitTaskCount : unitTaskCountList){
                    UnitTask unitTask = unitTaskMap.get(unitTaskCount.getUnitTaskId());
                    if(unitTask == null) continue;
                    //金额
                    amt = amt.add(unitTaskCount.getAmt());
                    //发放单元
                    grantUnitIdsSet.add(unitTask.getGrantUnitId());
                    grantUnitNameSet.add(unitTask.getGrantUnitName());
                }

                secondTaskCount.setAmt(amt);
                String grantUnitNams = String.join(",", grantUnitNameSet);
                secondTaskCount.setGrantUnitNames(grantUnitNams);
                String grantUnitIds = String.join(",",grantUnitIdsSet+"");
                secondTaskCount.setGrantUnitIds(grantUnitIds);
                this.updateById(secondTaskCount);
            }else{
                this.removeById(secondTaskCount.getId());
            }
        }
    }


}
