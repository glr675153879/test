package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.JsonObject;
import com.hscloud.hs.cost.account.constant.enums.kpi.MemberEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiIndexMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostClusterUnit;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.pojo.AllocateUnitInfo;
import com.hscloud.hs.cost.account.model.pojo.UnitInfo;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAllocationRuleListVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAllocationRuleVO;
import com.hscloud.hs.cost.account.service.impl.dataReport.CostClusterUnitService;
import com.hscloud.hs.cost.account.service.kpi.KpiAccountUnitService;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiAllocationRuleMapper;
import com.hscloud.hs.cost.account.service.kpi.IKpiAllocationRuleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
* 分摊公式表 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class KpiAllocationRuleService extends ServiceImpl<KpiAllocationRuleMapper, KpiAllocationRule> implements IKpiAllocationRuleService {

    @Autowired
    private KpiMemberService kpiMemberService;
    @Autowired
    private RemoteUserService remoteUserService;
    @Autowired
    private KpiIndexService kpiIndexService;
    @Autowired
    private KpiItemService kpiItemService;
    @Autowired
    private KpiAccountUnitService kpiAccountUnitService;
    @Autowired
    private KpiImputationService kpiImputationService;
    @Autowired
    private KpiCategoryService kpiCategoryService;
    @Autowired
    private KpiAllocationRuleMapper kpiAllocationRuleMapper;
    @Autowired
    private CostClusterUnitService costClusterUnitService;


    @Override
    public void saveOrUpdate(KpiAllocationRuleDto dto) {
        KpiAllocationRule kpiAllocationRule = new KpiAllocationRule();
        List<FormulateMemberDto> memberDtos = new ArrayList<>();
        //保存
        if (dto.getId() != null){
            kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>()
                    .in(KpiMember::getMemberType, ListUtil.of(MemberEnum.ALLOCATION_RULE_ITEM_F.getType(),MemberEnum.ALLOCATION_RULE_ITEM_X.getType(),MemberEnum.ALLOCATION_RULE_ITEM_Z.getType()
                    ,MemberEnum.IN_MEMBERS_EMP.getType(),MemberEnum.IN_MEMBERS_DEPT.getType(),MemberEnum.OUT_MEMBERS_IMP.getType(),MemberEnum.OUT_MEMBERS_EMP.getType(),MemberEnum.OUT_MEMBERS_DEPT.getType(),
                            MemberEnum.OUT_MEMBER_DEPT_GROUP.getType(), MemberEnum.OUT_MEMBER_DEPT_EXCEPT.getType()))
            .eq(KpiMember::getHostId, dto.getId()));
        }
        kpiAllocationRule = BeanUtil.copyProperties(dto, KpiAllocationRule.class);
        kpiAllocationRule.setDelFlag("0");
        fillMemberCodes(kpiAllocationRule);
        saveOrUpdate(kpiAllocationRule);

        KpiIndex kpiIndex = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, kpiAllocationRule.getIndexCode()));
        fillIndexMemberCodes(kpiIndex);
        kpiIndex.updateById();

        KpiAllocationRule finalKpiAllocationRule = kpiAllocationRule;
        List<KpiMember> list = new ArrayList<>();
        //关联摊入人员
        if (StringUtils.isNotBlank(dto.getInMembersEmp())){
            List<KpiMember> inMemberEmp = Arrays.stream(dto.getInMembersEmp().split(",")).map(s -> {
                KpiMember kpiMember = new KpiMember();
                kpiMember.setPeriod(0L);
                kpiMember.setHostId(finalKpiAllocationRule.getId());
                kpiMember.setMemberId(Long.valueOf(s));
                kpiMember.setMemberType(MemberEnum.IN_MEMBERS_EMP.getType());
                kpiMember.setBusiType("1");
                kpiMember.setTenantId(1L);
                return kpiMember;
            }).collect(Collectors.toList());
            list.addAll(inMemberEmp);
        }
        //关联摊入科室
        if (StringUtils.isNotBlank(dto.getInMembersDept())){
            List<KpiMember> list1 = Arrays.stream(dto.getInMembersDept().split(",")).map(s -> {
                KpiMember kpiMember = new KpiMember();
                kpiMember.setPeriod(0L);
                kpiMember.setHostId(finalKpiAllocationRule.getId());
                kpiMember.setMemberId(Long.valueOf(s));
                kpiMember.setMemberType(MemberEnum.IN_MEMBERS_DEPT.getType());
                kpiMember.setBusiType("1");
                return kpiMember;
            }).collect(Collectors.toList());
            list.addAll(list1);
        }
        //关联摊入归集
        if (StringUtils.isNotBlank(dto.getOutMembersImp())){
            List<KpiMember> list1 = Arrays.stream(dto.getOutMembersImp().split(",")).map(s -> {
                KpiMember kpiMember = new KpiMember();
                kpiMember.setPeriod(0L);
                kpiMember.setHostId(finalKpiAllocationRule.getId());
                kpiMember.setMemberId(Long.valueOf(s));
                kpiMember.setMemberType(MemberEnum.OUT_MEMBERS_IMP.getType());
                kpiMember.setBusiType("1");
                return kpiMember;
            }).collect(Collectors.toList());
            list.addAll(list1);
        }
        //关联摊出人员
        if (StringUtils.isNotBlank(dto.getOutMembersEmp())){
            List<KpiMember> list1 = Arrays.stream(dto.getOutMembersEmp().split(",")).map(s -> {
                KpiMember kpiMember = new KpiMember();
                kpiMember.setPeriod(0L);
                kpiMember.setHostId(finalKpiAllocationRule.getId());
                kpiMember.setMemberId(Long.valueOf(s));
                kpiMember.setMemberType(MemberEnum.OUT_MEMBERS_EMP.getType());
                kpiMember.setBusiType("1");
                return kpiMember;
            }).collect(Collectors.toList());
            list.addAll(list1);
        }
        //关联摊出科室 组 剔除
        if (StringUtils.isNotBlank(dto.getOutMembersDept())){
            OutMembersDetpDto outMembersDetpDto = JSONUtil.toBean(dto.getOutMembersDept(), OutMembersDetpDto.class);

            if (CollectionUtil.isNotEmpty(outMembersDetpDto.getOut_dept())){
                List<KpiMember> list1 = outMembersDetpDto.getOut_dept().stream().map(s -> {
                    KpiMember kpiMember = new KpiMember();
                    kpiMember.setPeriod(0L);
                    kpiMember.setHostId(finalKpiAllocationRule.getId());
                    kpiMember.setMemberId(Long.valueOf(s));
                    kpiMember.setMemberType(MemberEnum.OUT_MEMBERS_DEPT.getType());
                    kpiMember.setBusiType("1");
                    return kpiMember;
                }).collect(Collectors.toList());
                list.addAll(list1);
            }
//组
            if (CollectionUtil.isNotEmpty(outMembersDetpDto.getOut_dept_group())){
                List<KpiMember> list1 = outMembersDetpDto.getOut_dept_group().stream().map(s -> {
                    KpiMember kpiMember = new KpiMember();
                    kpiMember.setPeriod(0L);
                    kpiMember.setHostId(finalKpiAllocationRule.getId());
                    kpiMember.setMemberCode(s);
                    kpiMember.setMemberType(MemberEnum.OUT_MEMBER_DEPT_GROUP.getType());
                    kpiMember.setBusiType("1");
                    return kpiMember;
                }).collect(Collectors.toList());
                list.addAll(list1);
            }

            if (CollectionUtil.isNotEmpty(outMembersDetpDto.getOut_dept_except())){
                List<KpiMember> list1 = outMembersDetpDto.getOut_dept_except().stream().map(s -> {
                    KpiMember kpiMember = new KpiMember();
                    kpiMember.setPeriod(0L);
                    kpiMember.setHostId(finalKpiAllocationRule.getId());
                    kpiMember.setMemberId(Long.valueOf(s));
                    kpiMember.setMemberType(MemberEnum.OUT_MEMBER_DEPT_EXCEPT.getType());
                    kpiMember.setBusiType("1");
                    return kpiMember;
                }).collect(Collectors.toList());
                list.addAll(list1);
            }


        }

        //关联指标
        if (CollectionUtil.isNotEmpty(dto.getKpiFormulaItemDtos())){
            for (KpiFormulaItemDto kpiFormulaItemDto : dto.getKpiFormulaItemDtos()) {
                String memberType = "";
                switch (kpiFormulaItemDto.getFieldType()){
                    case "item": memberType = MemberEnum.ALLOCATION_RULE_ITEM_X.getType();break;
                    case "index": memberType = MemberEnum.ALLOCATION_RULE_ITEM_Z.getType();break;
                    case "allocation": memberType = MemberEnum.ALLOCATION_RULE_ITEM_F.getType();break;
                    default:throw new BizException("类型不支持");
                }

                KpiMember kpiMember = new KpiMember();
                //一个公式多个公式项
                kpiMember.setHostId(finalKpiAllocationRule.getId());
                kpiMember.setHostCode("");
                kpiMember.setPeriod(0L);
                kpiMember.setMemberCode(kpiFormulaItemDto.getFieldCode());
                kpiMember.setMemberType(memberType);
                kpiMember.setTenantId(1L);
                kpiMember.setBusiType("1");
                list.add(kpiMember);
            }
        }


        kpiMemberService.insertBatchSomeColumn(list);
    }

    public List<FormulateMemberDto> fillIndexMemberCodes(KpiIndex kpiIndex){
        List<KpiAllocationRule> list = list(new LambdaQueryWrapper<KpiAllocationRule>().eq(KpiAllocationRule::getIndexCode, kpiIndex.getCode()).eq(KpiAllocationRule::getDelFlag,"0"));
        List<FormulateMemberDto> result = new ArrayList<>();
        for (KpiAllocationRule kpiAllocationRule : list) {
            List<FormulateMemberDto> list1 = fillMemberCodes(kpiAllocationRule);
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

    public List<FormulateMemberDto> fillMemberCodes(KpiAllocationRule kpiAllocationRule){
        List<FormulateMemberDto> list = new ArrayList<>();
        if (StringUtils.isNotBlank(kpiAllocationRule.getFormula())){
            JSONObject jsonObject = JSONUtil.toBean(kpiAllocationRule.getFormula(), JSONObject.class);
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
        if (StringUtils.isNotBlank(kpiAllocationRule.getAllocationItems())){
            List<FormulateMemberDto> collect = Arrays.stream(kpiAllocationRule.getAllocationItems().split(",")).map(o -> {
                FormulateMemberDto formulateMemberDto = new FormulateMemberDto();
                formulateMemberDto.setFieldCode(o);
                formulateMemberDto.setFieldType("item");
                return formulateMemberDto;
            }).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(collect)){
                list.addAll(collect);
            }
        }
        if (StringUtils.isNotBlank(kpiAllocationRule.getAllocationIndexs())){
            List<FormulateMemberDto> collect = Arrays.stream(kpiAllocationRule.getAllocationIndexs().split(",")).map(o -> {
                FormulateMemberDto formulateMemberDto = new FormulateMemberDto();
                formulateMemberDto.setFieldCode(o);
                formulateMemberDto.setFieldType(o.startsWith("f")?"allocation":"index");
                return formulateMemberDto;
            }).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(collect)){
                list.addAll(collect);
            }
        }
        if (CollectionUtil.isNotEmpty(list)){
            list = list.stream().distinct().collect(Collectors.toList());
            kpiAllocationRule.setMemberCodes(JSONUtil.toJsonStr(list));
        }else {
            kpiAllocationRule.setMemberCodes("");
        }
        return list;
    }

    @Override
    public IPage<KpiAllocationRuleListVO> getRulePage(KpiAllocationRuleListDto dto) {
        IPage<KpiAllocationRuleListVO> page = kpiAllocationRuleMapper.getPage(new Page<KpiAllocationRule>(dto.getCurrent(), dto.getSize()), dto);
        //获取所有相关人员、指标项、指标、科室、归集
        List<Long> userIds = new ArrayList<>();
        List<SysUser> users = new ArrayList<>();

        List<String> indexItems = new ArrayList<>();
        List<KpiItem> kpiItems = new ArrayList<>();

        List<String> indexCodes = new ArrayList<>();
        List<KpiIndex> kpiIndices = new ArrayList<>();

        List<Long> deptIds = new ArrayList<>();
        List<KpiAccountUnit> depts = new ArrayList<>();

//        医护关系
        Map<String, List<Long>> yhgx = new HashMap<>();
        List<KpiAllocationRuleListVO.Yhgx> yhgxs = new ArrayList<>();

//是分组code
        List<Long> impIds = new ArrayList<>();
        List<CostClusterUnit> imputations = new ArrayList<>();

        List<SysDictItem> groups = kpiIndexMapper.getSysDict(null, "kpi_calculate_grouping",SecurityUtils.getUser().getTenantId());

        List<KpiAllocationRuleListVO> records = page.getRecords();
        if (page != null && CollectionUtil.isNotEmpty(records)){
            for (KpiAllocationRuleListVO record : page.getRecords()) {
                if (StringUtils.isNotBlank(record.getAllocationIndexs())){
                    indexCodes.addAll(Arrays.asList(record.getAllocationIndexs().split(",")));
                }
                if (StringUtils.isNotBlank(record.getAllocationItems())){
                    indexItems.addAll(Arrays.stream(record.getAllocationItems().split(",")).collect(Collectors.toList()));
                }
                if (StringUtils.isNotBlank(record.getOutMembersEmp())){
                    userIds.addAll(Arrays.stream(record.getOutMembersEmp().split(",")).map(s -> Long.valueOf(s)).collect(Collectors.toList()));
                }
                if (StringUtils.isNotBlank(record.getOutMembersDept())){
                    if (!record.getOutMembersDept().startsWith("{")){
                        deptIds.addAll(Arrays.stream(record.getOutMembersDept().split(",")).map(s -> Long.valueOf(s)).collect(Collectors.toList()));
                    }else {
                        OutMembersDetpDto outMembersDetpDto = JSONUtil.toBean(record.getOutMembersDept(), OutMembersDetpDto.class);
                        if (CollectionUtil.isNotEmpty(outMembersDetpDto.getOut_dept())){
                            deptIds.addAll(outMembersDetpDto.getOut_dept());
                        }
                        if (CollectionUtil.isNotEmpty(outMembersDetpDto.getOut_dept_except())){
                            deptIds.addAll(outMembersDetpDto.getOut_dept_except());
                        }
                    }

                }
                if (StringUtils.isNotBlank(record.getInMembersEmp())){
                    userIds.addAll(Arrays.stream(record.getInMembersEmp().split(",")).map(s -> Long.valueOf(s)).collect(Collectors.toList()));
                }
                if (StringUtils.isNotBlank(record.getInMembersDept())){
                    deptIds.addAll(Arrays.stream(record.getInMembersDept().split(",")).map(s -> Long.valueOf(s)).collect(Collectors.toList()));
                }
                if (StringUtils.isNotBlank(record.getOutMembersImp())){
                    impIds.addAll(Arrays.stream(record.getOutMembersImp().split(",")).map(s->Long.valueOf(s)).collect(Collectors.toList()));
                }
//                if (record.getType().equals("2")){
//                    yhgx.put(record.getDocCode(), Arrays.stream(record.getOutMembersDept().split(",")).map(s -> Long.valueOf(s)).collect(Collectors.toList()));
//                }
            }
            if (CollectionUtil.isNotEmpty(userIds)){
                R<List<SysUser>> userList = remoteUserService.getUserList(userIds);
                users = userList.getData();
            }
            if (CollectionUtil.isNotEmpty(indexItems)){
                kpiItems = kpiItemService.list(new LambdaQueryWrapper<KpiItem>().in(KpiItem::getCode, indexItems).eq(KpiItem::getBusiType, "1"));
            }
            if (CollectionUtil.isNotEmpty(indexCodes)){
                kpiIndices = kpiIndexService.list(new LambdaQueryWrapper<KpiIndex>().in(KpiIndex::getCode, indexCodes));
            }
            if (CollectionUtil.isNotEmpty(deptIds)){
                depts = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>().in(KpiAccountUnit::getId, deptIds));
            }
            if (CollectionUtil.isNotEmpty(impIds)){
                imputations = costClusterUnitService.list(new LambdaQueryWrapper<CostClusterUnit>().in(CostClusterUnit::getId, impIds));
            }
            if (CollectionUtil.isNotEmpty(yhgx.keySet())){
                for (String docCode : yhgx.keySet()) {
                    List<KpiAllocationRuleListVO.Yhgx> yhgx1 = kpiAllocationRuleMapper.getYhgx(docCode, yhgx.get(docCode));
                    yhgxs.addAll(yhgx1);
                }
            }
            //获取分摊指标
            for (KpiAllocationRuleListVO record : records) {
                List<String> allocationItemResult = new ArrayList<>();
                List<String> outMemberResult = new ArrayList<>();
                List<String> exceptResult = new ArrayList<>();
                List<String> imMemberResult = new ArrayList<>();

                if (CollectionUtil.isNotEmpty(kpiIndices)&&StringUtils.isNotBlank(record.getAllocationIndexs())){
                    List<String> collect = kpiIndices.stream().filter(k -> Arrays.asList(record.getAllocationIndexs().split(",")).contains(k.getCode())).map(k->k.getName()).collect(Collectors.toList());
                    allocationItemResult.addAll(collect);
                }
                if (CollectionUtil.isNotEmpty(kpiItems)&&StringUtils.isNotBlank(record.getAllocationItems())){
                    List<String> collect = kpiItems.stream().filter(kpiItem -> Arrays.asList(record.getAllocationItems().split(",")).contains(kpiItem.getCode())).map(k->k.getItemName()).collect(Collectors.toList());
                    allocationItemResult.addAll(collect);
                }
                if (CollectionUtil.isNotEmpty(users)&&StringUtils.isNotBlank(record.getOutMembersEmp())){
                    List<String> collect = users.stream().filter(sysUser -> Arrays.asList(record.getOutMembersEmp().split(",")).contains(sysUser.getUserId().toString())).map(sysUser -> sysUser.getName()).collect(Collectors.toList());
                    outMemberResult.addAll(collect);
                }
                if (CollectionUtil.isNotEmpty(depts)&&StringUtils.isNotBlank(record.getOutMembersDept())){
                    if (!record.getOutMembersDept().startsWith("{")){
                        List<String> collect = depts.stream().filter(d -> Arrays.asList(record.getOutMembersDept().split(",")).contains(d.getId().toString())).map(d->d.getName()).collect(Collectors.toList());
                        outMemberResult.addAll(collect);
                    }else {
                        OutMembersDetpDto outMembersDetpDto = JSONUtil.toBean(record.getOutMembersDept(), OutMembersDetpDto.class);
                        if (CollectionUtil.isNotEmpty(outMembersDetpDto.getOut_dept_group())){
                            List<String> collect = groups.stream().filter(o -> outMembersDetpDto.getOut_dept_group().contains(o.getItemValue())).map(o -> o.getLabel()).collect(Collectors.toList());
                            outMemberResult.addAll(collect);
                        }
                        if (CollectionUtil.isNotEmpty(outMembersDetpDto.getOut_dept())){
                            List<String> collect = depts.stream().filter(d -> outMembersDetpDto.getOut_dept().contains(d.getId())).map(d->d.getName()).collect(Collectors.toList());
                            outMemberResult.addAll(collect);
                        }
                        if (CollectionUtil.isNotEmpty(outMembersDetpDto.getOut_dept_except())){
                            List<String> collect = depts.stream().filter(d -> outMembersDetpDto.getOut_dept_except().contains(d.getId())).map(d->d.getName()).collect(Collectors.toList());
                            exceptResult.addAll(collect);
                        }

                    }

                }
                if (CollectionUtil.isNotEmpty(users)&&StringUtils.isNotBlank(record.getInMembersEmp())){
                    List<String> collect = users.stream().filter(d -> Arrays.asList(record.getInMembersEmp().split(",")).contains(d.getUserId().toString())).map(d->d.getName()).collect(Collectors.toList());
                    imMemberResult.addAll(collect);
                }
                if (CollectionUtil.isNotEmpty(depts)&&StringUtils.isNotBlank(record.getInMembersDept())){
                    List<String> collect = depts.stream().filter(d -> Arrays.asList(record.getInMembersDept().split(",")).contains(d.getId().toString())).map(d->d.getName()).collect(Collectors.toList());
                    imMemberResult.addAll(collect);
                }
                if (CollectionUtil.isNotEmpty(imputations)&&StringUtils.isNotBlank(record.getOutMembersImp())){
                    List<CostClusterUnit> collect1 = imputations.stream().filter(d -> Arrays.asList(record.getOutMembersImp().split(",")).contains(d.getId().toString())).collect(Collectors.toList());
                    List<String> collect = collect1.stream().map(d->d.getName()).collect(Collectors.toList());
                    outMemberResult.addAll(collect);

                    List<String> ls = new ArrayList<>();
                    for (CostClusterUnit costClusterUnit : collect1) {
                        if (StringUtils.isNotBlank(costClusterUnit.getUnits())){
                            List<JSONObject> list1 = JSONUtil.toList(costClusterUnit.getUnits(), JSONObject.class);
                            for (JSONObject jo : list1) {
                                ls.add(jo.get("name").toString());
                            }
                        }
                    }
                    imMemberResult.addAll(ls);

                }
                if (StringUtils.isNotBlank(record.getDocCode()) && CollectionUtil.isNotEmpty(yhgxs)){
                    List<String> collect = yhgxs.stream()
                            .filter(o -> o.getDocCode().equals(record.getDocCode()) && Arrays.asList(record.getOutMembersDept().split(",")).contains(o.getDocAccountId().toString()))
                            .map(o->o.getNurseAccountName())
                            .collect(Collectors.toList());
                    imMemberResult.addAll(collect);
                }
                record.setAllocationItem(StringUtil.join(allocationItemResult.stream().distinct().collect(Collectors.toList()), ","));
                record.setOutMember(StringUtil.join(outMemberResult.stream().distinct().collect(Collectors.toList()), ","));
                if (CollectionUtil.isNotEmpty(exceptResult)){
                    record.setOutMember(record.getOutMember()+"  剔除:"+StringUtil.join(exceptResult.stream().distinct().collect(Collectors.toList()), ","));
                }
                record.setImMember(StringUtil.join(imMemberResult.stream().distinct().collect(Collectors.toList()), ","));
            }
        }
        return page;
    }

    public static void main(String[] args) {
        List<String> a = new ArrayList<>();
        a.add("1");
        System.out.println(StringUtil.join(a,","));
    }

    @Override
    public void del(Long id) {
        KpiAllocationRule byId = getById(id);
        byId.setDelFlag("1");
        updateById(byId);

        //更新indexmember
        KpiIndex kpiIndex = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, byId.getIndexCode()));
        fillIndexMemberCodes(kpiIndex);
        kpiIndex.updateById();
