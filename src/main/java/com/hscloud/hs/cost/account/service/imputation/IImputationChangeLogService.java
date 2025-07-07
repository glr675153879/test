package com.hscloud.hs.cost.account.service.imputation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationChangeLog;
import com.hscloud.hs.cost.account.model.vo.imputation.ImputationChangeLogExcel;

import java.util.List;

/**
 * 归集变更日志 服务接口类
 */
public interface IImputationChangeLogService extends IService<ImputationChangeLog> {

    IPage<ImputationChangeLog> pageImputationChangeLog(Page<ImputationChangeLog> page, QueryWrapper<ImputationChangeLog> wrapper, Long imputationId);
    IPage<ImputationChangeLog> pageImputationChangeLog(Page<ImputationChangeLog> page, QueryWrapper<ImputationChangeLog> wrapper);

    List<ImputationChangeLogExcel> exportChangeLog(Long imputationId, String changeType, String changeModel);
    List<ImputationChangeLogExcel> exportChangeLog( String changeType, String changeModel,String imputationCode);
}
