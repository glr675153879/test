package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bestvike.linq.Linq;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hscloud.hs.cost.account.constant.enums.EnableEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.CategoryEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.CodePrefixEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.MemberEnum;
import com.hscloud.hs.cost.account.mapper.kpi.*;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.service.kpi.CommCodeService;
import com.hscloud.hs.cost.account.service.kpi.IKpiCategoryService;
import com.hscloud.hs.cost.account.utils.kpi.Convert;
import com.hscloud.hs.cost.account.utils.kpi.TreeUtilExtend;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分组表 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = false)
public class KpiCategoryService extends ServiceImpl<KpiCategoryMapper, KpiCategory> implements IKpiCategoryService {
    private final KpiMemberService kpiMembersService;
    private final CommCodeService commCodeService;
    private final KpiItemMapper kpiItemMapper;
    private final KpiIndexMapper kpiIndexMapper;
    private final KpiIndexFormulaMapper kpiIndexFormulaMapper;
    private final KpiIndexFormulaObjMapper kpiIndexFormulaObjMapper;
    private final KpiAccountPlanMapper kpiAccountPlanMapper;
    private final KpiAccountPlanChildMapper kpiAccountPlanChildMapper;
    @Lazy
    @Autowired
    private KpiIndexFormulaService kpiIndexFormulaService;
    private final KpiMemberService kpiMemberService;
    @Autowired
    @Lazy
    private KpiAccountPlanService kpiAccountPlanService;

    @Override
    public Long saveOrUpdateGroup(KpiCategoryDto input) {
        if (input.getId() != null && input.getId() > 0) {
            KpiCategory category = getById(input.getId());
            if (category == null) {
                throw new BizException("数据不存在");
            }
            BeanUtils.copyProperties(input, category);
            updateById(category);
        } else {
            KpiCategory category = new KpiCategory();
            BeanUtils.copyProperties(input, category);
            category.setCreatedId(SecurityUtils.getUser().getId());
            Long countForCategoryName = baseMapper
                    .selectCount(new LambdaQueryWrapper<KpiCategory>().eq(KpiCategory::getCategoryName, input.getCategoryName())
                            .eq(KpiCategory::getStatus, "0")
                            .eq(KpiCategory::getDelFlag, "0")
                            .eq(StringUtils.isNotBlank(input.getBusiType()), KpiCategory::getBusiType, input.getBusiType())
                            .eq(ObjectUtils.isNotNull(input.getParentId()) && input.getParentId() > 0, KpiCategory::getParentId, input.getParentId())
                            .eq(KpiCategory::getCategoryType, input.getCategoryType()));
            if (countForCategoryName > 0) {
                throw new BizException("已有同名分组存在,请修改后重试");
            }
            category.setCategoryCode(commCodeService.commCode(CodePrefixEnum.GROUP));
            save(category);
            input.setId(category.getId());
        }
        return input.getId();
    }

    /**
     * 使最后一个节点为null
     *
     * @param original
     * @return
     */
    public void getLastNodeNull(List<Tree<Long>> original) {
        for (Tree<Long> a : original) {
            if (CollectionUtils.isEmpty(a.getChildren())) {
                a.setChildren(null);
            } else {
                getLastNodeNull(a.getChildren());
            }
        }
    }

