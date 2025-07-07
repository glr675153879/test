package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.DoorSectionEntity;
import com.hscloud.hs.cost.account.model.vo.DoorSectionVo;

/**
 * 门诊收入
 *
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-07 15:01:08
 */
public interface DoorSectionService extends IService<DoorSectionEntity> {

    Page<DoorSectionVo> getDoorSectionPage(Page page, DoorSectionEntity entity);
}

