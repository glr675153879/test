package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiPlanCacheDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountPlanChild;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountPlanChildListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* 核算子方案表 Mapper 接口
*
*/
@Mapper
public interface KpiAccountPlanChildMapper extends BaseMapper<KpiAccountPlanChild> {

    void updateStatus(@Param("ids") String ids, @Param("status") String status);

    List<KpiPlanCacheDto.ObjCategory> getObjCategory();

    List<KpiPlanCacheDto.ObjCategory> getObjCategoryByUserId(@Param("userId") Long userId);

    List<KpiAccountPlanChildListVO> getUserPlanChild(@Param("planCode") String planCode,@Param("indexCode") String indexCode,@Param("userIds") List<Long> userIds);

    List<KpiAccountPlanChildListVO> getDeptPlanChild(@Param("planCode") String planCode,@Param("indexCode") String indexCode,@Param("deptIds") List<Long> deptIds);


}

