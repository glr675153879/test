package com.hscloud.hs.cost.account.service.imputation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationIndex;

import java.util.List;

/**
 * 归集指标 服务接口类
 */
public interface IImputationIndexService extends IService<ImputationIndex> {

    List<ImputationIndex> listByPId(Long imputationId);

    boolean saveOrUpdateImputationIndex(ImputationIndex imputationIndex);

    boolean removeImputationIndexById(Long id);

    IPage<ImputationIndex> pageImputationIndex(Page<ImputationIndex> page, QueryWrapper<ImputationIndex> wrapper, Long imputationId);
}
