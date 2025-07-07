package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.DoorSectionEntity;
import com.hscloud.hs.cost.account.model.vo.DoorSectionVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 门诊收入
 * 
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-07 15:01:08
 */
@Mapper
public interface DoorSectionMapper extends BaseMapper<DoorSectionEntity> {
    //返回页记录
    public List<DoorSectionVo> getPage(DoorSectionEntity entity);

    //返回满足条件的总行数
    public Long getTotal(DoorSectionEntity entity);
}
