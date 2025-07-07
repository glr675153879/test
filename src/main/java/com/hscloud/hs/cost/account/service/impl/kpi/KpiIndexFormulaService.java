package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.enums.EnableEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.*;
import com.hscloud.hs.cost.account.mapper.kpi.*;
import com.hscloud.hs.cost.account.model.dto.DictDto;
import com.hscloud.hs.cost.account.model.dto.deptCost.DcSysSoftwareDto;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.*;
import com.hscloud.hs.cost.account.utils.ExpressionCheckHelper;
import com.hscloud.hs.cost.account.utils.kpi.Convert;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.service.kpi.IKpiIndexFormulaService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
* 指标公式表 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class KpiIndexFormulaService extends ServiceImpl<KpiIndexFormulaMapper, KpiIndexFormula> implements IKpiIndexFormulaService {
    @Autowired
    @Lazy
    private KpiIndexService kpiIndexService;
    @Autowired
    private KpiIndexFormulaMapper kpiIndexFormulaMapper;
    @Autowired
    private KpiAccountUnitService kpiAccountUnitService;
    @Autowired
    private KpiMemberService kpiMemberService;
    @Autowired
    private RemoteUserService remoteUserService;
    @Autowired
    @Lazy
    private KpiAccountPlanService kpiAccountPlanService;
    @Autowired
    private KpiCategoryService kpiCategoryService;
    @Autowired
    private KpiIndexFormulaObjService kpiIndexFormulaObjService;
    @Autowired
    private KpiItemResultService kpiItemResultService;
    @Autowired
    @Lazy
    private KpiAccountPlanChildService kpiAccountPlanChildService;
    @Autowired
    @Lazy
    private KpiAllocationRuleService kpiAllocationRuleService;
    @Autowired
    private KpiMemberMapper kpiMemberMapper;
    @Autowired
    private KpiAccountPlanChildMapper kpiAccountPlanChildMapper;
    @Autowired
    private KpiIndexMapper kpiIndexMapper;

    @Override
    public Long saveOrupdate(KpiIndexFormulaDto dto) {
        //校验入参关联项
        KpiIndex one = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, dto.getIndexCode()));
        if (one == null){
            throw new BizException("指标不存在");
        }
//        if (one.getType().equals("1")){
//            dto.setPlanCode("");
//        }
        //公式校验
