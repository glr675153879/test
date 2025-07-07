package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.mapper.CostAccountIndexMapper;
import com.hscloud.hs.cost.account.mapper.CostAccountItemMapper;
import com.hscloud.hs.cost.account.mapper.CostCommentGroupMapper;
import com.hscloud.hs.cost.account.model.dto.CostCommentGroupDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountIndex;
import com.hscloud.hs.cost.account.model.entity.CostAccountItem;
import com.hscloud.hs.cost.account.model.entity.CostBaseGroup;
import com.hscloud.hs.cost.account.model.entity.second.Programme;
import com.hscloud.hs.cost.account.model.vo.CostGroupListVo;
import com.hscloud.hs.cost.account.service.ICostBaseGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.service.second.IProgrammeService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 核算指标分组 服务实现类
 * </p>
 *
 * @author
 * @since 2023-09-04
 */
@Service
public class CostBaseGroupServiceImpl extends ServiceImpl<CostCommentGroupMapper, CostBaseGroup> implements ICostBaseGroupService {


    @Autowired
    private CostAccountItemMapper accountItemMapper;

    @Autowired
    private CostAccountIndexMapper accountIndexMapper;
    @Autowired
    private IProgrammeService programmeService;

    /**
     * 新增或修改指标分组
     *
     * @param dto
     */
    @Override
    public Long saveOrUpdateBaseGroup(CostCommentGroupDto dto) {

        if (StrUtil.isEmpty(dto.getName())) {
            throw new BizException("分组名称不能为空");
        }
//        LambdaQueryWrapper<CostBaseGroup> queryWrapper = new LambdaQueryWrapper();
//        queryWrapper.eq(CostBaseGroup::getName, dto.getName())
//                .eq(CostBaseGroup::getTypeGroup,dto.getTypeGroup())
//                .eq(CostBaseGroup::getDelFlag,'0');
        CostBaseGroup one = this.getById(dto.getId());
        LambdaQueryWrapper<CostBaseGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CostBaseGroup::getId, dto.getId())
                .eq(CostBaseGroup::getIsReport, true);
        CostBaseGroup reportGroup = this.getOne(wrapper);
        if (reportGroup != null) {
            throw new BizException("数据上报核算项不允许修改");
        }
        CostBaseGroup costIndexGroup = BeanUtil.copyProperties(dto, CostBaseGroup.class);
        if (dto.getId() == null && one != null) {
            throw new BizException("该分组已存在");
        }
        if (dto.getId() != null && one != null) {
            //如果该分组被停用，则更新核算项、核算指标等数据
            if (!dto.getStatus().equals(one.getStatus())) {
                //同步修改核算项
                List<CostAccountItem> costAccountItems = new CostAccountItem().selectList(new LambdaQueryWrapper<CostAccountItem>().eq(CostAccountItem::getGroupId, dto.getId()));
                for (CostAccountItem costAccountItem : costAccountItems) {
                    costAccountItem.setStatus(dto.getStatus());
                    accountItemMapper.updateById(costAccountItem);
                }
                //同步修改指标
                List<CostAccountIndex> costAccountIndexList = new CostAccountIndex().selectList(new LambdaQueryWrapper<CostAccountIndex>().eq(CostAccountIndex::getIndexGroupId, dto.getId()));
                for (CostAccountIndex costAccountIndex : costAccountIndexList) {
                    costAccountIndex.setStatus(dto.getStatus());
                    accountIndexMapper.updateById(costAccountIndex);
                }
            }
        }
        saveOrUpdate(costIndexGroup);

