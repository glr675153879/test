package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountRelationVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountUnitVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
* 核算单元 服务接口类
 * @author Administrator
 */
public interface KpiAccountUnitService extends IService<KpiAccountUnit> {

    /**
     * 保存核算单元
     * @param dto 入参
     * @return id
     */
    Long saveOrUpdate(KpiAccountUnitDTO dto);

    /**
     * 状态切换
     * @param dto 入参
     */
    void switchStatus(BaseIdStatusDTO dto);

    /**
     * 删除核算单元
     * @param id 主键id
     */
    void deleteUnit(Long id);

    /**
     * 科室单元分页列表
     * @param dto 入参
     * @return 结果集
     */
    IPage<KpiAccountUnitVO> getUnitPageList(KpiAccountUnitQueryDTO dto);

    /**
     * 科室单元全部列表
     * @param dto 入参
     * @return 结果集
     */
    List<KpiAccountUnitVO> getUnitList(KpiAccountUnitQueryDTO dto);

    /**
     * 核算单元操作检测
     * @param id 主键id
     * @return 0-未被应用 1-已被应用
     */
    Integer unitCheck(Long id);

    /**
     * 查询单条记录
     * @param id 主键id
     * @return 单条记录
     */
    KpiAccountUnitVO getUnit(Long id);

    /**
     * 查询科室单元map
     * @param busiType 业务类型
     * @return map
     */
    Map<Long,String> getUnitMap(String busiType);

        /**
     * 查询科室单元map
     * @param busiType 业务类型
     * @return map
     */
    Map<String,Long> getUnitMapV2(String busiType);

    /**
     * 保存核算单元关系保存
     * @param dto 入参
     */
    void saveAccountRelation(KpiAccountRelationDTO dto);

    /**
     * 核算单元关系列表
     * @param dto 入参
     */
    IPage<KpiAccountRelationVO> getAccountRelationPageList(KpiAccountRelationQueryDTO dto);

    List<KpiAccountRelationVO> getAccountRelationPageList2(KpiAccountRelationQueryDTO dto);

    /**
     * 核算单元关系复制
     * @param id id
     */
    void accountRelationCopy(Long id);

    /**
     * 分组内医护关系导入
     * @param categoryCode 分组code
     * @param busiType 业务类型
     * @param file 文件
     * @return 返回文件名
     */
    String accountRelationImport(String categoryCode,String busiType, MultipartFile file);

    /**
     * 医护关系自动匹配
     * @param categoryCode 分组code
     * @param busiType 业务类型
     */
    void accountRelationMatch(String categoryCode,String busiType);

    List<String> importData(List<Map<Integer, String>> list, String overwriteFlag);
}
