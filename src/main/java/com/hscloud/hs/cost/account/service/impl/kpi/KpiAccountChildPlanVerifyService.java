package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.enums.kpi.CodePrefixEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.FormulaParamEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.IndexTypeEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.MemberEnum;
import com.hscloud.hs.cost.account.mapper.kpi.*;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostClusterUnit;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountPlanListVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiFormulaItemVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiPlanConfigVO;
import com.hscloud.hs.cost.account.service.impl.dataReport.CostClusterUnitService;
import com.hscloud.hs.cost.account.service.kpi.CommCodeService;
import com.hscloud.hs.cost.account.service.kpi.IKpiAccountPlanService;
import com.hscloud.hs.cost.account.utils.kpi.Convert;
import com.hscloud.hs.cost.account.utils.kpi.FormulaDependencyChecker;
import com.hscloud.hs.cost.account.utils.kpi.RealTimeFormulaDependencyChecker;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.admin.api.vo.UserCoreVo;
import com.pig4cloud.pigx.common.core.constant.SecurityConstants;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import io.netty.util.internal.StringUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
* 子方案校验
*
*/
@Service
public class KpiAccountChildPlanVerifyService {

    @Autowired
    private KpiAccountPlanMapper kpiAccountPlanMapper;
    @Autowired
    private CommCodeService commCodeService;
    @Autowired
    @Lazy
    private KpiAccountPlanService kpiAccountPlanService;
    @Autowired
    private KpiCategoryService kpiCategoryService;
    @Autowired
    private KpiIndexService kpiIndexService;
    @Autowired
    private KpiIndexFormulaObjService kpiIndexFormulaObjService;
    @Autowired
    private KpiIndexFormulaService kpiIndexFormulaService;
    @Autowired
    private KpiUserAttendanceMapper kpiUserAttendanceMapper;
    @Autowired
    private KpiMemberMapper kpiMemberMapper;
    @Autowired
    private KpiAccountUnitMapper kpiAccountUnitMapper;
    @Autowired
    private CostClusterUnitService costClusterUnitService;
    @Autowired
    private KpiMemberService kpiMemberService;
    @Autowired
    private KpiUserAttendanceService kpiUserAttendanceService;
    @Autowired
    private RemoteUserService remoteUserService;
    @Autowired
    private KpiCalculateMapper kpiCalculateMapper;


