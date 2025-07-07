package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.AccountStatementDetailQueryDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountStatementQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostTaskExecuteResult;
import com.hscloud.hs.cost.account.model.vo.*;

import java.util.List;

/**
 * @author 小小w
 * @date 2023/9/21 16:28
 */
public interface CostAccountStatementService extends IService<CostTaskExecuteResult> {



    /**
     * 绩效结果
     * @param queryDto
     * @return
     */
    IPage<CostResultStatementVo> resultStatementPage(CostAccountStatementQueryDto queryDto);

    List<CostResultStatementVo> resultStatementDetail(AccountStatementDetailQueryDto queryDto);

    List<ResultExcelVo> exportResult();

    /**
     * 分组
     * @param queryDto
     * @return
     */
    IPage<CostResultStatementVo> groupStatementPage(CostAccountStatementQueryDto queryDto);

    List<CostResultStatementVo> groupStatementDetail(AccountStatementDetailQueryDto queryDto);

    List<GroupExcelVo> exportGroup();

    /**
     * 核算单元
     * @param queryDto
     * @return
     */
    IPage<CostResultStatementVo> unitStatementPage(CostAccountStatementQueryDto queryDto);

    List<CostResultStatementVo> unitStatementDetail(AccountStatementDetailQueryDto queryDto);

    List<UnitExcelVo> exportUnit();


    /**
     * 指标
     * @param queryDto
     * @return
     */
    IPage<CostResultStatementVo> indexStatementPage(CostAccountStatementQueryDto queryDto);

    List<CostResultStatementVo> indexStatementDetail(AccountStatementDetailQueryDto queryDto);

    /**
     * 核算值分布
     * @param queryDto
     * @return
     */
    List<CostUnitIndexVo> itemStatementDetail(AccountStatementDetailQueryDto queryDto);

    List<IndexExcelVo> exportIndex();

}
