package com.hscloud.hs.cost.account.service.dataReport;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportItem;
import com.hscloud.hs.cost.account.model.dto.dataReport.CostReportItemPageDto;

/**
* 上报项 服务接口类
*/
public interface ICostReportItemService extends IService<CostReportItem> {

    Boolean activate(CostReportItem costReportItem);

    IPage<CostReportItemPageDto> pageData(Page<CostReportItem> page, QueryWrapper<CostReportItem> wrapper);

    Boolean isUsed(Long id);
}