    @Override
    public List<Tree<Long>> getTreeForCategory(KpiGroupListSearchDto dto) {
        List<KpiCategory> list = baseMapper
                .selectList(new LambdaQueryWrapper<KpiCategory>()
                        .eq(StringUtils.isNotBlank(dto.getName()), KpiCategory::getCategoryName, dto.getName())
                        .eq(StringUtils.isNotBlank(dto.getType()), KpiCategory::getCategoryType, dto.getType())
                        .eq(StringUtils.isNotBlank(dto.getBusiType()), KpiCategory::getBusiType, dto.getBusiType())
                        .eq(KpiCategory::getDelFlag, "0")
                        .orderByAsc(KpiCategory::getSeq));
        List<TreeNode<Long>> nodeList = list.stream().map(m -> {
            TreeNode<Long> treeNode = new TreeNode<>(m.getId(), m.getParentId(), m.getCategoryName(), null);
            Map<String, Object> extra = Maps.newHashMap();
            extra.put("categoryCode", m.getCategoryCode());
            extra.put("usable", true);
            extra.put("planType", m.getPlanType());
            extra.put("seq", m.getSeq());
            extra.put("status", m.getStatus());
            extra.put("category_type", m.getCategoryType());
            extra.put("description", m.getDescription());
            extra.put("thirdCode", m.getThirdCode());
            treeNode.setExtra(extra);
            return treeNode;
        }).collect(Collectors.toList());
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        treeNodeConfig.setNameKey("categoryName");
        treeNodeConfig.setParentIdKey("parent_id");
        List<Tree<Long>> build = TreeUtilExtend.build(nodeList, 0L, treeNodeConfig);
        getLastNodeNull(build);
        if (CollectionUtils.isNotEmpty(build) && build.size() > 1) {
            return build.stream().distinct().collect(Collectors.toList());
        }
        return build;
    }

    /**
     * 列表
     *
     * @return
     */


    public List<KpiGroupOutput> listByGroup(String name) {
        List<KpiGroupOutput> groupList = Lists.newArrayList();
        List<KpiCategory> list = baseMapper
                .selectList(new LambdaQueryWrapper<KpiCategory>().eq(StringUtils.isNotBlank(name), KpiCategory::getCategoryName, name)
                        .eq(KpiCategory::getStatus, "0").eq(KpiCategory::getDelFlag, "0").orderByAsc(KpiCategory::getSeq));
        for (KpiCategory a : list) {
            KpiGroupOutput group = new KpiGroupOutput();
            group.setId(a.getId());
            group.setName(a.getCategoryName());
        }
        return groupList;
    }