        return costIndexGroup.getId();
    }

    /**
     * 获取核算项分组列表
     *
     * @param
     * @return
     */
    @Override
    public List<CostGroupListVo> listAccountItemGroup(String status, String typeGroup) {
        LambdaQueryWrapper<CostBaseGroup> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(CostBaseGroup::getDelFlag, '0')
                .eq(CostBaseGroup::getTypeGroup, typeGroup)
                .eq(StrUtil.isNotEmpty(status), CostBaseGroup::getStatus, status);
        List<CostBaseGroup> list = this.list(queryWrapper);
        List<CostGroupListVo> children = findChildren(list);
        return children;
    }

    /**
     * 获取核算指标分组列表
     *
     * @param
     * @return
     */
    @Override
    public List<CostGroupListVo> listAccountIndexGroup(String status) {
        LambdaQueryWrapper<CostBaseGroup> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(CostBaseGroup::getDelFlag, '0')
                .eq(CostBaseGroup::getTypeGroup, "KPI_BASIC_QUOTA")
                .eq(StrUtil.isNotEmpty(status), CostBaseGroup::getStatus, status);
        List<CostBaseGroup> list = this.list(queryWrapper);
        List<CostGroupListVo> children = findChildren(list);
        return children;
    }

    @Override
    public Boolean deleteGroup(Long id) {

        LambdaQueryWrapper<CostBaseGroup> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(CostBaseGroup::getParentId, id);
        List<CostBaseGroup> list = this.list(queryWrapper);
        if (CollUtil.isNotEmpty(list)) {
            throw new BizException("当前分组中还有子分组，需要先删除所有子分组后才能继续删除当前分组");
        }
        LambdaQueryWrapper<CostAccountItem> costAccountQueryWrapper = new LambdaQueryWrapper();
        costAccountQueryWrapper.eq(CostAccountItem::getGroupId, id);
        List<CostAccountItem> costAccountItems = accountItemMapper.selectList(costAccountQueryWrapper);
        if (CollUtil.isNotEmpty(costAccountItems)) {
            throw new BizException("当前子分组中还有核算项，需要先删除所有核算项后才能继续删除当前子分组");
        }

        LambdaQueryWrapper<CostAccountIndex> costAccountIndexQueryWrapper = new LambdaQueryWrapper();
        costAccountIndexQueryWrapper.eq(CostAccountIndex::getIndexGroupId, id);
        List<CostAccountIndex> costAccountIndices = accountIndexMapper.selectList(costAccountIndexQueryWrapper);
        if (CollUtil.isNotEmpty(costAccountIndices)) {
            throw new BizException("当前子分组中还有核算指标，需要先删除所有核算项后才能继续删除当前子分组");
        }

        //判断是否有方案
        if (programmeService.exists(Wrappers.<Programme>lambdaQuery().eq(Programme::getPlanGroupId, id))) {
            throw new BizException("当前子分组中还有二次分配公共方案，需要先删除所有方案后才能继续删除当前子分组");
        }
        return this.removeById(id);
    }

    @Override
    public List<CostGroupListVo> listBaseGroup(String typeGroup, String isSystem) {
        LambdaQueryWrapper<CostBaseGroup> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(CostBaseGroup::getDelFlag, '0')
                .eq(StrUtil.isNotEmpty(typeGroup), CostBaseGroup::getTypeGroup, typeGroup)
                .eq(StrUtil.isNotEmpty(isSystem), CostBaseGroup::getIsSystem, isSystem);
        List<CostBaseGroup> list = this.list(queryWrapper);
        List<CostGroupListVo> children = findChildren(list);
        return children;
    }

    @Override
    public List<CostGroupListVo> listGroupTree(String typeGroup, String status) {
        LambdaQueryWrapper<CostBaseGroup> wrapper = Wrappers.<CostBaseGroup>lambdaQuery().eq(CostBaseGroup::getDelFlag, '0').eq(CostBaseGroup::getTypeGroup, typeGroup)
                .eq(StrUtil.isNotBlank(status), CostBaseGroup::getStatus, status);
        List<CostBaseGroup> costBaseGroups = list(wrapper);
        if (CollUtil.isEmpty(costBaseGroups)) {
            return Collections.emptyList();
        }
        return buildBaseGroupTree(costBaseGroups);
    }

    /**
     * 构建菜单树
     */
    private List<CostGroupListVo> buildBaseGroupTree(List<CostBaseGroup> costBaseGroups) {

        List<CostGroupListVo> costGroupListVos = BeanUtil.copyToList(costBaseGroups, CostGroupListVo.class);

        Map<Long, CostGroupListVo> groupMap = costGroupListVos.stream().collect(Collectors.toMap(CostGroupListVo::getId, Function.identity()));

        List<CostGroupListVo> rootNodes = costGroupListVos.stream().filter(item -> Objects.isNull(item.getParentId())).collect(Collectors.toList());

        //构建树结构
        for (CostBaseGroup costBaseGroup : costBaseGroups) {
            if (costBaseGroup.getParentId() != null) {
                CostGroupListVo parentGroup = groupMap.get(costBaseGroup.getParentId());
                CostGroupListVo childGroup = groupMap.get(costBaseGroup.getId());
                if (parentGroup != null) {
                    parentGroup.getGroupList().add(childGroup);
                }
            }
        }

        return rootNodes;
    }


    /**
     * 获取子级菜单
     *
     * @param list
     * @return
     */
    private List<CostGroupListVo> findChildren(List<CostBaseGroup> list) {
        List<CostGroupListVo> listVos = new ArrayList<>();
        for (CostBaseGroup group : list) {
            if (group.getParentId() == null) {
                CostGroupListVo costGroupListVo = BeanUtil.copyProperties(group, CostGroupListVo.class);
                listVos.add(costGroupListVo);
            }
        }

        for (CostGroupListVo listVo : listVos) {
            Long id = listVo.getId();
            List<CostGroupListVo> secondLevelMenus = new ArrayList<>();
            for (CostBaseGroup group : list) {
                if (id.equals(group.getParentId())) {
                    CostGroupListVo costGroupListVo = BeanUtil.copyProperties(group, CostGroupListVo.class);
                    secondLevelMenus.add(costGroupListVo);
                    listVo.setGroupList(secondLevelMenus);
                }
            }
        }
        return listVos;
    }
}
