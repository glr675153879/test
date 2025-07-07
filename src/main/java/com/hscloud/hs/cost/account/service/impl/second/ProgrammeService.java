package com.hscloud.hs.cost.account.service.impl.second;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.second.ActionType;
import com.hscloud.hs.cost.account.mapper.second.ProgrammeMapper;
import com.hscloud.hs.cost.account.model.dto.second.ProgrammePublishDTO;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.model.vo.second.ProgrammeInfoVo;
import com.hscloud.hs.cost.account.service.impl.second.async.ProgrammeAsyncService;
import com.hscloud.hs.cost.account.service.impl.second.async.SecRedisService;
import com.hscloud.hs.cost.account.service.second.*;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.hscloud.hs.cost.account.utils.RegularUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 核算方案 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgrammeService extends ServiceImpl<ProgrammeMapper, Programme> implements IProgrammeService {
    @Lazy
    @Autowired
    private IUnitTaskService unitTaskService;
    @Lazy
    @Autowired
    private IUnitTaskProjectService unitTaskProjectService;
    private final IProgProjectService progProjectService;
    private final ProgProjectDetailService progProjectDetailService;
    private final ProgDetailItemService progDetailItemService;
    private final IGrantUnitService grantUnitService;
    private final RegularUtil regularUtil;
    private final ProgrammeAsyncService programmeAsyncService;
    private final SecRedisService secRedisService;
    @Override
    public Programme getByUnitId(Long unitId) {
        return this.getOne(Wrappers.<Programme>lambdaQuery().eq(Programme::getGrantUnitId, unitId)
                .eq(Programme::getStatus,"0").last("limit 1"));
    }

    @Override
    @Transactional
    public void publish(ProgrammePublishDTO programmePublishDTO) {
        secRedisService.clearCache(null);
        //查询方案信息,判断是否公共
        Programme programme = new Programme().selectById(programmePublishDTO.getProgrammeId());
        //修改自己
        Long programmeId = programmePublishDTO.getProgrammeId();
        //判断是否公共模板
        if ("1".equals(programme.getIfCommon())) {
            publishCommonPlan(programmePublishDTO, programme, programmeId);

            //同步发放单元方案(主档)
            this.syncByProgCommon(programme);

            //该公共方案下的 所有发放单元方案
            List<Programme> unitProgrammeList = this.listByProgCommonId(programme.getId());
            for (Programme unitProgramme : unitProgrammeList){
                programmeAsyncService.syncByProgramme(unitProgramme);
//                //同步分配方案
//                progProjectService.syncByProgramme(unitProgramme);
//                //同步分配任务
//                unitTaskService.syncByProgramme(unitProgramme);
            }

            //unitProgrammeList.forEach(unitTaskService::syncByProgramme);

        } else {
            publishPlan(programmePublishDTO, programmeId);
        }



    }

    /**
     * 处理同步
     *
     * @param programmePublishDTO
     * @param programme
     * @param programmeId
     */
    private void publishCommonPlan(ProgrammePublishDTO programmePublishDTO, Programme programme, Long programmeId) {
        //获取发放单元方案,没有则新增
        //List<Programme> grantList = getChildProgramme(programme, programmeId);
        List<Programme> grantList = new ArrayList<>();
        //处理project
        for (int i = 0; i < programmePublishDTO.getProjectList().size(); i++) {
            ProgProject progProject = programmePublishDTO.getProjectList().get(i);
            progProject.setSortNum(i + 1);
            progProject.setProgrammeId(programmeId);
            String actionType = progProject.getActionType();
            //新增的项目
            if (ActionType.add.toString().equals(actionType)) {
                saveProject(progProject, grantList);
            } else if (ActionType.edit.toString().equals(actionType)) {//修改的项目
                progProjectService.updateById(progProject);
                editProject(progProject, grantList);
            } else if (ActionType.del.toString().equals(actionType)) {//删除的项目
                delProject(progProject);
            } else {
                progProjectService.updateById(progProject);
                editProject(progProject,grantList);
            }
        }
    }


    /**
     * 删除
     *
     * @param progProject
     */
    private void delProject(ProgProject progProject) {
        progProjectService.removeById(progProject);
        final List<ProgProject> progProjectList = new ProgProject().selectList(new LambdaQueryWrapper<ProgProject>()
                .eq(ProgProject::getCommonId, progProject.getId()));
        progProjectService.removeBatchByIds(progProjectList);
        delCommonDetail(progProject.getProgProjectDetailList());
    }

    /**
     * 修改project
     *
     * @param progProject
     * @param grantList
     */
    private void editProject(ProgProject progProject, List<Programme> grantList) {
        grantList = new ArrayList<>();
        List<ProgProject> addList = new ArrayList<>();
        List<ProgProject> editList = new ArrayList<>();
        //progProjectService.updateById(progProject);//不能放editList其他方法要用
        for (Programme childProgramme : grantList) {
            //先查有没有,没有就新增,有就修改
            //todo 后期可用map优化
            ProgProject childProject = new ProgProject().selectOne(new LambdaQueryWrapper<ProgProject>()
                    .eq(ProgProject::getCommonId, progProject.getId())
                    .eq(ProgProject::getProgrammeId, childProgramme.getId()));
            if (childProject == null) {
                ProgProject project = new ProgProject();
                BeanUtils.copyProperties(progProject, project);
                project.setId(null);
                project.setProgrammeId(childProgramme.getId());
                project.setCommonId(progProject.getId());
                addList.add(project);
            } else {
                Long id = childProject.getId();
                ProgProject project = new ProgProject();
                BeanUtils.copyProperties(progProject, project);
                project.setProgrammeId(childProgramme.getId());
                project.setCommonId(progProject.getId());
                project.setId(id);
                editList.add(project);
            }
        }
        progProjectService.saveBatch(addList);
        progProjectService.updateBatchById(editList);
        List<ProgProject> list = new ArrayList<>();
        list.addAll(addList);
        list.addAll(editList);
        publishEditCommonDetail(progProject, list);
    }

    /**
     * 新增project
     *
     * @param progProject
     */
    private void saveProject(ProgProject progProject, List<Programme> grantList) {
        grantList = new ArrayList<>();
        progProjectService.save(progProject);
        List<ProgProject> list = grantList.stream().map(childProgramme -> {
            ProgProject childProject = new ProgProject();
            BeanUtils.copyProperties(progProject, childProject);
            childProject.setId(null);
            childProject.setProgrammeId(childProgramme.getId());
            childProject.setCommonId(progProject.getId());
            return childProject;
        }).collect(Collectors.toList());
        progProjectService.saveBatch(list);
        publishCommonDetail(progProject, list);
    }

    /**
     * 获取发放单元方案
     *
     * @param programme
     * @param programmeId
     * @return
     */
    private List<Programme> getChildProgramme(Programme programme, Long programmeId) {
        List<Long> ids = regularUtil.getIds(programme.getGrantUnitIds());
        List<Programme> grantList = this.list(Wrappers.<Programme>lambdaQuery().eq(Programme::getParentId, programmeId));
        Set<Long> idsToRemove = grantList.stream()
                .map(Programme::getGrantUnitId)
                .collect(Collectors.toSet()); // 收集所有需要移除的ID
        ids.removeAll(idsToRemove);
        for (Long id : ids) {
            Programme childProgramme = new Programme();
            BeanUtils.copyProperties(programme,childProgramme);
            childProgramme.setId(null);
            childProgramme.setGrantUnitId(id);
            childProgramme.setIfCommon("0");
            childProgramme.setParentId(programmeId);
            childProgramme.setGrantUnitIds(null);
            childProgramme.setGrantUnitNames(null);
            this.save(childProgramme);
            grantList.add(childProgramme);
        }
        return grantList;
    }

    private void delCommonDetail(List<ProgProjectDetail> detailList) {
        for (ProgProjectDetail progProjectDetail : detailList) {
            progProjectDetailService.removeById(progProjectDetail);
            final List<ProgProjectDetail> progProjectDetailList = new ProgProjectDetail().selectList(new LambdaQueryWrapper<ProgProjectDetail>()
                    .eq(ProgProjectDetail::getCommonId, progProjectDetail.getId()));
            progProjectDetailService.removeBatchByIds(progProjectDetailList);
            delCommonItem(progProjectDetail);
        }
    }


    private void delCommonItem(ProgProjectDetail progProjectDetail) {
        if (!progProjectDetail.getProgDetailItemList().isEmpty()) {
            progDetailItemService.removeBatchByIds(progProjectDetail.getProgDetailItemList());
            for (ProgDetailItem progDetailItem : progProjectDetail.getProgDetailItemList()) {
                final List<ProgDetailItem> detailItemList = new ProgDetailItem().selectList(new LambdaQueryWrapper<ProgDetailItem>()
                        .eq(ProgDetailItem::getCommonId, progDetailItem.getId()));
                progDetailItemService.removeBatchByIds(detailItemList);
                if (!progDetailItem.getChildItemList().isEmpty()) {
                    progDetailItemService.removeBatchByIds(progDetailItem.getChildItemList());
                    for (ProgDetailItem detailItem : progDetailItem.getChildItemList()) {
                        final List<ProgDetailItem> newDetailItemList = new ProgDetailItem().selectList(new LambdaQueryWrapper<ProgDetailItem>()
                                .eq(ProgDetailItem::getCommonId, detailItem.getId()));
                        progDetailItemService.removeBatchByIds(newDetailItemList);
                    }
                }
            }
        }

    }

    /**
     * 修改detail
     *
     * @param progProject
     */
    private void publishEditCommonDetail(ProgProject progProject, List<ProgProject> childProjectList) {
        List<ProgProjectDetail> detailList = new ArrayList<>();
        long sortNum = 1;
        for (ProgProjectDetail progProjectDetail : progProject.getProgProjectDetailList()) {
            progProjectDetail.setSortNum(sortNum++);
            final String actionType = progProjectDetail.getActionType();
            if (ActionType.add.toString().equals(actionType)) {
                //新增detail
                progProjectDetail.setProgProjectId(progProject.getId());
                progProjectDetailService.save(progProjectDetail);
                //新增发放单元detail
                List<ProgProjectDetail> unitDetailList = childProjectList.stream().map(project -> {
                    ProgProjectDetail childDetail = new ProgProjectDetail();
                    BeanUtils.copyProperties(progProjectDetail, childDetail);
                    childDetail.setId(null);
                    childDetail.setCommonId(progProjectDetail.getId());
                    childDetail.setProgCommonId(project.getProgCommonId());
                    childDetail.setProgProjectId(project.getId());
                    return childDetail;
                }).collect(Collectors.toList());
                progProjectDetailService.saveBatch(unitDetailList);

                publishCommonItem(progProjectDetail, unitDetailList);
            } else if (ActionType.edit.toString().equals(actionType)) {//修改的项目
                progProjectDetailService.updateById(progProjectDetail);
                for (ProgProject childProject : childProjectList) {
                    final ProgProjectDetail childDetail = new ProgProjectDetail().selectOne(new LambdaQueryWrapper<ProgProjectDetail>()
                            //.eq(ProgProjectDetail::getProgProjectId, progProject.getId())
                            .eq(ProgProjectDetail::getCommonId, progProjectDetail.getId()));
                    if (childDetail == null) {
                        BeanUtils.copyProperties(progProjectDetail, childDetail);
                        childDetail.setId(null);
                        childDetail.setCommonId(progProjectDetail.getId());
                        childDetail.setProgCommonId(childProject.getProgCommonId());
                        childDetail.setProgProjectId(childProject.getId());
                        progProjectDetailService.save(childDetail);
                    } else {
                        Long id = childDetail.getId();
                        BeanUtils.copyProperties(progProjectDetail, childDetail);
                        childDetail.setId(id);
                        childDetail.setCommonId(progProjectDetail.getId());
                        childDetail.setProgCommonId(childProject.getProgCommonId());
                        childDetail.setProgProjectId(childProject.getId());
                        progProjectDetailService.updateById(childDetail);
                    }
                }
                publishEditCommonItem(progProjectDetail);
            } else if (ActionType.del.toString().equals(actionType)) {//删除的项目
                detailList.add(progProjectDetail);
            }else{
                publishEditCommonItem(progProjectDetail);
            }
        }
        delCommonDetail(detailList);
    }

    /**
     * 修改Item 大项
     *
     * @param progProjectDetail
     */
    private void publishEditCommonItem(ProgProjectDetail progProjectDetail) {
        if (!progProjectDetail.getProgDetailItemList().isEmpty()) {
            final List<ProgProjectDetail> unitDetailList = new ProgProjectDetail().selectList(new LambdaQueryWrapper<ProgProjectDetail>()
                    .eq(ProgProjectDetail::getCommonId, progProjectDetail.getId()));
            int sortNum = 1;
            for (ProgDetailItem progDetailItemBig : progProjectDetail.getProgDetailItemList()) {
                String actionType = progDetailItemBig.getActionType();
                //新增的项目
                if (ActionType.add.toString().equals(actionType)) {
                    progDetailItemBig.setSortNum((long) sortNum++);
                    progDetailItemBig.setProgProjectDetailId(progProjectDetail.getId());
                    this.addItemBig(progDetailItemBig,unitDetailList);
                } else if (ActionType.edit.toString().equals(actionType)) {//修改的项目
                    progDetailItemBig.setSortNum((long) sortNum++);
                    progDetailItemService.updateById(progDetailItemBig);
                    for (ProgProjectDetail childDetail : unitDetailList) {
                        ProgDetailItem childItem = new ProgDetailItem().selectOne(new LambdaQueryWrapper<ProgDetailItem>()
                                .eq(ProgDetailItem::getProgProjectDetailId, childDetail.getId())
                                .eq(ProgDetailItem::getCommonId, progDetailItemBig.getId()));
                        if (childItem == null) {
                            BeanUtils.copyProperties(progDetailItemBig, childItem);
                            childItem.setProgCommonId(childDetail.getProgCommonId());
                            childItem.setProgProjectDetailId(childDetail.getId());
                            childItem.setCommonId(progDetailItemBig.getId());
                            childItem.setId(null);
                            progDetailItemService.save(childItem);
                        } else {
                            Long id = childItem.getId();
                            BeanUtils.copyProperties(progDetailItemBig, childItem);
                            childItem.setProgCommonId(childDetail.getProgCommonId());
                            childItem.setProgProjectDetailId(childDetail.getId());
                            childItem.setCommonId(progDetailItemBig.getId());
                            childItem.setId(id);
                            progDetailItemService.updateById(childItem);
                        }
                    }
                    //todo 子项处理
                    if (!progDetailItemBig.getChildItemList().isEmpty()) {
                        int smallSortNum = 1;
                        for (ProgDetailItem detailItem : progDetailItemBig.getChildItemList()) {
                            detailItem.setSortNum((long) smallSortNum++);
                            detailItem.setParentId(progDetailItemBig.getId());
                            detailItem.setProgCommonId(progDetailItemBig.getProgCommonId());
                            detailItem.setProgProjectDetailId(progDetailItemBig.getProgProjectDetailId());
                            progDetailItemService.updateById(progDetailItemBig);
//                            final List<ProgDetailItem> childItemList = new ProgDetailItem().selectList(new LambdaQueryWrapper<ProgDetailItem>()
//                                    .eq(ProgDetailItem::getCommonId, progDetailItemBig.getId()));
//                            for (ProgDetailItem item : childItemList) {
//                                ProgDetailItem childItem = new ProgDetailItem();
//                                BeanUtils.copyProperties(detailItem, childItem);
//                                childItem.setId(null);
//                                childItem.setCommonId(item.getId());
//                                childItem.setProgCommonId(item.getProgCommonId());
//                                childItem.setProgProjectDetailId(item.getId());
//                                progDetailItemService.save(childItem);
//                            }
                        }
                    }
                } else if (ActionType.del.toString().equals(actionType)) {//删除的项目
                    progDetailItemService.removeById(progDetailItemBig);
                    for (ProgProjectDetail childDetail : unitDetailList) {
                        ProgDetailItem childItem = new ProgDetailItem().selectOne(new LambdaQueryWrapper<ProgDetailItem>()
                                .eq(ProgDetailItem::getProgProjectDetailId, childDetail.getId())
                                .eq(ProgDetailItem::getCommonId, progDetailItemBig.getId()));
                        if (childItem == null) {
                            progDetailItemService.removeById(childItem);
                        }
                    }
                }else{
                    if (!progDetailItemBig.getChildItemList().isEmpty()) {
                        int sortNum2 = 1;
                        for (ProgDetailItem progDetailItem : progDetailItemBig.getChildItemList()) {
                            progDetailItem.setSortNum((long) sortNum2++);
                        }
                        //新增 标记add的子项
                        List<ProgDetailItem> addChildItemList = progDetailItemBig.getChildItemList().stream().filter(item->ActionType.add.toString().equals(item.getActionType())).collect(Collectors.toList());
                        this.addChildItem(progDetailItemBig, addChildItemList);

                        //修改 标记edit的子项 只能修改名称
                        List<ProgDetailItem> editChildItemList = progDetailItemBig.getChildItemList().stream().filter(item->ActionType.edit.toString().equals(item.getActionType())).collect(Collectors.toList());
                        this.editChildItem(editChildItemList);

                        //删除 标记del的子项
                        List<ProgDetailItem> delChildItemList = progDetailItemBig.getChildItemList().stream().filter(item->ActionType.del.toString().equals(item.getActionType())).collect(Collectors.toList());
                        this.delChildItem(delChildItemList);

                    }
                }
            }

        }


    }

    private void addItemBig(ProgDetailItem progDetailItemBig, List<ProgProjectDetail> unitDetailList) {
        //大项
        progDetailItemBig.setId(null);
        progDetailItemService.save(progDetailItemBig);
        //发放单元 大项
        for (ProgProjectDetail childDetail : unitDetailList) {
            ProgDetailItem childItem = new ProgDetailItem();
            BeanUtils.copyProperties(progDetailItemBig, childItem);
            childItem.setId(null);
            childItem.setProgCommonId(childDetail.getProgCommonId());
            childItem.setProgProjectDetailId(childDetail.getId());
            childItem.setCommonId(progDetailItemBig.getId());
            progDetailItemService.save(childItem);
        }
        //新增所有子项
        if (!progDetailItemBig.getChildItemList().isEmpty()) {
            this.addChildItem(progDetailItemBig,progDetailItemBig.getChildItemList());
        }
    }

    private void delChildItem(List<ProgDetailItem> delChildItemList) {
        progDetailItemService.removeBatchByIds(delChildItemList);
        for (ProgDetailItem childItem : delChildItemList) {
            //删除子项
            Long childItemId = childItem.getId();
            //progDetailItemService.removeById(childItemId);
            //删除发放单元子项,根据对应的 公共子项id删
            progDetailItemService.remove(Wrappers.<ProgDetailItem>lambdaQuery().eq(ProgDetailItem::getCommonId,childItemId));
        }
    }

    private void editChildItem(List<ProgDetailItem> editChildItemList) {
        //修改子项
        progDetailItemService.updateBatchById(editChildItemList);
        for (ProgDetailItem childItem : editChildItemList) {
            Long childItemId = childItem.getId();
            //删除发放单元子项,根据对应的 公共子项id删
            progDetailItemService.update(null,Wrappers.<ProgDetailItem>lambdaUpdate()
                    .eq(ProgDetailItem::getCommonId,childItemId)
                    .set(ProgDetailItem::getName,childItem.getName()));
        }
    }

    private void addChildItem(ProgDetailItem progDetailItemBig, List<ProgDetailItem> childItemList) {
        int sortNum = 1;
        for (ProgDetailItem detailItem : childItemList) {
            //新增子项
            detailItem.setId(null);
            detailItem.setSortNum((long) sortNum++);
            detailItem.setParentId(progDetailItemBig.getId());
            detailItem.setProgCommonId(progDetailItemBig.getProgCommonId());
            detailItem.setProgProjectDetailId(progDetailItemBig.getProgProjectDetailId());
            progDetailItemService.save(detailItem);
            //为发放单元大项 增子项
            final List<ProgDetailItem> unitItemBigList = new ProgDetailItem().selectList(new LambdaQueryWrapper<ProgDetailItem>()
                    .eq(ProgDetailItem::getCommonId, progDetailItemBig.getId()));
            for (ProgDetailItem unitItemBig : unitItemBigList) {
                ProgDetailItem unitChildItem = new ProgDetailItem();
                BeanUtils.copyProperties(detailItem, unitChildItem);
                unitChildItem.setId(null);
                unitChildItem.setCommonId(detailItem.getId());
                unitChildItem.setProgCommonId(detailItem.getProgCommonId());
                unitChildItem.setParentId(unitItemBig.getId());
                unitChildItem.setProgProjectDetailId(unitItemBig.getProgProjectDetailId());
                progDetailItemService.save(unitChildItem);
            }
        }
    }

    /**
     * 复制Detail
     *
     * @param progProject
     */
    private void publishCommonDetail(ProgProject progProject, List<ProgProject> childProjectList) {
        //批量插入
        long sortNum = 1;
        for (ProgProjectDetail progProjectDetail : progProject.getProgProjectDetailList()) {
            progProjectDetail.setId(null);
            progProjectDetail.setSortNum(sortNum++);
            progProjectDetail.setProgProjectId(progProject.getId());
        };
        progProjectDetailService.saveBatch(progProject.getProgProjectDetailList());
        //遍历,同步
        for (ProgProjectDetail progProjectDetail : progProject.getProgProjectDetailList()) {
            List<ProgProjectDetail> list = childProjectList.stream().map(project -> {
                ProgProjectDetail childDetail = new ProgProjectDetail();
                BeanUtils.copyProperties(progProjectDetail, childDetail);
                childDetail.setId(null);
                childDetail.setCommonId(progProjectDetail.getId());
                childDetail.setProgCommonId(project.getProgCommonId());
                childDetail.setProgProjectId(project.getId());
                return childDetail;
            }).collect(Collectors.toList());
            progProjectDetailService.saveBatch(list);
            publishCommonItem(progProjectDetail, list);
        }
    }

    /**
     * 复制Item
     *
     * @param progProjectDetail
     */
    private void publishCommonItem(ProgProjectDetail progProjectDetail, List<ProgProjectDetail> unitDetailList) {
        if (!progProjectDetail.getProgDetailItemList().isEmpty()) {
            int sortNum = 1;
            for (ProgDetailItem progDetailItem :progProjectDetail.getProgDetailItemList()){
                progDetailItem.setSortNum((long) sortNum++);
                progDetailItem.setProgProjectDetailId(progProjectDetail.getId());
                //新增大项
                this.addItemBig(progDetailItem,unitDetailList);
            }
            //新增 item大项
//            progProjectDetail.getProgDetailItemList().stream().forEach(progDetailItem -> progDetailItem.setProgProjectDetailId(progProjectDetail.getId()));
//            progDetailItemService.saveBatch(progProjectDetail.getProgDetailItemList());
//
//            for (ProgDetailItem progDetailItemBig : progProjectDetail.getProgDetailItemList()) {
//                final List<ProgDetailItem> unitItemBigList = childDetailList.stream().map(childDetail -> {
//                    ProgDetailItem childItem = new ProgDetailItem();
//                    BeanUtils.copyProperties(progDetailItemBig, childItem);
//                    childItem.setId(null);
//                    childItem.setCommonId(progDetailItemBig.getId());
//                    childItem.setProgCommonId(childDetail.getProgCommonId());
//                    childItem.setProgProjectDetailId(childDetail.getId());
//                    return childItem;
//                }).collect(Collectors.toList());
//                progDetailItemService.saveBatch(unitItemBigList);
//                //新增子项
//                if (!progDetailItemBig.getChildItemList().isEmpty()) {
//                    saveChildItem(progDetailItemBig, unitItemBigList);
//                }
//            }
        }
    }

    /**
     * 复制子item
     *
     * @param progDetailItemBig
     * @param unitItemBigList
     * @param
     */
    private void saveChildItem(ProgDetailItem progDetailItemBig, List<ProgDetailItem> unitItemBigList) {
        progDetailItemBig.getChildItemList().stream().forEach(item -> {
            item.setParentId(progDetailItemBig.getId());
            item.setProgCommonId(progDetailItemBig.getProgCommonId());
            item.setProgProjectDetailId(progDetailItemBig.getProgProjectDetailId());
        });
        progDetailItemService.saveBatch(progDetailItemBig.getChildItemList());
        List<ProgDetailItem> list = new ArrayList<>();
        for (ProgDetailItem detailItem : progDetailItemBig.getChildItemList()) {
            List<ProgDetailItem> itemList = unitItemBigList.stream().map(item -> {
                ProgDetailItem childItem = new ProgDetailItem();
                BeanUtils.copyProperties(detailItem, childItem);
                childItem.setId(null);
                childItem.setCommonId(item.getId());
                childItem.setProgCommonId(item.getProgCommonId());
                childItem.setProgProjectDetailId(item.getId());
                return childItem;
            }).collect(Collectors.toList());
            list.addAll(itemList);
        }
        progDetailItemService.saveBatch(list);
    }

    /**
     * 方案处理
     *
     * @param programmePublishDTO
     * @param
     */
    private void publishPlan(ProgrammePublishDTO programmePublishDTO, Long programmeId) {
        //创建各种类型集合
        List<ProgProject> delList = new ArrayList<>();
        List<ProgProject> addList = new ArrayList<>();
        List<ProgProject> editList = new ArrayList<>();
        List<ProgProjectDetail> delDetailList = new ArrayList<>();
        List<ProgProjectDetail> addDetailList = new ArrayList<>();
        List<ProgProjectDetail> editDetailList = new ArrayList<>();
        List<ProgDetailItem> delDetailItemList = new ArrayList<>();
        List<ProgDetailItem> addDetailItemList = new ArrayList<>();
        List<ProgDetailItem> editDetailItemList = new ArrayList<>();
        List<ProgDetailItem> delChildDetailItemList = new ArrayList<>();
        List<ProgDetailItem> addChildDetailItemList = new ArrayList<>();
        List<ProgDetailItem> editChildDetailItemList = new ArrayList<>();
        //处理project,封装三种集合
        publishProgProject(programmePublishDTO, delList, addList, editList, delDetailList, addDetailList, editDetailList,delDetailItemList,addDetailItemList,editDetailItemList,delChildDetailItemList,addChildDetailItemList,editChildDetailItemList, programmeId);
        //处理project删除
        delProgProject(delList, delDetailList);
        //处理project新增
        addProgProject(addList, addDetailList);
        //处理project修改
        editProgProject(editList, addDetailList, delDetailList, editDetailList);
        //处理Detail删除
        delDetail(delDetailList, delDetailItemList);
        //处理Detail新增
        addDetail(addDetailList, addDetailItemList);
        //处理Detail修改
        editDetail(editDetailList, addDetailItemList, delDetailItemList, editDetailItemList);
        //处理ProgDetailItemBig
        saveOrdelOrEditItem(addDetailItemList, delDetailItemList, editDetailItemList,addChildDetailItemList, delChildDetailItemList, editChildDetailItemList);
        //处理ProgDetailItemChild
        saveOrdelOrEditItemChild(addChildDetailItemList, delChildDetailItemList, editChildDetailItemList);

        //同步分配任务
        Programme programme = this.getById(programmeId);
        unitTaskService.syncByProgramme(programme);
    }

    private void saveOrdelOrEditItemChild(List<ProgDetailItem> addChildDetailItemList, List<ProgDetailItem> delChildDetailItemList, List<ProgDetailItem> editChildDetailItemList) {
        if (!addChildDetailItemList.isEmpty()) {
            addChildDetailItemList.forEach(item -> item.setId(null));
            progDetailItemService.saveBatch(addChildDetailItemList);
        }
        if (!delChildDetailItemList.isEmpty()) {
            progDetailItemService.removeBatchByIds(delChildDetailItemList);
        }
        if (!editChildDetailItemList.isEmpty()) {
            progDetailItemService.updateBatchById(editChildDetailItemList);
        }
    }

    /**
     * 处理ProgDetailItem
     *
     * @param addDetailItemList
     * @param delDetailItemList
     * @param editDetailItemList
     * @param addChildDetailItemList
     * @param delChildDetailItemList
     * @param editChildDetailItemList
     */
    private void saveOrdelOrEditItem(List<ProgDetailItem> addDetailItemList, List<ProgDetailItem> delDetailItemList, List<ProgDetailItem> editDetailItemList, List<ProgDetailItem> addChildDetailItemList, List<ProgDetailItem> delChildDetailItemList, List<ProgDetailItem> editChildDetailItemList) {
        if (!addDetailItemList.isEmpty()) {
            Long max = this.getMaxSortNum(addDetailItemList);
            for (ProgDetailItem item : addDetailItemList){
                item.setId(null);
                if (item.getSortNum() == null){
                    item.setSortNum(max);
                    max = max + 1;
                }
            }
            progDetailItemService.saveBatch(addDetailItemList);

            addDetailItemList.stream()
                    .filter(Objects::nonNull) // 过滤掉为空的对象
                    .forEach(progDetailItem -> Optional.ofNullable(progDetailItem.getChildItemList())
                            .ifPresent(progDetailItemList -> {
                                progDetailItemList.forEach(progDetailItemChild -> {
                                    progDetailItemChild.setId(null);
                                    progDetailItemChild.setProgProjectDetailId(progDetailItem.getProgProjectDetailId());
                                    progDetailItemChild.setParentId(progDetailItem.getId());
                                    if (progDetailItemChild.getSortNum() == null){
                                        progDetailItemChild.setSortNum(1L);
                                    }
                                });
                                addChildDetailItemList.addAll(progDetailItemList);
                            }));
        }
        if (!delDetailItemList.isEmpty()) {
            progDetailItemService.removeBatchByIds(delDetailItemList);
            delDetailItemList.stream()
                    .map(ProgDetailItem::getChildItemList) // 获取每个对象的 getChildItemList
                    .filter(Objects::nonNull) // 过滤掉为空的列表
                    .flatMap(List::stream) // 扁平化处理，将多个列表合并为一个流
                    .forEach(delChildDetailItemList::add); // 将非空的列表元素添加到目标集合中
        }
        if (!editDetailItemList.isEmpty()) {
            progDetailItemService.updateBatchById(editDetailItemList);
        }
    }

    private Long getMaxSortNum(List<ProgDetailItem> addDetailItemList) {
        if (addDetailItemList == null) return 1L;
        Long progDetailId = addDetailItemList.get(0).getProgProjectDetailId();
        // 找到最大的sortnum+1
        List<ProgDetailItem> list = progDetailItemService.list(Wrappers.<ProgDetailItem>lambdaQuery()
                .eq(ProgDetailItem::getProgProjectDetailId, progDetailId)
                .isNull(ProgDetailItem::getParentId));
        Optional<Long> max = list.stream().map(ProgDetailItem::getSortNum).filter(Objects::nonNull).max(Long::compareTo);
        return max.map(aLong -> aLong + 1).orElse(1L);
    }

    /**
     * 修改ProgProjectDetail
     *
     * @param editDetailList
     * @param addDetailItemList
     * @param delDetailItemList
     * @param editDetailItemList
     */
    private void editDetail(List<ProgProjectDetail> editDetailList, List<ProgDetailItem> addDetailItemList, List<ProgDetailItem> delDetailItemList, List<ProgDetailItem> editDetailItemList) {
        if (!editDetailList.isEmpty()) {
            progProjectDetailService.updateBatchById(editDetailList);
            //editDetailList.forEach(progProjectDetail -> publishProgDetailItem(progProjectDetail, addDetailItemList, delDetailItemList, editDetailItemList));
        }
    }

    /**
     * 新增ProgProjectDetail
     *
     * @param addDetailList
     * @param addDetailItemList
     */
    private void addDetail(List<ProgProjectDetail> addDetailList, List<ProgDetailItem> addDetailItemList) {
        if (!addDetailList.isEmpty()) {
            addDetailList.forEach(item -> item.setId(null));
            progProjectDetailService.saveBatch(addDetailList);
            addDetailList.stream()
                    .filter(Objects::nonNull) // 过滤掉为空的对象
                    .forEach(progProjectDetail -> Optional.ofNullable(progProjectDetail.getProgDetailItemList())
                            .ifPresent(progDetailItemList -> {
                                progDetailItemList.forEach(progDetailItem -> progDetailItem.setProgProjectDetailId(progProjectDetail.getId()));
                                addDetailItemList.addAll(progDetailItemList);
                            }));
        }
    }

    /**
     * 删除ProgProjectDetail
     *
     * @param delDetailList
     * @param delDetailItemList
     */
    private void delDetail(List<ProgProjectDetail> delDetailList, List<ProgDetailItem> delDetailItemList) {
        if (!delDetailList.isEmpty()) {
            progProjectDetailService.removeBatchByIds(delDetailList);
            delDetailList.stream()
                    .map(ProgProjectDetail::getProgDetailItemList) // 获取每个对象的 progDetailItemList
                    .filter(Objects::nonNull) // 过滤掉为空的列表
                    .flatMap(List::stream) // 扁平化处理，将多个列表合并为一个流
                    .forEach(delDetailItemList::add); // 将非空的列表元素添加到目标集合中
        }
    }

    /**
     * 修改project
     *
     * @param editList
     * @param addDetailList
     * @param delDetailList
     * @param editDetailList
     */
    private void editProgProject(List<ProgProject> editList, List<ProgProjectDetail> addDetailList, List<ProgProjectDetail> delDetailList, List<ProgProjectDetail> editDetailList) {
        if (!editList.isEmpty()) {
            progProjectService.updateBatchById(editList);
            //editList.forEach(progProject -> publishProgProjectDetail(progProject, addDetailList, delDetailList, editDetailList, delDetailItemList, addDetailItemList, editDetailItemList, delChildDetailItemList, addChildDetailItemList, editChildDetailItemList));
        }
    }

    /**
     * 新增project
     *
     * @param addList
     * @param addDetailList
     */
    private void addProgProject(List<ProgProject> addList, List<ProgProjectDetail> addDetailList) {
        if (!addList.isEmpty()) {
            addList.forEach(item -> item.setId(null));
            progProjectService.saveBatch(addList);
            addList.forEach(progProject -> {
                progProject.getProgProjectDetailList().forEach(progProjectDetail -> progProjectDetail.setProgProjectId(progProject.getId()));
                addDetailList.addAll(progProject.getProgProjectDetailList());
            });
        }
    }

    /**
     * 删除project
     *
     * @param delList
     * @param delDetailList
     */
    private void delProgProject(List<ProgProject> delList, List<ProgProjectDetail> delDetailList) {
        if (!delList.isEmpty()) {
            progProjectService.removeBatchByIds(delList);
            //处理ProgProjectDetail
            delList.forEach(progProject -> {
                delDetailList.addAll(progProject.getProgProjectDetailList());
            });
        }
    }

    /**
     * 处理project
     *
     * @param programmePublishDTO
     * @param delList
     * @param addList
     * @param editList
     * @param delDetailList
     * @param addDetailList
     * @param editDetailList
     * @param delDetailItemList
     * @param addDetailItemList
     * @param editDetailItemList
     * @param delChildDetailItemList
     * @param addChildDetailItemList
     * @param editChildDetailItemList
     */
    private void publishProgProject(ProgrammePublishDTO programmePublishDTO, List<ProgProject> delList, List<ProgProject> addList, List<ProgProject> editList, List<ProgProjectDetail> delDetailList, List<ProgProjectDetail> addDetailList, List<ProgProjectDetail> editDetailList, List<ProgDetailItem> delDetailItemList, List<ProgDetailItem> addDetailItemList, List<ProgDetailItem> editDetailItemList, List<ProgDetailItem> delChildDetailItemList, List<ProgDetailItem> addChildDetailItemList, List<ProgDetailItem> editChildDetailItemList, Long programmeId) {
        //处理project
        for (int i = 0; i < programmePublishDTO.getProjectList().size(); i++) {
            ProgProject progProject = programmePublishDTO.getProjectList().get(i);
            progProject.setSortNum(i + 1);
            progProject.setProgrammeId(Long.valueOf(programmeId));
            //因为有排序，所以没标记的project都标记为edit 做排序修改
            String actionType = progProject.getActionType();
            actionType = actionType == null?ActionType.edit.toString():actionType;
            //新增的项目
            if (ActionType.add.toString().equals(actionType)) {
                addList.add(progProject);
            } else if (ActionType.edit.toString().equals(actionType)) {//修改的项目
                editList.add(progProject);
                publishProgProjectDetail(progProject, addDetailList, delDetailList, editDetailList,delDetailItemList, addDetailItemList,editDetailItemList, delChildDetailItemList, addChildDetailItemList,  editChildDetailItemList);
            } else if (ActionType.del.toString().equals(actionType)) {//删除的项目
                delList.add(progProject);
            } else {
                publishProgProjectDetail(progProject, addDetailList, delDetailList, editDetailList,delDetailItemList, addDetailItemList,editDetailItemList, delChildDetailItemList, addChildDetailItemList,  editChildDetailItemList);
            }
        }
    }

    /**
     * 处理ProgDetailItem
     *
     * @param progProjectDetail
     * @param addDetailItemList
     * @param delDetailItemList
     * @param editDetailItemList
     */
    private void publishProgDetailItem(ProgProjectDetail progProjectDetail, List<ProgDetailItem> addDetailItemList, List<ProgDetailItem> delDetailItemList, List<ProgDetailItem> editDetailItemList) {
        if (!progProjectDetail.getProgDetailItemList().isEmpty()) {
            for (ProgDetailItem progDetailItem : progProjectDetail.getProgDetailItemList()) {
                String actionType = progDetailItem.getActionType();
                //新增的项目
                if (ActionType.add.toString().equals(actionType)) {
                    addDetailItemList.add(progDetailItem);
                } else if (ActionType.edit.toString().equals(actionType)) {//修改的项目
                    editDetailItemList.add(progDetailItem);
                } else if (ActionType.del.toString().equals(actionType)) {//删除的项目
                    delDetailItemList.add(progDetailItem);
                }
            }
        }
    }

    /**
     * 处理ProgProjectDetail
     *
     * @param progProject
     * @param addDetailList
     * @param delDetailList
     * @param editDetailList
     * @param delDetailItemList
     * @param addDetailItemList
     * @param editDetailItemList
     * @param delChildDetailItemList
     * @param addChildDetailItemList
     * @param editChildDetailItemList
     */
    private void publishProgProjectDetail(ProgProject progProject, List<ProgProjectDetail> addDetailList, List<ProgProjectDetail> delDetailList, List<ProgProjectDetail> editDetailList, List<ProgDetailItem> delDetailItemList, List<ProgDetailItem> addDetailItemList, List<ProgDetailItem> editDetailItemList, List<ProgDetailItem> delChildDetailItemList, List<ProgDetailItem> addChildDetailItemList, List<ProgDetailItem> editChildDetailItemList) {
        //处理detail
        for (ProgProjectDetail progProjectDetail : progProject.getProgProjectDetailList()) {
            String actionType = progProjectDetail.getActionType();
            actionType = actionType == null ? ActionType.edit.toString() : actionType;
            //新增的项目
            if (ActionType.add.toString().equals(actionType)) {
                progProjectDetail.setId(null);
                progProjectDetail.setProgProjectId(progProject.getId());
                addDetailList.add(progProjectDetail);
            } else if (ActionType.edit.toString().equals(actionType)) {//修改的项目
                editDetailList.add(progProjectDetail);
                publishProgDetailItemBig(progProjectDetail,delDetailItemList, addDetailItemList,editDetailItemList, delChildDetailItemList, addChildDetailItemList,  editChildDetailItemList);
            } else if (ActionType.del.toString().equals(actionType)) {//删除的项目
                delDetailList.add(progProjectDetail);
            }else{
                publishProgDetailItemBig(progProjectDetail,delDetailItemList, addDetailItemList,editDetailItemList, delChildDetailItemList, addChildDetailItemList,  editChildDetailItemList);
            }
        }


    }

    private void publishProgDetailItemBig(ProgProjectDetail progProjectDetail,List<ProgDetailItem> delDetailItemList, List<ProgDetailItem> addDetailItemList, List<ProgDetailItem> editDetailItemList, List<ProgDetailItem> delChildDetailItemList, List<ProgDetailItem> addChildDetailItemList, List<ProgDetailItem> editChildDetailItemList) {
        //处理大项
        for (ProgDetailItem itemBig : progProjectDetail.getProgDetailItemList()) {
            String actionType = itemBig.getActionType();
            actionType = actionType == null ? ActionType.edit.toString() : actionType;
            //新增的 大项
            if (ActionType.add.toString().equals(actionType)) {
                itemBig.setId(null);
                itemBig.setProgProjectDetailId(progProjectDetail.getId());
                addDetailItemList.add(itemBig);
            } else if (ActionType.edit.toString().equals(actionType)) {//修改的项目
                editDetailItemList.add(itemBig);
                publishProgDetailItemChild(itemBig,delChildDetailItemList, addChildDetailItemList, editChildDetailItemList);
            } else if (ActionType.del.toString().equals(actionType)) {//删除的项目
                delDetailItemList.add(itemBig);
            } else {
                publishProgDetailItemChild(itemBig,delChildDetailItemList, addChildDetailItemList, editChildDetailItemList);
            }
        }
    }

    private void publishProgDetailItemChild(ProgDetailItem itemBig, List<ProgDetailItem> delChildDetailItemList, List<ProgDetailItem> addChildDetailItemList, List<ProgDetailItem> editChildDetailItemList) {
        for (ProgDetailItem itemChild : itemBig.getChildItemList()) {
            String actionType = itemChild.getActionType();
            actionType = actionType == null ? ActionType.edit.toString() : actionType;
            //新增的 子项
            if (ActionType.add.toString().equals(actionType)) {
                itemChild.setParentId(itemBig.getId());
                itemChild.setProgProjectDetailId(itemBig.getProgProjectDetailId());
                addChildDetailItemList.add(itemChild);
            } else if (ActionType.edit.toString().equals(actionType)) {//修改的项目
                editChildDetailItemList.add(itemChild);
            } else if (ActionType.del.toString().equals(actionType)) {//删除的项目
                delChildDetailItemList.add(itemChild);
            }
        }
    }


    @Override
    @Transactional
    public void copy(Long programmeId) {

    }

    /**
     * 方案详情
     *
     * @param id
     * @return
     */
    @Override
    public ProgrammeInfoVo getProgrammeInfo(Long id) {
        ProgrammeInfoVo vo = new ProgrammeInfoVo();
        //查询方案基础信息
        Programme programme = new Programme().selectById(id);
        BeanUtils.copyProperties(programme, vo);
        //查询perject
        List<ProgProject> progProjects = new ProgProject().selectList(new LambdaQueryWrapper<ProgProject>()
                .eq(ProgProject::getProgrammeId, id)
                .orderByAsc(ProgProject::getSortNum));
        //遍历progProjects,查询ProgProjectDetail
        progProjects.stream().forEach(progProject -> {
            List<ProgProjectDetail> progProjectDetails = new ProgProjectDetail().selectList(new LambdaQueryWrapper<ProgProjectDetail>()
                    .eq(ProgProjectDetail::getProgProjectId, progProject.getId()).orderByAsc(ProgProjectDetail::getSortNum));
            //遍历progProjects,查询ProgDetailItem
            progProjectDetails.stream().forEach(progProjectDetail -> {
                List<ProgDetailItem> progDetailItems = new ProgDetailItem().selectList(new LambdaQueryWrapper<ProgDetailItem>()
                        .eq(ProgDetailItem::getProgProjectDetailId, progProjectDetail.getId())
                        .isNull(ProgDetailItem::getParentId).orderByAsc(ProgDetailItem::getSortNum));
                //遍历progDetailItems,查询子ProgDetailItem
                progDetailItems.stream().forEach(progDetailItem -> {
                    List<ProgDetailItem> childItemList = new ProgDetailItem().selectList(new LambdaQueryWrapper<ProgDetailItem>()
                            .eq(ProgDetailItem::getProgProjectDetailId, progProjectDetail.getId())
                            .eq(ProgDetailItem::getParentId, progDetailItem.getId()).orderByAsc(ProgDetailItem::getSortNum));
                    progDetailItem.setChildItemList(childItemList);
                });
                progProjectDetail.setProgDetailItemList(progDetailItems);
            });
            progProject.setProgProjectDetailList(progProjectDetails);
        });
        vo.setProjectList(progProjects);
        return vo;
    }

    @Override
    public Boolean validGrantUnit(Programme programme) {
        if(!"1".equals(programme.getIfCommon())){
            return true;
        }
        if(programme.getGrantUnitIds() == null){
            return true;
        }
        List<Long> unitIds = CommonUtils.longs2List(programme.getGrantUnitIds());
        for (Long unitId : unitIds){
            Boolean exists = this.exists(Wrappers.<Programme>lambdaQuery()
                    .eq(Programme::getStatus,"0")
                    .ne(programme.getId() != null, Programme::getId,programme.getId())
                    .eq(Programme::getIfCommon,"1")
                    .like(Programme::getGrantUnitIds,unitId));
            if(exists){
                GrantUnit grantUnit = grantUnitService.getById(unitId);
                throw new BizException(grantUnit.getName()+" 存在开启的方案");
            }
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncByProgCommon(Programme progCommon) {
        Long progCommonId = progCommon.getId();
        //该公共方案下的 所有发放单元方案
        List<Programme> unitProgrammeList = this.listByProgCommonId(progCommonId);
        Map<Long,Programme> unitProgrammeMap = unitProgrammeList.stream().collect(Collectors.toMap(Programme::getGrantUnitId, item->item, (v1, v2) -> v2));

        String[] grantUnitIds = progCommon.getGrantUnitIds().split(",");
        for (String unitIdStr : grantUnitIds){
            Long unitId = Long.parseLong(unitIdStr);
            Programme programme = unitProgrammeMap.get(unitId);
            if(programme == null){
                programme = new Programme();
                programme.setName(progCommon.getName());
                programme.setIfCommon("0");
                programme.setParentId(progCommonId);
                programme.setIfContainLeader(progCommon.getIfContainLeader());
                programme.setGrantUnitId(unitId);
                programme.setStatus("0");
                this.save(programme);
                unitProgrammeList.add(programme);
            }else{
                if(!Objects.equals(programme.getIfContainLeader(), progCommon.getIfContainLeader())) {
                    programme.setIfContainLeader(progCommon.getIfContainLeader());
                    this.updateById(programme);
                }
            }
        }

        //unitProgrammeList.forEach(progProjectService::syncByProgramme);

        //删除 剔除的发放单元对应的方案
        List<Programme> delList = unitProgrammeList.stream()
                .filter(programme -> !inIds(programme.getGrantUnitId(),grantUnitIds))
                .collect(Collectors.toList());

        this.delByProgrammeId(delList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startStatus(Programme programme, String status) {
        this.updateById(programme);
        this.update(null,Wrappers.<Programme>lambdaUpdate()
                .eq(Programme::getParentId,programme.getId())
                .set(Programme::getStatus,status));
        if(Objects.equals(status,"0")) {
            //启用方案时，找到所有子方案
            List<Programme> programmeList = this.list(Wrappers.<Programme>lambdaQuery()
                    .eq(Programme::getParentId, programme.getId()));
            //找到各个子方案对应的发放单元
            for (Programme progChild : programmeList) {
                unitTaskService.syncByProgramme(progChild);
//                Long grantUnitId = progChild.getGrantUnitId();
//                //找到发放单元是否有进行中的任务
//                if (grantUnitId != null) {
//                    List<UnitTask> unitTaskList = unitTaskService.list(Wrappers.<UnitTask>lambdaQuery()
//                            .eq(UnitTask::getGrantUnitId, grantUnitId)
//                            .in(UnitTask::getStatus, Arrays.asList(SecondDistributionTaskStatusEnum.UNCOMMITTED, SecondDistributionTaskStatusEnum.APPROVAL_REJECTED)));
//                    //将这些任务的方案换成 新启用的方案
//                    unitTaskList.forEach(unitTask -> {
//                        unitTask.setProgrammeId(progChild.getId());
//                    });
//                    unitTaskService.updateBatchById(unitTaskList);
//                    //同步任务
//                    unitTaskList.forEach(unitTaskProjectService::syncByUnitTask);
//                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        Programme programme = this.getById(id);
        if(Objects.equals(programme.getIfCommon(),"1")){
            //删除发放单元方案
            List<Programme> unitProgrammeList = this.listByProgCommonId(id);
            unitProgrammeList.forEach(item-> this.removeById(item.getId()));
        }
        //删除公共方案
        this.removeById(id);

    }

    private boolean inIds(Long grantUnitId, String[] grantUnitIds) {
        for (String s : grantUnitIds){
            if(s.equals(grantUnitId+"")){
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
    }

    private List<Programme> listByProgCommonId(Long progCommonId) {
       return this.list(Wrappers.<Programme>lambdaQuery()
                .eq(Programme::getParentId,progCommonId));
    }

    private void delByProgrammeId(List<Programme> delList) {
        if(delList == null || delList.isEmpty()){
            return;
        }
        List<Long> delIds = delList.stream().map(Programme::getId).collect(Collectors.toList());
        this.remove(Wrappers.<Programme>lambdaQuery().in(Programme::getId,delIds));

        List<ProgProject> progProjectList = progProjectService.list(Wrappers.<ProgProject>lambdaQuery()
                .in(ProgProject::getProgrammeId,delIds));
        progProjectService.delByProjectList(progProjectList);
    }
}