    public void judge(KpiAccountPlanChild kpiAccountPlanChild, KpiPlanVerifyDto kpiPlanVerifyDto, RealTimeFormulaDependencyChecker realTimeFormulaDependencyChecker) {
        List<KpiIndexFormulaObj> kpiIndexFormulaObjs = kpiPlanVerifyDto.getKpiIndexFormulaObjs();
        List<KpiIndexFormula> kpiIndexFormulas = kpiPlanVerifyDto.getKpiIndexFormulas();
        List<KpiIndex> kpiIndices = kpiPlanVerifyDto.getKpiIndices();
        List<UserCoreVo> sysUsers = kpiPlanVerifyDto.getSysUsers();
        List<KpiAccountUnit> kpiAccountUnits = kpiPlanVerifyDto.getKpiAccountUnits();
        kpiPlanVerifyDto.setMyMemberId(Optional.ofNullable(kpiAccountPlanChild.getUserId()).orElse(kpiAccountPlanChild.getDeptId()));
        String planCode = kpiAccountPlanChild.getPlanCode();
        kpiPlanVerifyDto.setChildPlanCode(kpiAccountPlanChild.getCode());
        kpiPlanVerifyDto.setChildPlanName(kpiAccountPlanChild.getPlanName());
        KpiAccountPlan kpiAccountPlan = kpiAccountPlanService.getOne(new LambdaQueryWrapper<KpiAccountPlan>().eq(KpiAccountPlan::getPlanCode, planCode));
        //指标中绑定的方案是方案分组(category)
        KpiCategory kpiCategory = kpiCategoryService.getOne(new LambdaQueryWrapper<KpiCategory>().eq(KpiCategory::getCategoryCode, kpiAccountPlan.getCategoryCode()));
        kpiPlanVerifyDto.setPlanCategoryCode(kpiCategory.getCategoryCode());
        //获取指标
        String indexCode = kpiAccountPlanChild.getIndexCode();
//        KpiIndex kpiIndex = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, indexCode));
        KpiIndex kpiIndex = kpiIndices.stream().filter(o->o.getCode().equals(indexCode)).findFirst().orElse(null);
        if (kpiIndex.getStatus().equals("1")||kpiIndex.getDelFlag().equals("1")){
            KpiPlanVerifyDto.MissIndex missIndex = new KpiPlanVerifyDto.MissIndex();
            missIndex.setChildPlanCode(kpiAccountPlanChild.getCode());
            missIndex.setChildPlanName(kpiAccountPlanChild.getPlanName());
            missIndex.setIndexCode(kpiIndex.getCode());
            missIndex.setIndexName(kpiIndex.getName());
            kpiPlanVerifyDto.getMissIndices().add(missIndex);
            return;
        }
        String type = kpiIndex.getType();
        //检验非条件
        if (type.equals(IndexTypeEnum.NOT_COND.getType())){
            //找到包含方案和对象的对应公式
//            LambdaQueryWrapper<KpiIndexFormulaObj> wrapper = new LambdaQueryWrapper<KpiIndexFormulaObj>().eq(KpiIndexFormulaObj::getIndexCode, indexCode)
//                    .eq(KpiIndexFormulaObj::getPlanCode, kpiPlanVerifyDto.getPlanCategoryCode())
//                    .eq(KpiIndexFormulaObj::getPlanObj, kpiAccountPlanChild.getUserId()==null?kpiAccountPlanChild.getDeptId():kpiAccountPlanChild.getUserId());
//            KpiIndexFormulaObj kpiIndexFormulaObj = kpiIndexFormulaObjService.getOne(wrapper);
            KpiIndexFormulaObj kpiIndexFormulaObj = kpiIndexFormulaObjs.stream()
                    .filter(o->o.getIndexCode().equals(indexCode)
                            &&o.getPlanCode().equals(kpiPlanVerifyDto.getPlanCategoryCode())
                            &&(o.getPlanObj().equals(kpiAccountPlanChild.getUserId()==null?kpiAccountPlanChild.getDeptId():kpiAccountPlanChild.getUserId())||StringUtils.equalsAnyIgnoreCase(o.getPlanObj().toString(),"-100","-200"))
                    ).findFirst().orElse(null);

            if (kpiIndexFormulaObj == null){
                KpiPlanVerifyDto.MissFormula missFormula = new KpiPlanVerifyDto.MissFormula();
                missFormula.setChildPlanCode(kpiAccountPlanChild.getCode());
                missFormula.setChildPlanName(kpiAccountPlanChild.getPlanName());
                missFormula.setPlanObj(kpiAccountPlanChild.getUserId()==null?kpiAccountPlanChild.getDeptId():kpiAccountPlanChild.getUserId());
                if (kpiAccountPlanChild.getUserId() != null){
                    UserCoreVo userCoreVo = sysUsers.stream().filter(o -> o.getUserId().equals(kpiAccountPlanChild.getUserId())).findFirst().orElse(null);
                    missFormula.setPlanObjName(userCoreVo==null?"":userCoreVo.getName());
                }
                if (kpiAccountPlanChild.getDeptId() != null){
                    KpiAccountUnit accountUnit = kpiAccountUnits.stream().filter(o -> o.getId().equals(kpiAccountPlanChild.getDeptId())).findFirst().orElse(null);
                    missFormula.setPlanObjName(accountUnit==null?"":accountUnit.getName());
                }
                missFormula.setIndexCode(kpiIndex.getCode());
                missFormula.setIndexName(kpiIndex.getName());
                missFormula.setIndexCaliber(kpiIndex.getCaliber());
                kpiPlanVerifyDto.getMissFormulas().add(missFormula);
                return;
            }

//            KpiIndexFormula kpiIndexFormula = kpiIndexFormulaService.getById(kpiIndexFormulaObj.getFormulaId());
            KpiIndexFormula kpiIndexFormula = kpiIndexFormulas.stream().filter(o->o.getId().equals(kpiIndexFormulaObj.getFormulaId())&&o.getDelFlag().equals("0")).findFirst().orElse(null);
            if (kpiIndexFormula == null || StringUtils.isBlank(kpiIndexFormula.getFormula())){
                KpiPlanVerifyDto.MissFormula missFormula = new KpiPlanVerifyDto.MissFormula();
                missFormula.setChildPlanCode(kpiAccountPlanChild.getCode());
                missFormula.setChildPlanName(kpiAccountPlanChild.getPlanName());
                missFormula.setPlanObj(kpiAccountPlanChild.getUserId()==null?kpiAccountPlanChild.getDeptId():kpiAccountPlanChild.getUserId());
                if (kpiAccountPlanChild.getUserId() != null){
                    UserCoreVo userCoreVo = sysUsers.stream().filter(o -> o.getUserId().equals(kpiAccountPlanChild.getUserId())).findFirst().orElse(null);
                    missFormula.setPlanObjName(userCoreVo==null?"":userCoreVo.getName());
                }
                if (kpiAccountPlanChild.getDeptId() != null){
                    KpiAccountUnit accountUnit = kpiAccountUnits.stream().filter(o -> o.getId().equals(kpiAccountPlanChild.getDeptId())).findFirst().orElse(null);
                    missFormula.setPlanObjName(accountUnit==null?"":accountUnit.getName());
                }
                missFormula.setIndexCode(kpiIndex.getCode());
                missFormula.setIndexName(kpiIndex.getName());
                missFormula.setIndexCaliber(kpiIndex.getCaliber());
                kpiPlanVerifyDto.getMissFormulas().add(missFormula);
                return;
            }

            judgeMember(kpiIndexFormula, kpiPlanVerifyDto, realTimeFormulaDependencyChecker);
        }else if (type.equals(IndexTypeEnum.COND.getType())){

        }else if (type.equals(IndexTypeEnum.ALLOCATION.getType())){

        }else {
            throw new BizException("未找到对应指标类型");
        }
        return;
    }

