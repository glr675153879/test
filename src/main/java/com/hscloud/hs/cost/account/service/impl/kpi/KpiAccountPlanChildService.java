package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.constant.enums.EnableEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.CodePrefixEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.IndexTypeEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.MemberEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiMemberMapper;
import com.hscloud.hs.cost.account.model.dto.DictDto;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountPlanChildInfoVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountPlanChildListVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiFormulaItemVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiPlanConfigVO;
import com.hscloud.hs.cost.account.service.kpi.CommCodeService;
import com.hscloud.hs.cost.account.utils.kpi.Convert;
import com.hscloud.hs.cost.account.utils.kpi.RealTimeFormulaDependencyChecker;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiAccountPlanChildMapper;
import com.hscloud.hs.cost.account.service.kpi.IKpiAccountPlanChildService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
* 核算子方案表 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class KpiAccountPlanChildService extends ServiceImpl<KpiAccountPlanChildMapper, KpiAccountPlanChild> implements IKpiAccountPlanChildService {

    @Autowired
    private KpiAccountPlanChildMapper kpiAccountPlanChildMapper;
    @Autowired
    private RemoteUserService remoteUserService;
    @Autowired
    private CommCodeService commCodeService;
    @Autowired
    @Lazy
    private KpiIndexService kpiIndexService;
    @Autowired
    @Lazy
    private KpiAccountPlanService kpiAccountPlanService;
    @Autowired
    private KpiCategoryService kpiCategoryService;
    @Autowired
    private KpiIndexFormulaObjService kpiIndexFormulaObjService;
    @Autowired
    private KpiIndexFormulaService kpiIndexFormulaService;
    @Autowired
    private KpiAccountUnitService kpiAccountUnitService;
    @Autowired
    private KpiItemService kpiItemService;
    @Autowired
    private KpiMemberMapper kpiMemberMapper;
    @Autowired
    private KpiAccountTaskService kpiAccountTaskService;

    @Override
    public List<KpiAccountPlanChildListVO> list(KpiAccountPlanChildListDto input) {
        LambdaQueryWrapper<KpiAccountPlanChild> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KpiAccountPlanChild::getDelFlag, "0");
        if (StringUtils.isNotBlank(input.getPlanName())){
            wrapper.like(KpiAccountPlanChild::getPlanName, input.getPlanName());
        }
        if (input.getId() != null){
            wrapper.eq(KpiAccountPlanChild::getId, input.getId());
        }
        if (input.getCreatedDate0() != null){
            wrapper.ge(KpiAccountPlanChild::getCreatedDate, input.getCreatedDate0());
        }
        if (input.getCreatedDate1() != null){
            wrapper.le(KpiAccountPlanChild::getCreatedDate, input.getCreatedDate1());
        }
        if (StringUtils.isNotBlank(input.getPlanCode())){
            wrapper.eq(KpiAccountPlanChild::getPlanCode, input.getPlanCode());
        }
        if (input.getStatus() != null){
            wrapper.eq(KpiAccountPlanChild::getStatus, input.getStatus());
        }
        List<KpiAccountPlanChild> list = list(wrapper);
        List<KpiAccountPlanChildListVO> kpiAccountPlanChildListVOS = Convert.convertEntityToVo(list, KpiAccountPlanChildListVO::convertByKpiAccountPlanChildList);
        //更新人赋中文
        if (CollectionUtil.isNotEmpty(kpiAccountPlanChildListVOS)){
            List<Long> collect = kpiAccountPlanChildListVOS.stream().map(KpiAccountPlanChildListVO::getUpdatedId).collect(Collectors.toList()).stream().filter(l -> l != null).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(collect)){
                List<SysUser> data = remoteUserService.getUserList(collect).getData();
                for (KpiAccountPlanChildListVO kpiAccountPlanChildListVO : kpiAccountPlanChildListVOS) {
                    for (SysUser datum : data) {
                        if (kpiAccountPlanChildListVO.getUpdatedId().equals(datum.getUserId())){
                            kpiAccountPlanChildListVO.setUpdateName(datum.getName());
                        }
                    }
                }
            }
        }
        return kpiAccountPlanChildListVOS;
    }

    @Override
    public IPage<KpiAccountPlanChildListVO> getPage(KpiAccountPlanChildListDto input) {
        LambdaQueryWrapper<KpiAccountPlanChild> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KpiAccountPlanChild::getDelFlag, "0");
        if (StringUtils.isNotBlank(input.getPlanName())){
            wrapper.like(KpiAccountPlanChild::getPlanName, input.getPlanName());
        }
        if (input.getId() != null){
            wrapper.eq(KpiAccountPlanChild::getId, input.getId());
        }
        if (input.getCreatedDate0() != null){
            wrapper.ge(KpiAccountPlanChild::getCreatedDate, input.getCreatedDate0());
        }
        if (input.getCreatedDate1() != null){
            wrapper.le(KpiAccountPlanChild::getCreatedDate, input.getCreatedDate1());
        }
        if (StringUtils.isNotBlank(input.getPlanCode())){
            wrapper.eq(KpiAccountPlanChild::getPlanCode, input.getPlanCode());
        }
        if (input.getStatus() != null){
            wrapper.eq(KpiAccountPlanChild::getStatus, input.getStatus());
        }
        IPage<KpiAccountPlanChildListVO> iPage = page(new Page<>(input.getCurrent(), input.getSize()), wrapper).convert(KpiAccountPlanChildListVO::convertByKpiAccountPlanChildList);

        if (CollectionUtil.isNotEmpty(iPage.getRecords())){
            //更新人赋中文
            List<Long> collect = iPage.getRecords().stream().map(KpiAccountPlanChildListVO::getUpdatedId).collect(Collectors.toList()).stream().filter(l -> l != null).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(collect)){
                List<SysUser> data = remoteUserService.getUserList(collect).getData();
                for (KpiAccountPlanChildListVO kpiAccountPlanChildListVO : iPage.getRecords()) {
                    for (SysUser datum : data) {
                        if (kpiAccountPlanChildListVO.getUpdatedId().equals(datum.getUserId())){
                            kpiAccountPlanChildListVO.setUpdateName(datum.getName());
                        }
                    }
                }
            }

            //memberCode memberName
            List<Long> userIds = iPage.getRecords().stream().map(KpiAccountPlanChildListVO::getUserId).collect(Collectors.toList()).stream().filter(l -> l != null).collect(Collectors.toList());

            List<Long> deptIds = iPage.getRecords().stream().map(KpiAccountPlanChildListVO::getDeptId).collect(Collectors.toList()).stream().filter(l -> l != null).collect(Collectors.toList());

            List<String> indexs = iPage.getRecords().stream().map(KpiAccountPlanChildListVO::getIndexCode).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(userIds)){
                List<SysUser> data = remoteUserService.getUserList(userIds).getData();
                for (KpiAccountPlanChildListVO kpiAccountPlanChildListVO : iPage.getRecords()) {
                    if (kpiAccountPlanChildListVO.getObject().equals("1")){
                        for (SysUser datum : data) {
                            if (kpiAccountPlanChildListVO.getUserId().equals(datum.getUserId())){
                                kpiAccountPlanChildListVO.setMemberName(datum.getName());
                                kpiAccountPlanChildListVO.setMemberCode(datum.getUserId());
                            }
                        }
                    }
                }
            }
            if (CollectionUtil.isNotEmpty(deptIds)){
                List<KpiAccountUnit> data = kpiAccountUnitService.listByIds(deptIds);
                for (KpiAccountPlanChildListVO kpiAccountPlanChildListVO : iPage.getRecords()) {
                    if (kpiAccountPlanChildListVO.getObject().equals("2")){
                        for (KpiAccountUnit datum : data) {
                            if (kpiAccountPlanChildListVO.getDeptId().equals(datum.getId())){
                                kpiAccountPlanChildListVO.setMemberName(datum.getName());
                                kpiAccountPlanChildListVO.setMemberCode(datum.getId());
                            }
                        }
                    }
                }
            }
            if (CollectionUtil.isNotEmpty(indexs)){
                List<KpiIndex> data = kpiIndexService.list(new LambdaQueryWrapper<KpiIndex>().in(KpiIndex::getCode, indexs));
                for (KpiAccountPlanChildListVO kpiAccountPlanChildListVO : iPage.getRecords()) {
                        for (KpiIndex datum : data) {
                            if (kpiAccountPlanChildListVO.getIndexCode().equals(datum.getCode())){
                                kpiAccountPlanChildListVO.setIndexName(datum.getName());
                            }
                        }
                }
            }
        }
        return iPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(KpiAccountPlanChildAddDto dto) {
        if (dto.getId() != null){
            dupVerify(dto);
        }

        KpiAccountPlanChild kpiAccountPlanChild = new KpiAccountPlanChild();
        if (dto.getId() != null){
            kpiAccountPlanChild = getById(dto.getId());
            BeanUtil.copyProperties(dto, kpiAccountPlanChild, CopyOptions.create().ignoreNullValue());
        }else {
            BeanUtil.copyProperties(dto, kpiAccountPlanChild);
            kpiAccountPlanChild.setCode(commCodeService.commCode(CodePrefixEnum.PLAN));
        }
        kpiAccountPlanChild.setStatus("-1");
        saveOrUpdate(kpiAccountPlanChild);
        //这一段太慢了 优化后放开
//        try {
//            List<KpiPlanVerifyDto.MissChildPlan> verify = verify(kpiAccountPlanChild.getId());
//            if (CollectionUtil.isNotEmpty(verify)){
////                throw new BizException("校验通过失败,请确认配置正确");
//                kpiAccountPlanChild.setStatus("-1");
//            }
//        }catch (Exception e){
//            kpiAccountPlanChild.setStatus("-1");
//        }
//        kpiAccountPlanChildMapper.updateStatus(kpiAccountPlanChild.getId(), kpiAccountPlanChild.getStatus());
    }

    void dupVerify(KpiAccountPlanChildAddDto dto){
        //获取所有子方案
        KpiAccountPlan kpiAccountPlan = kpiAccountPlanService.getOne(new LambdaQueryWrapper<KpiAccountPlan>().eq(KpiAccountPlan::getPlanCode, dto.getPlanCode()));
        KpiCategory category = kpiCategoryService.getOne(new LambdaQueryWrapper<KpiCategory>().eq(KpiCategory::getCategoryCode, kpiAccountPlan.getCategoryCode()));
        List<KpiAccountPlan> list = kpiAccountPlanService.list(new LambdaQueryWrapper<KpiAccountPlan>().eq(KpiAccountPlan::getCategoryCode, category.getCategoryCode()));
        List<KpiAccountPlanChild> list1 = list(new LambdaQueryWrapper<KpiAccountPlanChild>().in(KpiAccountPlanChild::getPlanCode, list.stream().map(o -> o.getPlanCode()).collect(Collectors.toList())));
        LambdaQueryWrapper<KpiAccountPlanChild> wrapper = new LambdaQueryWrapper<>();
        if (dto.getUserId() != null){
            wrapper.eq(KpiAccountPlanChild::getUserId, dto.getUserId());
        }
        if (dto.getDeptId() != null){
            wrapper.eq(KpiAccountPlanChild::getDeptId, dto.getDeptId());
        }
        wrapper.ne(KpiAccountPlanChild::getId, dto.getId());
        wrapper.eq(KpiAccountPlanChild::getIndexCode, dto.getIndexCode());
        wrapper.in(KpiAccountPlanChild::getCode, list1.stream().map(o->o.getCode()).collect(Collectors.toList()));
        List<KpiAccountPlanChild> list2 = list(wrapper);
        if (CollectionUtil.isNotEmpty(list2)){
            throw new BizException("指标对象不能在同一个方案组中重复");
        }

    }

    public void enable(KpiIndexBatchEnableDto dto) {
        kpiAccountPlanChildMapper.updateStatus(dto.getIds(), dto.getStatus());
    }

    void setKpiFormulaItemVO(KpiFormulaItemVO kpiFormulaItemVO, String planCode, Long planObj, RealTimeFormulaDependencyChecker realTimeFormulaDependencyChecker, KpiPlanCacheDto kpiPlanCacheDto){
        DictDto bry = new DictDto();
        R<List<SysUser>> userList = remoteUserService.getUserList(ListUtil.of(planObj));
        if (userList.getData()!=null && CollectionUtil.isNotEmpty(userList.getData())){
            bry.setValue(userList.getData().get(0).getUserId().toString());
            bry.setLabel(userList.getData().get(0).getName());
        }


        DictDto bks = new DictDto();
        KpiAccountUnit accountUnit = kpiAccountUnitService.getById(planObj);
        if (accountUnit != null){
            bks.setValue(accountUnit.getId().toString());
            bks.setLabel(accountUnit.getName());
        }


        DictDto bryfzks = new DictDto();
        List<KpiAccountUnit> listks = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>()
                .eq(KpiAccountUnit::getResponsiblePersonId, planObj)
                .eq(KpiAccountUnit::getResponsiblePersonType, "user")
                .eq(KpiAccountUnit::getStatus, "0")
                .eq(KpiAccountUnit::getDelFlag, "0")
        );
        if (CollectionUtil.isNotEmpty(listks)){
            bryfzks.setValue(listks.get(0).getId().toString());
            bryfzks.setLabel(listks.get(0).getName());
        }

        if (StringUtils.equalsAnyIgnoreCase(kpiFormulaItemVO.getParamType(),"10","11","20","21","24")){
            if (kpiFormulaItemVO.getParamType().equals("10")){
                kpiFormulaItemVO.setParamDesc(bry.getLabel());
                kpiFormulaItemVO.setParamValues(ListUtil.of(new DictDto(bry.getLabel(), bry.getValue())));
            }else if (kpiFormulaItemVO.getParamType().equals("20")){
                kpiFormulaItemVO.setParamDesc(bks.getLabel());
                kpiFormulaItemVO.setParamValues(ListUtil.of(new DictDto(bks.getLabel(), bks.getValue())));
            }else if (StringUtils.equalsAnyIgnoreCase(kpiFormulaItemVO.getParamType(),"11","21")){
                if (CollectionUtil.isNotEmpty(kpiFormulaItemVO.getParamValues()) && kpiFormulaItemVO.getParamValues().size() == 1){
                    if ("11".equals(kpiFormulaItemVO.getParamType())){
                        kpiFormulaItemVO.setParamDesc(bry.getLabel());
                        kpiFormulaItemVO.setParamValues(ListUtil.of(new DictDto(bry.getLabel(), bry.getValue())));
                    }
                    if ("12".equals(kpiFormulaItemVO.getParamType())){
                        kpiFormulaItemVO.setParamDesc(bks.getLabel());
                        kpiFormulaItemVO.setParamValues(ListUtil.of(new DictDto(bks.getLabel(), bks.getValue())));
                    }
                }
            }else if (kpiFormulaItemVO.getParamType().equals("24")){
                if (planObj != null){
                    List<KpiAccountUnit> list = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>()
                            .eq(KpiAccountUnit::getResponsiblePersonId, planObj)
                            .eq(KpiAccountUnit::getResponsiblePersonType, "user")
                            .eq(KpiAccountUnit::getStatus, "0")
                            .eq(KpiAccountUnit::getDelFlag, "0")
                    );
                    if (CollectionUtil.isNotEmpty(list)){
                        if (list.size() > 1){

                        }else {
                            kpiFormulaItemVO.setParamDesc(bryfzks.getLabel());
                            kpiFormulaItemVO.setParamValues(ListUtil.of(new DictDto(bryfzks.getLabel(), bryfzks.getValue())));
                        }
                    }
                }
            }
        }


        //如果有下级
        if (kpiFormulaItemVO.getFieldType().equals("index")){
            //根据fieldCode获取指标
            KpiIndex kpiIndex = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, kpiFormulaItemVO.getFieldCode()));
            if (kpiFormulaItemVO.getFieldCode().equals("z_bbv")){
                System.out.println("1");
            }
            kpiFormulaItemVO.setDelFlag(kpiIndex.getDelFlag());
            kpiFormulaItemVO.setStatus(kpiIndex.getStatus());
            kpiFormulaItemVO.setFieldName(kpiIndex.getName());
            String[] accountType0 = new String[1];
            List<List<String>> categoryCodes0 = new ArrayList<>();

            if (kpiIndex.getType().equals(IndexTypeEnum.NOT_COND.getType())){
                //找到包含方案和对象的对应公式
                //适用对象

                LambdaQueryWrapper<KpiIndexFormulaObj> wrapper = new LambdaQueryWrapper<KpiIndexFormulaObj>().eq(KpiIndexFormulaObj::getIndexCode, kpiIndex.getCode())
                        .eq(KpiIndexFormulaObj::getPlanCode, planCode)
                        ;
                KpiIndexFormulaObj kpiIndexFormulaObj = null;
                if (kpiIndex.getCaliber().equals("1")){
                    //获取人员组
                    List<String> categoryCodes = kpiPlanCacheDto.getObjCategories().stream().filter(o->o.getUserId().equals(planObj)).map(o->o.getCategoryCode()).collect(Collectors.toList());
                    categoryCodes0.add(categoryCodes);
                }else {
                    String accountType = kpiAccountUnitService.getById(Long.valueOf(planObj)).getCategoryCode();
                    accountType0[0] = accountType;
                };

                List<KpiIndexFormulaObj> kpiIndexFormulaObjs = kpiIndexFormulaObjService.list(wrapper);
                if (CollectionUtil.isNotEmpty(kpiIndexFormulaObjs)){
                    Map<Long, List<KpiIndexFormulaObj>> collect = kpiIndexFormulaObjs.stream().collect(Collectors.groupingBy(o -> o.getFormulaId()));
                    Set<Long> formulaIds = collect.keySet();
                    List<KpiIndexFormulaObj> objs = new ArrayList<>();
                    for (Long formulaId : formulaIds) {
                        Boolean ignore = false;
                        List<KpiIndexFormulaObj> kpiIndexFormulaObjsGroupByFormula = collect.get(formulaId);
                        if (kpiIndex.getCaliber().equals("1")){
                            List<KpiIndexFormulaObj> collect3 = kpiIndexFormulaObjsGroupByFormula.stream()
                                    .filter(o -> StringUtils.isNotBlank(o.getExcludePerson()) && Arrays.stream(o.getExcludePerson().split(",")).collect(Collectors.toList()).contains(planObj.toString())).collect(Collectors.toList());
                            if (CollectionUtil.isNotEmpty(collect3)){
                                ignore = true;
                            }
                        }else {
                            List<KpiIndexFormulaObj> collect3 = kpiIndexFormulaObjsGroupByFormula.stream()
                                    .filter(o -> StringUtils.isNotBlank(o.getExcludeDept()) && Arrays.stream(o.getExcludeDept().split(",")).collect(Collectors.toList()).contains(planObj.toString())).collect(Collectors.toList());
                            if (CollectionUtil.isNotEmpty(collect3)){
                                ignore = true;
                            }
                        }

                        kpiIndexFormulaObj = kpiIndexFormulaObjsGroupByFormula.stream().filter(o -> o.getPlanObj() != null && o.getPlanObj().toString().equals(planObj.toString())).findFirst().orElse(null);
                        if (kpiIndexFormulaObj == null){
                            if (kpiIndex.getCaliber().equals("2")){
                                if (StringUtils.isNotBlank(accountType0[0])&&!ignore){
                                    kpiIndexFormulaObj = kpiIndexFormulaObjsGroupByFormula.stream().filter(o -> StringUtils.isNotBlank(o.getPlanObjAccountType())&&o.getPlanObjAccountType().toString().equals(accountType0[0])).findFirst().orElse(null);

                                }
                            }else {
                                if (CollectionUtil.isNotEmpty(categoryCodes0.get(0))&&!ignore){
                                    kpiIndexFormulaObj = kpiIndexFormulaObjsGroupByFormula.stream().filter(o ->StringUtils.isNotBlank(o.getPlanObjCategory()) && categoryCodes0.get(0).contains(o.getPlanObjCategory())).findFirst().orElse(null);
                                }
                            }
                        }
                        if (kpiIndexFormulaObj == null&&!ignore){
                            kpiIndexFormulaObj = kpiIndexFormulaObjsGroupByFormula.stream().filter(o ->o.getPlanObj() != null && (o.getPlanObj().toString().equals("-200")||o.getPlanObj().toString().equals("-100"))).findFirst().orElse(null);
                        }
                        if (kpiIndexFormulaObj != null){
                            objs.add(kpiIndexFormulaObj);
                        }
                    }
                    if (CollectionUtil.isNotEmpty(objs)){
                        if (objs.size()>1){
                            throw new BizException("指标"+kpiIndex.getName()+"方案"+planCode+"下适用对象重复,需先删除");
                        }
                        else {
                            kpiIndexFormulaObj = objs.get(0);
                        }
                    }else {
                        kpiIndexFormulaObj = null;
                    }
                }


                if (kpiIndexFormulaObj == null){
//是否有下级
                    if (StringUtils.equalsAnyIgnoreCase(kpiFormulaItemVO.getParamType(),"10","11","20","21","24")){
                        Boolean flag = true;
                        if (kpiFormulaItemVO.getParamType().equals("10")){

                        }else if (kpiFormulaItemVO.getParamType().equals("20")){
                        }else if (StringUtils.equalsAnyIgnoreCase(kpiFormulaItemVO.getParamType(),"11","21")){
                            if (CollectionUtil.isNotEmpty(kpiFormulaItemVO.getParamValues()) && kpiFormulaItemVO.getParamValues().size()>1){
                                flag = false;
                            }else {
                            }
                        }else if (kpiFormulaItemVO.getParamType().equals("24")){
                            if (planObj != null){
                                List<KpiAccountUnit> list = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>()
                                        .eq(KpiAccountUnit::getResponsiblePersonId, planObj)
                                        .eq(KpiAccountUnit::getResponsiblePersonType, "user")
                                        .eq(KpiAccountUnit::getStatus, "0")
                                        .eq(KpiAccountUnit::getDelFlag, "0")
                                );
                                if (CollectionUtil.isNotEmpty(list)){
                                    if (list.size() > 1){
                                        flag = false;
                                    }
                                }
                            }
                        }
                        if (flag){
                            kpiFormulaItemVO.setExpand("Y");
                        }else {
                            kpiFormulaItemVO.setExpand("N");
                        }
                    }
//                    throw new BizException("未设置指标"+kpiIndex.getName()+"方案适用对象");
                    return;
                }






                KpiIndexFormula kpiIndexFormula = kpiIndexFormulaService.getById(kpiIndexFormulaObj.getFormulaId());

                KpiFormulaDto formulaDto = JSONUtil.toBean(kpiIndexFormula.getFormula(), KpiFormulaDto.class);
                KpiPlanConfigVO nextLevel = new KpiPlanConfigVO();

                nextLevel.setFormulaId(kpiIndexFormula.getId());
                nextLevel.setFormulaOrigin(formulaDto.getFormulaOrigin());
                nextLevel.setFieldList(BeanUtil.copyToList(formulaDto.getFieldList(), KpiFormulaItemVO.class));
                nextLevel.setFormulaShow(formulaDto.getFormulaShow());

                kpiFormulaItemVO.setKpiPlanConfigVO(nextLevel);

                List<KpiFormulaItemVO> fieldList = nextLevel.getFieldList();
                for (KpiFormulaItemVO formulaItemVO : fieldList) {
                    if (StringUtils.isBlank(nextLevel.getFormulaShow())){
                        nextLevel.setFormulaShow(formulaItemVO.getFormulaShow());
                    }
                    //如果有下级
//                    if (StringUtils.equalsAnyIgnoreCase(formulaItemVO.getParamType(),"10","20")){
//                        setKpiFormulaItemVO(formulaItemVO,planCode,userId,deptId);
//                    }

                    //是否有下级
                    if (StringUtils.equalsAnyIgnoreCase(kpiFormulaItemVO.getParamType(),"10","11","20","21","24")){
                        Boolean flag = true;
                        if (kpiFormulaItemVO.getParamType().equals("10")){
                            kpiFormulaItemVO.setParamDesc(bry.getLabel());
                        }else if (kpiFormulaItemVO.getParamType().equals("20")){
                            kpiFormulaItemVO.setParamDesc(bks.getLabel());
                        }else if (StringUtils.equalsAnyIgnoreCase(kpiFormulaItemVO.getParamType(),"11","21")){
                            if (CollectionUtil.isNotEmpty(kpiFormulaItemVO.getParamValues()) && kpiFormulaItemVO.getParamValues().size()>1){
                                flag = false;
                            }else {
                            }
                        }else if (kpiFormulaItemVO.getParamType().equals("24")){
                            if (planObj != null){
                                List<KpiAccountUnit> list = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>()
                                        .eq(KpiAccountUnit::getResponsiblePersonId, planObj)
                                        .eq(KpiAccountUnit::getResponsiblePersonType, "user")
                                        .eq(KpiAccountUnit::getStatus, "0")
                                        .eq(KpiAccountUnit::getDelFlag, "0")
                                );
                                if (CollectionUtil.isNotEmpty(list) && list.size()>1){
                                    flag = false;
                                }
                            }
                        }
                        if (flag){
                            kpiFormulaItemVO.setExpand("Y");
                        }else {
                            kpiFormulaItemVO.setExpand("N");
                        }
                    }

                    if (StringUtils.equalsAnyIgnoreCase(formulaItemVO.getParamType(),"10","11","20","21","24")){
                        Boolean flag = true;
                        Long planObj1 = null;
                        if (formulaItemVO.getParamType().equals("10")){
                            planObj1 = planObj;
                        }else if (formulaItemVO.getParamType().equals("20")){
                            planObj1 = planObj;
                        }else if (StringUtils.equalsAnyIgnoreCase(formulaItemVO.getParamType(),"11","21")){
                            if (CollectionUtil.isNotEmpty(formulaItemVO.getParamValues()) && formulaItemVO.getParamValues().size()>1){
                                flag = false;
                            }else {
                                planObj1 = Long.valueOf(formulaItemVO.getParamValues().get(0).getValue());
                            }
                        }else if (formulaItemVO.getParamType().equals("24")){
                            if (planObj != null){
                                List<KpiAccountUnit> list = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>()
                                        .eq(KpiAccountUnit::getResponsiblePersonId, planObj)
                                        .eq(KpiAccountUnit::getResponsiblePersonType, "user")
                                        .eq(KpiAccountUnit::getStatus, "0")
                                        .eq(KpiAccountUnit::getDelFlag, "0")
                                );
                                if (CollectionUtil.isNotEmpty(list)){
                                    if (list.size()>1){
                                        flag = false;
                                    }else {
                                        planObj1 = list.get(0).getId();
                                    }
                                }
                            }
                        }
                        if (flag){
                            boolean b = realTimeFormulaDependencyChecker.addDependency(kpiIndex.getCode(), formulaItemVO.getFieldCode());
                            if (b){
                                setKpiFormulaItemVO(formulaItemVO, planCode, planObj1, realTimeFormulaDependencyChecker, kpiPlanCacheDto);
                            }
                        }
                    }

                }
            }
        }else {

        }

    }

    void fillCache(KpiPlanCacheDto dto){
        List<KpiPlanCacheDto.ObjCategory> list = kpiAccountPlanChildMapper.getObjCategory();
        dto.setObjCategories(list);
    }


    public KpiPlanConfigVO indexConfigInfoV2(String indexCode, String planCode, String memberCode) {
        RealTimeFormulaDependencyChecker realTimeFormulaDependencyChecker = new RealTimeFormulaDependencyChecker();

        KpiPlanCacheDto kpiPlanCacheDto = new KpiPlanCacheDto();
        fillCache(kpiPlanCacheDto);

        String[] accountType0 = new String[1];
        List<List<String>> categoryCodes0 = new ArrayList<>();
        KpiPlanConfigVO kpiPlanConfigVO = new KpiPlanConfigVO();
        KpiAccountPlan kpiAccountPlan = kpiAccountPlanService.getOne(new LambdaQueryWrapper<KpiAccountPlan>().eq(KpiAccountPlan::getPlanCode, planCode));
        //指标中绑定的方案是方案分组(category)
        KpiCategory kpiCategory = kpiCategoryService.getOne(new LambdaQueryWrapper<KpiCategory>().eq(KpiCategory::getCategoryCode, kpiAccountPlan.getCategoryCode()));
        planCode = kpiCategory.getCategoryCode();
        KpiIndex kpiIndex = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, indexCode));
        kpiPlanConfigVO.setDelFlag(kpiIndex.getDelFlag());
        kpiPlanConfigVO.setStatus(kpiIndex.getStatus());
        kpiPlanConfigVO.setBusiType(kpiCategory.getBusiType());
        String type = kpiIndex.getType();
        //条件或分摊,则直接展示指标名称,非条件在下一级展示公式
        //第一级指标公式是需要展示的
        if (type.equals(IndexTypeEnum.NOT_COND.getType())){


            //找到包含方案和对象的对应公式
            LambdaQueryWrapper<KpiIndexFormulaObj> wrapper = new LambdaQueryWrapper<KpiIndexFormulaObj>().eq(KpiIndexFormulaObj::getIndexCode, indexCode)
                    .eq(KpiIndexFormulaObj::getPlanCode, planCode);

            if (kpiIndex.getCaliber().equals("1")){
                //获取人员组
                List<String> categoryCodes = kpiPlanCacheDto.getObjCategories().stream().filter(o->o.getUserId().equals(Long.valueOf(memberCode))).map(o->o.getCategoryCode()).collect(Collectors.toList());
                categoryCodes0.add(categoryCodes);
            }else {
                String accountType = kpiAccountUnitService.getById(Long.valueOf(memberCode)).getCategoryCode();
                accountType0[0] = accountType;
            };

            KpiIndexFormulaObj kpiIndexFormulaObj = null;

            List<KpiIndexFormulaObj> kpiIndexFormulaObjs = kpiIndexFormulaObjService.list(wrapper);

            if (CollectionUtil.isNotEmpty(kpiIndexFormulaObjs)){
                Map<Long, List<KpiIndexFormulaObj>> collect = kpiIndexFormulaObjs.stream().collect(Collectors.groupingBy(o -> o.getFormulaId()));
                Set<Long> formulaIds = collect.keySet();
                List<KpiIndexFormulaObj> objs = new ArrayList<>();
                for (Long formulaId : formulaIds) {
                    Boolean ignore = false;
                    List<KpiIndexFormulaObj> kpiIndexFormulaObjsGroupByFormula = collect.get(formulaId);
                    if (kpiIndex.getCaliber().equals("1")){
                        List<KpiIndexFormulaObj> collect3 = kpiIndexFormulaObjsGroupByFormula.stream()
                                .filter(o -> StringUtils.isNotBlank(o.getExcludePerson()) && Arrays.stream(o.getExcludePerson().split(",")).collect(Collectors.toList()).contains(memberCode)).collect(Collectors.toList());
                        if (CollectionUtil.isNotEmpty(collect3)){
                            ignore = true;
                        }
                    }else {
                        List<KpiIndexFormulaObj> collect3 = kpiIndexFormulaObjsGroupByFormula.stream()
                                .filter(o -> StringUtils.isNotBlank(o.getExcludeDept()) && Arrays.stream(o.getExcludeDept().split(",")).collect(Collectors.toList()).contains(memberCode)).collect(Collectors.toList());
                        if (CollectionUtil.isNotEmpty(collect3)){
                            ignore = true;
                        }
                    }

                    kpiIndexFormulaObj = kpiIndexFormulaObjsGroupByFormula.stream().filter(o -> o.getPlanObj() != null && o.getPlanObj().toString().equals(memberCode)).findFirst().orElse(null);
                    if (kpiIndexFormulaObj == null){
                        if (kpiIndex.getCaliber().equals("2")){
                            if (StringUtils.isNotBlank(accountType0[0])&&!ignore){
                                kpiIndexFormulaObj = kpiIndexFormulaObjsGroupByFormula.stream().filter(o -> StringUtils.isNotBlank(o.getPlanObjAccountType())&&o.getPlanObjAccountType().toString().equals(accountType0[0])).findFirst().orElse(null);

                            }
                        }else {
                            if (CollectionUtil.isNotEmpty(categoryCodes0.get(0))&&!ignore){
                                kpiIndexFormulaObj = kpiIndexFormulaObjsGroupByFormula.stream().filter(o ->StringUtils.isNotBlank(o.getPlanObjCategory()) && categoryCodes0.get(0).contains(o.getPlanObjCategory())).findFirst().orElse(null);
                            }
                        }
                    }
                    if (kpiIndexFormulaObj == null&&!ignore){
                        kpiIndexFormulaObj = kpiIndexFormulaObjsGroupByFormula.stream().filter(o ->o.getPlanObj() != null && (o.getPlanObj().toString().equals("-200")||o.getPlanObj().toString().equals("-100"))).findFirst().orElse(null);
                    }
                    if (kpiIndexFormulaObj != null){
                        objs.add(kpiIndexFormulaObj);
                    }
                }
                if (CollectionUtil.isNotEmpty(objs)){
                    if (objs.size()>1){
                        throw new BizException("指标"+kpiIndex.getName()+"方案"+planCode+"下适用对象重复,需先删除");
                    }
                    else {
                        kpiIndexFormulaObj = objs.get(0);
                    }
                }else {
                    return null;
                }
            }else {
                return null;
            }

            KpiIndexFormula kpiIndexFormula = kpiIndexFormulaService.getById(kpiIndexFormulaObj.getFormulaId());

            KpiFormulaDto formulaDto = JSONUtil.toBean(kpiIndexFormula.getFormula(), KpiFormulaDto.class);

            //返回
            kpiPlanConfigVO.setFormulaId(kpiIndexFormula.getId());
            kpiPlanConfigVO.setFormulaOrigin(formulaDto.getFormulaOrigin());
            kpiPlanConfigVO.setFieldList(BeanUtil.copyToList(formulaDto.getFieldList(), KpiFormulaItemVO.class));
            kpiPlanConfigVO.setFormulaShow(formulaDto.getFormulaShow());

            for (KpiFormulaItemVO kpiFormulaItemVO : kpiPlanConfigVO.getFieldList()) {
                if (StringUtils.isBlank(kpiPlanConfigVO.getFormulaShow())){
                    kpiPlanConfigVO.setFormulaShow(kpiFormulaItemVO.getFormulaShow());
                }



                if (StringUtils.equalsAnyIgnoreCase(kpiFormulaItemVO.getParamType(),"10","11","20","21","24")){
                    Boolean flag = true;
                    Long planObj = null;
                    if (kpiFormulaItemVO.getParamType().equals("10")){
                        planObj = Long.valueOf(memberCode);
                        kpiFormulaItemVO.setParamValues(ListUtil.of(new DictDto("本人员",planObj.toString())));
                    }else if (kpiFormulaItemVO.getParamType().equals("20")){
                        planObj = Long.valueOf(memberCode);
                        kpiFormulaItemVO.setParamValues(ListUtil.of(new DictDto("本科室",planObj.toString())));

                    }else if (StringUtils.equalsAnyIgnoreCase(kpiFormulaItemVO.getParamType(),"11","21")){
                        if (CollectionUtil.isNotEmpty(kpiFormulaItemVO.getParamValues()) && kpiFormulaItemVO.getParamValues().size() == 1){
                            planObj = Long.valueOf(kpiFormulaItemVO.getParamValues().get(0).getValue());
                        }else {
                            flag = false;
                        }
                    }else if (kpiFormulaItemVO.getParamType().equals("24")){
                            List<KpiAccountUnit> list = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>()
                                    .eq(KpiAccountUnit::getResponsiblePersonId, Long.valueOf(memberCode))
                                    .eq(KpiAccountUnit::getResponsiblePersonType, "user")
                                    .eq(KpiAccountUnit::getStatus, "0")
                                    .eq(KpiAccountUnit::getDelFlag, "0")
                            );
                            if (CollectionUtil.isNotEmpty(list)){
                                if (list.size()>1){
                                    flag = false;
                                }else {
                                    planObj = list.get(0).getId();
                                    kpiFormulaItemVO.setParamDesc(list.get(0).getName());
                                    kpiFormulaItemVO.setParamValues(ListUtil.of(new DictDto(list.get(0).getName(),list.get(0).getId().toString())));
                                }
                            }
                    }
                    if (flag){
                        boolean b = realTimeFormulaDependencyChecker.addDependency(kpiIndex.getCode(), kpiFormulaItemVO.getFieldCode());
                        if (b){
                            setKpiFormulaItemVO(kpiFormulaItemVO, planCode, planObj, realTimeFormulaDependencyChecker, kpiPlanCacheDto);
                        }
                    }
                }
            }
        }else if (type.equals(IndexTypeEnum.COND.getType())){

        }else if (type.equals(IndexTypeEnum.ALLOCATION.getType())){

        }else {
            throw new BizException("未找到对应指标类型");
        }
        //填充状态
        fillStatus(kpiPlanConfigVO);
        return kpiPlanConfigVO;
    }

    void fillStatus(KpiPlanConfigVO kpiPlanConfigVO){
        if (kpiPlanConfigVO != null){
            Set<String> indexCodes = new HashSet<>();
            Set<String> itemCodes = new HashSet<>();

            Map<String, KpiItem> collectItem = new HashMap<>();
            Map<String, KpiIndex> collectIndex = new HashMap<>();

            List<KpiFormulaItemVO> fieldList = kpiPlanConfigVO.getFieldList();
            for (KpiFormulaItemVO kpiFormulaItemVO : fieldList) {
                findCodes(kpiFormulaItemVO, indexCodes, itemCodes);
            }
            if (CollectionUtil.isNotEmpty(itemCodes)){
                List<KpiItem> list = kpiItemService.list(new LambdaQueryWrapper<KpiItem>().in(KpiItem::getCode, itemCodes).eq(KpiItem::getDelFlag, 0).eq(KpiItem::getBusiType, kpiPlanConfigVO.getBusiType()));
                collectItem = list.stream().collect(Collectors.toMap(KpiItem::getCode, o -> o));

            }
            if (CollectionUtil.isNotEmpty(indexCodes)){
                List<KpiIndex> list = kpiIndexService.list(new LambdaQueryWrapper<KpiIndex>().in(KpiIndex::getCode, indexCodes));
                collectIndex = list.stream().collect(Collectors.toMap(KpiIndex::getCode, o -> o));
            }
            fill(kpiPlanConfigVO, collectItem, collectIndex);

        }
    }

    void fill(KpiPlanConfigVO kpiPlanConfigVO, Map<String ,KpiItem> mapItem, Map<String, KpiIndex> mapIndex){
        if (kpiPlanConfigVO != null){
            List<KpiFormulaItemVO> fieldList = kpiPlanConfigVO.getFieldList();
            for (KpiFormulaItemVO kpiFormulaItemVO : fieldList) {
                fillItem(kpiFormulaItemVO, mapItem, mapIndex);
            }
        }
    }

    void fillItem(KpiFormulaItemVO kpiFormulaItemVO, Map<String, KpiItem> itemMap, Map<String, KpiIndex> indexMap){
        if (kpiFormulaItemVO != null){
            if (kpiFormulaItemVO.getFieldType().equals("index")){
                KpiIndex kpiIndex = indexMap.get(kpiFormulaItemVO.getFieldCode());
                kpiFormulaItemVO.setDelFlag(kpiIndex.getDelFlag());
                kpiFormulaItemVO.setStatus(kpiIndex.getStatus());
            }else if (kpiFormulaItemVO.getFieldType().equals("item")){
                KpiItem kpiItem = itemMap.get(kpiFormulaItemVO.getFieldCode());
                if (kpiItem != null){
                    kpiFormulaItemVO.setDelFlag(kpiItem.getDelFlag());
                    kpiFormulaItemVO.setStatus(kpiItem.getStatus());
                }else {
                    kpiFormulaItemVO.setDelFlag("1");
                    kpiFormulaItemVO.setStatus("1");
                }

            }
            if (kpiFormulaItemVO.getKpiPlanConfigVO() != null && CollectionUtil.isNotEmpty(kpiFormulaItemVO.getKpiPlanConfigVO().getFieldList())){
                for (KpiFormulaItemVO formulaItemVO : kpiFormulaItemVO.getKpiPlanConfigVO().getFieldList()) {
                    fillItem(formulaItemVO, itemMap, indexMap);
                }
            }
        }
    }

    void findCodes(KpiFormulaItemVO kpiFormulaItemVO, Set<String> indexCodes, Set<String> itemCodes){
        if (kpiFormulaItemVO != null){
            if (kpiFormulaItemVO.getFieldType().equals("index")){
                indexCodes.add(kpiFormulaItemVO.getFieldCode());
            }else if (kpiFormulaItemVO.getFieldType().equals("item")){
                itemCodes.add(kpiFormulaItemVO.getFieldCode());
            }
            if (kpiFormulaItemVO.getKpiPlanConfigVO() != null && CollectionUtil.isNotEmpty(kpiFormulaItemVO.getKpiPlanConfigVO().getFieldList())){
                for (KpiFormulaItemVO formulaItemVO : kpiFormulaItemVO.getKpiPlanConfigVO().getFieldList()) {
                    findCodes(formulaItemVO, indexCodes, itemCodes);
                }
            }
        }
    }

    @Override
    public KpiAccountPlanChildInfoVO getInfo(Long id) {
        KpiAccountPlanChild byId = getById(id);
        KpiAccountPlanChildInfoVO kpiAccountPlanChildInfoVO = BeanUtil.copyProperties(byId, KpiAccountPlanChildInfoVO.class);
        if (byId.getUserId() != null){
            R<List<SysUser>> userList = remoteUserService.getUserList(ListUtil.of(byId.getUserId()));
            kpiAccountPlanChildInfoVO.setUserName(userList.getData().get(0).getName());
        }
        if (byId.getDeptId() != null){
            KpiAccountUnit byId1 = kpiAccountUnitService.getById(byId.getDeptId());
            kpiAccountPlanChildInfoVO.setDeptName(byId1.getName());
        }
        return kpiAccountPlanChildInfoVO;
    }

    @Override
    public void del(Long id) {
        KpiAccountPlanChild byId = getById(id);
        byId.setDelFlag("1");
        updateById(byId);
    }

    public KpiPlanVerifyDto.MissResult verify(Long id) {
        KpiPlanVerifyDto kpiPlanVerifyDto = new KpiPlanVerifyDto();
        kpiAccountPlanService.copy(kpiPlanVerifyDto);
        kpiAccountPlanService.judge(getById(id), kpiPlanVerifyDto);
        kpiAccountPlanService.convertKpiPlanVerifyDto(kpiPlanVerifyDto);
        KpiPlanVerifyDto.MissResult missResult = kpiPlanVerifyDto.getMissResult();
        if (missResult == null || (CollectionUtil.isEmpty(missResult.getMissFormulas())&&CollectionUtil.isEmpty(missResult.getMissIndices())&&CollectionUtil.isEmpty(missResult.getCycledependencies()))){
            KpiAccountPlanChild byId = getById(id);
            byId.setStatus("0");
            byId.updateById();
        }
        return missResult;
    }

    public List<KpiAccountPlanChildListVO> configList(KpiAccountPlanChildListDto input) {
            List<KpiAccountPlanChildListVO> result = new ArrayList<>();

            KpiAccountPlan plan = kpiAccountPlanService.getOne(new LambdaQueryWrapper<KpiAccountPlan>().eq(KpiAccountPlan::getPlanCode, input.getPlanCode()));

            if (StringUtils.isBlank(plan.getIndexCode())){
                throw new BizException("请配置指标");
            }

            KpiIndex kpiIndex = kpiIndexService.getOne(new LambdaQueryWrapper<KpiIndex>().eq(KpiIndex::getCode, plan.getIndexCode()));
            if (kpiIndex == null){
                throw new BizException("找不到指标");
            }
            if (!StringUtils.equalsAny(kpiIndex.getCaliber(), "1", "2")){
                throw new BizException("指标颗粒度需为人或科室");
            }


        List<Long> memberIds = kpiAccountTaskService.getMemberListComm_before(plan.getRange());
        if (kpiIndex.getCaliber().equals("1")){
            result = getUserPlanChild(kpiIndex.getCode(), plan.getPlanCode(), memberIds);
        } else if (kpiIndex.getCaliber().equals("2")) {
            result = getDeptPlanChild(kpiIndex.getCode(), plan.getPlanCode(), memberIds);
        }
        for (KpiAccountPlanChildListVO vo : result) {
            vo.setIndexCode(kpiIndex.getCode());
            vo.setIndexName(kpiIndex.getName());
        }

        return result;
    }

    private List<KpiAccountPlanChildListVO> getUserPlanChild(String code, String planCode, List<Long> userIds) {
        return kpiAccountPlanChildMapper.getUserPlanChild(code, planCode, userIds);
    }

    private List<KpiAccountPlanChildListVO> getDeptPlanChild(String code, String planCode, List<Long> userIds) {
        return kpiAccountPlanChildMapper.getDeptPlanChild(code, planCode, userIds);
    }

//
//    public void disenable(List<Long> ids) {
//        kpiAccountPlanChildMapper.disenable(ids);
//    }
}
