package com.hscloud.hs.cost.account.service.impl.kpi;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiServiceItemMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiServiceItem;
import com.hscloud.hs.cost.account.service.kpi.IKpiServiceItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 浙江省基本医疗保险医疗服务项目目录 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KpiServiceItemService extends ServiceImpl<KpiServiceItemMapper, KpiServiceItem> implements IKpiServiceItemService {
}
