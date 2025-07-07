package com.hscloud.hs.cost.account.service.report;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.report.ReportHead;

import java.util.List;

/**
 * 报表表头 服务接口类
 */
public interface IReportHeadService extends IService<ReportHead> {

    Boolean createOrEdit(ReportHead reportHead);

    Boolean deleteById(Long id);

    /**
     * 根据报表id组装树
     * 每层按照创建时间或sort字段正序排列
     *
     * @param id            同上
     * @param onlyBasicInfo 仅主表数据
     * @return {@link List}<{@link Tree}<{@link Long}>>
     */
    List<Tree<Long>> tree(Long id, boolean onlyBasicInfo);

    boolean batchSave(Long reportId, List<ReportHead> heads);

    List<ReportHead> listByReportId(Long sourceReportId);

    List<Tree<Long>> treeByReportCode(String reportCode);
}
