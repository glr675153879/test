package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.Constant;
import com.hscloud.hs.cost.account.constant.enums.kpi.CaliberEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.CodePrefixEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiAccountPlanMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiCategoryMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiIndexMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountPlanAddDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountPlanListDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexEnableDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiPlanVerifyDto;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostClusterUnit;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountPlanListVO;
import com.hscloud.hs.cost.account.service.impl.dataReport.CostClusterUnitService;
import com.hscloud.hs.cost.account.service.kpi.CommCodeService;
import com.hscloud.hs.cost.account.service.kpi.IKpiAccountPlanService;
import com.hscloud.hs.cost.account.utils.kpi.Convert;
import com.hscloud.hs.cost.account.utils.kpi.FormulaDependencyChecker;
import com.hscloud.hs.cost.account.utils.kpi.RealTimeFormulaDependencyChecker;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.admin.api.vo.UserCoreVo;
import com.pig4cloud.pigx.common.core.constant.SecurityConstants;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import groovy.lang.Lazy;
import groovyjarjarpicocli.CommandLine;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


/**
* 核算方案表(COST_ACCOUNT_PLAN) 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class KpiAccountPlanService extends ServiceImpl<KpiAccountPlanMapper, KpiAccountPlan> implements IKpiAccountPlanService {

    @Autowired
    private KpiAccountPlanMapper kpiAccountPlanMapper;
    @Autowired
    private CommCodeService commCodeService;
    @Autowired
    @Lazy
    private KpiAccountPlanChildService kpiAccountPlanChildService;
    @Autowired
    private KpiAccountChildPlanVerifyService kpiAccountChildPlanVerifyService;
    @Autowired
    private RemoteUserService remoteUserService;
    @Autowired
    private KpiAccountUnitService kpiAccountUnitService;
    @Autowired
    private KpiIndexFormulaService kpiIndexFormulaService;
    @Autowired
    private KpiIndexFormulaObjService kpiIndexFormulaObjService;
    @Autowired
    private KpiMemberService kpiMemberService;
    @Autowired
    private CostClusterUnitService costClusterUnitService;
    @Autowired
    private KpiIndexService kpiIndexService;
    @Autowired
    private KpiIndexMapper kpiIndexMapper;
    @Autowired
    private KpiCategoryMapper kpiCategoryMapper;

    @Override
    public List<KpiAccountPlanListVO> list(KpiAccountPlanListDto input) {
        List<KpiAccountPlanListVO> list = kpiAccountPlanMapper.getList(input);
//        List<SysDictItem> sysDictItems = kpiIndexMapper.getSysDict(null, "kpi_calculate_grouping");
//        List<KpiCategory> kpiCategories = kpiCategoryMapper.selectList(new LambdaQueryWrapper<KpiCategory>().eq(KpiCategory::getCategoryType, "user_group"));
//            for (KpiAccountPlanListVO record : list) {
//                if (StringUtils.isNotBlank(record.getAccountCategoryCode())){
//                    String collect = Arrays.stream(record.getAccountCategoryCode().split(",")).map(o -> {
//                        String label = sysDictItems.stream().filter(o1 -> o1.getDictType().equals(o)).collect(Collectors.toList()).get(0).getLabel();
//                        return label;
//                    }).collect(Collectors.joining(","));
//                    record.setAccountCategoryCode(collect);
//                }
//                if (StringUtils.isNotBlank(record.getUserCategoryCode())){
//                    String collect = Arrays.stream(record.getUserCategoryCode().split(",")).map(o -> {
//                        String label = kpiCategories.stream().filter(o1 -> o1.getCategoryCode().equals(o)).collect(Collectors.toList()).get(0).getCategoryName();
//                        return label;
//                    }).collect(Collectors.joining(","));
//                    record.setUserCategoryName(collect);
//                }
//            }
        return list;
    }

    @Override
    public IPage<KpiAccountPlanListVO> getPage(KpiAccountPlanListDto input) {

        IPage<KpiAccountPlanListVO> page = kpiAccountPlanMapper.getPage(new Page<>(input.getCurrent(), input.getSize()), input);
        return page;
    }

    @Override
    public void saveOrUpdate(KpiAccountPlanAddDto dto) {
        KpiAccountPlan kpiAccountPlan = null;
        if (dto.getId() != null){
            kpiAccountPlan = BeanUtil.copyProperties(dto, KpiAccountPlan.class);
        }else {
            //赋code
            kpiAccountPlan = BeanUtil.copyProperties(dto, KpiAccountPlan.class);
            kpiAccountPlan.setPlanCode(commCodeService.commCode(CodePrefixEnum.PLAN));
            kpiAccountPlan.setDelFlag("0");
        }
        saveOrUpdate(kpiAccountPlan);
    }

    @Override
    public void enable(KpiIndexEnableDto dto) {
        KpiAccountPlan byId = getById(dto.getId());
        byId.setStatus(dto.getStatus());
        updateById(byId);
    }

    @Override
    public void del(Long id) {
        KpiAccountPlan byId = getById(id);
        byId.setDelFlag("1");
        updateById(byId);
    }

//    skpi
    @Override
    public KpiPlanVerifyDto.MissResult verify(List<Long> ids) {
        KpiPlanVerifyDto kpiPlanVerifyDto = new KpiPlanVerifyDto();
        //copy
        copy(kpiPlanVerifyDto);
        List<KpiAccountPlan> kpiAccountPlans = listByIds(ids);
        for (KpiAccountPlan kpiAccountPlan : kpiAccountPlans) {
            //最终是校验子方案
            List<KpiAccountPlanChild> list = kpiAccountPlanChildService.list(new LambdaQueryWrapper<KpiAccountPlanChild>()
                    .eq(KpiAccountPlanChild::getPlanCode, kpiAccountPlan.getPlanCode())
                    .eq(KpiAccountPlanChild::getStatus, "0")
                    .eq(KpiAccountPlanChild::getDelFlag, "0"));
            for (KpiAccountPlanChild kpiAccountPlanChild : list) {
                judge(kpiAccountPlanChild, kpiPlanVerifyDto);
            }
        }
        //翻译
        convertKpiPlanVerifyDto(kpiPlanVerifyDto);
        return kpiPlanVerifyDto.getMissResult();
    }

    void copy(KpiPlanVerifyDto kpiPlanVerifyDto){
        List<KpiIndexFormulaObj> formulaObjs = kpiIndexFormulaObjService.list();
        kpiPlanVerifyDto.setKpiIndexFormulaObjs(formulaObjs);
        List<KpiMember> list = kpiMemberService.list();
        kpiPlanVerifyDto.setKpiMembers(list);
        List<KpiAccountUnit> kpiAccountUnits = kpiAccountUnitService.list();
        kpiPlanVerifyDto.setKpiAccountUnits(kpiAccountUnits);
        List<CostClusterUnit> list1 = costClusterUnitService.list();
        kpiPlanVerifyDto.setCostClusterUnits(list1);
        List<KpiIndexFormula> kpiIndexFormulas = kpiIndexFormulaService.list();
        kpiPlanVerifyDto.setKpiIndexFormulas(kpiIndexFormulas);
        List<KpiIndex> indices = kpiIndexService.list();
        kpiPlanVerifyDto.setKpiIndices(indices);
        List<UserCoreVo> var1 = remoteUserService.listMainDetails(SecurityConstants.FROM_IN).getData();
        kpiPlanVerifyDto.setSysUsers(var1);
    }

    public void judge(KpiAccountPlanChild kpiAccountPlanChild, KpiPlanVerifyDto kpiPlanVerifyDto){
        RealTimeFormulaDependencyChecker realTimeFormulaDependencyChecker = new RealTimeFormulaDependencyChecker();
        kpiAccountChildPlanVerifyService.judge(kpiAccountPlanChild, kpiPlanVerifyDto, realTimeFormulaDependencyChecker);
    }

    public void convertKpiPlanVerifyDto(KpiPlanVerifyDto kpiPlanVerifyDto){
        KpiPlanVerifyDto.MissResult missResult = new KpiPlanVerifyDto.MissResult();
        missResult.setMissFormulas(kpiPlanVerifyDto.getMissFormulas());
        missResult.setMissIndices(kpiPlanVerifyDto.getMissIndices());
        missResult.setCycledependencies(kpiPlanVerifyDto.getCycledependencies());
        kpiPlanVerifyDto.setMissResult(missResult);
//        List<KpiPlanVerifyDto.MissChildPlan> missChildPlans = kpiPlanVerifyDto.getMissChildPlans();
//        List<Long> userIds = missChildPlans.stream()
//                .filter(o -> o.getIndexCaliber().equals(CaliberEnum.PEOPLE.getType()))
//                .map(o->o.getMemberId()).distinct().collect(Collectors.toList());
//        List<Long> deptIds = missChildPlans.stream()
//                .filter(o -> o.getIndexCaliber().equals(CaliberEnum.DEPT.getType()))
//                .map(o->o.getMemberId()).distinct().collect(Collectors.toList());
//        if (CollectionUtil.isNotEmpty(userIds)){
//            R<List<SysUser>> userList = remoteUserService.getUserList(userIds);
//            for (KpiPlanVerifyDto.MissChildPlan missChildPlan : missChildPlans) {
//                if (missChildPlan.getIndexCaliber().equals(CaliberEnum.PEOPLE.getType())){
//                    if (userList != null && CollectionUtil.isNotEmpty(userList.getData())){
//                        SysUser sysUser = userList.getData().stream().filter(o -> o.getUserId().equals(missChildPlan.getMemberId())).findFirst().orElse(null);
//                        missChildPlan.setMemberName(sysUser==null?"":sysUser.getName());
//                    }
//                }
//            }
//        }
//        if (CollectionUtil.isNotEmpty(deptIds)){
//            List<KpiAccountUnit> depts = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>().in(KpiAccountUnit::getId, deptIds));
//            for (KpiPlanVerifyDto.MissChildPlan missChildPlan : missChildPlans) {
//                if (missChildPlan.getIndexCaliber().equals(CaliberEnum.PEOPLE.getType())){
//                    if (CollectionUtil.isNotEmpty(depts)){
//                        KpiAccountUnit accountUnit = depts.stream().filter(o -> o.getId().equals(missChildPlan.getMemberId())).findFirst().orElse(null);
//                        missChildPlan.setMemberName(accountUnit==null?null:accountUnit.getName());
//
//                    }
//                }
//            }
//        }

    }


    /**
     * 批量插入 仅适用于mysql
     *
     * @param entityList 实体列表
     * @return 影响行数
     */
    public Integer insertBatchSomeColumn(Collection<KpiAccountPlan> entityList){
        return kpiAccountPlanMapper.insertBatchSomeColumn(entityList);
    }

}
