package com.hscloud.hs.cost.account.mapper;

import com.hscloud.hs.cost.account.model.dto.AccountUnitIdAndNameDto;
import com.hscloud.hs.cost.account.model.entity.CostCollectionUnit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.vo.CostAccountUnitVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 归集单元表 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2023-09-05
 */
@Mapper
public interface CostCollectionUnitMapper extends BaseMapper<CostCollectionUnit> {

    void saveAccountCollection(@Param("collectionUnitId") Long collectionUnitId,@Param("accountId")  Long accountId);

    List<Long> getAccountUnitIds(Long collectionUnitId);

    void removeAccountUnitIds(Long collectionUnitId);
}