//        checkFormula(dto.getFormula());
        KpiIndexFormula kpiIndexFormula = new KpiIndexFormula();
        kpiIndexFormula.setFormulaGroup(1);
        if (dto.getId() != null){
            kpiIndexFormula = getById(dto.getId());
            if (kpiIndexFormula == null){
                throw new BizException("公式不存在");
            }
            //移除老的member
            List<Long> collect = list(new LambdaQueryWrapper<KpiIndexFormula>().eq(KpiIndexFormula::getIndexCode, kpiIndexFormula.getIndexCode()).eq(KpiIndexFormula::getFormulaGroup, kpiIndexFormula.getFormulaGroup())).stream().map(o -> o.getId()).collect(Collectors.toList());
            remove(new LambdaQueryWrapper<KpiIndexFormula>().eq(KpiIndexFormula::getIndexCode, one.getCode()).eq(KpiIndexFormula::getFormulaGroup, kpiIndexFormula.getFormulaGroup()).eq(KpiIndexFormula::getPlanCode, dto.getPlanCode()));
            kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>().in(KpiMember::getHostId, collect).eq(KpiMember::getMemberType, MemberEnum.FORMULA_ITEM.getType()));
        }
        BeanUtil.copyProperties(dto, kpiIndexFormula, CopyOptions.create().ignoreNullValue());
        kpiIndexFormula.setDelFlag("0");
        //组号
        Integer zh = kpiIndexFormulaMapper.getZh(one.getCode());

        kpiIndexFormula.setFormulaGroup(Optional.ofNullable(zh).orElse(1)+1);

        saveOrUpdate(kpiIndexFormula);


        //加入member关联
        int i = 1;
        if (CollectionUtil.isNotEmpty(dto.getFormulas())){
            for (String formula : dto.getFormulas()) {
                if (i > 1){
                    kpiIndexFormula.setId(null);
                    kpiIndexFormula.setFormula(formula);
                    save(kpiIndexFormula);
                }else {
                    kpiIndexFormula.setFormula(formula);
                    updateById(kpiIndexFormula);
                }

                JSONObject jsonObject = JSONUtil.toBean(formula, JSONObject.class);
                List<KpiFormulaItemDto> fields = jsonObject.getBeanList("fieldList", KpiFormulaItemDto.class);
                List<DictDto> members = jsonObject.getBeanList("memberList", DictDto.class);
                if (CollectionUtil.isNotEmpty(fields)) {
                    List<KpiMember> memberList = new ArrayList<>();
                    List<KpiFieldItemDto> fieldItemDtos = new ArrayList<>();

                    for (KpiFormulaItemDto kpiFormulaItemDto : fields) {
                        KpiFieldItemDto kpiFieldItemDto = new KpiFieldItemDto();
                        kpiFieldItemDto.setFieldType(kpiFormulaItemDto.getFieldType());
                        kpiFieldItemDto.setFieldCode(kpiFormulaItemDto.getFieldCode());
                        fieldItemDtos.add(kpiFieldItemDto);

                        KpiMember kpiMember = new KpiMember();
                        kpiMember.setPeriod(0L);
                        //一个公式多个公式项
                        kpiMember.setHostId(kpiIndexFormula.getId());
                        kpiMember.setHostCode(one.getCode());
                        kpiMember.setMemberCode(kpiFormulaItemDto.getFieldCode());
                        kpiMember.setMemberType(MemberEnum.FORMULA_ITEM.getType());
                        kpiMember.setTenantId(1L);
                        kpiMember.setBusiType("1");
                        memberList.add(kpiMember);
                    }
                    kpiMemberService.insertBatchSomeColumn(memberList);
                    kpiIndexFormula.setMemberCodes(JSONUtil.toJsonStr(fieldItemDtos));
                    updateById(kpiIndexFormula);
                }
                if (CollectionUtil.isNotEmpty(members)){
                    List<String> collect = members.stream().map(o -> o.getValue()).collect(Collectors.toList());
                    kpiIndexFormula.setMemberIds(StringUtils.join(collect,","));
                    updateById(kpiIndexFormula);
                }
                i++;
            }


        }
        if (StringUtils.isNotBlank(dto.getMemberIds())){
            KpiIndexPlanMemberEditDto kpiIndexPlanMemberEditDto = new KpiIndexPlanMemberEditDto();
            kpiIndexPlanMemberEditDto.setPlanCode(dto.getPlanCode());
            kpiIndexPlanMemberEditDto.setFormulaId(kpiIndexFormula.getId());
            kpiIndexPlanMemberEditDto.setMemberIds(dto.getMemberIds());
            kpiIndexPlanMemberEditDto.setDelflag("N");
            planSaveOrUpdate(kpiIndexPlanMemberEditDto);
        }
        fillIndexMemberCodes(one);
        one.updateById();
        return kpiIndexFormula.getId();
    }

    public List<FormulateMemberDto> fillIndexMemberCodes(KpiIndex kpiIndex){
        List<KpiIndexFormula> list = list(new LambdaQueryWrapper<KpiIndexFormula>().eq(KpiIndexFormula::getIndexCode, kpiIndex.getCode()).eq(KpiIndexFormula::getDelFlag,"0"));
        List<FormulateMemberDto> result = new ArrayList<>();
        for (KpiIndexFormula kpiIndexFormula : list) {
            List<FormulateMemberDto> list1 = fillMemberCodes(kpiIndexFormula);
            if (CollectionUtil.isNotEmpty(list1)){
                result.addAll(list1);
            }
        }
        if (CollectionUtil.isNotEmpty(result)){
            result = result.stream().distinct().collect(Collectors.toList());
            kpiIndex.setMemberCodes(JSONUtil.toJsonStr(result));
        }else {
            kpiIndex.setMemberCodes("");
        }
        return result;
    }

    public List<FormulateMemberDto> fillMemberCodes(KpiIndexFormula kpiIndexFormula){
        List<FormulateMemberDto> list = new ArrayList<>();
        if (StringUtils.isNotBlank(kpiIndexFormula.getFormula())){
            JSONObject jsonObject = JSONUtil.toBean(kpiIndexFormula.getFormula(), JSONObject.class);
            List<KpiFormulaItemDto> fields = jsonObject.getBeanList("fieldList", KpiFormulaItemDto.class);
            if (CollectionUtil.isNotEmpty(fields)){
                List<FormulateMemberDto> collect = fields.stream().map(o -> {
                    FormulateMemberDto formulateMemberDto = new FormulateMemberDto();
                    formulateMemberDto.setFieldCode(o.getFieldCode());
                    formulateMemberDto.setFieldType(o.getFieldType());
                    return formulateMemberDto;
                }).collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(collect)){
                    list.addAll(collect);
                }
            }

        }
        if (CollectionUtil.isNotEmpty(list)){
            list = list.stream().distinct().collect(Collectors.toList());
            kpiIndexFormula.setMemberCodes(JSONUtil.toJsonStr(list));
        }else {
            kpiIndexFormula.setMemberCodes("");
        }
        return list;
    }

    @Override
    public List<KpiIndexFormulaVO> getFormulaListByIndexCode(String indexCode,String planGroupCode, Long memberId, String planCategoryCode) {
        KpiIndex one = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, indexCode));
        if (one.getType().equals(IndexTypeEnum.COND.getType())){
            List<KpiIndexFormulaVO> result = new ArrayList<>();
            List<KpiIndexFormula> list = list(new LambdaQueryWrapper<KpiIndexFormula>()
                    .eq(KpiIndexFormula::getIndexCode, indexCode)
                    .eq(KpiIndexFormula::getDelFlag, "0").orderByDesc(KpiIndexFormula::getId));
            Map<Integer, List<KpiIndexFormula>> collect = list.stream().collect(Collectors.groupingBy(KpiIndexFormula::getFormulaGroup));
            Set<Integer> integers = collect.keySet();
            for (Integer integer : integers) {
                List<KpiIndexFormula> kpiIndexFormulas = collect.get(integer);
                KpiIndexFormula kpiIndexFormula = kpiIndexFormulas.get(0);
                KpiIndexFormulaVO kpiIndexFormulaVO = BeanUtil.copyProperties(kpiIndexFormula, KpiIndexFormulaVO.class);
                kpiIndexFormulaVO.setFormulas(kpiIndexFormulas.stream().map(KpiIndexFormula::getFormula).collect(Collectors.toList()));
                kpiIndexFormulaVO.setFormula("");
                result.add(kpiIndexFormulaVO);
            }
            return result;
        }else {
            LambdaQueryWrapper<KpiIndexFormula> wrapper = new LambdaQueryWrapper<KpiIndexFormula>()
                    .eq(StringUtils.isNotBlank(planCategoryCode), KpiIndexFormula::getPlanCode, planCategoryCode)
                    .eq(KpiIndexFormula::getIndexCode, indexCode)
                    .eq(KpiIndexFormula::getDelFlag, "0").orderByDesc(KpiIndexFormula::getId);
//            if (StringUtils.isNotBlank(planGroupCode)){
//                wrapper.eq(KpiIndexFormula::getPlanCode,planGroupCode);
//            }
            List<KpiIndexFormula> list = list(wrapper);
            List<KpiIndexFormulaVO> kpiIndexFormulaVOS = BeanUtil.copyToList(list, KpiIndexFormulaVO.class);
            for (KpiIndexFormulaVO kpiIndexFormulaVO : kpiIndexFormulaVOS) {
                fillPlanObjs(one.getCaliber(), kpiIndexFormulaVO.getId(),planCategoryCode, kpiIndexFormulaVO);
            }
            return kpiIndexFormulaVOS;
        }

    }

    void fillPlanObjs(String caliber, Long formulaId,String planCategoryCode, KpiIndexFormulaVO kpiIndexFormulaVO){
        List<KpiIndexFormulaObj> kpiObjs = kpiIndexFormulaObjService.list(new LambdaQueryWrapper<KpiIndexFormulaObj>().eq(KpiIndexFormulaObj::getFormulaId, formulaId).eq(KpiIndexFormulaObj::getPlanCode, planCategoryCode));
        List<Long> objCodes = kpiObjs.stream().filter(o->o.getPlanObj()!=null).map(kpiIndexFormulaObj -> kpiIndexFormulaObj.getPlanObj()).collect(Collectors.toList());

        List<String> objCategoryCodes = kpiObjs.stream().filter(o->StringUtils.isNotBlank(o.getPlanObjCategory())).map(o->o.getPlanObjCategory()).collect(Collectors.toList());
        List<String> objAccountTypes = kpiObjs.stream().filter(o->StringUtils.isNotBlank(o.getPlanObjAccountType())).map(o->o.getPlanObjAccountType()).collect(Collectors.toList());

        List<String> excludePersons = kpiObjs.stream().filter(o->StringUtils.isNotBlank(o.getExcludePerson())).map(o->o.getExcludePerson()).collect(Collectors.toList());
        List<String> excludeDepts = kpiObjs.stream().filter(o->StringUtils.isNotBlank(o.getExcludeDept())).map(o->o.getExcludeDept()).collect(Collectors.toList());

        if (CollectionUtil.isNotEmpty(excludeDepts)){
            List<KpiAccountUnit> data = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>().in(KpiAccountUnit::getId, Arrays.stream(excludeDepts.get(0).split(",")).map(Long::valueOf).collect(Collectors.toList())));
            List<KpiIndexPlanMemberDto> kpiIndexPlanMemberDtos = Convert.convertEntityToVo(data, KpiIndexPlanMemberDto::convertByAccountUnit);
            kpiIndexFormulaVO.setExcludeDept(excludeDepts.get(0));
            kpiIndexFormulaVO.setExcludeMembers(kpiIndexPlanMemberDtos);
        }

        if (CollectionUtil.isNotEmpty(excludePersons)){
            R<List<SysUser>> userList = remoteUserService.getUserList(Arrays.stream(excludePersons.get(0).split(",")).map(Long::valueOf).collect(Collectors.toList()));
            List<SysUser> data = userList.getData();
            List<KpiIndexPlanMemberDto> kpiIndexPlanMemberDtos = Convert.convertEntityToVo(data, KpiIndexPlanMemberDto::convertBySysUser);
            kpiIndexFormulaVO.setExcludeMembers(kpiIndexPlanMemberDtos);
            kpiIndexFormulaVO.setExcludePerson(excludePersons.get(0));
        }

        if (CollectionUtil.isNotEmpty(kpiObjs)){
            //获取适用对象
            List<KpiIndexPlanMemberDto> planObjs = new ArrayList<>();
            if (caliber.equals(CaliberEnum.DEPT.getType())){
                if (CollectionUtil.isNotEmpty(objCodes) && objCodes.size() == 1 && objCodes.get(0).equals(-200L)){
                    KpiIndexPlanMemberDto kpiIndexPlanMemberDto = new KpiIndexPlanMemberDto();
                    kpiIndexPlanMemberDto.setLabel("所有科室");
                    kpiIndexPlanMemberDto.setValue("-200");
                    planObjs.add(kpiIndexPlanMemberDto);

                }else {
                    if (CollectionUtil.isNotEmpty(objCodes)){
                        List<KpiAccountUnit> depts = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>().in(KpiAccountUnit::getId, objCodes));
                        planObjs = Convert.convertEntityToVo(depts, KpiIndexPlanMemberDto::convertByAccountUnit);
                    }
                }

                if (CollectionUtil.isNotEmpty(objAccountTypes)){
                    List<SysDictItem> list = kpiIndexMapper.getSysDict(objAccountTypes, "kpi_calculate_grouping",SecurityUtils.getUser().getTenantId());
                    List<KpiIndexPlanMemberDto> planObjs1 = Convert.convertEntityToVo(list, KpiIndexPlanMemberDto::convertByDict);
                    planObjs.addAll(planObjs1);
                }

            }else if (caliber.equals(CaliberEnum.PEOPLE.getType())){
                if (CollectionUtil.isNotEmpty(objCodes) && objCodes.size() == 1 && objCodes.get(0).equals(-100L)){
                    KpiIndexPlanMemberDto kpiIndexPlanMemberDto = new KpiIndexPlanMemberDto();
                    kpiIndexPlanMemberDto.setLabel("所有人员");
                    kpiIndexPlanMemberDto.setValue("-100");
                    planObjs.add(kpiIndexPlanMemberDto);

                }else if (CollectionUtil.isNotEmpty(objCodes)){
                    R<List<SysUser>> listR = remoteUserService.getUserList(objCodes);
                    planObjs = Convert.convertEntityToVo(listR.getData(), KpiIndexPlanMemberDto::convertBySysUser);
                }
                if (CollectionUtil.isNotEmpty(objCategoryCodes)){
                    List<KpiCategory> list = kpiCategoryService.list(new LambdaQueryWrapper<KpiCategory>().in(KpiCategory::getCategoryCode, objCategoryCodes));
                    List<KpiIndexPlanMemberDto> planObjs1 = Convert.convertEntityToVo(list, KpiIndexPlanMemberDto::convertByCategory);
                    planObjs.addAll(planObjs1);
                }
            }
            kpiIndexFormulaVO.setPlanMembers(planObjs);
        }
    }

    /**
     * 非条件指标才需要
     * @param dto
     * @return
     */
    @Override
    public List<KpiIndexFormulaPlanVO> getPlanList(KpiIndexFormulaPlanListInfoDto dto) {
        List<KpiIndexFormulaPlanVO> result = new ArrayList<>();
        List<KpiIndexFormulaPlanVO> list = new ArrayList<>();
        //获取指标和公式
        KpiIndexFormula kpiIndexFormula = getById(dto.getFormulaId());
        KpiIndex kpiIndex = null;
        try {
            kpiIndex = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, kpiIndexFormula.getIndexCode()));
        }catch (Exception e){
            throw new BizException("获取指标异常");
        }

        //获取所有方案分组
        List<KpiCategory> allPlanGroup = kpiCategoryService.list(new LambdaQueryWrapper<KpiCategory>().eq(KpiCategory::getCategoryType, CategoryEnum.PLAN_GROUP.getType()).eq(KpiCategory::getStatus, 0L).eq(KpiCategory::getDelFlag, "0"));


        //获取已经填充的方案和对象
        List<KpiIndexFormulaObj> kpiObjs = kpiIndexFormulaObjService.list(new LambdaQueryWrapper<KpiIndexFormulaObj>()
                .eq(KpiIndexFormulaObj::getFormulaId, kpiIndexFormula.getId()));

        if (CollectionUtil.isNotEmpty(kpiObjs)){
            Map<String, List<KpiIndexFormulaObj>> collect = kpiObjs.stream().collect(Collectors.groupingBy(KpiIndexFormulaObj::getPlanCode));
            for (String planCode : collect.keySet()) {
                KpiIndexFormulaPlanVO kpiIndexFormulaPlanVO = new KpiIndexFormulaPlanVO();

                List<KpiIndexFormulaObj> planObjCodes = collect.get(planCode);
                List<Long> objCodes = planObjCodes.stream().filter(o->o.getPlanObj()!=null).map(kpiIndexFormulaObj -> kpiIndexFormulaObj.getPlanObj()).collect(Collectors.toList());
                List<String> objCategoryCodes = planObjCodes.stream().filter(o->StringUtils.isNotBlank(o.getPlanObjCategory())).map(kpiIndexFormulaObj -> kpiIndexFormulaObj.getPlanObjCategory()).collect(Collectors.toList());
                List<String> objAccountTypes = planObjCodes.stream().filter(o->StringUtils.isNotBlank(o.getPlanObjAccountType())).map(o->o.getPlanObjAccountType()).collect(Collectors.toList());

                //获取方案
                KpiCategory plan = kpiCategoryService.getOne(new LambdaQueryWrapper<KpiCategory>().eq(KpiCategory::getCategoryCode, planCode));
                //获取适用对象
                List<KpiIndexPlanMemberDto> planObjs = new ArrayList<>();
                if (kpiIndex.getCaliber().equals(CaliberEnum.DEPT.getType())){
                    if (CollectionUtil.isNotEmpty(objCodes) && objCodes.size() == 1 && objCodes.get(0).equals(-200L)){
                        KpiIndexPlanMemberDto kpiIndexPlanMemberDto = new KpiIndexPlanMemberDto();
                        kpiIndexPlanMemberDto.setLabel("所有科室");
                        kpiIndexPlanMemberDto.setValue("-200");
                        kpiIndexFormulaPlanVO.setExcludeDept(planObjCodes.get(0).getExcludeDept());
                        if (StringUtils.isNotBlank(planObjCodes.get(0).getExcludeDept())){
                            List<KpiAccountUnit> data = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>().in(KpiAccountUnit::getId, planObjCodes.get(0).getExcludeDept().split(",")));
                            List<KpiIndexPlanMemberDto> kpiIndexPlanMemberDtos = Convert.convertEntityToVo(data, KpiIndexPlanMemberDto::convertByAccountUnit);
                            kpiIndexFormulaPlanVO.setExcludeDepts(kpiIndexPlanMemberDtos);
                        }
                        planObjs.add(kpiIndexPlanMemberDto);
                    }else {
                        if (CollectionUtil.isNotEmpty(objCodes)){
                            List<KpiAccountUnit> depts = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>().in(KpiAccountUnit::getId, objCodes));
                            planObjs = Convert.convertEntityToVo(depts, KpiIndexPlanMemberDto::convertByAccountUnit);
                        }
                    }
                    if (CollectionUtil.isNotEmpty(objAccountTypes)){
                        List<SysDictItem> list1 = kpiIndexMapper.getSysDict(objAccountTypes, "kpi_calculate_grouping", SecurityUtils.getUser().getTenantId());
                        List<KpiIndexPlanMemberDto> planObjs1 = Convert.convertEntityToVo(list1, KpiIndexPlanMemberDto::convertByDict);
                        planObjs.addAll(planObjs1);
                    }
                }else if (kpiIndex.getCaliber().equals(CaliberEnum.PEOPLE.getType())){
                    if (CollectionUtil.isNotEmpty(objCodes) && objCodes.size() == 1 && objCodes.get(0).equals(-100L)){
                        KpiIndexPlanMemberDto kpiIndexPlanMemberDto = new KpiIndexPlanMemberDto();
                        kpiIndexPlanMemberDto.setLabel("所有人员");
                        kpiIndexPlanMemberDto.setValue("-100");
                        kpiIndexFormulaPlanVO.setExcludePerson(planObjCodes.get(0).getExcludePerson());
                        if (StringUtils.isNotBlank(planObjCodes.get(0).getExcludePerson())){
                            R<List<SysUser>> userList = remoteUserService.getUserList(Arrays.stream(planObjCodes.get(0).getExcludePerson().split(",")).map(Long::valueOf).collect(Collectors.toList()));
                            List<SysUser> data = userList.getData();
                            List<KpiIndexPlanMemberDto> kpiIndexPlanMemberDtos = Convert.convertEntityToVo(data, KpiIndexPlanMemberDto::convertBySysUser);
                            kpiIndexFormulaPlanVO.setExcludeMembers(kpiIndexPlanMemberDtos);
                        }
                        planObjs.add(kpiIndexPlanMemberDto);
                    }else if (CollectionUtil.isNotEmpty(objCodes)){
                        R<List<SysUser>> listR = remoteUserService.getUserList(objCodes);
                        planObjs = Convert.convertEntityToVo(listR.getData(), KpiIndexPlanMemberDto::convertBySysUser);
                    }
                    if (CollectionUtil.isNotEmpty(objCategoryCodes)){
                        List<KpiCategory> list1 = kpiCategoryService.list(new LambdaQueryWrapper<KpiCategory>().in(KpiCategory::getCategoryCode, objCategoryCodes));
                        List<KpiIndexPlanMemberDto> planObjs1 = Convert.convertEntityToVo(list1, KpiIndexPlanMemberDto::convertByCategory);
                        planObjs.addAll(planObjs1);
                    }

                }
                //填充
                kpiIndexFormulaPlanVO.setPlanId(plan.getId());
                kpiIndexFormulaPlanVO.setPlanCode(plan.getCategoryCode());
                kpiIndexFormulaPlanVO.setPlanName(plan.getCategoryName());
                kpiIndexFormulaPlanVO.setPlanMembers(planObjs);
                list.add(kpiIndexFormulaPlanVO);
            }
        }

        for (KpiCategory plan : allPlanGroup) {
            KpiIndexFormulaPlanVO kpiIndexFormulaPlanVO = new KpiIndexFormulaPlanVO();
            kpiIndexFormulaPlanVO.setPlanId(plan.getId());
            kpiIndexFormulaPlanVO.setPlanCode(plan.getCategoryCode());
            kpiIndexFormulaPlanVO.setPlanName(plan.getCategoryName());
            List<KpiIndexFormulaPlanVO> collect = list.stream().filter(kpiIndexFormulaPlanVO1 -> kpiIndexFormulaPlanVO1.getPlanId().equals(plan.getId())).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(collect)){
                kpiIndexFormulaPlanVO.setExcludePerson(collect.get(0).getExcludePerson());
                kpiIndexFormulaPlanVO.setExcludeDept(collect.get(0).getExcludeDept());
                kpiIndexFormulaPlanVO.setPlanMembers(collect.get(0).getPlanMembers());
                kpiIndexFormulaPlanVO.setExcludeMembers(collect.get(0).getExcludeMembers());
            }
            result.add(kpiIndexFormulaPlanVO);
        }

        return result;
    }

