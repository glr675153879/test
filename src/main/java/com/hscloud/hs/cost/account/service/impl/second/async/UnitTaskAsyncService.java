package com.hscloud.hs.cost.account.service.impl.second.async;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.constant.enums.SecondDistributionTaskStatusEnum;
import com.hscloud.hs.cost.account.mapper.second.SecondTaskMapper;
import com.hscloud.hs.cost.account.model.dto.second.SecStartDataDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountTask;
import com.hscloud.hs.cost.account.model.entity.second.GrantUnit;
import com.hscloud.hs.cost.account.model.entity.second.Programme;
import com.hscloud.hs.cost.account.model.entity.second.SecondTask;
import com.hscloud.hs.cost.account.model.entity.second.UnitTask;
import com.hscloud.hs.cost.account.service.kpi.IKpiAccountTaskService;
import com.hscloud.hs.cost.account.service.second.*;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.media.service.DingRobotService;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
* 二次分配总任务 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class UnitTaskAsyncService {

    private final IUnitTaskService unitTaskService;

    private final IProgrammeService programmeService;

    private final IUnitTaskProjectService unitTaskProjectService;

    private final IUnitTaskUserService unitTaskUserService;

    private final SecondTaskMapper secondTaskMapper;

    private final IUnitTaskProjectCountService projectCountService;
    private final IKpiAccountTaskService kpiAccountTaskService;


    @Resource
    private DingRobotService dingRobotService;
//    @Autowired
//    private TransactionTemplate transactionTemplate;

    @Transactional(rollbackFor = Exception.class)
    @Async("secondAsync")
    public void createByGrantUnit(GrantUnit grantUnit, SecondTask secondTask, Programme programme) {
        long l = System.currentTimeMillis();
        try{
            //删除已生成的发放单元
            unitTaskService.removeByGrantUnitId(secondTask,grantUnit);

            Long secondTaskId = secondTask.getId();
            BigDecimal ksAmtTotal = BigDecimal.ZERO;
            if(programme != null){
                //获取一次分配结果金额
                //科室金额 是不含 编外人员的金额的
                //先查不含编外的 科室金额：科室一次分配金额接口
                BigDecimal ksAmt = BigDecimal.ZERO;
                if(StrUtil.isNotBlank(grantUnit.getKsUnitIdsNonStaff())){
                    ksAmt = unitTaskService.getKsAmtNonStaff(grantUnit,secondTask.getCycle());
                }
                //有 额外新增人员 字段,则计算
                if(StrUtil.isNotBlank(grantUnit.getExtraUserIds())){
                    BigDecimal ksAmtNonStaff = unitTaskService.getKsAmtExtraUserIds(grantUnit, secondTask.getCycle());
                    ksAmt = ksAmt.add(ksAmtNonStaff);
                }
                //再查含编外的 科室金额：科室一次分配金额接口+编外人员一次分配金额接口
                if(StrUtil.isNotBlank(grantUnit.getKsUnitIds())){
                    log.info("含编外发放单元"+grantUnit.getKsUnitIds());
                    BigDecimal ksAmtNonStaff = unitTaskService.getKsAmt(grantUnit,secondTask.getCycle());
                    log.info("含编外发放单元金额"+ksAmtNonStaff);
                    ksAmt = ksAmt.add(ksAmtNonStaff);
                }

                UnitTask unitTask = new UnitTask();
                unitTask.setProgrammeId(programme.getId());
                unitTask.setSecondTaskId(secondTask.getId());
                unitTask.setName(secondTask.getName());
                unitTask.setGrantUnitId(grantUnit.getId());
                unitTask.setGrantUnitName(grantUnit.getName());
                unitTask.setLeaderIds(grantUnit.getLeaderIds());
                unitTask.setLeaderNames(grantUnit.getLeaderNames());
                unitTask.setStartTime(LocalDateTime.now());
                unitTask.setCycle(secondTask.getCycle());
                unitTask.setStatus(SecondDistributionTaskStatusEnum.UNCOMMITTED.getCode());
                unitTask.setIfFinish("0");
                //一次分配总金额 包含编外和编内的金额信息
                unitTask.setKsAmt(ksAmt);
                unitTask.setIfUpload(grantUnit.getIfUpload());
                unitTaskService.save(unitTask);

                ksAmtTotal = ksAmtTotal.add(ksAmt);
                //生成人员配置
                unitTaskUserService.createInit(unitTask);

                //新增project
                unitTaskProjectService.createByProg(unitTask,programme);

                //计算金额
                //projectCountService.doCount(unitTask.getId());
            }
            //修改总任务金额
            List<UnitTask> unitTaskList = unitTaskService.list(Wrappers.<UnitTask>lambdaQuery().eq(UnitTask::getSecondTaskId, secondTaskId));
            BigDecimal secondAmt = BigDecimal.ZERO;
            for (UnitTask unitTask : unitTaskList){
                secondAmt = secondAmt.add(unitTask.getKsAmt());
            }
            secondTask.setKsAmt(secondAmt);
            secondTaskMapper.updateById(secondTask);
            //secondTaskMapper.update(null,Wrappers.<SecondTask>lambdaUpdate().eq(SecondTask::getId,secondTask.getId()).set(SecondTask::getKsAmt,ksAmtTotal));

//            if (true){
//                throw new RuntimeException("手动终止");
//            }
        }catch (BizException e){
            String msg = grantUnit.getName()+"任务下发失败：" + e.getDefaultMessage();
            log.error("任务下发失败",e);
            dingRobotService.sendMsg2DingRobot(SecurityUtils.getUser().getTenantId() + "", "二次分配", msg);

            SpringUtil.getBean(this.getClass()).errlog(secondTask,msg);
        }

        long l1 = System.currentTimeMillis();
        log.info(grantUnit.getName() +"下发成功:"+(l1-l));
    }

    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
    public void errlog(SecondTask secondTask, String msg) {
        KpiAccountTask firstTask = kpiAccountTaskService.getById(secondTask.getFirstTaskId());
        if (firstTask != null){
            firstTask.setSendFlag("N");
            firstTask.setSendLog(msg);
            kpiAccountTaskService.updateById(firstTask);
        }

    }
}