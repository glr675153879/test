package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.CostTaskSnapshot;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务快照表
 * 
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-14 17:44:17
 */
@Mapper
public interface CostTaskSnapshotMapper extends BaseMapper<CostTaskSnapshot> {
	
}