@Autowired
private KpiCalculateMapper kpiCalculateMapper;

    List<Long> judegePeople(KpiIndexPlanMemberEditDto dto){

        KpiIndexFormula kpiIndexFormula = getById(dto.getFormulaId());
        KpiIndex kpiIndex = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, kpiIndexFormula.getIndexCode()));

        List<SysUser> users = kpiCalculateMapper.getUsers(SecurityUtils.getUser().getTenantId());
        List<Long> allUserIds = users.stream().map(SysUser::getUserId).collect(Collectors.toList());
        //入参所带的人员
        List<Long> inUserIds = new ArrayList<>();
        List<Long> inDataSourceUserIds = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getMemberIds())){
            if (dto.getMemberIds().equals("-100")){
                inUserIds = allUserIds;
                if (StringUtils.isNotBlank(dto.getExcludePerson())){
                    inUserIds.removeAll(Arrays.stream(dto.getExcludePerson().split(",")).map(Long::valueOf).collect(Collectors.toList()));
                }
            }else {
                inUserIds = Arrays.stream(dto.getMemberIds().split(",")).map(o->Long.valueOf(o)).collect(Collectors.toList());
            }
        }

        if (StringUtils.isNotBlank(dto.getMemberCategroyCodes())){
            List<KpiMember> kpiMembers = kpiMemberMapper.selectList(new LambdaQueryWrapper<KpiMember>()
                    .in(KpiMember::getHostCode, Arrays.asList(dto.getMemberCategroyCodes().split(",")))
                    .eq(KpiMember::getMemberType, MemberEnum.ROLE_EMP)
                    .eq(KpiMember::getPeriod, 0L)
            );
            List<Long> collect = kpiMembers.stream().map(o -> o.getMemberId()).collect(Collectors.toList());
            inUserIds.addAll(collect);
            if (StringUtils.isNotBlank(dto.getExcludePerson())){
                inUserIds.removeAll(Arrays.stream(dto.getExcludePerson().split(",")).map(Long::valueOf).collect(Collectors.toList()));
            }
        }



        List<KpiIndexFormulaObj> list = kpiIndexFormulaObjService.list(new LambdaQueryWrapper<KpiIndexFormulaObj>()
                .eq(KpiIndexFormulaObj::getIndexCode, kpiIndex.getCode())
                .eq(KpiIndexFormulaObj::getPlanCode, dto.getPlanCode())
                .ne(KpiIndexFormulaObj::getFormulaId, kpiIndexFormula.getId())
        );

        List<Long> list1 = list.stream().filter(o -> o.getPlanObj() != null).map(o -> o.getPlanObj()).collect(Collectors.toList());
        Map<Long, List<KpiIndexFormulaObj>> collect1 = list.stream().collect(Collectors.groupingBy(o -> o.getFormulaId()));
        Set<Long> formulaIds = collect1.keySet();
        for (Long formulaId : formulaIds) {
            List<Long> exclude = new ArrayList<>();
            //当行人员
            List<Long> dqry = new ArrayList<>();
            List<KpiIndexFormulaObj> groupByFormula = collect1.get(formulaId);


            for (KpiIndexFormulaObj kpiIndexFormulaObj : groupByFormula) {
                if (kpiIndexFormulaObj.getPlanObj()!=null && kpiIndexFormulaObj.getPlanObj().equals(-100L)){
                    dqry.addAll(allUserIds);
                    inDataSourceUserIds.addAll(allUserIds);
                }
                if (StringUtils.isNotBlank(kpiIndexFormulaObj.getPlanObjCategory())){
                    //获取组里的人
                    List<KpiMember> kpiMembers = kpiMemberMapper.selectList(new LambdaQueryWrapper<KpiMember>()
                            .eq(KpiMember::getHostCode, kpiIndexFormulaObj.getPlanObjCategory())
                            .eq(KpiMember::getMemberType, MemberEnum.ROLE_EMP)
                            .eq(KpiMember::getPeriod, 0L)
                    );
                    dqry.addAll(kpiMembers.stream().map(o -> o.getMemberId()).collect(Collectors.toList()));
                    inDataSourceUserIds.addAll(kpiMembers.stream().map(o->o.getMemberId()).collect(Collectors.toList()));
                }
                if (kpiIndexFormulaObj.getPlanObj() != null){
//                人
                    dqry.add(kpiIndexFormulaObj.getPlanObj());
                    inDataSourceUserIds.add(kpiIndexFormulaObj.getPlanObj());
                }
                if (StringUtils.isNotBlank(kpiIndexFormulaObj.getExcludePerson())){
                    List<Long> collect = Arrays.stream(kpiIndexFormulaObj.getExcludePerson().split(",")).map(Long::valueOf).collect(Collectors.toList());
                    //不在当中的就不算
                    collect = collect.stream().filter(o->dqry.contains(o)).collect(Collectors.toList());
                    exclude.addAll(collect);
                }
            }






            if (CollectionUtil.isNotEmpty(exclude)){
                inDataSourceUserIds.removeAll(exclude);
            }
        }




        Collection<Long> intersection = CollectionUtil.intersection(inUserIds, inDataSourceUserIds);
        if (CollectionUtil.isNotEmpty(intersection)){
            //如果皆是在入参和planobj之中,则提示
            if (list1.containsAll(intersection) && StringUtils.isNotBlank(dto.getMemberIds()) && Arrays.stream(dto.getMemberIds().split(",")).map(Long::valueOf).collect(Collectors.toList()).containsAll(intersection)){
                return Arrays.asList(intersection.toArray(new Long[intersection.size()]));
            }else {
                throw new BizException("已有适用对象被其他公式引用");
            }
        }
        return null;
    }



    List<Long> judegeDept(KpiIndexPlanMemberEditDto dto){

        KpiIndexFormula kpiIndexFormula = getById(dto.getFormulaId());
        KpiIndex kpiIndex = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, kpiIndexFormula.getIndexCode()));

        List<KpiAccountUnit> allDepts = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>().eq(KpiAccountUnit::getDelFlag, EnableEnum.ENABLE.getType()));

        List<Long> allDeptIds = allDepts.stream().map(KpiAccountUnit::getId).collect(Collectors.toList());
        //入参所带的人员
        List<Long> inDeptIds = new ArrayList<>();
        List<Long> inDataSourceDeptIds = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getMemberIds())){
            if (dto.getMemberIds().equals("-200")){
                inDeptIds = allDeptIds;
                if (StringUtils.isNotBlank(dto.getExcludeDept())){
                    inDeptIds.removeAll(Arrays.stream(dto.getExcludeDept().split(",")).map(Long::valueOf).collect(Collectors.toList()));
                }
            }else {
                inDeptIds = Arrays.stream(dto.getMemberIds().split(",")).map(o->Long.valueOf(o)).collect(Collectors.toList());
            }
        }

        if (StringUtils.isNotBlank(dto.getPlanObjAccountType())){
            List<KpiAccountUnit> list2 = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>().eq(KpiAccountUnit::getDelFlag, EnableEnum.ENABLE.getType())
                    .in(KpiAccountUnit::getCategoryCode, dto.getPlanObjAccountType().split(",")));
            List<Long> collect = list2.stream().map(o -> o.getId()).collect(Collectors.toList());
            inDeptIds.addAll(collect);
            if (StringUtils.isNotBlank(dto.getExcludeDept())){
                inDeptIds.removeAll(Arrays.stream(dto.getExcludeDept().split(",")).map(Long::valueOf).collect(Collectors.toList()));
            }
        }



        List<KpiIndexFormulaObj> list = kpiIndexFormulaObjService.list(new LambdaQueryWrapper<KpiIndexFormulaObj>()
                .eq(KpiIndexFormulaObj::getIndexCode, kpiIndex.getCode())
                .eq(KpiIndexFormulaObj::getPlanCode, dto.getPlanCode())
                .ne(KpiIndexFormulaObj::getFormulaId, kpiIndexFormula.getId())
        );

        List<Long> list1 = list.stream().filter(o -> o.getPlanObj() != null).map(o -> o.getPlanObj()).collect(Collectors.toList());




        Map<Long, List<KpiIndexFormulaObj>> collect1 = list.stream().collect(Collectors.groupingBy(o -> o.getFormulaId()));
        Set<Long> formulaIds = collect1.keySet();
        for (Long formulaId : formulaIds) {
            List<Long> exclude = new ArrayList<>();
            //当行科室
            List<Long> dqks = new ArrayList<>();
            List<KpiIndexFormulaObj> groupByFormula = collect1.get(formulaId);
            for (KpiIndexFormulaObj kpiIndexFormulaObj : groupByFormula) {

                if (kpiIndexFormulaObj.getPlanObj()!=null && kpiIndexFormulaObj.getPlanObj().equals(-200L)){
                    dqks.addAll(allDeptIds);
                    inDataSourceDeptIds.addAll(allDeptIds);
                }
                if (StringUtils.isNotBlank(kpiIndexFormulaObj.getPlanObjAccountType())){
                    //获取组里的科室
                    List<KpiAccountUnit> list2 = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>().eq(KpiAccountUnit::getDelFlag, EnableEnum.ENABLE.getType())
                            .in(KpiAccountUnit::getCategoryCode, kpiIndexFormulaObj.getPlanObjAccountType().split(",")));
                    List<Long> collect = list2.stream().map(o -> o.getId()).collect(Collectors.toList());
                    dqks.addAll(collect);
                    inDataSourceDeptIds.addAll(collect);
                }
                if (kpiIndexFormulaObj.getPlanObj() != null){
//                科室
                    dqks.add(kpiIndexFormulaObj.getPlanObj());
                    inDataSourceDeptIds.add(kpiIndexFormulaObj.getPlanObj());
                }

                if (StringUtils.isNotBlank(kpiIndexFormulaObj.getExcludeDept())){
                    List<Long> collect = Arrays.stream(kpiIndexFormulaObj.getExcludeDept().split(",")).map(Long::valueOf).collect(Collectors.toList());
                    //不在当中的就不算
                    collect = collect.stream().filter(o->dqks.contains(o)).collect(Collectors.toList());
                    exclude.addAll(collect);
                }
            }
            if (CollectionUtil.isNotEmpty(exclude)){
                inDataSourceDeptIds.removeAll(exclude);
            }
        }





        Collection<Long> intersection = CollectionUtil.intersection(inDeptIds, inDataSourceDeptIds);
        if (CollectionUtil.isNotEmpty(intersection)){
            //如果皆是在入参和planobj之中,则提示
            if (list1.containsAll(intersection) && StringUtils.isNotBlank(dto.getMemberIds()) && Arrays.stream(dto.getMemberIds().split(",")).map(Long::valueOf).collect(Collectors.toList()).containsAll(intersection)){
                return Arrays.asList(intersection.toArray(new Long[intersection.size()]));
            }else {
                throw new BizException("已有适用对象被其他公式引用");
            }
        }
        return null;
    }



    @Override
    public void planSaveOrUpdate(KpiIndexPlanMemberEditDto dto) {

        KpiIndexFormula kpiIndexFormula = getById(dto.getFormulaId());
        KpiIndex one = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, kpiIndexFormula.getIndexCode()));

