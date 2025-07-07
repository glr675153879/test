package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.*;
import com.hscloud.hs.cost.account.model.entity.CostAccountProportion;
import com.hscloud.hs.cost.account.model.vo.CostAccountListVo;
import com.hscloud.hs.cost.account.model.vo.CostAccountProportionListVo;
import com.hscloud.hs.cost.account.model.vo.CostAccountProportionVo;

import java.util.List;

/**
 * 核算比例service接口
 * @author banana
 * @create 2023-09-13 15:10
 */
public interface ICostAccountProportionService extends IService<CostAccountProportion> {

    /**
     * 新增核算比例
     * @param input 入参
     */
    public CostAccountListVo CostAccountProportionAdd(CostAccountProportionAddDto input);

    /**
     * 查询指定核算项的核算比例信息
     * @param input 核算项id 及 相关查询信息
     * @return 核算项比例信息
     */
    public List<CostAccountProportionListVo> CostAccountProportionList(CostAccountProportionListDto input);


    /**
     * 查询核算项信息
     * @param input 查询过滤条件
     * @return 查询到的核算项信息
     */
    public IPage<CostAccountListVo> CostAccountList(CostAccountListDto input);

    /**
     * 切换指定id核算项的状态
     * @param input 指定id 和 状态
     */
    public Boolean CostAccountProportionChange(CostAccountProportionStatusDto input);


    /**
     * 编辑核算项信息
     * @param input
     */
    public void CostAccountProportionEdit(List<CostAccountProportionEditDto> input);

    /**
     * 获取对应的核算比例数据
     * @param input 业务id列表
     * @return 核算比例数据
     */
    List<CostAccountProportionVo> getProportion(CostAccountProportionDto input);
}
