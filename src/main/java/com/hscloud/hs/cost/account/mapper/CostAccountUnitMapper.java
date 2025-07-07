package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.CostAccountUnitQueryDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountUnitQueryDtoNew;
import com.hscloud.hs.cost.account.model.dto.CostUnitRelateInfoDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.CostUnitExcludedInfo;
import com.pig4cloud.pigx.common.data.datascope.PigxBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CostAccountUnitMapper extends PigxBaseMapper<CostAccountUnit> {


    @Select("select name from cost_account_unit where id = #{id}")
    String getByName(Long id);

     List<CostUnitRelateInfoDto> selectRelateInfoByUnitId(@Param("unitId") Long unitId);

    List<CostUnitExcludedInfo> selectExcludeInfoByUnitId(@Param("unitId") Long unitId);

    void insertRelateInfo(@Param("unitId") Long unitId, @Param("name") String name, @Param("relateId") String relateId,@Param("type") String type,@Param("code") String code);

    void removeRelateInfoByUnitId(@Param("unitId") Long unitId);

    void removeExcludedInfoByUnitId(@Param("unitId") Long unitId);

    IPage<CostAccountUnitQueryDto> list(Page page, @Param("input") CostAccountUnitQueryDto input);

    String selectStatusById(@Param("id") Long id);

    void updateStatusById(@Param("id") Long id,@Param("status") String status);

    IPage<CostAccountUnit> listByQueryDto(Page page,@Param("query") CostAccountUnitQueryDtoNew input);
}
