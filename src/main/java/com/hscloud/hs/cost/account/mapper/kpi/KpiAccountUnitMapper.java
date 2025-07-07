package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountRelationQueryDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountUnitQueryDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountDocNurseVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountRelationVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountUnitVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* 核算单元 Mapper 接口
*
*/
@Mapper
public interface KpiAccountUnitMapper extends BaseMapper<KpiAccountUnit> {

    /**
     * 核算单元关系列表（分页）
     * @param ids 查询参数
     * @return 分页数据
     */
    List<KpiAccountRelationVO> getAccountRelationPageList(@Param("ids")List<Long> ids,
                                                           @Param("dto") KpiAccountRelationQueryDTO dto,
                                                           @Param("categoryCode") String categoryCode,
                                                           @Param("memberType") String memberType);

    /**
     * 查询id数组
     * @param page 分页
     * @param dto 参数
     * @param categoryCode 参数
     * @param memberType 参数
     * @return 分页数据
     */
    IPage<Long> getAccountRelationIdList(Page<Long> page,
                                        @Param("dto") KpiAccountRelationQueryDTO dto,
                                        @Param("categoryCode") String categoryCode,
                                        @Param("memberType") String memberType);


    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM kpi_account_unit WHERE tenant_id=#{tenantId} and  busi_type = #{busiType}")
    List<KpiAccountUnit> selectAllUnit(@Param("tenantId") Long tenantId,@Param("busiType") String busiType);

    @Select("select virtual_unit_id from hsx_deptcost.dc_virtual_kpi_account_unit where del_flag = '0' and status = '1'")
    List<Long> getVirtualIds();


    IPage<KpiAccountUnitVO> getAccountUnit(Page page, @Param("input") KpiAccountUnitQueryDTO dto);

}