    /**
     * 1、该分组内无数据；
     * 2、该分组未被运用于归集管理特殊归集中；
     * 3、该数据表未被运用于核算项的sql配置中；
     * 4、该分组没有被应用于方案的适用对象中
     */
    @Override
    public void deleteGroup(KpiGroupDelDto dto) {
        KpiCategory byId = getById(dto.getId());
        switch (CategoryEnum.findByType(dto.getType())) {
            case USER_GROUP:
                long count = kpiMembersService.count(new LambdaQueryWrapper<KpiMember>()
                        .eq(KpiMember::getMemberType, MemberEnum.ROLE_EMP.getType())
                        .eq(KpiMember::getBusiType, byId.getBusiType())
                        .eq(KpiMember::getHostId, dto.getId()));
                if (count > 0) {
                    throw new BizException("该分组内已存在数据，请先删除数据");
                } else {
                    removeById(dto.getId());
                }
                break;
            case IMPUTATION_GROUP:
                long imputationCount = kpiIndexMapper.selectCount(new LambdaQueryWrapper<KpiIndex>()
                        .eq(KpiIndex::getImpCategoryCode, byId.getCategoryCode())
                        .eq(KpiIndex::getStatus, "0")
                        .eq(KpiIndex::getDelFlag, "0"));
//                long imputationCount = kpiMembersService.count(new LambdaQueryWrapper<KpiMember>()
//                        .eq(KpiMember::getMemberType, MemberEnum.IMPUTATION_DEPT_EMP.getType())
//                        .eq(KpiMember::getBusiType, byId.getBusiType())
//                        .eq(KpiMember::getHostCode, byId.getCategoryCode()));
                if (imputationCount > 0) {
                    throw new BizException("该分组已被指标引用，请先删除数据");
                } else {
                    removeById(dto.getId());
                }
                break;
            case ITEM_GROUP: {
                if (byId.getParentId() == 0) {
                    LambdaQueryWrapper<KpiCategory> wrapper = Wrappers.<KpiCategory>lambdaQuery()
                            .eq(KpiCategory::getParentId, dto.getId())
                            .eq(KpiCategory::getDelFlag, EnableEnum.ENABLE.getType());
                    List<KpiCategory> list = list(wrapper);
                    if (CollectionUtils.isNotEmpty(list)) {
                        List<String> codes = list.stream().map(KpiCategory::getCategoryCode).collect(Collectors.toList());

                        LambdaQueryWrapper<KpiItem> wrapper2 = Wrappers.<KpiItem>lambdaQuery()
                                .in(KpiItem::getCategoryCode, codes)
                                .eq(KpiItem::getDelFlag, EnableEnum.ENABLE.getType());
                        if (kpiItemMapper.selectCount(wrapper2) > 0) {
                            throw new BizException("子分组内已存在核算项，请先删除核算项");
                        }

                        LambdaUpdateWrapper<KpiCategory> updateWrapper = Wrappers.<KpiCategory>lambdaUpdate()
                                .eq(KpiCategory::getParentId, dto.getId())
                                .set(KpiCategory::getDelFlag, EnableEnum.DISABLE.getType());
                        update(updateWrapper);
                    }
                }
            }
            case INDEX_GROUP: {
                LambdaQueryWrapper<KpiCategory> wrapper = Wrappers.<KpiCategory>lambdaQuery()
                        .eq(KpiCategory::getParentId, dto.getId())
                        .eq(KpiCategory::getDelFlag, EnableEnum.ENABLE.getType());
                List<KpiCategory> list = list(wrapper);
                List<String> codes = ListUtil.of(byId.getCategoryCode());
                if (CollectionUtils.isNotEmpty(list)) {
                    codes.addAll(list.stream().map(KpiCategory::getCategoryCode).collect(Collectors.toList()));
                }
                LambdaQueryWrapper<KpiIndex> wrapper2 = Wrappers.<KpiIndex>lambdaQuery()
                        .in(KpiIndex::getCategoryCode, codes)
                        .eq(KpiIndex::getDelFlag, EnableEnum.ENABLE.getType());
                if (kpiIndexMapper.selectCount(wrapper2) > 0) {
                    throw new BizException("组内已存在指标，请先删除指标");
                }

                LambdaUpdateWrapper<KpiCategory> updateWrapper = Wrappers.<KpiCategory>lambdaUpdate()
                        .eq(KpiCategory::getParentId, dto.getId())
                        .set(KpiCategory::getDelFlag, EnableEnum.DISABLE.getType());
                update(updateWrapper);

            }
            case PLAN_GROUP: {
                LambdaQueryWrapper<KpiCategory> wrapper = Wrappers.<KpiCategory>lambdaQuery()
                        .eq(KpiCategory::getParentId, dto.getId())
                        .eq(KpiCategory::getDelFlag, EnableEnum.ENABLE.getType());
                List<KpiCategory> list = list(wrapper);
                List<String> codes = ListUtil.of(byId.getCategoryCode());
                if (CollectionUtils.isNotEmpty(list)) {
                    codes.addAll(list.stream().map(KpiCategory::getCategoryCode).collect(Collectors.toList()));
                }
                LambdaQueryWrapper<KpiAccountPlan> wrapper2 = Wrappers.<KpiAccountPlan>lambdaQuery()
                        .in(KpiAccountPlan::getCategoryCode, codes)
                        .eq(KpiAccountPlan::getDelFlag, EnableEnum.ENABLE.getType());
                List<KpiAccountPlan> kpiAccountPlans = kpiAccountPlanMapper.selectList(wrapper2);
                if (CollectionUtils.isNotEmpty(kpiAccountPlans)) {
                    List<String> collect = kpiAccountPlans.stream().map(o -> o.getPlanCode()).collect(Collectors.toList());
                    Long l = kpiAccountPlanChildMapper.selectCount(new LambdaQueryWrapper<KpiAccountPlanChild>().in(KpiAccountPlanChild::getPlanCode, collect).eq(KpiAccountPlanChild::getDelFlag, EnableEnum.ENABLE.getType()));
                    if (l > 0) {
                        throw new BizException("存在子方案,请先删除子方案");
                    }
                }
            }
            default:
                //默认假删
                byId.setDelFlag("1");
                updateById(byId);
                break;
        }
    }

