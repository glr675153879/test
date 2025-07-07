package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.enums.kpi.FormulaParamEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.MemberEnum;
import com.hscloud.hs.cost.account.mapper.kpi.*;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.entity.second.GrantUnit;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountTaskListVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiCalculateReportVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiKeyValueVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiTaskReportLeftVO;
import com.hscloud.hs.cost.account.service.second.IGrantUnitService;
import com.hscloud.hs.cost.account.utils.kpi.Convert;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.service.kpi.IKpiAccountTaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 核算任务表(cost_account_task) 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class KpiAccountTaskService extends ServiceImpl<KpiAccountTaskMapper, KpiAccountTask> implements IKpiAccountTaskService {
    @Autowired
    private KpiAccountTaskChildService kpiAccountTaskChildService;
    @Autowired
    private KpiMemberMapper kpiMemberMapper;
    @Autowired
    private KpiMemberCopyMapper kpiMemberCopyMapper;
    @Autowired
    private KpiUserAttendanceCopyMapper kpiUserAttendanceCopyMapper;
    @Autowired
    private KpiAccountUnitCopyMapper kpiAccountUnitCopyMapper;
    @Autowired
    private KpiItemEquivalentCopyMapper kpiItemEquivalentCopyMapper;
    @Autowired
    private KpiAccountTaskMapper kpiAccountTaskMapper;
    @Autowired
    private KpiAccountPlanCopyMapper kpiAccountPlanCopyMapper;
    @Autowired
    private KpiAccountPlanChildCopyMapper kpiAccountPlanChildCopyMapper;
    @Autowired
    private KpiCalculateMapper kpiCalculateMapper;
    @Autowired
    private KpiAccountTaskChildMapper kpiAccountTaskChildMapper;
    @Autowired
    private KpiConfigMapper kpiConfigMapper;
    @Autowired
    private KpiAllocationRuleCopyMapper kpiAllocationRuleCopyMapper;
    @Autowired
    private KpiAllocationRuleMapper kpiAllocationRuleMapper;
    @Autowired
    private KpiItemCopyMapper kpiItemCopyMapper;
    @Autowired
    private KpiReportConfigMapper kpiReportConfigMapper;
    @Autowired
    private KpiReportConfigCopyMapper kpiReportConfigCopyMapper;
    @Autowired
    private KpiConfigService kpiConfigService;
    @Autowired
    private KpiUserAttendanceMapper kpiUserAttendanceMapper;
    @Autowired
    private KpiAccountUnitMapper kpiAccountUnitMapper;
    @Autowired
    private KpiReportConfigPowerMapper kpiReportConfigPowerMapper;
    @Autowired
    private IGrantUnitService grantUnitService;
    @Autowired
    private KpiItemResultCopyMapper kpiItemResultCopyMapper;
    @Autowired
    private KpiClusterUnitCopyMapper kpiClusterUnitCopyMapper;
    @Autowired
    private KpiCalculateCompMapper kpiCalculateCompMapper;


    @Override
    public List<KpiAccountTaskListVO> list(KpiAccountTaskListDto input) {
        LambdaQueryWrapper<KpiAccountTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KpiAccountTask::getDelFlag, "0");
        if (StringUtils.isNotBlank(input.getIndexName())) {
            wrapper.like(KpiAccountTask::getAccountTaskName, input.getIndexName());
        }
        if (StringUtils.isNotBlank(input.getTestFlag())) {
            wrapper.like(KpiAccountTask::getTestFlag, input.getTestFlag());
        }
        if (input.getCreatedId() != 0) {
            wrapper.eq(KpiAccountTask::getCreatedId, input.getCreatedId());
        }
        List<KpiAccountTask> list = list(wrapper);

        return Convert.convertEntityToVo(list, KpiAccountTaskListVO::convertByKpiAccountTask);
    }

    @Override
    public IPage<KpiAccountTaskListVO> getPage(KpiAccountTaskListDto input) {
        if (StringUtil.isNullOrEmpty(input.getTestFlag())){
            input.setTestFlag("N");
        }
        IPage<KpiAccountTaskListVO> page = kpiAccountTaskMapper.pageTask(new Page<>(input.getCurrent(), input.getSize()), input);
        page.getRecords().forEach(r -> {
            if (StringUtil.isNullOrEmpty(r.getStatusName())) {
                r.setStatusName("未执行");
            }
        });
        return page;

    }

    @Override
    public void saveOrUpdate(KpiAccountTaskAddDto dto) {
        KpiAccountTask e = null;
        if (dto.getId() != null) {
            e = getById(dto.getId());
            BeanUtil.copyProperties(dto, e);
            this.getBaseMapper().update2(e);
        } else {
            e = BeanUtil.copyProperties(dto, KpiAccountTask.class);
            e.setDelFlag("0");
            e.setIssuedFlag("N");
            e.setStatus(1L);
            saveOrUpdate(e);
        }

        if (e.getPeriod().toString().endsWith("13")){
            KpiAccountTaskChild task_child = new KpiAccountTaskChild();
            task_child.setRunLog("");
            task_child.setPeriod(e.getPeriod());
            task_child.setTenantId(e.getTenantId());
            task_child.setCreatedId(e.getCreatedId());
            task_child.setIssuedFlag("N");
            task_child.setStatus(99L);
            task_child.setStatusName("待锁定");
            task_child.setCreatedDate(new Date());
            task_child.setTaskId(e.getId());
            kpiAccountTaskChildMapper.insert(task_child);

            e.setTaskChildId(task_child.getId());
            updateById(e);

            KpiConfig next = kpiConfigMapper.selectOne(
                    new QueryWrapper<KpiConfig>()
                            .eq("period", e.getPeriod())
            );

            if (next == null){
                KpiConfig config = new KpiConfig();
                config.setPeriod(e.getPeriod())
                        .setUserFlag("N")
                        .setIndexFlag("9")
                        .setDefaultKsFlag("N")
                        .setIndexFlagKs("9")
                        .setIssuedFlag("N")
                        .setImputationFlag("N")
                        .setDefaultFlag("N")
                        .setUserFlagKs("N");
                kpiConfigMapper.insert(config);
            }
        }
    }


    @Override
    public void del(Long id) {
        KpiAccountTask byId = getById(id);
        byId.setDelFlag("1");
        updateById(byId);
    }

    @Override
    public void issued(IssuedDTO input) {

        KpiAccountTask byId = getById(input.getId());
        KpiConfig kpiConfig = kpiConfigMapper.selectOne(
                new QueryWrapper<KpiConfig>()
                        .eq("period", byId.getPeriod())
        );
        if ("Y".equals(kpiConfig.getIssuedFlag())){
            throw new BizException("本周期已锁定，请勿重复锁定");
        }
        byId.setIssuedFlag("Y");
        byId.setIssuedDate(new Date());
        if ("Y".equals(input.getNoSecondAssign())){
            byId.setSendFlag("Y");
            byId.setSendDate(new Date());
            byId.setSendLog("");
            ReportConfigCopyDTO input2 = new ReportConfigCopyDTO();
            input2.setTaskChildId(byId.getTaskChildId());
            input2.setPeriod(byId.getPeriod());
            this.reportConfig(input2);
        }
        updateById(byId);

        KpiAccountTaskChild child = kpiAccountTaskChildService.getById(byId.getTaskChildId());
        child.setIssuedFlag("Y");
        child.setIssuedDate(new Date());
        if (byId.getPeriod().toString().endsWith("13")){
            child.setStatusName("已完成");
        }
        kpiAccountTaskChildService.updateById(child);

        kpiConfig.setIssuedFlag("Y");
        kpiConfig.setDefaultFlag("N");
        kpiConfig.setIssuedDate(new Date());
        kpiConfig.setTaskChildId(byId.getTaskChildId());
        kpiConfigMapper.updateById(kpiConfig);

        //todo 下发其他
        memberCopy(child.getTenantId(), child.getPeriod());

        if (byId.getPeriod().toString().endsWith("13")){
            return;
        }
        Long nextPeriod = getNextPeriod(byId.getPeriod());
        KpiConfig next = kpiConfigMapper.selectOne(
                new QueryWrapper<KpiConfig>()
                        .eq("period", nextPeriod)
        );
        if (next == null){
            KpiConfig config = new KpiConfig();
            config.setPeriod(nextPeriod)
                    .setUserFlag("N")
                    .setIndexFlag("9")
                    .setDefaultKsFlag("N")
                    .setIndexFlagKs("9")
                    .setIssuedFlag("N")
                    .setImputationFlag("N")
                    .setDefaultFlag("Y")
                    .setUserFlagKs("N");
            kpiConfigMapper.insert(config);
        }else{
            kpiConfigMapper.updateDef();
            next.setDefaultFlag("Y");
            kpiConfigMapper.updateById(next);
        }

    }

    public Long getNextPeriod(Long period){
        if(((period+1)+"").endsWith("13")){
            String substring = period.toString().substring(0, 4);

            return Long.parseLong(Long.parseLong(substring)+1 + "01");
        }else {
            return period+1;
        }
    }

    @Override
    public void unlock(Long id) {

        KpiConfig kpiConfig = kpiConfigMapper.selectById(id);
        KpiAccountTaskChild child = kpiAccountTaskChildService.getById(kpiConfig.getTaskChildId());
        child.setIssuedFlag("N");
        child.setIssuedDate(null);
        kpiAccountTaskChildService.updateById(child);

        KpiAccountTask byId = kpiAccountTaskMapper.selectById(child.getTaskId());
        byId.setIssuedFlag("N");
        byId.setSendFlag("N");
        byId.setIssuedDate(null);
        updateById(byId);

        kpiConfig.setIssuedFlag("N");
        kpiConfig.setIssuedDate(null);
        kpiConfig.setTaskChildId(null);
        kpiConfigMapper.updateById(kpiConfig);

    }

    @Override
    public String log(Long taskChildId) {
        KpiAccountTaskChild child = kpiAccountTaskChildService.getById(taskChildId);
        return child == null ? "" : child.getRunLog();
    }

    @Override
    public String send_log(Long taskId) {
        KpiAccountTask task = getById(taskId);
        return task.getSendLog();
    }

    @Override
    public String logErro(Long taskChildId) {
        KpiAccountTaskChild child = kpiAccountTaskChildService.getById(taskChildId);
        return child.getEroLog();
    }

    @Override
    public KpiTaskReportLeftVO reportLeft(Long taskChildId) {
        KpiTaskReportLeftVO output = new KpiTaskReportLeftVO();
        List<KpiAccountPlanCopy> list = kpiAccountPlanCopyMapper.selectList(
                new QueryWrapper<KpiAccountPlanCopy>()
                        .eq("task_child_id", taskChildId)
        );
        List<KpiReportLeftDto> indexs = kpiAccountPlanChildCopyMapper.getIndexs(taskChildId);

        list.forEach(r -> {
            KpiKeyValueVO vo = new KpiKeyValueVO();
            vo.setKey(r.getPlanCode());
            vo.setValue(r.getPlanName());
            vo.setIndexs(Linq.of(indexs).where(t->t.getPlanCode().equals(r.getPlanCode())).select(t -> {
                KpiKeyValueVO kpiKeyValueVO = new KpiKeyValueVO();
                kpiKeyValueVO.setKey(t.getCode());
                kpiKeyValueVO.setValue(t.getName());
                return kpiKeyValueVO;
            }).toList());
            output.getPlans().add(vo);
        });
        return output;
    }

    @Override
    public KpiCalculateReportVO reportCalculate(KpiCalculateReportDTO input) {
        KpiCalculateReportVO output = new KpiCalculateReportVO();
        List<KpiKeyValueVO> li = new ArrayList<>();
        KpiAccountTask task = kpiAccountTaskMapper.selectById(input.getId());

        QueryWrapper<KpiCalculate> qw = new QueryWrapper<KpiCalculate>();
        if (StringUtil.isNullOrEmpty(input.getIds())) {
            if (input.getId()==null){
                KpiAccountTaskChild child = kpiAccountTaskChildMapper.selectById(input.getTaskChildId());
                input.setId(child.getTaskId());
            }
            qw.eq("period", task.getPeriod())
                    .eq("task_child_id", input.getTaskChildId())
                    .eq("code", input.getIndexCode())
                    .in("imputation_type", Arrays.asList("0,1".split(",")));
            if ("N".equals(task.getTestFlag()) && !StringUtil.isNullOrEmpty(input.getPlanCode())) {
                qw.eq("plan_code", input.getPlanCode());
            }
            if (input.getFilterZero() != null && input.getFilterZero() == 1) {
                qw.ne("value", 0);
            }
//            if (input.getType() != 0) {
//                if (input.getType() == 1 && input.getId() != null) {
//                    qw.eq("user_id", input.getId());
//                } else if (input.getType() == 2 && input.getId() != null) {
//                    qw.eq("dept_id", input.getId());
//                }
//            }
        }else{
            qw.in("id", Arrays.asList(input.getIds().split(",")));
        }
        List<KpiCalculate> calculates = kpiCalculateMapper.selectList(qw);

        List<KpiCalculateComp> comps = new ArrayList<>();
        if (!calculates.isEmpty() && task != null && "Y".equals(task.getTestFlag())) {
            comps = kpiCalculateCompMapper.selectList(new QueryWrapper<KpiCalculateComp>()
                    .eq("period", calculates.get(0).getPeriod())
                    .in("imputation_type", Arrays.asList("0,1".split(",")))
                    .eq("code", task.getIndexCode()));
        }
        for (KpiCalculate r : calculates) {
            if (!"1".equals(r.getImputationType())) {
                li.addAll(Linq.of(JSON.parseObject(r.getResultJson(), KpiFormulaDto2.class).getFieldList()).select(t -> {
                    KpiKeyValueVO dto = new KpiKeyValueVO();
                    dto.setKey(t.getCode());
                    dto.setValue(t.getFieldName());
                    return dto;
                }).toList());
            }
            if (!comps.isEmpty() && r.getCompValue() == null){
                KpiCalculateComp comp = Linq.of(comps).firstOrDefault(x ->
                        (StringUtil.isNullOrEmpty(x.getUserName()) || x.getUserName().equals(r.getUserName()))
                                && (StringUtil.isNullOrEmpty(x.getDeptName()) || x.getDeptName().equals(r.getDeptName()))
                                && (StringUtil.isNullOrEmpty(x.getOutName()) || x.getOutName().equals(r.getOutName()))
                );
                if (comp!=null){
                    r.setCompValue(comp.getCompValue());
                }
            }
        }

        if (input.getFilterEro() != null && input.getFilterEro()==1){
            calculates=Linq.of(calculates).where(ca-> !new BigDecimal(0).equals(ca.getValue()) && (ca.getCompValue()==null ||
                    ca.getCompValue().subtract(ca.getValue()).abs().compareTo(new BigDecimal(0.00001))>0)).toList();
        }
        output.setCalculates(calculates);
        output.setSum(Linq.of(calculates).select(r -> r.getValue()).sumDecimal());
        output.setHead(Linq.of(li).stream().distinct().collect(Collectors.toList()));
        return output;
    }

    @Override
    public KpiCalculateReportVO reportCalculate2(KpiCalculateReportDTO input) {
        KpiCalculateReportVO output = new KpiCalculateReportVO();
        KpiAccountTask task = new KpiAccountTask();

        QueryWrapper<KpiItemResultCopyDTO> qw = new QueryWrapper<KpiItemResultCopyDTO>();
        if (StringUtil.isNullOrEmpty(input.getIds())) {
            if (input.getId()==null){
                KpiAccountTaskChild child = kpiAccountTaskChildMapper.selectById(input.getTaskChildId());
                input.setId(child.getTaskId());
            }
            task = kpiAccountTaskMapper.selectById(input.getId());
            qw.eq("period", task.getPeriod())
                    .eq("task_child_id", input.getTaskChildId())
                    .eq("code", input.getIndexCode());

            if (input.getFilterZero() != null && input.getFilterZero() == 1) {
                qw.ne("value", 0);
            }
        }else{
            if (input.getId()!=null){
                task = kpiAccountTaskMapper.selectById(input.getId());
            }else if (input.getTaskChildId() != null){
                task = kpiAccountTaskMapper.selectOne(
                        new QueryWrapper<KpiAccountTask>()
                                .eq("task_child_id", input.getTaskChildId())
                );
            }
            qw.in("id", Arrays.asList(input.getIds().split(",")));
        }
        List<KpiItemResultCopyDTO> calculates = kpiItemResultCopyMapper.getList(qw);
        calculates = itemResultConvert(calculates,task,input.getTaskChildId());

        output.setItemResults(calculates);
        output.setSum(Linq.of(calculates).select(r -> r.getValue()).sumDecimal());
        return output;
    }

    public List<KpiItemResultCopyDTO> itemResultConvert(List<KpiItemResultCopyDTO> calculates, KpiAccountTask task, Long task_child_id,
                                                        List<KpiUserAttendanceCopy> kpiUserAttendances,
                                                        List<KpiAccountUnitCopy> kpiAccountUnits,
                                                        List<KpiClusterUnitCopy> kpiClusterUnits) {
        for (KpiItemResultCopyDTO calculate : calculates) {
            if (calculate.getDeptId() != null) {
                KpiAccountUnitCopy first = Linq.of(kpiAccountUnits).firstOrDefault(x -> calculate.getDeptId().equals(x.getId()));
                if (first != null) {
                    calculate.setDeptName(first.getName());
                }
            }
             if (calculate.getUserId()!=null) {
                KpiUserAttendanceCopy first = Linq.of(kpiUserAttendances).firstOrDefault(x -> calculate.getUserId().equals(x.getUserId()));
                if (first!=null){
                    calculate.setUserName(first.getEmpName());
                }
            }
            if (calculate.getZdys()!=null) {
                KpiUserAttendanceCopy first = Linq.of(kpiUserAttendances).firstOrDefault(x -> calculate.getZdys().equals(x.getUserId()));
                if (first!=null){
                    calculate.setZdysName(first.getEmpName());
                }
            }
            if (calculate.getKzys()!=null) {
                KpiUserAttendanceCopy first = Linq.of(kpiUserAttendances).firstOrDefault(x -> calculate.getKzys().equals(x.getUserId()));
                if (first!=null){
                    calculate.setKzysName(first.getEmpName());
                }
            }
            if (calculate.getBrks()!=null) {
                KpiAccountUnitCopy first = Linq.of(kpiAccountUnits).firstOrDefault(x -> calculate.getBrks().equals(x.getId()));
                if (first!=null){
                    calculate.setBrksName(first.getName());
                }
            }
            if (calculate.getZdysks()!=null) {
                KpiAccountUnitCopy first = Linq.of(kpiAccountUnits).firstOrDefault(x -> calculate.getZdysks().equals(x.getId()));
                if (first!=null){
                    calculate.setZdysksName(first.getName());
                }
            }
            if (calculate.getKzysks()!=null) {
                KpiAccountUnitCopy first = Linq.of(kpiAccountUnits).firstOrDefault(x -> calculate.getKzysks().equals(x.getId()));
                if (first!=null){
                    calculate.setKzysksName(first.getName());
                }
            }
            if (calculate.getWard()!=null) {
                KpiAccountUnitCopy first = Linq.of(kpiAccountUnits).firstOrDefault(x -> calculate.getWard().equals(x.getId()));
                if (first!=null){
                    calculate.setWardName(first.getName());
                }
            }
            if (calculate.getBrbq()!=null) {
                KpiAccountUnitCopy first = Linq.of(kpiAccountUnits).firstOrDefault(x -> calculate.getBrbq().equals(x.getId()));
                if (first!=null){
                    calculate.setBrbqName(first.getName());
                }
            }
            if (calculate.getImputationDeptId()!=null) {
                KpiClusterUnitCopy first = Linq.of(kpiClusterUnits).firstOrDefault(x -> calculate.getImputationDeptId().equals(x.getId()));
                if (first!=null){
                    calculate.setImputationDeptIdName(first.getName());
                }
            }
        }
        return calculates;
    }

    public List<KpiItemResultCopyDTO> itemResultConvert(List<KpiItemResultCopyDTO> calculates, KpiAccountTask task, Long task_child_id) {
        List<KpiUserAttendanceCopy> kpiUserAttendances = new ArrayList<>();
        List<KpiAccountUnitCopy> kpiAccountUnits = new ArrayList<>();
        List<KpiClusterUnitCopy> kpiClusterUnits = new ArrayList<>();
        if ("N".equals(task.getTestFlag())) {
            kpiUserAttendances = kpiUserAttendanceCopyMapper.selectList(
                    new QueryWrapper<KpiUserAttendanceCopy>()
                            .eq("task_child_id", task_child_id)
            );
            kpiAccountUnits = kpiAccountUnitCopyMapper.selectList(
                    new QueryWrapper<KpiAccountUnitCopy>()
                            .eq("task_child_id", task_child_id)
            );
            kpiClusterUnits = kpiClusterUnitCopyMapper.selectList(
                    new QueryWrapper<KpiClusterUnitCopy>()
                            .eq("task_child_id", task_child_id)
            );
        }
        else{
            String str = MemberEnum.IMPUTATION_DEPT_EMP.getType() + "," + MemberEnum.ROLE_EMP.getType() + "," + MemberEnum.EMP_TYPE.getType();

            kpiUserAttendances = kpiUserAttendanceCopyMapper.getList(
                    new QueryWrapper<KpiUserAttendanceCopy>()
                            .eq("period", task.getPeriod())
                            .eq("del_flag", "0")
                            .eq("tenant_id", task.getTenantId())
                            .eq("busi_type", "1")
            );
            kpiAccountUnits = kpiAccountUnitCopyMapper.getList(
                    new QueryWrapper<KpiAccountUnitCopy>()
                            .eq("del_flag", "0")
                            .eq("status", "0")
                            .eq("tenant_id", task.getTenantId())
                            .eq("busi_type", "1")
            );
            kpiClusterUnits = kpiClusterUnitCopyMapper.getList(
                    new QueryWrapper<KpiClusterUnitCopy>()
                            .eq("tenant_id", task.getTenantId())
            );
        }
        return itemResultConvert(calculates, task, task_child_id, kpiUserAttendances, kpiAccountUnits, kpiClusterUnits);
    }

    @Override
    public KpiCalculateReportVO reportDetail(KpiCalculateDetailDTO input) {
        KpiCalculate rr = kpiCalculateMapper.selectById(input.getId());
        List<KpiKeyValueVO> li = new ArrayList<>();
        KpiCalculateReportVO output = new KpiCalculateReportVO();

        KpiFormulaDto2 kpiFormulaDto2 = JSON.parseObject(rr.getResultJson(), KpiFormulaDto2.class);
        if (StringUtil.isNullOrEmpty(input.getCode())) {
            throw new BizException("code不能为空");
        }

        KpiAccountTask task = kpiAccountTaskMapper.selectOne(
                new QueryWrapper<KpiAccountTask>()
                        .eq("task_child_id", input.getTaskChildId())
        );
        if (task == null ){
            throw new BizException("任务不存在");
        }
        List<KpiMemberCopy> kpiMembers = new ArrayList<>();
        List<KpiUserAttendanceCopy> kpiUserAttendances = new ArrayList<>();
        List<KpiAccountUnitCopy> kpiAccountUnits = new ArrayList<>();
        List<Long> depts = new ArrayList<>();

        if (input.getReportId()!=null){
            //权限
            if (!SecurityUtils.getUser().getAdminFlag()){
                List<KpiReportConfigPower> list = kpiReportConfigPowerMapper.selectList(
                        new QueryWrapper<KpiReportConfigPower>()
                                .eq("user_id", SecurityUtils.getUser().getId())
                                .eq("report_id",input.getReportId())
                                .eq("type","3")
                );
                if (!CollectionUtil.isEmpty(list)){
                    depts.addAll(Linq.of(list).select(x->x.getDeptId()).toList());
                }
                List<GrantUnit> list1 = grantUnitService.list(new QueryWrapper<GrantUnit>().like("leader_ids", SecurityUtils.getUser().getId()).select("ks_unit_ids","ks_unit_ids_non_staff"));
                for (GrantUnit grantUnit : list1) {
                    if(grantUnit != null) {
                        if (!StringUtil.isNullOrEmpty(grantUnit.getKsUnitIdsNonStaff())) {
                            for (String s : grantUnit.getKsUnitIdsNonStaff().split(",")) {
                                if (!StringUtil.isNullOrEmpty(s)) {
                                    depts.add(Long.parseLong(s));
                                }
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(grantUnit.getKsUnitIds())) {
                            for (String s : grantUnit.getKsUnitIds().split(",")) {
                                if (!StringUtil.isNullOrEmpty(s)) {
                                    depts.add(Long.parseLong(s));
                                }
                            }
                        }
                    }
                }
                depts=depts.stream().distinct().collect(Collectors.toList());
            }else{
                depts.add(-200L);
            }
            if (CollectionUtil.isEmpty(depts)) {
                depts.add(-1L);
            }
        }

        List<SysUser> users = kpiCalculateMapper.getUsers(SecurityUtils.getUser().getTenantId());
        if ("N".equals(task.getTestFlag())) {
            kpiMembers = kpiMemberCopyMapper.selectList(
                    new QueryWrapper<KpiMemberCopy>()
                            .eq("task_child_id", input.getTaskChildId())
            );
            kpiUserAttendances = kpiUserAttendanceCopyMapper.selectList(
                    new QueryWrapper<KpiUserAttendanceCopy>()
                            .eq("task_child_id", input.getTaskChildId())
            );
            kpiAccountUnits = kpiAccountUnitCopyMapper.selectList(
                    new QueryWrapper<KpiAccountUnitCopy>()
                            .eq("task_child_id", input.getTaskChildId())
            );
        }
        else{
            String str = MemberEnum.IMPUTATION_DEPT_EMP.getType() + "," + MemberEnum.ROLE_EMP.getType() + "," + MemberEnum.EMP_TYPE.getType();
            kpiMembers = kpiMemberCopyMapper.getList(
                    new QueryWrapper<KpiMemberCopy>()
                            .in("period", Arrays.asList((task.getPeriod() + ",0").split(",")))
                            .eq("tenant_id", task.getTenantId())
                            .in("member_type", Arrays.asList(str.split(",")))
                            .eq("busi_type", "1")
            );
            kpiUserAttendances = kpiUserAttendanceCopyMapper.getList(
                    new QueryWrapper<KpiUserAttendanceCopy>()
                            .eq("period", task.getPeriod())
                            .eq("del_flag", "0")
                            .eq("tenant_id", task.getTenantId())
                            .eq("busi_type", "1")
            );
            kpiAccountUnits = kpiAccountUnitCopyMapper.getList(
                    new QueryWrapper<KpiAccountUnitCopy>()
                            .eq("del_flag", "0")
                            .eq("status", "0")
                            .eq("tenant_id", task.getTenantId())
                            .eq("busi_type", "1")
            );
        }

        QueryWrapper<KpiCalculate> qw = new QueryWrapper<KpiCalculate>()
                .eq("task_child_id", input.getTaskChildId())
                .eq("period",rr.getPeriod());
        //.ne("value", 0);
        if (!CollectionUtil.isEmpty(depts)){
            if(!Linq.of(depts).any(t->t.equals(-200L)))
            {
                qw.in("dept_id",depts);
            }
        }
        if (input.getFilterZero() != null && input.getFilterZero()==1){
            qw.ne("value", 0);
        }
        if (input.getImputationType() == 0) {
            KpiFormulaDto2.FieldListDTO dto = Linq.of(kpiFormulaDto2.getFieldList()).firstOrDefault(t -> t.getCode().equals(input.getCode()));
            qw.eq("code",dto.getFieldCode());
            qw.in("imputation_type", Arrays.asList("1,0".split(",")));
            CalAllDto allDto = new CalAllDto();
            allDto.setParam(dto);
            if(rr.getUserId()!=null)
            {
                allDto.setMemberId(rr.getUserId());
            } else {
                allDto.setMemberId(rr.getDeptId());
            }
            if (dto.getParamType().startsWith("1") || dto.getParamType().startsWith("31")) {
                //allDto.setMemberId(rr.getUserId());
                allDto.setAlloEmpFlag(true);
                qw.in("user_id", getMemberList(allDto, rr, dto.getAllImpMembers(), kpiMembers, kpiUserAttendances, kpiAccountUnits,users));
                if (FormulaParamEnum.P_16.getType().equals(dto.getParamType())) {
                    qw.eq("dept_id", rr.getDeptId());
                }

            } else if (dto.getParamType().startsWith("2") || dto.getParamType().startsWith("32")) {
                //allDto.setMemberId(rr.getDeptId());
                allDto.setAlloEmpFlag(false);
                if (FormulaParamEnum.P_24.getType().equals(dto.getParamType())) {
                    qw.eq("dept_id", rr.getDeptId());
                }
                else if (FormulaParamEnum.P_26.getType().equals(dto.getParamType())) {
                    qw.eq("dept_id", rr.getDeptId());
                }
                else
                {
                    qw.in("dept_id", getMemberList(allDto, rr, dto.getAllImpMembers(), kpiMembers, kpiUserAttendances, kpiAccountUnits,users));
                }

            }

        } else if (input.getImputationType() == 1) {
            if (StringUtil.isNullOrEmpty(input.getImputationCode())) {
                throw new BizException("归集code不能为空");
            }
            qw.eq("imputation_code", input.getImputationCode())
                    .eq("imputation_type", "2");
        }
        List<KpiCalculate> calculates = kpiCalculateMapper.selectList(qw);

        List<KpiCalculateComp> comps = new ArrayList<>();
        if (!calculates.isEmpty() && task != null && "Y".equals(task.getTestFlag())) {
            comps = kpiCalculateCompMapper.selectList(new QueryWrapper<KpiCalculateComp>()
                    .eq("period", calculates.get(0).getPeriod())
                    .eq("imputation_type", "2")
                    .eq("code", task.getIndexCode()));
        }

        for (KpiCalculate r : calculates) {
            li.addAll(Linq.of(JSON.parseObject(r.getResultJson(), KpiFormulaDto2.class).getFieldList()).select(t -> {
                KpiKeyValueVO dto = new KpiKeyValueVO();
                dto.setKey(t.getCode());
                dto.setValue(t.getFieldName());
                return dto;
            }).toList());

            if (!comps.isEmpty() && r.getCompValue() == null){
                KpiCalculateComp comp = Linq.of(comps).firstOrDefault(x ->
                        (StringUtil.isNullOrEmpty(x.getUserName()) || x.getUserName().equals(r.getUserName()))
                                && (StringUtil.isNullOrEmpty(x.getDeptName()) || x.getDeptName().equals(r.getDeptName()))
                                && (StringUtil.isNullOrEmpty(x.getOutName()) || x.getUserName().equals(r.getOutName()))
                );
                if (comp!=null){
                    r.setCompValue(comp.getCompValue());
                }
            }
        }
        if (input.getFilterEro() != null && input.getFilterEro()==1){
            calculates=Linq.of(calculates).where(ca->ca.getCompValue()!=null &&
                    ca.getCompValue().subtract(ca.getValue()).abs().compareTo(new BigDecimal(0.000001))>0).toList();
        }
        output.setCalculates(calculates);
        output.setSum(Linq.of(calculates).select(r -> r.getValue()).sumDecimal());
        output.setHead(Linq.of(li).stream().distinct().collect(Collectors.toList()));
        return output;
    }

    @Override
    public KpiCalculateReportVO reportDetail2(KpiCalculateDetailDTO input) {
        KpiCalculate rr = kpiCalculateMapper.selectById(input.getId());
        KpiCalculateReportVO output = new KpiCalculateReportVO();

        KpiAccountTask task = kpiAccountTaskMapper.selectOne(
                new QueryWrapper<KpiAccountTask>()
                        .eq("task_child_id", input.getTaskChildId())
        );
        if (task == null ){
            throw new BizException("任务不存在");
        }
        KpiFormulaDto2 kpiFormulaDto2 = JSON.parseObject(rr.getResultJson(), KpiFormulaDto2.class);
        KpiFormulaDto2.FieldListDTO dto = Linq.of(kpiFormulaDto2.getFieldList()).first(x -> x.getCode().equals(input.getCode()));

        List<Long> depts = new ArrayList<>();
        List<KpiMemberCopy> kpiMembers = new ArrayList<>();
        List<SysUser> users = kpiCalculateMapper.getUsers(SecurityUtils.getUser().getTenantId());

        List<KpiUserAttendanceCopy> kpiUserAttendances = new ArrayList<>();
        List<KpiAccountUnitCopy> kpiAccountUnits = new ArrayList<>();
        List<KpiItemCopy> kpiItems = new ArrayList<>();
        List<KpiClusterUnitCopy> kpiClusterUnits = new ArrayList<>();
        if ("N".equals(task.getTestFlag())) {
            kpiMembers = kpiMemberCopyMapper.selectList(
                    new QueryWrapper<KpiMemberCopy>()
                            .eq("task_child_id", input.getTaskChildId())
            );
            kpiUserAttendances = kpiUserAttendanceCopyMapper.selectList(
                    new QueryWrapper<KpiUserAttendanceCopy>()
                            .eq("task_child_id", input.getTaskChildId())
            );
            kpiAccountUnits = kpiAccountUnitCopyMapper.selectList(
                    new QueryWrapper<KpiAccountUnitCopy>()
                            .eq("task_child_id", input.getTaskChildId())
            );
            kpiItems = kpiItemCopyMapper.selectList(
                    new QueryWrapper<KpiItemCopy>()
                            .eq("task_child_id", input.getTaskChildId())
                            .eq("del_flag", "0")
                            .eq("status", "0")
                            .eq("tenant_id", task.getTenantId())
                            .eq("busi_type", "1")
            );
            kpiClusterUnits = kpiClusterUnitCopyMapper.selectList(
                    new QueryWrapper<KpiClusterUnitCopy>()
                            .eq("task_child_id", input.getTaskChildId())
            );
        }
        else{
            String str = MemberEnum.IMPUTATION_DEPT_EMP.getType() + "," + MemberEnum.ROLE_EMP.getType() + "," + MemberEnum.EMP_TYPE.getType();
            kpiMembers = kpiMemberCopyMapper.getList(
                    new QueryWrapper<KpiMemberCopy>()
                            .in("period", Arrays.asList((task.getPeriod() + ",0").split(",")))
                            .eq("tenant_id", task.getTenantId())
                            .in("member_type", Arrays.asList(str.split(",")))
                            .eq("busi_type", "1")
            );
            kpiUserAttendances = kpiUserAttendanceCopyMapper.getList(
                    new QueryWrapper<KpiUserAttendanceCopy>()
                            .eq("period", task.getPeriod())
                            .eq("del_flag", "0")
                            .eq("tenant_id", task.getTenantId())
                            .eq("busi_type", "1")
            );
            kpiAccountUnits = kpiAccountUnitCopyMapper.getList(
                    new QueryWrapper<KpiAccountUnitCopy>()
                            .eq("del_flag", "0")
                            .eq("status", "0")
                            .eq("tenant_id", task.getTenantId())
                            .eq("busi_type", "1")
            );
            kpiItems = kpiItemCopyMapper.getList2(
                    new QueryWrapper<KpiItemCopy>()
                            .eq("del_flag", "0")
                            .eq("status", "0")
                            .eq("tenant_id", task.getTenantId())
                            .eq("busi_type", "1")
            );
            kpiClusterUnits = kpiClusterUnitCopyMapper.getList(
                    new QueryWrapper<KpiClusterUnitCopy>()
                            .eq("tenant_id", task.getTenantId())
            );
        }
        if (input.getReportId() != null) {
            //权限
            if (!SecurityUtils.getUser().getAdminFlag()) {
                List<KpiReportConfigPower> list = kpiReportConfigPowerMapper.selectList(
                        new QueryWrapper<KpiReportConfigPower>()
                                .eq("user_id", SecurityUtils.getUser().getId())
                                .eq("report_id", input.getReportId())
                                .eq("type", "3")
                );
                if (!CollectionUtil.isEmpty(list)) {
                    depts.addAll(Linq.of(list).select(x -> x.getDeptId()).toList());
                }
                List<GrantUnit> list1 = grantUnitService.list(new QueryWrapper<GrantUnit>().like("leader_ids", SecurityUtils.getUser().getId()).select("ks_unit_ids", "ks_unit_ids_non_staff"));
                for (GrantUnit grantUnit : list1) {
                    if (grantUnit != null) {
                        if (!StringUtil.isNullOrEmpty(grantUnit.getKsUnitIdsNonStaff())) {
                            for (String s : grantUnit.getKsUnitIdsNonStaff().split(",")) {
                                if (!StringUtil.isNullOrEmpty(s)) {
                                    depts.add(Long.parseLong(s));
                                }
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(grantUnit.getKsUnitIds())) {
                            for (String s : grantUnit.getKsUnitIds().split(",")) {
                                if (!StringUtil.isNullOrEmpty(s)) {
                                    depts.add(Long.parseLong(s));
                                }
                            }
                        }
                    }
                }
                depts = depts.stream().distinct().collect(Collectors.toList());
            } else {
                depts.add(-200L);
            }
            if (CollectionUtil.isEmpty(depts)) {
                depts.add(-1L);
            }
        }
        QueryWrapper<KpiItemResultCopyDTO> qw = new QueryWrapper<KpiItemResultCopyDTO>()
                .eq("period", rr.getPeriod());
        if (!task.getTestFlag().equals("Y")) {
            qw.eq("task_child_id", input.getTaskChildId());
        }
        if (!CollectionUtil.isEmpty(depts)) {
            if (!Linq.of(depts).any(t -> t.equals(-200L))) {
                qw.in("dept_id", depts);
            }
        }

        CalAllDto allDto = new CalAllDto();
        allDto.setParam(dto);
        if(rr.getUserId()!=null)
        {
            allDto.setMemberId(rr.getUserId());
        } else {
            allDto.setMemberId(rr.getDeptId());
        }
        if(!"4".equals(dto.getParamCate())) {
            if (dto.getParamType().startsWith("1") || dto.getParamType().startsWith("31")) {
                //allDto.setMemberId(rr.getUserId());
                allDto.setAlloEmpFlag(true);
                qw.in("user_id", getMemberList(allDto, rr, dto.getAllImpMembers(), kpiMembers, kpiUserAttendances, kpiAccountUnits,users));
                if (FormulaParamEnum.P_16.getType().equals(dto.getParamType())) {
                    qw.eq("dept_id", rr.getDeptId());
                }

            } else if (dto.getParamType().startsWith("2") || dto.getParamType().startsWith("32")) {
                //allDto.setMemberId(rr.getDeptId());
                allDto.setAlloEmpFlag(false);
                if (FormulaParamEnum.P_24.getType().equals(dto.getParamType())) {
                    qw.eq("dept_id", rr.getDeptId());
                }
                else if (FormulaParamEnum.P_26.getType().equals(dto.getParamType())) {
                    qw.eq("dept_id", rr.getDeptId());
                }else {
                    qw.in("dept_id", getMemberList(allDto, rr, dto.getAllImpMembers(), kpiMembers, kpiUserAttendances, kpiAccountUnits,users));
                }
            }
        }

        //正常核算项下转
        if (StringUtil.isNullOrEmpty(input.getCodeType())) {
            List<KpiItemResultCopyDTO> calculates = null;
            qw.eq("code",dto.getFieldCode());
            if (input.getFilterZero() != null && input.getFilterZero() == 1) {
                qw.ne("value", 0);
            }
            if (!task.getTestFlag().equals("Y")) {
                calculates = kpiItemResultCopyMapper.getList(qw);
            } else {
                calculates = kpiItemResultCopyMapper.getList2(qw);
            }
            calculates = itemResultConvert(calculates, task, input.getTaskChildId(), kpiUserAttendances, kpiAccountUnits, kpiClusterUnits);

            output.setItemResults(calculates);
            output.setSum(Linq.of(calculates).select(r -> r.getValue()).sumDecimal());
        }else{
            //核算项项目分类指标下转
            List<KpiItemEquivalentCopyDTO> calculates = null;
            List<String> codes = Linq.of(kpiItems).where(x -> input.getCodeType().equals(x.getProCategoryCode())).select(x -> x.getCode()).toList();
            qw.in("code",codes);
            if (input.getFilterZero() != null && input.getFilterZero() == 1) {
                qw.ne("total_equivalent", 0);
            }
            if (!task.getTestFlag().equals("Y")) {
                calculates = kpiItemEquivalentCopyMapper.getList(qw);
            } else {
                calculates = kpiItemEquivalentCopyMapper.getList2(qw);
            }
            List<KpiItemEquivalentCopyDTO> rts = new ArrayList<>();
            for (KpiItemEquivalentCopyDTO r : calculates) {
                KpiItemCopy kpiItemCopy = Linq.of(kpiItems).firstOrDefault(t -> t.getCode().equals(r.getCode()));
                if (kpiItemCopy != null) {
                    r.setName(kpiItemCopy.getItemName());
                    if("1".equals(kpiItemCopy.getAssignFlag()))
                    {
                        r.setCaliber("1");
                    }
                    else {
                        r.setCaliber(kpiItemCopy.getCaliber());
                    }
                }
                rts.addAll(Linq.of(calculates).where(x->x.getCode().equals(r.getCode()) && x.getEquivalentType().equals(r.getCaliber())).toList());
            }

            output.setItemEquivalent(rts);
            output.setSum(Linq.of(rts).select(r -> r.getTotalEquivalent()).sumDecimal());
        }
        return output;
    }

    public List<KpiMemberCopy> planMemberCopy(Long period) {
        List<KpiMemberCopy> outputs = new ArrayList<>();
        String str = MemberEnum.ACCOUNT_UNIT_RELATION.getType()
                + "," + MemberEnum.IMPUTATION_DEPT_EMP.getType()
                + "," + MemberEnum.ROLE_EMP.getType()
                + "," + MemberEnum.EMP_TYPE.getType()
                + "," + MemberEnum.ROLE_EMP_ZW.getType();
        List<KpiMember> list = kpiMemberMapper.selectList(
                new QueryWrapper<KpiMember>()
                        .in("period", Arrays.asList((period + ",0").split(",")))
                        .in("member_type", Arrays.asList(str.split(",")))
                        .eq("busi_type", "1")
        );
        List<KpiMemberCopy> list_copy = new ArrayList<>();
        list.forEach(r -> {
            KpiMemberCopy entity = new KpiMemberCopy();
            entity.setPeriod(r.getPeriod())
                    .setHostId(r.getHostId())
                    .setHostCode(r.getHostCode())
                    .setMemberId(r.getMemberId())
                    .setTenantId(r.getTenantId())
                    .setMemberCode(r.getMemberCode())
                    .setMemberType(r.getMemberType());
            entity.setCopyDate(new Date());
            list_copy.add(entity);
        });
        outputs.addAll(list_copy);
        return outputs;
    }

    public List<KpiUserAttendanceCopy> planUserCopy2(Long period) {
        List<KpiUserAttendanceCopy> outputs = new ArrayList<>();
        List<KpiUserAttendance> list = kpiUserAttendanceMapper.selectList(
                new QueryWrapper<KpiUserAttendance>()
                        .eq("period", period)
                        .eq("del_flag", "0")
                        .eq("busi_type", "1")
        );
        List<KpiUserAttendanceCopy> list_copy = new ArrayList<>();
        list.forEach(r -> {
            KpiUserAttendanceCopy entity = new KpiUserAttendanceCopy();
            entity.setPeriod(r.getPeriod())
                    .setEmpId(r.getEmpId())
                    .setEmpName(r.getEmpName())
                    .setAttendanceGroup(r.getAttendanceGroup())
                    .setUserType(r.getUserType())
                    .setUserTypeCode(r.getUserTypeCode())
                    .setDutiesOrigin(r.getDutiesOrigin())
                    .setDutiesCode(r.getDutiesCode())
                    .setDutiesName(r.getDutiesName())
                    .setAccountGroup(r.getAccountGroup())
                    .setTitles(r.getTitles())
                    .setTitlesCode(r.getTitlesCode())
                    .setAccountUnit(r.getAccountUnit())
                    .setAttendCount(r.getAttendCount())
                    .setAttendRate(r.getAttendRate())
                    .setRegisteredRate(r.getRegisteredRate())
                    .setJobNature(r.getJobNature())
                    .setAttendDays(r.getAttendDays())
                    .setPost(r.getPost())
                    .setReward(r.getReward())
                    .setRewardIndex(r.getRewardIndex())
                    .setNoRewardReason(r.getNoRewardReason())
                    .setAttendanceGroupDays(r.getAttendanceGroupDays())
                    .setOneKpiAttendDays(r.getOneKpiAttendDays())
                    .setOneKpiAttendRate(r.getOneKpiAttendRate())
                    .setIsLocked(r.getIsLocked())
                    .setCustomFields(r.getCustomFields())
                    .setOriginCustomFields(r.getOriginCustomFields())
                    .setIsEdited(r.getIsEdited())
                    .setDelFlag(r.getDelFlag())
                    .setTenantId(r.getTenantId())
                    .setCreatedBy(r.getCreatedBy())
                    .setCreatedTime(r.getCreatedTime())
                    .setUpdateBy(r.getUpdateBy())
                    .setUpdateTime(r.getUpdateTime())
                    .setTreatRoomDays(r.getTreatRoomDays())
                    .setDeptCode(r.getDeptCode())
                    .setDeptName(r.getDeptName())
                    .setSourceType(r.getSourceType())
                    .setAccountUnitName(r.getAccountUnitName())
                    .setUserId(r.getUserId());
            entity.setCopyDate(new Date());
            list_copy.add(entity);
        });

        outputs.addAll(list_copy);
        return outputs;
    }


    public List<KpiAccountUnitCopy> planUnitCopy2() {
        List<KpiAccountUnitCopy> outputs = new ArrayList<>();
        List<KpiAccountUnit> list = kpiAccountUnitMapper.selectList(
                new QueryWrapper<KpiAccountUnit>()
                        .eq("del_flag", "0")
                        .eq("status", "0")
                        .eq("busi_type", "1")
        );
        List<KpiAccountUnitCopy> list_copy = new ArrayList<>();
        list.forEach(r -> {
            KpiAccountUnitCopy entity = new KpiAccountUnitCopy();
            entity.setId(r.getId())
                    .setName(r.getName())
                    .setCategoryCode(r.getCategoryCode())
                    .setAccountTypeCode(r.getAccountTypeCode())
                    .setResponsiblePersonId(r.getResponsiblePersonId())
                    .setResponsiblePersonType(r.getResponsiblePersonType())
                    .setStatus(r.getStatus())
                    .setDelFlag(r.getDelFlag())
                    .setInitialized(r.getInitialized())
                    .setCreatedId(r.getCreatedId())
                    .setCreatedDate(r.getCreatedDate())
                    .setUpdatedId(r.getUpdatedId())
                    .setUpdatedDate(r.getUpdatedDate())
                    .setTenantId(r.getTenantId())
                    .setAccountUserCode(r.getAccountUserCode());
            entity.setCopyDate(new Date());
            list_copy.add(entity);
        });

        outputs.addAll(list_copy);

        return outputs;
    }

    public List<Long> getMemberListComm_before(String rule){
        String period = kpiConfigService.getLastCycle(false);
        KpiFormulaDto2.FieldListDTO bean = JSONUtil.toBean(rule, KpiFormulaDto2.FieldListDTO.class);
        List<KpiMemberCopy> kpiMemberCopies = planMemberCopy(Long.valueOf(period));
        List<KpiUserAttendanceCopy> kpiUserAttendanceCopies = planUserCopy2(Long.valueOf(period));
        List<KpiAccountUnitCopy> kpiAccountUnitCopies = planUnitCopy2();
//kpiUserAtt
        return getMemberListComm(bean, kpiMemberCopies, kpiUserAttendanceCopies, kpiAccountUnitCopies);
    }

    public List<Long> getMemberListComm(KpiFormulaDto2.FieldListDTO dto, List<KpiMemberCopy> kpiMembers,
                                         List<KpiUserAttendanceCopy> kpiUserAttendances , List<KpiAccountUnitCopy> kpiAccountUnits) {
        List<Long> rt = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(dto.getParamType())) {
            FormulaParamEnum formulaParamEnum = FormulaParamEnum.find(dto.getParamType());
            List<KpiFormulaDto2.MemberListDTO> params = dto.getParamValues();
            List<KpiFormulaDto2.MemberListDTO> excludes = dto.getParamExcludes();
            switch (formulaParamEnum) {
                case P_11://自定义人员
                    rt.addAll(Linq.of(dto.getParamValues()).select(r -> Long.parseLong(r.getValue())).toList());
                    break;
                case P_12://人员类型（字典对应user_type）
                    List<Long> users = Linq.of(kpiMembers).where(t -> t.getMemberType().equals(MemberEnum.EMP_TYPE.getType())
                                    && Linq.of(params).select(x -> x.getValue()).toList().contains(t.getMemberCode()))
                            .select(t -> t.getHostId()).toList();
                    rt.addAll(users);
                    break;
                case P_14://按人员分组
                    List<Long> users_role = Linq.of(kpiMembers).where(t -> t.getMemberType().equals(MemberEnum.ROLE_EMP.getType())
                                    && Linq.of(params).select(x -> x.getValue()).toList().contains(t.getHostCode()))
                            .select(t -> t.getMemberId()).toList();
                    rt.addAll(users_role);
                    break;
                case P_15://按工作性质
                    List<Long> users_job = Linq.of(kpiUserAttendances).where(t ->
                                    Linq.of(params).select(x -> x.getValue()).toList()
                                            .contains(t.getJobNature()))
                            .select(t -> t.getUserId()).toList();
                    rt.addAll(users_job);
                    break;
                case P_100://全院人员
                    rt.addAll(Linq.of(kpiUserAttendances).select(r -> r.getUserId()).toList());
                    break;
                case P_21://自定义科室
                    rt.addAll(Linq.of(params).select(r -> Long.parseLong(r.getValue())).toList());
                    break;
                case P_22://核算类型（字典对应kpi_calculate_type）
                    List<KpiAccountUnitCopy> account_type = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && Linq.of(params).select(r -> r.getValue()).toList().contains(t.getAccountTypeCode())).toList();

                    rt.addAll(Linq.of(account_type).select(r -> r.getId()).toList());
                    break;
                case P_23://核算分组
                    List<KpiAccountUnitCopy> account_group = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && Linq.of(params).select(r -> r.getValue()).toList().contains(t.getCategoryCode())).toList();
                    rt.addAll(Linq.of(account_group).select(r -> r.getId()).toList());
                    break;
                case P_25://科室单元人员类型（字典对应user_type）
                    List<KpiAccountUnitCopy> account_rylx = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && Linq.of(params).select(r -> r.getValue()).toList().contains(t.getAccountUserCode())).toList();
                    rt.addAll(Linq.of(account_rylx).select(r -> r.getId()).toList());
                    break;
                case P_29://所有科室单元
                    rt.addAll(Linq.of(kpiAccountUnits).select(r -> r.getId()).toList());
                    break;

            }
            //口径剔除
            if (CollectionUtil.isNotEmpty(excludes)) {
                List<Long> list = new ArrayList<>();

                for (KpiFormulaDto2.MemberListDTO r : excludes) {
                    if (r.getType() == null
                            || r.getType().equals(FormulaParamEnum.P_11.getType())
                            || r.getType().equals(FormulaParamEnum.P_21.getType())) {
                        list.add(Long.parseLong(r.getValue()));
                    } else {
                        list.addAll(getMemberList(r.getType(), r.getValue(),kpiMembers,kpiUserAttendances,kpiAccountUnits));
                    }
                }
                rt.removeAll(list);
            }
        }
        return rt.stream().distinct().collect(Collectors.toList());
    }

    //报表getMemberList
    private List<Long> getMemberList(CalAllDto allDto,KpiCalculate rr,List<Long> allImpMembers,List<KpiMemberCopy> kpiMembers,
                                     List<KpiUserAttendanceCopy> kpiUserAttendances ,List<KpiAccountUnitCopy> kpiAccountUnits,List<SysUser> users) {
        List<Long> rt = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(allDto.getParam().getParamType())) {
            FormulaParamEnum formulaParamEnum = FormulaParamEnum.find(allDto.getParam().getParamType());
            switch (formulaParamEnum) {
                case P_16:
                case P_10://本人员
                case P_19:
                    rt.add(allDto.getMemberId());
                    break;
                case P_11://自定义人员
                    rt.addAll(Linq.of(allDto.getParam().getParamValues()).select(r -> Long.parseLong(r.getValue())).toList());
                    break;
                case P_12://人员类型（字典对应user_type）
                    List<Long> users2 = Linq.of(kpiMembers).where(t -> t.getMemberType().equals(MemberEnum.EMP_TYPE.getType())
                                    && Linq.of(allDto.getParam().getParamValues()).select(x -> x.getValue()).toList().contains(t.getMemberCode()))
                            .select(t -> t.getHostId()).toList();
                    rt.addAll(users2);
                    break;
                case P_13://按归集
                    List<Long> imps = Linq.of(kpiMembers)
                            .where(t -> t.getMemberType().equals(MemberEnum.IMPUTATION_DEPT_EMP.getType())
                                    && t.getHostId().equals(allDto.getMemberId())
                                    && t.getHostCode().equals(allDto.getIndex().getImpCategoryCode())).select(t -> t.getMemberId()).toList();
                    rt.addAll(imps);
                    break;
                case P_14://按人员分组
                    List<Long> users_role = Linq.of(kpiMembers).where(t -> t.getMemberType().equals(MemberEnum.ROLE_EMP.getType())
                                    && Linq.of(allDto.getParam().getParamValues()).select(x -> x.getValue()).toList().contains(t.getHostCode()))
                            .select(t -> t.getMemberId()).toList();
                    rt.addAll(users_role);
                    break;
                case P_15://按工作性质
                    List<Long> users_job = Linq.of(kpiUserAttendances).where(t ->
                                    Linq.of(allDto.getParam().getParamValues()).select(x -> x.getValue()).toList()
                                            .contains(t.getJobNature()))
                            .select(t -> t.getUserId()).toList();
                    rt.addAll(users_job);
                    break;
                case P_100://所有人员
                    rt.addAll(Linq.of(users).select(r -> r.getUserId()).toList());
                    break;
                case P_20://本科室单元
                    //归集指标 取归集科室，memberid为本人
                    if (allDto.getImpDeptId() != null) {
                        rt.add(allDto.getImpDeptId());
                    } else {
                        rt.add(allDto.getMemberId());
                    }
                    break;
                case P_21://自定义科室
                    rt.addAll(Linq.of(allDto.getParam().getParamValues()).select(r -> Long.parseLong(r.getValue())).toList());
                    break;
                case P_22://核算类型（字典对应kpi_calculate_type）
                    List<KpiAccountUnitCopy> account_type = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && Linq.of(allDto.getParam().getParamValues()).select(r -> r.getValue()).toList().contains(t.getAccountTypeCode())).toList();

                    rt.addAll(Linq.of(account_type).select(r -> r.getId()).toList());
                    break;
                case P_23://核算分组
                    List<KpiAccountUnitCopy> account_group = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && Linq.of(allDto.getParam().getParamValues()).select(r -> r.getValue()).toList().contains(t.getCategoryCode())).toList();
                    rt.addAll(Linq.of(account_group).select(r -> r.getId()).toList());
                    break;
                case P_24://本人员负责科室
                    List<KpiAccountUnitCopy> account_dept = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && t.getResponsiblePersonId() != null && Arrays.asList(t.getResponsiblePersonId().split(",")).contains(allDto.getMemberId().toString())).toList();
                    rt.addAll(Linq.of(account_dept).select(r -> r.getId()).toList());
                    break;
                case P_25://科室单元人员类型（字典对应user_type）
                    List<KpiAccountUnitCopy> account_rylx = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && Linq.of(allDto.getParam().getParamValues()).select(r -> r.getValue()).toList().contains(t.getAccountUserCode())).toList();
                    rt.addAll(Linq.of(account_rylx).select(r -> r.getId()).toList());
                    break;
                case P_29://所有科室单元
                    rt.addAll(Linq.of(kpiAccountUnits).select(r -> r.getId()).toList());
                    break;
                case P_310:// 310 本摊入人
                    rt.add(allDto.getMemberId());
                    break;
                case P_319:// 319 所有摊入人
                    rt.addAll(allImpMembers);
                    break;
                case P_320:// 320 本摊入科室单元
                    rt.add(allDto.getMemberId());
                    break;
                case P_329://所有摊入科室单元
                    rt.addAll(allImpMembers);
                    break;
            }
            //口径剔除
            if (!allDto.getParam().getParamExcludes().isEmpty()) {
                List<Long> list = new ArrayList<>();

                for (KpiFormulaDto2.MemberListDTO r : allDto.getParam().getParamExcludes()) {
                    if (r.getType() == null
                            || r.getType().equals(FormulaParamEnum.P_11.getType())
                            || r.getType().equals(FormulaParamEnum.P_21.getType())) {
                        list.add(Long.parseLong(r.getValue()));
                    } else {
                        list.addAll(getMemberList(r.getType(), r.getValue(),kpiMembers,kpiUserAttendances,kpiAccountUnits));
                    }
                }
                rt.removeAll(list);
            }
        }
        return rt;
    }

    //取口径剔除对象
    private List<Long> getMemberList(String paramType, String value,List<KpiMemberCopy> kpiMembers,
                                     List<KpiUserAttendanceCopy> kpiUserAttendances ,List<KpiAccountUnitCopy> kpiAccountUnits) {
        List<Long> rt = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(paramType)) {
            FormulaParamEnum formulaParamEnum = FormulaParamEnum.find(paramType);
            switch (formulaParamEnum) {
                case P_12://人员类型（字典对应user_type）
                    List<Long> users = Linq.of(kpiMembers).where(t -> t.getMemberType().equals(MemberEnum.EMP_TYPE.getType())
                                    && value.equals(t.getMemberCode()))
                            .select(t -> t.getHostId()).toList();
                    rt.addAll(users);
                    break;
                case P_14://按人员分组
                    List<Long> users_role = Linq.of(kpiMembers).where(t -> t.getMemberType().equals(MemberEnum.ROLE_EMP.getType())
                                    && value.equals(t.getMemberCode()))
                            .select(t -> t.getHostId()).toList();
                    rt.addAll(users_role);
                    break;
                case P_15://按工作性质
                    List<Long> users_job = Linq.of(kpiUserAttendances).where(t ->
                                    value.equals(t.getJobNature()))
                            .select(t -> t.getUserId()).toList();
                    rt.addAll(users_job);
                    break;
                case P_22://核算类型（字典对应kpi_calculate_type）
                    List<KpiAccountUnitCopy> account_type = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && value.equals(t.getAccountTypeCode())).toList();
                    rt.addAll(Linq.of(account_type).select(r -> r.getId()).toList());
                    break;
                case P_23://核算分组
                    List<KpiAccountUnitCopy> account_group = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && value.equals(t.getCategoryCode())).toList();
                    rt.addAll(Linq.of(account_group).select(r -> r.getId()).toList());
                    break;
            }
        }
        return rt;
    }


    @Override
    public void saveTest(KpiAccountTaskTestAddDto dto) {
        KpiConfig kpiConfig = kpiConfigMapper.selectOne(
                new QueryWrapper<KpiConfig>()
                        .eq("default_flag", "Y")
        );

        KpiAccountTask task = kpiAccountTaskMapper.selectOne(
                new QueryWrapper<KpiAccountTask>()
                        .eq("index_code",dto.getCode())
                        .eq("plan_code",dto.getPlanCode())
                        .eq("del_flag","0")
        );
        if (task == null){
            task = new KpiAccountTask();
            task.setPlanCode(dto.getPlanCode());
            task.setTenantId(SecurityUtils.getUser().getTenantId());
            task.setStatus(1L);
            task.setAccountTaskName(dto.getCode());
            task.setIndexCode(dto.getCode());
            task.setTestFlag("Y");
            task.setDelFlag("0");
            task.setIssuedFlag("N");
            task.setPeriod(kpiConfig.getPeriod());
            task.insert();
        }else{
            throw new BizException("指标测试任务已存在");
        }
    }

    @Override
    public List<KpiReportAlloValue> reportAlloValue(KpiCalculateDetailDTO input) {
        List<KpiReportAlloValue> outputs = new ArrayList<>();
        KpiAllocationRuleCopy r = new KpiAllocationRuleCopy();
        List<KpiReportAlloValue> items = new ArrayList<>();
        List<KpiAccountUnitCopy> depts = new ArrayList<>();

        KpiAccountTask task = kpiAccountTaskMapper.getTask(input.getTaskChildId());
        if ("N".equals(task.getTestFlag())) {
            r = kpiAllocationRuleCopyMapper.selectOne(
                    new QueryWrapper<KpiAllocationRuleCopy>()
                            .eq("task_child_id", input.getTaskChildId())
                            .eq("id", input.getFormulateId())
            );
            depts = kpiAccountUnitCopyMapper.selectList(
                    new QueryWrapper<KpiAccountUnitCopy>()
                            .eq("task_child_id", input.getTaskChildId())
            );
            if (r!=null ){
                if (!StringUtil.isNullOrEmpty(r.getOutMembersDept()) && r.getOutMembersDept().contains("{")){
                    List<String> li = new ArrayList<>();
                    KpiAlloOutDeptDTO obj = JSONObject.parseObject(r.getOutMembersDept(),KpiAlloOutDeptDTO.class);
                    li.addAll(obj.getOutDept());
                    if (!obj.getOutDeptGroup().isEmpty()){
                        List<String> list = Linq.of(depts).where(x -> obj.getOutDept().contains(x.getCategoryCode())).select(x -> x.getId().toString()).toList();
                        li.addAll(list);
                    }
                    if (!obj.getOutDeptExcept().isEmpty()) {
                        li = Linq.of(li).where(x -> !obj.getOutDeptExcept().contains(x)).distinct().toList();
                    }
                    r.setOutMembersDept(String.join(",",li));
                }
            }
            if (!StringUtil.isNullOrEmpty(r.getAllocationItems())) {
                items = kpiItemCopyMapper.getAlloValue2(r, task.getPeriod());
            }
        }else {
            r = kpiAllocationRuleMapper.selectById2(input.getFormulateId());
            r.setTaskChildId(input.getTaskChildId());
            depts = kpiAccountUnitCopyMapper.getList(
                    new QueryWrapper<KpiAccountUnitCopy>()
                            .eq("busi_type","1")
            );
            if (r!=null ){
                if (!StringUtil.isNullOrEmpty(r.getOutMembersDept()) && r.getOutMembersDept().contains("{")){
                    List<String> li = new ArrayList<>();
                    KpiAlloOutDeptDTO obj = JSONObject.parseObject(r.getOutMembersDept(),KpiAlloOutDeptDTO.class);
                    li.addAll(obj.getOutDept());
                    //System.out.println(JSONObject.toJSONString(li));
                    if (!obj.getOutDeptGroup().isEmpty()){
                        List<String> list = Linq.of(depts).where(x -> obj.getOutDeptGroup().contains(x.getCategoryCode())).select(x -> x.getId().toString()).toList();
                        li.addAll(list);
                        //System.out.println(JSONObject.toJSONString(li));
                    }
                    if (!obj.getOutDeptExcept().isEmpty()) {
                        li.removeAll(obj.getOutDeptExcept());
                        //System.out.println(JSONObject.toJSONString(li));
                    }
                    r.setOutMembersDept(String.join(",",li));
                }
            }
            if (!StringUtil.isNullOrEmpty(r.getAllocationItems())) {
                items = kpiItemCopyMapper.getAlloValue(r, task.getPeriod());
            }
        }
        outputs.addAll(items);

        if (!StringUtil.isNullOrEmpty(r.getAllocationIndexs())) {
            List<KpiReportAlloValue> cas = kpiCalculateMapper.getAlloValue(r, task.getPeriod());
            outputs.addAll(cas);
        }
        KpiCalculate calculate = kpiCalculateMapper.selectById(input.getId());

        return Linq.of(outputs).where(ca-> (StringUtil.isNullOrEmpty(ca.getImputationDeptName())
                &&StringUtil.isNullOrEmpty(ca.getDeptName())
                &&StringUtil.isNullOrEmpty(ca.getUserName()))
                ||Arrays.asList(calculate.getOutName().split(",")).contains(ca.getImputationDeptName())
                ||Arrays.asList(calculate.getOutName().split(",")).contains(ca.getDeptName())
                ||Arrays.asList(calculate.getOutName().split(",")).contains(ca.getUserName())).toList();
    }

    @Override
    public void reportConfig(ReportConfigCopyDTO input) {
        if (kpiReportConfigCopyMapper.selectCount(
                new QueryWrapper<KpiReportConfigCopy>()
                        .eq("task_child_id", input.getTaskChildId()))>0){
            return;
        }
        kpiReportConfigCopyMapper.delete(
                new QueryWrapper<KpiReportConfigCopy>()
                        .eq("period", input.getPeriod())
                        .eq("tenant_id", SecurityUtils.getUser().getTenantId()));

        List<KpiReportConfigListDTO> list = kpiReportConfigMapper.selectList(
                new QueryWrapper<KpiReportConfig>()
                        .eq("status", "0")
                        .eq("tenant_id", SecurityUtils.getUser().getTenantId())
        );
        List<KpiReportConfigCopy> list_copy = new ArrayList<>();
        list.forEach(r -> {
            KpiReportConfigCopy entity = new KpiReportConfigCopy();
            entity.setId(r.getId())
                    .setGroup(r.getGroup())
                    .setName(r.getName())
                    .setCaliber(r.getCaliber())
                    .setField(r.getField())
                    .setIndex(r.getIndex())
                    .setRange(r.getRange())
                    .setPeriod(input.getPeriod())
                    .setImpCode(r.getImpCode())
                    .setCreatedId(r.getCreatedId())
                    .setCreatedDate(r.getCreatedDate())
                    .setUpdatedId(r.getUpdatedId())
                    .setUpdatedDate(r.getUpdatedDate())
                    .setImpType(r.getImpType())
                    .setReportCode(r.getReportCode())
                    .setReportType(r.getReportType())
                    .setTenantId(r.getTenantId())
                    .setType(r.getType())
                    .setEstablish(r.getEstablish())
                    .setStatus(r.getStatus())
                    .setSeq(r.getSeq());
            entity.setTaskChildId(input.getTaskChildId());
            entity.setCopyDate(new Date());
            list_copy.add(entity);
        });
        if (!list_copy.isEmpty()) {
            kpiReportConfigCopyMapper.insertBatchSomeColumn(list_copy);
        }

    }

    public List<KpiMemberCopy> memberCopy(Long tenantId, Long period) {
        List<KpiMemberCopy> outputs = new ArrayList<>();
        List<KpiMember> list = kpiMemberMapper.selectList(
                new QueryWrapper<KpiMember>()
                        .eq("period", period)
                        .eq("tenant_id", tenantId)
                        .eq("member_type", MemberEnum.USER_DEPT)
        );
        List<KpiMember> list_copy = new ArrayList<>();
        list.forEach(r -> {
            KpiMember entity = new KpiMember();
            BeanUtil.copyProperties(r, entity);
            entity.setPeriod(entity.getPeriod() + 1);
            list_copy.add(entity);
        });

        if (!list_copy.isEmpty()) {
            kpiMemberMapper.insertBatchSomeColumn(list_copy);
        }
        return outputs;
    }

    @Override
    public void change(KpiConfigChangeDTO dto) {
        List<KpiAccountTask> list = this.list(
                new QueryWrapper<KpiAccountTask>()
                        .eq("issued_flag", "Y")
                        .eq("period", dto.getPeriod())
        );
        for (KpiAccountTask task : list) {
            task.setIssuedFlag("N");
            task.setIssuedDate(null);
            task.setSendFlag("N");
            task.setSendDate(null);
            this.updateById(task);

            if (task.getTaskChildId()!=null) {
                KpiAccountTaskChild child = kpiAccountTaskChildService.getById(task.getTaskChildId());
                child.setIssuedFlag("N");
                task.setIssuedDate(null);
                kpiAccountTaskChildService.updateById(child);
            }
        }
        KpiAccountTask task = this.getById(dto.getTaskId());
        task.setIssuedFlag("Y");
        task.setIssuedDate(new Date());
        task.setSendFlag("Y");
        task.setSendDate(new Date());
        this.updateById(task);
        if (task.getTaskChildId()!=null) {
            KpiAccountTaskChild child = kpiAccountTaskChildService.getById(task.getTaskChildId());
            child.setIssuedFlag("Y");
            task.setIssuedDate(new Date());
            kpiAccountTaskChildService.updateById(child);
        }
        this.getBaseMapper().changeConfig(dto.getPeriod(),task.getTaskChildId());
    }
}
