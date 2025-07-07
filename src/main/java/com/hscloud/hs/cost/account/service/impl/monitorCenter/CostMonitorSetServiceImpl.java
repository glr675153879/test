package com.hscloud.hs.cost.account.service.impl.monitorCenter;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.hscloud.hs.cost.account.constant.enums.CostMonitorSetStatusEnum;
import com.hscloud.hs.cost.account.mapper.CostMonitorSetMapper;
import com.hscloud.hs.cost.account.model.dto.monitorCenter.CostMonitorSetDto;
import com.hscloud.hs.cost.account.model.dto.monitorCenter.CostMonitorSetQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostMonitorSet;
import com.hscloud.hs.cost.account.model.vo.monitorCenter.CostMonitorSetVo;
import com.hscloud.hs.cost.account.service.monitorCenter.CostMonitorSetService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.hscloud.hs.cost.account.constant.Constant.NULL_STR;
import static com.hscloud.hs.cost.account.constant.Constant.UNDEFINED_STR;
import static com.hscloud.hs.cost.account.constant.enums.CostMonitorSetStatusEnum.NOT_SETTLE;
/**
 * 监测值设置
 * @author  lian
 * @date  2023-09-22 9:56
 *
 */

@Service
public class CostMonitorSetServiceImpl extends ServiceImpl<CostMonitorSetMapper, CostMonitorSet> implements CostMonitorSetService {

    @Autowired
    private CostMonitorSetMapper costMonitorSetMapper;

    @Override
    public Object queryCount(CostMonitorSetQueryDto dto) {
        //目前是查询未设置的数量
        dto.setStatus(NOT_SETTLE.getCode());
        return costMonitorSetMapper.queryCount(dto);
    }

    @Override
    public IPage queryListAll(Page page,CostMonitorSetQueryDto dto) {
        //查询科室单元信息,查询核算项信息 cross join
        IPage<CostMonitorSetVo> costMonitorSetVoPage = costMonitorSetMapper.queryListAll(page, dto);
        //返回总数
        commonSearchVo(costMonitorSetVoPage.getRecords());
        return costMonitorSetVoPage;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object saveOrUpdateCostMonitorSet(CostMonitorSetDto dto) {
        if(null!=(dto.getId())){
            //编辑
        }else{
            //新增
            LambdaQueryWrapper<CostMonitorSet> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.eq(CostMonitorSet::getUnitId,dto.getUnitId())
                    .eq(CostMonitorSet::getItemId,dto.getItemId());
            CostMonitorSet one = this.getOne(queryWrapper);
            if(null!=one){
                throw new BizException("该科室,当前核算项已存在");
            }
        }
        CostMonitorSet costMonitorSet = BeanUtil.copyProperties(dto, CostMonitorSet.class);
        if(costMonitorSet.getTargetValue().contains(NULL_STR)||costMonitorSet.getTargetValue().contains(UNDEFINED_STR)){
            throw new BizException("目标值请不要包含null或者undefined");
        }
        saveOrUpdate(costMonitorSet);
        return costMonitorSet.getId();
    }

    /**
     * 公共方法查询list
     *@param  costMonitorSetVos list
     */
    void commonSearchVo(List<CostMonitorSetVo> costMonitorSetVos ){
        costMonitorSetVos.forEach(vo -> {
            if (StringUtils.isNotBlank(vo.getTargetValue())) {
                vo.setStatus(CostMonitorSetStatusEnum.SETTLED.getCode());
            }else{
                vo.setStatus(NOT_SETTLE.getCode());
                vo.setTargetValue(NOT_SETTLE.getDesc());
            }
        });
    }
}