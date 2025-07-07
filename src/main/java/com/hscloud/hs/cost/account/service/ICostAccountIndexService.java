package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.*;
import com.hscloud.hs.cost.account.model.entity.CostAccountIndex;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.vo.CostAccountIndexVo;
import com.hscloud.hs.cost.account.model.vo.ValidatorResultVo;

/**
 * <p>
 * 核算指标表 服务类
 * </p>
 *
 * @author 
 * @since 2023-09-04
 */
public interface ICostAccountIndexService extends IService<CostAccountIndex> {


    /**
     * 核算指标分页查询
     * @param queryDto
     * @return
     *
     */
    IPage getAccountIndexPage(CostAccountIndexQueryDto queryDto);

    /**
     * 新增或修改核算指标
     * @param accountIndexDto
     */
    void saveOrUpdateAccountIndex(CostAccountIndexDto accountIndexDto);

    Boolean updateStatusAccountIndex(CostAccountIndexStatusDto dto);

    /**
     *核算指标校验
     * @param dto
     * @return
     */
    ValidatorResultVo verificationAccountIndex(CostAccountIndexVerificationDto dto);

    /**
     * 计算核算项
     * @param id   核算配置项id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param objectId  核算对象id
     * @return
     */
    ValidatorResultVo verificationItem(Long id, String startTime, String endTime, String objectId,String objectIdType);

    /**
     * 分摊计算核算项
     * @param costIndexConfigItemDto   配置信息
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param objectId  核算对象id
     * @return
     */
    ValidatorResultVo verificationRuleItem(CostIndexConfigItemDto costIndexConfigItemDto,String dimension ,String startTime, String endTime, String objectId,String objectIdType);

    /**
     * 指标计算
     * @param id  指标id
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param objectId    核算对象id
     * @return
     */
    ValidatorResultVo verificationIndex(Long id, String startTime,String endTime,String objectId);

    CostAccountIndexVo getAccountIndexById(Long id);

    void deleteById(Long id);

    void updateSystemIndex(CostAccountIndexDto accountIndexDto);
}
