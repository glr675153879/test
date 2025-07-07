package com.hscloud.hs.cost.account.service.impl.second;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.comparator.CompareUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.constant.enums.second.ActionType;
import com.hscloud.hs.cost.account.constant.enums.second.InputType;
import com.hscloud.hs.cost.account.constant.enums.second.ModeType;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskDetailItemMapper;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskMapper;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskProjectDetailMapper;
import com.hscloud.hs.cost.account.model.dto.second.*;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskDetailItemVo;
import com.hscloud.hs.cost.account.model.vo.second.importXls.ImportResultVo;
import com.hscloud.hs.cost.account.service.impl.second.kpi.SecondKpiService;
import com.hscloud.hs.cost.account.service.second.*;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.hscloud.hs.cost.account.utils.DmoUtil;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.commons.codec.CharEncoding.UTF_8;

/**
* 科室二次分配明细大项值 服务实现类
*
*/
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnitTaskDetailItemService extends ServiceImpl<UnitTaskDetailItemMapper, UnitTaskDetailItem> implements IUnitTaskDetailItemService {

    private final IUnitTaskUserService unitTaskUserService;
    private final UnitTaskMapper unitTaskMapper;
    private final IGrantUnitService grantUnitService;
    // private final UnitTaskProjectMapper unitTaskProjectMapper;
    private final UnitTaskProjectDetailMapper unitTaskProjectDetailMapper;
    private final IProgDetailItemService progDetailItemService;
    private final IProgProjectDetailService progProjectDetailService;
    private final IUnitTaskProjectCountService unitTaskProjectCountService;
    private final DmoUtil dmoUtil;
    private final SecondKpiService secondKpiService;
    private final RedisUtil redisUtil;
    private final IUnitTaskDetailItemWorkService unitTaskDetailItemWorkService;
    @Lazy
    @Autowired
    private UnitTaskProjectService unitTaskProjectService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initUserData(Long unitTaskId, UnitTaskProjectDetail unitTaskProjectDetail, List<UnitTaskUser> userList) {
        //人员list
        if(userList == null){
            userList = unitTaskUserService.listByTaskId(unitTaskId);
        }
        Long progProjectDetailId = unitTaskProjectDetail.getProgProjectDetailId();
        Long projectId = unitTaskProjectDetail.getUnitTaskProjectId();
        UnitTaskProject unitTaskProject = unitTaskProjectService.getById(projectId);

        //方案指标明细大项
        List<ProgDetailItem> progDetailItemList = progDetailItemService.list(Wrappers.<ProgDetailItem>lambdaQuery().eq(ProgDetailItem::getProgProjectDetailId,progProjectDetailId).orderByAsc(ProgDetailItem::getSortNum));

        this.addByProgDetailItemList(unitTaskProject,unitTaskProjectDetail,userList,progDetailItemList);
    }

    @Override
    public List<UnitTaskDetailItemVo> userList(Long detailId) {
        return this.userList(detailId,null);
    }
    @Override
    public List<UnitTaskDetailItemVo> userList(Long detailId,String detailEmpCode) {
        List<UnitTaskDetailItemVo> rtnList = new ArrayList<>();
        UnitTaskProjectDetail detail = unitTaskProjectDetailMapper.selectById(detailId);
        Long projectId = detail.getUnitTaskProjectId();
        UnitTaskProject project = unitTaskProjectService.getById(projectId);
        Long unitTaskId = project.getUnitTaskId();
        List<UnitTaskUser> userList = unitTaskUserService.listByTaskId(unitTaskId,detailEmpCode);

        //系数分配
        if(detail.getModeType() == null)
            return new ArrayList<>();
        String modeTpe = CommonUtils.getDicVal(detail.getModeType());
        if(ModeType.ratio.toString().equals(modeTpe)){
            //大项list
            List<UnitTaskDetailItem> bigItemList = this.list(Wrappers.<UnitTaskDetailItem>lambdaQuery()
                    .eq(UnitTaskDetailItem::getUnitTaskProjectDetailId,detailId).isNull(UnitTaskDetailItem::getParentId)
                    .eq(detailEmpCode!=null,UnitTaskDetailItem::getEmpCode,detailEmpCode)
                    .orderByAsc(UnitTaskDetailItem::getProgDetailItemId));

            //子项list
            List<UnitTaskDetailItem> itemList = this.list(Wrappers.<UnitTaskDetailItem>lambdaQuery().eq(UnitTaskDetailItem::getUnitTaskProjectDetailId,detailId).isNotNull(UnitTaskDetailItem::getParentId)
                    .eq(detailEmpCode!=null,UnitTaskDetailItem::getEmpCode,detailEmpCode)
                    .orderByAsc(UnitTaskDetailItem::getProgDetailItemId));
            //根据大项分组 itemMap ，key：bigItemId
            Map<Long,List<UnitTaskDetailItem>> itemMap = new HashMap<>();
            for (UnitTaskDetailItem item : itemList){
                Long key = item.getParentId();
                List<UnitTaskDetailItem> list = itemMap.computeIfAbsent(key, k -> new ArrayList<>());
                list.add(item);
            }

            //根据人员分组 itemMap ，key：empcode
            Map<String,List<UnitTaskDetailItem>> bigItemMap = new HashMap<>();
            for (UnitTaskDetailItem bigItem : bigItemList){
                String key = bigItem.getEmpCode();
                List<UnitTaskDetailItem> list = bigItemMap.computeIfAbsent(key, k -> new ArrayList<>());
                bigItem.setUnitTaskDetailItemList(itemMap.get(bigItem.getId()));
                list.add(bigItem);
            }
            sortItem(detail.getProgProjectDetailId(), bigItemMap);

            for (UnitTaskUser unitTaskUser : userList){
                String empCode = unitTaskUser.getEmpCode();
                UnitTaskDetailItemVo vo = new UnitTaskDetailItemVo();
                BeanUtils.copyProperties(unitTaskUser, vo);
                vo.setItemList(bigItemMap.get(empCode));
                rtnList.add(vo);
            }

        }else{//工作量分配
            List<UnitTaskDetailItem> itemList = this.list(Wrappers.<UnitTaskDetailItem>lambdaQuery().eq(UnitTaskDetailItem::getUnitTaskProjectDetailId,detailId)
                    .eq(detailEmpCode!=null,UnitTaskDetailItem::getEmpCode,detailEmpCode)
                    .orderByAsc(UnitTaskDetailItem::getProgDetailItemId));
            //根据人员分组 itemMap ，key：empcode
            Map<String,List<UnitTaskDetailItem>> itemMap = new HashMap<>();
            for (UnitTaskDetailItem item : itemList){
                String key = item.getEmpCode();
                List<UnitTaskDetailItem> list = itemMap.computeIfAbsent(key, k -> new ArrayList<>());
                list.add(item);
            }
            sortItem(detail.getProgProjectDetailId(), itemMap);
            for (UnitTaskUser unitTaskUser : userList){
                String empCode = unitTaskUser.getEmpCode();
                UnitTaskDetailItemVo vo = new UnitTaskDetailItemVo();
                BeanUtils.copyProperties(unitTaskUser, vo);
                vo.setItemList(itemMap.get(empCode));
                rtnList.add(vo);
            }

            // 工作量系数写入
            List<UnitTaskDetailItemWork> unitTaskDetailItemWorks = unitTaskDetailItemWorkService.listByDetailId(detailId);
            Map<String, UnitTaskDetailItemWork> collect = unitTaskDetailItemWorks.stream().collect(Collectors.toMap(UnitTaskDetailItemWork::getEmpCode, e -> e, (v1, v2) -> v2));
            for (UnitTaskDetailItemVo unitTaskDetailItemVo : rtnList) {
                if (collect.containsKey(unitTaskDetailItemVo.getEmpCode())) {
                    unitTaskDetailItemVo.setWorkRate(collect.get(unitTaskDetailItemVo.getEmpCode()).getWorkRate());
                    unitTaskDetailItemVo.setExamPoint(collect.get(unitTaskDetailItemVo.getEmpCode()).getExamPoint());
                    unitTaskDetailItemVo.setItemWork(collect.get(unitTaskDetailItemVo.getEmpCode()));
                }
            }
        }

        return rtnList;
    }

    private void sortItem(Long progProjectDetailId, Map<String, List<UnitTaskDetailItem>> bigItemMap) {
        if(CollUtil.isEmpty(bigItemMap)){
            return;
        }
        //方案指标明细大项
        List<ProgDetailItem> progDetailItemList = progDetailItemService.list(Wrappers.<ProgDetailItem>lambdaQuery().eq(ProgDetailItem::getProgProjectDetailId,progProjectDetailId).orderByAsc(ProgDetailItem::getSortNum));
        Map<Long, Long> collect = progDetailItemList.stream().collect(Collectors.toMap(e -> e.getId(), e -> e.getSortNum(), (v1, v2) -> v1));
        Map<Long, Long> collect2 = progDetailItemList.stream().collect(Collectors.toMap(e -> e.getId(), e -> e.getId(), (v1, v2) -> v1));
        for (Map.Entry<String, List<UnitTaskDetailItem>> stringListEntry : bigItemMap.entrySet()) {
            List<UnitTaskDetailItem> value = stringListEntry.getValue();
            sortItem(collect, value);
            for (UnitTaskDetailItem unitTaskDetailItem : value) {
                List<UnitTaskDetailItem> unitTaskDetailItemList = unitTaskDetailItem.getUnitTaskDetailItemList();
                sortItem(collect, unitTaskDetailItemList);
            }
        }
    }

    private void sortItem(Map<Long, Long> collect, List<UnitTaskDetailItem> unitTaskDetailItemList) {
        if(CollUtil.isEmpty(unitTaskDetailItemList)){
            return;
        }
        unitTaskDetailItemList.sort((o1,o2)->{
            Long sort1 = collect.get(o1.getProgDetailItemId());
            Long sort2 = collect.get(o2.getProgDetailItemId());
            int compare = CompareUtil.compare(sort1, sort2);
            if (compare != 0) {
                return compare;
            }
            return CompareUtil.compare(o1.getProgDetailItemId(), o1.getProgDetailItemId());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveItems(UnitTaskDetailItemSaveDTO unitTaskDetailItemSaveDTO) {
        Long detailId = unitTaskDetailItemSaveDTO.getProjectDetailId();
        UnitTaskProjectDetail detail = unitTaskProjectDetailMapper.selectById(detailId);
        UnitTaskProject project = unitTaskProjectService.getById(detail.getUnitTaskProjectId());
        List<UnitTaskDetailItemVo> userList = unitTaskDetailItemSaveDTO.getUserList();

        int scale = 6;
        RoundingMode roundingMode = CommonUtils.getCarryRule(project.getCarryRule());

        //修改ifEdit
        detail.setIfEdited("1");
        unitTaskProjectDetailMapper.updateById(detail);
        //系数分配
        String modeType = CommonUtils.getDicVal(detail.getModeType());
        if(ModeType.ratio.toString().equals(modeType)){
            //把userList中的 itemList 及 itemList的getUnitTaskDetailItemList  合并到一起
            List<UnitTaskDetailItem> itemList = new ArrayList<>();
            for (UnitTaskDetailItemVo user : userList){
                itemList.addAll(user.getItemList());
                for (UnitTaskDetailItem bigItem : user.getItemList()){
                    if(bigItem.getUnitTaskDetailItemList() != null){
                        itemList.addAll(bigItem.getUnitTaskDetailItemList());
                    }
                }
            }

            for (UnitTaskDetailItem item : itemList){
                BigDecimal amt = item.getPoint().setScale(scale,roundingMode);
                item.setAmt(amt);
            }
            this.updateBatchById(itemList);
        }else{//工作量分配
            //修改工作量系数 和 考核得分
            List<UnitTaskUser> useUpdateList = new ArrayList<>(userList);
            unitTaskUserService.updateBatchById(useUpdateList);

            //修改item
            List<UnitTaskDetailItem> itemList = userList.stream()
                    .flatMap(unitTaskDetailItemVo -> unitTaskDetailItemVo.getItemList().stream())
                    .collect(Collectors.toList());
            for (UnitTaskDetailItem item : itemList){
                String itemModeType = CommonUtils.getDicVal(item.getModeType());
                if(ModeType.qtyxprice.toString().equals(itemModeType)){
                    BigDecimal amt = item.getPoint().multiply(item.getPriceValue()).setScale(scale,roundingMode);
                    item.setAmt(amt);
                }else if(ModeType.input.toString().equals(itemModeType)){
                    BigDecimal amt = item.getPoint().setScale(scale,roundingMode);
                    item.setAmt(amt);
                }
            }
            this.updateBatchById(itemList);

            List<UnitTaskDetailItemWork> collect1 = userList.stream().map(e -> {
                UnitTaskDetailItemWork unitTaskDetailItemWork = new UnitTaskDetailItemWork();
                unitTaskDetailItemWork.setId(e.getItemWork().getId());
                unitTaskDetailItemWork.setWorkRate(e.getWorkRate());
                unitTaskDetailItemWork.setExamPoint(e.getExamPoint());
                return unitTaskDetailItemWork;
            }).collect(Collectors.toList());
            unitTaskDetailItemWorkService.updateBatchById(collect1);
        }

        //计算projectCount
        //UnitTaskProject project = unitTaskProjectMapper.selectById(detail.getUnitTaskProjectId());
        unitTaskProjectCountService.doCount(project.getUnitTaskId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProgDetailItem2(ProgDetailItemSave2DTO progDetailItemSaveDTO) {
        Long unitTaskProjectDetailId = progDetailItemSaveDTO.getUnitTaskProjectDetailId();
        UnitTaskProjectDetail unitTaskProjectDetail = unitTaskProjectDetailMapper.selectById(unitTaskProjectDetailId);
        UnitTaskProject unitTaskProject = unitTaskProjectService.getById(unitTaskProjectDetail.getUnitTaskProjectId());
        Long unitTaskId = unitTaskProject.getUnitTaskId();
        UnitTask unitTask = unitTaskMapper.selectById(unitTaskId);

        List<ProgDetailItem> delList = new ArrayList<>();
        List<ProgDetailItem> addList = new ArrayList<>();
        List<ProgDetailItem> updateList = new ArrayList<>();
        for (ProgDetailItem progDetailItem : progDetailItemSaveDTO.getItemList()) {
            String actionType = progDetailItem.getActionType();
            //新增的项目
            if(ActionType.add.toString().equals(actionType)){
                addList.add(progDetailItem);
            }else if(ActionType.edit.toString().equals(actionType)){//修改的项目
                // 如果修改了数据来源、核算项、是否继承上月，则删除再添加
                ProgDetailItem oldItem = progDetailItemService.getById(progDetailItem.getId());
                if (changeNeedInit(oldItem, progDetailItem)) {
                    addList.add(progDetailItem);
                    delList.add(progDetailItem);
                } else {
                    updateList.add(progDetailItem);
                }
            }else  if(ActionType.del.toString().equals(actionType)){//删除的项目
                delList.add(progDetailItem);
            }
        }

        //先处理删除
        if(!delList.isEmpty()) {
            this.delByUnitTaskProject(unitTask, unitTaskProject, unitTaskProjectDetail, delList);
        }
        //处理新增
        if(!addList.isEmpty()) {
            this.addByUnitTaskProject(unitTask, unitTaskProject, unitTaskProjectDetail, addList);
        }
        // 处理更新
        if (!updateList.isEmpty()) {
            this.updateByUnitTaskProject(unitTask, unitTaskProject, unitTaskProjectDetail, updateList);
        }
    }

    /**
     * 修改 数据来源、核算项id 核算项code 则需要删除再新增
     *
     * @param oldItem
     * @param newItem
     * @return boolean
     */
    private boolean changeNeedInit(ProgDetailItem oldItem, ProgDetailItem newItem) {
        log.info("changeNeedInit oldDetail:{}", JSON.toJSONString(oldItem));
        log.info("changeNeedInit newDetail:{}", JSON.toJSONString(newItem));
        boolean equals1 = Objects.equals(CommonUtils.getDicVal(oldItem.getInputType()), CommonUtils.getDicVal(newItem.getInputType()));
        boolean equals2 = Objects.equals(oldItem.getAccountItemId(), newItem.getAccountItemId());
        boolean equals3 = Objects.equals(oldItem.getAccountItemCode(), newItem.getAccountItemCode());
        // boolean equals3 = StrUtil.equals(oldItem.getIfExtendLast(), newItem.getIfExtendLast());
        log.info("changeNeedInit changeNeedInit:{},{}", equals1, equals2);
        // 有一个不同则删除重新创建
        return !equals1 || !equals2 || !equals3;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgDetailItem2Index(ProgDetailItemSave2DTO progDetailItemSaveDTO) {
        for (int i = 0; i < progDetailItemSaveDTO.getItemList().size(); i++) {
            progDetailItemSaveDTO.getItemList().get(i).setSortNum((long) (i + 1));
        }
        progDetailItemService.updateBatchById(progDetailItemSaveDTO.getItemList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProgDetailItem1(ProgDetailItemSave1DTO progDetailItemSave1DTO) {
        Long unitTaskProjectDetailId = progDetailItemSave1DTO.getUnitTaskProjectDetailId();
        UnitTaskProjectDetail unitTaskProjectDetail = unitTaskProjectDetailMapper.selectById(unitTaskProjectDetailId);
        UnitTaskProject unitTaskProject = unitTaskProjectService.getById(unitTaskProjectDetail.getUnitTaskProjectId());
        Long unitTaskId = unitTaskProject.getUnitTaskId();
        //UnitTask unitTask = unitTaskMapper.selectById(unitTaskId);
        Long progDetailId = unitTaskProjectDetail.getProgProjectDetailId();
        ProgProjectDetail progDetail = progProjectDetailService.getById(progDetailId);

        List<ProgDetailItem> childAddList = new ArrayList<>();
        //新增方案大项
        //新增大项
        Long itemBigId = progDetailItemSave1DTO.getId();
        ProgDetailItem progDetailItem = progDetailItemService.getById(itemBigId);
        if(progDetailItem == null){
            progDetailItem = new ProgDetailItem();
            // 找到最大的sortnum+1
            List<ProgDetailItem> list = progDetailItemService.list(Wrappers.<ProgDetailItem>lambdaQuery()
                    .eq(ProgDetailItem::getProgProjectDetailId, progDetailId)
                    .isNull(ProgDetailItem::getParentId));
            Optional<Long> max = list.stream().map(ProgDetailItem::getSortNum).filter(Objects::nonNull).max(Long::compareTo);
            if (max.isPresent()) {
                progDetailItem.setSortNum(max.get() + 1);
            } else {
                progDetailItem.setSortNum(1L);
            }
            // 找到最大的sortnum+1 end
            progDetailItem.setProgProjectDetailId(progDetailId);
            progDetailItem.setName(progDetailItemSave1DTO.getItemName());
            progDetailItem.setProgCommonId(progDetail.getProgCommonId());
            progDetailItemService.save(progDetailItem);
            //新增任务大项
            childAddList.add(progDetailItem);
        }

        //新增子项 list
        for(ProgDetailItem progItem : progDetailItemSave1DTO.getChildItemList()){
            String childName = progItem.getName();
            ProgDetailItem childItem = new ProgDetailItem();
            // 找到最大的sortnum+1
            List<ProgDetailItem> list = progDetailItemService.list(Wrappers.<ProgDetailItem>lambdaQuery()
                    .eq(ProgDetailItem::getProgProjectDetailId, progDetailId)
                    .eq(ProgDetailItem::getParentId, progDetailItem.getId()));
            Optional<Long> max = list.stream().map(ProgDetailItem::getSortNum).filter(Objects::nonNull).max(Long::compareTo);
            if (max.isPresent()) {
                childItem.setSortNum(max.get() + 1);
            } else {
                childItem.setSortNum(1L);
            }
            // 找到最大的sortnum+1 end
            childItem.setProgProjectDetailId(progDetailId);
            childItem.setName(childName);
            childItem.setParentId(progDetailItem.getId());
            childItem.setProgCommonId(progDetail.getProgCommonId());
            progDetailItemService.save(childItem);
            childAddList.add(childItem);
        }
//        if(!childAddList.isEmpty()){
//            progDetailItemService.saveBatch(childAddList);
//        }


        List<UnitTaskUser> userList = unitTaskUserService.list(Wrappers.<UnitTaskUser>lambdaQuery().eq(UnitTaskUser::getUnitTaskId,unitTaskId));
        this.addByProgDetailItemList(unitTaskProject,unitTaskProjectDetail,userList,childAddList);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ProgItemDelDTO progItemDelDTO) {
        Long taskDetailId = progItemDelDTO.getTaskDetailId();
        Long progItemId = progItemDelDTO.getProgItemId();
        ProgDetailItem progItem = progDetailItemService.getById(progItemId);

        Long parentId = progItem.getParentId();
        if(parentId != null){//子项
            progDetailItemService.removeById(progItemId);
            this.remove(Wrappers.<UnitTaskDetailItem>lambdaQuery().eq(UnitTaskDetailItem::getProgDetailItemId,progItemId).eq(UnitTaskDetailItem::getUnitTaskProjectDetailId,taskDetailId));
        }else{//大项
            //删方案
            progDetailItemService.remove(Wrappers.<ProgDetailItem>lambdaQuery().and(item->item.eq(ProgDetailItem::getId,progItemId).or().eq(ProgDetailItem::getParentId,progItemId)));

            List<UnitTaskDetailItem> taskItemList = this.list(Wrappers.<UnitTaskDetailItem>lambdaQuery().eq(UnitTaskDetailItem::getUnitTaskProjectDetailId,taskDetailId).eq(UnitTaskDetailItem::getProgDetailItemId,progItemId));
            for (UnitTaskDetailItem taskItem : taskItemList){
                Long taskItemId = taskItem.getId();
                //删任务
                this.remove(Wrappers.<UnitTaskDetailItem>lambdaQuery().eq(UnitTaskDetailItem::getUnitTaskProjectDetailId,taskDetailId).and(item ->item.eq(UnitTaskDetailItem::getId,taskItemId).or().eq(UnitTaskDetailItem::getParentId,taskItemId)));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delByProjectId(List<Long> projectIds) {
        if(!projectIds.isEmpty()) {
            this.remove(Wrappers.<UnitTaskDetailItem>lambdaQuery().in(UnitTaskDetailItem::getUnitTaskProjectId, projectIds));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delByDetailId(List<Long> detailIds) {
        if(!detailIds.isEmpty()) {
            this.remove(Wrappers.<UnitTaskDetailItem>lambdaQuery().in(UnitTaskDetailItem::getUnitTaskProjectDetailId, detailIds));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncByDetail(String cycle,UnitTaskProjectDetail unitTaskProjectDetail) {
        //科室二次分配使用，每个人每个item 都有一笔数据
        //progItem 和 taskItem（每个人都有一笔） 对比，根据增删改分类
        Long progDetailId = unitTaskProjectDetail.getProgProjectDetailId();
        Long taskDetailId = unitTaskProjectDetail.getId();
        Long taskProjectId = unitTaskProjectDetail.getUnitTaskProjectId();
        UnitTaskProject taskProject = unitTaskProjectService.getById(taskProjectId);
        Long unitTaskId = taskProject.getUnitTaskId();

        List<ProgDetailItem> progItemList = progDetailItemService.listByPidCache(cycle,progDetailId);
        List<UnitTaskDetailItem> taskItemList = this.list(Wrappers.<UnitTaskDetailItem>lambdaQuery().eq(UnitTaskDetailItem::getUnitTaskProjectDetailId,taskDetailId));
        //taskItemList  找出 现在在用的 item，将其中方案item的冗余字段组成一个ProgDetailItem，再和progDetailList进行比较，找出增删改集合
        List<ProgDetailItem> currentProgItemList = this.task2progItem(taskItemList);

        //新增的集合，为每个人增出item
        List<UnitTaskUser> userList = unitTaskUserService.list(Wrappers.<UnitTaskUser>lambdaQuery().eq(UnitTaskUser::getUnitTaskId,unitTaskId));
        List<ProgDetailItem> addList = progItemList.stream()
                .filter(progItem -> currentProgItemList.stream().map(ProgDetailItem::getId).noneMatch(progItemId -> Objects.equals(progItemId, progItem.getId())))
                .collect(Collectors.toList());
        //this.addByProgDetailList(unitTaskProject,userList,addList); 和 修改集合合并后执行

        //删除的集合
        List<ProgDetailItem> delList = currentProgItemList.stream()
                .filter(progItem -> progItemList.stream().map(ProgDetailItem::getId).noneMatch(id -> Objects.equals(id, progItem.getId())))
                .collect(Collectors.toList());
        List<Long> delProgItemIds = delList.stream().map(ProgDetailItem::getId).collect(Collectors.toList());
        //this.remove(Wrappers.<UnitTaskDetailItem>lambdaQuery().eq(UnitTaskDetailItem::getUnitTaskProjectDetailId,taskDetailId).in(UnitTaskDetailItem::getProgDetailItemId,delProgItemIds));和 修改集合合并后执行

        //修改的集合 ,比较任务数据中冗余的方案字段  和 方案字段是否相同。只要有一个不同，则重新获取数据(删除再创建)；都相同 则跳过
        List<ProgDetailItem> editList = this.getSyncByDetailEditList(progItemList,currentProgItemList);
        List<Long> editProgItemIds = editList.stream().map(ProgDetailItem::getId).collect(Collectors.toList());
        List<Long> delMergedList = new ArrayList<>(delProgItemIds);
        delMergedList.addAll(editProgItemIds);
        if(!delMergedList.isEmpty()) {
            this.remove(Wrappers.<UnitTaskDetailItem>lambdaQuery().eq(UnitTaskDetailItem::getUnitTaskProjectDetailId, taskDetailId).in(UnitTaskDetailItem::getProgDetailItemId, delMergedList));
        }
        List<ProgDetailItem> addMergedList = new ArrayList<>(addList);
        addMergedList.addAll(editList);
        if(!addMergedList.isEmpty()){
            this.addByProgDetailItemList(taskProject,unitTaskProjectDetail,userList,addMergedList);
        }

    }

    @Override
    public void exportErci(Long unitTaskProjectDetailId, HttpServletResponse response) {
        try(OutputStream out = response.getOutputStream();){
            UnitTaskProjectDetail detail = unitTaskProjectDetailMapper.selectById(unitTaskProjectDetailId);
            UnitTaskProject project = unitTaskProjectService.getById(detail.getUnitTaskProjectId());
            Long progDetailId = detail.getProgProjectDetailId();
            List<ProgDetailItem> progItemList = progDetailItemService.list(Wrappers.<ProgDetailItem>lambdaQuery().eq(ProgDetailItem::getProgProjectDetailId,progDetailId));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(UTF_8);
            ExcelWriter writer = EasyExcelFactory.write(out)//.automaticMergeHead(false)
                    .build();
            // 动态添加表头，适用一些表头动态变化的场景
            WriteSheet sheet1 = new WriteSheet();
            sheet1.setSheetName(detail.getName());
            sheet1.setSheetNo(0);
            // 创建一个表格，用于 Sheet 中使用
            WriteTable table = new WriteTable();
            table.setTableNo(1);
            //查全部
            table.setHead(this.getHead(progItemList,detail.getModeType()));
            // 写数据
            List<UnitTaskUser> userList = unitTaskUserService.list(Wrappers.<UnitTaskUser>lambdaQuery().eq(UnitTaskUser::getUnitTaskId,project.getUnitTaskId()));
            writer.write(this.getContent(userList), sheet1, table);
            writer.finish();
        }catch (Exception e){
            //e.printStackTrace();
        }
    }

    private List<List<String>> getHead(List<ProgDetailItem> progItemList, String modeType) {
        modeType = CommonUtils.getDicVal(modeType);
        if(ModeType.ratio.toString().equals(modeType)){
            return this.getHeadXishu(progItemList);
        }else{
            return this.getHeadWork(progItemList);
        }
    }

    private List<List<Object>> getContent(List<UnitTaskUser> userList){
        List<List<Object>> totalContent = new ArrayList<>();
        for (UnitTaskUser unitTaskUser : userList) {
            List<Object> list = new ArrayList<>();
            list.add(unitTaskUser.getEmpName());
            list.add(unitTaskUser.getEmpCode());
            totalContent.add(list);
        }
        return totalContent;
    }

    private List<List<String>> getHeadWork(List<ProgDetailItem> progItemList) {

        List<ProgDetailItem> itemList = progItemList.stream().filter(item->item.getParentId()==null).collect(Collectors.toList());

        List<List<String>> total = new ArrayList<>();
        //人员信息
        List<String> name = new ArrayList<>();
        name.add("姓名");
        List<String> empCode = new ArrayList<>();
        empCode.add("工号");
        total.add(name);
        total.add(empCode);

        //指标
        int i = 0;
        for (ProgDetailItem item : itemList) {
            //跳过采集型
            if(InputType.auto.toString().equals(CommonUtils.getDicVal(item.getInputType()))){
                continue;
            }

            i++;
            String modeType = CommonUtils.getDicVal(item.getModeType());

            List<String> detailHead = new ArrayList<>();
            detailHead.add(item.getName());
            if(ModeType.qtyxprice.toString().equals(modeType)){
                detailHead.add(i%2==0?"数量":"数 量");
            }else{
                detailHead.add(i%2==0?"合计（元）":"合计 （元）");
            }
            total.add(detailHead);
        }
        total.add(CollUtil.newArrayList("工作量系数"));
        total.add(CollUtil.newArrayList("考核得分"));
        return total;
    }
    private List<List<String>> getHeadXishu(List<ProgDetailItem> progItemList) {
        //大项
        List<ProgDetailItem> itemBigList = progItemList.stream().filter(item->item.getParentId()==null).collect(Collectors.toList());
        Map<Long,ProgDetailItem> itemBigMap = itemBigList.stream().collect(Collectors.toMap(ProgDetailItem::getId,item->item, (v1, v2) -> v2));
        //给大项挂载子项
        List<ProgDetailItem> itemChildList = progItemList.stream().filter(item->item.getParentId()!=null).collect(Collectors.toList());
        for (ProgDetailItem itemChild : itemChildList){
            Long parentId = itemChild.getParentId();
            ProgDetailItem parentItem = itemBigMap.get(parentId);
            parentItem.getChildItemList().add(itemChild);
        }

        List<List<String>> total = new ArrayList<>();
        //人员信息
        List<String> name = new ArrayList<>();
        name.add("姓名");
        List<String> empCode = new ArrayList<>();
        empCode.add("工号");
        total.add(name);
        total.add(empCode);

        //指标
        int i = 0;
        for (ProgDetailItem itemBig : itemBigList) {
            if(itemBig.getChildItemList().isEmpty()){
                List<String> detailHead = new ArrayList<>();
                detailHead.add(itemBig.getName());
                detailHead.add(itemBig.getName());
                total.add(detailHead);
            }else {
                for (ProgDetailItem itemChild : itemBig.getChildItemList()) {
                    List<String> detailHead = new ArrayList<>();
                    detailHead.add(itemBig.getName());
                    detailHead.add(itemChild.getName());
                    total.add(detailHead);
                }
            }
        }
        return total;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVo importErciXishu(Long unitTaskProjectDetailId, String[][] xlsDataArr) {
        UnitTaskProjectDetail detail = unitTaskProjectDetailMapper.selectById(unitTaskProjectDetailId);
        UnitTaskProject project = unitTaskProjectService.getById(detail.getUnitTaskProjectId());
        //人员
        List<UnitTaskUser> userList = unitTaskUserService.listByTaskId(project.getUnitTaskId());
        Map<String,UnitTaskUser> userMap = userList.stream().collect(Collectors.toMap(UnitTaskUser::getEmpCode,item->item, (v1, v2) -> v2));

        List<ProgDetailItem> progItemList = progDetailItemService.list(Wrappers.<ProgDetailItem>lambdaQuery().eq(ProgDetailItem::getProgProjectDetailId,detail.getProgProjectDetailId()));
        //大项
        List<ProgDetailItem> itemBigList = progItemList.stream().filter(item->item.getParentId()==null).collect(Collectors.toList());
        Map<Long,ProgDetailItem> itemBigMap = itemBigList.stream().collect(Collectors.toMap(ProgDetailItem::getId,item->item, (v1, v2) -> v2));
        //给大项挂载子项
        List<ProgDetailItem> itemChildList = progItemList.stream().filter(item->item.getParentId()!=null).collect(Collectors.toList());
        for (ProgDetailItem itemChild : itemChildList){
            Long parentId = itemChild.getParentId();
            ProgDetailItem parentItem = itemBigMap.get(parentId);
            parentItem.getChildItemList().add(itemChild);
        }

        //逐条检验，成功的落库 和 失败的入redis
        List<String> errorList = new ArrayList<>();
        //key  empCode-detailName  ,value = qty
        Map<String,String> dataMap = new HashMap<>();
        for (int row=0;row< xlsDataArr.length;row++){
            String[] data = xlsDataArr[row];
            //姓名
            //String name = data[0];
            //工号
            String empCode = data[1];
            //校验row人员是否存在
            this.importDanxiangUserValid(errorList,empCode,row,"工号",userMap);
            int colIndex = 2;
            for (ProgDetailItem itemBig : itemBigList){
                String itemBigName = itemBig.getName();
                if(itemBig.getChildItemList().isEmpty()){
                    String value = data[colIndex++];
                    //校验是否数字
                    if(this.importDanxiangValid(errorList,value,row, itemBigName)){
                        dataMap.put(empCode+"-"+ itemBigName,value);
                    }
                }else{
                    for (ProgDetailItem itemChild : itemBig.getChildItemList()){
                        String value = data[colIndex++];
                        String itemChildName = itemChild.getName();
                        String keyName = itemBigName+"-"+itemChildName;
                        //校验是否数字
                        if(this.importDanxiangValid(errorList,value,row,keyName)){
                            dataMap.put(empCode+"-"+keyName,value);
                        }
                    }
                }

            }
        }

        //导入数据，找不到就赋0
        List<UnitTaskDetailItem> itemList = this.listByPid(unitTaskProjectDetailId);
        //任务大项
        List<UnitTaskDetailItem> unitItemBigList = itemList.stream().filter(item->item.getParentId()==null).collect(Collectors.toList());
        Map<Long,UnitTaskDetailItem> unitItemBigMap = unitItemBigList.stream().collect(Collectors.toMap(UnitTaskDetailItem::getId,item->item, (v1, v2) -> v2));

        for (UnitTaskDetailItem item : itemList){
            if(item.getParentId() == null && !item.getUnitTaskDetailItemList().isEmpty()){//大项跳过
                continue;
            }
            String empCode = item.getEmpCode();
            String keyName = item.getName();
            //是子项
            if(item.getParentId() != null){
                UnitTaskDetailItem itemBig = unitItemBigMap.get(item.getParentId());
                if(itemBig == null){
                    continue;
                }
                keyName = itemBig.getName()+"-"+item.getName();
            }
            String value = dataMap.get(empCode+"-"+keyName);
            if(value == null){
                item.setPoint(BigDecimal.ZERO);
            }else {
                item.setPoint(new BigDecimal(value));
            }
        }
        //批量修改
        this.updateBatchById(itemList);

        //计算projectCount
        unitTaskProjectCountService.doCount(project.getUnitTaskId());

        //错误消息入redis
        redisUtil.set(CacheConstants.SEC_IMPORT_ERRLOG +unitTaskProjectDetailId,errorList,30, TimeUnit.MINUTES);

        //导入消息
        ImportResultVo vo = new ImportResultVo();
        vo.setTotalCount(xlsDataArr.length);
        if(!errorList.isEmpty()){
            vo.setErrorCount(errorList.size());
        }
        return vo;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVo importErciWork(Long unitTaskProjectDetailId, String[][] xlsDataArr) {
        UnitTaskProjectDetail detail = unitTaskProjectDetailMapper.selectById(unitTaskProjectDetailId);
        UnitTaskProject project = unitTaskProjectService.getById(detail.getUnitTaskProjectId());
        //人员
        List<UnitTaskUser> userList = unitTaskUserService.listByTaskId(project.getUnitTaskId());
        Map<String,UnitTaskUser> userMap = userList.stream().collect(Collectors.toMap(UnitTaskUser::getEmpCode,item->item, (v1, v2) -> v2));

        List<ProgDetailItem> progItemList = progDetailItemService.list(Wrappers.<ProgDetailItem>lambdaQuery().eq(ProgDetailItem::getProgProjectDetailId,detail.getProgProjectDetailId()));
        Map<Long,ProgDetailItem> itemMap = progItemList.stream().collect(Collectors.toMap(ProgDetailItem::getId,item->item, (v1, v2) -> v2));

        //逐条检验，成功的落库 和 失败的入redis
        List<String> errorList = new ArrayList<>();
        //key  empCode-detailName  ,value = qty
        Map<String,String> dataMap = new HashMap<>();
        for (int row=0;row< xlsDataArr.length;row++){
            String[] data = xlsDataArr[row];
            //姓名
            //String name = data[0];
            //工号
            String empCode = data[1];
            //校验row人员是否存在
            this.importDanxiangUserValid(errorList,empCode,row,"工号",userMap);
            int colIndex = 2;
            for (ProgDetailItem item : progItemList){
                //跳过采集型
                if(InputType.auto.toString().equals(CommonUtils.getDicVal(item.getInputType()))){
                    continue;
                }
                String itemName = item.getName();
                String value = data[colIndex++];
                //校验是否数字
                if(this.importDanxiangValid(errorList,value,row, itemName)){
                    dataMap.put(empCode+"-"+ itemName,value);
                }
            }
            String workRateStr = data[colIndex++];
            // 校验是否数字
            if (this.importDanxiangValid(errorList, workRateStr, row, "工作量系数")) {
                dataMap.put(empCode + "-" + "workRate", workRateStr);
            }
            String examPointStr = data[colIndex];
            // 校验是否数字
            if (this.importDanxiangValid(errorList, examPointStr, row, "考核得分")) {
                dataMap.put(empCode + "-" + "examPoint", examPointStr);
            }
        }

        // 导入数据，找不到就赋0
        List<UnitTaskDetailItem> itemList = this.listByPid(unitTaskProjectDetailId);
        Map<Long, UnitTaskDetailItem> unitItemMap = itemList.stream().collect(Collectors.toMap(UnitTaskDetailItem::getId, item -> item, (v1, v2) -> v2));

        for (UnitTaskDetailItem item : itemList) {
            // 跳过采集型
            if (InputType.auto.toString().equals(CommonUtils.getDicVal(item.getInputType()))) {
                continue;
            }
            String empCode = item.getEmpCode();
            String keyName = item.getName();
            String value = dataMap.get(empCode + "-" + keyName);
            if (value == null) {
                item.setPoint(BigDecimal.ZERO);
            } else {
                item.setPoint(new BigDecimal(value));
            }
            // 设置金额
            this.setItemAmt(item.getModeType(), item, project.getCarryRule());
        }
        // 批量修改
        this.updateBatchById(itemList);


        // 导入工作量系数
        List<UnitTaskDetailItemWork> itemWorks = unitTaskDetailItemWorkService.listByDetailId(unitTaskProjectDetailId);

        for (UnitTaskDetailItemWork itemWork : itemWorks) {
            String empCode = itemWork.getEmpCode();
            String workRateStr = dataMap.get(empCode + "-" + "workRate");
            if (workRateStr == null) {
                itemWork.setWorkRate(BigDecimal.ONE);
            } else {
                itemWork.setWorkRate(new BigDecimal(workRateStr));
            }
            String examPointStr = dataMap.get(empCode + "-" + "examPoint");
            if (workRateStr == null) {
                itemWork.setExamPoint(new BigDecimal("100.00"));
            } else {
                itemWork.setExamPoint(new BigDecimal(examPointStr));
            }
        }
        // 批量修改
        unitTaskDetailItemWorkService.updateBatchById(itemWorks);

        //计算projectCount
        unitTaskProjectCountService.doCount(project.getUnitTaskId());

        //错误消息入redis
        redisUtil.set(CacheConstants.SEC_IMPORT_ERRLOG +unitTaskProjectDetailId,errorList,30, TimeUnit.MINUTES);

        //导入消息
        ImportResultVo vo = new ImportResultVo();
        vo.setTotalCount(xlsDataArr.length);
        if(!errorList.isEmpty()){
            vo.setErrorCount(errorList.size());
        }
        return vo;
    }

    @Override
    public List<UnitTaskDetailItem> listByUnitTask(Long unitTaskId) {
        // 获得所有project
        List<UnitTaskProject> projectList = unitTaskProjectService.listByUnitTask(unitTaskId);
        // unitTaskProjectMapper.selectList(Wrappers.<UnitTaskProject>lambdaQuery().eq(UnitTaskProject::getUnitTaskId, unitTaskId).orderByAsc(UnitTaskProject::getSortNum));
        if (projectList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> projectIds = projectList.stream().map(UnitTaskProject::getId).collect(Collectors.toList());
        return this.list(Wrappers.<UnitTaskDetailItem>lambdaQuery().in(UnitTaskDetailItem::getUnitTaskProjectId, projectIds));
    }

    @Override
    public List<ProgDetailItem> getProgItemList(Long unitTaskId, Long unitDetailId) {
        List<ProgDetailItem> rtn = new ArrayList<>();
        List<UnitTaskDetailItem> itemList = this.listByPid(unitDetailId);
        Map<Long,ProgDetailItem> distinctMap = new HashMap<>();
        for (UnitTaskDetailItem item : itemList){
            Long progDetailItemId = item.getProgDetailItemId();
            if (distinctMap.get(progDetailItemId) != null){
                continue;
            }
            ProgDetailItem progDetailItem = new ProgDetailItem();
            BeanUtils.copyProperties(item, progDetailItem);
            progDetailItem.setId(progDetailItemId);
            rtn.add(progDetailItem);
            distinctMap.put(progDetailItemId,progDetailItem);
        }
        return rtn;
    }

    private void setItemAmt(String modeType, UnitTaskDetailItem unitTaskDetailItem, String carryRule) {
        if(modeType == null){
            unitTaskDetailItem.setAmt(unitTaskDetailItem.getPoint());
            return;
        }
        modeType = CommonUtils.getDicVal(modeType);
        if(ModeType.input.toString().equals(modeType)){//直接输入金额
            unitTaskDetailItem.setAmt(unitTaskDetailItem.getPoint());
        }else{//金额 = 数量 * 单价
            if(unitTaskDetailItem.getPoint() != null && unitTaskDetailItem.getPriceValue() != null){
                BigDecimal amt = unitTaskDetailItem.getPoint().multiply(unitTaskDetailItem.getPriceValue());
                amt = CommonUtils.amtSetScale(amt, 6,carryRule);
                unitTaskDetailItem.setAmt(amt);
            }
        }
    }

    private List<UnitTaskDetailItem> listByPid(Long unitTaskProjectDetailId) {
        return this.list(Wrappers.<UnitTaskDetailItem>lambdaQuery().eq(UnitTaskDetailItem::getUnitTaskProjectDetailId,unitTaskProjectDetailId)
                .orderByAsc(UnitTaskDetailItem::getParentId,UnitTaskDetailItem::getSortNum));
    }

    private boolean importDanxiangValid(List<String> errorList, String qty, int row, String colName) {
        row++;
        if(StringUtils.isBlank(qty)){
            errorList.add("原文件第"+row+"行，"+colName+"[未填写]错误");
            return false;
        }
        try {
            new BigDecimal(qty);
        }catch (Exception e){
            errorList.add("原文件第"+row+"行，"+colName+"["+qty+"]错误");
            return false;
        }
        return true;
    }

    private Boolean importDanxiangUserValid(List<String> errorList, String empCode, int row, String colName, Map<String, UnitTaskUser> userMap) {
        row++;
        if(userMap.get(empCode) == null){
            errorList.add("原文件第"+row+"行，"+colName+"["+empCode+"]不存在或匹配不上");
            return false;
        }
        return true;
    }

    private List<ProgDetailItem> getSyncByDetailEditList(List<ProgDetailItem> progItemList, List<ProgDetailItem> currentProgItemList) {
        List<ProgDetailItem> editList = new ArrayList<>();
        for (ProgDetailItem progDetailItem : progItemList) {
            for (ProgDetailItem currentProgDetailItem : currentProgItemList) {
                if (Objects.equals(progDetailItem.getId(), currentProgDetailItem.getId()) && !ifProgItemEquals(progDetailItem,currentProgDetailItem)) {
                    editList.add(progDetailItem);
                    break;
                }
            }
        }
        return editList;
    }

    private Boolean ifProgItemEquals(ProgDetailItem progDetailItem, ProgDetailItem currentProgDetailItem) {
        if(!Objects.equals(progDetailItem.getName(),currentProgDetailItem.getName())
                || !Objects.equals(progDetailItem.getPriceValue(),currentProgDetailItem.getPriceValue())
                || !Objects.equals(progDetailItem.getInputType(),currentProgDetailItem.getInputType())
                || !Objects.equals(progDetailItem.getAccountItemCode(),currentProgDetailItem.getAccountItemCode())
                || !Objects.equals(progDetailItem.getIfExtendLast(),currentProgDetailItem.getIfExtendLast())){
            return false;
        }
        return true;
    }

    private List<ProgDetailItem> task2progItem(List<UnitTaskDetailItem> taskItemList) {
        Map<Long,ProgDetailItem> map = new HashMap<>();//key : progDetailItemId
        for (UnitTaskDetailItem taskDetailItem : taskItemList){
            Long progItemId = taskDetailItem.getProgDetailItemId();
            if(map.get(progItemId) == null){
                ProgDetailItem progItem = new ProgDetailItem();
                BeanUtils.copyProperties(taskDetailItem, progItem);
                progItem.setId(taskDetailItem.getProgDetailItemId());
                map.put(progItemId,progItem);
            }
        }
        return new ArrayList<>(map.values());
    }

    private void addByUnitTaskProject(UnitTask unitTask, UnitTaskProject unitTaskProject,UnitTaskProjectDetail unitTaskProjectDetail, List<ProgDetailItem> addList) {
        Long unitTaskId = unitTask.getId();
        Long progDetailId = unitTaskProjectDetail.getProgProjectDetailId();
        for (ProgDetailItem progDetailItem : addList){
            progDetailItem.setId(null);
            progDetailItem.setProgProjectDetailId(progDetailId);
        }
        //新增方案detail
        progDetailItemService.saveBatch(addList);

        //新增任务detail
        List<UnitTaskUser> userList = unitTaskUserService.list(Wrappers.<UnitTaskUser>lambdaQuery().eq(UnitTaskUser::getUnitTaskId,unitTaskId));
        this.addByProgDetailItemList(unitTaskProject,unitTaskProjectDetail,userList,addList);
    }

    private void updateByUnitTaskProject(UnitTask unitTask, UnitTaskProject unitTaskProject, UnitTaskProjectDetail unitTaskProjectDetail, List<ProgDetailItem> updateList) {
        Long unitTaskId = unitTask.getId();
        // 更新方案detail
        progDetailItemService.updateBatchById(updateList);

        // 更新任务detail
        List<UnitTaskUser> userList = unitTaskUserService.list(Wrappers.<UnitTaskUser>lambdaQuery().eq(UnitTaskUser::getUnitTaskId, unitTaskId));
        this.updateByProgDetailItemList(unitTaskProject, unitTaskProjectDetail, userList, updateList);
    }

    private void updateByProgDetailItemList(UnitTaskProject unitTaskProject, UnitTaskProjectDetail unitTaskProjectDetail, List<UnitTaskUser> userList, List<ProgDetailItem> progDetailItemList) {
        // 分配方式
        String modeTpe = CommonUtils.getDicVal(unitTaskProjectDetail.getModeType());

        for (ProgDetailItem progDetailItem : progDetailItemList) {// 先循环item
            Long progDetailItemId = progDetailItem.getId();
            Long progProjectDetailId = progDetailItem.getProgProjectDetailId();
            // 获取progDetailItem下所有UnitTaskDetailItem
            List<UnitTaskDetailItem> list = this.list(Wrappers.<UnitTaskDetailItem>lambdaQuery().eq(UnitTaskDetailItem::getProgProjectDetailId, progProjectDetailId)
                    .eq(UnitTaskDetailItem::getProgDetailItemId, progDetailItemId));
            for (UnitTaskDetailItem unitTaskDetailItem : list) {
                if (Objects.equals(ModeType.work.toString(), modeTpe)) {
                    // 工作量 类型的 progDetailItem 更新，需要联动更新 unitTaskDetailItem 的值
                    unitTaskDetailItem.setModeType(progDetailItem.getModeType());
                    unitTaskDetailItem.setPriceValue(progDetailItem.getPriceValue());
                    unitTaskDetailItem.setInputType(progDetailItem.getInputType());
                    unitTaskDetailItem.setIfExtendLast(progDetailItem.getIfExtendLast());
                } else if (Objects.equals(ModeType.ratio.toString(), modeTpe)) {
                }
                unitTaskDetailItem.setName(progDetailItem.getName());
                this.setItemAmt(progDetailItem.getModeType(), unitTaskDetailItem, unitTaskProject.getCarryRule());
            }
            if (CollUtil.isNotEmpty(list)) {
                this.updateBatchById(list);
            }
        }
    }

    private void addByProgDetailItemList(UnitTaskProject unitTaskProject,UnitTaskProjectDetail unitTaskProjectDetail, List<UnitTaskUser> userList, List<ProgDetailItem> progDetailItemList) {
        List<UnitTaskDetailItem> addList = new ArrayList<>();
        String userIds = userList.stream().map(UnitTaskUser::getUserId).collect(Collectors.joining(","));


        String modeType = CommonUtils.getDicVal(unitTaskProjectDetail.getModeType());

        UnitTask unitTask = unitTaskMapper.selectById(unitTaskProject.getUnitTaskId());
        String cycle = unitTask.getCycle();
        GrantUnit grantUnit = grantUnitService.getById(unitTask.getGrantUnitId());
        String deptIds = grantUnit.getKsUnitIds();

        for (ProgDetailItem progDetailItem : progDetailItemList) {//先循环item
            //是否需要采集数据
            Map<String, List<ItemValueDTO>> userItemValueMap = new HashMap<>();
            String inputType = CommonUtils.getDicVal(progDetailItem.getInputType());
            if (InputType.auto.toString().equals(inputType)) {
                List<ItemValueDTO> itemValueDTOList = secondKpiService.accountItemValue(cycle,deptIds,userIds, progDetailItem.getAccountItemCode(),progDetailItem.getAccountItemType());
                userItemValueMap = itemValueDTOList.stream().collect(Collectors.groupingBy(ItemValueDTO::getUserId));
            }
            //继承上一次
            Map<String, UnitTaskDetailItem> lastItemValueMap = new HashMap<>();
            List<UnitTaskDetailItem> userItemValueList = this.getLastItemValue(unitTaskProjectDetail.getId(), unitTaskProjectDetail.getProgProjectDetailId(), progDetailItem);
            lastItemValueMap = userItemValueList.stream().collect(Collectors.toMap(UnitTaskDetailItem::getEmpCode, item -> item, (v1, v2) -> v2));

            for (UnitTaskUser unitTaskUser : userList) {//再循环人员
                String empCode = unitTaskUser.getEmpCode();
                String userId = unitTaskUser.getUserId();
                UnitTaskDetailItem unitTaskDetailItem = new UnitTaskDetailItem();//每人每个大项一笔
                BeanUtils.copyProperties(progDetailItem, unitTaskDetailItem);

                unitTaskDetailItem.setId(null);
                unitTaskDetailItem.setEmpCode(unitTaskUser.getEmpCode());
                unitTaskDetailItem.setUnitTaskProjectDetailId(unitTaskProjectDetail.getId());
                unitTaskDetailItem.setProgDetailItemId(progDetailItem.getId());
                unitTaskDetailItem.setUnitTaskProjectId(unitTaskProject.getId());

                if (Objects.equals(ModeType.work.toString(), modeType)) {
                    if (InputType.auto.toString().equals(inputType)) {
                        List<ItemValueDTO> itemValueDTOList = userItemValueMap.get(userId);
                        if (itemValueDTOList != null) {
                            BigDecimal itemValue = itemValueDTOList.stream().map(o->(o.getItemValue() == null?BigDecimal.ZERO:new BigDecimal(o.getItemValue()))).reduce(BigDecimal.ZERO,BigDecimal::add);
                            unitTaskDetailItem.setPoint(itemValue);
                        }

                    } else if (InputType.input.toString().equals(inputType) && "1".equals(progDetailItem.getIfExtendLast())) {
                        // 继承上个月量
                        UnitTaskDetailItem lastItem = lastItemValueMap.get(empCode);
                        if (lastItem != null) {
                            unitTaskDetailItem.setPoint(lastItem.getPoint());
                        }
                    }
                    // 每数量单位标准 增加继承逻辑
                    UnitTaskDetailItem lastItem = lastItemValueMap.get(empCode);
                    if (lastItem != null) {
                        unitTaskDetailItem.setPriceValue(lastItem.getPriceValue());
                    }
                } else if (Objects.equals(ModeType.ratio.toString(), modeType)) {
                    UnitTaskDetailItem lastItem = lastItemValueMap.get(empCode);
                    if (lastItem != null) {
                        unitTaskDetailItem.setPoint(lastItem.getPoint());
                        unitTaskDetailItem.setPriceValue(lastItem.getPriceValue());
                    }
                }
                this.setItemAmt(progDetailItem.getModeType(),unitTaskDetailItem,unitTaskProject.getCarryRule());

                addList.add(unitTaskDetailItem);
            }
        }
        if(!addList.isEmpty()){
            this.saveBatch(addList);
        }
        //替换parentId ，之前为 方案的parentId，要换成 任务的parentId
        this.changeParentId(addList,unitTaskProjectDetail.getId());
        if(!addList.isEmpty()){
            this.updateBatchById(addList);
        }

    }

    private List<UnitTaskDetailItem> getLastItemValue(Long detailId, Long progProjectDetailId, ProgDetailItem progDetailItem) {
        //查询最近一笔 progDetailId 所对应的 unitDetailId
        UnitTaskProjectDetail unitTaskProjectDetail = unitTaskProjectDetailMapper.selectOne(Wrappers.<UnitTaskProjectDetail>lambdaQuery()
                .eq(UnitTaskProjectDetail::getProgProjectDetailId,progProjectDetailId)
                .ne(UnitTaskProjectDetail::getId,detailId)
                .orderByDesc(UnitTaskProjectDetail::getCreateTime)
                .last("limit 1"));
        //查询 unitDetailId 下的 所有 progDetailItem 对应的unitItem的数据
        if(unitTaskProjectDetail != null){
            return this.list(Wrappers.<UnitTaskDetailItem>lambdaQuery()
                    .eq(UnitTaskDetailItem::getUnitTaskProjectDetailId,unitTaskProjectDetail.getId())
                    .eq(UnitTaskDetailItem::getProgDetailItemId,progDetailItem.getId()));
        }
        return new ArrayList<>();
    }

    private void changeParentId(List<UnitTaskDetailItem> addList, Long detailId) {
        //key ：empcode+item对应的方案itemid ，value: item
        Map<String,UnitTaskDetailItem> map = new HashMap<>();
        for (UnitTaskDetailItem detailItem : addList){
            String empCode = detailItem.getEmpCode();
            map.put(empCode+"-"+detailItem.getProgDetailItemId(),detailItem);
        }
        //addList.stream().collect(Collectors.toMap(UnitTaskDetailItem::getProgDetailItemId,item->item,(k1,k2)->k1));
        for (UnitTaskDetailItem item : addList){
            Long parentId = item.getParentId();
            String empCode = item.getEmpCode();
            UnitTaskDetailItem parentItem = map.get(empCode+"-"+parentId);
            if(parentItem != null){
                item.setParentId(parentItem.getId());
            }else{//单独增子项 时 ，大项要去数据库查
                parentItem = this.getParentItem(detailId,empCode,parentId);
                if(parentItem != null){
                    item.setParentId(parentItem.getId());
                }
            }
        }
    }

    private UnitTaskDetailItem getParentItem(Long detailId, String empCode, Long progDetailItemId) {
        return this.getOne(Wrappers.<UnitTaskDetailItem>lambdaQuery()
                .eq(UnitTaskDetailItem::getUnitTaskProjectDetailId,detailId)
                .eq(UnitTaskDetailItem::getEmpCode,empCode)
                .eq(UnitTaskDetailItem::getProgDetailItemId,progDetailItemId));
    }

    private void delByUnitTaskProject(UnitTask unitTask, UnitTaskProject unitTaskProject,UnitTaskProjectDetail unitTaskProjectDetail, List<ProgDetailItem> progDelList) {
        Long unitTaskProjectDetailId = unitTaskProjectDetail.getId();
        //删除任务detail
        List<Long> progItemIdList = progDelList.stream()
                .map(ProgDetailItem::getId)
                .collect(Collectors.toList());
        this.remove(Wrappers.<UnitTaskDetailItem>lambdaQuery().eq(UnitTaskDetailItem::getUnitTaskProjectDetailId,unitTaskProjectDetailId).in(UnitTaskDetailItem::getProgDetailItemId,progItemIdList));

        //删除方案detail
        progDetailItemService.removeBatchByIds(progDelList);
    }
}