    //公式里指标的members是否都有对应方案的公式
    void judgeMember(KpiIndexFormula kpiIndexFormula, KpiPlanVerifyDto kpiPlanVerifyDto, RealTimeFormulaDependencyChecker realTimeFormulaDependencyChecker){
        List<KpiIndexFormulaObj> kpiIndexFormulaObjs = kpiPlanVerifyDto.getKpiIndexFormulaObjs();
        List<KpiIndex> kpiIndices = kpiPlanVerifyDto.getKpiIndices();
        List<KpiIndexFormula> kpiIndexFormulas = kpiPlanVerifyDto.getKpiIndexFormulas();
        List<UserCoreVo> sysUsers = kpiPlanVerifyDto.getSysUsers();
        List<KpiAccountUnit> kpiAccountUnits = kpiPlanVerifyDto.getKpiAccountUnits();
        //获取公式中指标
        KpiIndex kpiIndex = kpiIndices.stream().filter(o -> o.getCode().equals(kpiIndexFormula.getIndexCode()) ).findFirst().orElse(null);
        if (kpiIndex.getStatus().equals("1")||kpiIndex.getDelFlag().equals("1")){

        }
        kpiPlanVerifyDto.setIndexCaliber(kpiIndex.getCaliber());
        KpiFormulaDto formulaDto = JSONUtil.toBean(kpiIndexFormula.getFormula(), KpiFormulaDto.class);
        List<KpiFormulaItemDto> fieldList = formulaDto.getFieldList();
        if (kpiIndex.getType().equals(IndexTypeEnum.NOT_COND.getType())){
            //获取对应人员或科室
            for (KpiFormulaItemDto kpiFormulaItemDto : fieldList) {
                if (!"index".equals(kpiFormulaItemDto.getFieldType())){
                    continue;
                }
                if (!kpiFormulaItemDto.getFieldType().equals("index")){
                    continue;
                }


                List<Long> memberList = getMemberList(kpiFormulaItemDto, kpiPlanVerifyDto);
                boolean b = realTimeFormulaDependencyChecker.addDependency(kpiIndex.getCode(), kpiFormulaItemDto.getFieldCode());
                if (!b){
                    KpiPlanVerifyDto.Cycledependency cycle = new KpiPlanVerifyDto.Cycledependency();
                    cycle.setChildPlanCode(kpiPlanVerifyDto.getChildPlanCode());
                    cycle.setChildPlanName(kpiPlanVerifyDto.getChildPlanName());
                    cycle.setIndexCode(kpiIndex.getCode());
                    cycle.setIndexName(kpiIndex.getName());
                    cycle.setDependencyCode(kpiFormulaItemDto.getFieldCode());
                    cycle.setDependencyName(kpiFormulaItemDto.getFieldName());
                    kpiPlanVerifyDto.getCycledependencies().add(cycle);
                    continue;
                }
                KpiIndex kpiIndex1 = kpiIndices.stream()
                        .filter(o -> o.getCode().equals(kpiFormulaItemDto.getFieldCode())
                        )
                        .findFirst().orElse(null);
                if (kpiIndex1.getDelFlag().equals("1")||kpiIndex1.getStatus().equals("1")){
                    KpiPlanVerifyDto.MissIndex missIndex = new KpiPlanVerifyDto.MissIndex();
                    missIndex.setChildPlanCode(kpiPlanVerifyDto.getChildPlanCode());
                    missIndex.setChildPlanName(kpiPlanVerifyDto.getChildPlanName());
                    missIndex.setIndexCode(kpiIndex1.getCode());
                    missIndex.setIndexName(kpiIndex1.getName());
                    kpiPlanVerifyDto.getMissIndices().add(missIndex);
                    continue;
                }
                if (!kpiIndex1.getType().equals("1")){
                    continue;
                }
                for (Long memberId : memberList) {
                    kpiPlanVerifyDto.setMyMemberId(memberId);
                    //判断池子是否已有,已有则跳过
                    {
                        List<KpiPlanVerifyDto.ConfigState> collect = kpiPlanVerifyDto.getConfigStates().stream()
                                .filter(configState -> configState.getMemberId().equals(memberId) && configState.getIndexCode().equals(kpiIndex1.getCode()) && configState.getPlanCode().equals(kpiPlanVerifyDto.getPlanCategoryCode())).collect(Collectors.toList());
                        if (CollectionUtil.isNotEmpty(collect)){
                            continue;
                        }
                    }
                    KpiIndexFormulaObj kpiIndexFormulaObj = kpiIndexFormulaObjs.stream()
                            .filter(o->o.getIndexCode().equals(kpiFormulaItemDto.getFieldCode())
                                    &&o.getPlanCode().equals(kpiPlanVerifyDto.getPlanCategoryCode())
                                    &&(o.getPlanObj().equals(memberId)||StringUtils.equalsAnyIgnoreCase(o.getPlanObj().toString(), "-100","-200"))).findFirst().orElse(null);


                    KpiPlanVerifyDto.ConfigState configState = new KpiPlanVerifyDto.ConfigState();
                    configState.setMemberId(memberId);
                    configState.setPlanCode(kpiPlanVerifyDto.getPlanCategoryCode());
                    configState.setIndexCode(kpiFormulaItemDto.getFieldCode());

                    if (kpiIndexFormulaObj == null){
                        configState.setState(1);
                        kpiPlanVerifyDto.getConfigStates().add(configState);
                        //未配置的指标对象
                        {

                            KpiPlanVerifyDto.MissFormula missFormula = new KpiPlanVerifyDto.MissFormula();
                            missFormula.setChildPlanCode(kpiPlanVerifyDto.getChildPlanCode());
                            missFormula.setChildPlanName(kpiPlanVerifyDto.getChildPlanName());
                            missFormula.setPlanObj(kpiPlanVerifyDto.getMyMemberId());
                            if (StringUtils.equalsAnyIgnoreCase(kpiFormulaItemDto.getParamType(),"10","11")){
                                UserCoreVo userCoreVo = sysUsers.stream().filter(o -> o.getUserId().equals(memberId)).findFirst().orElse(null);
                                missFormula.setPlanObjName(userCoreVo==null?"":userCoreVo.getName());
                            }
                            else{
                                KpiAccountUnit accountUnit = kpiAccountUnits.stream().filter(o -> o.getId().equals(memberId)).findFirst().orElse(null);
                                missFormula.setPlanObjName(accountUnit==null?"":accountUnit.getName());
                            }
                            missFormula.setIndexCode(kpiIndex1.getCode());
                            missFormula.setIndexName(kpiIndex1.getName());
                            missFormula.setIndexCaliber(kpiIndex1.getCaliber());
                            kpiPlanVerifyDto.getMissFormulas().add(missFormula);
                        }
                    }else {
                        KpiIndexFormula formula = kpiIndexFormulas.stream()
                                .filter(o->o.getId().equals(kpiIndexFormulaObj.getFormulaId())
                                &&o.getDelFlag().equals("0")
                                ).findFirst().orElse(null);
                        if (formula == null || StringUtils.isBlank(formula.getFormula())){
//未配置的指标对象
                            {

                                KpiPlanVerifyDto.MissFormula missFormula = new KpiPlanVerifyDto.MissFormula();
                                missFormula.setChildPlanCode(kpiPlanVerifyDto.getChildPlanCode());
                                missFormula.setChildPlanName(kpiPlanVerifyDto.getChildPlanName());
                                missFormula.setPlanObj(kpiPlanVerifyDto.getMyMemberId());
                                if (StringUtils.equalsAnyIgnoreCase(kpiFormulaItemDto.getParamType(),"10","11")){
                                    UserCoreVo userCoreVo = sysUsers.stream().filter(o -> o.getUserId().equals(memberId)).findFirst().orElse(null);
                                    missFormula.setPlanObjName(userCoreVo==null?"":userCoreVo.getName());
                                }
                                else{
                                    KpiAccountUnit accountUnit = kpiAccountUnits.stream().filter(o -> o.getId().equals(memberId)).findFirst().orElse(null);
                                    missFormula.setPlanObjName(accountUnit==null?"":accountUnit.getName());
                                }
                                missFormula.setIndexCode(kpiIndex1.getCode());
                                missFormula.setIndexName(kpiIndex1.getName());
                                missFormula.setIndexCaliber(kpiIndex1.getCaliber());
                                kpiPlanVerifyDto.getMissFormulas().add(missFormula);
                            }
                        }else {
                            configState.setState(0);
                            kpiPlanVerifyDto.getConfigStates().add(configState);
                            judgeMember(formula, kpiPlanVerifyDto, realTimeFormulaDependencyChecker);
                        }


                    }
                }
            }
        }
    }

