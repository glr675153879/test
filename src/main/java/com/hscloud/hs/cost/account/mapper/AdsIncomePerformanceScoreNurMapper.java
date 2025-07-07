package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.pojo.AdsIncomePerformanceScoreNur;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.vo.NurseAchievementVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * <p>
 * 对应绩效套标《10-核算业绩分》- 护理组 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2023-11-29
 */
@Mapper
public interface AdsIncomePerformanceScoreNurMapper extends BaseMapper<AdsIncomePerformanceScoreNur> {

    Page<NurseAchievementVo> selectNurseAchievementVo(@Param("page") Page<NurseAchievementVo> page, @Param("accountTime") String accountTime,@Param("unitId") Long unitId,@Param("unitName")String unitName);

    BigDecimal selectTotalCost(@Param("accountTime") String accountTime);
}
