package com.hscloud.hs.cost.account.service;

import com.hscloud.hs.cost.account.model.dto.CostDataChangeRecordCountDto;
import com.hscloud.hs.cost.account.model.entity.CostDataChangeRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.vo.CostDataChangeRecordCountVo;
import com.hscloud.hs.cost.account.model.vo.CostDataChangeRecordVo;

import java.util.List;

/**
 * <p>
 * 数据变更记录 服务类
 * </p>
 *
 * @author 
 * @since 2023-09-11
 */
public interface ICostDataChangeRecordService extends IService<CostDataChangeRecord> {

    List<CostDataChangeRecordVo> getChangeRecord(String bizCode);

    CostDataChangeRecordCountVo getCount(CostDataChangeRecordCountDto dto);
}