//        if (one.getCaliber().equals(CaliberEnum.PEOPLE.getType())){
//            judgePlanObjCategory(one.getCode(), dto.getPlanCode(), dto.getFormulaId(), dto.getMemberIds(), dto.getMemberCategroyCodes());
//        }
//        if (one.getCaliber().equals(CaliberEnum.DEPT.getType())){
//            judgeAccountType(one.getCode(), dto.getPlanCode(), dto.getFormulaId(), dto.getMemberIds(), dto.getPlanObjAccountType());
//        }
//
//        //获取所有方案下的适用对象,删除已存在的,在此之前会提示前端是否覆盖
//        List<KpiIndexFormulaObj> list = kpiIndexFormulaObjService.list(new LambdaQueryWrapper<KpiIndexFormulaObj>()
//                .eq(KpiIndexFormulaObj::getIndexCode, kpiIndexFormula.getIndexCode())
//                .eq(KpiIndexFormulaObj::getPlanCode, dto.getPlanCode())
//                .ne(KpiIndexFormulaObj::getFormulaId,dto.getFormulaId()));
//
//        //如果加入的或历史的包含所有对象,则报错
//        List<KpiIndexFormulaObj> collect = list.stream().filter(
//                o -> (o.getPlanObj() != null && o.getPlanObj().equals(-100L)
//                        && (
//                        StringUtils.isBlank(o.getExcludePerson())
//                                || !Arrays.asList(o.getExcludePerson().split(",")).containsAll(Arrays.asList(dto.getMemberIds().split(",")))
//                )
//                )
//                        || (o.getPlanObj() != null && o.getPlanObj().equals(-200L)
//                        && (
//                        StringUtils.isBlank(o.getExcludeDept())
//                                || !Arrays.asList(o.getExcludeDept().split(",")).containsAll(Arrays.asList(dto.getMemberIds().split(",")))
//                )
//                )).collect(Collectors.toList());
//        if (CollectionUtil.isNotEmpty(collect)){
//            throw new BizException("已有公式包含所有对象");
//        }
//
//        if (StringUtils.isNotBlank(dto.getExcludePerson())){
//            List<String> collect1 = list.stream().filter(o -> o.getPlanObj() != null).map(o -> o.getPlanObj().toString()).collect(Collectors.toList());
//            if (Arrays.asList(dto.getExcludePerson().split(",")).containsAll(collect1)){
//
//            }else {
//                throw new BizException("已有公式包含未剔除对象");
//            }
//        }
//        if (StringUtils.isNotBlank(dto.getExcludeDept())){
//            List<String> collect1 = list.stream().filter(o -> o.getPlanObj() != null).map(o -> o.getPlanObj().toString()).collect(Collectors.toList());
//            if (Arrays.asList(dto.getExcludeDept().split(",")).containsAll(collect1)){
//
//            }else {
//                throw new BizException("已有公式包含未剔除对象");
//            }
//        }
         if (one.getCaliber().equals(CaliberEnum.PEOPLE.getType())){
            judegePeople(dto);
        }
        if (one.getCaliber().equals(CaliberEnum.DEPT.getType())){
            judegeDept(dto);
        }

        //删除formulaObj
        if (dto.getDelflag().equals("Y")){
            kpiIndexFormulaObjService.remove(new LambdaQueryWrapper<KpiIndexFormulaObj>().eq(KpiIndexFormulaObj::getPlanCode, dto.getPlanCode()).eq(KpiIndexFormulaObj::getIndexCode, kpiIndexFormula.getIndexCode()).eq(KpiIndexFormulaObj::getFormulaId, dto.getFormulaId()));
        }

        //获取所有方案下的适用对象,删除已存在的,在此之前会提示前端是否覆盖
        if (StringUtils.isNotBlank(dto.getMemberIds())){
            List<KpiIndexFormulaObj> list1 = kpiIndexFormulaObjService.list(new LambdaQueryWrapper<KpiIndexFormulaObj>().eq(KpiIndexFormulaObj::getIndexCode, kpiIndexFormula.getIndexCode()).eq(KpiIndexFormulaObj::getPlanCode, dto.getPlanCode()));
            List<Long> ids = new ArrayList<>();
            if (dto.getMemberIds().equals("-100") || dto.getMemberIds().equals("-200")){
                if (StringUtils.isNotBlank(dto.getExcludePerson())){
                    list1 = list1.stream().filter(o->o.getPlanObj() != null && Arrays.asList(dto.getExcludePerson()).contains(o.getPlanObj())).collect(Collectors.toList());
                }
                if (StringUtils.isNotBlank(dto.getExcludeDept())){
                    list1 = list1.stream().filter(o->o.getPlanObj() != null && Arrays.asList(dto.getExcludeDept()).contains(o.getPlanObj())).collect(Collectors.toList());
                }
                ids = list1.stream().map(kpiIndexFormulaObj -> kpiIndexFormulaObj.getId()).collect(Collectors.toList());
            }else {
                ids = list1.stream().filter(s -> ListUtil.of(dto.getMemberIds().split(",")).contains(s.getPlanObj()==null?"":s.getPlanObj().toString())).map(kpiIndexFormulaObj -> kpiIndexFormulaObj.getId()).collect(Collectors.toList());
            }
            if (CollectionUtil.isNotEmpty(ids)){
                kpiIndexFormulaObjService.removeBatchByIds(ids);
            }
        }

        //同步适用对象到formulaObj
        if (StringUtils.isNotBlank(dto.getMemberIds())){
            KpiIndexFormula finalKpiIndexFormula = kpiIndexFormula;
            List<KpiIndexFormulaObj> kpiMembers = Arrays.stream(dto.getMemberIds().split(",")).map(s -> {
                KpiIndexFormulaObj kpiIndexFormulaObj = new KpiIndexFormulaObj();
                kpiIndexFormulaObj.setFormulaId(dto.getFormulaId());
                kpiIndexFormulaObj.setIndexCode(finalKpiIndexFormula.getIndexCode());
                kpiIndexFormulaObj.setPlanObj(Long.valueOf(s));
                kpiIndexFormulaObj.setPlanCode(dto.getPlanCode());
                kpiIndexFormulaObj.setTenantId(SecurityUtils.getUser().getTenantId());
                if (StringUtils.isBlank(dto.getMemberCategroyCodes()) && StringUtils.isBlank(dto.getPlanObjAccountType())){
                    kpiIndexFormulaObj.setExcludePerson(dto.getExcludePerson());
                    kpiIndexFormulaObj.setExcludeDept(dto.getExcludeDept());
                }
                return kpiIndexFormulaObj;
            }).collect(Collectors.toList());
            kpiIndexFormulaObjService.insertBatchSomeColumn(kpiMembers);
        }
        if (StringUtils.isNotBlank(dto.getMemberCategroyCodes())){
            KpiIndexFormula finalKpiIndexFormula = kpiIndexFormula;
            List<KpiIndexFormulaObj> kpiMembers = Arrays.stream(dto.getMemberCategroyCodes().split(",")).map(s -> {
                KpiIndexFormulaObj kpiIndexFormulaObj = new KpiIndexFormulaObj();
                kpiIndexFormulaObj.setFormulaId(dto.getFormulaId());
                kpiIndexFormulaObj.setIndexCode(finalKpiIndexFormula.getIndexCode());
                kpiIndexFormulaObj.setPlanObjCategory(s);
                kpiIndexFormulaObj.setPlanCode(dto.getPlanCode());
                kpiIndexFormulaObj.setTenantId(SecurityUtils.getUser().getTenantId());
                return kpiIndexFormulaObj;
            }).collect(Collectors.toList());
            kpiIndexFormulaObjService.insertBatchSomeColumn(kpiMembers);
        }
        if (StringUtils.isNotBlank(dto.getPlanObjAccountType())){
            KpiIndexFormula finalKpiIndexFormula = kpiIndexFormula;
            List<KpiIndexFormulaObj> kpiMembers = Arrays.stream(dto.getPlanObjAccountType().split(",")).map(s -> {
                KpiIndexFormulaObj kpiIndexFormulaObj = new KpiIndexFormulaObj();
                kpiIndexFormulaObj.setFormulaId(dto.getFormulaId());
                kpiIndexFormulaObj.setIndexCode(finalKpiIndexFormula.getIndexCode());
                kpiIndexFormulaObj.setPlanObjAccountType(s);
                kpiIndexFormulaObj.setPlanCode(dto.getPlanCode());
                kpiIndexFormulaObj.setTenantId(SecurityUtils.getUser().getTenantId());
                return kpiIndexFormulaObj;
            }).collect(Collectors.toList());
            kpiIndexFormulaObjService.insertBatchSomeColumn(kpiMembers);
        }
        if ((StringUtils.isNotBlank(dto.getExcludeDept())||StringUtils.isNotBlank(dto.getExcludePerson())) && (StringUtils.isNotBlank(dto.getMemberCategroyCodes())||StringUtils.isNotBlank(dto.getPlanObjAccountType()))){
            KpiIndexFormula finalKpiIndexFormula = kpiIndexFormula;
            List<KpiIndexFormulaObj> kpiMembers = Arrays.stream(dto.getPlanObjAccountType().split(",")).map(s -> {
                KpiIndexFormulaObj kpiIndexFormulaObj = new KpiIndexFormulaObj();
                kpiIndexFormulaObj.setFormulaId(dto.getFormulaId());
                kpiIndexFormulaObj.setIndexCode(finalKpiIndexFormula.getIndexCode());
                kpiIndexFormulaObj.setPlanCode(dto.getPlanCode());
                kpiIndexFormulaObj.setExcludeDept(dto.getExcludeDept());
                kpiIndexFormulaObj.setExcludePerson(dto.getExcludePerson());
                kpiIndexFormulaObj.setTenantId(SecurityUtils.getUser().getTenantId());
                return kpiIndexFormulaObj;
            }).collect(Collectors.toList());
            kpiIndexFormulaObjService.insertBatchSomeColumn(kpiMembers);
        }

            //子方案置为草稿
