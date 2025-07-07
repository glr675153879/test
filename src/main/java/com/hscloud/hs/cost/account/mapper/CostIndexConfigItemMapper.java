package com.hscloud.hs.cost.account.mapper;

import com.hscloud.hs.cost.account.model.entity.CostIndexConfigItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 核算指标配置项 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2023-09-10
 */
@Mapper
public interface CostIndexConfigItemMapper extends BaseMapper<CostIndexConfigItem> {

    List<CostIndexConfigItem> getByIndexId(Long id);

    void deleteByIndexId(Long id);
}
