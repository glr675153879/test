package com.hscloud.hs.cost.account.service.kpi.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.config.BaseConfig;
import com.hscloud.hs.cost.account.constant.enums.YesNoEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.*;
import com.hscloud.hs.cost.account.mapper.dataReport.CostClusterUnitMapper;
import com.hscloud.hs.cost.account.mapper.kpi.*;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiTransferListVO;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiAccountTaskService;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiItemResultService;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiUserAttendanceService;
import com.hscloud.hs.cost.account.service.kpi.IKpiImputationService;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TaskService {

    //region
    @Autowired
    private KpiAccountPlanMapper kpiAccountPlanMapper;
    @Autowired
    private KpiAccountPlanCopyMapper kpiAccountPlanCopyMapper;
    @Autowired
    private KpiAccountUnitMapper kpiAccountUnitMapper;
    @Autowired
    private KpiAccountUnitCopyMapper kpiAccountUnitCopyMapper;
    @Autowired
    private KpiAllocationRuleMapper kpiAllocationRuleMapper;
    @Autowired
    private KpiAllocationRuleCopyMapper kpiAllocationRuleCopyMapper;
    @Autowired
    private KpiCategoryMapper kpiCategoryMapper;
    @Autowired
    private KpiCategoryCopyMapper kpiCategoryCopyMapper;
    @Autowired
    private KpiIndexMapper kpiIndexMapper;
    @Autowired
    private KpiIndexCopyMapper kpiIndexCopyMapper;
    @Autowired
    private KpiItemMapper kpiItemMapper;
    @Autowired
    private KpiCoefficientMapper kpiCoefficientMapper;
    @Autowired
    private KpiCoefficientCopyMapper kpiCoefficientCopyMapper;
    @Autowired
    private KpiItemCopyMapper kpiItemCopyMapper;
    @Autowired
    private KpiMemberMapper kpiMemberMapper;
    @Autowired
    private KpiMemberCopyMapper kpiMemberCopyMapper;
    @Autowired
    private KpiUserFactorMapper kpiUserFactorMapper;
    @Autowired
    private KpiUserFactorCopyMapper kpiUserFactorCopyMapper;
    @Autowired
    private KpiDictItemMapper kpiDictItemMapper;
    @Autowired
    private KpiDictItemCopyMapper kpiDictItemCopyMapper;
    @Autowired
    private KpiUserAttendanceMapper kpiUserAttendanceMapper;
    @Autowired
    private KpiUserAttendanceCustomMapper kpiUserAttendanceCustomMapper;
    @Autowired
    private KpiUserAttendanceCustomCopyMapper kpiUserAttendanceCustomCopyMapper;
    @Autowired
    private KpiUserAttendanceCopyMapper kpiUserAttendanceCopyMapper;
    @Autowired
    private KpiValueAdjustMapper kpiValueAdjustMapper;
    @Autowired
    private KpiValueAdjustCopyMapper kpiValueAdjustCopyMapper;
    @Autowired
    private KpiCalculateMapper kpiCalculateMapper;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    private KpiAccountTaskMapper kpiAccountTaskMapper;
    @Autowired
    private KpiIndexFormulaMapper kpiIndexFormulaMapper;
    @Autowired
    private KpiIndexFormulaCopyMapper kpiIndexFormulaCopyMapper;
    @Autowired
    private KpiIndexFormulaObjMapper kpiIndexFormulaObjMapper;
    @Autowired
    private KpiIndexFormulaObjCopyMapper kpiIndexFormulaObjCopyMapper;
    @Autowired
    private KpiItemResultMapper kpiItemResultMapper;
    @Autowired
    private KpiItemEquivalentMapper kpiItemEquivalentMapper;
    @Autowired
    private KpiItemEquivalentCopyMapper kpiItemEquivalentCopyMapper;
    @Autowired
    private KpiItemResultCopyMapper kpiItemResultCopyMapper;
    @Autowired
    private KpiAccountTaskChildMapper kpiAccountTaskChildMapper;
    @Autowired
    private CostClusterUnitMapper costClusterUnitMapper;
    @Autowired
    private KpiClusterUnitCopyMapper kpiClusterUnitCopyMapper;
    @Autowired
    private KpiConfigMapper kpiConfigMapper;
    @Autowired
    private IKpiItemService kpiItemService;
    @Autowired
    private IKpiImputationService kpiImputationService;
    @Autowired
    private KpiUserAttendanceService kpiUserAttendanceService;
    @Autowired
    private KpiAccountTaskService kpiAccountTaskService;
    @Autowired
    private KpiItemResultService kpiItemResultService;
    //endregion

    @SchedulerLock(name = BaseConfig.appCode + "_KpiCalculateMain")
    public void calculate(Long task_id,boolean itemRefresh,boolean empRefresh,boolean equivalent) {
        TaskCaculateService taskCaculateService = new TaskCaculateService();
        taskCaculateService.clean();
        try {
            List<String> codes = new ArrayList<>();
            KpiAccountTask task = kpiAccountTaskMapper.selectById(task_id);
            if (task == null) {
                throw new BizException("任务不存在");
            }
            taskCaculateService.task = task;
            KpiConfig kpiConfig = kpiConfigMapper.selectOne(
                    new QueryWrapper<KpiConfig>()
                            .eq("period", task.getPeriod())
            );
            //分区
            taskCaculateService.part(task.getPeriod(),kpiCalculateMapper,jdbcTemplate);
            taskCaculateService.equitemtprice = kpiConfig.getEquivalentPrice();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            KpiAccountTaskChild task_child = new KpiAccountTaskChild();
            //task_child.setRunLog("<" + sdf.format(new Date()) + ">" + "获取计算所需数据\n");
            task_child.setRunLog("<" + sdf.format(new Date()) + ">" + "开始计算\n");
            task_child.setPeriod(task.getPeriod());
            task_child.setTenantId(task.getTenantId());
            task_child.setCreatedId(task.getCreatedId());
            task_child.setIssuedFlag("N");
            task_child.setStatus(1L);
            task_child.setStatusName("开始计算");
            task_child.setCreatedDate(new Date());
            task_child.setTaskId(task_id);
            taskCaculateService.task_child = task_child;

            kpiAccountTaskChildMapper.insert(task_child);
            Long taskChildId = task.getTaskChildId();
            task.setTaskChildId(task_child.getId());
            task.setUpdatedDate(new Date());
            kpiAccountTaskMapper.updateById(task);
            taskCaculateService.delData(taskChildId,kpiCalculateMapper,kpiMemberCopyMapper,kpiItemResultCopyMapper);

            if (equivalent && !"Y".equals(kpiConfig.getEquivalentFlag())){
                taskCaculateService.updateLog(TaskStatusEnum.S_96, "当量未锁定，无法计算",kpiAccountTaskChildMapper);
                return;
            }
            //校验数据获取
            {
                taskCaculateService.users = taskCaculateService.getUsers(kpiCalculateMapper);
                taskCaculateService.kpiAccountUnits = taskCaculateService.unitCopy2(kpiAccountUnitMapper);
                taskCaculateService.dict = taskCaculateService.getDicts(kpiCalculateMapper);
                taskCaculateService.kpiAllocationRules = taskCaculateService.ruleCopy2(kpiAllocationRuleMapper);
                taskCaculateService.kpiCategorys = taskCaculateService.cateGoryCopy2(kpiCategoryMapper);
                taskCaculateService.kpiIndexs = taskCaculateService.indexCopy2(codes,kpiIndexMapper);
                taskCaculateService.kpiIndexFormulas = taskCaculateService.indexFormulaCopy2(codes,kpiIndexFormulaMapper);
                taskCaculateService.kpiMembers = taskCaculateService.memberCopy2(kpiMemberMapper);
                taskCaculateService.kpiUserFactors = taskCaculateService.userFactorCopy2(kpiUserFactorMapper);
                taskCaculateService.kpiDictItems = taskCaculateService.dictItemCopy2(kpiDictItemMapper);
                //kpiItemResults = itemResultCopy2();
                taskCaculateService.kpiUserAttendances = taskCaculateService.userCopy2(kpiUserAttendanceMapper);
                taskCaculateService.kpiUserAttendancesCustoms = taskCaculateService.userCustomCopy2(kpiUserAttendanceCustomMapper);
                taskCaculateService.kpiClusterUnitCopyList = taskCaculateService.clusterUnitCopy2(costClusterUnitMapper);
                taskCaculateService.kpiItems = taskCaculateService.itemCopy2(kpiItemMapper);
                taskCaculateService.kpiIndexFormulasObj = taskCaculateService.indexFormulaObjCopy2(codes,kpiIndexFormulaObjMapper);
                taskCaculateService.kpiItems.forEach(r -> {
                    String flag = "";
                    if (r.getDelFlag().equals("1")) {
                        flag = "[已删除]";
                    } else if (r.getStatus().equals("1")) {
                        flag = "[已停用]";
                    }
                    taskCaculateService.field_total.put(r.getCode(), r.getItemName() + flag);
                });
                taskCaculateService.kpiIndexs.forEach(r -> {
                    String flag = "";
                    if (r.getDelFlag().equals("1")) {
                        flag = "[已删除]";
                    } else if (r.getStatus().equals("1")) {
                        flag = "[已停用]";
                    }
                    taskCaculateService.field_total.put(r.getCode(), r.getName() + flag);
                });
            }
            taskCaculateService.getAllFormula();
            taskCaculateService.fakeCaculate2(kpiAccountTaskChildMapper);
            if (!taskCaculateService.indexErroTips.isEmpty()) {
                taskCaculateService.updateLog(TaskStatusEnum.S_96, String.join("\n", taskCaculateService.indexErroTips),kpiAccountTaskChildMapper);
                return;
            }

            if (itemRefresh) {
                taskCaculateService.updateLog(TaskStatusEnum.S_2, "核算项计算开始",kpiAccountTaskChildMapper);
                kpiItemService.itemBatchCalculate(null, "1", task.getPeriod(), YesNoEnum.NO.getValue());
                KpiConfig one = kpiConfigMapper.selectOne(new QueryWrapper<KpiConfig>().eq("period", task.getPeriod()));
                while (one.getIndexFlag().equals("0")) {
                    Thread.sleep(3000L);
                    one = kpiConfigMapper.selectOne(new QueryWrapper<KpiConfig>().eq("period", task.getPeriod()));
                }
                if (one.getIndexFlag().equals("9")) {
                    taskCaculateService.updateErrorLog(TaskStatusEnum.S_96, "核算项计算异常",kpiAccountTaskChildMapper);
                    return;
                }
                taskCaculateService.updateLog(TaskStatusEnum.S_2, "核算项计算完成",kpiAccountTaskChildMapper);
            }else{
                taskCaculateService.updateLog(TaskStatusEnum.S_2, "核算项计算跳过",kpiAccountTaskChildMapper);
            }

            taskCaculateService.updateLog(TaskStatusEnum.S_1, "人员锁定归集计算开始",kpiAccountTaskChildMapper);
            int lock = 1;
            while (lock > 0) {
                lock = kpiAccountTaskMapper.getShedLog("('cost_refresh','cost_lockData')");
            }
            if (!(kpiConfig.getIssuedFlag().equals("Y") && !empRefresh)) {
                kpiImputationService.refresh(null, kpiConfig.getPeriod(), "1", task.getTenantId(), task.getPeriod(), empRefresh);
                kpiUserAttendanceService.lockData(kpiConfig.getPeriod().toString(), "1", task.getTenantId(), empRefresh);
            }
            taskCaculateService.updateLog(TaskStatusEnum.S_1, "人员锁定归集计算完成",kpiAccountTaskChildMapper);

            //转科数据未处理
            KpiTransferListDTO dto = new KpiTransferListDTO();
            dto.setBusiType("1");
            dto.setStatus("0");
            dto.setPeriod(task.getPeriod()+"");
            IPage<KpiTransferListVO> transferPage = kpiItemResultService.getTransferPage(dto);
            if (!transferPage.getRecords().isEmpty()){
                taskCaculateService.updateErrorLog(TaskStatusEnum.S_96, "转科人员数据未处理",kpiAccountTaskChildMapper);
                return;
            }

            taskCaculateService.updateLog(TaskStatusEnum.S_10,kpiAccountTaskChildMapper);
            taskCaculateService.planCodeMemberLists = taskCaculateService.planCopy(kpiAccountPlanMapper,kpiAccountTaskService,kpiAccountPlanCopyMapper,kpiAccountTaskChildMapper);
            taskCaculateService.kpiAccountUnits = taskCaculateService.unitCopy(kpiAccountUnitMapper,kpiAccountUnitCopyMapper,kpiAccountTaskChildMapper);
            taskCaculateService.kpiAllocationRules = taskCaculateService.ruleCopy(kpiAllocationRuleMapper,kpiAllocationRuleCopyMapper,kpiAccountTaskChildMapper);
            taskCaculateService.kpiCategorys = taskCaculateService.cateGoryCopy(kpiCategoryMapper,kpiCategoryCopyMapper,kpiAccountTaskChildMapper);
            taskCaculateService.kpiIndexs = taskCaculateService.indexCopy(kpiIndexMapper,kpiIndexCopyMapper,kpiAccountTaskChildMapper);
            taskCaculateService.kpiIndexFormulas = taskCaculateService.indexFormulaCopy(kpiIndexFormulaMapper,kpiIndexFormulaCopyMapper,kpiAccountTaskChildMapper);
            taskCaculateService.kpiMembers = taskCaculateService.memberCopy(kpiMemberMapper,kpiMemberCopyMapper,kpiAccountTaskChildMapper);
            taskCaculateService.kpiUserFactors = taskCaculateService.userFactorCopy(kpiUserFactorMapper,kpiUserFactorCopyMapper,kpiAccountTaskChildMapper);
            taskCaculateService.kpiDictItems = taskCaculateService.dictItemCopy(kpiDictItemMapper,kpiDictItemCopyMapper,kpiAccountTaskChildMapper);
            taskCaculateService.kpiUserAttendances = taskCaculateService.userCopy(kpiUserAttendanceMapper,kpiUserAttendanceCopyMapper,kpiAccountTaskChildMapper);
            taskCaculateService.kpiUserAttendancesCustoms = taskCaculateService.userCustomCopy(kpiUserAttendanceCustomMapper,kpiUserAttendanceCustomCopyMapper,kpiAccountTaskChildMapper);

            taskCaculateService.kpiValueAdjustCopies = taskCaculateService.kpiValueAdjustCopy(kpiValueAdjustMapper,kpiValueAdjustCopyMapper,kpiAccountTaskChildMapper);
            taskCaculateService.kpiItems = taskCaculateService.itemCopy(kpiItemMapper,kpiItemCopyMapper,kpiAccountTaskChildMapper);
            taskCaculateService.coefficients = taskCaculateService.coefficientsCopy(kpiCoefficientMapper,kpiCoefficientCopyMapper,kpiAccountTaskChildMapper);
            taskCaculateService.kpiItemResults = taskCaculateService.itemResultCopy(kpiItemResultMapper,kpiItemResultCopyMapper,kpiAccountTaskChildMapper);
            taskCaculateService.kpiItemEquivalents = taskCaculateService.equivalentCopy(kpiItemEquivalentMapper,kpiItemEquivalentCopyMapper,kpiAccountTaskChildMapper);
            taskCaculateService.kpiClusterUnitCopyList = taskCaculateService.clusterUnitCopy(costClusterUnitMapper,kpiClusterUnitCopyMapper,kpiAccountTaskChildMapper);
            taskCaculateService.kpiIndexFormulasObj = taskCaculateService.indexFormulaObjCopy(kpiIndexFormulaObjMapper,kpiIndexFormulaObjCopyMapper,kpiAccountTaskChildMapper);


            taskCaculateService.field_total.clear();
            taskCaculateService.kpiItems = Linq.of(taskCaculateService.kpiItems).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")).toList();
            taskCaculateService.kpiIndexs = Linq.of(taskCaculateService.kpiIndexs).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")).toList();
            taskCaculateService.kpiItems.forEach(r -> {
                taskCaculateService.field_total.put(r.getCode(), r.getItemName());
            });
            taskCaculateService.kpiIndexs.forEach(r -> {
                taskCaculateService.field_total.put(r.getCode(), r.getName());
            });
            task_child.setRunLog("<" + sdf.format(new Date()) + ">" + "转存完成");
            task_child.setStatus((long) TaskStatusEnum.S_22.getType());
            task_child.updateById();
            taskCaculateService.cateCalculate2(null,kpiAccountTaskChildMapper,kpiCalculateMapper);
        } catch (Exception ex) {
            taskCaculateService.updateErrorLog(TaskStatusEnum.S_96, taskCaculateService.getEroLog(ex),kpiAccountTaskChildMapper);
        }
    }

    @SchedulerLock(name = BaseConfig.appCode + "_KpiCalculateMain")
    public void calculateTest(Long task_id) {
        TaskCaculateService taskCaculateService = new TaskCaculateService();
        taskCaculateService.clean();
        try {
            taskCaculateService.task = kpiAccountTaskMapper.selectById(task_id);
            KpiConfig kpiConfig = kpiConfigMapper.selectOne(
                    new QueryWrapper<KpiConfig>()
                            .eq("default_flag", "Y")
            );
            taskCaculateService.equitemtprice = kpiConfig.getEquivalentPrice();
            if (taskCaculateService.task == null) {
                throw new BizException("任务不存在");
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            taskCaculateService.task_child = new KpiAccountTaskChild();
            //task_child.setRunLog("<" + sdf.format(new Date()) + ">" + "获取计算所需数据\n");

            taskCaculateService.task.setPeriod(kpiConfig.getPeriod());
            taskCaculateService.task.setUpdatedDate(new Date());
            taskCaculateService.task_child.setRunLog("<" + sdf.format(new Date()) + ">" + "开始计算\n");
            taskCaculateService.task_child.setPeriod(taskCaculateService.task.getPeriod());
            taskCaculateService.task_child.setTenantId(taskCaculateService.task.getTenantId());
            taskCaculateService.task_child.setCreatedId(taskCaculateService.task.getCreatedId());
            taskCaculateService.task_child.setIssuedFlag("N");
            taskCaculateService.task_child.setStatus(1L);
            taskCaculateService.task_child.setStatusName("开始计算");
            taskCaculateService.task_child.setCreatedDate(new Date());
            taskCaculateService.task_child.setTaskId(task_id);
            kpiAccountTaskChildMapper.insert(taskCaculateService.task_child);
            Long taskChildId = taskCaculateService.task.getTaskChildId();
            taskCaculateService.task.setTaskChildId(taskCaculateService.task_child.getId());
            kpiAccountTaskMapper.updateById(taskCaculateService.task);

            taskCaculateService.delData(taskChildId,kpiCalculateMapper,kpiMemberCopyMapper,kpiItemResultCopyMapper);
            taskCaculateService.kpiIndexsAll = kpiIndexMapper.selectList(new QueryWrapper<KpiIndex>()
                    .eq("tenant_id", taskCaculateService.task.getTenantId())
            );

            taskCaculateService.kpiIndexs = taskCaculateService.indexCopy2(new ArrayList<>(),kpiIndexMapper);
            taskCaculateService.kpiIndexFormulas = taskCaculateService.indexFormulaCopy2(new ArrayList<>(),kpiIndexFormulaMapper);

            //校验数据获取
            taskCaculateService.users = taskCaculateService.getUsers(kpiCalculateMapper);
            taskCaculateService.kpiAccountUnits = taskCaculateService.unitCopy2(kpiAccountUnitMapper);
            taskCaculateService.kpiAllocationRules = taskCaculateService.ruleCopy2(kpiAllocationRuleMapper);
            taskCaculateService.dict = taskCaculateService.getDicts(kpiCalculateMapper);
            taskCaculateService.kpiCategorys = taskCaculateService.cateGoryCopy2(kpiCategoryMapper);

            taskCaculateService.caTempCodes(taskCaculateService.task.getIndexCode());
            taskCaculateService.kpiIndexFormulas = Linq.of(taskCaculateService.kpiIndexFormulas).where(t -> taskCaculateService.tempCodes.contains(t.getIndexCode())).toList();
            taskCaculateService.kpiAllocationRules = Linq.of(taskCaculateService.kpiAllocationRules).where(t -> taskCaculateService.tempCodes.contains(t.getIndexCode())).toList();
            taskCaculateService.kpiMembers = taskCaculateService.memberCopy2(kpiMemberMapper);
            taskCaculateService.kpiUserFactors = taskCaculateService.userFactorCopy2(kpiUserFactorMapper);
            taskCaculateService.kpiDictItems = taskCaculateService.dictItemCopy2(kpiDictItemMapper);
            taskCaculateService.kpiUserAttendances = taskCaculateService.userCopy2(kpiUserAttendanceMapper);
            taskCaculateService.kpiUserAttendancesCustoms = taskCaculateService.userCustomCopy2(kpiUserAttendanceCustomMapper);
            //kpiItemResults = itemResultCopy2();
            taskCaculateService.kpiClusterUnitCopyList = taskCaculateService.clusterUnitCopy2(costClusterUnitMapper);
            taskCaculateService.kpiItems = taskCaculateService.itemCopy2(kpiItemMapper);
            taskCaculateService.coefficients = taskCaculateService.coefficientsCopy2(kpiCoefficientMapper);
            taskCaculateService.kpiIndexFormulasObj = taskCaculateService.indexFormulaObjCopy2(taskCaculateService.tempCodes,kpiIndexFormulaObjMapper);
            taskCaculateService.kpiItems.forEach(r -> {
                String flag = "";
                if (r.getDelFlag().equals("1")) {
                    flag = "[已删除]";
                } else if (r.getStatus().equals("1")) {
                    flag = "[已停用]";
                }
                taskCaculateService.field_total.put(r.getCode(), r.getItemName() + flag);
            });

            taskCaculateService.kpiIndexsAll.forEach(r -> {
                String flag = "";
                if (r.getDelFlag().equals("1")) {
                    flag = "[已删除]";
                } else if (r.getStatus().equals("1")) {
                    flag = "[已停用]";
                }
                taskCaculateService.field_total.put(r.getCode(), r.getName() + flag);
            });

            taskCaculateService.getAllFormula();
//            long l = System.currentTimeMillis();
//            System.out.println(">>>>>>"+l);
//            fakeCaculate2();
//            System.out.println(">>>>>>"+(System.currentTimeMillis()-l));
            if (!taskCaculateService.indexErroTips.isEmpty()) {
                taskCaculateService.updateLog(TaskStatusEnum.S_96, String.join("\n", taskCaculateService.indexErroTips),kpiAccountTaskChildMapper);
                return;
            }

            taskCaculateService.kpiValueAdjustCopies = taskCaculateService.kpiValueAdjustCopy2(kpiValueAdjustMapper,kpiAccountTaskChildMapper);
            taskCaculateService.kpiUserAttendances = taskCaculateService.userCopy2(kpiUserAttendanceMapper);
            taskCaculateService.kpiUserAttendancesCustoms = taskCaculateService.userCustomCopy2(kpiUserAttendanceCustomMapper);
            taskCaculateService.kpiItemResults = taskCaculateService.itemResultCopy2(kpiItemResultMapper);
            taskCaculateService.kpiItemEquivalents = taskCaculateService.equivalentCopy2(kpiItemEquivalentMapper);

            //分区
            taskCaculateService.part(taskCaculateService.task.getPeriod(),kpiCalculateMapper,jdbcTemplate);
            taskCaculateService.field_total.clear();
            taskCaculateService.kpiItems = Linq.of(taskCaculateService.kpiItems).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")).toList();
            taskCaculateService.kpiIndexs = Linq.of(taskCaculateService.kpiIndexs).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")).toList();
            taskCaculateService.kpiItems.forEach(r -> {
                taskCaculateService.field_total.put(r.getCode(), r.getItemName());
            });
            taskCaculateService.kpiIndexs.forEach(r -> {
                taskCaculateService.field_total.put(r.getCode(), r.getName());
            });
            taskCaculateService.task_child.setRunLog("<" + sdf.format(new Date()) + ">" + "转存完成");
            taskCaculateService.task_child.setStatus((long) TaskStatusEnum.S_22.getType());
            taskCaculateService.task_child.updateById();

            taskCaculateService.cateCalculate2(taskCaculateService.task.getIndexCode(),kpiAccountTaskChildMapper,kpiCalculateMapper);
        } catch (Exception ex) {
            taskCaculateService.updateErrorLog(TaskStatusEnum.S_96, taskCaculateService.getEroLog(ex),kpiAccountTaskChildMapper);
        }
    }

    //公式校验
    public void formulaTest(Long task_id) {
        TaskCaculateService taskCaculateService = new TaskCaculateService();
        taskCaculateService.clean();
        List<String> codes = new ArrayList<>();
        taskCaculateService.task = kpiAccountTaskMapper.selectById(task_id);

        if (taskCaculateService.task == null) {
            throw new BizException("任务不存在");
        }
        if (taskCaculateService.task.getIssuedFlag().equals("Y")) {
            throw new BizException("当前状态不允许测试");
        }
        taskCaculateService.task.setUpdatedDate(new Date());
        kpiAccountTaskMapper.updateById(taskCaculateService.task);

        taskCaculateService.kpiIndexsAll = kpiIndexMapper.selectList(new QueryWrapper<KpiIndex>()
                .eq("tenant_id", taskCaculateService.task.getTenantId()));
        //codes = indexCopy(task.getIndexCode(), task.getTenantId());

        //校验数据获取
        taskCaculateService.users = taskCaculateService.getUsers(kpiCalculateMapper);
        taskCaculateService.kpiAccountUnits = taskCaculateService.unitCopy2(kpiAccountUnitMapper);
        taskCaculateService.dict = taskCaculateService.getDicts(kpiCalculateMapper);
        taskCaculateService.kpiAllocationRules = taskCaculateService.ruleCopy2(kpiAllocationRuleMapper);
        taskCaculateService.kpiCategorys = taskCaculateService.cateGoryCopy2(kpiCategoryMapper);
        taskCaculateService.kpiIndexs = taskCaculateService.indexCopy2(codes,kpiIndexMapper);
        taskCaculateService.kpiIndexFormulas = taskCaculateService.indexFormulaCopy2(codes,kpiIndexFormulaMapper);
        taskCaculateService.kpiIndexFormulasObj = taskCaculateService.indexFormulaObjCopy2(codes,kpiIndexFormulaObjMapper);
        //kpiItemResults = itemResultCopy2();
        taskCaculateService.kpiMembers = taskCaculateService.memberCopy2(kpiMemberMapper);
        taskCaculateService.kpiUserFactors = taskCaculateService.userFactorCopy2(kpiUserFactorMapper);
        taskCaculateService.kpiDictItems = taskCaculateService.dictItemCopy2(kpiDictItemMapper);
        taskCaculateService.kpiUserAttendances = taskCaculateService.userCopy2(kpiUserAttendanceMapper);
        taskCaculateService.kpiClusterUnitCopyList = taskCaculateService.clusterUnitCopy2(costClusterUnitMapper);
        taskCaculateService.kpiItems = taskCaculateService.itemCopy2(kpiItemMapper);
        taskCaculateService.kpiItems.forEach(r -> {
            String flag = "";
            if (r.getDelFlag().equals("1")) {
                flag = "[已删除]";
            } else if (r.getStatus().equals("1")) {
                flag = "[已停用]";
            }
            taskCaculateService.field_total.put(r.getCode(), r.getItemName() + flag);
        });

        taskCaculateService.kpiIndexsAll.forEach(r -> {
            String flag = "";
            if (r.getDelFlag().equals("1")) {
                flag = "[已删除]";
            } else if (r.getStatus().equals("1")) {
                flag = "[已停用]";
            }
            taskCaculateService.field_total.put(r.getCode(), r.getName() + flag);
        });

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        taskCaculateService.task_child = new KpiAccountTaskChild();
        taskCaculateService.task_child.setRunLog("<" + sdf.format(new Date()) + ">" + "开始计算\n");
        taskCaculateService.task_child.setPeriod(taskCaculateService.task.getPeriod());
        taskCaculateService.task_child.setTenantId(taskCaculateService.task.getTenantId());
        taskCaculateService.task_child.setCreatedId(taskCaculateService.task.getCreatedId());
        taskCaculateService.task_child.setIssuedFlag("N");
        taskCaculateService.task_child.setStatus(0L);
        taskCaculateService.task_child.setCreatedDate(new Date());
        taskCaculateService.task_child.setTaskId(task_id);
        kpiAccountTaskChildMapper.insert(taskCaculateService.task_child);
        taskCaculateService.task.setTaskChildId(taskCaculateService.task_child.getId());
        kpiAccountTaskMapper.updateById(taskCaculateService.task);

        taskCaculateService.kpiItems = Linq.of(taskCaculateService.kpiItems).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")).toList();
        taskCaculateService.kpiIndexs = Linq.of(taskCaculateService.kpiIndexs).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")).toList();
        taskCaculateService.getAllFormula();
        taskCaculateService.fakeCaculate2(kpiAccountTaskChildMapper);
        if (!taskCaculateService.indexErroTips.isEmpty()) {
            taskCaculateService.updateLog(TaskStatusEnum.S_96, String.join("\n", taskCaculateService.indexErroTips),kpiAccountTaskChildMapper);
        }
        taskCaculateService.clean();
    }

    public String formulaTest2(KpiTestFormulaDTO input) {
        TaskCaculateService taskCaculateService = new TaskCaculateService();
        taskCaculateService.clean();
        List<String> codes = new ArrayList<>();
        KpiConfig kpiConfig = kpiConfigMapper.selectOne(
                new QueryWrapper<KpiConfig>()
                        .eq("default_flag", "Y")
        );

        taskCaculateService.task = new KpiAccountTask();
        taskCaculateService.task.setTenantId(SecurityUtils.getUser().getTenantId());
        taskCaculateService.task.setPeriod(kpiConfig.getPeriod());
        taskCaculateService.task.setPlanCode(input.getPlan_code());

        taskCaculateService.kpiIndexsAll = kpiIndexMapper.selectList(new QueryWrapper<KpiIndex>()
                .eq("tenant_id", taskCaculateService.task.getTenantId()));
        //codes = indexCopy(task.getIndexCode(), task.getTenantId());

        //校验数据获取
        taskCaculateService.users = taskCaculateService.getUsers(kpiCalculateMapper);
        taskCaculateService.kpiAccountUnits = taskCaculateService.unitCopy2(kpiAccountUnitMapper);
        taskCaculateService.dict = taskCaculateService.getDicts(kpiCalculateMapper);
        taskCaculateService.kpiAllocationRules = taskCaculateService.ruleCopy2(kpiAllocationRuleMapper);
        taskCaculateService.kpiCategorys = taskCaculateService.cateGoryCopy2(kpiCategoryMapper);
        taskCaculateService.kpiIndexs = taskCaculateService.indexCopy2(codes,kpiIndexMapper);
        taskCaculateService.kpiIndexFormulas = taskCaculateService.indexFormulaCopy2(codes,kpiIndexFormulaMapper);
        taskCaculateService.kpiIndexFormulasObj = taskCaculateService.indexFormulaObjCopy2(codes,kpiIndexFormulaObjMapper);
        //kpiItemResults = itemResultCopy2();
        taskCaculateService.kpiMembers = taskCaculateService.memberCopy2(kpiMemberMapper);
        taskCaculateService.kpiUserFactors = taskCaculateService.userFactorCopy2(kpiUserFactorMapper);
        taskCaculateService.kpiDictItems = taskCaculateService.dictItemCopy2(kpiDictItemMapper);
        taskCaculateService.kpiUserAttendances = taskCaculateService.userCopy2(kpiUserAttendanceMapper);
        taskCaculateService.kpiClusterUnitCopyList = taskCaculateService.clusterUnitCopy2(costClusterUnitMapper);
        taskCaculateService.kpiItems = taskCaculateService.itemCopy2(kpiItemMapper);
        taskCaculateService.kpiItems.forEach(r -> {
            String flag = "";
            if (r.getDelFlag().equals("1")) {
                flag = "[已删除]";
            } else if (r.getStatus().equals("1")) {
                flag = "[已停用]";
            }
            taskCaculateService.field_total.put(r.getCode(), r.getItemName() + flag);
        });

        taskCaculateService.kpiIndexsAll.forEach(r -> {
            String flag = "";
            if (r.getDelFlag().equals("1")) {
                flag = "[已删除]";
            } else if (r.getStatus().equals("1")) {
                flag = "[已停用]";
            }
            taskCaculateService.field_total.put(r.getCode(), r.getName() + flag);
        });

        taskCaculateService.kpiItems = Linq.of(taskCaculateService.kpiItems).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")).toList();
        taskCaculateService.kpiIndexs = Linq.of(taskCaculateService.kpiIndexs).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")).toList();
        taskCaculateService.getAllFormula();
        taskCaculateService.fakeCaculate2(kpiAccountTaskChildMapper);
        if (!taskCaculateService.indexErroTips.isEmpty()) {
            return String.join("\n", taskCaculateService.indexErroTips);
        } else {
            return "";
        }
    }
}
