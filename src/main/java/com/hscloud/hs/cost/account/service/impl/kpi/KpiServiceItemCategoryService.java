package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiServiceItemCategoryMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiServiceItem;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiServiceItemCategory;
import com.hscloud.hs.cost.account.service.kpi.IKpiServiceItemCategoryService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 医疗服务目录 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KpiServiceItemCategoryService extends ServiceImpl<KpiServiceItemCategoryMapper, KpiServiceItemCategory> implements IKpiServiceItemCategoryService {

    private final KpiServiceItemService kpiServiceItemService;

    @Override
    public boolean checkAndAdd(KpiServiceItemCategory kpiServiceItemCategory) {
        // boolean isLegal = ServiceItemUtil.checkCode(kpiServiceItemCategory.getCode());
        // if (!isLegal) {
        //     throw new BizException("编码不符合要求");
        // }
        boolean exists = super.exists(Wrappers.<KpiServiceItemCategory>lambdaQuery().eq(KpiServiceItemCategory::getCode, kpiServiceItemCategory.getCode()));
        if (exists) {
            throw new BizException("编码已存在");
        }
        // String parentCode = kpiServiceItemCategory.parentCode();
        // if (StrUtil.isNotBlank(parentCode)) {
        //     boolean parentExists = super.exists(Wrappers.<KpiServiceItemCategory>lambdaQuery().eq(KpiServiceItemCategory::getCode, parentCode));
        //     if (!parentExists) {
        //         throw new BizException("请先录入父目录");
        //     }
        // }

        return super.save(kpiServiceItemCategory);
    }


    @Override
    public boolean checkAndEdit(KpiServiceItemCategory kpiServiceItemCategory) {
        // boolean isLegal = ServiceItemUtil.checkCode(kpiServiceItemCategory.getCode());
        // if (!isLegal) {
        //     throw new BizException("编码不符合要求");
        // }
        boolean exists = super.exists(Wrappers.<KpiServiceItemCategory>lambdaQuery().eq(KpiServiceItemCategory::getCode, kpiServiceItemCategory.getCode()).ne(KpiServiceItemCategory::getId,
                kpiServiceItemCategory.getId()));
        if (exists) {
            throw new BizException("编码已存在");
        }
        // String parentCode = kpiServiceItemCategory.parentCode();
        // boolean parentExists = super.exists(Wrappers.<KpiServiceItemCategory>lambdaQuery().eq(KpiServiceItemCategory::getCode, parentCode));
        // if (!parentExists) {
        //     throw new BizException("请先录入父目录");
        // }
        return super.updateById(kpiServiceItemCategory);
    }

    @Override
    public void checkAndDelete(Long id) {
        KpiServiceItem item = kpiServiceItemService.getById(id);
        if (item == null) {
            return;
        }
        // 检查分类下是否有数据
        boolean exists = kpiServiceItemService.exists(Wrappers.<KpiServiceItem>lambdaQuery().eq(KpiServiceItem::getItemCode, item.getItemCode()));
        if (exists) {
            throw new BizException("分类下有数据，不能删除");
        }
        kpiServiceItemService.removeById(id);
    }

    @Override
    public List<KpiServiceItemCategory> tree() {
        List<KpiServiceItemCategory> list = super.list(Wrappers.<KpiServiceItemCategory>lambdaQuery());

        List<KpiServiceItemCategory> topLevenNodes = new ArrayList<>();

        // 每个节点找到自己的父节点，如果没有父节点，则认为是顶级节点
        // 父节点规则（前缀匹配最长的为父节点  例如 abc、ab、a， 则认为ab是abc的父节点）
        for (KpiServiceItemCategory currentNode : list) {
            String code = currentNode.getCode();
            if (StrUtil.length(code) == 0 || StrUtil.length(code) == 1) {
                topLevenNodes.add(currentNode);
                continue;
            }
            // 尝试匹配父节点
            // 比如code为dree，则按顺序尝试 dre，dr，d是否存在
            boolean noParent = true;
            String parentCode = code;
            do {
                parentCode = parentCode.substring(0, parentCode.length() - 1);
                // log.info("code：{}， parentCode:{}", code, parentCode);
                String finalParentCode = parentCode;
                List<KpiServiceItemCategory> parentNodes = list.stream().filter(e -> Objects.equals(finalParentCode, e.getCode())).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(parentNodes)) {
                    noParent = false;
                    for (KpiServiceItemCategory serviceItemCategory : parentNodes) {
                        List<KpiServiceItemCategory> children = serviceItemCategory.getChildren();
                        if (children == null) {
                            serviceItemCategory.setChildren(CollUtil.newArrayList(currentNode));
                        } else {
                            children.add(currentNode);
                        }
                    }
                    break;
                }
            } while (StrUtil.length(parentCode) > 0);
            if (noParent) {
                topLevenNodes.add(currentNode);
            }
        }
        return topLevenNodes;
    }

    @Override
    public List<KpiServiceItemCategory> cutTree(String selectedItemCodes) {
        List<KpiServiceItemCategory> list = super.list(Wrappers.<KpiServiceItemCategory>lambdaQuery());
        List<KpiServiceItemCategory> filterList = new ArrayList<>();
        if (StrUtil.isNotBlank(selectedItemCodes)) {
            List<String> split = StrUtil.split(selectedItemCodes, ",");
            for (KpiServiceItemCategory kpiServiceItemCategory : list) {
                for (String s : split) {
                    if (StrUtil.startWith(s, kpiServiceItemCategory.getCode())) {
                        filterList.add(kpiServiceItemCategory);
                        break;
                    }
                }
            }
        } else {
            filterList.addAll(list);
        }
        List<KpiServiceItemCategory> topLevenNodes = new ArrayList<>();

        // 每个节点找到自己的父节点，如果没有父节点，则认为是顶级节点
        // 父节点规则（前缀匹配最长的为父节点  例如 abc、ab、a， 则认为ab是abc的父节点）
        for (KpiServiceItemCategory currentNode : filterList) {
            String code = currentNode.getCode();
            if (StrUtil.length(code) == 0 || StrUtil.length(code) == 1) {
                topLevenNodes.add(currentNode);
                continue;
            }
            // 尝试匹配父节点
            // 比如code为dree，则按顺序尝试 dre，dr，d是否存在
            boolean noParent = true;
            String parentCode = code;
            do {
                parentCode = parentCode.substring(0, parentCode.length() - 1);
                // log.info("code：{}， parentCode:{}", code, parentCode);
                String finalParentCode = parentCode;
                List<KpiServiceItemCategory> parentNodes = filterList.stream().filter(e -> Objects.equals(finalParentCode, e.getCode())).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(parentNodes)) {
                    noParent = false;
                    for (KpiServiceItemCategory serviceItemCategory : parentNodes) {
                        List<KpiServiceItemCategory> children = serviceItemCategory.getChildren();
                        if (children == null) {
                            serviceItemCategory.setChildren(CollUtil.newArrayList(currentNode));
                        } else {
                            children.add(currentNode);
                        }
                    }
                    break;
                }
            } while (StrUtil.length(parentCode) > 0);
            if (noParent) {
                topLevenNodes.add(currentNode);
            }
        }
        return topLevenNodes;
    }


    public static void main(String[] args) {

    }
}
