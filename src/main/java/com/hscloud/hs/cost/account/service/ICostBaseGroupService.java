package com.hscloud.hs.cost.account.service;

import com.hscloud.hs.cost.account.model.dto.CostCommentGroupDto;
import com.hscloud.hs.cost.account.model.entity.CostBaseGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.vo.CostGroupListVo;

import java.util.List;

/**
 * <p>
 * 核算指标分组 服务类
 * </p>
 *
 * @author 
 * @since 2023-09-04
 */
public interface ICostBaseGroupService extends IService<CostBaseGroup> {

    /**
     * 保存或修改分组
     *
     * @param dto
     */
        Long saveOrUpdateBaseGroup(CostCommentGroupDto dto);


    /**
     * 获取核算项分组列表
     *
     * @param
     */
    List<CostGroupListVo> listAccountItemGroup(String status,String typeGroup);

    /**
     * 获取核算指标分组列表
     *
     * @param
     */
    List<CostGroupListVo> listAccountIndexGroup(String status);


    /**
     * 删除分组
     */
    Boolean deleteGroup(Long id);

    List<CostGroupListVo> listBaseGroup(String typeGroup,String isSystem);

    List<CostGroupListVo> listGroupTree(String typeGroup, String status);
}
