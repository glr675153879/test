package com.hscloud.hs.cost.account.service.impl.second;

import cn.hutool.core.comparator.CompareUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.constant.enums.second.ModeType;
import com.hscloud.hs.cost.account.constant.enums.second.ProjectType;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskMapper;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskProjectCountMapper;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskProjectMapper;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskDetailItemVo;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskProjectCountVo;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskProjectDetailVo;
import com.hscloud.hs.cost.account.service.second.*;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* 核算指标分配结果按人汇总 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UnitTaskProjectCountService extends ServiceImpl<UnitTaskProjectCountMapper, UnitTaskProjectCount> implements IUnitTaskProjectCountService {
    private final UnitTaskMapper unitTaskMapper;
    private final UnitTaskProjectMapper unitTaskProjectMapper;
    @Lazy
    @Autowired
    private IUnitTaskProjectDetailService unitTaskProjectDetailService;
    @Lazy
    @Autowired
    private IUnitTaskDetailItemService unitTaskDetailItemService;

    private final IUnitTaskDetailCountService unitTaskDetailCountService;
    private final IUnitTaskCountService unitTaskCountService;
    @Lazy
    @Autowired
    private IUnitTaskProjectService unitTaskProjectService;
    private final IUnitTaskUserService unitTaskUserService;
    private final IUnitTaskDetailItemWorkService unitTaskDetailItemWorkService;


    private final RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
//    @Async
    public void doCount(Long unitTaskId) {
        long l1 = System.currentTimeMillis();
        String key = CacheConstants.SEC_DOCOUNT+unitTaskId;
        if(!redisUtil.setLock(key,1,30L, TimeUnit.SECONDS)){
            throw new BizException("doCount 请勿重复操作");
        }
        try{
            UnitTask unitTask = unitTaskMapper.selectById(unitTaskId);
            BigDecimal beforeAmt = unitTask.getKsAmt();
            //计算所有project 和 projectCount
            List<UnitTaskProject> projectList = unitTaskProjectService.list(Wrappers.<UnitTaskProject>lambdaQuery()
                    .eq(UnitTaskProject::getUnitTaskId,unitTaskId)
                    .orderByAsc(UnitTaskProject::getSortNum));
            long l11 = System.currentTimeMillis();
            for (UnitTaskProject project : projectList){
                long l111 = System.currentTimeMillis();
                BigDecimal countAmt = this.projectCount(beforeAmt,project,unitTaskId,unitTask.getSecondTaskId());
                long l222 = System.currentTimeMillis();
                log.info("UnitTaskProjectCountService.doCount耗时为{}",  l222 - l111);
                unitTaskProjectService.updateBatchById(projectList);
                beforeAmt = beforeAmt.subtract(countAmt);
            }
            long l22 = System.currentTimeMillis();
            log.info("UnitTaskProjectCountService.doCount耗时为{}",  l22 - l11);
            unitTaskProjectService.updateBatchById(projectList);

            //清理projectCount
            this.clearProjectCount(unitTaskId,projectList);

            //计算UnitTaskCount
            unitTaskCountService.doCount(unitTask);
        }finally {
            redisUtil.unLock(key);
            System.out.println("unLock======");
        }
        long l2 = System.currentTimeMillis();
        log.info("UnitTaskProjectCountService.doCount耗时为{}",  l2 - l1);
    }

    private void clearProjectCount(Long unitTaskId, List<UnitTaskProject> projectList) {
        List<Long> projectIds = projectList.stream().map(UnitTaskProject::getId).collect(Collectors.toList());
        if(!projectIds.isEmpty()){
            List<UnitTaskProjectCount> list = this.list(Wrappers.<UnitTaskProjectCount>lambdaQuery()
                    .eq(UnitTaskProjectCount::getUnitTaskId, unitTaskId)
                    .notIn(UnitTaskProjectCount::getProjectId, projectIds));
            if(!list.isEmpty()){
                this.removeBatchByIds(list);
            }

        }
    }

    @Override
    public List<UnitTaskProjectCountVo> userList(Long unitTaskId) {
        List<UnitTaskUser> userList = unitTaskUserService.listByTaskId(unitTaskId);
        List<UnitTaskProjectCount> projectCountList = this.list(Wrappers.<UnitTaskProjectCount>lambdaQuery().eq(UnitTaskProjectCount::getUnitTaskId,unitTaskId));

        //根据人员分组 projectCountMap ，key：userId
        Map<Long,List<UnitTaskProjectCount>> projectCountMap = projectCountList.stream().collect(Collectors.groupingBy(UnitTaskProjectCount::getUserId));

        //查询 taskCount
        List<UnitTaskCount> taskCounts = unitTaskCountService.listByTaskId(unitTaskId);
        Map<Long, BigDecimal> taskCountMap = taskCounts.stream().collect(Collectors.toMap(UnitTaskCount::getUserId, UnitTaskCount::getAmt, (v1, v2) -> v2));

        List<UnitTaskProjectCountVo> rtnList = new ArrayList<>();
        for (UnitTaskUser unitTaskUser : userList){
            Long userId = Long.parseLong(unitTaskUser.getUserId());
            UnitTaskProjectCountVo vo = new UnitTaskProjectCountVo();
            BeanUtils.copyProperties(unitTaskUser, vo);
            vo.setProjectCountList(projectCountMap.get(userId));
            vo.setTaskCount(taskCountMap.get(userId));
            rtnList.add(vo);
        }
        return rtnList;
    }

    @Override
    public void syncProjectName(List<UnitTaskProject> updateList) {

        for (UnitTaskProject project : updateList){
            this.update(null,Wrappers.<UnitTaskProjectCount>lambdaUpdate()
                    .set(UnitTaskProjectCount::getProjectName,project.getName())
                    .set(UnitTaskProjectCount::getSortNum,project.getSortNum())
                    .eq(UnitTaskProjectCount::getProjectId,project.getId()));
        }
    }

    private BigDecimal projectCount(BigDecimal beforeAmt, UnitTaskProject project, Long unitTaskId, Long secondTaskId) {
        BigDecimal countAmt = BigDecimal.ZERO;

        String projectType = CommonUtils.getDicVal(project.getProjectType());
        if(ProjectType.danxiang.toString().equals(projectType)){//单项
            countAmt = this.danxiangCount(beforeAmt,project,unitTaskId,secondTaskId);
        }else  if(ProjectType.pinjun.toString().equals(projectType)){//平均
            countAmt = this.pinjunCount(beforeAmt,project,unitTaskId,secondTaskId);
        }else  if(ProjectType.erci.toString().equals(projectType)){//科室二次
            countAmt = this.erciCount(beforeAmt,project,unitTaskId,secondTaskId);
        }
        project.setBeforeAmt(beforeAmt);
        project.setAfterAmt(beforeAmt.subtract(countAmt));
        project.setCountAmt(countAmt);

        return countAmt;
    }

    private BigDecimal erciCount(BigDecimal beforeAmt, UnitTaskProject project, Long unitTaskId, Long secondTaskId) {
        // int scale = 2;
        // RoundingMode roundingMode = CommonUtils.getCarryRule(project.getCarryRule());
        long l1 = System.currentTimeMillis();
        BigDecimal countAmt = BigDecimal.ZERO;
        List<UnitTaskProjectDetail> detailList = unitTaskProjectDetailService.list(Wrappers.<UnitTaskProjectDetail>lambdaQuery().eq(UnitTaskProjectDetail::getUnitTaskProjectId,project.getId()));
        //List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (UnitTaskProjectDetail unitTaskProjectDetail : detailList){
            BigDecimal erciRate = unitTaskProjectDetail.getErciRate();
            //detail可分配金额，最终要等于 每个人的detailCount之和
            BigDecimal amt = beforeAmt.multiply(erciRate).multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.DOWN);
            unitTaskProjectDetail.setAmt(amt);
            countAmt = countAmt.add(amt);
            //计算每个人的detailCount
            this.erciCountItem(amt,unitTaskProjectDetail,project, unitTaskId, secondTaskId);

//            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
//                    this.erciCountItem(amt, unitTaskProjectDetail, project, unitTaskId, secondTaskId)
//            );
//            futures.add(future);
        }

        //CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long l2 = System.currentTimeMillis();
        log.info("UnitTaskProjectCountService.doCount耗时为{}",  l2 - l1);
        unitTaskProjectDetailService.updateBatchById(detailList);
        //清理 detailCount
        this.clearDetailCount(project.getId(),detailList);
        //根据detailCount计算 projectCount
        this.erciCountProject(project, unitTaskId, secondTaskId);
        long l3 = System.currentTimeMillis();
        log.info("UnitTaskProjectCountService.erciCountProject耗时为{}",  l3 - l1);
        return countAmt;
    }

    private void clearDetailCount(Long projectId, List<UnitTaskProjectDetail> detailList) {
        List<Long> detailIds = detailList.stream().map(UnitTaskProjectDetail::getId).collect(Collectors.toList());
        if(!detailIds.isEmpty()){
            List<UnitTaskDetailCount> list = unitTaskDetailCountService.list(Wrappers.<UnitTaskDetailCount>lambdaQuery().eq(UnitTaskDetailCount::getProjectId, projectId)
                    .notIn(UnitTaskDetailCount::getDetailId, detailIds));
            if(!list.isEmpty()){
                unitTaskDetailCountService.removeBatchByIds(list);
            }
        }
    }

    private void erciCountProject(UnitTaskProject project, Long unitTaskId, Long secondTaskId) {
        Long projectId = project.getId();
        List<UnitTaskProjectCount> projectCountList = this.listByProjectId(project.getId());
        List<UnitTaskProjectCount> addList = new ArrayList<>();
        Map<String,UnitTaskProjectCount> projectCountMap =  projectCountList.stream().collect(Collectors.toMap(UnitTaskProjectCount::getEmpCode, item -> item, (v1, v2) -> v2));

        List<UnitTaskDetailCount> detailCountList = unitTaskDetailCountService.listByProjectId(project.getId());

        //每个人的 detailCount合计,key:empcode
        Map<String,BigDecimal> detailCountMap =  new HashMap<>();
        for (UnitTaskDetailCount detailCount : detailCountList){
            String empCode = detailCount.getEmpCode();
            BigDecimal count = detailCountMap.computeIfAbsent(empCode, k -> BigDecimal.ZERO);
            count = count.add(detailCount.getAmt());
            detailCountMap.put(empCode,count);

            UnitTaskProjectCount projectCount = projectCountMap.get(empCode);
            if(projectCount == null){//新加人员的projectCount 在这里新增
                projectCount = new UnitTaskProjectCount();
                projectCount.setProjectId(projectId);
                projectCount.setProjectName(project.getName());
                projectCount.setUnitTaskId(unitTaskId);
                projectCount.setSecondTaskId(secondTaskId);
                projectCount.setEmpCode(detailCount.getEmpCode());
                projectCount.setUserId(detailCount.getUserId());
                projectCount.setEmpName(detailCount.getEmpName());
                projectCountList.add(projectCount);
                projectCountMap.put(empCode,projectCount);

                addList.add(projectCount);
            }
        }

        for (UnitTaskProjectCount projectCount : projectCountList){
            String empCode = projectCount.getEmpCode();
            BigDecimal count = detailCountMap.get(empCode);
            projectCount.setAmt(count);
        }
        if (!addList.isEmpty()){
            this.saveBatch(addList);
        }
        this.updateBatchById(projectCountList);
    }

    @Async
    public void erciCountItem(BigDecimal beforeAmt, UnitTaskProjectDetail detail, UnitTaskProject project, Long unitTaskId, Long secondTaskId) {
        log.info("erciCountItem开始执行==================");
        int scale = 6;
        RoundingMode roundingMode = CommonUtils.getCarryRule(project.getCarryRule());
        Long detailId = detail.getId();
        Long projectId = project.getId();

        List<UnitTaskDetailItemVo> voList = unitTaskDetailItemService.userList(detailId);

        List<UnitTaskDetailCount> detailCountList = unitTaskDetailCountService.listByDetailId(detailId);
        List<UnitTaskDetailCount> addList = new ArrayList<>();
        Map<String,UnitTaskDetailCount> detailCountMap =  detailCountList.stream().collect(Collectors.toMap(UnitTaskDetailCount::getEmpCode, item -> item, (v1, v2) -> v2));
        //所有人的绩效总和，最终要 = beforeAmt，差的一分钱 算到第一个人头上
        BigDecimal countAmt = BigDecimal.ZERO;

        //获取每个人的分值
        Map<String,BigDecimal> userPointMap = this.getUserPointMap(detail,voList);
        //计算总分值
        BigDecimal totalPoint = BigDecimal.ZERO;
        for (String key : userPointMap.keySet()){
            BigDecimal value = userPointMap.get(key);
            totalPoint = totalPoint.add(value);
        }

        //计算每个人的detailCount
        for (UnitTaskDetailItemVo itemVo : voList){
            String empCode = itemVo.getEmpCode();
            BigDecimal userPoint = userPointMap.get(empCode);
            if(userPoint == null) continue;

            //amt = 剩余金额*个人分值/总分值；
            BigDecimal amt = BigDecimal.ZERO;
            if(totalPoint.compareTo(BigDecimal.ZERO) > 0){
                amt = beforeAmt.multiply(userPoint).divide(totalPoint,2, RoundingMode.DOWN);
            }

            UnitTaskDetailCount detailCount = detailCountMap.get(empCode);
            if(detailCount == null){//新加人员的projectCount 在这里新增
                detailCount = new UnitTaskDetailCount();
                detailCount.setDetailId(detailId);
                detailCount.setProjectId(projectId);
                detailCount.setUnitTaskId(unitTaskId);
                detailCount.setSecondTaskId(secondTaskId);
                detailCount.setEmpCode(itemVo.getEmpCode());
                detailCount.setUserId(Long.parseLong(itemVo.getUserId()));
                detailCount.setEmpName(itemVo.getEmpName());
                detailCountList.add(detailCount);

                addList.add(detailCount);
            }
            detailCount.setAmt(amt);
            countAmt = countAmt.add(amt);
        }
        //一分钱算给第一个人
        if(Objects.equals("1",detail.getIfEdited()) && countAmt.compareTo(beforeAmt) != 0){
            BigDecimal oneFen = beforeAmt.subtract(countAmt);
            if(!detailCountList.isEmpty()){
                this.setOneFen(detailCountList,oneFen);
            }
        }

        if (!addList.isEmpty()){
            unitTaskDetailCountService.saveBatch(addList);
        }
        unitTaskDetailCountService.updateBatchById(detailCountList);


    }

    private void setOneFen(List<UnitTaskDetailCount> detailCountList, BigDecimal oneFen) {
        if(!detailCountList.isEmpty()){
            List<UnitTaskUser> unitTaskUsers = unitTaskUserService.listByTaskId(detailCountList.get(0).getUnitTaskId());
            Map<String, Float> collect = unitTaskUsers.stream().collect(Collectors.toMap(UnitTaskUser::getEmpCode, UnitTaskUser::getSortNum, (v1, v2) -> v1));
            detailCountList.sort((o1, o2) -> {
                Float sortNum1 = collect.get(o1.getEmpCode());
                Float sortNum2 = collect.get(o2.getEmpCode());
                return CompareUtil.compare(sortNum1, sortNum2);
            });
            //加到第一个amt!=0的人头上，总会有的
            for (UnitTaskDetailCount detailCount : detailCountList){
                BigDecimal amt = detailCount.getAmt();
                if(amt != null && amt.compareTo(BigDecimal.ZERO) > 0){
                    detailCount.setAmt(amt.add(oneFen));
                    return;
                }
            }
        }
    }

    @Override
    public Map<String, BigDecimal> getUserPointMap(UnitTaskProjectDetail detail, List<UnitTaskDetailItemVo> voList) {
        Map<String, BigDecimal> rtnMap = new HashMap<>();
        String ifItemValueAdd = detail.getIfItemValueAdd()==null?"1":detail.getIfItemValueAdd();
        String parentItemValueAdd = detail.getIfParentItemValueAdd()==null?"1":detail.getIfParentItemValueAdd();

        for (UnitTaskDetailItemVo itemVo : voList){
            String empCode = itemVo.getEmpCode();
            List<UnitTaskDetailItem> itemList = itemVo.getItemList();
            BigDecimal userPoint = null;
            //系数分配
            String modeType = CommonUtils.getDicVal(detail.getModeType());
            String ifCareWorkdays = detail.getIfCareWorkdays();
            if(ModeType.ratio.toString().equals(modeType)){
                if(itemList != null && !itemList.isEmpty()) {
                    userPoint = parentItemValueAdd.equals("1") ? BigDecimal.ZERO : BigDecimal.ONE;
                    //每个大项的值
                    for (UnitTaskDetailItem bigItem : itemList) {
                        BigDecimal bigItemAmt = bigItem.getAmt();
                        if (bigItem.getUnitTaskDetailItemList() != null) {//有子项
                            bigItemAmt = ifItemValueAdd.equals("1") ? BigDecimal.ZERO : BigDecimal.ONE;
                            for (UnitTaskDetailItem item : bigItem.getUnitTaskDetailItemList()) {
                                if (ifItemValueAdd.equals("1")) {
                                    bigItemAmt = bigItemAmt.add(item.getAmt());
                                } else {
                                    bigItemAmt = bigItemAmt.multiply(item.getAmt());
                                }
                            }
                        }
                        if (parentItemValueAdd.equals("1")) {
                            userPoint = userPoint.add(bigItemAmt);
                        } else {
                            userPoint = userPoint.multiply(bigItemAmt);
                        }
                    }
                }
                if (ifCareWorkdays.equals("1")){
                    BigDecimal  workdays = itemVo.getWorkdays();
                    userPoint = userPoint == null?BigDecimal.ZERO:userPoint;
                    userPoint = userPoint.multiply(workdays);
                }
                //每个大项的值 如果需要保存，在这里保存


            } else {// 工作量分配
                // 工作量系数，考核得分 抽到一张表，userid、detailId关联
                List<UnitTaskDetailItemWork> unitTaskDetailItemWorks = unitTaskDetailItemWorkService.listByDetailId(detail.getId());
                Map<String, UnitTaskDetailItemWork> unitTaskDetailItemWorkMap =
                        unitTaskDetailItemWorks.stream().filter(e -> Objects.nonNull(e.getEmpCode())).collect(Collectors.toMap(UnitTaskDetailItemWork::getEmpCode, e -> e, (v1, v2) -> v2));

                // BigDecimal  workRate = itemVo.getWorkRate();
                // BigDecimal  examPoint = itemVo.getExamPoint();
                BigDecimal workRate = BigDecimal.ONE;
                BigDecimal examPoint = new BigDecimal("100.00");
                if (unitTaskDetailItemWorkMap.containsKey(empCode)) {
                    workRate = unitTaskDetailItemWorkMap.get(empCode).getWorkRate();
                    examPoint = unitTaskDetailItemWorkMap.get(empCode).getExamPoint();
                }
                if (itemList != null && !itemList.isEmpty()) {
                    BigDecimal amt = BigDecimal.ZERO;
                    amt = itemList.stream()
                            .map(UnitTaskDetailItem::getAmt)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    userPoint = amt.multiply(workRate).multiply(examPoint);
                    if (ifCareWorkdays.equals("1")) {
                        BigDecimal workdays = itemVo.getWorkdays();
                        userPoint = userPoint.multiply(workdays);
                    }
                }
            }
            rtnMap.put(empCode,userPoint==null?BigDecimal.ZERO:userPoint);
        }
        return rtnMap;
    }

    private BigDecimal pinjunCount(BigDecimal beforeAmt, UnitTaskProject project, Long unitTaskId, Long secondTaskId) {
        int scale = 2;
        // RoundingMode roundingMode = CommonUtils.getCarryRule(project.getCarryRule());
        Long projectId = project.getId();

        List<UnitTaskProjectDetailVo> voList = unitTaskProjectDetailService.userList(projectId);
        List<UnitTaskProjectCount> projectCountList = this.listByProjectId(projectId);
        List<UnitTaskProjectCount> addList = new ArrayList<>();
        Map<String,UnitTaskProjectCount> projectCountMap =  projectCountList.stream().collect(Collectors.toMap(UnitTaskProjectCount::getEmpCode, item -> item, (v1, v2) -> v2));
        BigDecimal countAmt = BigDecimal.ZERO;

        BigDecimal totalPoint = BigDecimal.ZERO;//合计分值
        for (UnitTaskUser unitTaskUser : voList){
            BigDecimal userRate = unitTaskUser.getUserRate();
            BigDecimal workdays = unitTaskUser.getWorkdays();
            totalPoint = totalPoint.add(workdays.multiply(userRate));
        }
        if(totalPoint.compareTo(BigDecimal.ZERO) == 0){
            return BigDecimal.ZERO;
        }
        //计算每个人的projectCount
        for (UnitTaskUser unitTaskUser : voList){
            String empCode = unitTaskUser.getEmpCode();
            BigDecimal userRate = unitTaskUser.getUserRate();
            BigDecimal workdays = unitTaskUser.getWorkdays();
            BigDecimal avgRate = unitTaskUser.getAvgRate() == null?BigDecimal.ONE:unitTaskUser.getAvgRate();

            //amt = 剩余金额*个人分值*绩效倍数/总分值；个人分值 = 出勤天数 * 个人系数；
            //BigDecimal amt = beforeAmt.multiply(workdays).multiply(userRate).multiply(avgRate).divide(totalPoint,scale,roundingMode);
            BigDecimal amt = beforeAmt.multiply(workdays).multiply(userRate).multiply(avgRate).divide(totalPoint, scale, RoundingMode.DOWN);
            UnitTaskProjectCount projectCount = projectCountMap.get(empCode);
            if(projectCount == null){//新加人员的projectCount 在这里新增
                projectCount = new UnitTaskProjectCount();
                projectCount.setProjectId(projectId);
                projectCount.setProjectName(project.getName());
                projectCount.setUnitTaskId(unitTaskId);
                projectCount.setSecondTaskId(secondTaskId);
                projectCount.setEmpCode(empCode);
                projectCount.setUserId(Long.parseLong(unitTaskUser.getUserId()));
                projectCount.setEmpName(unitTaskUser.getEmpName());
                projectCountList.add(projectCount);

                addList.add(projectCount);
            }
            projectCount.setAmt(amt);
            countAmt = countAmt.add(amt);
        }
        if (!addList.isEmpty()){
            this.saveBatch(addList);
        }
        this.updateBatchById(projectCountList);
        return countAmt;
    }

    private BigDecimal danxiangCount(BigDecimal beforeAmt, UnitTaskProject project, Long unitTaskId, Long secondTaskId) {
        int scale = 2;
        RoundingMode roundingMode = CommonUtils.getCarryRule(project.getCarryRule());
        Long projectId = project.getId();

        List<UnitTaskProjectDetailVo> userList = unitTaskProjectDetailService.userList(projectId);
        List<UnitTaskProjectCount> projectCountList = this.listByProjectId(projectId);
        List<UnitTaskProjectCount> addList = new ArrayList<>();
        //key ：empCode
        Map<String,UnitTaskProjectCount> projectCountMap =  projectCountList.stream().collect(Collectors.toMap(UnitTaskProjectCount::getEmpCode, item -> item, (v1, v2) -> v2));
        BigDecimal countAmt = BigDecimal.ZERO;

        //计算每个人的projectCount
        for (UnitTaskProjectDetailVo projectDetailVo : userList){
            String empCode = projectDetailVo.getEmpCode();
            List<UnitTaskProjectDetail> detailList = projectDetailVo.getDetailList();
            BigDecimal amt = detailList.stream()
                    .map(UnitTaskProjectDetail::getAmt)
                    .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(scale,roundingMode);
            UnitTaskProjectCount projectCount = projectCountMap.get(empCode);
            if(projectCount == null){//新加人员的projectCount 在这里新增
                projectCount = new UnitTaskProjectCount();
                projectCount.setProjectId(projectId);
                projectCount.setProjectName(project.getName());
                projectCount.setUnitTaskId(unitTaskId);
                projectCount.setSecondTaskId(secondTaskId);
                projectCount.setEmpCode(projectDetailVo.getEmpCode());
                projectCount.setUserId(Long.parseLong(projectDetailVo.getUserId()));
                projectCount.setEmpName(projectDetailVo.getEmpName());
                projectCountList.add(projectCount);

                addList.add(projectCount);
            }
            projectCount.setAmt(amt);
            countAmt = countAmt.add(amt);
        }
        if (!addList.isEmpty()){
            this.saveBatch(addList);
        }
        this.updateBatchById(projectCountList);
        return countAmt;
    }

    private List<UnitTaskProjectCount> listByProjectId(Long projectId) {
        return this.list(Wrappers.<UnitTaskProjectCount>lambdaQuery().eq(UnitTaskProjectCount::getProjectId,projectId));
    }
}
