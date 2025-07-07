package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.MaterialChargeQueryDto;
import com.hscloud.hs.cost.account.model.dto.MaterialChargeStatusDto;
import com.hscloud.hs.cost.account.model.entity.MaterialCharge;

/**
 * <p>
 * 人员信息 服务类
 * </p>
 *
 * @author author
 * @since 2023-11-20
 */
public interface IMaterialChargeService extends IService<MaterialCharge> {

    /**
     * 查询匹配和未匹配数据
     *@param  dto 参数
     *@return  vo
     */
    IPage<MaterialCharge> pageList(MaterialChargeQueryDto dto);

    /**
     * 同步更新获取数据小组数据
     *@param  dto 参数
     *@return  dto
     */
    Object syncData(MaterialChargeQueryDto dto);

    /**
     * 启用/停用
     *@param  dto 参数
     *@return
     */
    void switchStatus(MaterialChargeStatusDto dto);
}
