package com.hscloud.hs.cost.account.service.kpi.task;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.config.BaseConfig;
import com.hscloud.hs.cost.account.constant.enums.kpi.FormulaParamEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.MemberEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.TaskStatusEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.UserFactorCodeEnum;
import com.hscloud.hs.cost.account.mapper.dataReport.CostClusterUnitMapper;
import com.hscloud.hs.cost.account.mapper.kpi.*;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostClusterUnit;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiAccountTaskService;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiItemResultService;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiUserAttendanceService;
import com.hscloud.hs.cost.account.service.kpi.IKpiImputationService;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemService;
import com.hscloud.hs.cost.account.utils.ExpressionCheckHelper;
import com.hscloud.hs.cost.account.utils.SnowflakeGenerator;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import io.netty.util.internal.StringUtil;
import lombok.SneakyThrows;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class TaskCaculateService {
    //region
    public List<KpiAccountUnitCopy> kpiAccountUnits = new ArrayList<>();
    public List<KpiAllocationRuleCopy> kpiAllocationRules = new ArrayList<>();
    public List<KpiAllocationRuleCaDto> kpiAllocationRulesCa = new ArrayList<>();
    public List<KpiCategoryCopy> kpiCategorys = new ArrayList<>();
    public List<KpiIndexCopy> kpiIndexs = new ArrayList<>();
    public List<KpiIndex> kpiIndexsAll = new ArrayList<>();
    public List<String> tempCodes = new ArrayList<>();
    public List<KpiIndexFormulaCopy> kpiIndexFormulas = new ArrayList<>();
    public List<KpiIndexFormulaObjCopy> kpiIndexFormulasObj = new ArrayList<>();
    public List<KpiItemCopy> kpiItems = new ArrayList<>();
    public List<KpiItemResultCopy> kpiItemResults = new ArrayList<>();
    public List<KpiItemEquivalentCopy> kpiItemEquivalents = new ArrayList<>();
    public List<KpiMemberCopy> kpiMembers = new ArrayList<>();
    public List<KpiUserFactorCopy> kpiUserFactors = new ArrayList<>();
    public List<KpiDictItemCopy> kpiDictItems = new ArrayList<>();
    public List<KpiUserAttendanceCopy> kpiUserAttendances = new ArrayList<>();
    public List<KpiUserAttendanceCustomCopy> kpiUserAttendancesCustoms = new ArrayList<>();
    public KpiAccountTaskChild task_child = new KpiAccountTaskChild();
    public List<KpiItemResultCopyGroupDto> itemResultGroup = new ArrayList<>();
    public List<KpiItemEquivalentCopyGroupDto> itemEquivalentGroup = new ArrayList<>();
    public List<KpiCalculateGroupDto> calculateGroup = new ArrayList<>();
    public List<KpiClusterUnitCopy> kpiClusterUnitCopyList = new ArrayList<>();
    public KpiAccountTask task = new KpiAccountTask();
    public List<SysUser> users;
    public List<SysDictItem> dict;
    public Map<String, String> field_total = new HashMap<>();
    public List<String> indexErroTips = new ArrayList<>();
    public int count = 0;
    public Long seq = 0L;
    public List<KpiIndexCopyExt2> index_Allca = new ArrayList<>();
    public List<KpiCoefficientCopy> coefficients = new ArrayList<>();
    public List<KpiValueAdjustCopy> kpiValueAdjustCopies = new ArrayList<>();
    public List<PlanCodeMemberListDto> planCodeMemberLists = new ArrayList<>();
    public BigDecimal equitemtprice = BigDecimal.ZERO;
    //endregion


    public TaskCaculateService() {
        clean();
    }

    //region
    //清除
    public void clean() {
        equitemtprice = BigDecimal.ZERO;
        kpiAccountUnits = new ArrayList<>();
        kpiAllocationRules = new ArrayList<>();
        kpiCategorys = new ArrayList<>();
        kpiIndexs = new ArrayList<>();
        kpiIndexsAll = new ArrayList<>();
        kpiIndexFormulas = new ArrayList<>();
        kpiIndexFormulasObj = new ArrayList<>();
        kpiItems = new ArrayList<>();
        kpiItemResults = new ArrayList<>();
        kpiItemEquivalents = new ArrayList<>();
        kpiMembers = new ArrayList<>();
        kpiUserFactors = new ArrayList<>();
        kpiDictItems = new ArrayList<>();
        kpiUserAttendances = new ArrayList<>();
        kpiUserAttendancesCustoms = new ArrayList<>();
        task_child = new KpiAccountTaskChild();
        itemResultGroup = new ArrayList<>();
        itemEquivalentGroup = new ArrayList<>();
        calculateGroup = new ArrayList<>();
        task = new KpiAccountTask();
        kpiAllocationRulesCa = new ArrayList<>();
        //kpiAccountPlanChildCopies = new ArrayList<>();
        users = new ArrayList<>();
        dict = new ArrayList<>();
        index_Allca = new ArrayList<>();
        indexErroTips = new ArrayList<>();
        tempCodes = new ArrayList<>();
        coefficients = new ArrayList<>();
        count = 0;
        seq = 0L;
        kpiValueAdjustCopies = new ArrayList<>();
    }

    //进位规则
    public static RoundingMode getRoundingMode(String ruleName) {
        //默认值
        RoundingMode rule = RoundingMode.HALF_UP;
        if ("1".equals(ruleName)) {
            rule = RoundingMode.HALF_UP;
        } else if ("2".equals(ruleName)) {
            rule = RoundingMode.CEILING;
        } else if ("3".equals(ruleName)) {
            rule = RoundingMode.FLOOR;
        }
        return rule;
    }

    //取人员
    public List<SysUser> getUsers(KpiCalculateMapper kpiCalculateMapper) {
        return kpiCalculateMapper.getUsers(task.getTenantId());
    }

    //取字典
    public List<SysDictItem> getDicts(KpiCalculateMapper kpiCalculateMapper) {
        return kpiCalculateMapper.getDicts(task_child.getTenantId());
    }

    /**
     * 核算方案转存
     */
    public List<PlanCodeMemberListDto> planCopy(KpiAccountPlanMapper kpiAccountPlanMapper,KpiAccountTaskService kpiAccountTaskService,
                                               KpiAccountPlanCopyMapper kpiAccountPlanCopyMapper,KpiAccountTaskChildMapper kpiAccountTaskChildMapper ) {
        List<PlanCodeMemberListDto> outputs = new ArrayList<>();
        {
            List<KpiAccountPlan> plans = kpiAccountPlanMapper.selectList(
                    new QueryWrapper<KpiAccountPlan>()
                            .eq("category_code", task.getPlanCode())
                            .eq("del_flag", "0")
                            //.eq("status", "0")
                            .eq("tenant_id", task.getTenantId())
            );
            List<KpiAccountPlanCopy> plan_copy = new ArrayList<>();
            plans.forEach(r -> {
                KpiAccountPlanCopy entity = new KpiAccountPlanCopy();
                BeanUtil.copyProperties(r, entity);
                entity.setTaskChildId(task_child.getId());
                entity.setCopyDate(new Date());
                plan_copy.add(entity);

                KpiFormulaDto2.FieldListDTO dto = JSONObject.parseObject(r.getRange(), KpiFormulaDto2.FieldListDTO.class);
                outputs.add(new PlanCodeMemberListDto(r.getIndexCode(),r.getPlanCode(),
                        kpiAccountTaskService.getMemberListComm(dto,
                                kpiMembers,kpiUserAttendances,kpiAccountUnits)));
            });
            if (!plan_copy.isEmpty())
                kpiAccountPlanCopyMapper.insertBatchSomeColumn(plan_copy);

            //核算子方案转存
            List<String> plan_codes = Linq.of(plans).select(r -> r.getPlanCode()).toList();
            if (plan_codes.isEmpty()) {
                updateLog(TaskStatusEnum.S_96, "无可用核算方案",kpiAccountTaskChildMapper);
                throw new BizException("无可用核算方案");
            }
            updateLog(TaskStatusEnum.S_11,kpiAccountTaskChildMapper);
        }
        return outputs;
    }

    /**
     * 核算单元转存
     */
    public List<KpiAccountUnitCopy> unitCopy(KpiAccountUnitMapper kpiAccountUnitMapper,KpiAccountUnitCopyMapper kpiAccountUnitCopyMapper,KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        List<KpiAccountUnitCopy> outputs = new ArrayList<>();
        {
            List<KpiAccountUnit> list = kpiAccountUnitMapper.selectList(
                    new QueryWrapper<KpiAccountUnit>()
                            .eq("del_flag", "0")
                            //.eq("status", "0")
                            .eq("tenant_id", task.getTenantId())
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
                        .setThirdCode(r.getThirdCode())
                        .setAccountUserCode(r.getAccountUserCode())
                        .setDeptType(r.getDeptType())
                        .setFactor(r.getFactor());
                entity.setTaskChildId(task_child.getId());
                entity.setCopyDate(new Date());
                if ("1".equals(entity.getStatus())){
                    entity.setName(entity.getName()+"(停用)");
                }
                list_copy.add(entity);
            });
            if (!list_copy.isEmpty()) {
                List<List<KpiAccountUnitCopy>> partition = ListUtils.partition(list_copy, 2000);
                partition.parallelStream().forEach(r -> {
                    kpiAccountUnitCopyMapper.insertBatchSomeColumn(r);
                });
            }
            updateLog(TaskStatusEnum.S_12,kpiAccountTaskChildMapper);

            outputs.addAll(list_copy);
        }
        return outputs;
    }

    public List<KpiAccountUnitCopy> unitCopy2(KpiAccountUnitMapper kpiAccountUnitMapper) {
        List<KpiAccountUnitCopy> outputs = new ArrayList<>();
        List<KpiAccountUnit> list = kpiAccountUnitMapper.selectList(
                new QueryWrapper<KpiAccountUnit>()
                        .eq("del_flag", "0")
                        //.eq("status", "0")
                        .eq("tenant_id", task.getTenantId())
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
                    .setThirdCode(r.getThirdCode())
                    .setDelFlag(r.getDelFlag())
                    .setInitialized(r.getInitialized())
                    .setCreatedId(r.getCreatedId())
                    .setCreatedDate(r.getCreatedDate())
                    .setUpdatedId(r.getUpdatedId())
                    .setUpdatedDate(r.getUpdatedDate())
                    .setTenantId(r.getTenantId())
                    .setAccountUserCode(r.getAccountUserCode())
                    .setDeptType(r.getDeptType())
                    .setFactor(r.getFactor());
            entity.setTaskChildId(task_child.getId());
            entity.setCopyDate(new Date());
            if ("1".equals(entity.getStatus())){
                entity.setName(entity.getName()+"(停用)");
            }
            list_copy.add(entity);
        });

        outputs.addAll(list_copy);

        return outputs;
    }

    /**
     * 分摊公式转存
     */
    public List<KpiAllocationRuleCopy> ruleCopy(KpiAllocationRuleMapper kpiAllocationRuleMapper,KpiAllocationRuleCopyMapper kpiAllocationRuleCopyMapper,KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        kpiAllocationRulesCa = new ArrayList<>();
        List<KpiAllocationRuleCopy> outputs = new ArrayList<>();
        {
            List<KpiAllocationRule> list = kpiAllocationRuleMapper.selectList(
                    new QueryWrapper<KpiAllocationRule>()
                            .eq("plan_code", task.getPlanCode())
                            .eq("tenant_id", task.getTenantId())
            );
            List<KpiAllocationRuleCopy> list_copy = new ArrayList<>();
            list.forEach(r -> {
                KpiAllocationRuleCopy entity = new KpiAllocationRuleCopy();
                entity.setIndexCode(r.getIndexCode())
                        .setFormula(r.getFormula())
                        .setCheckFlag(r.getCheckFlag())
                        .setType(r.getType())
                        .setRatio(r.getRatio())
                        .setAllocationIndexs(r.getAllocationIndexs())
                        .setAllocationItems(r.getAllocationItems())
                        .setPlanCode(r.getPlanCode())
                        .setInMembersEmp(r.getInMembersEmp())
                        .setInMembersDept(r.getInMembersDept())
                        .setOutMembersDept(r.getOutMembersDept())
                        .setOutMembersImp(r.getOutMembersImp())
                        .setOutMembersEmp(r.getOutMembersEmp())
                        .setDocCode(r.getDocCode())
                        .setMemberCodes(r.getMemberCodes())
                        .setCreatedId(r.getCreatedId())
                        .setCreatedDate(r.getCreatedDate())
                        .setUpdatedDate(r.getUpdatedDate())
                        .setUpdatedId(r.getUpdatedId())
                        .setId(r.getId())
                        .setTenantId(r.getTenantId());
                entity.setTaskChildId(task_child.getId());
                entity.setCopyDate(new Date());
                list_copy.add(entity);
            });
            if (!list_copy.isEmpty()) {
                List<List<KpiAllocationRuleCopy>> partition = ListUtils.partition(list_copy, 2000);
                partition.parallelStream().forEach(r -> {
                    kpiAllocationRuleCopyMapper.insertBatchSomeColumn(r);
                });
            }
            updateLog(TaskStatusEnum.S_13,kpiAccountTaskChildMapper);
            outputs.addAll(list_copy);
        }

        List<Long> removeDepts = Linq.of(kpiAccountUnits).where(tt -> tt.getStatus().equals("1") || tt.getDelFlag().equals("1")).select(x -> x.getId()).toList();
        outputs.forEach(t -> {
            KpiAllocationRuleCaDto caDto = new KpiAllocationRuleCaDto();
            BeanUtil.copyProperties(t, caDto);
            if (!StringUtil.isNullOrEmpty(caDto.getAllocationIndexs())) {
                caDto.setAllocationIndexs_list(Linq.of(Arrays.asList(caDto.getAllocationIndexs().split(","))).toList());
            }
            if (!StringUtil.isNullOrEmpty(caDto.getAllocationItems())) {
                caDto.setAllocationItems_list(Linq.of(Arrays.asList(caDto.getAllocationItems().split(","))).toList());
            }
            if (!StringUtil.isNullOrEmpty(caDto.getInMembersDept())) {
                //caDto.setInMembersDept_list(Linq.of(Arrays.asList(caDto.getInMembersDept().split(","))).select(m -> Long.parseLong(m)).toList());
                List<Long> depts = Linq.of(Arrays.asList(caDto.getInMembersDept().split(","))).select(m -> Long.parseLong(m)).toList();
                depts.removeAll(removeDepts);
                caDto.setInMembersDept_list(depts);
                caDto.setInMembersDept(String.join(",",Linq.of(caDto.getInMembersDept_list()).select(x->x.toString()).toList()));
            }
            if (!StringUtil.isNullOrEmpty(caDto.getInMembersEmp())) {
                caDto.setInMembersEmp_list(Linq.of(Arrays.asList(caDto.getInMembersEmp().split(","))).select(m -> Long.parseLong(m)).toList());
            }
            if (!StringUtil.isNullOrEmpty(caDto.getOutMembersDept())) {
                if (caDto.getOutMembersDept().contains("{")) {
                    KpiAllocationRuleCaDto.outDept obj = JSONObject.parseObject(caDto.getOutMembersDept(), KpiAllocationRuleCaDto.outDept.class);
                    List<Long> depts = new ArrayList<>();
                    if (!obj.getOut_dept().isEmpty()){
                        depts.addAll(obj.getOut_dept());
                    }
                    if (!obj.getOut_dept_group().isEmpty()){
                        depts.addAll(Linq.of(kpiAccountUnits).where(x->obj.getOut_dept_group().contains(x.getCategoryCode())).select(x->x.getId()).toList());
                    }
                    if (!obj.getOut_dept_except().isEmpty()){
                        depts = Linq.of(depts).where(x->!obj.getOut_dept_except().contains(x)).toList();
                    }
                    caDto.setOutMembersDept_list(Linq.of(depts).distinct().toList());
                }
                else {
                    caDto.setOutMembersDept_list(Linq.of(Arrays.asList(caDto.getOutMembersDept().split(","))).select(m -> Long.parseLong(m)).toList());
                }
                List<Long> depts = caDto.getOutMembersDept_list();
                depts.removeAll(removeDepts);
                caDto.setOutMembersDept_list(depts);
                caDto.setOutMembersDept(String.join(",",Linq.of(caDto.getOutMembersDept_list()).select(x->x.toString()).toList()));
            }
            if (!StringUtil.isNullOrEmpty(caDto.getOutMembersEmp())) {
                caDto.setOutMembersEmp_list(Linq.of(Arrays.asList(caDto.getOutMembersEmp().split(","))).select(m -> Long.parseLong(m)).toList());
            }
            if (!StringUtil.isNullOrEmpty(caDto.getOutMembersImp())) {
                caDto.setOutMembersImp_list(Linq.of(Arrays.asList(caDto.getOutMembersImp().split(","))).select(m -> Long.parseLong(m)).toList());
            }
            if (!StringUtil.isNullOrEmpty(caDto.getMemberCodes())) {
                caDto.setMemberCodes_list(JSON.parseArray(t.getMemberCodes(), KpiFieldItemDto.class));
            }
            kpiAllocationRulesCa.add(caDto);
        });

        return outputs;
    }

    public List<KpiAllocationRuleCopy> ruleCopy2(KpiAllocationRuleMapper kpiAllocationRuleMapper) {
        kpiAllocationRulesCa = new ArrayList<>();
        List<KpiAllocationRuleCopy> outputs = new ArrayList<>();
        List<KpiAllocationRule> list = kpiAllocationRuleMapper.selectList(
                new QueryWrapper<KpiAllocationRule>()
                        .eq("plan_code", task.getPlanCode())
                        .eq("tenant_id", task.getTenantId())
        );
        List<KpiAllocationRuleCopy> list_copy = new ArrayList<>();
        list.forEach(r -> {
            KpiAllocationRuleCopy entity = new KpiAllocationRuleCopy();
            entity.setIndexCode(r.getIndexCode())
                    .setFormula(r.getFormula())
                    .setCheckFlag(r.getCheckFlag())
                    .setType(r.getType())
                    .setRule(r.getRule())
                    .setRatio(r.getRatio())
                    .setAllocationIndexs(r.getAllocationIndexs())
                    .setAllocationItems(r.getAllocationItems())
                    .setPlanCode(r.getPlanCode())
                    .setInMembersEmp(r.getInMembersEmp())
                    .setInMembersDept(r.getInMembersDept())
                    .setOutMembersDept(r.getOutMembersDept())
                    .setOutMembersImp(r.getOutMembersImp())
                    .setOutMembersEmp(r.getOutMembersEmp())
                    .setDocCode(r.getDocCode())
                    .setMemberCodes(r.getMemberCodes())
                    .setCreatedId(r.getCreatedId())
                    .setCreatedDate(r.getCreatedDate())
                    .setUpdatedDate(r.getUpdatedDate())
                    .setUpdatedId(r.getUpdatedId())
                    .setId(r.getId())
                    .setTenantId(r.getTenantId());
            entity.setTaskChildId(task_child.getId());
            entity.setCopyDate(new Date());
            list_copy.add(entity);
        });

        outputs.addAll(list_copy);
        List<Long> removeDepts = Linq.of(kpiAccountUnits).where(tt -> tt.getStatus().equals("1") || tt.getDelFlag().equals("1")).select(x -> x.getId()).toList();

        outputs.forEach(t -> {
            KpiAllocationRuleCaDto caDto = new KpiAllocationRuleCaDto();
            BeanUtil.copyProperties(t, caDto);
            if (!StringUtil.isNullOrEmpty(caDto.getAllocationIndexs())) {
                caDto.setAllocationIndexs_list(Linq.of(Arrays.asList(caDto.getAllocationIndexs().split(","))).toList());
            }
            if (!StringUtil.isNullOrEmpty(caDto.getAllocationItems())) {
                caDto.setAllocationItems_list(Linq.of(Arrays.asList(caDto.getAllocationItems().split(","))).toList());
            }
            if (!StringUtil.isNullOrEmpty(caDto.getInMembersDept())) {
                //caDto.setInMembersDept_list(Linq.of(Arrays.asList(caDto.getInMembersDept().split(","))).select(m -> Long.parseLong(m)).toList());
                List<Long> depts = Linq.of(Arrays.asList(caDto.getInMembersDept().split(","))).select(m -> Long.parseLong(m)).toList();
                depts.removeAll(removeDepts);
                caDto.setInMembersDept_list(depts);
                caDto.setInMembersDept(String.join(",",Linq.of(caDto.getInMembersDept_list()).select(x->x.toString()).toList()));
            }
            if (!StringUtil.isNullOrEmpty(caDto.getInMembersEmp())) {
                caDto.setInMembersEmp_list(Linq.of(Arrays.asList(caDto.getInMembersEmp().split(","))).select(m -> Long.parseLong(m)).toList());
            }
            if (!StringUtil.isNullOrEmpty(caDto.getOutMembersDept())) {
                if (caDto.getOutMembersDept().contains("{")) {
                    KpiAllocationRuleCaDto.outDept obj = JSONObject.parseObject(caDto.getOutMembersDept(), KpiAllocationRuleCaDto.outDept.class);
                    List<Long> depts = new ArrayList<>();
                    if (!obj.getOut_dept().isEmpty()){
                        depts.addAll(obj.getOut_dept());
                    }
                    if (!obj.getOut_dept_group().isEmpty()){
                        depts.addAll(Linq.of(kpiAccountUnits).where(x->obj.getOut_dept_group().contains(x.getCategoryCode())).select(x->x.getId()).toList());
                    }
                    if (!obj.getOut_dept_except().isEmpty()){
                        depts = Linq.of(depts).where(x->!obj.getOut_dept_except().contains(x)).toList();
                    }
                    caDto.setOutMembersDept_list(Linq.of(depts).distinct().toList());
                }
                else {
                    caDto.setOutMembersDept_list(Linq.of(Arrays.asList(caDto.getOutMembersDept().split(","))).select(m -> Long.parseLong(m)).toList());
                }
                List<Long> depts = caDto.getOutMembersDept_list();
                depts.removeAll(removeDepts);
                caDto.setOutMembersDept_list(depts);
                caDto.setOutMembersDept(String.join(",",Linq.of(caDto.getOutMembersDept_list()).select(x->x.toString()).toList()));
            }
            if (!StringUtil.isNullOrEmpty(caDto.getOutMembersEmp())) {
                caDto.setOutMembersEmp_list(Linq.of(Arrays.asList(caDto.getOutMembersEmp().split(","))).select(m -> Long.parseLong(m)).toList());
            }
            if (!StringUtil.isNullOrEmpty(caDto.getOutMembersImp())) {
                caDto.setOutMembersImp_list(Linq.of(Arrays.asList(caDto.getOutMembersImp().split(","))).select(m -> Long.parseLong(m)).toList());
            }
            if (!StringUtil.isNullOrEmpty(caDto.getMemberCodes())) {
                caDto.setMemberCodes_list(JSON.parseArray(t.getMemberCodes(), KpiFieldItemDto.class));
            }

            kpiAllocationRulesCa.add(caDto);
        });

        return outputs;
    }

    /**
     * 分类转存
     */
    public List<KpiCategoryCopy> cateGoryCopy(KpiCategoryMapper kpiCategoryMapper,KpiCategoryCopyMapper kpiCategoryCopyMapper,KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {

        List<KpiCategoryCopy> outputs = new ArrayList<>();
        {
            List<KpiCategory> list = kpiCategoryMapper.selectList(
                    new QueryWrapper<KpiCategory>()
                            .eq("del_flag", "0")
                            .eq("status", "0")
                            .eq("tenant_id", task.getTenantId())
                            .eq("busi_type", "1")
            );
            List<KpiCategoryCopy> list_copy = new ArrayList<>();
            list.forEach(r -> {
                KpiCategoryCopy entity = new KpiCategoryCopy();
                entity.setCategoryType(r.getCategoryType())
                        .setCategoryCode(r.getCategoryCode())
                        .setThirdCode(r.getThirdCode())
                        .setCategoryName(r.getCategoryName())
                        .setDescription(r.getDescription())
                        .setParentId(r.getParentId())
                        .setCreatedId(r.getCreatedId())
                        .setCreatedDate(r.getCreatedDate())
                        .setSeq(r.getSeq())
                        .setStatus(r.getStatus())
                        .setDelFlag(r.getDelFlag())
                        .setTenantId(r.getTenantId());
                entity.setTaskChildId(task_child.getId());
                entity.setCopyDate(new Date());
                list_copy.add(entity);
            });
            if (!list_copy.isEmpty()) {
                List<List<KpiCategoryCopy>> partition = ListUtils.partition(list_copy, 2000);
                partition.parallelStream().forEach(r -> {
                    kpiCategoryCopyMapper.insertBatchSomeColumn(r);
                });
            }
            updateLog(TaskStatusEnum.S_14,kpiAccountTaskChildMapper);

            outputs.addAll(list_copy);
        }
        return outputs;
    }

    public List<KpiCategoryCopy> cateGoryCopy2(KpiCategoryMapper kpiCategoryMapper) {

        List<KpiCategoryCopy> outputs = new ArrayList<>();
        List<KpiCategory> list = kpiCategoryMapper.selectList(
                new QueryWrapper<KpiCategory>()
                        .eq("del_flag", "0")
                        .eq("status", "0")
                        .eq("tenant_id", task.getTenantId())
                        .eq("busi_type", "1")
        );
        List<KpiCategoryCopy> list_copy = new ArrayList<>();
        list.forEach(r -> {
            KpiCategoryCopy entity = new KpiCategoryCopy();
            entity.setCategoryType(r.getCategoryType())
                    .setCategoryCode(r.getCategoryCode())
                    .setThirdCode(r.getThirdCode())
                    .setCategoryName(r.getCategoryName())
                    .setDescription(r.getDescription())
                    .setParentId(r.getParentId())
                    .setCreatedId(r.getCreatedId())
                    .setCreatedDate(r.getCreatedDate())
                    .setSeq(r.getSeq())
                    .setStatus(r.getStatus())
                    .setDelFlag(r.getDelFlag())
                    .setTenantId(r.getTenantId());
            entity.setTaskChildId(task_child.getId());
            entity.setCopyDate(new Date());
            list_copy.add(entity);
        });

        outputs.addAll(list_copy);
        return outputs;
    }

    /**
     * 指标转存
     **/
    public List<KpiIndexCopy> indexCopy(KpiIndexMapper kpiIndexMapper,KpiIndexCopyMapper kpiIndexCopyMapper,KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        List<KpiIndexCopy> outputs = new ArrayList<>();
        {
            List<KpiIndex> list = kpiIndexMapper.selectList(
                    new QueryWrapper<KpiIndex>()
                            .eq("del_flag", "0")
                            .eq("status", "0")
                            .eq("tenant_id", task.getTenantId())
            );
            List<KpiIndexCopy> list_copy = new ArrayList<>();
            list.forEach(r -> {
                KpiIndexCopy entity = new KpiIndexCopy();
                entity.setCode(r.getCode())
                        .setIndexUnit(r.getIndexUnit())
                        .setName(r.getName())
                        .setAccountObject(r.getAccountObject())
                        .setIndexProperty(r.getIndexProperty())
                        .setCategoryCode(r.getCategoryCode())
                        .setCarryRule(r.getCarryRule())
                        .setReservedDecimal(r.getReservedDecimal())
                        .setIndexFormula(r.getIndexFormula())
                        .setStatus(r.getStatus())
                        .setDelFlag(r.getDelFlag())
                        .setCreatedDate(r.getCreatedDate())
                        .setCreatedId(r.getCreatedId())
                        .setUpdatedId(r.getUpdatedId())
                        .setUpdatedDate(r.getUpdatedDate())
                        .setTenantId(r.getTenantId())
                        .setCaliber(r.getCaliber())
                        .setType(r.getType())
                        .setImpFlag(r.getImpFlag())
                        .setImpCategoryCode(r.getImpCategoryCode())
                        .setSecondFlag(r.getSecondFlag())
                        .setMemberCodes(r.getMemberCodes());
                //.setCountStatus(r.getDelFlag().equals("0")?"0":"1");
                entity.setTaskChildId(task_child.getId());
                entity.setCopyDate(new Date());
                entity.setCountStatus("0");
                list_copy.add(entity);
            });
            if (!list_copy.isEmpty()) {
                List<List<KpiIndexCopy>> partition = ListUtils.partition(list_copy, 2000);
                partition.parallelStream().forEach(r -> {
                    kpiIndexCopyMapper.insertBatchSomeColumn(r);
                });
            }
            updateLog(TaskStatusEnum.S_15,kpiAccountTaskChildMapper);

            outputs.addAll(list_copy);
        }
        return outputs;
    }

    public List<KpiIndexCopy> indexCopy2(List<String> code,KpiIndexMapper kpiIndexMapper) {
        List<KpiIndexCopy> outputs = new ArrayList<>();
        QueryWrapper<KpiIndex> eq = new QueryWrapper<KpiIndex>()
                .eq("del_flag", "0")
                .eq("status", "0")
                .eq("tenant_id", task.getTenantId());
        if (!code.isEmpty()) {
            eq.in("code", code);
        }
        List<KpiIndex> list = kpiIndexMapper.selectList(eq);
        List<KpiIndexCopy> list_copy = new ArrayList<>();
        list.forEach(r -> {
            KpiIndexCopy entity = new KpiIndexCopy();
            entity.setCode(r.getCode())
                    .setIndexUnit(r.getIndexUnit())
                    .setName(r.getName())
                    .setAccountObject(r.getAccountObject())
                    .setIndexProperty(r.getIndexProperty())
                    .setCategoryCode(r.getCategoryCode())
                    .setCarryRule(r.getCarryRule())
                    .setReservedDecimal(r.getReservedDecimal())
                    .setIndexFormula(r.getIndexFormula())
                    .setStatus(r.getStatus())
                    .setDelFlag(r.getDelFlag())
                    .setCreatedDate(r.getCreatedDate())
                    .setCreatedId(r.getCreatedId())
                    .setUpdatedId(r.getUpdatedId())
                    .setUpdatedDate(r.getUpdatedDate())
                    .setTenantId(r.getTenantId())
                    .setCaliber(r.getCaliber())
                    .setType(r.getType())
                    .setImpFlag(r.getImpFlag())
                    .setImpCategoryCode(r.getImpCategoryCode())
                    .setSecondFlag(r.getSecondFlag())
                    .setMemberCodes(r.getMemberCodes());
            entity.setTaskChildId(task_child.getId());
            entity.setCopyDate(new Date());
            entity.setCountStatus("0");
            list_copy.add(entity);
        });

        outputs.addAll(list_copy);

        return outputs;
    }

    /**
     * 指标公式转存
     */
    public List<KpiIndexFormulaCopy> indexFormulaCopy(KpiIndexFormulaMapper kpiIndexFormulaMapper,KpiIndexFormulaCopyMapper kpiIndexFormulaCopyMapper,
                                                      KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        List<KpiIndexFormulaCopy> outputs = new ArrayList<>();
        {
            List<KpiIndexFormula> list = kpiIndexFormulaMapper.selectList(
                    new QueryWrapper<KpiIndexFormula>()
                            .eq("tenant_id", task.getTenantId())
                            .apply("(plan_code = '"+task.getPlanCode()+"' or plan_code is null  or plan_code ='')")
                            .eq("del_flag", "0")
            );
            List<KpiIndexFormulaCopy> list_copy = new ArrayList<>();
            List<String> codes = Linq.of(kpiIndexs).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")).select(x -> x.getCode()).toList();
            list.forEach(r -> {
                if (codes.contains(r.getIndexCode())) {
                    KpiIndexFormulaCopy entity = new KpiIndexFormulaCopy();
                    entity.setId(r.getId())
                            .setPlanCode(r.getPlanCode())
                            .setFormula(r.getFormula())
                            .setShowFlag(r.getShowFlag())
                            .setCheckFlag(r.getCheckFlag())
                            .setIndexCode(r.getIndexCode())
                            .setCreatedId(r.getCreatedId())
                            .setCreatedDate(r.getCreatedDate())
                            .setUpdatedDate(r.getCreatedDate())
                            .setUpdatedId(r.getUpdatedId())
                            .setMemberCodes(r.getMemberCodes())
                            .setMemberIds(r.getMemberIds())
                            .setTenantId(r.getTenantId());
                    entity.setTaskChildId(task_child.getId());
                    entity.setCopyDate(new Date());
                    list_copy.add(entity);
                }
            });
            if (!list_copy.isEmpty()) {
                List<List<KpiIndexFormulaCopy>> partition = ListUtils.partition(list_copy, 2000);
                partition.parallelStream().forEach(r -> {
                    kpiIndexFormulaCopyMapper.insertBatchSomeColumn(r);
                });
            }
            updateLog(TaskStatusEnum.S_16,kpiAccountTaskChildMapper);
            outputs.addAll(list_copy);
        }
        return outputs;
    }

    public List<KpiIndexFormulaCopy> indexFormulaCopy2(List<String> code,KpiIndexFormulaMapper kpiIndexFormulaMapper) {
        List<KpiIndexFormulaCopy> outputs = new ArrayList<>();
        QueryWrapper<KpiIndexFormula> eq = new QueryWrapper<KpiIndexFormula>()
                .eq("tenant_id", task.getTenantId())
                .apply("(plan_code = '"+task.getPlanCode()+"' or plan_code is null  or plan_code ='')")
                .eq("del_flag", "0");
        if (!code.isEmpty()) {
            eq.in("index_code", code);
        }
        List<KpiIndexFormula> list = kpiIndexFormulaMapper.selectList(eq);

        List<KpiIndexFormulaCopy> list_copy = new ArrayList<>();
        List<String> codes = Linq.of(kpiIndexs).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")).select(x -> x.getCode()).toList();

        list.forEach(r -> {
            if (codes.contains(r.getIndexCode())) {
                KpiIndexFormulaCopy entity = new KpiIndexFormulaCopy();
                entity.setId(r.getId())
                        .setPlanCode(r.getPlanCode())
                        .setFormula(r.getFormula())
                        .setShowFlag(r.getShowFlag())
                        .setCheckFlag(r.getCheckFlag())
                        .setIndexCode(r.getIndexCode())
                        .setCreatedId(r.getCreatedId())
                        .setCreatedDate(r.getCreatedDate())
                        .setUpdatedDate(r.getCreatedDate())
                        .setUpdatedId(r.getUpdatedId())
                        .setMemberCodes(r.getMemberCodes())
                        .setMemberIds(r.getMemberIds())
                        .setTenantId(r.getTenantId());
                entity.setTaskChildId(task_child.getId());
                entity.setCopyDate(new Date());
                list_copy.add(entity);
            }
        });
        outputs.addAll(list_copy);
        return outputs;
    }

    /**
     * 指标公式适用对象转存
     */
    public List<KpiIndexFormulaObjCopy> indexFormulaObjCopy(KpiIndexFormulaObjMapper kpiIndexFormulaObjMapper,
                                                            KpiIndexFormulaObjCopyMapper kpiIndexFormulaObjCopyMapper,
                                                            KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        List<KpiIndexFormulaObjCopy> outputs = new ArrayList<>();
        List<KpiIndexFormulaObj> list = kpiIndexFormulaObjMapper.selectList(
                new QueryWrapper<KpiIndexFormulaObj>()
                        .eq("plan_code", task.getPlanCode())
                        .eq("tenant_id", task.getTenantId())
        );
        List<KpiIndexFormulaObjCopy> list_copy = new ArrayList<>();
        List<String> codes = Linq.of(kpiIndexs).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")).select(x -> x.getCode()).toList();
        list.forEach(r -> {
            if (codes.contains(r.getIndexCode())) {
                KpiIndexFormulaObjCopy entity = new KpiIndexFormulaObjCopy();
                entity.setFormulaId(r.getFormulaId())
                        .setPlanObjCategory(r.getPlanObjCategory())
                        .setIndexCode(r.getIndexCode())
                        .setPlanCode(r.getPlanCode())
                        .setPlanObj(r.getPlanObj())
                        .setCreatedDate(r.getCreatedDate())
                        .setCreatedId(r.getCreatedId())
                        .setUpdatedDate(r.getUpdatedDate())
                        .setExcludePerson(r.getExcludePerson())
                        .setExcludeDept(r.getExcludeDept())
                        .setPlanObjAccountType(r.getPlanObjAccountType())
                        .setUpdatedId(r.getUpdatedId())
                        .setTenantId(r.getTenantId());
                entity.setTaskChildId(task_child.getId());
                entity.setCopyDate(new Date());
                list_copy.add(entity);
            }
        });
        if (!list_copy.isEmpty()) {
            List<List<KpiIndexFormulaObjCopy>> partition = ListUtils.partition(list_copy, 2000);
            partition.parallelStream().forEach(r -> {
                kpiIndexFormulaObjCopyMapper.insertBatchSomeColumn(r);
            });
        }
        updateLog(TaskStatusEnum.S_17,kpiAccountTaskChildMapper);
        outputs = objCopyGetMemberList(outputs, list_copy);

        return outputs;
    }

    public List<KpiIndexFormulaObjCopy> indexFormulaObjCopy2(List<String> code,KpiIndexFormulaObjMapper kpiIndexFormulaObjMapper) {
        List<KpiIndexFormulaObjCopy> outputs = new ArrayList<>();
        QueryWrapper<KpiIndexFormulaObj> eq = new QueryWrapper<KpiIndexFormulaObj>()
                .eq("plan_code", task.getPlanCode())
                .eq("tenant_id", task.getTenantId());
        if (!code.isEmpty()) {
            eq.in("index_code", code);
        }
        List<KpiIndexFormulaObj> list = kpiIndexFormulaObjMapper.selectList(eq);
        List<KpiIndexFormulaObjCopy> list_copy = new ArrayList<>();
        List<String> codes = Linq.of(kpiIndexs).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")).select(x -> x.getCode()).toList();
        list.forEach(r -> {
            if (codes.contains(r.getIndexCode())) {
                KpiIndexFormulaObjCopy entity = new KpiIndexFormulaObjCopy();
                entity.setFormulaId(r.getFormulaId())
                        .setIndexCode(r.getIndexCode())
                        .setPlanCode(r.getPlanCode())
                        .setPlanObjCategory(r.getPlanObjCategory())
                        .setPlanObjAccountType(r.getPlanObjAccountType())
                        .setPlanObj(r.getPlanObj())
                        .setCreatedDate(r.getCreatedDate())
                        .setCreatedId(r.getCreatedId())
                        .setUpdatedDate(r.getUpdatedDate())
                        .setExcludePerson(r.getExcludePerson())
                        .setExcludeDept(r.getExcludeDept())
                        .setUpdatedId(r.getUpdatedId())
                        .setTenantId(r.getTenantId());
                entity.setTaskChildId(task_child.getId());
                entity.setCopyDate(new Date());
                list_copy.add(entity);
            }
        });

        outputs = objCopyGetMemberList(outputs, list_copy);
        return outputs;
    }

    public List<KpiIndexFormulaObjCopy> objCopyGetMemberList(List<KpiIndexFormulaObjCopy> outputs, List<KpiIndexFormulaObjCopy> list_copy) {
        List<Long> removeDepts = Linq.of(kpiAccountUnits).where(tt -> tt.getStatus().equals("1") || tt.getDelFlag().equals("1")).select(tt -> tt.getId()).toList();
        outputs.addAll(Linq.of(list_copy).where(t ->t.getPlanObj() != null && !removeDepts.contains(t.getPlanObj()) && t.getPlanObj() != -100L && t.getPlanObj() != -200L).toList());
        Linq.of(list_copy).where(t -> t.getPlanObj() == null || t.getPlanObj() == -100L || t.getPlanObj() == -200L).groupBy(KpiIndexFormulaObjCopy::getFormulaId).forEach(t -> {
            List<Long> ids = new ArrayList<>();

            for (KpiIndexFormulaObjCopy objCopy : t) {
                if (objCopy.getPlanObj() != null) {
                    if (objCopy.getPlanObj() == -100L) {
                        ids = Linq.of(kpiUserAttendances).select(KpiUserAttendanceCopy::getUserId).distinct().toList();
                    } else if (objCopy.getPlanObj() == -200L) {
                        ids = Linq.of(kpiAccountUnits).select(KpiAccountUnitCopy::getId).distinct().toList();
                    }
                }
                if (!StringUtil.isNullOrEmpty(objCopy.getPlanObjCategory())) {
                    //适用对象是分组的 换成人
                    List<Long> users = Linq.of(kpiMembers).where(m -> MemberEnum.ROLE_EMP.getType().equals(m.getMemberType())
                            && objCopy.getPlanObjCategory().equals(m.getHostCode()) && m.getMemberId() != null).select(m -> m.getMemberId()).toList();
                    ids.addAll(users);
                }
                if (!StringUtil.isNullOrEmpty(objCopy.getPlanObjAccountType())) {
                    //适用对象是核算分组 换成科室
                    List<Long> depts = Linq.of(kpiAccountUnits).where(m ->
                            objCopy.getPlanObjAccountType().equals(m.getCategoryCode())).select(m -> m.getId()).toList();
                    ids.addAll(depts);
                }
                KpiIndexCopy index = Linq.of(kpiIndexs).firstOrDefault(x -> x.getCode().equals(objCopy.getIndexCode()));
                if (index!=null && "2".equals(index.getCaliber())){
                    ids.removeAll(removeDepts);
                }
            }
            for (KpiIndexFormulaObjCopy objCopy : Linq.of(t).where(m ->
                    !StringUtil.isNullOrEmpty(m.getExcludePerson()) || !StringUtil.isNullOrEmpty(m.getExcludeDept()))) {
                if (!StringUtil.isNullOrEmpty(objCopy.getExcludePerson())) {
                    ids.removeAll(Linq.of(Arrays.asList(objCopy.getExcludePerson().split(","))).select(x -> Long.parseLong(x)).toList());
                }
                if (!StringUtil.isNullOrEmpty(objCopy.getExcludeDept())) {
                    ids.removeAll(Linq.of(Arrays.asList(objCopy.getExcludeDept().split(","))).select(x -> Long.parseLong(x)).toList());
                }
            }

            KpiIndexFormulaObjCopy r = t.first();
            ids.stream().distinct().forEach(m -> {
                if (!Linq.of(outputs).any(tt -> tt.getFormulaId().equals(r.getFormulaId()) && tt.getPlanObj().equals(m))) {
                    KpiIndexFormulaObjCopy entity = new KpiIndexFormulaObjCopy();
                    entity.setFormulaId(r.getFormulaId())
                            .setIndexCode(r.getIndexCode())
                            .setPlanCode(r.getPlanCode())
                            .setPlanObj(m)
                            .setCreatedDate(r.getCreatedDate())
                            .setCreatedId(r.getCreatedId())
                            .setUpdatedDate(r.getUpdatedDate())
                            .setUpdatedId(r.getUpdatedId())
                            .setTenantId(r.getTenantId());
                    outputs.add(entity);
                }
            });
        });
        return outputs;
    }

    /**
     * 核算项结果集转存
     */
    public List<KpiItemResultCopy> itemResultCopy(KpiItemResultMapper kpiItemResultMapper,KpiItemResultCopyMapper kpiItemResultCopyMapper,
                                                  KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        List<KpiItemResultCopy> outputs = new ArrayList<>();
        {
            List<KpiItemResultCopy> list_copy = kpiItemResultMapper.getList(
                    new QueryWrapper<KpiItemResult>()
                            .eq("period", task.getPeriod())
                            .eq("tenant_id", task.getTenantId())
                            .eq("busi_type", "1")
            );

            List<Long> rm=new ArrayList<>();
            for (KpiValueAdjustCopy adjust : Linq.of(kpiValueAdjustCopies).where(x->FormulaParamEnum.P_ITEM.getType().equals(x.getType()))) {
                KpiItemResultCopy calculate = new KpiItemResultCopy();
                String mateFlag = "0";
                if(adjust.getOperation().equals("="))
                {
                    if (adjust.getUserId() != null && adjust.getAccountUnit() != null) {
                        List<KpiItemResultCopy> li = Linq.of(list_copy).where(x -> adjust.getCode().equals(x.getCode()) && adjust.getUserId().equals(x.getUserId()) && adjust.getAccountUnit().equals(x.getDeptId())).toList();
                        if (CollectionUtils.isNotEmpty(li)) {
                            rm.addAll(Linq.of(li).select(r -> r.getId()).toList());
                            mateFlag = li.get(0).getMateFlag();
                        }
                    } else if (adjust.getUserId() != null) {
                        List<KpiItemResultCopy> li = Linq.of(list_copy).where(x -> adjust.getCode().equals(x.getCode()) && adjust.getUserId().equals(x.getUserId())).toList();
                        if (CollectionUtils.isNotEmpty(li)) {
                            rm.addAll(Linq.of(li).select(r -> r.getId()).toList());
                            mateFlag = li.get(0).getMateFlag();
                        }
                    } else if (adjust.getAccountUnit() != null) {
                        List<KpiItemResultCopy> li = Linq.of(list_copy).where(x -> adjust.getCode().equals(x.getCode()) && adjust.getAccountUnit().equals(x.getDeptId())).toList();
                        if (CollectionUtils.isNotEmpty(li)) {
                            rm.addAll(Linq.of(li).select(r -> r.getId()).toList());
                            mateFlag = li.get(0).getMateFlag();
                        }                    }
                    calculate.setValue(adjust.getValue());
                }
                else {
                    if (adjust.getAccountUnit() != null) {
                        mateFlag = "1";
                    }
                    calculate.setValue(new BigDecimal(adjust.getOperation() + adjust.getValue()));
                }

                calculate.setPeriod(adjust.getPeriod());
                calculate.setCode(adjust.getCode());
                calculate.setUserId(adjust.getUserId());
                calculate.setDeptId(adjust.getAccountUnit());
                calculate.setMateFlag(mateFlag);
                calculate.setTenantId(task.getTenantId());
                calculate.setBusiType("1");
                list_copy.add(calculate);
            }
            List<Long> finalRm = rm;
            list_copy=Linq.of((list_copy)).where(r->!finalRm.contains(r.getId())).toList();
            list_copy.forEach(r -> {
                r.setTaskChildId(task_child.getId());
                r.setCopyDate(new Date());
                if (r.getValue() == null) {
                    r.setValue(BigDecimal.ZERO);
                }
                if (r.getUserId() != null) {
                    List<KpiUserAttendanceCopy> cs = Linq.of(kpiUserAttendances).where(t -> t.getUserId().equals(r.getUserId())).toList();
                    if (!cs.isEmpty()) {
                        r.setUserName(cs.get(0).getEmpName());
                        r.setEmpId(cs.get(0).getEmpId());
                        r.setUserType(String.join(",", Linq.of(cs).select(o -> o.getUserType()).distinct().toList()));
                    }
                }
                if (r.getDeptId() != null) {
                    KpiAccountUnitCopy c = Linq.of(kpiAccountUnits).firstOrDefault(t -> t.getId().equals(r.getDeptId()));
                    if (c != null) {
                        r.setDeptName(c.getName());
                        SysDictItem s = Linq.of(dict).firstOrDefault(t -> t.getItemValue().equals(c.getCategoryCode()));
                        if (s != null) {
                            r.setGroupName(s.getLabel());
                        }
                        SysDictItem dict3 = Linq.of(dict).firstOrDefault(d -> d.getItemValue().equals(c.getAccountTypeCode()) && "kpi_unit_calc_type".equals(d.getDictType()));
                        if (dict3 != null) {
                            r.setUnitType(dict3.getLabel());
                        }
                        SysDictItem dict2 = Linq.of(dict).firstOrDefault(d -> d.getItemValue().equals(c.getAccountUserCode()) && "user_type".equals(d.getDictType()));
                        if (dict2 != null) {
                            r.setDeptUserType(dict2.getLabel());
                        }
                    }
                }
            });
            if (!list_copy.isEmpty()) {
                List<List<KpiItemResultCopy>> partition = ListUtils.partition(list_copy, 500);
                partition.parallelStream().forEach(r -> {
                    kpiItemResultCopyMapper.insertBatchSomeColumn(r);
                });
            }
            updateLog(TaskStatusEnum.S_18,kpiAccountTaskChildMapper);
            outputs.addAll(list_copy);
        }
        itemResultGroup = Linq.of(outputs).groupBy(r -> r.getCode()).select(r -> new KpiItemResultCopyGroupDto(r.getKey(),
                Linq.of(outputs).where(rr -> rr.getCode().equals(r.getKey())).toList())).toList();
        itemResultGroup.forEach(r -> {
            KpiItemCopy kpiItemCopy = Linq.of(kpiItems).firstOrDefault(t -> t.getCode().equals(r.getCode()));
            if (kpiItemCopy != null) {
                r.setCaliber(kpiItemCopy.getCaliber());
            }
        });
        return outputs;
    }

    public List<KpiItemResultCopy> itemResultCopy2(KpiItemResultMapper kpiItemResultMapper) {
        List<KpiItemResultCopy> outputs = new ArrayList<>();
        List<KpiItemResultCopy> list_copy = kpiItemResultMapper.getList(
                new QueryWrapper<KpiItemResult>()
                        .eq("period", task.getPeriod())
                        .eq("tenant_id", task.getTenantId())
                        .eq("busi_type", "1")
        );
        List<Long> rm = new ArrayList<>();
        for (KpiValueAdjustCopy adjust : Linq.of(kpiValueAdjustCopies).where(x -> FormulaParamEnum.P_ITEM.getType().equals(x.getType()))) {
            KpiItemResultCopy calculate = new KpiItemResultCopy();
            String mateFlag = "0";
            if (adjust.getOperation().equals("=")) {
                if (adjust.getUserId() != null && adjust.getAccountUnit() != null) {
                    List<KpiItemResultCopy> li = Linq.of(list_copy).where(x -> adjust.getCode().equals(x.getCode()) && adjust.getUserId().equals(x.getUserId()) && adjust.getAccountUnit().equals(x.getDeptId())).toList();
                    if (CollectionUtils.isNotEmpty(li)) {
                        rm.addAll(Linq.of(li).select(r -> r.getId()).toList());
                        mateFlag = li.get(0).getMateFlag();
                    }
                } else if (adjust.getUserId() != null) {
                    List<KpiItemResultCopy> li = Linq.of(list_copy).where(x -> adjust.getCode().equals(x.getCode()) && adjust.getUserId().equals(x.getUserId())).toList();
                    if (CollectionUtils.isNotEmpty(li)) {
                        rm.addAll(Linq.of(li).select(r -> r.getId()).toList());
                        mateFlag = li.get(0).getMateFlag();
                    }
                } else if (adjust.getAccountUnit() != null) {
                    List<KpiItemResultCopy> li = Linq.of(list_copy).where(x -> adjust.getCode().equals(x.getCode()) && adjust.getAccountUnit().equals(x.getDeptId())).toList();
                    if (CollectionUtils.isNotEmpty(li)) {
                        rm.addAll(Linq.of(li).select(r -> r.getId()).toList());
                        mateFlag = li.get(0).getMateFlag();
                    }
                }
                calculate.setValue(adjust.getValue());
            } else {
                if (adjust.getAccountUnit() != null) {
                    mateFlag = "1";
                }
                calculate.setValue(new BigDecimal(adjust.getOperation() + adjust.getValue()));
            }

            calculate.setPeriod(adjust.getPeriod());
            calculate.setCode(adjust.getCode());
            calculate.setUserId(adjust.getUserId());
            calculate.setDeptId(adjust.getAccountUnit());
            calculate.setMateFlag(mateFlag);
            calculate.setTenantId(task.getTenantId());
            calculate.setBusiType("1");
            list_copy.add(calculate);
        }
        List<Long> finalRm = rm;
        list_copy = Linq.of((list_copy)).where(r -> !finalRm.contains(r.getId())).toList();
        list_copy.forEach(r -> {
            r.setTaskChildId(task_child.getId());
            r.setCopyDate(new Date());
            if (r.getValue() == null) {
                r.setValue(BigDecimal.ZERO);
            }
        });

        outputs.addAll(list_copy);
        itemResultGroup = Linq.of(outputs).groupBy(r -> r.getCode()).select(r -> new KpiItemResultCopyGroupDto(r.getKey(),
                Linq.of(outputs).where(rr -> rr.getCode().equals(r.getKey())).toList())).toList();
        itemResultGroup.forEach(r -> {
            KpiItemCopy kpiItemCopy = Linq.of(kpiItems).firstOrDefault(t -> t.getCode().equals(r.getCode()));
            if (kpiItemCopy != null) {
                r.setCaliber(kpiItemCopy.getCaliber());
            }
        });
        return outputs;
    }

    /**
     * 核算项结果集转存
     */
    public List<KpiItemEquivalentCopy> equivalentCopy(KpiItemEquivalentMapper kpiItemEquivalentMapper,KpiItemEquivalentCopyMapper kpiItemEquivalentCopyMapper,
                                                  KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        List<KpiItemEquivalentCopy> outputs = new ArrayList<>();
        {
            List<KpiItemEquivalentCopy> list_copy = kpiItemEquivalentMapper.getList(
                    new QueryWrapper<KpiItemEquivalent>()
                            .eq("period", task.getPeriod())
                            .eq("tenant_id", task.getTenantId())
                            .eq("del_flag", "0")
            );
            list_copy.forEach(r -> {
                r.setTaskChildId(task_child.getId());
                r.setCopyDate(new Date());
                if (r.getTotalEquivalent() == null) {
                    r.setTotalEquivalent(BigDecimal.ZERO);
                }
                if (r.getUserId() != null) {
                    List<KpiUserAttendanceCopy> cs = Linq.of(kpiUserAttendances).where(t -> t.getUserId().equals(r.getUserId())).toList();
                    if (!cs.isEmpty()) {
                        r.setUserName(cs.get(0).getEmpName());
                        r.setEmpId(cs.get(0).getEmpId());
                        r.setUserType(String.join(",", Linq.of(cs).select(o -> o.getUserType()).distinct().toList()));
                    }
                }
                if (r.getAccountUnitId() != null) {
                    KpiAccountUnitCopy c = Linq.of(kpiAccountUnits).firstOrDefault(t -> t.getId().equals(r.getAccountUnitId()));
                    if (c != null) {
                        r.setDeptName(c.getName());
                        SysDictItem s = Linq.of(dict).firstOrDefault(t -> t.getItemValue().equals(c.getCategoryCode()));
                        if (s != null) {
                            r.setGroupName(s.getLabel());
                        }
                        SysDictItem dict3 = Linq.of(dict).firstOrDefault(d -> d.getItemValue().equals(c.getAccountTypeCode()) && "kpi_unit_calc_type".equals(d.getDictType()));
                        if (dict3 != null) {
                            r.setUnitType(dict3.getLabel());
                        }
                        SysDictItem dict2 = Linq.of(dict).firstOrDefault(d -> d.getItemValue().equals(c.getAccountUserCode()) && "user_type".equals(d.getDictType()));
                        if (dict2 != null) {
                            r.setDeptUserType(dict2.getLabel());
                        }
                    }
                }
            });
            if (!list_copy.isEmpty()) {
                List<List<KpiItemEquivalentCopy>> partition = ListUtils.partition(list_copy, 500);
                partition.parallelStream().forEach(r -> {
                    kpiItemEquivalentCopyMapper.insertBatchSomeColumn(r);
                });
            }
            updateLog(TaskStatusEnum.S_18,kpiAccountTaskChildMapper);
            outputs.addAll(list_copy);
        }
        itemEquivalentGroup = Linq.of(outputs).groupBy(r -> r.getCode()).select(r -> new KpiItemEquivalentCopyGroupDto(r.getKey(),
                Linq.of(outputs).where(rr -> rr.getCode().equals(r.getKey())).toList())).toList();
        itemEquivalentGroup.forEach(r -> {
            KpiItemCopy kpiItemCopy = Linq.of(kpiItems).firstOrDefault(t -> t.getCode().equals(r.getCode()));
            if (kpiItemCopy != null) {
                if("1".equals(kpiItemCopy.getAssignFlag()))
                {
                    r.setCaliber("1");
                }
                else {
                    r.setCaliber(kpiItemCopy.getCaliber());
                }
            }
            r.setList(Linq.of(r.getList()).where(x->x.getEquivalentType().equals(r.getCaliber())).toList());
        });


        return outputs;
    }

    public List<KpiItemEquivalentCopy> equivalentCopy2(KpiItemEquivalentMapper kpiItemEquivalentMapper) {
        List<KpiItemEquivalentCopy> outputs = new ArrayList<>();
        List<KpiItemEquivalentCopy> list_copy = kpiItemEquivalentMapper.getList(
                new QueryWrapper<KpiItemEquivalent>()
                        .eq("period", task.getPeriod())
                        .eq("tenant_id", task.getTenantId())
                        .eq("del_flag", "0")
        );
        list_copy.forEach(r -> {
            r.setTaskChildId(task_child.getId());
            r.setCopyDate(new Date());
            if (r.getTotalEquivalent() == null) {
                r.setTotalEquivalent(BigDecimal.ZERO);
            }
        });

        outputs.addAll(list_copy);
        itemEquivalentGroup = Linq.of(outputs).groupBy(r -> r.getCode()).select(r -> new KpiItemEquivalentCopyGroupDto(r.getKey(),
                Linq.of(outputs).where(rr -> rr.getCode().equals(r.getKey())).toList())).toList();
        itemEquivalentGroup.forEach(r -> {
            KpiItemCopy kpiItemCopy = Linq.of(kpiItems).firstOrDefault(t -> t.getCode().equals(r.getCode()));
            if (kpiItemCopy != null) {
                if("1".equals(kpiItemCopy.getAssignFlag()))
                {
                    r.setCaliber("1");
                }
                else {
                    r.setCaliber(kpiItemCopy.getCaliber());
                }
            }
            r.setList(Linq.of(r.getList()).where(x->x.getEquivalentType().equals(r.getCaliber())).toList());
        });
        return outputs;
    }

    /**
     * member转存
     */
    public List<KpiMemberCopy> memberCopy(KpiMemberMapper kpiMemberMapper,KpiMemberCopyMapper kpiMemberCopyMapper,
                                          KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        List<KpiMemberCopy> outputs = new ArrayList<>();
        String str = MemberEnum.ACCOUNT_UNIT_RELATION.getType()
                + "," + MemberEnum.IMPUTATION_DEPT_EMP.getType()
                + "," + MemberEnum.ROLE_EMP.getType()
                + "," + MemberEnum.EMP_TYPE.getType()
                + "," + MemberEnum.ROLE_EMP_ZW.getType();
        {
            List<KpiMember> list = kpiMemberMapper.selectList(
                    new QueryWrapper<KpiMember>()
                            .in("period", Arrays.asList((task.getPeriod() + ",0").split(",")))
                            .eq("tenant_id", task.getTenantId())
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
                entity.setTaskChildId(task_child.getId());
                entity.setCopyDate(new Date());
                list_copy.add(entity);
            });
            if (!list_copy.isEmpty()) {
                List<List<KpiMemberCopy>> partition = ListUtils.partition(list_copy, 2000);
                partition.parallelStream().forEach(r -> {
                    kpiMemberCopyMapper.insertBatchSomeColumn(r);
                });
            }
            updateLog(TaskStatusEnum.S_19,kpiAccountTaskChildMapper);
            outputs.addAll(list_copy);
        }
        return outputs;
    }

    public List<KpiMemberCopy> memberCopy2(KpiMemberMapper kpiMemberMapper) {
        List<KpiMemberCopy> outputs = new ArrayList<>();
        String str = MemberEnum.ACCOUNT_UNIT_RELATION.getType()
                + "," + MemberEnum.IMPUTATION_DEPT_EMP.getType()
                + "," + MemberEnum.ROLE_EMP.getType()
                + "," + MemberEnum.EMP_TYPE.getType()
                + "," + MemberEnum.ROLE_EMP_ZW.getType();
        List<KpiMember> list = kpiMemberMapper.selectList(
                new QueryWrapper<KpiMember>()
                        .in("period", Arrays.asList((task.getPeriod() + ",0").split(",")))
                        .eq("tenant_id", task.getTenantId())
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
            entity.setTaskChildId(task_child.getId());
            entity.setCopyDate(new Date());
            list_copy.add(entity);
        });
        outputs.addAll(list_copy);
        return outputs;
    }

    public List<KpiUserFactorCopy> userFactorCopy(KpiUserFactorMapper kpiUserFactorMapper,KpiUserFactorCopyMapper kpiUserFactorCopyMapper,
                                                  KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        List<KpiUserFactorCopy> outputs = new ArrayList<>();
        {
            List<KpiUserFactor> list = kpiUserFactorMapper.selectList(
                    new QueryWrapper<KpiUserFactor>()
                            .eq("tenant_id", task.getTenantId())
                            .eq("del_flag", "0")
            );
            List<KpiUserFactorCopy> list_copy = new ArrayList<>();
            list.forEach(r -> {
                KpiUserFactorCopy entity = new KpiUserFactorCopy();
                entity.setDeptId(r.getDeptId())
                        .setType(r.getType())
                        .setUserId(r.getUserId())
                        .setDictType(r.getDictType())
                        .setItemCode(r.getItemCode())
                        .setValue(r.getValue())
                        .setCreateTime(r.getCreateTime())
                        .setUpdateTime(r.getUpdateTime())
                        .setDelFlag(r.getDelFlag())
                        .setTenantId(r.getTenantId());
                entity.setTaskChildId(task_child.getId());
                entity.setCopyDate(new Date());
                list_copy.add(entity);
            });
            if (!list_copy.isEmpty()) {
                List<List<KpiUserFactorCopy>> partition = ListUtils.partition(list_copy, 2000);
                partition.parallelStream().forEach(r -> {
                    kpiUserFactorCopyMapper.insertBatchSomeColumn(r);
                });
            }
            updateLog(TaskStatusEnum.S_26,kpiAccountTaskChildMapper);
            outputs.addAll(list_copy);
        }
        return outputs;
    }

    public List<KpiUserFactorCopy> userFactorCopy2(KpiUserFactorMapper kpiUserFactorMapper) {
        List<KpiUserFactorCopy> outputs = new ArrayList<>();
        {
            List<KpiUserFactor> list = kpiUserFactorMapper.selectList(
                    new QueryWrapper<KpiUserFactor>()
                            .eq("tenant_id", task.getTenantId())
                            .eq("del_flag", "0")
            );
            List<KpiUserFactorCopy> list_copy = new ArrayList<>();
            list.forEach(r -> {
                KpiUserFactorCopy entity = new KpiUserFactorCopy();
                entity.setDeptId(r.getDeptId())
                        .setType(r.getType())
                        .setUserId(r.getUserId())
                        .setDictType(r.getDictType())
                        .setItemCode(r.getItemCode())
                        .setValue(r.getValue())
                        .setCreateTime(r.getCreateTime())
                        .setUpdateTime(r.getUpdateTime())
                        .setDelFlag(r.getDelFlag())
                        .setTenantId(r.getTenantId());
                entity.setTaskChildId(task_child.getId());
                entity.setCopyDate(new Date());
                list_copy.add(entity);
            });
            outputs.addAll(list_copy);
        }
        return outputs;
    }

    public List<KpiDictItemCopy> dictItemCopy(KpiDictItemMapper kpiDictItemMapper,KpiDictItemCopyMapper kpiDictItemCopyMapper,
                                                  KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        List<KpiDictItemCopy> outputs = new ArrayList<>();
        {
            List<KpiDictItem> list = kpiDictItemMapper.selectList(
                    new QueryWrapper<KpiDictItem>()
                            .eq("tenant_id", task.getTenantId())
                            .eq("del_flag", "0")
            );
            List<KpiDictItemCopy> list_copy = new ArrayList<>();
            list.forEach(r -> {
                KpiDictItemCopy entity = new KpiDictItemCopy();
                entity.setDictId(r.getDictId())
                        .setItemValue(r.getItemValue())
                        .setItemCode(r.getItemCode())
                        .setParentCode(r.getParentCode())
                        .setLabel(r.getLabel())
                        .setDictType(r.getDictType())
                        .setDescription(r.getDescription())
                        .setSortOrder(r.getSortOrder())
                        .setCreateBy(r.getCreateBy())
                        .setUpdateBy(r.getUpdateBy())
                        .setCreateTime(r.getCreateTime())
                        .setUpdateTime(r.getUpdateTime())
                        .setRemarks(r.getRemarks())
                        .setDelFlag(r.getDelFlag())
                        .setStatus(r.getStatus())
                        .setPerformanceSubsidyValue(r.getPerformanceSubsidyValue())
                        .setPersonnelFactorValue(r.getPersonnelFactorValue())
                        .setTenantId(r.getTenantId());
                entity.setTaskChildId(task_child.getId());
                entity.setCopyDate(new Date());
                list_copy.add(entity);
            });
            if (!list_copy.isEmpty()) {
                List<List<KpiDictItemCopy>> partition = ListUtils.partition(list_copy, 2000);
                partition.parallelStream().forEach(r -> {
                    kpiDictItemCopyMapper.insertBatchSomeColumn(r);
                });
            }
            updateLog(TaskStatusEnum.S_27,kpiAccountTaskChildMapper);
            outputs.addAll(list_copy);
        }
        return outputs;
    }

    public List<KpiDictItemCopy> dictItemCopy2(KpiDictItemMapper kpiDictItemMapper) {
        List<KpiDictItemCopy> outputs = new ArrayList<>();
        {
            List<KpiDictItem> list = kpiDictItemMapper.selectList(
                    new QueryWrapper<KpiDictItem>()
                            .eq("tenant_id", task.getTenantId())
                            .eq("del_flag", "0")
            );
            List<KpiDictItemCopy> list_copy = new ArrayList<>();
            list.forEach(r -> {
                KpiDictItemCopy entity = new KpiDictItemCopy();
                entity.setDictId(r.getDictId())
                        .setItemValue(r.getItemValue())
                        .setItemCode(r.getItemCode())
                        .setParentCode(r.getParentCode())
                        .setLabel(r.getLabel())
                        .setDictType(r.getDictType())
                        .setDescription(r.getDescription())
                        .setSortOrder(r.getSortOrder())
                        .setCreateBy(r.getCreateBy())
                        .setUpdateBy(r.getUpdateBy())
                        .setCreateTime(r.getCreateTime())
                        .setUpdateTime(r.getUpdateTime())
                        .setRemarks(r.getRemarks())
                        .setDelFlag(r.getDelFlag())
                        .setStatus(r.getStatus())
                        .setPerformanceSubsidyValue(r.getPerformanceSubsidyValue())
                        .setPersonnelFactorValue(r.getPersonnelFactorValue())
                        .setTenantId(r.getTenantId());
                entity.setTaskChildId(task_child.getId());
                entity.setCopyDate(new Date());
                list_copy.add(entity);
            });
            outputs.addAll(list_copy);
        }
        return outputs;
    }

    //调整值转存
    public List<KpiValueAdjustCopy> kpiValueAdjustCopy(KpiValueAdjustMapper kpiValueAdjustMapper,KpiValueAdjustCopyMapper kpiValueAdjustCopyMapper,
                                                       KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        List<KpiValueAdjustCopy> outputs = new ArrayList<>();
        {
            List<KpiValueAdjust> list = kpiValueAdjustMapper.selectList(
                    new QueryWrapper<KpiValueAdjust>()
                            .eq("period", task.getPeriod())
                            .eq("busi_type", "1")
            );
            List<KpiValueAdjustCopy> list_copy = new ArrayList<>();
            list.forEach(r -> {
                KpiValueAdjustCopy entity = new KpiValueAdjustCopy();
                entity.setType(r.getType())
                        .setCode(r.getCode())
                        .setOperation(r.getOperation())
                        .setValue(r.getValue())
                        .setTenantId(r.getTenantId())
                        .setAccountUnit(r.getAccountUnit())
                        .setUserId(r.getUserId())
                        .setBusiType(r.getBusiType())
                        .setPeriod(r.getPeriod());
                entity.setTaskChildId(task_child.getId());
                entity.setCopyDate(new Date());
                list_copy.add(entity);
            });
            if (!list_copy.isEmpty()) {
                List<List<KpiValueAdjustCopy>> partition = ListUtils.partition(list_copy, 2000);
                partition.parallelStream().forEach(r -> {
                    kpiValueAdjustCopyMapper.insertBatchSomeColumn(r);
                });
            }
            updateLog(TaskStatusEnum.S_24,kpiAccountTaskChildMapper);

            outputs.addAll(list_copy);
        }
        return outputs;
    }

    public List<KpiValueAdjustCopy> kpiValueAdjustCopy2(KpiValueAdjustMapper kpiValueAdjustMapper,KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        List<KpiValueAdjustCopy> outputs = new ArrayList<>();
        {
            List<KpiValueAdjust> list = kpiValueAdjustMapper.selectList(
                    new QueryWrapper<KpiValueAdjust>()
                            .eq("period", task.getPeriod())
                            .eq("busi_type", "1")
            );
            List<KpiValueAdjustCopy> list_copy = new ArrayList<>();
            list.forEach(r -> {
                KpiValueAdjustCopy entity = new KpiValueAdjustCopy();
                entity.setType(r.getType())
                        .setCode(r.getCode())
                        .setOperation(r.getOperation())
                        .setValue(r.getValue())
                        .setTenantId(r.getTenantId())
                        .setAccountUnit(r.getAccountUnit())
                        .setUserId(r.getUserId())
                        .setBusiType(r.getBusiType())
                        .setPeriod(r.getPeriod());
                entity.setTaskChildId(task_child.getId());
                entity.setCopyDate(new Date());
                list_copy.add(entity);
            });
            updateLog(TaskStatusEnum.S_24,kpiAccountTaskChildMapper);

            outputs.addAll(list_copy);
        }
        return outputs;
    }

    /**
     * 人员考勤转存
     */
    public List<KpiUserAttendanceCopy> userCopy(KpiUserAttendanceMapper kpiUserAttendanceMapper,KpiUserAttendanceCopyMapper kpiUserAttendanceCopyMapper,
                                                KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        List<KpiUserAttendanceCopy> outputs = new ArrayList<>();
        {
            List<KpiUserAttendance> list = kpiUserAttendanceMapper.selectList(
                    new QueryWrapper<KpiUserAttendance>()
                            .eq("period", task.getPeriod())
                            .eq("del_flag", "0")
                            .eq("tenant_id", task.getTenantId())
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
                entity.setTaskChildId(task_child.getId());
                entity.setCopyDate(new Date());
                if (entity.getAttendRate() == null){
                    entity.setAttendRate(BigDecimal.ZERO);
                }
                if (entity.getRegisteredRate() == null){
                    entity.setRegisteredRate(BigDecimal.ZERO);
                }
                list_copy.add(entity);
            });
            if (!list_copy.isEmpty()) {
                List<List<KpiUserAttendanceCopy>> partition = ListUtils.partition(list_copy, 2000);
                partition.parallelStream().forEach(r -> {
                    kpiUserAttendanceCopyMapper.insertBatchSomeColumn(r);
                });
            }
            updateLog(TaskStatusEnum.S_20,kpiAccountTaskChildMapper);

            outputs.addAll(list_copy);
        }
        return outputs;
    }

    public List<KpiUserAttendanceCopy> userCopy2(KpiUserAttendanceMapper kpiUserAttendanceMapper) {
        List<KpiUserAttendanceCopy> outputs = new ArrayList<>();
        List<KpiUserAttendance> list = kpiUserAttendanceMapper.selectList(
                new QueryWrapper<KpiUserAttendance>()
                        .eq("period", task.getPeriod())
                        .eq("del_flag", "0")
                        .eq("tenant_id", task.getTenantId())
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
            entity.setTaskChildId(task_child.getId());
            entity.setCopyDate(new Date());
            if (entity.getAttendRate() == null){
                entity.setAttendRate(BigDecimal.ZERO);
            }
            if (entity.getRegisteredRate() == null){
                entity.setRegisteredRate(BigDecimal.ZERO);
            }
            list_copy.add(entity);
        });

        outputs.addAll(list_copy);
        return outputs;
    }

    //todo  人员考勤自定义备份
    public List<KpiUserAttendanceCustomCopy> userCustomCopy(KpiUserAttendanceCustomMapper kpiUserAttendanceCustomMapper,
                                                            KpiUserAttendanceCustomCopyMapper kpiUserAttendanceCustomCopyMapper,
                                                            KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        List<KpiUserAttendanceCustomCopy> list = kpiUserAttendanceCustomMapper.getList(
                new QueryWrapper<KpiUserAttendanceCustomCopy>()
                        .eq("a.period", task.getPeriod())
                        .eq("a.del_flag", "0")
                        .eq("a.tenant_id", task.getTenantId())
                        .eq("a.busi_type", "1")
        );
        list.forEach(r -> {
            r.setTaskChildId(task_child.getId());
            r.setCopyDate(new Date());
        });

        if (!list.isEmpty()) {
            List<List<KpiUserAttendanceCustomCopy>> partition = ListUtils.partition(list, 500);
            partition.parallelStream().forEach(r -> {
                kpiUserAttendanceCustomCopyMapper.insertBatchSomeColumn(r);
            });
        }
        updateLog(TaskStatusEnum.S_25,kpiAccountTaskChildMapper);

        return list;
    }

    public List<KpiUserAttendanceCustomCopy> userCustomCopy2(KpiUserAttendanceCustomMapper kpiUserAttendanceCustomMapper) {
        List<KpiUserAttendanceCustomCopy> list = kpiUserAttendanceCustomMapper.getList(
                new QueryWrapper<KpiUserAttendanceCustomCopy>()
                        .eq("a.period", task.getPeriod())
                        .eq("a.del_flag", "0")
                        .eq("a.tenant_id", task.getTenantId())
                        .eq("a.busi_type", "1")
        );
        list.forEach(r -> {
            r.setTaskChildId(task_child.getId());
            r.setCopyDate(new Date());
        });

        return list;
    }


    /**
     * 归集转存
     */
    public List<KpiClusterUnitCopy> clusterUnitCopy(CostClusterUnitMapper costClusterUnitMapper,KpiClusterUnitCopyMapper kpiClusterUnitCopyMapper,
                                                    KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        List<KpiClusterUnitCopy> outputs = new ArrayList<>();
        {
            List<CostClusterUnit> list = costClusterUnitMapper.selectList(
                    new QueryWrapper<CostClusterUnit>()
                            .eq("del_flag", "0")
                            .eq("status", "0")
                            .eq("tenant_id", task.getTenantId())
                            .eq("type", "0")
            );
            List<KpiClusterUnitCopy> list_copy = new ArrayList<>();
            list.forEach(r -> {
                KpiClusterUnitCopy entity = new KpiClusterUnitCopy();
                entity.setId(r.getId())
                        .setName(r.getName())
                        .setUnits(r.getUnits())
                        .setIsFixUnit(r.getIsFixUnit())
                        .setStatus(r.getStatus())
                        .setInitialized(r.getInitialized())
                        .setDel_flag(r.getDelFlag())
                        .setThirdAccountId(r.getThirdAccountId())
                        .setThirdId(r.getThirdId())
                        .setThirdName(r.getThirdName())
                        .setCreateBy(r.getCreateBy())
                        .setCreateTime(Date.from(r.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()))
                        .setUpdateBy(r.getUpdateBy())
                        .setUpdateTime(r.getUpdateTime() == null ? null : Date.from(r.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant()));
                entity.setTaskChildId(task_child.getId());
                entity.setCopyDate(new Date());
                list_copy.add(entity);
            });
            if (!list_copy.isEmpty()) {
                List<List<KpiClusterUnitCopy>> partition = ListUtils.partition(list_copy, 2000);
                partition.parallelStream().forEach(r -> {
                    kpiClusterUnitCopyMapper.insertBatchSomeColumn(r);
                });
            }
            updateLog(TaskStatusEnum.S_21,kpiAccountTaskChildMapper);

            outputs.addAll(list_copy);
        }
        return outputs;
    }

    public List<KpiClusterUnitCopy> clusterUnitCopy2(CostClusterUnitMapper costClusterUnitMapper) {
        List<KpiClusterUnitCopy> outputs = new ArrayList<>();
        List<CostClusterUnit> list = costClusterUnitMapper.selectList(
                new QueryWrapper<CostClusterUnit>()
                        .eq("del_flag", "0")
                        .eq("status", "0")
                        .eq("tenant_id", task.getTenantId())
                        .eq("type", "0")
        );
        List<KpiClusterUnitCopy> list_copy = new ArrayList<>();
        list.forEach(r -> {
            KpiClusterUnitCopy entity = new KpiClusterUnitCopy();
            entity.setId(r.getId())
                    .setName(r.getName())
                    .setUnits(r.getUnits())
                    .setIsFixUnit(r.getIsFixUnit())
                    .setStatus(r.getStatus())
                    .setInitialized(r.getInitialized())
                    .setDel_flag(r.getDelFlag())
                    .setThirdAccountId(r.getThirdAccountId())
                    .setThirdId(r.getThirdId())
                    .setThirdName(r.getThirdName())
                    .setCreateBy(r.getCreateBy())
                    .setCreateTime(Date.from(r.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()))
                    .setUpdateBy(r.getUpdateBy())
                    .setUpdateTime(r.getUpdateTime() == null ? null : Date.from(r.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant()));
            entity.setTaskChildId(task_child.getId());
            entity.setCopyDate(new Date());
            list_copy.add(entity);
        });

        outputs.addAll(list_copy);
        return outputs;
    }

    public List<KpiItemCopy> itemCopy(KpiItemMapper kpiItemMapper,KpiItemCopyMapper kpiItemCopyMapper,KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {

        List<KpiItemCopy> outputs = kpiItemMapper.getList(
                new QueryWrapper<KpiItem>()
                        .eq("tenant_id", task.getTenantId())
                        .eq("busi_type","1")
                //.eq("del_flag", "0")
                //.eq("status", "0")
        );
        outputs.forEach(r -> {
            r.setTaskChildId(task_child.getId());
            r.setCopyDate(new Date());
        });
        if (!outputs.isEmpty()) {
            List<List<KpiItemCopy>> partition = ListUtils.partition(outputs, 2000);
            partition.parallelStream().forEach(r -> {
                kpiItemCopyMapper.insertBatchSomeColumn(r);
            });
        }
        updateLog(TaskStatusEnum.S_22,kpiAccountTaskChildMapper);

        return outputs;
    }

    public List<KpiItemCopy> itemCopy2(KpiItemMapper kpiItemMapper) {
        List<KpiItemCopy> outputs = kpiItemMapper.getList(
                new QueryWrapper<KpiItem>()
                        .eq("tenant_id", task.getTenantId())
                        .eq("busi_type","1")
                //.eq("del_flag", "0")
                //.eq("status", "0")
        );
        outputs.forEach(r -> {
            r.setTaskChildId(task_child.getId());
            r.setCopyDate(new Date());
        });
        return outputs;
    }

    public List<KpiCoefficientCopy> coefficientsCopy(KpiCoefficientMapper kpiCoefficientMapper,KpiCoefficientCopyMapper kpiCoefficientCopyMapper,
                                                     KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        List<KpiCoefficientCopy> outputs = new ArrayList<>();
        List<KpiCoefficient> list = kpiCoefficientMapper.selectList(
                new QueryWrapper<KpiCoefficient>()
                        .eq("tenant_id", task.getTenantId())
        );
        List<KpiCoefficientCopy> list_copy = new ArrayList<>();
        list.forEach(r -> {
            KpiCoefficientCopy entity = new KpiCoefficientCopy();
            entity.setValue(r.getValue())
                    .setDicCode(r.getDicCode())
                    .setDicType(r.getDicType())
                    .setTenantId(r.getTenantId());
            entity.setTaskChildId(task_child.getId());
            list_copy.add(entity);
        });
        if (!list_copy.isEmpty()) {
            List<List<KpiCoefficientCopy>> partition = ListUtils.partition(list_copy, 2000);
            partition.parallelStream().forEach(r -> {
                kpiCoefficientCopyMapper.insertBatchSomeColumn(r);
            });
        }
        updateLog(TaskStatusEnum.S_23,kpiAccountTaskChildMapper);
        outputs.addAll(list_copy);
        return outputs;
    }


    public List<KpiCoefficientCopy> coefficientsCopy2(KpiCoefficientMapper kpiCoefficientMapper) {
        List<KpiCoefficientCopy> outputs = new ArrayList<>();
        List<KpiCoefficient> list = kpiCoefficientMapper.selectList(
                new QueryWrapper<KpiCoefficient>()
                        .eq("tenant_id", task.getTenantId())
        );
        List<KpiCoefficientCopy> list_copy = new ArrayList<>();
        list.forEach(r -> {
            KpiCoefficientCopy entity = new KpiCoefficientCopy();
            entity.setValue(r.getValue())
                    .setDicCode(r.getDicCode())
                    .setDicType(r.getDicType())
                    .setTenantId(r.getTenantId());
            entity.setTaskChildId(task_child.getId());
            list_copy.add(entity);
        });
        outputs.addAll(list_copy);
        return outputs;
    }

    /**
     * 分区校验、创建
     *
     * @param period 周期
     */
    public void part(Long period, KpiCalculateMapper kpiCalculateMapper, NamedParameterJdbcTemplate jdbcTemplate) {
        String part = "p_" + period;
        List<String> kpiCalculatePart = kpiCalculateMapper.findTablePartitionNmae("kpi_calculate");
        if (!kpiCalculatePart.contains(part)) {
            String sql = "alter table kpi_calculate add partition(partition p_" + period + " values in (" + period + "))";
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            jdbcTemplate.update(sql, parameters);
        }

        String part2 = "p_" + period;
        List<String> kpiItemResultPart = kpiCalculateMapper.findTablePartitionNmae("kpi_item_result_copy");
        if (!kpiItemResultPart.contains(part2)) {
            String sql = "alter table kpi_item_result_copy add partition(partition p_" + period + " values in (" + period + "))";
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            jdbcTemplate.update(sql, parameters);
        }

        String part3 = "p_" + period;
        List<String> kpiItemEquivalentPart = kpiCalculateMapper.findTablePartitionNmae("kpi_item_equivalent_copy");
        if (!kpiItemEquivalentPart.contains(part3)) {
            String sql = "alter table kpi_item_equivalent_copy add partition(partition p_" + period + " values in (" + period + "))";
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            jdbcTemplate.update(sql, parameters);
        }
    }

    //更新taskchild日志
    public void updateLog(TaskStatusEnum status,KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        updateLog(status, status.getName(),kpiAccountTaskChildMapper);
    }

    public void updateLog(TaskStatusEnum status, List<String> logs, KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        Date currentTime = new Date();
        // 设置时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 格式化当前时间
        String formattedTime = sdf.format(currentTime);
        task_child.setStatus((long) status.getType());
        kpiAccountTaskChildMapper.updateLog(task_child.getId(),
                String.join("\n", logs) + "\n<" + formattedTime + ">" + "第" + seq + "轮计算完成\n",
                null, status.getType(), status.getName());
    }

    public void updateLog(TaskStatusEnum status, String log,KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        task_child.setStatus((long) status.getType());
        Date currentTime = new Date();
        // 设置时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 格式化当前时间
        String formattedTime = sdf.format(currentTime);

        kpiAccountTaskChildMapper.updateLog(task_child.getId(),
                Objects.equals(log, "") ? "" : ("<" + formattedTime + ">" + log + "\n"),
                null, status.getType(), status.getName());
    }

    //更新taskchild日志
    public void updateErrorLog(TaskStatusEnum status, String errorlog,KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        task_child.setStatus((long) status.getType());
        errorlog = errorlog.length() > 2000 ? errorlog.substring(0, 2000) : errorlog;
        Date currentTime = new Date();
        // 设置时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 格式化当前时间
        String formattedTime = sdf.format(currentTime);

        kpiAccountTaskChildMapper.updateLog(task_child.getId(), null,
                "<" + formattedTime + ">" + errorlog + "\n", status.getType(), status.getName());
    }

    //单指标测试取相关指标合集
    public void caTempCodes(String code) {
        if (tempCodes.contains(code)) {
            return;
        }
        tempCodes.add(code);
        List<KpiIndex> list = Linq.of(kpiIndexsAll).where(t -> t.getCode().equals(code)).toList();
        list.forEach(r -> {
            if (r.getType().equals("3")) {
                List<KpiAllocationRuleCopy> allo = Linq.of(kpiAllocationRules).where(t -> t.getIndexCode().equals(r.getCode())).toList();
                allo.forEach(a -> {
                    if (!StringUtil.isNullOrEmpty(a.getMemberCodes())) {
                        List<String> list1 = Linq.of(JSON.parseArray(a.getMemberCodes(), FormulateMemberDto.class)).where(t ->
                                FormulaParamEnum.P_INDEX.getType().equals(t.getFieldType()) ||
                                        FormulaParamEnum.P_ALLOCATION.getType().equals(t.getFieldType())).select(t -> t.getFieldCode()).toList();
                        list1.forEach(q -> {
                            caTempCodes(q);
                        });
                    }
                });
            } else {
                List<KpiIndexFormulaCopy> allo = Linq.of(kpiIndexFormulas).where(t -> t.getIndexCode().equals(r.getCode())).toList();
                allo.forEach(a -> {
                    if (!StringUtil.isNullOrEmpty(a.getMemberCodes())) {
                        List<String> list1 = Linq.of(JSON.parseArray(a.getMemberCodes(), FormulateMemberDto.class)).where(t ->
                                FormulaParamEnum.P_INDEX.getType().equals(t.getFieldType()) ||
                                        FormulaParamEnum.P_ALLOCATION.getType().equals(t.getFieldType())).select(t -> t.getFieldCode()).toList();
                        list1.forEach(q -> {
                            caTempCodes(q);
                        });
                    }
                });
            }
        });
    }

    //统计计算数量用
    public synchronized void increment() {
        count++;
    }

    //统计计算数量用
    public synchronized int getCount() {
        return count;
    }

    //换取公式适用对象列表，用作计算循环
    public void getAllFormula() {
        //countStatus0 =未计算

        List<KpiIndexFormulaObjCopy> listT = Linq.of(kpiIndexFormulasObj).where(t -> Linq.of(kpiIndexFormulasObj).any(q ->
                q.getIndexCode().equals(t.getIndexCode())
                        && !q.getFormulaId().equals(t.getFormulaId())
                        && t.getPlanObj().equals(q.getPlanObj()))).toList();
        if (!listT.isEmpty()) {
            List<FormulaObjGroup> selected = Linq.of(listT).groupBy(t -> new FormulaObjGroup(t.getIndexCode(), t.getPlanObj()))
                    .select(t -> new FormulaObjGroup(t.getKey().getIndexCode(), t.select(q -> q.getPlanObj()).toList())).toList();
            for (FormulaObjGroup formulaObjGroup : selected) {
                indexErroTips.add("============当前指标存在重复适用对象:[" + field_total.get(formulaObjGroup.getIndexCode()) + "#" + formulaObjGroup.getIndexCode() + "#]===============");
                String err = "";
                for (Long l : formulaObjGroup.getPlanObj()) {
                    SysUser sysUser = Linq.of(users).firstOrDefault(t -> t.getUserId().equals(l));
                    if (sysUser != null) {
                        err += "[" + sysUser.getName() + "]";
                    }
                }
                indexErroTips.add(err);
            }
        }
        Linq.of(kpiIndexs).forEach(index -> {
            if (!StringUtil.isNullOrEmpty(index.getMemberCodes())) {
                logDepError(index);
            }
            KpiCalculateGroupDto kpiCalculateGroupDto = new KpiCalculateGroupDto();
            kpiCalculateGroupDto.setCode(index.getCode());
            kpiCalculateGroupDto.setList(new ArrayList<>());
            calculateGroup.add(kpiCalculateGroupDto);
            if ("3".equals(index.getType())) {//分摊
                List<KpiAllocationRuleCaDto> allRule = Linq.of(kpiAllocationRulesCa).where(f -> f.getIndexCode().equals(index.getCode())).toList();
                allRule.forEach(f -> {
                    KpiIndexCopyExt2 ext = JSON.parseObject(JSON.toJSONString(index), KpiIndexCopyExt2.class);
                    ext.setFormulaAllo(f);
                    ext.setGroupId(f.getId());
                    ext.setFid(UUID.randomUUID().toString());
                    ext.setImpFlag("0");
                    if (!StringUtil.isNullOrEmpty(f.getMemberCodes())) {
                        ext.setDepends(Linq.of(JSON.parseArray(f.getMemberCodes(), FormulateMemberDto.class))
                                .where(t -> FormulaParamEnum.P_INDEX.getType().equals(t.getFieldType())
                                        || FormulaParamEnum.P_ALLOCATION.getType().equals(t.getFieldType())).toList());
                    }
                    index_Allca.add(ext);
                });
            } else {
                List<KpiIndexFormulaCopy> formulas = Linq.of(kpiIndexFormulas).where(f -> f.getIndexCode().equals(index.getCode())).toList();
                formulas.forEach(f -> {
                    //非条件
                    KpiFormulaDto2 fdto = JSON.parseObject(f.getFormula(), KpiFormulaDto2.class);
                    boolean needUserDept;
                    // 判断人口径指标是否要添加科室
                    // todo 待完善
                    if ("1".equals(index.getCaliber()) || "2".equals(index.getCaliber())) {
                        needUserDept = Linq.of(fdto.getFieldList()).any(t ->
                                FormulaParamEnum.P_16.getType().equals(t.getParamType()) ||
                                        FormulaParamEnum.P_17.getType().equals(t.getParamType()) ||
                                        FormulaParamEnum.P_26.getType().equals(t.getParamType()) ||
                                        FormulaParamEnum.P_24.getType().equals(t.getParamType()));
                    } else {
                        needUserDept = false;
                    }
                    if (index.getType().equals("1")) {
                        List<Long> members = Linq.of(kpiIndexFormulasObj).where(r ->
                                r.getIndexCode().equals(f.getIndexCode()) && r.getFormulaId().equals(f.getId())
                        ).select(r -> r.getPlanObj()).toList();

                        boolean depart = false;
                        if (!StringUtil.isNullOrEmpty(f.getMemberCodes())) {
                            List<FormulateMemberDto> list = Linq.of(JSON.parseArray(f.getMemberCodes(), FormulateMemberDto.class))
                                    .where(t -> FormulaParamEnum.P_INDEX.getType().equals(t.getFieldType()) ||
                                            FormulaParamEnum.P_ALLOCATION.getType().equals(t.getFieldType())).toList();
                            if (!list.isEmpty()) {
                                depart = true;
                            }
                        }
                        if (!depart) {
                            KpiIndexCopyExt2 ext = JSON.parseObject(JSON.toJSONString(index), KpiIndexCopyExt2.class);
                            ext.setImpFlag("0");
                            ext.setFormula(f);
                            ext.setFid(UUID.randomUUID().toString());
                            ext.setMembers(members);
                            ext.setGroupId(f.getId());
                            ext.setNeedUserDept(needUserDept);
                            fdto.getFieldList().forEach(t -> {
                                if (FormulaParamEnum.P_13.getType().equals(t.getParamType())) {
                                    ext.setImpFlag("1");
                                }
                            });
                            index_Allca.add(ext);
                        } else {
                            members.forEach(s -> {
                                KpiIndexCopyExt2 ext = JSON.parseObject(JSON.toJSONString(index), KpiIndexCopyExt2.class);
                                ext.setImpFlag("0");
                                ext.setFormula(f);
                                ext.setFid(UUID.randomUUID().toString());
                                ext.getMembers().add(s);
                                ext.setNeedUserDept(needUserDept);
                                ext.setGroupId(f.getId());
                                CalAllDto allDto = new CalAllDto();
                                allDto.setMemberId(s);
                                allDto.setTmp(false);
                                allDto.setParent_impCode(null);
                                allDto.setAlloEmpFlag(false);
                                allDto.setIndex(index);

                                fdto.getFieldList().forEach(t -> {
                                    if (FormulaParamEnum.P_13.getType().equals(t.getParamType())) {
                                        ext.setImpFlag("1");
                                        allDto.setTmp(true);
                                    }
                                    if (FormulaParamEnum.P_INDEX.getType().equals(t.getFieldType()) ||
                                            FormulaParamEnum.P_ALLOCATION.getType().equals(t.getFieldType())) {
                                        allDto.setParam(t);
                                        List<Long> memberList = getMemberList(allDto);
                                        FormulateMemberDto dto1 = Linq.of(ext.getDepends()).firstOrDefault(q -> q.getFieldCode().equals(t.getFieldCode()));
                                        if (dto1 != null) {
                                            dto1.getIds().addAll(memberList);
                                        } else {
                                            FormulateMemberDto dto = new FormulateMemberDto();
                                            dto.setFieldCode(t.getFieldCode());
                                            dto.setFieldType(t.getFieldType());
                                            dto.setIds(memberList);
                                            ext.getDepends().add(dto);
                                        }
                                    }
                                });
                                ext.getDepends().forEach(q -> {
                                    q.setIds(q.getIds().stream().distinct().collect(Collectors.toList()));
                                });
                                index_Allca.add(ext);
                            });
                        }
                    } else if (index.getType().equals("2")) {
                        KpiIndexCopyExt2 ext = JSON.parseObject(JSON.toJSONString(index), KpiIndexCopyExt2.class);
                        ext.setImpFlag("0");
                        ext.setFormula(f);
                        ext.setFid(UUID.randomUUID().toString());
                        ext.setGroupId(f.getId());
                        ext.getMembers().add(Long.parseLong(f.getMemberIds()));
                        fdto.getFieldList().forEach(t -> {
                            if (FormulaParamEnum.P_13.getType().equals(t.getParamType())) {
                                ext.setImpFlag("1");
                            }
                        });
                        index_Allca.add(ext);
                    }
                });
            }
        });
    }

    //获取能计算的公式
    public List<KpiIndexCopyExt2> getCanCaList(Long count) {
        if (count == 0L && Linq.of(index_Allca).any(r -> r.getDepends().isEmpty() && !r.isFinish())) {
            return Linq.of(index_Allca).where(r -> r.getDepends().isEmpty() && !r.isFinish()).toList();
        }
        List<KpiIndexCopyExt2> list = new ArrayList<>();
        index_Allca.parallelStream().forEach(r -> {
            boolean addFlag = true;
            if (!r.isFinish()) {
                if (r.getType().equals("3")) {
                    for (FormulateMemberDto q : r.getDepends()) {
                        if (Linq.of(index_Allca).any(allo -> allo.getCode().equals(q.getFieldCode()) && !allo.isFinish())) {
                            addFlag = false;
                            break;
                        }
                    }
                } else {
                    for (FormulateMemberDto q : r.getDepends()) {
                        //该依赖在指标公式池子中存在没算的公式 而且我需要的适用对象的指标没计算完成则当前公式不能计算
                        if (Linq.of(index_Allca).any(allo -> allo.getCode().equals(q.getFieldCode()))) {
                            //是否存在我需要的没计算的指标公式+适用对象  retrun true是不能算
                            if (Linq.of(index_Allca).any(t -> {
                                if (!t.isFinish()) {
                                    if (FormulaParamEnum.P_ALLOCATION.getType().equals(q.getFieldType())) {
                                        if (t.getCode().equals(q.getFieldCode())) {
                                            return true;
                                        }
                                    } else {
                                        if (t.getCode().equals(q.getFieldCode())) {
                                            for (Long member : t.getMembers()) {
                                                if (q.getIds().contains(member)) {
                                                    return true;
                                                }
                                            }
                                        }
                                    }
                                }
                                return false;
                            })) {
                                addFlag = false;
                                break;
                            }
                        }
                    }
                }
            } else {
                addFlag = false;
            }
            if (addFlag) {
                synchronized (list) {
                    list.add(r);
                }
            }
        });
        return list;
    }

    public void logDepError(KpiIndexCopy r) {
        List<String> logs = new ArrayList<>();
        List<FormulateMemberDto> list = new ArrayList<>();
        if (r.getType().equals("3")) {
            List<KpiAllocationRuleCaDto> allRule = Linq.of(kpiAllocationRulesCa).where(f -> f.getIndexCode().equals(r.getCode())).toList();
            allRule.forEach(f -> {
                KpiFormulaDto2 fdto = new KpiFormulaDto2();
                if (!StringUtil.isNullOrEmpty(f.getFormula())) {
                    fdto = JSON.parseObject(f.getFormula(), KpiFormulaDto2.class);
                    fdto.getFieldList().forEach(v -> {
                        FormulateMemberDto dto = new FormulateMemberDto();
                        dto.setFieldCode(v.getFieldCode());
                        dto.setFieldType(v.getFieldType());
                        list.add(dto);
                    });
                }
            });
        } else {
            List<KpiIndexFormulaCopy> formulas = Linq.of(kpiIndexFormulas).where(f -> f.getIndexCode().equals(r.getCode())).toList();
            formulas.forEach(f -> {
                KpiFormulaDto2 fdto = JSON.parseObject(f.getFormula(), KpiFormulaDto2.class);
                fdto.getFieldList().forEach(v -> {
                    FormulateMemberDto dto = new FormulateMemberDto();
                    dto.setFieldCode(v.getFieldCode());
                    dto.setFieldType(v.getFieldType());
                    list.add(dto);
                });
            });
        }
        List<FormulateMemberDto> collect = list.stream().distinct().collect(Collectors.toList());
        logs.add("============当前指标:[" + r.getName() + "#" + r.getCode() + "#]===============");
        collect.forEach(m -> {
            if (!FormulaParamEnum.P_SYSTEM.getType().equals(m.getFieldType())) {
                String kpiIndexCopy = field_total.get(m.getFieldCode());
                if (!StringUtil.isNullOrEmpty(kpiIndexCopy)) {
                    if (kpiIndexCopy.contains("[已删除]")) {
                        logs.add("[" + kpiIndexCopy + "|" + m.getFieldCode() + "]");
                    } else if (kpiIndexCopy.contains("[已停用]")) {
                        logs.add("[" + kpiIndexCopy + "|" + m.getFieldCode() + "]");
                    }
                } else {
                    logs.add("[" + m.getFieldCode() + "](不存在)");
                }
            }
        });
        if (logs.size() > 1) {
            indexErroTips.addAll(logs);
        }
    }

    //取适用对象
    public List<Long> getMemberList(CalAllDto allDto) {
        List<Long> rt = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(allDto.getParam().getParamType())) {
            FormulaParamEnum formulaParamEnum = FormulaParamEnum.find(allDto.getParam().getParamType());
            switch (formulaParamEnum) {
                case P_16:
                case P_19://本人员归集人员
                case P_10://本人员
                    rt.add(allDto.getMemberId());
                    break;
                case P_18://本人员限定科室
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
                    //rt.add(allDto.getUserDeptId());
                    break;
                case P_25://科室单元人员类型（字典对应user_type）
                    List<KpiAccountUnitCopy> account_rylx = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && Linq.of(allDto.getParam().getParamValues()).select(r -> r.getValue()).toList().contains(t.getAccountUserCode())).toList();
                    rt.addAll(Linq.of(account_rylx).select(r -> r.getId()).toList());
                    break;
                case P_26://本人员所在科室
                    if(allDto.getUserDeptId()!=null)
                    {
                        rt.add(allDto.getUserDeptId());
                    }
                    else {
                        List<Long> deptid = Linq.of(kpiUserAttendances).where(t -> t.getDelFlag().equals("0") && t.getUserId().equals(allDto.getMemberId())
                                && t.getAccountUnit()!=null &&t.getAccountUnit()>0).select(t->t.getAccountUnit()).distinct().toList();
                        rt.addAll(deptid);
                    }
                    break;
                case P_29://所有科室单元
                    rt.addAll(Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")).select(r -> r.getId()).toList());
                    break;
                case P_310:// 310 本摊入人
                    if (allDto.isAlloEmpFlag()) {
                        rt.add(allDto.getMemberId());
                    }
                    break;
                case P_319:// 319 所有摊入人
                    List<String> list = Arrays.asList(allDto.getAlloRule().getInMembersEmp().split(","));
                    for (String s : list) {
                        rt.add(Long.parseLong(s));
                    }
                    break;
                case P_320:// 320 本摊入科室单元
                    if (!allDto.isAlloEmpFlag()) {
                        rt.add(allDto.getMemberId());
                    }
                    break;
                case P_329://所有摊入科室单元
                    List<String> list2 = Arrays.asList(allDto.getAlloRule().getInMembersDept().split(","));
                    for (String s : list2) {
                        rt.add(Long.parseLong(s));
                    }
                    break;
            }
            //口径剔除
            if (!allDto.getParam().getParamExcludes().isEmpty()) {
                List<Long> list = new ArrayList<>();
                allDto.getParam().getParamExcludes().forEach(r -> {
                    if (r.getType() == null
                            || r.getType().equals(FormulaParamEnum.P_11.getType())
                            || r.getType().equals(FormulaParamEnum.P_21.getType())) {
                        list.add(Long.parseLong(r.getValue()));
                    } else {
                        list.addAll(getMemberList(r.getType(), r.getValue()));
                    }
                });
                rt.removeAll(list);
            }
        }
        return rt;
    }

    //取口径剔除对象
    public List<Long> getMemberList(String paramType, String value) {
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
                                    && value.equals(t.getHostCode()))
                            .select(t -> t.getMemberId()).toList();
                    rt.addAll(users_role);
                    break;
                case P_15://按工作性质
                    List<Long> users_job = Linq.of(kpiUserAttendances).where(t ->
                                    value.equals(t.getJobNature()))
                            .select(t -> t.getUserId()).toList();
                    rt.addAll(users_job);
                    break;
                case P_21://自定义科室
                    rt.add(Long.parseLong(value));
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
                case P_25://科室单元人员类型（字典对应user_type）
                    List<KpiAccountUnitCopy> account_rylx = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && value.equals(t.getAccountUserCode())).toList();
                    rt.addAll(Linq.of(account_rylx).select(r -> r.getId()).toList());
                    break;
            }
        }
        return rt;
    }

    //新模拟计算
    public String fakeCaculate2(KpiAccountTaskChildMapper kpiAccountTaskChildMapper) {
        String rt = "";
        updateLog(TaskStatusEnum.S_0, "模拟计算开始",kpiAccountTaskChildMapper);
        Long count = 0L;
        List<KpiIndexCopyExt2> list = getCanCaList(count);
        count++;
        while (!list.isEmpty()) {
            list.forEach(index -> {
                Linq.of(index_Allca).firstOrDefault(r -> r.getFid().equals(index.getFid())).setFinish(true);
            });
            list = getCanCaList(count);
            count++;
        }
        List<KpiIndexCopyExt2> notFinish = Linq.of(index_Allca).where(m -> !m.isFinish()).toList();
        if (!notFinish.isEmpty()) {
            indexErroTips.add("================无法计算的指标公式==================");
            Linq.of(notFinish).groupBy(KpiIndexCopy::getCode).forEach(m -> {
                //记录无法计算的指标和公式
                List<String> members = new ArrayList<>();
                if ("1".equals(m.first().getCaliber())) {
                    m.toList().forEach(t -> {
                        t.getMembers().forEach(q -> {
                            SysUser sysUser = Linq.of(users).firstOrDefault(a -> a.getUserId().equals(q));
                            if (sysUser != null) {
                                members.add(sysUser.getName());
                            }
                        });
                    });
                } else if ("2".equals(m.first().getCaliber())) {
                    m.toList().forEach(t -> {
                        t.getMembers().forEach(q -> {
                            KpiAccountUnitCopy kpiAccountUnitCopy = Linq.of(kpiAccountUnits).firstOrDefault(a -> a.getId().equals(q));
                            if (kpiAccountUnitCopy != null) {
                                members.add(kpiAccountUnitCopy.getName());
                            }
                        });
                    });
                }
                indexErroTips.add("[" + m.first().getName() + "#" + m.first().getCode() + "#" );
                indexErroTips.add((m.first().getFormula() == null ? m.first().getFormulaAllo().getId() : m.first().getFormula().getId() )+ "](" + String.join(",", members) + ")");
            });
        }
        Linq.of(index_Allca).forEach(r -> {
            r.setFinish(false);
        });
        updateLog(TaskStatusEnum.S_0, "模拟计算完成", kpiAccountTaskChildMapper);
        return rt;
    }

    public void delData(Long taskChildId,KpiCalculateMapper kpiCalculateMapper,KpiMemberCopyMapper kpiMemberCopyMapper,KpiItemResultCopyMapper kpiItemResultCopyMapper) {

        int del1 = 1;
        int del2 = 1;
        int del3 = 1;
        while (del1 > 0){
            del1 = kpiCalculateMapper.delete(
                    new QueryWrapper<KpiCalculate>()
                            .eq("task_child_id", taskChildId)
                            .eq("period",task_child.getPeriod())
                            .last("limit 2000")
            );
        }
        while (del2 > 0){
            del2 = kpiMemberCopyMapper.delete(
                    new QueryWrapper<KpiMemberCopy>()
                            .eq("task_child_id", taskChildId)
                            .last("limit 2000")
            );
        }
        while (del3 > 0){
            del3 = kpiItemResultCopyMapper.delete(
                    new QueryWrapper<KpiItemResultCopy>()
                            .eq("task_child_id", taskChildId)
                            .eq("period",task_child.getPeriod())
                            .last("limit 2000")
            );
        }
    }

    //计算任务入口v2
    public void cateCalculate2(String code,KpiAccountTaskChildMapper kpiAccountTaskChildMapper,KpiCalculateMapper kpiCalculateMapper) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //double MDay = getPeriodMDay();
            List<KpiIndexCopyExt2> list = getCanCaList(seq);
            int total = index_Allca.size();
            List<KpiMemberCopy> biannei = Linq.of(kpiMembers).where(x ->
                    Linq.of(kpiCategorys).where(t -> t.getCategoryName().contains("编内")).select(t -> t.getCategoryCode()).toList().contains(x.getHostCode())
                            && MemberEnum.ROLE_EMP.getType().equals(x.getMemberType())).toList();
            List<KpiMemberCopy> bianwai = Linq.of(kpiMembers).where(x ->
                    Linq.of(kpiCategorys).where(t -> t.getCategoryName().contains("编外")).select(t -> t.getCategoryCode()).toList().contains(x.getHostCode())
                            && MemberEnum.ROLE_EMP.getType().equals(x.getMemberType())).toList();
            while (!list.isEmpty()) {
                List<String> logs = new ArrayList<>();
                list.parallelStream().forEach(index -> {
                    try {
                        long start = System.currentTimeMillis();
                        List<KpiCalculate> ca_result = new ArrayList<>();
                        //System.out.println("===================" + index.getName());
                        if ("3".equals(index.getType())) {//分摊
                            KpiAllocationRuleCaDto f = index.getFormulaAllo();
                            KpiFormulaDto2 fdto = new KpiFormulaDto2();
                            if (!StringUtil.isNullOrEmpty(f.getFormula())) {
                                fdto = JSON.parseObject(f.getFormula(), KpiFormulaDto2.class);
                            }

                            CalAllDto allDto = new CalAllDto();
                            allDto.setPlanCode(task.getPlanCode());
                            allDto.setPeriod(task.getPeriod());
                            allDto.setIndex(index);
                            allDto.setFormulaDto(fdto);
                            allDto.setAlloRule(f);

                            List<KpiCalculate> caAllos = caAllo(allDto);//分摊
                            ca_result.addAll(caAllos);
                        } else {
                            //formulas是指标下的指标公式
                            KpiIndexFormulaCopy f = index.getFormula();
                            KpiFormulaDto2 fdto = JSON.parseObject(f.getFormula(), KpiFormulaDto2.class);

                            CalAllDto allDto = new CalAllDto();
                            allDto.setPlanCode(task.getPlanCode());
                            allDto.setPeriod(task.getPeriod());
                            allDto.setIndex(index);
                            allDto.setFormulaDto(fdto);
                            allDto.setTmp("1".equals(index.getImpFlag()));
                            allDto.setImpCode(index.getImpCategoryCode());
                            allDto.setConditions(fdto.getConditionList());
                            allDto.setNeedUserDept(index.isNeedUserDept());

                            if (index.getType().equals("1")) {
                                index.getMembers().forEach(s -> {
                                    allDto.setMemberId(s);
                                    List<KpiCalculate> caNormals = caNormal(allDto);//非条件
                                    ca_result.addAll(caNormals);
                                });
                            } else if (index.getType().equals("2")) {
                                allDto.setMemberId(Long.parseLong(f.getMemberIds()));
                                List<KpiCalculate> caNormals = caNormal(allDto);//条件
                                ca_result.addAll(caNormals);
                            }
                        }
                        //绑定子方案 todo
                        /*Linq.of(kpiAccountPlanChildCopies).where(b -> index.getCode().equals(b.getIndexCode())).toList().forEach(z -> {
                            if ("1".equals(z.getObject())) {
                                for (KpiCalculate calculate : Linq.of(ca_result).where(w -> w.getUserId().equals(z.getUserId())).toList()) {
                                    if (calculate != null) {
                                        calculate.setPlanChildCode(z.getCode());
                                        calculate.setPlanCode(z.getPlanCode());
                                    }
                                }
                            } else {
                                KpiCalculate calculate = Linq.of(ca_result).firstOrDefault(w -> w.getDeptId().equals(z.getDeptId()));
                                if (calculate != null) {
                                    calculate.setPlanChildCode(z.getCode());
                                    calculate.setPlanCode(z.getPlanCode());
                                }
                            }
                        });*/
                        Linq.of(planCodeMemberLists).where(b -> index.getCode().equals(b.getIndexCode())).toList().forEach(z -> {
                            if (!z.getMemberList().isEmpty()) {
                                if ("1".equals(index.getCaliber())) {
                                    for (KpiCalculate calculate : Linq.of(ca_result).where(w ->
                                            ( "0".equals(w.getImputationType())|| "1".equals(w.getImputationType()))
                                                    && z.getMemberList().contains(w.getUserId())).toList()) {
                                        if (calculate != null) {
                                            calculate.setPlanCode(z.getPlanCode());
                                        }
                                    }
                                } else {
                                    KpiCalculate calculate = Linq.of(ca_result).firstOrDefault(w ->
                                            ( "0".equals(w.getImputationType())|| "1".equals(w.getImputationType()))
                                                    && z.getMemberList().contains(w.getDeptId()));
                                    if (calculate != null) {
                                        calculate.setPlanCode(z.getPlanCode());
                                    }
                                }
                            }
                        });

                        for (KpiValueAdjustCopy adjust : Linq.of(kpiValueAdjustCopies).where(x->FormulaParamEnum.P_INDEX.getType().equals(x.getType()))) {
                            KpiCalculate calculate = null;
                            if (adjust.getUserId() != null && adjust.getAccountUnit() != null) {
                                calculate = Linq.of(ca_result).firstOrDefault(x -> !x.getImputationType().equals("2") && adjust.getCode().equals(x.getCode()) && adjust.getUserId().equals(x.getUserId()) && adjust.getAccountUnit().equals(x.getDeptId()));
                            } else if (adjust.getUserId() != null) {
                                calculate = Linq.of(ca_result).firstOrDefault(x -> !x.getImputationType().equals("2") && adjust.getCode().equals(x.getCode()) && adjust.getUserId().equals(x.getUserId()));
                            } else if (adjust.getAccountUnit() != null) {
                                calculate = Linq.of(ca_result).firstOrDefault(x -> !x.getImputationType().equals("2") && adjust.getCode().equals(x.getCode()) && adjust.getAccountUnit().equals(x.getDeptId()));
                            }

                            if (calculate != null) {
                                calculate.setOriginValue(calculate.getValue());
                                calculate.setValue(getValByAdjust(index,calculate.getValue(), adjust));
                                calculate.setAdjustOperation(adjust.getOperation());
                                calculate.setAdjustValue(adjust.getValue());
                            }
                        }

                        ca_result.forEach(q -> {
                            q.setFormulaId(index.getGroupId());
                            q.setSeq(seq);
                            if (q.getUserId() != null) {
                                List<KpiUserAttendanceCopy> cs = Linq.of(kpiUserAttendances).where(t -> q.getUserId().equals(t.getUserId())).toList();
                                if (!cs.isEmpty()) {
                                    q.setUserName(cs.get(0).getEmpName());
                                    q.setEmpId(cs.get(0).getEmpId());
                                    q.setUserType(String.join(",", Linq.of(cs).select(o -> o.getUserType()).distinct().toList()));
                                }
                                KpiMemberCopy kpiMemberCopy = Linq.of(biannei).firstOrDefault(x -> x.getMemberId().equals(q.getUserId()));
                                if (kpiMemberCopy != null) {
                                    q.setEstablish("1");
                                } else {
                                    kpiMemberCopy = Linq.of(bianwai).firstOrDefault(x -> x.getMemberId().equals(q.getUserId()));
                                    if (kpiMemberCopy != null) {
                                        q.setEstablish("0");
                                    }
                                }
                            }
                            if (q.getUserId() != null && q.getDeptId() != null) {
                                q.setUserImp("1");
                            } else {
                                q.setUserImp("0");
                            }
                            if (q.getDeptId() != null) {
                                KpiAccountUnitCopy c = Linq.of(kpiAccountUnits).firstOrDefault(t -> t.getId().equals(q.getDeptId()));
                                if (c != null) {
                                    q.setUserImp("1");
                                    q.setDeptName(c.getName());
                                    SysDictItem s = Linq.of(dict).firstOrDefault(t -> t.getItemValue().equals(c.getCategoryCode()));
                                    if (s != null) {
                                        q.setGroupName(s.getLabel());
                                    }
                                    SysDictItem dict3 = Linq.of(dict).firstOrDefault(d -> d.getItemValue().equals(c.getAccountTypeCode()) && "kpi_unit_calc_type".equals(d.getDictType()));
                                    if (dict3 != null) {
                                        q.setUnitType(dict3.getLabel());
                                    }
                                    SysDictItem dict2 = Linq.of(dict).firstOrDefault(d -> d.getItemValue().equals(c.getAccountUserCode()) && "user_type".equals(d.getDictType()));
                                    if (dict2 != null) {
                                        q.setDeptUserType(dict2.getLabel());
                                    }
                                }
                            }
                            q.setName(field_total.get(q.getCode()));
                        });
                        synchronized (calculateGroup) {
                            KpiCalculateGroupDto calculateGroupDto = Linq.of(calculateGroup).firstOrDefault(cal -> cal.getCode().equals(index.getCode()));
                            calculateGroupDto.getList().addAll(ca_result);
                        }
                        synchronized (index_Allca) {
                            Linq.of(index_Allca).firstOrDefault(r -> r.getFid().equals(index.getFid())).setFinish(true);
                        }
                        long end = System.currentTimeMillis();
                        long timestamp = end - start;
                        increment();
                        Date currentTime = new Date();
                        String formattedTime = sdf.format(currentTime);
                        String log = "<" + formattedTime + ">指标[" + index.getName() + "]计算完成," + getCount() + "/" + total + "," + timestamp + "ms。";
                        synchronized (logs) {
                            logs.add(log);
                        }
                        //updateLog(TaskStatusEnum.S_50, "指标[" + index.getName() + "]计算完成," + getCount() + "/" + total + "," + timestamp + "ms。");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        updateErrorLog(TaskStatusEnum.S_97, getEroLog(ex),kpiAccountTaskChildMapper);
                        throw new RuntimeException(index.getCode() + index.getName() + ",公式id" + index.getGroupId() + "-计算出错:" + ex.getMessage());
                    }
                });
                updateLog(TaskStatusEnum.S_50, logs,kpiAccountTaskChildMapper);
                //不存在没完成的头部指标，测试任务提前结束
                if (!StringUtil.isNullOrEmpty(code) && !Linq.of(index_Allca).any(t -> t.getCode().equals(code) && !t.isFinish())) {
                    updateLog(TaskStatusEnum.S_50, "测试任务提前结束",kpiAccountTaskChildMapper);
                    break;
                }
                seq++;
                list = getCanCaList(seq);
                if(list.size()==0)
                {
                    updateLog(TaskStatusEnum.S_50, "无可继续计算公式",kpiAccountTaskChildMapper);
                }
            }


            List<KpiCalculate> allca = new ArrayList<>();
            calculateGroup.parallelStream().forEach(r -> {
                r.getList().forEach(q -> {
                    if (q.getUserId() != null && q.getDeptId() == null) {
                        List<KpiUserAttendanceCopy> d = Linq.of(kpiUserAttendances).where(t -> t.getUserId().equals(q.getUserId())).toList();
                        if (d != null && d.size() == 1) {
                            q.setDeptId(d.get(0).getAccountUnit());
                            q.setDeptName(d.get(0).getAccountUnitName());
                            q.setGroupName(d.get(0).getAccountGroup());

                            KpiAccountUnitCopy c = Linq.of(kpiAccountUnits).firstOrDefault(t -> t.getId().equals(d.get(0).getAccountUnit()));
                            if (c != null) {
                                SysDictItem dict3 = Linq.of(dict).firstOrDefault(t -> t.getItemValue().equals(c.getAccountTypeCode()) && "kpi_unit_calc_type".equals(t.getDictType()));
                                if (dict3 != null) {
                                    q.setUnitType(dict3.getLabel());
                                }
                            }
                        }
                    }
                });
                synchronized (allca) {
                    allca.addAll(r.getList());
                }
            });
            updateLog(TaskStatusEnum.S_50, "报表生成开始",kpiAccountTaskChildMapper);
            if (StringUtil.isNullOrEmpty(code)){
                List<Long> cafids = Linq.of(allca).select(x -> x.getFormulaId()).distinct().toList();
                List<Long> fids = Linq.of(kpiIndexFormulas).select(x -> x.getId()).toList();

                fids.removeAll(cafids);
                if (!fids.isEmpty()){
                    indexErroTips.add("================无法计算的指标公式id=================");
                    fids.forEach(f->{
                        indexErroTips.add(f.toString());
                    });
                }
            }
            if (!allca.isEmpty()) {
                List<List<KpiCalculate>> partition = ListUtils.partition(allca, 500);
                partition.parallelStream().forEach(r -> {
                    kpiCalculateMapper.insertBatchSomeColumn(r);
                });
            }
            updateLog(TaskStatusEnum.S_50, "报表生成完成",kpiAccountTaskChildMapper);
            updateLog(TaskStatusEnum.S_99,kpiAccountTaskChildMapper);
