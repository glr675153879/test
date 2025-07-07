package com.hscloud.hs.cost.account.service.impl.second;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.second.GrantUnitLogMapper;
import com.hscloud.hs.cost.account.model.entity.second.GrantUnitLog;
import com.hscloud.hs.cost.account.service.second.IGrantUnitLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 发放单元操作日志 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GrantUnitLogService extends ServiceImpl<GrantUnitLogMapper, GrantUnitLog> implements IGrantUnitLogService {


}
