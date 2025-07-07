package com.hscloud.hs.cost.account.service.impl.second;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.constant.enums.second.InputType;
import com.hscloud.hs.cost.account.constant.enums.second.ModeType;
import com.hscloud.hs.cost.account.model.dto.second.ItemValueDTO;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.service.impl.second.async.SecRedisService;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.second.ProgDetailItemMapper;
import com.hscloud.hs.cost.account.service.second.IProgDetailItemService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
* 科室二次分配明细大项 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgDetailItemService extends ServiceImpl<ProgDetailItemMapper, ProgDetailItem> implements IProgDetailItemService {

    private final SecRedisService secRedisService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createByDetail(ProgProjectDetail progProjectDetail, ProgProjectDetail comProjectDetail) {
        Long comDetailId = comProjectDetail.getId();
        Long progDetailId = progProjectDetail.getId();


        //方案指标明细大项
        List<ProgDetailItem> comItemList = this.list(Wrappers.<ProgDetailItem>lambdaQuery().eq(ProgDetailItem::getProgProjectDetailId,comDetailId));

        this.createByProgItemList(progProjectDetail,comItemList);


    }

    private void createByProgItemList(ProgProjectDetail progProjectDetail, List<ProgDetailItem> comItemList) {
        Long progDetailId = progProjectDetail.getId();

        List<ProgDetailItem> addList = new ArrayList<>();
        for (ProgDetailItem comItem : comItemList) {
            ProgDetailItem progDetailItem = new ProgDetailItem();
            BeanUtils.copyProperties(comItem, progDetailItem);

            progDetailItem.setId(null);
            progDetailItem.setProgProjectDetailId(progDetailId);
            progDetailItem.setCommonId(comItem.getId());

            addList.add(progDetailItem);
        }
        if(!addList.isEmpty()){
            this.saveBatch(addList);
        }
        //替换parentId ，之前为 方案的parentId，要换成 任务的parentId
        if(!addList.isEmpty()){
            this.changeParentId(addList);
            this.updateBatchById(addList);
        }
    }

    @Override
    public void delByItemList(List<ProgDetailItem> delList) {
        if(delList == null || delList.isEmpty()){
            return;
        }
        List<Long> delIds = delList.stream().map(ProgDetailItem::getId).collect(Collectors.toList());
        this.remove(Wrappers.<ProgDetailItem>lambdaQuery().in(ProgDetailItem::getId,delIds));

    }

    @Override
    public void syncByDetail(ProgProjectDetail progProjectDetail) {
        //comItem 和 progItem 对比，根据增删改分类
        Long comDetailId = progProjectDetail.getCommonId();
        Long progDetailId = progProjectDetail.getId();
        List<ProgDetailItem> comItemList = this.listByDetailId(comDetailId);
        List<ProgDetailItem> progItemList = this.listByDetailId(progDetailId);

        //新增的集合
        List<ProgDetailItem> addList = comItemList.stream()
                .filter(comItem -> progItemList.stream().map(ProgDetailItem::getCommonId).noneMatch(commonId -> Objects.equals(commonId,comItem.getId())))
                .collect(Collectors.toList());
        this.createByProgItemList(progProjectDetail,addList);

        //删除的集合
        List<ProgDetailItem> delList = progItemList.stream()
                .filter(progItem -> comItemList.stream().map(ProgDetailItem::getId).noneMatch(id ->  Objects.equals(id,progItem.getCommonId())))
                .collect(Collectors.toList());
        this.delByItemList(delList);

        //修改的集合 往下处理 detail
        List<ProgDetailItem> updateList = progItemList.stream()
                .filter(progItem -> delList.stream().noneMatch(delItem -> Objects.equals(delItem.getId(), progItem.getId())))
                .collect(Collectors.toList());
        this.updateByComItemList(comItemList,updateList);
    }

    @Override
    public List<ProgDetailItem> listByPidCache(String cycle, Long progDetailId) {
        List<ProgDetailItem> progDetailItemAll = secRedisService.itemList(cycle);
        return progDetailItemAll.stream().filter(progDetailItem -> progDetailItem.getProgProjectDetailId().equals(progDetailId)).collect(Collectors.toList());
    }

    private void updateByComItemList(List<ProgDetailItem> comList, List<ProgDetailItem> updateList) {
        Map<Long,ProgDetailItem> comMap = comList.stream().collect(Collectors.toMap(ProgDetailItem::getId, item->item, (v1, v2) -> v2));
        for (ProgDetailItem item : updateList){
            Long commonId = item.getCommonId();
            ProgDetailItem comItem = comMap.get(commonId);
            item.setName(comItem.getName());
            item.setModeType(comItem.getModeType());
            item.setPriceValue(comItem.getPriceValue());
            item.setInputType(comItem.getInputType());
            item.setAccountItemType(comItem.getAccountItemType());
            item.setAccountItemId(comItem.getAccountItemId());
            item.setAccountItemCode(comItem.getAccountItemCode());
            item.setAccountItemName(comItem.getAccountItemName());
            item.setIfExtendLast(comItem.getIfExtendLast());
            item.setSortNum(comItem.getSortNum());
        }
        if(!updateList.isEmpty()){
            this.updateBatchById(updateList);
        }
    }

    private List<ProgDetailItem> listByDetailId(Long detailId) {
        return this.list(Wrappers.<ProgDetailItem>lambdaQuery().eq(ProgDetailItem::getProgProjectDetailId,detailId));
    }

    private void changeParentId(List<ProgDetailItem> addList) {
        //key ：item对应的公共itemid ，value: item
        Map<Long,ProgDetailItem> map = new HashMap<>();
        for (ProgDetailItem item : addList){
            map.put(item.getCommonId(),item);
        }
        for (ProgDetailItem item : addList){
            Long parentId = item.getParentId();
            ProgDetailItem parentItem = map.get(parentId);
            if(parentItem != null){
                item.setParentId(parentItem.getId());
            }
        }
    }
}
