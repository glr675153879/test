package com.hscloud.hs.cost.account.controller.second;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.constant.Constant;
import com.hscloud.hs.cost.account.constant.DeptOrUserConstant;
import com.hscloud.hs.cost.account.constant.enums.report.OperatorEnum;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountUnitInfo;
import com.hscloud.hs.cost.account.model.dto.report.CustomParamDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import com.hscloud.hs.cost.account.model.entity.second.GrantUnit;
import com.hscloud.hs.cost.account.model.entity.second.GrantUnitLog;
import com.hscloud.hs.cost.account.model.entity.second.Programme;
import com.hscloud.hs.cost.account.model.vo.second.GrantUnitMineVo;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiAccountUnitService;
import com.hscloud.hs.cost.account.service.second.IGrantUnitLogService;
import com.hscloud.hs.cost.account.service.second.IGrantUnitService;
import com.hscloud.hs.cost.account.service.second.IProgrammeService;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.feign.RemoteDictService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
* 发放单元
*
*/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/grantUnit")
@Tag(name = "grantUnit", description = "发放单元")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class GrantUnitController {

    private final IGrantUnitService grantUnitService;
    private final IGrantUnitLogService grantUnitLogService;
    private final IProgrammeService programmeService;
    @Autowired
    private KpiAccountUnitService kpiAccountUnitService;
    @Autowired
    private RemoteDictService remoteDictService;

    @SysLog("发放单元info")
    @GetMapping("/info/{id}")
    @Operation(summary = "发放单元info")
    public R<GrantUnit> info(@PathVariable Long id) {
        return R.ok(grantUnitService.getById(id));
    }

    @SysLog("发放单元page")
    @GetMapping("/page")
    @Operation(summary = "发放单元page")
    public R<IPage<GrantUnit>> page(Long id, String leaderUser, String name, String ksUnitNames, String status, Page<GrantUnit> pr) {
        List<KpiAccountUnit> list = kpiAccountUnitService.list();
        //核算分组
        List<SysDictItem> hsfz = remoteDictService.getDictByType("kpi_calculate_grouping").getData();
        LambdaQueryWrapper<GrantUnit> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null, GrantUnit::getName, name)
                .eq(status != null, GrantUnit::getStatus, status)
                .eq(id != null, GrantUnit::getId, id);
        if(ksUnitNames != null){
            wrapper.and(w -> {
                w.like(ksUnitNames != null, GrantUnit::getKsUnitNames, ksUnitNames)
                        .or()
                        .like(StrUtil.isNotBlank(ksUnitNames), GrantUnit::getKsUnitNamesNonStaff, ksUnitNames);
            });
        }
        //加入人员选择
        if(leaderUser != null){
            String[] leaderIds = leaderUser.split(",");
            wrapper.and(w ->{
                for (String leaderId : leaderIds){
                    w.like(GrantUnit::getLeaderIds,leaderId).or();
                }
                w.like(GrantUnit::getLeaderIds,"-1");
            });
        }

        IPage<GrantUnit> page = grantUnitService.page(pr,wrapper);
        //负责人组装
        for (GrantUnit grantUnit : page.getRecords()) {
            grantUnit.setDepts(new ArrayList<>());
            //实时取科室
            List<String> deptIds = new ArrayList<>();
            if (StringUtils.isNotBlank(grantUnit.getKsUnitIds())) {
                deptIds.addAll(Arrays.asList(grantUnit.getKsUnitIds().split(",")));
            }
            if (StringUtils.isNotBlank(grantUnit.getKsUnitIdsNonStaff())) {
                deptIds.addAll(Arrays.asList(grantUnit.getKsUnitIdsNonStaff().split(",")));
            }
            if (!deptIds.isEmpty()) {
                for (String unitId : deptIds) {
                    KpiAccountUnitInfo info = new KpiAccountUnitInfo();
                    info.setId(Long.valueOf(unitId));
                    KpiAccountUnit accountUnit = list.stream().filter(o -> o.getId().toString().equals(unitId)).findFirst().orElse(null);
                    if (accountUnit != null) {
                        info.setName(accountUnit.getName());
                        info.setDelFlag(accountUnit.getDelFlag());
                        info.setStatus(accountUnit.getStatus());
                        SysDictItem sysDictItem = hsfz.stream().filter(o -> o.getItemValue().equals(accountUnit.getCategoryCode())).findFirst().orElse(null);
                        if (sysDictItem != null) {
                            info.setCategoryName(sysDictItem.getLabel());
                        }
                    }
                    grantUnit.getDepts().add(info);
                }
            }
            String ids = grantUnit.getLeaderIds();
            String names = grantUnit.getLeaderNames();
            if(ids == null || names == null){
                continue;
            }
            grantUnit.setLeaderUser(CommonUtils.getUserObj(ids, names, DeptOrUserConstant.USER_LIST));
            if(StrUtil.isBlank(grantUnit.getKsUnitIds())){
                grantUnit.setKsUnitIds(null);
                grantUnit.setKsUnitNames(null);
            }
            if(StrUtil.isBlank(grantUnit.getKsUnitIdsNonStaff())){
                grantUnit.setKsUnitIdsNonStaff(null);
                grantUnit.setKsUnitNamesNonStaff(null);
            }
        }
        return R.ok(page);
    }

    @SysLog("发放单元list")
    @GetMapping("/list")
    @Operation(summary = "发放单元list")
    public R<List<GrantUnit>> list(PageRequest<GrantUnit> pr) {
        return R.ok(grantUnitService.list(pr.getWrapper()));
    }

    @SysLog("登录人 的 发放单元")
    @GetMapping("/mine")
    @Operation(summary = "登录人 的 发放单元")
    public R<List<GrantUnitMineVo>> mine() {
        Long curentUserId = SecurityUtils.getUser().getId();
        List<GrantUnit> list = grantUnitService.list(Wrappers.<GrantUnit>lambdaQuery()
                .eq(GrantUnit::getStatus,"0").like(GrantUnit::getLeaderIds,","+curentUserId+","));
        List<GrantUnitMineVo> voList = new ArrayList<>();
        for (GrantUnit grantUnit : list){
            GrantUnitMineVo vo = new GrantUnitMineVo();
            BeanUtil.copyProperties(grantUnit, vo);
            try{
                Programme programme = programmeService.getByUnitId(grantUnit.getId());
                vo.setProgrammeId(programme.getId());
                voList.add(vo);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        return R.ok(voList);
    }

    @SysLog("我负责的发放单元(用于动态报表)")
    @GetMapping("/mineGrantUnit4ReportData")
    @Operation(summary = "我负责的发放单元(用于动态报表)")
    public R<CustomParamDto> mineGrantUnit4ReportData() {
        Long currentUserId = SecurityUtils.getUser().getId();
        Set<String> result = grantUnitService.managerUnits(currentUserId);
        CustomParamDto customParamDto = new CustomParamDto();
        customParamDto.setOperator(OperatorEnum.IN.getOperator());
        customParamDto.setCode(Constant.ACCOUNT_UNIT_ID);
        customParamDto.setValue(result);
        return R.ok(customParamDto);
    }

    @SysLog("发放单元初始化init")
    @PostMapping("/init")
    @Operation(summary = "发放单元初始化init")
    public R init()  {
        grantUnitService.init();
        return R.ok();
    }

    @SysLog("发放单元 是否已经初始化")
    @GetMapping("/ifInit")
    @Operation(summary = "发放单元 是否已经初始化")
    public R<Boolean> ifInit() {
        return R.ok(grantUnitService.ifInit());
    }

    @SysLog("发放单元add")
    @PostMapping("/add")
    @Operation(summary = "发放单元add")
    public R add(@RequestBody GrantUnit grantUnit)  {
        //检查发放单元名称不能重复
        this.validName(grantUnit);
        //检查发放单元下的科室不能重复
        this.validKsUnitIds(grantUnit);
        //校验对应科室单元（不含编外）和对应科室单元（含编外）
        this.validUnitIdsNonStaff(grantUnit);

        Map<String,Object> leaderUser= grantUnit.getLeaderUser();
        grantUnit.setLeaderIds(","+CommonUtils.getValueFromUserObj(leaderUser,"id", DeptOrUserConstant.USER_LIST)+",");
        grantUnit.setLeaderNames(CommonUtils.getValueFromUserObj(leaderUser,"name", DeptOrUserConstant.USER_LIST));
        grantUnit.setStatus("0");
        grantUnitService.save(grantUnit);
        //日志
        Boolean ifInit = grantUnitService.ifInit();
        if(ifInit){
            GrantUnitLog grantUnitLog = new GrantUnitLog();
            grantUnitLog.setGrantUnitId(grantUnit.getId());
            grantUnitLog.setContent("新增发放单元："+grantUnit.getName());
            grantUnitLogService.save(grantUnitLog);
        }
        return R.ok();
    }

    private void validKsUnitIds(GrantUnit grantUnit) {
        if(StrUtil.isBlank(grantUnit.getKsUnitIds())){
            return;
        }
        //获取 ksUnitId的list
        List<String> ksUnitIds = Arrays.stream(grantUnit.getKsUnitIds().split(",")).collect(Collectors.toList());
        ksUnitIds.forEach(ksUnitId -> {
            if(grantUnitService.exists(Wrappers.<GrantUnit>lambdaQuery()
                    .eq(GrantUnit::getStatus,"0")
                    .like(GrantUnit::getKsUnitIds,ksUnitId)
                    .ne(grantUnit.getId() != null,GrantUnit::getId,grantUnit.getId())
            )){
                throw new BizException("该发放单元对应的科室单元与其他发放单元重复，请重新选择科室单元");
            }
        });
    }

    private void validName(GrantUnit grantUnit) {
        if(grantUnitService.exists(Wrappers.<GrantUnit>lambdaQuery()
                .eq(GrantUnit::getName,grantUnit.getName())
                .eq(GrantUnit::getStatus,"0")
                .ne(grantUnit.getId() != null,GrantUnit::getId,grantUnit.getId())
        )){
            throw new BizException("发放单元名称已存在，请重新编辑");
        }
    }

    /**
     * 校验对应科室单元（不含编外）和对应科室单元（含编外）
     *@param grantUnit
     */
    private void validUnitIdsNonStaff(GrantUnit grantUnit) {
        String ksUnitIds = grantUnit.getKsUnitIds();
        String ksUnitIdsNonStaff = grantUnit.getKsUnitIdsNonStaff();
        String ksUnitNamesStaff = grantUnit.getKsUnitNames();
        String ksUnitNamesNonStaff = grantUnit.getKsUnitNamesNonStaff();
        if (StrUtil.isNotBlank(ksUnitIds) && StrUtil.isNotBlank(ksUnitIdsNonStaff)) {
            String[] split = ksUnitIds.split(",");
            String[] splitNoStaff = ksUnitIdsNonStaff.split(",");
            String[] splitNames = ksUnitNamesStaff.split(",");
            String[] splitNamesNonStaff = ksUnitNamesNonStaff.split(",");

            StringBuilder duplicateUnits = new StringBuilder();

            Set<String> unitIdSet = new HashSet<>(Arrays.asList(split));

            for (int i = 0; i < splitNoStaff.length; i++) {
                if (unitIdSet.contains(splitNoStaff[i])) {
                    if (duplicateUnits.length() > 0) {
                        duplicateUnits.append(", ");
                    }
                    // 找到重复项对应的科室名称
                    String duplicateName = findUnitName(split, splitNames, splitNoStaff[i], splitNamesNonStaff[i]);
                    duplicateUnits.append(duplicateName);
                }
            }

            if (duplicateUnits.length() > 0) {
                throw new BizException("重复的科室信息: " + duplicateUnits);
            }
        }
    }

    private String findUnitName(String[] split, String[] splitNames, String duplicateId, String defaultName) {
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals(duplicateId)) {
                return splitNames[i];
            }
        }
        return defaultName;
    }

    @SysLog("发放单元edit")
    @PostMapping("/edit")
    @Operation(summary = "发放单元edit")
    public R edit(@RequestBody GrantUnit grantUnit)  {
        GrantUnit grantUnitDB = grantUnitService.getById(grantUnit.getId());
        Map<String,Object> leaderUser= grantUnit.getLeaderUser();
        if(leaderUser != null){
            grantUnit.setLeaderIds(","+CommonUtils.getValueFromUserObj(leaderUser,"id", DeptOrUserConstant.USER_LIST)+",");
            grantUnit.setLeaderNames(CommonUtils.getValueFromUserObj(leaderUser,"name", DeptOrUserConstant.USER_LIST));
        }
        //校验科室单元
        this.validUnitIdsNonStaff(grantUnit);
        if(grantUnitDB.getStatus().equals("1") && grantUnit.getStatus().equals("0")){//启用
            //检查发放单元名称不能重复
            this.validName(grantUnitDB);
            //检查发放单元下的科室不能重复
            this.validKsUnitIds(grantUnitDB);
            grantUnitService.updateById(grantUnit);
        }else if(grantUnitDB.getStatus().equals("0") && grantUnit.getStatus().equals("1")){//禁用
            grantUnitService.updateById(grantUnit);
        }else{
            //检查发放单元名称不能重复
            this.validName(grantUnit);
            //检查发放单元下的科室不能重复
            this.validKsUnitIds(grantUnit);
            grantUnitService.updateById(grantUnit);
        }

        //日志
        grantUnitService.updateLog(grantUnitDB,grantUnit);

        return R.ok(grantUnitService.updateById(grantUnit));
    }

    @SysLog("发放单元del")
    @PostMapping("/del/{id}")
    @Operation(summary = "发放单元del")
    public R del(@PathVariable Long id)  {
        return R.ok(grantUnitService.removeById(id));
    }

    @SysLog("发放单元delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "发放单元delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(grantUnitService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

}