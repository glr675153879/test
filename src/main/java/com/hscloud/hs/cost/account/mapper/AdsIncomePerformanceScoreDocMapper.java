package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseMapper;
import com.hscloud.hs.cost.account.model.pojo.AdsIncomePerformanceScoreDoc;
import com.hscloud.hs.cost.account.model.vo.DoctorTechAchievementVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * <p>
 * 核算业绩分-医生组 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2023-12-05
 */
@Mapper
public interface AdsIncomePerformanceScoreDocMapper extends MPJBaseMapper<AdsIncomePerformanceScoreDoc> {

    Page<DoctorTechAchievementVo> selectDoctorTechAchievementVo(@Param("page") Page<DoctorTechAchievementVo> page, @Param("accountTime") String accountTime,@Param("unitId")Long unitId,@Param("unitName")String unitName);

    BigDecimal selectTotalCost(@Param("accountTime")String accountTime);
}