//        删除member
        kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>()
                .in(KpiMember::getMemberType, ListUtil.of(MemberEnum.ALLOCATION_RULE_ITEM_F.getType(),MemberEnum.ALLOCATION_RULE_ITEM_X.getType(),MemberEnum.ALLOCATION_RULE_ITEM_Z.getType()
                        ,MemberEnum.IN_MEMBERS_EMP.getType(),MemberEnum.IN_MEMBERS_DEPT.getType(),MemberEnum.OUT_MEMBERS_IMP.getType(),MemberEnum.OUT_MEMBERS_EMP.getType(),MemberEnum.OUT_MEMBERS_DEPT.getType()))
                .eq(KpiMember::getHostId, id));
    }

    @Autowired
    private KpiIndexMapper kpiIndexMapper;

    @Override
    public KpiAllocationRuleVO info(Long id) {
        KpiAllocationRule byId = getById(id);
        KpiAllocationRuleVO record = BeanUtil.copyProperties(byId, KpiAllocationRuleVO.class);

        if (StringUtils.isNotBlank(record.getAllocationIndexs())){
            List<UnitInfo> allocationIndexsJson = kpiIndexService.list(new LambdaQueryWrapper<KpiIndex>().in(KpiIndex::getCode, record.getAllocationIndexs().split(","))).stream().map(o->{
                UnitInfo unitInfo = new UnitInfo();
                unitInfo.setLabel(o.getName());
                unitInfo.setValue(o.getCode());
                return unitInfo;
            }).collect(Collectors.toList());
            record.setAllocationIndexsJson(allocationIndexsJson);
        }
        if (StringUtils.isNotBlank(record.getAllocationItems())){
            List<UnitInfo> allocationItemsJson = kpiItemService.list(new LambdaQueryWrapper<KpiItem>().in(KpiItem::getCode, record.getAllocationItems().split(",")).eq(KpiItem::getBusiType, "1")).stream().map(o->{
                UnitInfo unitInfo = new UnitInfo();
                unitInfo.setLabel(o.getItemName());
                unitInfo.setValue(o.getCode());
                unitInfo.setStatus(o.getStatus());
                unitInfo.setDelFlag(o.getDelFlag());
                return unitInfo;
            }).collect(Collectors.toList());
            record.setAllocationItemsJson(allocationItemsJson);
        }
        if (StringUtils.isNotBlank(record.getOutMembersEmp())){
            R<List<SysUser>> userList = remoteUserService.getUserList(Arrays.stream(record.getOutMembersEmp().split(",")).map(Long::valueOf).collect(Collectors.toList()));
            List<SysUser> data = userList.getData();

            List<UnitInfo> outMembersEmpJson = data.stream().map(o->{
                UnitInfo unitInfo = new UnitInfo();
                unitInfo.setLabel(o.getName());
                unitInfo.setValue(o.getUserId().toString());
                return unitInfo;
            }).collect(Collectors.toList());
            record.setOutMembersEmpJson(outMembersEmpJson);
        }
        if (StringUtils.isNotBlank(record.getOutMembersDept())){
            //老数据
            if (!StringUtils.startsWith(record.getOutMembersDept(), "{")){
                List<AllocateUnitInfo> outMembersDeptJson = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>().in(KpiAccountUnit::getId, record.getOutMembersDept().split(","))).stream().map(o->{
                    AllocateUnitInfo unitInfo = new AllocateUnitInfo();
                    unitInfo.setLabel(o.getName());
                    unitInfo.setValue(o.getId().toString());
                    unitInfo.setType("dept");
                    return unitInfo;
                }).collect(Collectors.toList());
                record.setOutMembersDeptJson(outMembersDeptJson);

                OutMembersDetpDto outMembersDetpDto = new OutMembersDetpDto();
                List<Long> collect = Arrays.stream(record.getOutMembersDept().split(",")).map(o -> Long.valueOf(o)).collect(Collectors.toList());
                outMembersDetpDto.setOut_dept(collect);
                record.setOutMembersDept(JSONUtil.toJsonStr(outMembersDetpDto));

                //新数据 {开头
            }else if (StringUtils.startsWith(record.getOutMembersDept(), "{")){
                OutMembersDetpDto outMembersDetpDto = JSONUtil.toBean(record.getOutMembersDept(), OutMembersDetpDto.class);
                List<AllocateUnitInfo> list = new ArrayList<>();

                if (CollectionUtil.isNotEmpty(outMembersDetpDto.getOut_dept())){
                    List<AllocateUnitInfo> outMembersDeptJson = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>().in(KpiAccountUnit::getId, outMembersDetpDto.getOut_dept())).stream().map(o->{
                        AllocateUnitInfo unitInfo = new AllocateUnitInfo();
                        unitInfo.setLabel(o.getName());
                        unitInfo.setValue(o.getId().toString());
                        unitInfo.setType("dept");
                        return unitInfo;
                    }).collect(Collectors.toList());
                    list.addAll(outMembersDeptJson);
                }
                if (CollectionUtil.isNotEmpty(outMembersDetpDto.getOut_dept_group())){
                    List<AllocateUnitInfo> outMembersDeptJson = kpiIndexMapper.getSysDict(outMembersDetpDto.getOut_dept_group(), "kpi_calculate_grouping",SecurityUtils.getUser().getTenantId()).stream().map(o->{
                        AllocateUnitInfo unitInfo = new AllocateUnitInfo();
                        unitInfo.setLabel(o.getLabel());
                        unitInfo.setValue(o.getItemValue());
                        unitInfo.setType("group");
                        return unitInfo;
                    }).collect(Collectors.toList());
                    list.addAll(outMembersDeptJson);
                }

                if (CollectionUtil.isNotEmpty(outMembersDetpDto.getOut_dept_except())){
                    List<AllocateUnitInfo> outMembersDeptJson = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>().in(KpiAccountUnit::getId, outMembersDetpDto.getOut_dept_except())).stream().map(o->{
                        AllocateUnitInfo unitInfo = new AllocateUnitInfo();
                        unitInfo.setLabel(o.getName());
                        unitInfo.setValue(o.getId().toString());
                        unitInfo.setType("except");
                        return unitInfo;
                    }).collect(Collectors.toList());
                    list.addAll(outMembersDeptJson);
                }
                record.setOutMembersDeptJson(list);
            }


        }
        if (StringUtils.isNotBlank(record.getInMembersEmp())){
            R<List<SysUser>> userList = remoteUserService.getUserList(Arrays.stream(record.getInMembersEmp().split(",")).map(Long::valueOf).collect(Collectors.toList()));
            List<SysUser> data = userList.getData();

            List<UnitInfo> inMembersEmpJson = data.stream().map(o->{
                UnitInfo unitInfo = new UnitInfo();
                unitInfo.setLabel(o.getName());
                unitInfo.setValue(o.getUserId().toString());
                return unitInfo;
            }).collect(Collectors.toList());
            record.setInMembersEmpJson(inMembersEmpJson);        }
        if (StringUtils.isNotBlank(record.getInMembersDept())){
            List<UnitInfo> inMembersDeptJson = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>().in(KpiAccountUnit::getId, record.getInMembersDept().split(","))).stream().map(o->{
                UnitInfo unitInfo = new UnitInfo();
                unitInfo.setLabel(o.getName());
                unitInfo.setValue(o.getId().toString());
                return unitInfo;
            }).collect(Collectors.toList());
            record.setInMembersDeptJson(inMembersDeptJson);        }
        if (StringUtils.isNotBlank(record.getOutMembersImp())){
            //根据摊出归集拿到摊入归集
            List<CostClusterUnit> list = costClusterUnitService.list(new LambdaQueryWrapper<CostClusterUnit>().in(CostClusterUnit::getId, record.getOutMembersImp().split(",")));
            List<UnitInfo> outMembersImpJson = list.stream().map(o->{
                UnitInfo unitInfo = new UnitInfo();
                unitInfo.setLabel(o.getName());
                unitInfo.setValue(o.getId().toString());
                return unitInfo;
            }).collect(Collectors.toList());
            record.setOutMembersImpJson(outMembersImpJson);

            List<UnitInfo> inMembersImpJson = new ArrayList<>();
            for (CostClusterUnit costClusterUnit : list) {
                if (StringUtils.isNotBlank(costClusterUnit.getUnits())){
                    List<JSONObject> list1 = JSONUtil.toList(costClusterUnit.getUnits(), JSONObject.class);
                    for (JSONObject jo : list1) {
                        UnitInfo unitInfo = new UnitInfo();
                        unitInfo.setValue(jo.get("id").toString());
                        unitInfo.setLabel(jo.get("name").toString());
                        inMembersImpJson.add(unitInfo);
                    }
                }
            }
            record.setInMembersImpJson(inMembersImpJson.stream().distinct().collect(Collectors.toList()));
        }
        return record;
    }

}