//            try {
//                List<Long> newMemberIds = Arrays.stream(dto.getMemberIds().split(",")).map(Long::valueOf).collect(Collectors.toList());
//                List<KpiAccountPlanChild> kpiAccountPlanChildren = effectPlanChild(newMemberIds, one, kpiIndexFormula);
//                if (CollectionUtil.isNotEmpty(kpiAccountPlanChildren)){
//                    for (KpiAccountPlanChild kpiAccountPlanChild : kpiAccountPlanChildren) {
//                        kpiAccountPlanChild.setStatus("-1");
//                        kpiAccountPlanChild.updateById();
//                    }
//                }
//            }catch (Exception e){
//                log.error("子方案置为草稿出错,{}",e);
//            }




    }


    @Override
    public List<DictDto> planSaveOrUpdateJudge(KpiIndexPlanMemberEditDto dto) {

        KpiIndexFormula kpiIndexFormula = getById(dto.getFormulaId());
        KpiIndex one = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, kpiIndexFormula.getIndexCode()));


        List<Long> ids = new ArrayList<>();


        if (one.getCaliber().equals(CaliberEnum.PEOPLE.getType())){
            ids = judegePeople(dto);
        }
        if (one.getCaliber().equals(CaliberEnum.DEPT.getType())){
            ids = judegeDept(dto);
        }

        if (CollectionUtil.isNotEmpty(ids)){


            //封装对象
            if (one.getCaliber().equals("1")){
                R<List<SysUser>> userList = remoteUserService.getUserList(ids);
                return userList.getData().stream().map(o->{
                    DictDto dictDto = new DictDto();
                    dictDto.setLabel(o.getName());
                    dictDto.setValue(o.getUserId().toString());
                    return dictDto;
                }).collect(Collectors.toList());
            }else if (one.getCaliber().equals("2")){
                List<KpiAccountUnit> list1 = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>().in(KpiAccountUnit::getId, ids));
                return list1.stream().map(o->{
                    DictDto dictDto = new DictDto();
                    dictDto.setValue(o.getId().toString());
                    dictDto.setLabel(o.getName());
                    return dictDto;
                }).collect(Collectors.toList());
            }else {
                return null;
            }
        }
        return null;
    }


    @Override
    public void del(Long id) {
        KpiIndexFormula one = getById(id);
        KpiIndex kpiIndex = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, one.getIndexCode()));
        if (kpiIndex.getType().equals("2")){
            List<KpiIndexFormula> list = list(new LambdaQueryWrapper<KpiIndexFormula>().eq(KpiIndexFormula::getIndexCode, one.getIndexCode()).eq(KpiIndexFormula::getFormulaGroup, one.getFormulaGroup()));
            kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>().in(KpiMember::getHostId, list.stream().map(o->o.getId()).collect(Collectors.toList())).eq(KpiMember::getMemberType, MemberEnum.FORMULA_ITEM.getType()));
            for (KpiIndexFormula kpiIndexFormula : list) {
                kpiIndexFormula.setDelFlag("1");
            }
            saveOrUpdateBatch(list);
        }else {
            one.setDelFlag("1");
            kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>().eq(KpiMember::getHostId, one.getId()).eq(KpiMember::getMemberType, MemberEnum.FORMULA_ITEM.getType()));
            one.updateById();
        }

        //指标存member_codes
        List<KpiIndexFormula> list1 = list(new LambdaQueryWrapper<KpiIndexFormula>().eq(KpiIndexFormula::getIndexCode, one.getIndexCode()));
        if (CollectionUtil.isNotEmpty(list1)){
            Set<FormulateMemberDto> list2 = new HashSet<>();
            for (KpiIndexFormula indexFormula : list1) {
                if (StringUtils.isNotBlank(indexFormula.getMemberCodes()) && indexFormula.getDelFlag().equals("0")){
                    List<FormulateMemberDto> list3 = JSONUtil.toList(indexFormula.getMemberCodes(), FormulateMemberDto.class);
                    list2.addAll(list3);
                }
            }
            kpiIndex.setMemberCodes(JSONUtil.toJsonStr(list2));
        }else {
            kpiIndex.setMemberCodes("");
        }
        kpiIndexFormulaObjService.remove(new LambdaQueryWrapper<KpiIndexFormulaObj>().eq(KpiIndexFormulaObj::getFormulaId, id));
        fillIndexMemberCodes(kpiIndex);
        kpiIndex.updateById();
    }

    @Override
    public KpiIndexFormulaInfoVO getCondInfo(String indexCode, Integer formulaGroup) {
        KpiIndexFormulaInfoVO vo = new KpiIndexFormulaInfoVO();
        List<KpiIndexFormula> list = list(new LambdaQueryWrapper<KpiIndexFormula>().eq(KpiIndexFormula::getIndexCode, indexCode).eq(KpiIndexFormula::getFormulaGroup, formulaGroup));
        if (CollectionUtil.isNotEmpty(list)){
            vo = BeanUtil.copyProperties(list.get(0), KpiIndexFormulaInfoVO.class);
            List<String> collect = list.stream().map(kpiIndexFormula -> kpiIndexFormula.getFormula()).collect(Collectors.toList());
            vo.setFormulas(collect);
        }
        return vo;
    }

    private double getPeriodMDay(String s) {


        if (s.length() != 6) {
            return 0d;
        }
        int month = Integer.parseInt(s.substring(4, 6));
        int year = Integer.parseInt(s.substring(0, 4));
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1); // 设置为指定年份和月份的第一天 因为Calendar的月份从0开始计数
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    @Override
    public String getResult(KpiIndex kpiIndex,String formulaOrigin,List<KpiFormulaParamsDto> list, String period) {
        //入参
        Map<String, Double> map = new HashMap<>();
        for (KpiFormulaParamsDto itemDto : list) {
            map.put(itemDto.getCode(), itemDto.getFieldValue() == null ? 1D : Double.valueOf(itemDto.getFieldValue()));
        }

        if (StringUtils.isNotBlank(period)) {
            formulaOrigin = formulaOrigin.replaceAll("MDAY", String.valueOf(getPeriodMDay(period)));
        }
        formulaOrigin = formulaOrigin.replaceAll("EQUITEMTPRICE", "1");
        formulaOrigin = formulaOrigin.replace("MAX(", "max(").replace("MIN(", "min(");
        String carryRule = kpiIndex.getCarryRule();
        carryRule = carryRule.equals("1") ? "四舍五入" : carryRule.equals("2") ? "向上取整" : carryRule.equals("3") ? "向下取整" : "";



        String s = ExpressionCheckHelper.checkAndCalculateKpi(map, formulaOrigin, kpiIndex.getIndexUnit(), kpiIndex.getReservedDecimal(), carryRule);
        return s;
    }

    @Override
    public String nocondVerify(KpiNocondFormulaVerifyDto dto) {
        KpiFormulaDto formulaDto = JSONUtil.toBean(dto.getFormula(), KpiFormulaDto.class);
        KpiIndex kpiIndex = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, dto.getIndexCode()));
        String formulaOrigin = formulaDto.getFormulaOrigin();
        List<KpiFormulaParamsDto> params = dto.getParams();
        String result = getResult(kpiIndex, formulaOrigin, params, "");
        return result;
    }

    @Override
    public String condVerify(KpiCondFormulaVerifyDto dto) {



        KpiIndex kpiIndex = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, dto.getIndexCode()));
        List<String> formulas = dto.getFormulas();

        //cond
        List<KpiFormulaCondition> conditionList = JSONUtil.toBean(formulas.get(0), KpiFormulaDto.class).getConditionList();

        Boolean flag = false;
        //对象适配哪个公式
        KpiFormulaDto formulaDto = null;
        for (String formula : formulas) {
            formulaDto = JSONUtil.toBean(formula, KpiFormulaDto.class);
            String value = formulaDto.getMemberList().get(0).getValue();
            //如果是-101,-102...指代条件中的科室人的
            CondFormulaMemberEnum condFormulaMemberEnum = CondFormulaMemberEnum.find(value);
            if (condFormulaMemberEnum != null) {

                KpiFormulaCondition condition = conditionList.stream().filter(o -> o.getKey().equals(condFormulaMemberEnum.getKey())).findFirst().orElse(null);
                if (condition == null){
                    throw new BizException("未指定条件");
                }
                if (!condition.getRelation().equals("等于")){
                    throw new BizException("条件中未指定对象");
                }
                if (condition.getValue().stream().map(o->o.getValue()).collect(Collectors.toList()).contains(dto.getMember())){
                    flag = true;
                }

            }else {
                if (value.equals(dto.getMember())){
                    flag = true;
                    break;
                }
            }
        }
        if (!flag){
            throw new BizException("未找到适配对象");
        }
        //根据条件找指标值
        LambdaQueryWrapper<KpiItemResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KpiItemResult::getPeriod, dto.getPeriod());
        for (KpiFormulaCondition condition : conditionList) {
            String key = condition.getKey();
            SFunction<KpiItemResult, Object> sFun = null;
            switch (key){
                case "zdys":sFun = KpiItemResult::getZdys; break;
                case "brks":sFun = KpiItemResult::getBrks; break;
                case "kzys":sFun = KpiItemResult::getKzys; break;
                case "zdysks":sFun = KpiItemResult::getZdysks; break;
                case "kzysks":sFun = KpiItemResult::getZdysks; break;
                case "kzyh":sFun = KpiItemResult::getKzyh; break;
                default: throw new BizException("条件错误");
            }

            if (condition.getRelation().equals("等于")){
                if (sFun != null){
                    wrapper.in(sFun,condition.getValue().stream().map(o->o.getValue()).collect(Collectors.toList()));
                }
            }else {
                if (sFun != null){
                    wrapper.ne(sFun,condition.getValue().stream().map(o->o.getValue()).collect(Collectors.toList()));
                }
            }
        }
        List<KpiItemResult> list = kpiItemResultService.list(wrapper);
        String formulaOrigin = formulaDto.getFormulaOrigin();