//            List<KpiIndexCopyExt2> notFinish = Linq.of(index_Allca).where(m -> !m.isFinish()).toList();
//            if (notFinish.isEmpty()) {
//                updateLog(TaskStatusEnum.S_99);
//            } else {
//                updateLog(TaskStatusEnum.S_98);
//            }
        } catch (Exception exception) {
            updateLog(TaskStatusEnum.S_97, exception.getMessage(),kpiAccountTaskChildMapper);
        } finally {
            //清除
            clean();
        }
    }

    @SneakyThrows
    public String getEroLog(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String result = sw.toString(); //异常字符串
        sw.close();
        pw.close();
        return result;
    }

    //拿当前任务周期的自然月天数
    private double getPeriodMDay() {
        String s = task.getPeriod().toString();
        if (s.length() != 6) {
            return 0d;
        }
        int month = Integer.parseInt(s.substring(4, 6));
        int year = Integer.parseInt(s.substring(0, 4));
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1); // 设置为指定年份和月份的第一天 因为Calendar的月份从0开始计数
        //Date time = calendar.getTime();
        //System.out.println(time);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    //条件非条件指标计算
    private List<KpiCalculate> caNormal(CalAllDto allDto) {
        List<KpiCalculate> list = new ArrayList<>();
        if (!allDto.isTmp()) {
            if (allDto.getIndex().getType().equals("2") && Long.valueOf(allDto.getFormulaDto().getMemberList().get(0).getValue()) < 0) {
                // 找到所有公式对象汇总循环 修改子memberid，和主memberid
                List<Long> rs = getResultList(allDto);
                rs.forEach(r -> {
                    String json = JSON.toJSONString(allDto);
                    CalAllDto allDto2 = JSON.parseObject(json, CalAllDto.class);
                    allDto2.setTmp(false);
                    List<KpiFormulaDto2.MemberListDTO> dto = new ArrayList<>();
                    KpiFormulaDto2.MemberListDTO memberListDTO = new KpiFormulaDto2.MemberListDTO();
                    memberListDTO.setValue(r.toString());
                    memberListDTO.setLabel(Linq.of(kpiAccountUnits).where(t -> t.getId().equals(r)).select(t -> t.getName()).firstOrDefault());
                    dto.add(memberListDTO);

                    KpiFormulaDto2.ConditionListDTO conditionListDTO = new KpiFormulaDto2.ConditionListDTO();
                    switch (allDto.getFormulaDto().getMemberList().get(0).getValue()) {
                        case "-100":
                            conditionListDTO.setKey("brks");
                            break;
                        case "-101":
                            conditionListDTO.setKey("zdysks");
                            break;
                        case "-102":
                            conditionListDTO.setKey("kzysks");
                            break;
                        case "-103":
                            conditionListDTO.setKey("brbq");
                            break;
                    }
                    conditionListDTO.setValue(new ArrayList<>());
                    KpiFormulaDto2.MemberListDTO memberListDTO1 = new KpiFormulaDto2.MemberListDTO();
                    memberListDTO1.setLabel(memberListDTO.getLabel());
                    memberListDTO1.setValue(r.toString());
                    conditionListDTO.getValue().add(memberListDTO1);
                    conditionListDTO.setRelation("等于");
                    conditionListDTO.setType("string");
                    conditionListDTO.setName(memberListDTO.getLabel());
                    conditionListDTO.setId(0L);
                    allDto2.getFormulaDto().getConditionList().add(conditionListDTO);
                    allDto2.getConditions().add(conditionListDTO);

                    allDto2.getFormulaDto().setMemberList(dto);
                    list.addAll(caNormal(allDto2));
                });
            }
            else if (allDto.isNeedUserDept()) {
                // 优先以负责科室为主
                List<Long> account_dept = new ArrayList<>();

                if (Linq.of(allDto.getFormulaDto().getFieldList()).any(t ->
                        FormulaParamEnum.P_16.getType().equals(t.getParamType()) ||
                                FormulaParamEnum.P_24.getType().equals(t.getParamType()))) {
                    account_dept = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                                    && t.getResponsiblePersonId() != null && Arrays.asList(t.getResponsiblePersonId().split(",")).contains(allDto.getMemberId().toString()))
                            .select(KpiAccountUnitCopy::getId).toList();
                }
                if (Linq.of(allDto.getFormulaDto().getFieldList()).any(t ->
                        FormulaParamEnum.P_26.getType().equals(t.getParamType()) ||
                                FormulaParamEnum.P_17.getType().equals(t.getParamType()))) {
                    account_dept.addAll(Linq.of(kpiUserAttendances).where(t -> t.getAccountUnit() != null
                                    && t.getAccountUnit() > 0 && t.getUserId().equals(allDto.getMemberId()))
                            .select(KpiUserAttendanceCopy::getAccountUnit).toList());
                }

                //剔除
                List<KpiFormulaDto2.FieldListDTO> alllist = Linq.of(allDto.getFormulaDto().getFieldList()).where(t ->
                        FormulaParamEnum.P_16.getType().equals(t.getParamType()) ||
                                FormulaParamEnum.P_24.getType().equals(t.getParamType()) ||
                                FormulaParamEnum.P_26.getType().equals(t.getParamType()) ||
                                FormulaParamEnum.P_17.getType().equals(t.getParamType())).toList();
                List<Long> li = new ArrayList<>();
                alllist.forEach(x->{
                    x.getParamExcludes().forEach(r -> {
                        li.addAll(getMemberList(r.getType(), r.getValue()));
                    });
                });
                account_dept.removeAll(li);

                // todo 待完善
                allDto.setNeedUserDept(false);
                account_dept.stream().distinct().forEach(t -> {
                    allDto.setUserDeptId(t);
                    List<KpiCalculate> kpiCalculates = caNormal(allDto);
                    kpiCalculates.forEach(r -> {
                        r.setDeptId(t);
                    });
                    list.addAll(kpiCalculates);
                });
                allDto.setNeedUserDept(true);
            }
            else {
                KpiCalculate calculate = new KpiCalculate();
                calculate.setTenantId(task.getTenantId());
                if (!StringUtil.isNullOrEmpty(allDto.getParent_impCode())) {
                    calculate.setImputationCode(allDto.getParent_impCode());
                    calculate.setImputationType("2");
                } else {
                    calculate.setImputationType("0");
                }
                Map<String, Object> map = new HashMap<>();
                //指定科室
                List<KpiFormulaDto2.MemberListDTO> dept_18 = new ArrayList<>();
                for (KpiFormulaDto2.FieldListDTO t : allDto.getFormulaDto().getFieldList()){
                    allDto.setParam(t);
                    if (FormulaParamEnum.P_18.getType().equals(t.getParamType())){
                        dept_18.addAll(t.getParamValues());
                    }
                    //每个变量计算 唯一入口 统一入口
                    BigDecimal bigDecimal = getFiledValue(allDto);
                    if (FormulaParamEnum.P_319.getType().equals(t.getParamType()) ||
                            FormulaParamEnum.P_329.getType().equals(t.getParamType())) {
                        t.setAllImpMembers(getMemberList(allDto));
                    }
                    map.put(t.getCode(), bigDecimal.doubleValue());
                    //jep.addVariable(t.getCode(), bigDecimal.doubleValue());
                    t.setFieldValue(bigDecimal);
                    t.setFieldName(changeField(t).getFieldName());
                }
                //计算 四舍五入
                //JEP jep2 = ExpressionCheckHelper.myKpi(jep, map, allDto.getFormulaDto().getFormulaOrigin());
                BigDecimal result = ExpressionCheckHelper.aviatorCacu(allDto.getFormulaDto().getFormulaOrigin(), map, task.getPeriod(),equitemtprice);
                //RoundingMode rule = getRoundingMode(allDto.getIndex().getCarryRule());
                //BigDecimal result = new BigDecimal(jep2.getValue()).setScale(allDto.getIndex().getReservedDecimal().intValue(), rule);
                // BigDecimal result = new BigDecimal(jep2.getValue());

                calculate.setPeriod(allDto.getPeriod());
                calculate.setTaskChildId(task_child.getId());
                calculate.setCode(allDto.getIndex().getCode());
                calculate.setValue(result);
                calculate.setResultJson(JSON.toJSONString(allDto.getFormulaDto()));
                calculate.setCreatedDate(new Date());
                //多条件取memberlist中的value
                boolean is_tj = allDto.getIndex().getType().equals("2");
                if ("0".equals(calculate.getImputationType()) || "1".equals(calculate.getImputationType())) {
                    if ("1".equals(allDto.getIndex().getCaliber())) {
                        if (is_tj) {
                            calculate.setUserId(Long.valueOf(allDto.getFormulaDto().getMemberList().get(0).getValue()));
                        } else {
                            calculate.setUserId(allDto.getMemberId());
                        }
                        SysUser c = Linq.of(users).firstOrDefault(t -> t.getUserId().equals(calculate.getUserId()));
                        if (c != null) {
                            calculate.setUserName(c.getName());
                            calculate.setEmpId(c.getJobNumber());
                        }
                        dept_18 = Linq.of(dept_18).distinct().toList();
                        if (dept_18!=null && dept_18.size() == 1 && NumberUtil.isNumber(dept_18.get(0).getValue())){
                            calculate.setDeptId(Long.parseLong(dept_18.get(0).getValue()));
                        }
                    } else if ("2".equals(allDto.getIndex().getCaliber())) {
                        if (is_tj) {
                            calculate.setDeptId(Long.valueOf(allDto.getFormulaDto().getMemberList().get(0).getValue()));
                        } else {
                            calculate.setDeptId(allDto.getMemberId());
                        }
                        KpiAccountUnitCopy c = Linq.of(kpiAccountUnits).firstOrDefault(t -> t.getId().equals(calculate.getDeptId()));
                        if (c != null) {
                            calculate.setDeptName(c.getName());
                            SysDictItem s = Linq.of(dict).firstOrDefault(t -> t.getItemValue().equals(c.getCategoryCode()));
                            if (s != null) {
                                calculate.setGroupName(s.getLabel());
                            }
                        }
                    }
                } else if ("2".equals(calculate.getImputationType())) {
                    calculate.setUserId(allDto.getMemberId());
                    SysUser c = Linq.of(users).firstOrDefault(t -> t.getUserId().equals(calculate.getUserId()));
                    if (c != null) {
                        calculate.setUserName(c.getName());
                        calculate.setEmpId(c.getJobNumber());
                    }
                }
                list.add(calculate);
            }
        }
        else {
            //归集根据memberid 获取子id 根据 impCode
            List<Long> imputationDeptEmp = Linq.of(kpiMembers).where(t -> MemberEnum.IMPUTATION_DEPT_EMP.getType().equals(t.getMemberType())
                    && t.getHostCode().equals(allDto.getIndex().getImpCategoryCode())
                    && t.getHostId().equals(allDto.getMemberId())).select(t -> t.getMemberId()).toList();
            KpiCalculate calculate = new KpiCalculate();
            calculate.setTenantId(task.getTenantId());
            calculate.setImputationType("1");
            String imp_code = SnowflakeGenerator.ID() + "";
            calculate.setImputationCode(imp_code);
            allDto.setImpDeptId(allDto.getMemberId());
            for (Long l : imputationDeptEmp) {
                if (l == null) {
                    continue;
                }
                CalAllDto allDto2 = new CalAllDto();
                BeanUtil.copyProperties(allDto, allDto2);
                allDto2.getFormulaDto().getFieldList().forEach(z -> {
                    if (FormulaParamEnum.P_13.getType().equals(z.getParamType())) {
                        z.setParamType(FormulaParamEnum.P_19.getType());
                        z.setParamCate("1");
                    }
                });
                allDto2.setTmp(false);
                allDto2.setParent_impCode(imp_code);
                allDto2.setMemberId(l);
                List<KpiCalculate> kpiCalculates1 = caNormal(allDto2);
                for (KpiCalculate kpiCalculate : kpiCalculates1) {
                    kpiCalculate.setDeptId(allDto.getImpDeptId());
                }
                list.addAll(kpiCalculates1);

            }
            calculate.setPeriod(allDto.getPeriod());
            calculate.setTaskChildId(task_child.getId());
            calculate.setCode(allDto.getIndex().getCode());
            calculate.setValue(Linq.of(list).select(r -> r.getValue()).sumDecimal());
            calculate.setImputationCode(imp_code);
            calculate.setResultJson(JSON.toJSONString(allDto.getFormulaDto()));
            calculate.setCreatedDate(new Date());
            //多条件取memberlist中的value
            boolean is_tj = allDto.getIndex().getType().equals("2");
            if ("0".equals(calculate.getImputationType()) || "1".equals(calculate.getImputationType())) {
                if ("1".equals(allDto.getIndex().getCaliber())) {
                    if (is_tj) {
                        calculate.setUserId(Long.valueOf(allDto.getFormulaDto().getMemberList().get(0).getValue()));
                    } else {
                        calculate.setUserId(allDto.getMemberId());
                    }
                    SysUser c = Linq.of(users).firstOrDefault(t -> t.getUserId().equals(calculate.getUserId()));
                    if (c != null) {
                        calculate.setUserName(c.getName());
                        calculate.setEmpId(c.getJobNumber());
                    }
                } else if ("2".equals(allDto.getIndex().getCaliber())) {
                    if (is_tj) {
                        calculate.setDeptId(Long.valueOf(allDto.getFormulaDto().getMemberList().get(0).getValue()));
                    } else {
                        calculate.setDeptId(allDto.getMemberId());
                    }
                    KpiAccountUnitCopy c = Linq.of(kpiAccountUnits).firstOrDefault(t -> t.getId().equals(calculate.getDeptId()));
                    if (c != null) {
                        calculate.setDeptName(c.getName());
                        SysDictItem s = Linq.of(dict).firstOrDefault(t -> t.getItemValue().equals(c.getCategoryCode()));
                        if (s != null) {
                            calculate.setGroupName(s.getLabel());
                        }
                    }
                }
            } else if ("2".equals(calculate.getImputationType())) {
                calculate.setUserId(allDto.getMemberId());
                SysUser c = Linq.of(users).firstOrDefault(t -> t.getUserId().equals(calculate.getUserId()));
                if (c != null) {
                    calculate.setUserName(c.getName());
                    calculate.setEmpId(c.getJobNumber());
                }
            }

            list.add(calculate);
        }
        return list;
    }

    //分摊计算
    private List<KpiCalculate> caAllo(CalAllDto allDto) {
        List<KpiCalculate> calculate = new ArrayList<>();
        //分摊类型 1全院分摊 2医护分摊 3病区分摊或借床分摊 4门诊共用分摊
        switch (allDto.getAlloRule().getType()) {
            case "1":
                //全院分摊
                calculate = qyAlloCal(allDto);
                break;
            case "2":
                //医护分摊
                calculate = yhAlloCal(allDto);
                break;
            case "3":
                //借床或病区分摊
                calculate = jchbqAlloCal(allDto);
                break;
            case "4":
                //门诊共用
                switch (allDto.getAlloRule().getRule()) {
                    case "1":
                        //按收入
                        calculate = mzInAlloCal(allDto);
                        break;
                    case "2":
                        //平均分摊
                        calculate = mzAvgAlloCal(allDto);
                        break;
                    case "3":
                        break;
                }

                break;
        }
        return calculate;
    }

    //借床或病区分摊
    private List<KpiCalculate> jchbqAlloCal(CalAllDto allDto) {
        KpiAllocationRuleCaDto r = allDto.getAlloRule();

        allDto.setTmp(false);
        allDto.setParent_impCode(null);

        List<String> allos = new ArrayList<>();
        List<String> items = Linq.of(kpiItems).where(i -> r.getAllocationItems_list().contains(i.getCode())).select(i -> i.getItemName()).distinct().toList();
        List<String> indexs = Linq.of(kpiIndexs).where(i -> r.getAllocationIndexs_list().contains(i.getCode())).select(i -> i.getName()).distinct().toList();
        allos.addAll(items);
        allos.addAll(indexs);

        List<KpiCalculate> rt = new ArrayList<>();
        for (Long s : r.getOutMembersDept_list()) {
            //account_unit_relation
            List<KpiItemResultCopy> results = new ArrayList<>();
            List<KpiItemResultCopyGroupDto> list = Linq.of(itemResultGroup).where(t -> r.getAllocationItems_list().contains(t.getCode())).toList();
            list.forEach(a -> results.addAll(Linq.of(a.getList()).where(m -> s.equals(m.getDeptId())).toList()));

            BigDecimal sum = Linq.of(results).select(z -> z.getValue()).sumDecimal();

            List<KpiItemResultCopy> total_wards = Linq.of(kpiItemResults)
                    .where(i -> allDto.getFormulaDto().getFieldList().get(0).getFieldCode().equals(i.getCode())
                            && s.equals(i.getWard())).toList();


            BigDecimal ward = Linq.of(total_wards).select(z -> z.getValue()).sumDecimal();
            KpiAccountUnitCopy unit = Linq.of(kpiAccountUnits).firstOrDefault(u -> u.getId().equals(s));

            for (Long deptId : Linq.of(total_wards).select(a -> a.getDeptId()).distinct().toList()) {
                BigDecimal dept = Linq.of(total_wards)
                        .where(i -> deptId.equals(i.getDeptId())).select(z -> z.getValue()).sumDecimal();

                String type = "5";//借床
                if (Linq.of(kpiMembers).any(m -> m.getMemberType().equals(MemberEnum.ACCOUNT_UNIT_RELATION.getType())
                        && m.getHostCode().equals(allDto.getAlloRule().getDocCode()) && m.getHostId().equals(deptId)
                        && m.getMemberId().equals(s))) {
                    type = "3";//病区
                }
                allDto.setMemberId(deptId);
                allDto.setAlloEmpFlag(false);
                BigDecimal bigDecimal = Objects.equals(ward, BigDecimal.ZERO) ? BigDecimal.ZERO : dept.divide(ward, 10, RoundingMode.HALF_UP);

                //sum + "*" + dept + "/" + ward
                allDto.getFormulaDto().getFieldList().forEach(t -> {
                    if (t.getCode().contains("_1")) {
                        t.setFieldValue(dept);
                        t.setFieldName(changeField(t).getFieldName());
                    } else if (t.getCode().contains("_2")) {
                        t.setFieldValue(ward);
                        t.setFieldName(changeField(t).getFieldName());
                    }
                });

                KpiCalculate calculate = getCa(null, type, String.join(",", allos), unit == null ? null : unit.getName(), allDto,
                        sum.multiply(bigDecimal), bigDecimal, sum);
                rt.add(calculate);
            }
        }
        return rt;
    }

    //门诊共用  按收入
    private List<KpiCalculate> mzInAlloCal(CalAllDto allDto) {
        KpiAllocationRuleCaDto r = allDto.getAlloRule();
        List<KpiCalculate> rt = new ArrayList<>();
        List<KpiCalculate> list;

        allDto.setTmp(false);
        allDto.setParent_impCode(null);

        List<String> outs = new ArrayList<>();
        List<String> unit = Linq.of(kpiAccountUnits).where(u -> r.getOutMembersDept_list().contains(u.getId())).select(u -> u.getName()).distinct().toList();
        List<String> user = Linq.of(users).where(u -> r.getOutMembersEmp_list().contains(u.getUserId())).select(u -> u.getName()).distinct().toList();
        List<String> imp = Linq.of(kpiClusterUnitCopyList).where(u -> r.getOutMembersImp_list().contains(u.getId())).select(u -> u.getName()).distinct().toList();
        outs.addAll(unit);
        outs.addAll(user);
        outs.addAll(imp);

        List<String> allos = new ArrayList<>();
        List<String> items = Linq.of(kpiItems).where(i -> r.getAllocationItems_list().contains(i.getCode())).select(i -> i.getItemName()).distinct().toList();
        List<String> indexs = Linq.of(kpiIndexs).where(i -> r.getAllocationIndexs_list().contains(i.getCode())).select(i -> i.getName()).distinct().toList();
        allos.addAll(items);
        allos.addAll(indexs);

        //门诊共用-按收入计算-固定摊入核算单元（四大金刚）
        if (r.getOutMembersImp_list().isEmpty()) {
            BigDecimal itemAll = Linq.of(itemResultGroup)
                    .where(t -> r.getAllocationItems_list().contains(t.getCode())).select(t -> {
                        List<KpiItemResultCopy> list1 = t.getList();
                        return Linq.of(list1).where(x -> t.getCaliber().equals("4") || (r.getOutMembersDept_list().contains(x.getDeptId()) || r.getOutMembersEmp_list().contains(x.getUserId()))).select(x -> x.getValue()).sumDecimal();
                    }).sumDecimal();
            BigDecimal indexAll = BigDecimal.ZERO;
            synchronized (calculateGroup) {
                indexAll = Linq.of(calculateGroup)
                        .where(t -> r.getAllocationIndexs_list().contains(t.getCode())).select(t -> {
                            List<KpiCalculate> list1 = t.getList();
                            return Linq.of(list1).where(rr -> !"2".equals(rr.getImputationType())).where(x -> (r.getOutMembersDept_list().contains(x.getDeptId()) || r.getOutMembersEmp_list().contains(x.getUserId()))).select(x -> x.getValue()).sumDecimal();
                        }).sumDecimal();
            }
            BigDecimal all = itemAll.add(indexAll);

            for (Long s : r.getInMembersDept_list()) {
                allDto.setMemberId(s);
                allDto.setAlloEmpFlag(false);
                list = caNormal(allDto);
                BigDecimal bigDecimal = Linq.of(list).select(l -> l.getValue()).sumDecimal();
                //all + "*" + bigDecimal
                KpiCalculate calculate = getCa(list.get(0), r.getType(), String.join(",", allos), String.join(",", outs.stream().distinct().collect(Collectors.toList())), allDto,
                        bigDecimal.multiply(all), bigDecimal, all);
                rt.add(calculate);

            }
            for (Long s : r.getInMembersEmp_list()) {
                allDto.setMemberId(s);
                allDto.setAlloEmpFlag(true);
                list = caNormal(allDto);
                BigDecimal bigDecimal = Linq.of(list).select(l -> l.getValue()).sumDecimal();

                KpiCalculate calculate = getCa(list.get(0), r.getType(), String.join(",", allos), String.join(",", outs.stream().distinct().collect(Collectors.toList())), allDto,
                        bigDecimal.multiply(all), bigDecimal, all);
                rt.add(calculate);

            }
        }
        //6.门诊共用-按收入计算-非固定摊入核算单元
        else {
            List<Long> depts = new ArrayList<>();
            List<Long> emps = new ArrayList<>();
            BigDecimal impSum = Linq.of(itemResultGroup)
                    .where(t -> r.getAllocationItems_list().contains(t.getCode())).select(t -> {
                        List<KpiItemResultCopy> list1 = t.getList();
                        return Linq.of(list1).where(x -> t.getCaliber().equals("4") || (r.getOutMembersImp_list().contains(x.getImputationDeptId()))).select(x -> x.getValue()).sumDecimal();
                    }).sumDecimal();

            Linq.of(kpiClusterUnitCopyList).where(q -> r.getOutMembersImp_list().contains(q.getId())).forEach(m -> {
                if (m.getIsFixUnit().equals("1")) {
                    //固定
                    JSONArray ja = JSON.parseArray(m.getUnits());
                    for (int i = 0; i < ja.size(); i++) {
                        String id = ja.getJSONObject(i).getString("id");
                        depts.add(Long.parseLong(id));
                    }
                } else {
                    Linq.of(allDto.getFormulaDto().getFieldList()).forEach(q -> {
                        //非固定
                        if (q.getParamCate().equals("1")) {
                            Linq.of(itemResultGroup).where(i -> q.getFieldCode().equals(i.getCode())).forEach(i -> {
                                emps.addAll(Linq.of(i.getList()).select(l -> l.getUserId()).toList());
                            });
                            synchronized (calculateGroup) {
                                Linq.of(calculateGroup).where(i -> q.getFieldCode().equals(i.getCode())).forEach(i -> {
                                    emps.addAll(Linq.of(i.getList()).select(l -> l.getUserId()).toList());
                                });
                            }
                        } else if (q.getParamCate().equals("2")) {
                            Linq.of(itemResultGroup).where(i -> q.getFieldCode().equals(i.getCode())).forEach(i -> {
                                depts.addAll(Linq.of(i.getList()).select(l -> l.getDeptId()).toList());
                            });
                            synchronized (calculateGroup) {
                                Linq.of(calculateGroup).where(i -> q.getFieldCode().equals(i.getCode())).forEach(i -> {
                                    depts.addAll(Linq.of(i.getList()).select(l -> l.getDeptId()).toList());
                                });
                            }
                        }
                    });
                }
            });

            List<String> members_dept = Linq.of(depts).where(q -> q != null).select(q -> q.toString()).distinct().toList();
            List<String> members_emp = Linq.of(emps).where(q -> q != null).select(q -> q.toString()).distinct().toList();
            if (members_emp.isEmpty() && members_dept.isEmpty()) {
                return rt;
            }
            allDto.getAlloRule().setInMembersDept(String.join(",", members_dept));
            allDto.getAlloRule().setInMembersEmp(String.join(",", members_emp));
            List<Long> all_members = new ArrayList<>();
            all_members.addAll(depts);
            all_members.addAll(emps);
            all_members = all_members.stream().distinct().collect(Collectors.toList());
            for (int i = 0; i < all_members.size(); i++) {
                allDto.setMemberId(all_members.get(i));
                allDto.setAlloEmpFlag(false);
                list = caNormal(allDto);
                BigDecimal bigDecimal = Linq.of(list).select(l -> l.getValue()).sumDecimal();

                // bigDecimal.multiply(impSum)

                KpiCalculate calculate = getCa(list.get(0), r.getType(), String.join(",", allos), String.join(",", outs.stream().distinct().collect(Collectors.toList())),
                        allDto, bigDecimal.multiply(impSum), bigDecimal, impSum);
                rt.add(calculate);
            }
        }
        return rt;
    }

    //门诊共用 平均分摊
    private List<KpiCalculate> mzAvgAlloCal(CalAllDto allDto) {
        KpiAllocationRuleCaDto r = allDto.getAlloRule();
        List<KpiCalculate> rt = new ArrayList<>();
        List<KpiCalculate> list = new ArrayList<>();
        List<String> allos = new ArrayList<>();
        List<String> outs = new ArrayList<>();

        List<String> items = Linq.of(kpiItems).where(i -> r.getAllocationItems_list().contains(i.getCode())).select(i -> i.getItemName()).distinct().toList();
        List<String> indexs = Linq.of(kpiIndexs).where(i -> r.getAllocationIndexs_list().contains(i.getCode())).select(i -> i.getName()).distinct().toList();
        allos.addAll(items);
        allos.addAll(indexs);

        List<String> unit = Linq.of(kpiAccountUnits).where(u -> r.getOutMembersDept_list().contains(u.getId())).select(u -> u.getName()).distinct().toList();
        List<String> user = Linq.of(users).where(u -> r.getOutMembersEmp_list().contains(u.getUserId())).select(u -> u.getName()).distinct().toList();
        List<String> imp = Linq.of(kpiClusterUnitCopyList).where(u -> r.getOutMembersImp_list().contains(u.getId())).select(u -> u.getName()).distinct().toList();
        outs.addAll(unit);
        outs.addAll(user);
        outs.addAll(imp);

        //摊出非归集
        if (StringUtil.isNullOrEmpty(allDto.getAlloRule().getOutMembersImp())) {
            allDto.setTmp(false);
            allDto.setParent_impCode(null);
            allDto.setAlloEmpFlag(false);
            KpiFormulaDto2.FieldListDTO dto = new KpiFormulaDto2.FieldListDTO();
            dto.setParamType("329");
            allDto.setParam(dto);
            List<Long> memberList = getMemberList(allDto);

            allDto.setAlloEmpFlag(true);
            dto.setParamType("319");
            allDto.setParam(dto);
            List<Long> memberList2 = getMemberList(allDto);
            if (memberList.isEmpty() && memberList2.isEmpty()) {
                return rt;
            }

            BigDecimal itemAll = Linq.of(itemResultGroup)
                    .where(t -> r.getAllocationItems_list().contains(t.getCode())).select(t -> {
                        List<KpiItemResultCopy> list1 = t.getList();
                        return Linq.of(list1).where(x -> t.getCaliber().equals("4") || (memberList.contains(x.getDeptId()) || memberList2.contains(x.getUserId()))).select(x -> x.getValue()).sumDecimal();
                    }).sumDecimal();
            BigDecimal indexAll = BigDecimal.ZERO;
            synchronized (calculateGroup) {
                indexAll = Linq.of(calculateGroup)
                        .where(t -> r.getAllocationItems_list().contains(t.getCode())).select(t -> {
                            List<KpiCalculate> list1 = t.getList();
                            return Linq.of(list1).where(rr -> !"2".equals(rr.getImputationType())).where(x -> (memberList.contains(x.getDeptId()) || memberList2.contains(x.getUserId()))).select(x -> x.getValue()).sumDecimal();
                        }).sumDecimal();
            }
            BigDecimal all = itemAll.add(indexAll);

            int length = r.getInMembersEmp_list().size() + r.getInMembersDept_list().size();
            BigDecimal value = all.divide(new BigDecimal(length), 10, BigDecimal.ROUND_HALF_UP);

            for (Long s : r.getInMembersEmp_list()) {
                allDto.setMemberId(s);
                allDto.setAlloEmpFlag(true);

                KpiCalculate calculate = getCa(null, r.getType(), String.join(",", allos), String.join(",", outs.stream().distinct().collect(Collectors.toList())),
                        allDto, value, new BigDecimal(1.0 / length), all);

                list.add(calculate);
            }
            for (Long s : r.getInMembersDept_list()) {
                allDto.setMemberId(s);
                allDto.setAlloEmpFlag(false);

                KpiCalculate calculate = getCa(null, r.getType(), String.join(",", allos), String.join(",", outs.stream().distinct().collect(Collectors.toList())),
                        allDto, value, new BigDecimal(1.0 / length), all);

                list.add(calculate);
            }
            rt.addAll(list);
        } else {
            List<Long> cluster = new ArrayList<>();
            List<Long> memberList = new ArrayList<>();
            for (String s : allDto.getAlloRule().getOutMembersImp().split(",")) {
                cluster.add(Long.parseLong(s));
            }
            Linq.of(kpiClusterUnitCopyList).where(y -> cluster.contains(y.getId())).forEach(z -> {
                if(z.getUnits()!=null) {
                    for (ClusterDto o : JSON.parseArray(z.getUnits(), ClusterDto.class)) {
                        memberList.add(o.getId());
                    }
                }
            });

            List<Long> collect = memberList.stream().distinct().collect(Collectors.toList());
            BigDecimal itemAll = Linq.of(itemResultGroup)
                    .where(t -> r.getAllocationItems_list().contains(t.getCode())).select(t -> {
                        List<KpiItemResultCopy> list1 = t.getList();
                        return Linq.of(list1).where(x -> t.getCaliber().equals("4") || (r.getOutMembersImp_list().contains(x.getImputationDeptId()))).select(x -> x.getValue()).sumDecimal();
                    }).sumDecimal();
            int length = collect.size();
            BigDecimal value = length == 0 ? BigDecimal.ZERO : itemAll.divide(new BigDecimal(length), 10, BigDecimal.ROUND_HALF_UP);

            collect.forEach(s -> {
                allDto.setMemberId(s);
                allDto.setAlloEmpFlag(false);

                KpiCalculate calculate = getCa(null, r.getType(), String.join(",", allos), String.join(",", outs.stream().distinct().collect(Collectors.toList())),
                        allDto, value, new BigDecimal(1.0 / length), itemAll);
                list.add(calculate);
            });
            rt.addAll(list);
        }

        return rt;
    }

    //全院分摊
    private List<KpiCalculate> qyAlloCal(CalAllDto allDto) {
        KpiAllocationRuleCaDto r = allDto.getAlloRule();
        List<KpiCalculate> rt = new ArrayList<>();

        List<String> allos = new ArrayList<>();
        List<String> items = Linq.of(kpiItems).where(i -> r.getAllocationItems_list().contains(i.getCode())).select(i -> i.getItemName()).distinct().toList();
        List<String> indexs = Linq.of(kpiIndexs).where(i -> r.getAllocationIndexs_list().contains(i.getCode())).select(i -> i.getName()).distinct().toList();
        allos.addAll(items);
        allos.addAll(indexs);

        switch (r.getRule()) {
            case "1":
                allDto.setTmp(false);
                allDto.setParent_impCode(null);
                List<KpiCalculate> list;

                BigDecimal itemAll = Linq.of(itemResultGroup).where(t -> r.getAllocationItems_list().contains(t.getCode())).select(t -> {
                    List<KpiItemResultCopy> list1 = t.getList();
                    return Linq.of(list1).select(rr -> rr.getValue()).sumDecimal();
                }).sumDecimal();
                BigDecimal indexAll = BigDecimal.ZERO;
                synchronized (calculateGroup) {
                    indexAll = Linq.of(calculateGroup).where(t -> r.getAllocationIndexs_list().contains(t.getCode())).select(t -> {
                        List<KpiCalculate> list1 = t.getList();
                        return Linq.of(list1).where(rr -> !"2".equals(rr.getImputationType())).select(rr -> rr.getValue()).sumDecimal();
                    }).sumDecimal();
                }
                BigDecimal sum = itemAll.add(indexAll);

                for (Long s : r.getInMembersDept_list()) {
                    allDto.setMemberId(s);
                    allDto.setAlloEmpFlag(false);
                    list = caNormal(allDto);
                    BigDecimal bigDecimal = Linq.of(list).select(q -> q.getValue()).sumDecimal();
                    //sum + "*" + bigDecimal
                    KpiCalculate calculate = getCa(list.get(0), r.getType(), String.join(",", allos), "全院", allDto,
                            bigDecimal.multiply(sum), bigDecimal, sum);
                    rt.add(calculate);
                }
                for (Long s : r.getInMembersEmp_list()) {
                    allDto.setMemberId(s);
                    allDto.setAlloEmpFlag(true);
                    list = caNormal(allDto);
                    BigDecimal bigDecimal = Linq.of(list).select(q -> q.getValue()).sumDecimal();
                    //sum + "*" + bigDecimal
                    KpiCalculate calculate = getCa(list.get(0), r.getType(), String.join(",", allos), "全院", allDto,
                            bigDecimal.multiply(sum), bigDecimal, sum);
                    rt.add(calculate);
                }
                break;
        }

        return rt;
    }

    //医护分摊
    private List<KpiCalculate> yhAlloCal(CalAllDto allDto) {
        KpiAllocationRuleCaDto r = allDto.getAlloRule();
        List<KpiCalculate> rt = new ArrayList<>();

        allDto.setTmp(false);
        allDto.setParent_impCode(null);
        allDto.setAlloEmpFlag(false);
        List<KpiCalculate> list = new ArrayList<>();
        KpiFormulaDto2.FieldListDTO dto = new KpiFormulaDto2.FieldListDTO();
        dto.setParamType("329");
        allDto.setParam(dto);

        List<String> allos = new ArrayList<>();
        List<String> items = Linq.of(kpiItems).where(i -> r.getAllocationItems_list().contains(i.getCode())).select(i -> i.getItemName()).distinct().toList();
        List<String> indexs = Linq.of(kpiIndexs).where(i -> r.getAllocationIndexs_list().contains(i.getCode())).select(i -> i.getName()).distinct().toList();
        allos.addAll(items);
        allos.addAll(indexs);

        for (Long s : r.getOutMembersDept_list()) {
            BigDecimal itemAll = Linq.of(itemResultGroup).where(t -> r.getAllocationItems_list().contains(t.getCode())).select(t -> {
                List<KpiItemResultCopy> list1 = t.getList();
                return Linq.of(list1).where(w -> t.getCaliber().equals("4") || s.equals(w.getDeptId())).select(rr -> rr.getValue()).sumDecimal();
            }).sumDecimal();
            BigDecimal indexAll = BigDecimal.ZERO;
            synchronized (calculateGroup) {
                indexAll = Linq.of(calculateGroup).where(t -> r.getAllocationIndexs_list().contains(t.getCode())).select(t -> {
                    List<KpiCalculate> list1 = t.getList();
                    return Linq.of(list1).where(rr -> !"2".equals(rr.getImputationType())).where(w -> s.equals(w.getDeptId())).select(rr -> rr.getValue()).sumDecimal();
                }).sumDecimal();
            }
            BigDecimal bigDecimal = itemAll.add(indexAll);
            List<KpiMemberCopy> list1 = Linq.of(kpiMembers).where(m -> m.getMemberType().equals(MemberEnum.ACCOUNT_UNIT_RELATION.getType()) &&
                    m.getHostCode().equals(allDto.getAlloRule().getDocCode()) && m.getHostId().equals(s)).toList();
            for (KpiMemberCopy kpiMemberCopy : list1) {
                allDto.setMemberId(kpiMemberCopy.getMemberId());
                allDto.setAlloEmpFlag(false);
                KpiAccountUnitCopy unit = Linq.of(kpiAccountUnits).firstOrDefault(u -> u.getId().equals(s));
                KpiCalculate calculate = getCa(null, r.getType(), String.join(",", allos), unit == null ? null : unit.getName(), allDto,
                        bigDecimal.multiply(r.getRatio()).divide(new BigDecimal(100)), r.getRatio().divide(new BigDecimal(100)), bigDecimal);
                list.add(calculate);
            }
        }
        rt.addAll(list);

        return rt;
    }

    public BigDecimal getValByAdjust(KpiIndexCopyExt2 index,BigDecimal value, KpiValueAdjustCopy adjust) {
        switch (adjust.getOperation()) {
            case "+":
                value = value.add(adjust.getValue());
                break;
            case "-":
                value = value.subtract(adjust.getValue());
                break;
            case "*":
                value = value.multiply(adjust.getValue());
                break;
            case "/":
                value = value.divide(adjust.getValue());
                break;
            case "=":
                value = adjust.getValue();
                break;
        }
        BigDecimal bigDecimal = value.setScale(index.getReservedDecimal(), getRoundingMode(index.getCarryRule()));
        return bigDecimal;
    }

    //计算每个小项结果
    @SneakyThrows
    private BigDecimal getFiledValue(CalAllDto allDto) {
        //适用对象
        List<Long> memberList = getMemberList(allDto);
        //核算项
        if (allDto.getParam().getFieldType().equals(FormulaParamEnum.P_ITEM.getType())) {
            KpiItemResultCopyGroupDto first = Linq.of(itemResultGroup).firstOrDefault(t -> t.getCode().equals(allDto.getParam().getFieldCode()));
            if (first == null) {
                return BigDecimal.ZERO;
            }
            List<KpiItemResultCopy> list = first.getList();
            if ("4".equals(allDto.getParam().getParamCate()) && !"2".equals(allDto.getIndex().getType())) {
                return Linq.of(list).sumDecimal(t -> t.getValue());
            }
            List<KpiItemResultCopy> newList = new ArrayList<>();
            Map<Field, FieldRelationDto<Object>> fieldMap = new HashMap<>();
            Map<Field, FieldRelationDto<Object>> fieldMap2 = new HashMap<>();
            //多条件
            if (allDto.getConditions() != null) {
                allDto.getConditions().forEach(t -> {
                    try {
                        Field key = KpiItemResultCopy.class.getDeclaredField(toCamelCase(t.getKey()));
                        Object value = null;
                        boolean relation = "等于".equals(t.getRelation().trim());
                        if (t.getType().equals("string")) {
                            if (KpiItemResultCopy.class.getDeclaredField(toCamelCase(t.getKey())).getType().equals(Long.class)) {
                                value = Linq.of(t.getValue()).select(q -> Long.parseLong(q.getValue())).toList();
                            } else {
                                value = Linq.of(t.getValue()).select(q -> q.getValue()).toList();
                            }
                        } else {
                            value = KpiItemResultCopy.class.getDeclaredField(toCamelCase(t.getValue().get(0).getValue()));
                        }
                        fieldMap.put(key, new FieldRelationDto(relation, value));

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } else if (!memberList.isEmpty()) {
                if ("1".equals(allDto.getIndex().getCaliber()) && FormulaParamEnum.P_16.getType().equals(allDto.getParam().getParamType())) {
                    fieldMap.put(KpiItemResultCopy.class.getDeclaredField("userId"), new FieldRelationDto(allDto.getMemberId()));
                    fieldMap.put(KpiItemResultCopy.class.getDeclaredField("deptId"), new FieldRelationDto(allDto.getUserDeptId()));
                }
                else if (FormulaParamEnum.P_19.getType().equals(allDto.getParam().getParamType())) {
                    fieldMap.put(KpiItemResultCopy.class.getDeclaredField("userId"), new FieldRelationDto(allDto.getMemberId()));
                    fieldMap.put(KpiItemResultCopy.class.getDeclaredField("deptId"), new FieldRelationDto(allDto.getImpDeptId()));
                    fieldMap.put(KpiItemResultCopy.class.getDeclaredField("mateFlag"), new FieldRelationDto("1"));
                    fieldMap2.put(KpiItemResultCopy.class.getDeclaredField("userId"), new FieldRelationDto(allDto.getMemberId()));
                    fieldMap2.put(KpiItemResultCopy.class.getDeclaredField("mateFlag"), new FieldRelationDto("0"));
                } else if ("1".equals(allDto.getIndex().getCaliber()) && FormulaParamEnum.P_24.getType().equals(allDto.getParam().getParamType())) {
                    fieldMap.put(KpiItemResultCopy.class.getDeclaredField("deptId"), new FieldRelationDto(allDto.getUserDeptId()));
                } else if ("1".equals(allDto.getParam().getParamCate())) {
                    fieldMap.put(KpiItemResultCopy.class.getDeclaredField("userId"), new FieldRelationDto(memberList));
                }
                //指标颗粒度是科室 小项范围是按归集人员(转科
                else if ("2".equals(allDto.getIndex().getCaliber()) && FormulaParamEnum.P_13.getType().equals(allDto.getParam().getParamType())) {
                    fieldMap.put(KpiItemResultCopy.class.getDeclaredField("deptId"), new FieldRelationDto(allDto.getMemberId()));
                } else if ("2".equals(allDto.getParam().getParamCate())) {
                    fieldMap.put(KpiItemResultCopy.class.getDeclaredField("deptId"), new FieldRelationDto(memberList));
                }
            } else if (memberList.isEmpty()) {
                return BigDecimal.ZERO;
            }

            for (KpiItemResultCopy item : list) {
                try {
                    if (matchModel(item, fieldMap)) {
                        newList.add(item);
                    }
                    if (!fieldMap2.isEmpty()) {
                        if (matchModel(item, fieldMap2)) {
                            newList.add(item);
                        }
                    }
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            return Linq.of(newList).sumDecimal(t -> t.getValue());
        }
        //核算项 当量
        else if (allDto.getParam().getFieldType().equals(FormulaParamEnum.P_EQUIVALENT.getType())) {
            KpiItemEquivalentCopyGroupDto first = Linq.of(itemEquivalentGroup).firstOrDefault(t -> t.getCode().equals(allDto.getParam().getFieldCode()));
            if (first == null) {
                return BigDecimal.ZERO;
            }
            List<KpiItemEquivalentCopy> list = first.getList();
            if ("4".equals(allDto.getParam().getParamCate()) && !"2".equals(allDto.getIndex().getType())) {
                return Linq.of(list).sumDecimal(t -> t.getTotalEquivalent());
            }
            List<KpiItemEquivalentCopy> newList = new ArrayList<>();
            Map<Field, FieldRelationDto<Object>> fieldMap = new HashMap<>();
            Map<Field, FieldRelationDto<Object>> fieldMap2 = new HashMap<>();
            if (!memberList.isEmpty()) {
                if (FormulaParamEnum.P_19.getType().equals(allDto.getParam().getParamType())) {
                    fieldMap.put(KpiItemEquivalentCopy.class.getDeclaredField("userId"), new FieldRelationDto(allDto.getMemberId()));
                    fieldMap.put(KpiItemEquivalentCopy.class.getDeclaredField("accountUnitId"), new FieldRelationDto(allDto.getImpDeptId()));
                } else if ("1".equals(allDto.getIndex().getCaliber()) && FormulaParamEnum.P_24.getType().equals(allDto.getParam().getParamType())) {
                    fieldMap.put(KpiItemEquivalentCopy.class.getDeclaredField("accountUnitId"), new FieldRelationDto(allDto.getUserDeptId()));
                } else if ("1".equals(allDto.getParam().getParamCate())) {
                    fieldMap.put(KpiItemEquivalentCopy.class.getDeclaredField("userId"), new FieldRelationDto(memberList));
                }
                //指标颗粒度是科室 小项范围是按归集人员(转科
                else if ("2".equals(allDto.getIndex().getCaliber()) && FormulaParamEnum.P_13.getType().equals(allDto.getParam().getParamType())) {
                    fieldMap.put(KpiItemEquivalentCopy.class.getDeclaredField("accountUnitId"), new FieldRelationDto(allDto.getMemberId()));
                } else if ("2".equals(allDto.getParam().getParamCate())) {
                    fieldMap.put(KpiItemEquivalentCopy.class.getDeclaredField("accountUnitId"), new FieldRelationDto(memberList));
                }
            } else if (memberList.isEmpty()) {
                return BigDecimal.ZERO;
            }

            for (KpiItemEquivalentCopy item : list) {
                try {
                    if (matchModel(item, fieldMap)) {
                        newList.add(item);
                    }
                    if (!fieldMap2.isEmpty()) {
                        if (matchModel(item, fieldMap2)) {
                            newList.add(item);
                        }
                    }
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            return Linq.of(newList).sumDecimal(t -> t.getTotalEquivalent());
        }
        // 系统指标
        else if (FormulaParamEnum.P_SYSTEM.getType().equals(allDto.getParam().getFieldType())) {
            // todo 待完善
            FormulaParamEnum formulaParamEnum = FormulaParamEnum.find(allDto.getParam().getFieldCode());
            switch (formulaParamEnum) {
                case X_GANGWEI: {
                    List<String> list = Linq.of(kpiMembers).where(t ->
                                    allDto.getMemberId().equals(t.getHostId())
                                            && (t.getMemberType().equals("emp_type") || t.getMemberType().equals("role_emp_zw")))
                            .select(t -> t.getMemberCode()).toList();
                    KpiCoefficientCopy kpiCoefficientCopy = Linq.of(coefficients).where(t -> list.contains(t.getDicCode())).orderByDescending(t -> t.getValue()).firstOrDefault();
                    if (kpiCoefficientCopy == null) {
                        return BigDecimal.ZERO;
                    } else {
                        return kpiCoefficientCopy.getValue();
                    }
                }
                case X_GANGWEI_ZW:
                    List<String> list_zw = Linq.of(kpiMembers).where(t ->
                                    allDto.getMemberId().equals(t.getHostId())
                                            && ( t.getMemberType().equals("role_emp_zw")))
                            .select(t -> t.getMemberCode()).toList();
                    KpiCoefficientCopy kpiCoefficientCopy_zw = Linq.of(coefficients).where(t -> list_zw.contains(t.getDicCode())).orderByDescending(t -> t.getValue()).firstOrDefault();
                    if (kpiCoefficientCopy_zw == null) {
                        return BigDecimal.ZERO;
                    } else {
                        return kpiCoefficientCopy_zw.getValue();
                    }
                case X_GANGWEI_RY:
                    List<String> list_ry = Linq.of(kpiMembers).where(t ->
                                    allDto.getMemberId().equals(t.getHostId())
                                            && (t.getMemberType().equals("emp_type")))
                            .select(t -> t.getMemberCode()).toList();
                    KpiCoefficientCopy kpiCoefficientCopy_ry = Linq.of(coefficients).where(t -> list_ry.contains(t.getDicCode())).orderByDescending(t -> t.getValue()).firstOrDefault();
                    if (kpiCoefficientCopy_ry == null) {
                        return BigDecimal.ZERO;
                    } else {
                        return kpiCoefficientCopy_ry.getValue();
                    }
                case X_CHUQIN:
                    if (FormulaParamEnum.P_17.getType().equals(allDto.getParam().getParamType())
                        ||FormulaParamEnum.P_16.getType().equals(allDto.getParam().getParamType())) {
                        return Linq.of(kpiUserAttendances).where(t -> t.getAttendRate() != null && t.getUserId().equals(allDto.getMemberId()) && t.getAccountUnit().equals(allDto.getUserDeptId()))
                                .sumDecimal(t -> t.getAttendRate());
                    }
                    else if(FormulaParamEnum.P_18.getType().equals(allDto.getParam().getParamType()))
                    {
                        return Linq.of(kpiUserAttendances).where(t -> t.getAttendRate() != null &&
                                        t.getUserId().equals(allDto.getMemberId())
                                        && memberList.contains(t.getAccountUnit()))
                                .sumDecimal(t -> t.getAttendRate());
                    }
                    else {
                        return Linq.of(kpiUserAttendances).where(t -> t.getAttendRate() != null &&t.getUserId().equals(allDto.getMemberId()))
                                .sumDecimal(t -> t.getAttendRate());
                    }
                case X_CHUQIN_KS:
                    return Linq.of(kpiUserAttendances).where(t -> t.getAttendRate() != null &&t.getAccountUnit().equals(allDto.getMemberId()))
                            .sumDecimal(t -> t.getAttendRate());
                case X_ZAICE:
                    if (FormulaParamEnum.P_17.getType().equals(allDto.getParam().getParamType())
                        ||FormulaParamEnum.P_16.getType().equals(allDto.getParam().getParamType())) {
                        return Linq.of(kpiUserAttendances).where(t -> t.getRegisteredRate() != null &&t.getUserId().equals(allDto.getMemberId()) && t.getAccountUnit().equals(allDto.getUserDeptId()))
                                .sumDecimal(t -> t.getRegisteredRate());
                    }
                    else if(FormulaParamEnum.P_18.getType().equals(allDto.getParam().getParamType()))
                    {
                        return Linq.of(kpiUserAttendances).where(t ->t.getRegisteredRate() != null &&
                                        t.getUserId().equals(allDto.getMemberId())
                                        && memberList.contains(t.getAccountUnit()))
                                .sumDecimal(t -> t.getRegisteredRate());
                    }
                    else {
                        return Linq.of(kpiUserAttendances).where(t -> t.getRegisteredRate() != null &&t.getUserId().equals(allDto.getMemberId()))
                                .sumDecimal(t -> t.getRegisteredRate());
                    }
                case X_ZAICE_KS:
                    return Linq.of(kpiUserAttendances).where(t -> t.getRegisteredRate() != null &&t.getAccountUnit().equals(allDto.getMemberId()))
                            .sumDecimal(t -> t.getRegisteredRate());
                case X_ZDY:
                    if (FormulaParamEnum.P_17.getType().equals(allDto.getParam().getParamType())
                        ||FormulaParamEnum.P_16.getType().equals(allDto.getParam().getParamType())) {
                        return Linq.of(kpiUserAttendancesCustoms).where(t -> t.getValue() != null &&t.getUserId().equals(allDto.getMemberId()) && t.getDeptId().equals(allDto.getUserDeptId())
                                        && t.getColumnId().toString().equals(allDto.getParam().getFieldCodeExtra()))
                                .sumDecimal(t -> t.getValue());
                    }
                    else if(FormulaParamEnum.P_18.getType().equals(allDto.getParam().getParamType()))
                    {
                        return Linq.of(kpiUserAttendancesCustoms).where(t ->t.getValue() != null &&
                                        t.getUserId().equals(allDto.getMemberId())
                                        && memberList.contains(t.getDeptId())
                                        && t.getColumnId().toString().equals(allDto.getParam().getFieldCodeExtra()))
                                .sumDecimal(t -> t.getValue());
                    }
                    else {
                        return Linq.of(kpiUserAttendancesCustoms).where(t -> t.getValue() != null &&t.getUserId().equals(allDto.getMemberId())
                                        && t.getColumnId().toString().equals(allDto.getParam().getFieldCodeExtra()))
                                .sumDecimal(t -> t.getValue());
                    }
                case X_ZDY_KS:
                    return Linq.of(kpiUserAttendancesCustoms).where(t -> t.getValue() != null &&t.getDeptId().equals(allDto.getMemberId())
                                    && t.getColumnId().toString().equals(allDto.getParam().getFieldCodeExtra()))
                            .sumDecimal(t -> t.getValue());
                case X_GSKS:
                    return BigDecimal.ZERO;
                case X_JIANGJIN:
                    if (FormulaParamEnum.P_17.getType().equals(allDto.getParam().getParamType())
                        ||FormulaParamEnum.P_16.getType().equals(allDto.getParam().getParamType())) {
                        return Linq.of(kpiUserAttendances).where(t -> t.getRewardIndex() != null && t.getUserId().equals(allDto.getMemberId()) && t.getAccountUnit().equals(allDto.getUserDeptId()))
                                .sumDecimal(t -> t.getRewardIndex());
                    }
                    else if(FormulaParamEnum.P_18.getType().equals(allDto.getParam().getParamType()))
                    {
                        return Linq.of(kpiUserAttendances).where(t -> t.getRewardIndex() != null &&
                                        t.getUserId().equals(allDto.getMemberId())
                                        && memberList.contains(t.getAccountUnit()))
                                .sumDecimal(t -> t.getRewardIndex());
                    }
                    else {
                        return Linq.of(kpiUserAttendances).where(t -> t.getRewardIndex() != null &&t.getUserId().equals(allDto.getMemberId()))
                                .sumDecimal(t -> t.getRewardIndex());
                    }
                case X_KAOQIN:
                    if (FormulaParamEnum.P_17.getType().equals(allDto.getParam().getParamType())
                        ||FormulaParamEnum.P_16.getType().equals(allDto.getParam().getParamType())) {
                        return Linq.of(kpiUserAttendances).where(t -> t.getAttendanceGroupDays() != null && t.getUserId().equals(allDto.getMemberId()) && t.getAccountUnit().equals(allDto.getUserDeptId()))
                                .sumDecimal(t -> t.getAttendanceGroupDays());
                    }
                    else if(FormulaParamEnum.P_18.getType().equals(allDto.getParam().getParamType()))
                    {
                        return Linq.of(kpiUserAttendances).where(t -> t.getAttendanceGroupDays() != null &&
                                        t.getUserId().equals(allDto.getMemberId())
                                        && memberList.contains(t.getAccountUnit()))
                                .sumDecimal(t -> t.getAttendanceGroupDays());
                    }
                    else {
                        return Linq.of(kpiUserAttendances).where(t -> t.getAttendanceGroupDays() != null &&t.getUserId().equals(allDto.getMemberId()))
                                .sumDecimal(t -> t.getAttendanceGroupDays());
                    }
                case X_COEFFICIENT:
                {
                    List<KpiUserFactorCopy> list = Linq.of(kpiUserFactors).where(t ->
                            allDto.getMemberId().equals(t.getUserId())
                                    && (t.getType().equals(UserFactorCodeEnum.COEFFICIENT.getCode()))
                                    && allDto.getParam().getFieldCodeExtra().equals(t.getDictType())).toList();
                    if (CollectionUtils.isEmpty(list)) {
                        list = Linq.of(kpiUserFactors).where(t ->
                                !StringUtil.isNullOrEmpty(allDto.getParam().getFieldCodeExtra())
                                        && allDto.getParam().getFieldCodeExtra().equals(t.getDictType())
                                        && (t.getType().equals(UserFactorCodeEnum.OFFICE.getCode()))
                                        && allDto.getMemberId().equals(t.getUserId())).toList();
                        list.forEach(x -> {
                            List<KpiDictItemCopy> items = Linq.of(kpiDictItems).where(a -> x.getDictType().equals(a.getDictType()) && x.getItemCode().equals(a.getItemCode())).toList();
                            KpiDictItemCopy first = Linq.of(items).orderByDescending(t -> t.getPersonnelFactorValue()).firstOrDefault();
                            if (first != null) {
                                x.setValue(first.getPersonnelFactorValue() == null ? BigDecimal.ZERO:first.getPersonnelFactorValue());
                            } else {
                                x.setValue(BigDecimal.ZERO);
                            }
                        });
                    }
                    KpiUserFactorCopy factor = Linq.of(list).orderByDescending(t -> t.getValue()).firstOrDefault();
                    if (factor == null) {
                        return BigDecimal.ZERO;
                    } else {
                        return factor.getValue();
                    }
                }
                case X_SUBSIDY: {
                    List<KpiUserFactorCopy> list = Linq.of(kpiUserFactors).where(t ->
                            allDto.getMemberId().equals(t.getUserId())
                                    && (t.getType().equals(UserFactorCodeEnum.SUBSIDY.getCode()))
                                    && allDto.getParam().getFieldCodeExtra().equals(t.getDictType())).toList();
                    if (CollectionUtils.isEmpty(list)) {
                        list = Linq.of(kpiUserFactors).where(t ->
                                !StringUtil.isNullOrEmpty(allDto.getParam().getFieldCodeExtra())
                                        && allDto.getParam().getFieldCodeExtra().equals(t.getDictType())
                                        && (t.getType().equals(UserFactorCodeEnum.OFFICE.getCode()))
                                        && allDto.getMemberId().equals(t.getUserId())).toList();
                        list.forEach(x -> {
                            List<KpiDictItemCopy> items = Linq.of(kpiDictItems).where(a -> x.getDictType().equals(a.getDictType()) && x.getItemCode().equals(a.getItemCode())).toList();
                            KpiDictItemCopy first = Linq.of(items).orderByDescending(t -> t.getPerformanceSubsidyValue()).firstOrDefault();
                            if (first != null) {
                                x.setValue(first.getPerformanceSubsidyValue() == null ? BigDecimal.ZERO:first.getPerformanceSubsidyValue());
                            } else {
                                x.setValue(BigDecimal.ZERO);
                            }
                        });
                    }
                    KpiUserFactorCopy factor = Linq.of(list).orderByDescending(t -> t.getValue()).firstOrDefault();
                    if (factor == null) {
                        return BigDecimal.ZERO;
                    } else {
                        return factor.getValue();
                    }
                }
                case X_DICT_PRO_CATEGORY: {
                    List<String> codes = Linq.of(kpiItems).where(x -> allDto.getParam().getFieldCodeExtra().equals(x.getProCategoryCode())).select(x -> x.getCode()).toList();
                    if (CollectionUtils.isEmpty(codes)) {
                        return BigDecimal.ZERO;
                    }
                    List<KpiItemEquivalentCopyGroupDto> list = Linq.of(itemEquivalentGroup).where(x -> codes.contains(x.getCode())).toList();
                    if (CollectionUtils.isEmpty(list)) {
                        return BigDecimal.ZERO;
                    }
                    List<KpiItemEquivalentCopy> results = new ArrayList<>();
                    list.forEach(a -> results.addAll(Linq.of(a.getList()).where(m -> (allDto.getMemberId().equals(m.getAccountUnitId())
                            || allDto.getMemberId().equals(m.getUserId())) && m.getTotalEquivalent() != null).toList()));
                    return Linq.of(results).sumDecimal(KpiItemEquivalentCopy::getTotalEquivalent);
                }
                case X_EQUITEMTPRICE: {
                    return equitemtprice;
                }
                case X_DEPT_FACTOR: {
                    return Linq.of(kpiAccountUnits).where(r -> memberList.contains(r.getId()) && r.getFactor() != null)
                            .sumDecimal(KpiAccountUnitCopy::getFactor);
                }
            }
            return BigDecimal.ZERO;
        }
        else {
            //指标
            KpiCalculateGroupDto first = null;
            synchronized (calculateGroup) {
                first = Linq.of(calculateGroup).firstOrDefault(t -> t.getCode().equals(allDto.getParam().getFieldCode()));
            }
            if (first == null) {
                return BigDecimal.ZERO;
            }
            List<KpiCalculate> list = first.getList();
            list = Linq.of(list).where(r -> !"2".equals(r.getImputationType())).toList();
            if ("4".equals(allDto.getParam().getParamCate())) {
                return Linq.of(list).sumDecimal(KpiCalculate::getValue);
            }
            List<KpiCalculate> newList = new ArrayList<>();
            Map<Field, FieldRelationDto<Object>> fieldMap = new HashMap<>();
            Map<Field, FieldRelationDto<Object>> fieldMap2 = new HashMap<>();
            fieldMap.put(KpiCalculate.class.getDeclaredField("imputationType"), new FieldRelationDto(false, "2"));
            // 本人员及负责科室特殊处理双重过滤
            if ("1".equals(allDto.getIndex().getCaliber()) && FormulaParamEnum.P_16.getType().equals(allDto.getParam().getParamType())) {
                fieldMap.put(KpiCalculate.class.getDeclaredField("userId"), new FieldRelationDto(allDto.getMemberId()));
                fieldMap.put(KpiCalculate.class.getDeclaredField("deptId"), new FieldRelationDto(allDto.getUserDeptId()));
            } else if ("1".equals(allDto.getIndex().getCaliber()) && FormulaParamEnum.P_24.getType().equals(allDto.getParam().getParamType())) {
                fieldMap.put(KpiCalculate.class.getDeclaredField("deptId"), new FieldRelationDto(allDto.getUserDeptId()));
            } else if ("1".equals(allDto.getParam().getParamCate())) {
                // todo 待完善
                if (FormulaParamEnum.P_19.getType().equals(allDto.getParam().getParamType())) {
                    fieldMap.put(KpiCalculate.class.getDeclaredField("userId"), new FieldRelationDto(memberList));
                    fieldMap.put(KpiCalculate.class.getDeclaredField("deptId"), new FieldRelationDto(null));
                    fieldMap2.put(KpiCalculate.class.getDeclaredField("userId"), new FieldRelationDto(memberList));
                    fieldMap2.put(KpiCalculate.class.getDeclaredField("deptId"), new FieldRelationDto(allDto.getImpDeptId()));
                } else {
                    fieldMap.put(KpiCalculate.class.getDeclaredField("userId"), new FieldRelationDto(memberList));
                }
            }
            //指标颗粒度是科室 小项范围是按归集人员(转科
            else if ("2".equals(allDto.getIndex().getCaliber()) && FormulaParamEnum.P_13.getType().equals(allDto.getParam().getParamType())) {
                fieldMap.put(KpiCalculate.class.getDeclaredField("deptId"), new FieldRelationDto(allDto.getMemberId()));
            } else if ("2".equals(allDto.getParam().getParamCate())) {
                fieldMap.put(KpiCalculate.class.getDeclaredField("deptId"), new FieldRelationDto(memberList));
            }
            for (KpiCalculate item : list) {
                try {
                    if (matchModel(item, fieldMap)) {
                        newList.add(item);
                    }
                    if (!fieldMap2.isEmpty()) {
                        if (matchModel(item, fieldMap2)) {
                            newList.add(item);
                        }
                    }
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            return Linq.of(newList).sumDecimal(t -> t.getValue());
        }
    }

    //用code刷新公式的指标名称
    private KpiFormulaDto2.FieldListDTO changeField(KpiFormulaDto2.FieldListDTO field) {
        String s = field_total.get(field.getFieldCode());
        if (s != null) {
            field.setFieldName(s);
        }
        return field;
    }

    private KpiCalculate getCa(KpiCalculate ca, String type, String alloName, String outName, CalAllDto allDto
            , BigDecimal value, BigDecimal allo_ratio, BigDecimal allo_value) {
        KpiCalculate calculate = new KpiCalculate();
        calculate.setOutName(outName);
        calculate.setAllocationRatio(allo_ratio);
        calculate.setAllocationValue(allo_value);
        calculate.setAllocationName(alloName);
        calculate.setAllocationType(type);
        calculate.setTenantId(task.getTenantId());
        calculate.setPeriod(allDto.getPeriod());
        calculate.setTaskChildId(task_child.getId());
        calculate.setCode(allDto.getIndex().getCode());
        calculate.setValue(value);
        calculate.setImputationType("0");

        calculate.setCreatedDate(new Date());

        if (ca == null) {
            calculate.setResultJson(JSON.toJSONString(allDto.getFormulaDto()));
        } else {
            calculate.setResultJson(ca.getResultJson());
        }
        if (allDto.isAlloEmpFlag()) {
            calculate.setUserId(allDto.getMemberId());
        } else {
            calculate.setDeptId(allDto.getMemberId());
            KpiAccountUnitCopy c = Linq.of(kpiAccountUnits).firstOrDefault(t -> t.getId().equals(calculate.getDeptId()));
            if (c != null) {
                calculate.setDeptName(c.getName());
                SysDictItem s = Linq.of(dict).firstOrDefault(t -> t.getItemValue().equals(c.getCategoryCode()));
                if (s != null) {
                    calculate.setGroupName(s.getLabel());
                }
            }
        }
        return calculate;
    }

    //找到所有公式对象汇总循环 修改子memberid，和主memberid
    private List<Long> getResultList(CalAllDto allDto) {
        List<Long> result = new ArrayList<>();
        allDto.getFormulaDto().getFieldList().forEach(r -> {
            Map<Field, FieldRelationDto<Object>> fieldMap = new HashMap<>();
            allDto.getConditions().forEach(t -> {
                try {
                    Field key = KpiItemResultCopy.class.getDeclaredField(toCamelCase(t.getKey()));
                    Object value;
                    boolean relation = "等于".equals(t.getRelation().trim());
                    if (t.getType().equals("string")) {
                        if (KpiItemResultCopy.class.getDeclaredField(toCamelCase(t.getKey())).getType().equals(Long.class)) {
                            value = Linq.of(t.getValue()).select(q -> Long.parseLong(q.getValue())).toList();
                        } else {
                            value = Linq.of(t.getValue()).select(q -> q.getValue()).toList();
                        }
                    } else {
                        value = KpiItemResultCopy.class.getDeclaredField(toCamelCase(t.getValue().get(0).getValue()));
                    }
                    fieldMap.put(key, new FieldRelationDto(relation, value));
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            });
            KpiItemResultCopyGroupDto first = Linq.of(itemResultGroup).firstOrDefault(m -> m.getCode().equals(r.getFieldCode()));
            if (first == null) {
                return;
            }
            List<KpiItemResultCopy> list = first.getList();
            List<KpiItemResultCopy> newList = new ArrayList<>();
            for (KpiItemResultCopy item : list) {
                try {
                    if (matchModel(item, fieldMap)) {
                        newList.add(item);
                    }
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            switch (allDto.getFormulaDto().getMemberList().get(0).getValue()) {
                case "-100":
                    result.addAll(Linq.of(newList).where(t -> t.getBrks() != null).select(t -> t.getBrks()).toList());
                    break;
                case "-101":
                    result.addAll(Linq.of(newList).where(t -> t.getZdysks() != null).select(t -> t.getZdysks()).toList());
                    break;
                case "-102":
                    result.addAll(Linq.of(newList).where(t -> t.getKzysks() != null).select(t -> t.getKzysks()).toList());
                    break;
                case "-103":
                    result.addAll(Linq.of(newList).where(t -> t.getBrbq() != null).select(t -> t.getBrbq()).toList());
                    break;
                case "-201":
                    break;
                case "-202":
                    break;
            }
        });
        return result.stream().distinct().collect(Collectors.toList());
    }

    //字段转驼峰
    public String toCamelCase(String str) {
        if (str == null || str.isEmpty() || !str.contains("_")) {
            return str;
        }
        str = str.toLowerCase();
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        for (int i = 0; i < str.length(); i++) {
            char currentChar = str.charAt(i);
            if (currentChar == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(currentChar));
                    nextUpperCase = false;
                } else {
                    result.append(Character.toLowerCase(currentChar));
                }
            }
        }

        return result.toString();
    }

    //按入参条件匹配
    private boolean matchModel(Object model, Map<Field, FieldRelationDto<Object>> fieldMap)
            throws NoSuchFieldException, IllegalAccessException {
        for (Map.Entry<Field, FieldRelationDto<Object>> entry : fieldMap.entrySet()) {
            FieldRelationDto<Object> fieldValue = entry.getValue();
            entry.getKey().setAccessible(true);
            Object object = fieldValue.getObject();
            if (object == null) {
                if (entry.getKey().get(model) != null) {
                    return false;
                }
            } else if (object instanceof List) {
                List<Object> li_obj = (List) object;
                if ((fieldValue.isRelation() && !Linq.of(li_obj).any(t -> {
                    try {
                        return t.equals(entry.getKey().get(model));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })) || (!fieldValue.isRelation() && Linq.of(li_obj).any(t -> {
                    try {
                        return t.equals(entry.getKey().get(model));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }))) {
                    return false;
                }
            } else if (object instanceof Field) {
                ((Field) object).setAccessible(true);
                if ((fieldValue.isRelation() && !entry.getKey().get(model).equals(((Field) object).get(model))) ||
                        (!fieldValue.isRelation() && entry.getKey().get(model).equals(((Field) object).get(model)))) {
                    return false;
                }
            } else if ((fieldValue.isRelation() && !object.equals(entry.getKey().get(model)))
                    || (!fieldValue.isRelation() && object.equals(entry.getKey().get(model)))) {
                return false;
            }
        }
        return true;
    }
//endregion

}
