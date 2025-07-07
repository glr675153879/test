package com.hscloud.hs.cost.account.service.imputation;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.constant.enums.imputation.ImputationType;
import com.hscloud.hs.cost.account.model.entity.imputation.Imputation;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationBaseEntity;
import com.hscloud.hs.cost.account.model.vo.imputation.HistoryVO;

/**
 * 归集主档 服务接口类
 */
public interface IImputationService extends IService<Imputation> {

    String generate();

    boolean lock(String cycle);

    boolean unlock(String cycle);

    Imputation getByType(String currentCycle, ImputationType imputationType);

    IPage<HistoryVO> pageHistory(Page<Imputation> page);

    <T extends ImputationBaseEntity<T>> void setImputation(T entity, Long imputationId);
}
