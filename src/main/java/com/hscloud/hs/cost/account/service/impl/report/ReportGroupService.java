package com.hscloud.hs.cost.account.service.impl.report;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.report.ReportGroupMapper;
import com.hscloud.hs.cost.account.model.entity.report.ReportGroup;
import com.hscloud.hs.cost.account.service.report.IReportGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 报表分组 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportGroupService extends ServiceImpl<ReportGroupMapper, ReportGroup> implements IReportGroupService {


}