    @Override
    public Map<String, String> getCodeAndNameMap(String categoryType, String categoryCode, String busiType) {
        if (null == busiType) {
            busiType = EnableEnum.DISABLE.getType();
        }
        LambdaQueryWrapper<KpiCategory> wrapper = Wrappers.<KpiCategory>lambdaQuery()
                .eq(KpiCategory::getCategoryType, categoryType)
                .eq(ObjectUtils.isNotEmpty(categoryCode), KpiCategory::getCategoryCode, categoryCode)
                .eq(KpiCategory::getDelFlag, EnableEnum.ENABLE.getType())
                .eq(KpiCategory::getBusiType, busiType);
        List<KpiCategory> categoryList = this.list(wrapper);
        return categoryList.stream().collect(Collectors.toMap(KpiCategory::getCategoryCode, KpiCategory::getCategoryName));
    }

    @Override
    public void copy(CategoryCopyDto dto) {
        //oldCategory
        KpiCategory oldCategory = getById(dto.getId());
        //newCategory
        KpiCategory newCategory = BeanUtil.copyProperties(oldCategory, KpiCategory.class);
        newCategory.setCategoryCode(commCodeService.commCode(CodePrefixEnum.GROUP));
        newCategory.setCategoryName(dto.getName());
        newCategory.setId(null);
        save(newCategory);

        //oldPlan
        List<KpiAccountPlan> oldPlans = kpiAccountPlanService.list(new LambdaQueryWrapper<KpiAccountPlan>().eq(KpiAccountPlan::getCategoryCode, oldCategory.getCategoryCode()).eq(KpiAccountPlan::getDelFlag, 0));
        List<KpiAccountPlan> newPlans = oldPlans.stream().map(o -> {
            o.setId(null);
            o.setCategoryCode(newCategory.getCategoryCode());
            o.setPlanCode(commCodeService.commCode(CodePrefixEnum.PLAN));
            return o;
        }).collect(Collectors.toList());
        kpiAccountPlanService.insertBatchSomeColumn(newPlans);

        //oldFormula
        List<KpiIndexFormula> oldFormulas = kpiIndexFormulaService.list(new LambdaQueryWrapper<KpiIndexFormula>().eq(KpiIndexFormula::getPlanCode, oldCategory.getCategoryCode()).eq(KpiIndexFormula::getDelFlag, 0));
        List<KpiIndexFormulaObj> kpiIndexFormulaObjs = kpiIndexFormulaObjMapper.selectList(new LambdaQueryWrapper<KpiIndexFormulaObj>()
                .eq(KpiIndexFormulaObj::getPlanCode, oldCategory.getCategoryCode()));
        Linq.of(oldFormulas).forEach(o -> {
            Long id = o.getId();
            o.setId(null);
            o.setPlanCode(newCategory.getCategoryCode());
            kpiIndexFormulaService.save(o);
            Linq.of(kpiIndexFormulaObjs).where(t -> t.getFormulaId().equals(id)).forEach(x -> {
                x.setId(null);
                x.setPlanCode(newCategory.getCategoryCode());
                x.setFormulaId(o.getId());
            });
        });
        kpiIndexFormulaObjMapper.insertBatchSomeColumn(Linq.of(kpiIndexFormulaObjs).where(t->t.getPlanCode().equals(newCategory.getCategoryCode())).toList());
//        List<KpiIndexFormula> newFormulas = new ArrayList<>();
//
//        for (KpiIndexFormula oldFormula : oldFormulas) {
//            //newFormula
//            KpiIndexFormula newFormula = BeanUtil.copyProperties(oldFormula, KpiIndexFormula.class);
//            newFormula.setPlanCode(newCategory.getCategoryCode());
//            newFormula.setId(null);
//            newFormulas.add(newFormula);
//            kpiIndexFormulaService.save(newFormula);
//
//            //oldMember
//            List<KpiMember> oldMembers = kpiMemberService.list(new LambdaQueryWrapper<KpiMember>().eq(KpiMember::getHostId, newFormula.getId()));
//            //newMembers
//            List<KpiMember> newMembers = oldMembers.stream().map(o -> {
//                o.setId(null);
//                o.setHostId(newFormula.getId());
//                return o;
//            }).collect(Collectors.toList());
//            if (CollectionUtils.isNotEmpty(newMembers)) {
//                kpiMemberService.insertBatchSomeColumn(newMembers);
//            }
//        }

    }
}
