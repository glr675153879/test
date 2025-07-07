package com.hscloud.hs.cost.account.service.impl.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.report.ReportHeadMapper;
import com.hscloud.hs.cost.account.model.entity.report.Report;
import com.hscloud.hs.cost.account.model.entity.report.ReportField;
import com.hscloud.hs.cost.account.model.entity.report.ReportHead;
import com.hscloud.hs.cost.account.service.report.IReportHeadService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表表头 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportHeadService extends ServiceImpl<ReportHeadMapper, ReportHead> implements IReportHeadService {

    private final ReportFieldService reportFieldService;
    @Lazy
    @Resource
    private ReportService reportService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createOrEdit(ReportHead reportHead) {
        // 联动更新字段的别名
        if (Objects.nonNull(reportHead.getFieldId()) && StrUtil.isNotBlank(reportHead.getFieldViewAlias())) {
            reportFieldService.update(Wrappers.<ReportField>lambdaUpdate().eq(ReportField::getId, reportHead.getFieldId()).set(ReportField::getFieldViewAlias, reportHead.getFieldViewAlias()));
        }
        super.saveOrUpdate(reportHead);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        // 创建一个队列，并将要删除的子树的根节点添加到队列中
        Queue<Long> queue = new LinkedList<>();
        queue.add(id);
        // 创建一个集合，用于存储已访问过的节点
        Set<Long> visitedNodes = new HashSet<>();
        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            // 如果当前节点已被访问过，跳过它
            if (visitedNodes.contains(currentId)) {
                continue;
            }
            // 查找当前节点的所有子节点
            List<ReportHead> childNodes = super.list(Wrappers.<ReportHead>lambdaQuery().eq(ReportHead::getParentId, currentId));
            // 将子节点的ID添加到队列中
            for (ReportHead childNode : childNodes) {
                queue.add(childNode.getId());
            }
            // 将当前节点添加到已访问节点的集合中
            visitedNodes.add(currentId);
            // 删除当前节点
            super.removeById(currentId);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSave(Long reportId, List<ReportHead> headsNew) {
        // tree展开为list
        List<ReportHead> headList = tree2List(headsNew);
        // 数据库中删除不存在的ids
        List<Long> ids = headList.stream().map(ReportHead::getId).filter(Objects::nonNull).collect(Collectors.toList());
        super.remove(Wrappers.<ReportHead>lambdaQuery().eq(ReportHead::getReportId, reportId).notIn(CollUtil.isNotEmpty(ids), ReportHead::getId, ids));
        for (ReportHead reportHead : headList) {
            // 联动更新字段的别名
            if (Objects.nonNull(reportHead.getFieldId()) && StrUtil.isNotBlank(reportHead.getFieldViewAlias())) {
                reportFieldService.update(Wrappers.<ReportField>lambdaUpdate().eq(ReportField::getId, reportHead.getFieldId()).set(ReportField::getFieldViewAlias, reportHead.getFieldViewAlias()));
            }
        }
        // 保存或更新
        headList.forEach(e -> {
            e.setReportId(reportId);
        });
        super.saveOrUpdateBatch(headsNew);
        return true;
    }

    public List<Tree<Long>> tree(Long reportId, boolean onlyBasicInfo) {
        List<ReportHead> list = super.list(Wrappers.<ReportHead>lambdaQuery().eq(ReportHead::getReportId, reportId));
        List<ReportField> fieldList = reportFieldService.listByReportId(reportId);
        if (!onlyBasicInfo) {
            for (ReportField reportField : fieldList) {
                reportFieldService.fillFieldFlag(reportField);
            }
        }
        Map<Long, ReportField> fieldMap = fieldList.stream().collect(Collectors.toMap(ReportField::getId, e -> e));
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        treeNodeConfig.setDeep(10);

        return TreeUtil.build(list, 0L, treeNodeConfig, (treeNode, tree) -> {
            tree.setId(treeNode.getId());
            tree.setParentId(Objects.nonNull(treeNode.getParentId()) ? treeNode.getParentId() : 0L);
            tree.setWeight(treeNode.getSort() == null ? 0 : treeNode.getSort());
            tree.put("fieldId", treeNode.getFieldId());
            tree.put("sort", treeNode.getSort());
            if (fieldMap.containsKey(treeNode.getFieldId())) {
                ReportField reportField = fieldMap.get(treeNode.getFieldId());
                tree.put("fieldViewAlias", reportField.getFieldViewAlias());
                tree.put("fieldText", reportField.getFieldText());
                tree.put("fieldType", reportField.getFieldType());
                tree.put("fieldName", reportField.getFieldName());
                tree.put("reportDbId", reportField.getReportDbId());
                tree.put("reportId", reportField.getReportId());
                tree.put("sonReportFlag", reportField.getSonReportFlag());
                tree.put("formulaFlag", reportField.getFormulaFlag());
                tree.put("searchFlag", reportField.getSearchFlag());
            }
        });
    }

    @Override
    public List<ReportHead> listByReportId(Long sourceReportId) {
        return super.list(Wrappers.<ReportHead>lambdaQuery().eq(ReportHead::getReportId, sourceReportId));
    }

    @Override
    public List<Tree<Long>> treeByReportCode(String reportCode) {
        Report report = reportService.getByReportCode(reportCode);
        if (Objects.isNull(report)) {
            throw new BizException("报表不存在");
        }
        return tree(report.getId(), false);
    }

    public void removeByReportId(Long reportId) {
        super.remove(Wrappers.<ReportHead>lambdaQuery().eq(ReportHead::getReportId, reportId));
    }

    /**
     * tree2List
     */
    public static List<ReportHead> tree2List(List<ReportHead> heads) {
        if (CollUtil.isEmpty(heads)) {
            return heads;
        }
        List<ReportHead> headList = new ArrayList<>(heads);
        // 若子节点不为空，则将子节点添加至所属父节点信息中
        for (ReportHead head : heads) {
            if (CollectionUtil.isNotEmpty(head.getChildren())) {
                headList.addAll(tree2List(head.getChildren()));
            }
        }
        return headList;

    }

}
