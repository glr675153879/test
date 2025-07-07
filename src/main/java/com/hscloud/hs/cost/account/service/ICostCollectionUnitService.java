package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.CostCollectionUnitDto;
import com.hscloud.hs.cost.account.model.dto.CostCollectionUnitQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostCollectionUnit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.vo.CostCollectionUnitVo;

import java.util.List;

/**
 * <p>
 * 归集单元表 服务类
 * </p>
 *
 * @author 
 * @since 2023-09-05
 */
public interface ICostCollectionUnitService extends IService<CostCollectionUnit> {

    void saveCollectionUnit(CostCollectionUnitDto dto);

    Boolean updateCollectionUnit(CostCollectionUnitDto dto);

    IPage listCollectionUnit(CostCollectionUnitQueryDto queryDto);

    void deleteCollectionUnitById(Long id);

    Boolean updateStatusCollectionUnit(CostCollectionUnitDto dto);
}