//        for (KpiFormulaItemDto itemDto : formulaDto.getFieldList()) {
//            formulaOrigin = formulaOrigin.replaceAll(itemDto.getCode(), itemDto.getFieldCode());
//        }
        List<KpiFormulaParamsDto> params = formulaDto.getFieldList().stream().map(o -> {
            double sum = list.stream().filter(kpiItemResult -> kpiItemResult.getCode().equals(o.getFieldCode())).mapToDouble(kpiItemResult -> Double.valueOf(kpiItemResult.getValue().toString())).sum();
            KpiFormulaParamsDto kpiFormulaParamsDto = new KpiFormulaParamsDto();
            kpiFormulaParamsDto.setCode(o.getCode());
            kpiFormulaParamsDto.setFieldValue(sum);
            return kpiFormulaParamsDto;
        }).collect(Collectors.toList());

        String result = getResult(kpiIndex, formulaOrigin, params, dto.getPeriod());
        return result;
    }

//    skip
    @Override
    public List<DictDto> childPlanJudge(KpiIndexPlanMemberEditDto dto) {
        List<Long> new_memberIds = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getMemberIds())){
             new_memberIds = Arrays.stream(dto.getMemberIds().split(",")).map(o->Long.valueOf(o)).collect(Collectors.toList());
        }
        KpiIndexFormula kpiIndexFormula = getById(dto.getFormulaId());
        KpiIndex one = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, kpiIndexFormula.getIndexCode()));

        //方案分组下的子方案
        List<String> collect1 = kpiAccountPlanService.list(new LambdaQueryWrapper<KpiAccountPlan>().eq(KpiAccountPlan::getCategoryCode, kpiIndexFormula.getPlanCode())).stream().map(o -> o.getPlanCode()).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(collect1)){
            return null;
        }


        List<KpiIndexFormulaObj> list = kpiIndexFormulaObjService.list(new LambdaQueryWrapper<KpiIndexFormulaObj>().eq(KpiIndexFormulaObj::getIndexCode, kpiIndexFormula.getIndexCode()).eq(KpiIndexFormulaObj::getPlanCode, dto.getPlanCode()).eq(KpiIndexFormulaObj::getFormulaId,dto.getFormulaId()));
        //求差集合
        if (CollectionUtil.isNotEmpty(list)){
            List<Long> collect = CollectionUtil.subtract(list.stream().map(o -> o.getPlanObj()).collect(Collectors.toList()), new_memberIds).stream().collect(Collectors.toList());
            //若在子计划中存在
            if (CollectionUtil.isNotEmpty(collect)){
                List<KpiAccountPlanChild> list2 = new ArrayList<>();
                List<Long> ids = new ArrayList<>();
                if (one.getCaliber().equals("1")){
                    list2 = kpiAccountPlanChildService.list(new LambdaQueryWrapper<KpiAccountPlanChild>().in(KpiAccountPlanChild::getUserId, collect)
                                    .eq(KpiAccountPlanChild::getIndexCode, one.getCode())
                                    .in(KpiAccountPlanChild::getPlanCode, collect1)
                                    .eq(KpiAccountPlanChild::getStatus, "0"));

                }else if (one.getCaliber().equals("2")){
                    list2 = kpiAccountPlanChildService.list(new LambdaQueryWrapper<KpiAccountPlanChild>().in(KpiAccountPlanChild::getDeptId, collect)
                            .eq(KpiAccountPlanChild::getIndexCode, one.getCode())
                            .in(KpiAccountPlanChild::getPlanCode, collect1)
                            .eq(KpiAccountPlanChild::getStatus, "0"));
                }


                if (one.getCaliber().equals("1")){
                    if (CollectionUtil.isNotEmpty(list2)){
                        ids = list2.stream().map(KpiAccountPlanChild::getUserId).collect(Collectors.toList());
                    }
                    R<List<SysUser>> userList = remoteUserService.getUserList(ids);
                    return userList.getData().stream().map(o->{
                        DictDto dictDto = new DictDto();
                        dictDto.setLabel(o.getName());
                        dictDto.setValue(o.getUserId().toString());
                        return dictDto;
                    }).collect(Collectors.toList());
                }else if (one.getCaliber().equals("2")){
                    if (CollectionUtil.isNotEmpty(list2)){
                        ids = list2.stream().map(KpiAccountPlanChild::getUserId).collect(Collectors.toList());
                    }
                    List<KpiAccountUnit> list1 = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>().in(KpiAccountUnit::getId, ids));
                    return list1.stream().map(o->{
                        DictDto dictDto = new DictDto();
                        dictDto.setValue(o.getId().toString());
                        dictDto.setLabel(o.getName());
                        return dictDto;
                    }).collect(Collectors.toList());
                }else {
                    return null;
                }
            }

        }


        return null;
    }

    @Override
    public void jzmember() {
        List<KpiIndex> list = kpiIndexService.list();
        for (KpiIndex kpiIndex : list) {
//            if (kpiIndex.getDelFlag().equals("1")){
//                //打formula 标记 删除member表
//                List<KpiIndexFormula> list1 = list(new LambdaQueryWrapper<KpiIndexFormula>().eq(KpiIndexFormula::getIndexCode, kpiIndex.getCode()));
////                list1.stream().map(kpiIndexFormula -> kpiIndexFormula.setDelFlag("1"));
////                updateBatchById(list1);
//                if (CollectionUtil.isNotEmpty(list1)){
//                    kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>().in(KpiMember::getHostId, list1.stream().map(kpiIndexFormula -> kpiIndexFormula.getId()).collect(Collectors.toList())).eq(KpiMember::getMemberType, MemberEnum.FORMULA_ITEM.getType()));
//                }
//            }
//            //指标存member_codes
//            List<KpiIndexFormula> list1 = list(new LambdaQueryWrapper<KpiIndexFormula>().eq(KpiIndexFormula::getIndexCode, kpiIndex.getCode()));
//            if (CollectionUtil.isNotEmpty(list1)){
//                Set<FormulateMemberDto> list2 = new HashSet<>();
//                for (KpiIndexFormula indexFormula : list1) {
//                    if (indexFormula.getDelFlag().equals("1")){
//                        kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>().eq(KpiMember::getHostId, indexFormula.getId()).eq(KpiMember::getMemberType, MemberEnum.FORMULA_ITEM.getType()));
//                    }
//                    if (StringUtils.isNotBlank(indexFormula.getMemberCodes()) && indexFormula.getDelFlag().equals("0")){
//                        List<FormulateMemberDto> list3 = JSONUtil.toList(indexFormula.getMemberCodes(), FormulateMemberDto.class);
//                        list2.addAll(list3);
//                    }
//                }
//                kpiIndex.setMemberCodes(JSONUtil.toJsonStr(list2));
//                kpiIndex.updateById();
//            }else {
//                kpiIndex.setMemberCodes("");
//                kpiIndex.updateById();
//            }

            //分摊
//            if (kpiIndex.getType().equals("3")&&kpiIndex.getDelFlag().equals("0")){
//                kpiAllocationRuleService.fillIndexMemberCodes(kpiIndex);
//                kpiIndex.updateById();
//            }

            if (StringUtils.equalsAnyIgnoreCase(kpiIndex.getType(), "1","2")){
                fillIndexMemberCodes(kpiIndex);
                kpiIndex.updateById();
            }

        }
//        List<KpiAllocationRule> list1 = kpiAllocationRuleService.list();
//        for (KpiAllocationRule kpiAllocationRule : list1) {
//            kpiAllocationRuleService.fillMemberCodes(kpiAllocationRule);
//            kpiAllocationRule.updateById();
//        }

        List<KpiIndexFormula> list1 = list(new LambdaQueryWrapper<KpiIndexFormula>().eq(KpiIndexFormula::getDelFlag, "0"));
        for (KpiIndexFormula kpiIndexFormula : list1) {
            fillMemberCodes(kpiIndexFormula);
            kpiIndexFormula.updateById();
        }


    }