    @Autowired
    private KpiAccountUnitService kpiAccountUnitService;

    //取对象合集
    private List<Long> getMemberList(KpiFormulaItemDto kpiFormulaItemDto, KpiPlanVerifyDto kpiPlanVerifyDto) {
        Long myMemberId = kpiPlanVerifyDto.getMyMemberId();
        List<Long> rt = new ArrayList<>();
        List<KpiMember> kpiMembers = kpiPlanVerifyDto.getKpiMembers();
        List<KpiAccountUnit> kpiAccountUnits = kpiPlanVerifyDto.getKpiAccountUnits();
        List<CostClusterUnit> costClusterUnits = kpiPlanVerifyDto.getCostClusterUnits();
        List<SysUser> users = kpiCalculateMapper.getUsers(SecurityUtils.getUser().getTenantId());
        if (!StringUtil.isNullOrEmpty(kpiFormulaItemDto.getParamType())) {
            FormulaParamEnum formulaParamEnum = FormulaParamEnum.find(kpiFormulaItemDto.getParamType());
            switch (formulaParamEnum) {
                case P_10://本人员
                case P_19:
                    rt.add(myMemberId);
                    break;
                case P_11://自定义人员
                    rt.addAll(Linq.of(kpiFormulaItemDto.getParamValues()).select(r -> Long.parseLong(r.getValue())).toList());
                    break;
                case P_12://人员类型（字典对应user_type）
//                    List<KpiUserAttendance> users = kpiUserAttendanceMapper.selectList(
//                            new QueryWrapper<KpiUserAttendance>()
//                                    .in("user_type", kpiFormulaItemDto.getParamValues().stream().map(o->o.getValue()).collect(Collectors.toList())));
//                    rt.addAll(Linq.of(users).select(r -> r.getUserId()).toList());
                    break;
                case P_13://按归集
//                    List<CostClusterUnit> list = costClusterUnits.stream().filter(o -> o.getId().equals(myMemberId) && o.getDelFlag().equals("0") && o.getStatus().equals("0")).collect(Collectors.toList());
//                    if (CollectionUtil.isNotEmpty(list)){
//                        for (CostClusterUnit costClusterUnit : list) {
//                            if (StringUtils.isNotBlank(costClusterUnit.getUnits())){
//                                List<JSONObject> list1 = JSONUtil.toList(costClusterUnit.getUnits(), JSONObject.class);
//                                for (JSONObject jo : list1) {
//                                    rt.add(Long.valueOf(jo.get("id").toString()));
//                                }
//                            }
//                        }
//                    }
                    break;
                case P_14://按人员分组
//                    List<Long> users_role = Linq.of(kpiMembers).where(t -> t.getMemberType().equals(MemberEnum.ROLE_EMP.getType())
//                                    && Linq.of(kpiFormulaItemDto.getParamValues()).select(x -> x.getValue()).toList().contains(t.getMemberCode()))
//                            .select(t -> t.getHostId()).toList();
//                    rt.addAll(users_role);
                    break;
                case P_15://按工作性质
//                    List<Long> users_job = kpiUserAttendanceService.list(new LambdaQueryWrapper<KpiUserAttendance>()
//                            .in(KpiUserAttendance::getJobNature, Linq.of(kpiFormulaItemDto.getParamValues())
//                                    .select(x -> x.getValue()))).stream().map(o->o.getUserId()).collect(Collectors.toList());
//
//                    rt.addAll(users_job);
                    break;
                case P_100://所有人员
                    rt.addAll(Linq.of(users).select(r -> r.getUserId()).toList());
                case P_20://本科室单元
                    rt.add(myMemberId);
                    break;
                case P_21://自定义科室
                    rt.addAll(Linq.of(kpiFormulaItemDto.getParamValues()).select(r -> Long.parseLong(r.getValue())).toList());
                    break;
                case P_22://核算类型（字典对应kpi_calculate_type）
//                    List<Long> list1 = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
//                            && Linq.of(kpiFormulaItemDto.getParamValues()).select(r -> r.getValue()).toList().contains(t.getAccountTypeCode())).select(o -> o.getId()).toList();
//
//                    rt.addAll(list1);
                    break;
                case P_23://核算分组
//                    List<KpiAccountUnit> account_group = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
//                            && Linq.of(kpiFormulaItemDto.getParamValues()).select(r -> r.getValue()).toList().contains(t.getCategoryCode())).toList();
//                    rt.addAll(Linq.of(account_group).select(r -> r.getId()).toList());
                    break;
                case P_24://本人员负责科室
                    List<KpiAccountUnit> account_dept = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && t.getResponsiblePersonId() != null && Arrays.asList(t.getResponsiblePersonId().split(",")).contains(myMemberId.toString())).toList();
                    rt.addAll(Linq.of(account_dept).select(r -> r.getId()).toList());
                    break;
                case P_25://科室单元人员类型（字典对应user_type）
//                    List<KpiAccountUnit> account_rylx = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
//                            && Linq.of(kpiFormulaItemDto.getParamValues()).select(r -> r.getValue()).toList().contains(t.getAccountUserCode())).toList();
//                    rt.addAll(Linq.of(account_rylx).select(r -> r.getId()).toList());
                    break;
                case P_29://所有科室单元
//                    rt.addAll(Linq.of(kpiAccountUnits).select(r -> r.getId()).toList());
                    break;
            }
            //口径剔除
            if (!kpiFormulaItemDto.getParamExcludes().isEmpty()) {
                List<Long> list = Linq.of(kpiFormulaItemDto.getParamExcludes()).select(t -> Long.parseLong(t.getValue())).toList();
                rt.removeAll(list);
            }
        }
        return rt;
    }

}
