package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.BindDocNDto;
import com.hscloud.hs.cost.account.model.dto.listDocNRelationDto;
import com.hscloud.hs.cost.account.model.entity.CostDocNRelation;
import com.hscloud.hs.cost.account.model.vo.DocNRelationListVo;

/**
 * 医护对应组Service接口
 * @author banana
 * @create 2023-09-11 14:02
 */
public interface ICostDocNRelationService extends IService<CostDocNRelation> {

    /**
     * 医护对应关系绑定
     * @param input 医生id和护士id入参
     */
    public void bindDocNRelation(BindDocNDto input);


    /**
     * 获取医护对应组列表
     * @param input 入参
     * @return 医护对应关系
     */
    public IPage<DocNRelationListVo> listDocNRelation(listDocNRelationDto input);

}