//    skpi
    @Override
    public KpiFindObject findObject(Long formulaId,Long planId, Long memberId) {
        KpiIndexFormula formula = getById(formulaId);
        KpiIndex kpiIndex = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, formula.getIndexCode()));

//        KpiAccountPlanChild planChild = kpiAccountPlanChildService.getById(planId);
//        KpiAccountPlan plan = kpiAccountPlanService.getOne(new LambdaQueryWrapper<KpiAccountPlan>().eq(KpiAccountPlan::getPlanCode, planChild.getPlanCode()));
        KpiAccountPlan plan = kpiAccountPlanService.getById(planId);
        KpiCategory category = kpiCategoryService.getOne(new LambdaQueryWrapper<KpiCategory>().eq(KpiCategory::getCategoryCode, plan.getCategoryCode()));

//        String planCaliber = planChild.getObject();
        String planCaliber = kpiIndex.getCaliber();
        Long myObjId = null;
        String myObjName = null;
        List<Long> otherObjIds = new ArrayList<>();
        List<String> otherObjNames = new ArrayList<>();

        List<String> allObjnames = new ArrayList<>();


        switch (planCaliber){
            case "1": myObjId = memberId; break;
            case "2": myObjId = memberId; break;
            default: throw new BizException("颗粒度未在范围内");
        }

        List<KpiIndexFormulaObj> formulaObjs = kpiIndexFormulaObjService.list(new LambdaQueryWrapper<KpiIndexFormulaObj>().eq(KpiIndexFormulaObj::getFormulaId, formulaId).eq(KpiIndexFormulaObj::getPlanCode, category.getCategoryCode()));
        List<Long> collect = formulaObjs.stream().filter(o->o.getPlanObj() != null).map(o -> o.getPlanObj()).collect(Collectors.toList());
        List<Long> categories = formulaObjs.stream().filter(o->StringUtils.isNotBlank(o.getPlanObjCategory())).map(o -> o.getPlanObj()).collect(Collectors.toList());

        if (CollectionUtil.isEmpty(collect)){
            return new KpiFindObject();
        }
        switch (planCaliber){
            case "1":
                R<List<SysUser>> userList = remoteUserService.getUserList(collect);
                allObjnames = userList.getData().stream().map(o->o.getName()).collect(Collectors.toList());
                Long finalMyObjId = myObjId;
                myObjName = userList.getData().stream().filter(o->o.getUserId().equals(finalMyObjId)).map(o->o.getName()).findFirst().orElse(null);
                break;
            case "2":
                List<KpiAccountUnit> kpiAccountUnits = kpiAccountUnitService.listByIds(collect);
                allObjnames = kpiAccountUnits.stream().map(o -> o.getName()).collect(Collectors.toList());
                Long finalMyObjId1 = myObjId;
                myObjName = kpiAccountUnits.stream().filter(o -> o.getId().equals(finalMyObjId1)).map(o -> o.getName()).findFirst().orElse(null);
                break;
        }
        allObjnames.remove(myObjName);
        otherObjNames = allObjnames;

        KpiFindObject findObject = new KpiFindObject();
        findObject.setThisPlanObj(myObjName);
        findObject.setOtherObj(CollectionUtil.join(otherObjNames, ","));


        return findObject;
    }

    @Override
    public AllowCopyVo allowCopy(String planCode, String indexCode, Long planObj) {
        AllowCopyVo allowCopyVo = new AllowCopyVo();
        allowCopyVo.setAllow(true);
        KpiIndex kpiIndex = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, indexCode));
        if (kpiIndex.getCaliber().equals("1")){
            //获取组别
            List<KpiPlanCacheDto.ObjCategory> objCategories = kpiAccountPlanChildMapper.getObjCategoryByUserId(planObj);
            //判断此组别是否在 相同指标方案的公式下存在,有则不允许,提示
            if (CollectionUtil.isNotEmpty(objCategories)){
                List<KpiIndexFormulaObj> formulaObjs = kpiIndexFormulaObjService.list(new LambdaQueryWrapper<KpiIndexFormulaObj>()
                        .eq(KpiIndexFormulaObj::getPlanCode, planCode)
                        .eq(KpiIndexFormulaObj::getIndexCode, indexCode)
                        .in(KpiIndexFormulaObj::getPlanObjCategory, objCategories.stream().map(o->o.getCategoryCode()).collect(Collectors.toList())));
                if (CollectionUtil.isNotEmpty(formulaObjs)){
                    allowCopyVo.setAllow(false);
                    List<AllowCopyVo.NotAllowCategory> collect = formulaObjs.stream().map(o -> {
                        KpiCategory category = kpiCategoryService.getOne(new LambdaQueryWrapper<KpiCategory>().eq(KpiCategory::getCategoryCode, o.getPlanObjCategory()));
                        AllowCopyVo.NotAllowCategory notAllowCategory = new AllowCopyVo.NotAllowCategory();
                        notAllowCategory.setCategory(category.getCategoryName());
                        return notAllowCategory;
                    }).collect(Collectors.toList());
                    allowCopyVo.setNotAllowCategoryList(collect);
                }

            }

        }
        return allowCopyVo;
    }


    public void updateDelFlag(List<Long> ids) {
        if (CollectionUtil.isNotEmpty(ids)){
            List<List<Long>> split = CollectionUtil.split(ids, 500);
            for (List<Long> list : split) {
                kpiIndexFormulaMapper.updateDelFlag(list);
            }
        }
    }



    @Override
    public void copyFor(ForCopyDto dto) {
        if(dto.getPlanCode().equals(dto.getOldPlanCode()))
        {
            throw new BizException("不能复制同一个方案");
        }
        List<KpiIndexFormula> oldFormulas = this.list(new LambdaQueryWrapper<KpiIndexFormula>()
                .eq(KpiIndexFormula::getPlanCode, dto.getOldPlanCode()).eq(KpiIndexFormula::getDelFlag, 0).in(KpiIndexFormula::getId, dto.getId()));
        Linq.of(oldFormulas).forEach(o -> {
            o.setId(null);
            o.setPlanCode(dto.getPlanCode());
            this.save(o);
        });
    }
}
