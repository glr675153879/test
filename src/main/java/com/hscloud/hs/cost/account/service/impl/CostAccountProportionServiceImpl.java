package com.hscloud.hs.cost.account.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.CostAccountProportionType;
import com.hscloud.hs.cost.account.mapper.CostAccountProportionMapper;
import com.hscloud.hs.cost.account.model.dto.*;
import com.hscloud.hs.cost.account.model.entity.*;
import com.hscloud.hs.cost.account.model.pojo.AccountUnitInfo;
import com.hscloud.hs.cost.account.model.pojo.DeptInfo;
import com.hscloud.hs.cost.account.model.pojo.UserInfo;
import com.hscloud.hs.cost.account.model.vo.*;
import com.hscloud.hs.cost.account.service.*;
import com.pig4cloud.pigx.admin.api.entity.SysDept;
import com.pig4cloud.pigx.admin.api.feign.RemoteDeptService;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.admin.api.vo.UserVO;
import com.pig4cloud.pigx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 核算比例service接口实现类
 *
 * @author banana
 * @create 2023-09-13 15:12
 */

@Service
public class CostAccountProportionServiceImpl extends ServiceImpl<CostAccountProportionMapper, CostAccountProportion> implements ICostAccountProportionService {

    @Autowired
    private CostAccountUnitService costAccountUnitService;

    @Autowired
    private ICostAccountProportionRelationService iCostAccountProportionRelationService;

    @Autowired
    private CostAccountItemService costAccountItemService;

    @Autowired
    private ICostBaseGroupService iCostBaseGroupService;

    @Autowired
    private RemoteDeptService remoteDeptService;

    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
    private ICostDocNRelationService iCostDocNRelationService;


    /**
     * 新增核算比例项
     *
     * @param input 入参
     */
    @Override
    public CostAccountListVo CostAccountProportionAdd(CostAccountProportionAddDto input) {

        //保存核算比例信息
        CostAccountProportion costAccountProportion = new CostAccountProportion();
        BeanUtils.copyProperties(input, costAccountProportion);
        //设置核算项名称
        CostAccountItem costAccountItem = costAccountItemService.getById(input.getCostAccountItemId());
        if (costAccountItem == null) throw new BizException("当前核算项不存在");
        costAccountProportion.setCostAccountItem(costAccountItem.getAccountItemName());
        //设置分组名称
        CostBaseGroup costBaseGroup = iCostBaseGroupService.getById(input.getTypeGroupId());
        if (costBaseGroup == null) throw new BizException("当前分组不存在");
        costAccountProportion.setTypeGroup(costBaseGroup.getName());

        //获得核算范围信息（字典对象）
        DictItemVo docDictItem = JSON.parseObject(input.getAccountObject(), DictItemVo.class);

        //核算比例项不重复配置(针对固定科室单元)
        if ("GROUP".equals(getType(docDictItem.getValue())) && !"KSDYFW007".equals(docDictItem.getValue())) {
            Long count = baseMapper.selectCount(new LambdaQueryWrapper<CostAccountProportion>()
                    .eq(CostAccountProportion::getCostAccountItemId, costAccountProportion.getCostAccountItemId())
                    .eq(CostAccountProportion::getTypeGroupId, costAccountProportion.getTypeGroupId())
                    .eq(CostAccountProportion::getAccountObject, costAccountProportion.getAccountObject()));
            if (count != 0) {
                throw new BizException("已经进行了该核算比例项的配置");
            }
        }
        baseMapper.insert(costAccountProportion);

        //保存核算比例关联信息
        switch (docDictItem.getValue()) {
            //TODO 可以分成三类 三个处理Handler，但内部还是要进行判断，其实差不多
            case "KSDYFW003":
                //医生组科室单元
                BindDocGroup(costAccountProportion);
                break;
            case "KSDYFW004":
                //护理组科室单元
                BindNurseGroup(costAccountProportion);
                break;
            case "KSDYFW005":
                //医技组科室单元
                BindMedicalTechnologyGroup(costAccountProportion);
                break;
            case "KSDYFW011":
                //药剂组科室单元
                BindReagentGroup(costAccountProportion);
                break;
            case "KSDYFW007":
                //自定义科室单元
                BindCustomGroup(costAccountProportion, input.getCustomInput());
                break;
            case "KSDYFW008":
                //自定义科室
                BindCustomDepartment(costAccountProportion, input.getCustomInput());
                break;
            case "KSDYFW009":
                //自定义人员
                BindUser(costAccountProportion, input.getCustomInput());
                break;
            case "KSDYFW010":
                //医护对应科室单元组
                BindDocNurse(costAccountProportion);
                break;
            default:
                break;
        }
        CostAccountListVo costAccountListVo = new CostAccountListVo();
        costAccountListVo.setId(costAccountProportion.getId());
        //获取核算范围名称 现在要求完整json格式返回
        //DictItemVo dictItemVo = JSON.parseObject(r.getAccountObject(), DictItemVo.class);
        //costAccountListVo.setAccountObject(dictItemVo.getLabel());
        costAccountListVo.setAccountObject(costAccountProportion.getAccountObject());
        costAccountListVo.setCostAccountItem(costAccountProportion.getCostAccountItem());
        //新添加的记录的状态默认为启用0
        costAccountListVo.setStatus("0");
        costAccountListVo.setTypeGroupName(costAccountProportion.getTypeGroup());
        return costAccountListVo;
    }

    //查询指定核算项的核算比例信息
    @Override
    public List<CostAccountProportionListVo> CostAccountProportionList(CostAccountProportionListDto input) {
        //声明出参
        List<CostAccountProportionListVo> ret = new ArrayList<>();

        //获取当前核算项信息
        CostAccountProportion costAccountProportion = baseMapper
                .selectOne(new LambdaQueryWrapper<CostAccountProportion>()
                        .eq(CostAccountProportion::getId, Long.parseLong(input.getId())));
        if (costAccountProportion == null) throw new BizException("当前核算项不存在");

        //获取当前核算项的核算范围
        String accountArrage = costAccountProportion.getAccountObject();

        //根据不同的核算范围 封装不同的出参
        //总共分成三类：
        // 1.科室单元: KSDYFW003/KSDYFW004/KSDYFW005/KSDYFW007/KSDYFW010
        // 2.科室: KSDYFW008
        // 3.人员: KSDYFW009
        if (accountArrage.contains("KSDYFW003")
                || accountArrage.contains("KSDYFW004")
                || accountArrage.contains("KSDYFW005")
                || accountArrage.contains("KSDYFW010")
                || accountArrage.contains("KSDYFW007")
                || accountArrage.contains("KSDYFW011")) {

            //获取当前核算项对应的比例关联信息
            LambdaQueryWrapper<CostAccountProportionRelation> lambdaQueryWrapper = new LambdaQueryWrapper<CostAccountProportionRelation>();
            lambdaQueryWrapper.eq(CostAccountProportionRelation::getCostAccountProportionId, input.getId());

            //科室单元
            if (StringUtils.isNotBlank(input.getAccountUnit())) lambdaQueryWrapper
                    .apply("JSON_EXTRACT(context, '$.accountUnit') LIKE CONCAT('%', {0}, '%')", input.getAccountUnit());
            //核算分组
            DictItemVo dictItemVo = JSON.parseObject(input.getTypeGroup(), DictItemVo.class);
            if (StringUtils.isNotBlank(input.getTypeGroup())) lambdaQueryWrapper
                    .apply("JSON_EXTRACT(context, '$.typeGroup') = {0}", dictItemVo.getLabel());
            List<CostAccountProportionRelation> lists = iCostAccountProportionRelationService.list(lambdaQueryWrapper);

            //封装出参
            //科室单元 关联信息主键(id)  科室单元名称(accountUnit) 核算分组名称(typeGroup) 比例(proportion)
            for (CostAccountProportionRelation list : lists) {
                CostAccountProportionListVo vo = new CostAccountProportionListVo();
                vo.setId(list.getId());
                //核算项名称
                vo.setCostAccountItem(costAccountProportion.getCostAccountItem());
                //科室单元名称 + 核算分组名称
                vo.setContext(list.getContext());
                //比例信息
                vo.setProportion(list.getProportion());
                ret.add(vo);
            }
        } else if (accountArrage.contains("KSDYFW008")) {
            LambdaQueryWrapper<CostAccountProportionRelation> lambdaQueryWrapper = new LambdaQueryWrapper<CostAccountProportionRelation>();
            lambdaQueryWrapper.eq(CostAccountProportionRelation::getCostAccountProportionId, input.getId());
            //部门
            if (StringUtils.isNotBlank(input.getDept())) lambdaQueryWrapper
                    .apply("JSON_EXTRACT(context, '$.dept') LIKE CONCAT('%', {0}, '%')", input.getDept());
            //部门性质
            DictItemVo dictItemVo = JSON.parseObject(input.getDeptType(), DictItemVo.class);
            if (StringUtils.isNotBlank(input.getDeptType())) lambdaQueryWrapper
                    .apply("JSON_EXTRACT(context, '$.deptType') = {0}", dictItemVo.getLabel());

            List<CostAccountProportionRelation> lists = iCostAccountProportionRelationService.list(lambdaQueryWrapper);

            //封装出参
            //自定义科室 关联信息主键(id)  核算科室（dept） 科室性质（deptType） 核算比例(proportion)
            for (CostAccountProportionRelation list : lists) {
                CostAccountProportionListVo vo = new CostAccountProportionListVo();
                //关联信息ID(id)
                vo.setId(list.getId());
                //核算项名称
                vo.setCostAccountItem(costAccountProportion.getCostAccountItem());
                //核算科室名称
                vo.setContext(list.getContext());
                //比例信息
                vo.setProportion(list.getProportion());
                ret.add(vo);
            }
        } else if (accountArrage.contains("KSDYFW009")) {
            LambdaQueryWrapper<CostAccountProportionRelation> lambdaQueryWrapper = new LambdaQueryWrapper<CostAccountProportionRelation>();
            lambdaQueryWrapper.eq(CostAccountProportionRelation::getCostAccountProportionId, input.getId());
            //姓名
            if (StringUtils.isNotBlank(input.getName())) lambdaQueryWrapper
                    .apply("JSON_EXTRACT(context, '$.name') LIKE CONCAT('%', {0}, '%')", input.getName());
            //人员工号
            if (StringUtils.isNotBlank(input.getJobNumber())) lambdaQueryWrapper
                    .apply("JSON_EXTRACT(context, '$.jobNumber') = {0}", input.getJobNumber());

            List<CostAccountProportionRelation> lists = iCostAccountProportionRelationService.list(lambdaQueryWrapper);

            //封装出参
            //自定义人员   姓名（name） 关联信息主键（id）  工号（jobNumber） 科室名称（dept） 核算比例（proportion）
            for (CostAccountProportionRelation list : lists) {
                CostAccountProportionListVo vo = new CostAccountProportionListVo();
                //关联信息ID(id)
                vo.setId(list.getId());
                //核算项名称
                vo.setCostAccountItem(costAccountProportion.getCostAccountItem());
                //人员信息
                vo.setContext(list.getContext());
                //比例信息
                vo.setProportion(list.getProportion());
                ret.add(vo);
            }
        }
        return ret;
    }

    //查询核算项信息
    @Override
    public IPage<CostAccountListVo> CostAccountList(CostAccountListDto input) {
        //条件过滤
        LambdaQueryWrapper<CostAccountProportion> lambdaQueryWrapper = new LambdaQueryWrapper<CostAccountProportion>();
        // 核算分组
        if (StringUtils.isNotBlank(input.getTypeGroupId())) lambdaQueryWrapper.eq(CostAccountProportion::getTypeGroupId,
                input.getTypeGroupId());
        // 核算项
        if (StringUtils.isNotBlank(input.getCostAccountItemId()))
            lambdaQueryWrapper.eq(CostAccountProportion::getCostAccountItemId,
                    input.getCostAccountItemId());
        // 核算对象
        if (StringUtils.isNotBlank(input.getAccountObject()))
            lambdaQueryWrapper.eq(CostAccountProportion::getAccountObject,
                    input.getAccountObject());
        // 状态
        if (StringUtils.isNotBlank(input.getStatus())) lambdaQueryWrapper.eq(CostAccountProportion::getStatus,
                input.getStatus());

        Page<CostAccountProportion> list = baseMapper.selectPage(new Page<>(input.getCurrent(), input.getSize()), lambdaQueryWrapper);

        Page ret = new Page(input.getCurrent(), input.getSize()).setRecords(
                list.getRecords().stream().map(r -> {
                    CostAccountListVo costAccountListVo = new CostAccountListVo();
                    costAccountListVo.setId(r.getId());
                    //获取核算范围名称 现在要求完整json格式返回
                    //DictItemVo dictItemVo = JSON.parseObject(r.getAccountObject(), DictItemVo.class);
                    //costAccountListVo.setAccountObject(dictItemVo.getLabel());
                    costAccountListVo.setAccountObject(r.getAccountObject());
                    costAccountListVo.setCostAccountItem(r.getCostAccountItem());
                    costAccountListVo.setStatus(r.getStatus());
                    costAccountListVo.setTypeGroupName(r.getTypeGroup());
                    return costAccountListVo;
                }).collect(Collectors.toList()));

        ret.setTotal(list.getTotal());
        ret.setSize(input.getSize());

        //封装返回数据
        return ret;
    }

    //切换指定id的核算比例项
    @Override
    public Boolean CostAccountProportionChange(CostAccountProportionStatusDto input) {
        //获取对应id的核算项
        CostAccountProportion costAccountProportion = baseMapper.selectOne(new LambdaQueryWrapper<CostAccountProportion>()
                .eq(CostAccountProportion::getId, input.getId()));

        if (costAccountProportion == null) throw new BizException("当前核算比例项不存在");

        //切换核算项的状态
        costAccountProportion.setStatus(input.getStatus());
        return updateById(costAccountProportion);
    }

    /**
     * 编辑核算项
     *
     * @param input 入参
     */
    @Override
    public void CostAccountProportionEdit(List<CostAccountProportionEditDto> input) {
        for (CostAccountProportionEditDto tmp : input) {
            //查找对应的核算比例关联信息
            CostAccountProportionRelation hsbl = iCostAccountProportionRelationService.getById(tmp.getId());
            if (hsbl == null) throw new BizException("当前核算比例关联信息不存在");

            //设置核算比例(保留四位小数)
            BigDecimal b = new BigDecimal(tmp.getProportion());
            tmp.setProportion(b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
            hsbl.setProportion(tmp.getProportion());

            iCostAccountProportionRelationService.update(hsbl, new LambdaQueryWrapper<CostAccountProportionRelation>()
                    .eq(CostAccountProportionRelation::getId, tmp.getId()));
        }
    }


    //医生组科室单元核算比例关联存储
    //将所有核算单元中核算分组为 医生组 的核算单元全部添加到关联关系中
    private void BindDocGroup(CostAccountProportion costAccountProportion) {
        //获取所有核算分组为医生组（字典编码为HSDX001）的核算单元科室
        List<CostAccountUnit> docList = costAccountUnitService
                .list(new LambdaQueryWrapper<CostAccountUnit>()
                        .like(CostAccountUnit::getAccountGroupCode, "HSDX001"));

        //封装关联表信息
        for (CostAccountUnit doc : docList) {
            //保存对象
            CostAccountProportionRelation costAccountProportionRelation
                    = new CostAccountProportionRelation();

            //关联当前核算项id
            costAccountProportionRelation.setCostAccountProportionId(costAccountProportion.getId());

            //科室单元id --> bzid
            costAccountProportionRelation.setBzid(doc.getId().toString());

            //科室单元type --> type
            DictItemVo dictItemVo = JSON.parseObject(costAccountProportion.getAccountObject(), DictItemVo.class);
            costAccountProportionRelation.setType(getType(dictItemVo.getValue()));

            //核算科室单元名称、核算分组 -->  context
            dictItemVo = JSON.parseObject(doc.getAccountGroupCode(), DictItemVo.class);
            //科室单元信息
            AccountUnitInfo accountUnitInfo = new AccountUnitInfo();
            //核算单元名称
            accountUnitInfo.setAccountUnit(doc.getName());
            //核算分组名称
            accountUnitInfo.setTypeGroup(dictItemVo.getLabel());
            costAccountProportionRelation.setContext(JSON.toJSONString(accountUnitInfo));
            iCostAccountProportionRelationService.save(costAccountProportionRelation);
        }
    }

    //护理组科室单元核算比例关联存储
    //将所有核算单元中核算分组为 护理组 的核算单元全部添加到关联关系中
    private void BindNurseGroup(CostAccountProportion costAccountProportion) {
        //获取所有核算分组为医生组（字典编码为HSDX001）的核算单元科室
        List<CostAccountUnit> nurseList = costAccountUnitService.list(new LambdaQueryWrapper<CostAccountUnit>()
                .like(CostAccountUnit::getAccountGroupCode, "HSDX002"));
        /*List<CostAccountUnit> nurseList = costAccountUnitMapper
                .selectList(new LambdaQueryWrapper<CostAccountUnit>()
                        .like(CostAccountUnit::getAccountGroupCode, "HSDX002"));*/

        //封装关联表信息
        for (CostAccountUnit nurse : nurseList) {
            //保存对象
            CostAccountProportionRelation costAccountProportionRelation
                    = new CostAccountProportionRelation();

            //关联当前核算项id
            costAccountProportionRelation.setCostAccountProportionId(costAccountProportion.getId());

            //科室单元id --> bzid
            costAccountProportionRelation.setBzid(nurse.getId().toString());

            //科室单元type --> type
            DictItemVo dictItemVo = JSON.parseObject(costAccountProportion.getAccountObject(), DictItemVo.class);
            costAccountProportionRelation.setType(getType(dictItemVo.getValue()));

            //核算科室单元名称、核算分组 -->  context
            dictItemVo = JSON.parseObject(nurse.getAccountGroupCode(), DictItemVo.class);
            //科室单元信息
            AccountUnitInfo accountUnitInfo = new AccountUnitInfo();
            //核算单元名称
            accountUnitInfo.setAccountUnit(nurse.getName());
            //核算分组名称
            accountUnitInfo.setTypeGroup(dictItemVo.getLabel());
            costAccountProportionRelation.setContext(JSON.toJSONString(accountUnitInfo));
            iCostAccountProportionRelationService.save(costAccountProportionRelation);
        }
    }

    //医技组科室单元核算比例关联存储
    //将所有核算单元中核算分组为 医技组 的核算单元全部添加到关联关系中
    private void BindMedicalTechnologyGroup(CostAccountProportion costAccountProportion) {
        //获取所有核算分组为医生组（字典编码为HSDX001）的核算单元科室
        List<CostAccountUnit> medicalTechnologyList = costAccountUnitService
                .list(new LambdaQueryWrapper<CostAccountUnit>()
                        .like(CostAccountUnit::getAccountGroupCode, "HSDX004"));
        /*List<CostAccountUnit> medicalTechnologyList = costAccountUnitMapper
                .selectList(new LambdaQueryWrapper<CostAccountUnit>()
                        .like(CostAccountUnit::getAccountGroupCode, "HSDX004"));*/

        //封装关联表信息
        for (CostAccountUnit medicalTechnology : medicalTechnologyList) {
            //保存对象
            CostAccountProportionRelation costAccountProportionRelation
                    = new CostAccountProportionRelation();

            //关联当前核算项id
            costAccountProportionRelation.setCostAccountProportionId(costAccountProportion.getId());

            //科室单元id --> bzid
            costAccountProportionRelation.setBzid(medicalTechnology.getId().toString());

            //科室单元type --> type
            DictItemVo dictItemVo = JSON.parseObject(costAccountProportion.getAccountObject(), DictItemVo.class);
            costAccountProportionRelation.setType(getType(dictItemVo.getValue()));

            //核算科室单元名称、核算分组 -->  context
            dictItemVo = JSON.parseObject(medicalTechnology.getAccountGroupCode(), DictItemVo.class);
            //科室单元信息
            AccountUnitInfo accountUnitInfo = new AccountUnitInfo();
            //核算单元名称
            accountUnitInfo.setAccountUnit(medicalTechnology.getName());
            //核算分组名称
            accountUnitInfo.setTypeGroup(dictItemVo.getLabel());
            costAccountProportionRelation.setContext(JSON.toJSONString(accountUnitInfo));
            iCostAccountProportionRelationService.save(costAccountProportionRelation);
        }
    }


    //自定义科室单元核算比例关联表存储
    private void BindCustomGroup(CostAccountProportion costAccountProportion, List<CommonDTO> customInput) {
        //获取所有自定义输入的核算单元科室
        List<CostAccountUnit> customGroupList = new ArrayList<>();
        for (CommonDTO tmp : customInput) {
            CostAccountUnit costAccountUnit = costAccountUnitService.getById(Long.valueOf(tmp.getId()));
            customGroupList.add(costAccountUnit);
        }

        //封装关联表信息
        for (CostAccountUnit customGroup : customGroupList) {
            //保存对象
            CostAccountProportionRelation costAccountProportionRelation
                    = new CostAccountProportionRelation();

            //关联当前核算项id
            costAccountProportionRelation.setCostAccountProportionId(costAccountProportion.getId());

            //科室单元id --> bzid
            costAccountProportionRelation.setBzid(customGroup.getId().toString());

            //科室单元type --> type
            DictItemVo dictItemVo = JSON.parseObject(costAccountProportion.getAccountObject(), DictItemVo.class);
            costAccountProportionRelation.setType(getType(dictItemVo.getValue()));

            //核算科室单元名称、核算分组 -->  context
            dictItemVo = JSON.parseObject(customGroup.getAccountGroupCode(), DictItemVo.class);
            //科室单元信息
            AccountUnitInfo accountUnitInfo = new AccountUnitInfo();
            //核算单元名称
            accountUnitInfo.setAccountUnit(customGroup.getName());
            //核算分组名称
            accountUnitInfo.setTypeGroup(dictItemVo.getLabel());
            costAccountProportionRelation.setContext(JSON.toJSONString(accountUnitInfo));
            iCostAccountProportionRelationService.save(costAccountProportionRelation);
        }
    }

    //自定义科室 核算比例关联表存储
    private void BindCustomDepartment(CostAccountProportion costAccountProportion, List<CommonDTO> customInput) {
        //获取所有自定义输入的核算科室信息
        List<SysDept> customDepartmentList = new ArrayList<>();
        for (CommonDTO tmp : customInput) {
            SysDept dept = remoteDeptService.getById(Long.parseLong(tmp.getId())).getData();
            customDepartmentList.add(dept);
        }

        //封装关联表信息
        for (SysDept customDepartment : customDepartmentList) {
            //保存对象
            CostAccountProportionRelation costAccountProportionRelation
                    = new CostAccountProportionRelation();

            //关联当前核算项id
            costAccountProportionRelation.setCostAccountProportionId(costAccountProportion.getId());

            //部门id --> bzid
            costAccountProportionRelation.setBzid(customDepartment.getDeptId().toString());

            //部门type --> type
            DictItemVo dictItemVo = JSON.parseObject(costAccountProportion.getAccountObject(), DictItemVo.class);
            costAccountProportionRelation.setType(getType(dictItemVo.getValue()));

            //科室名称 科室性质 -->context
            DeptInfo deptInfo = new DeptInfo();
            //科室名称
            deptInfo.setDept(customDepartment.getName());
            dictItemVo = JSON.parseObject(customDepartment.getType(), DictItemVo.class);
            //科室性质名称
            deptInfo.setDeptType(dictItemVo.getLabel());
            costAccountProportionRelation.setContext(JSON.toJSONString(deptInfo));
            iCostAccountProportionRelationService.save(costAccountProportionRelation);
        }
    }

    //自定义人员 核算比例关联表存储
    private void BindUser(CostAccountProportion costAccountProportion, List<CommonDTO> customInput) {
        //获取所有自定义输入的人员信息
        List<UserVO> customUserList = new ArrayList<>();
        for (CommonDTO tmp : customInput) {
            UserVO user = remoteUserService.details(Long.parseLong(tmp.getId())).getData();
            customUserList.add(user);
        }

        //封装关联表信息
        for (UserVO customUser : customUserList) {
            //保存对象
            CostAccountProportionRelation costAccountProportionRelation
                    = new CostAccountProportionRelation();

            //关联当前核算项id
            costAccountProportionRelation.setCostAccountProportionId(costAccountProportion.getId());

            //人员id --> bzid
            costAccountProportionRelation.setBzid(customUser.getUserId().toString());

            //人员type --> type
            DictItemVo dictItemVo = JSON.parseObject(costAccountProportion.getAccountObject(), DictItemVo.class);
            costAccountProportionRelation.setType(getType(dictItemVo.getValue()));

            //人员名称 人员工号 科室信息名称(逗号分割) --> context
            UserInfo userInfo = new UserInfo();
            //人员名称
            userInfo.setName(customUser.getName());
            //人员工号
            if (customUser.getJobNumber() == null) userInfo.setJobNumber("");
            else userInfo.setJobNumber(customUser.getJobNumber());

            //科室名称 逗号风格
            if (CollectionUtils.isEmpty(customUser.getDeptList())) throw new BizException("当前人员科室未配置");
            List<SysDept> deptList = customUser.getDeptList();
            List<String> deptInfo = new ArrayList<>();
            for (SysDept dept : deptList) {
                //查询部门信息
                deptInfo.add(dept.getName().toString());
            }
            userInfo.setDept(String.join(", ", deptInfo));

            costAccountProportionRelation.setContext(JSON.toJSONString(userInfo));
            iCostAccountProportionRelationService.save(costAccountProportionRelation);
        }
    }

    private void BindReagentGroup(CostAccountProportion costAccountProportion) {
        //获取所有核算分组为药剂组（字典编码为HSDX001）的核算单元科室
        List<CostAccountUnit> reagentList = costAccountUnitService
                .list(new LambdaQueryWrapper<CostAccountUnit>()
                        .like(CostAccountUnit::getAccountGroupCode, "HSDX006"));
        /*List<CostAccountUnit> medicalTechnologyList = costAccountUnitMapper
                .selectList(new LambdaQueryWrapper<CostAccountUnit>()
                        .like(CostAccountUnit::getAccountGroupCode, "HSDX004"));*/

        //封装关联表信息
        for (CostAccountUnit reagent : reagentList) {
            //保存对象
            CostAccountProportionRelation costAccountProportionRelation
                    = new CostAccountProportionRelation();

            //关联当前核算项id
            costAccountProportionRelation.setCostAccountProportionId(costAccountProportion.getId());

            //科室单元id --> bzid
            costAccountProportionRelation.setBzid(reagent.getId().toString());

            //科室单元type --> type
            DictItemVo dictItemVo = JSON.parseObject(costAccountProportion.getAccountObject(), DictItemVo.class);
            costAccountProportionRelation.setType(getType(dictItemVo.getValue()));

            //核算科室单元名称、核算分组 -->  context
            dictItemVo = JSON.parseObject(reagent.getAccountGroupCode(), DictItemVo.class);
            //科室单元信息
            AccountUnitInfo accountUnitInfo = new AccountUnitInfo();
            //核算单元名称
            accountUnitInfo.setAccountUnit(reagent.getName());
            //核算分组名称
            accountUnitInfo.setTypeGroup(dictItemVo.getLabel());
            costAccountProportionRelation.setContext(JSON.toJSONString(accountUnitInfo));
            iCostAccountProportionRelationService.save(costAccountProportionRelation);
        }
    }

    //医护对应科室单元 核算比例关联表存储
    private void BindDocNurse(CostAccountProportion costAccountProportion) {
        //获取所有医护对应科室单元
        List<CostAccountUnit> groupList = new ArrayList<>();
        //TODO 分页获取页数
        listDocNRelationDto input = new listDocNRelationDto();
        input.setCurrent(1);
        input.setSize(Long.MAX_VALUE);
        List<DocNRelationListVo> records = iCostDocNRelationService.listDocNRelation(input).getRecords();

        for (DocNRelationListVo record : records) {
            if (record.getNurseInfo().size() != 0) {
                CostAccountUnit doc = costAccountUnitService.getById(record.getDocInfo().getId());
                groupList.add(doc);
                for (CommonDTO nurse : record.getNurseInfo()) {
                    groupList.add(costAccountUnitService.getById(nurse.getId()));
                }
            }
        }
        //去重
        groupList = new ArrayList<>(groupList.stream()
                .collect(Collectors.toMap(CostAccountUnit::getId, Function.identity(), (c1, c2) -> c1)).values()
        );


        for (CostAccountUnit group : groupList) {
            //保存对象
            CostAccountProportionRelation costAccountProportionRelation
                    = new CostAccountProportionRelation();

            //关联当前核算项id
            costAccountProportionRelation.setCostAccountProportionId(costAccountProportion.getId());

            //科室单元id --> bzid
            costAccountProportionRelation.setBzid(group.getId().toString());

            //科室单元type --> type
            DictItemVo dictItemVo = JSON.parseObject(costAccountProportion.getAccountObject(), DictItemVo.class);
            costAccountProportionRelation.setType(getType(dictItemVo.getValue()));


            //核算科室单元名称、核算分组 -->  context
            dictItemVo = JSON.parseObject(group.getAccountGroupCode(), DictItemVo.class);
            //科室单元信息
            AccountUnitInfo accountUnitInfo = new AccountUnitInfo();
            //核算单元名称
            accountUnitInfo.setAccountUnit(group.getName());
            //核算分组名称
            accountUnitInfo.setTypeGroup(dictItemVo.getLabel());
            costAccountProportionRelation.setContext(JSON.toJSONString(accountUnitInfo));
            iCostAccountProportionRelationService.save(costAccountProportionRelation);
        }
    }


    //获取核算范围对应的type
    private String getType(String groupArrage) {
        if (StringUtils.isBlank(groupArrage)) throw new BizException("当前核算范围不存在");
        CostAccountProportionType[] values = CostAccountProportionType.values();
        for (CostAccountProportionType value : values) {
            if (value.getGroupArrange().equals(groupArrage)) return value.getType();
        }
        throw new BizException("当前核算范围不存在对应的type值");
    }


    @Override
    public List<CostAccountProportionVo> getProportion(CostAccountProportionDto input) {
        String type = input.getType();
        List<CostAccountProportion> costAccountProportions;
        if (CostAccountProportionType.DOCNURSEGROUP.getGroupArrange().equals(type)){
            //医护对应科室单元
            costAccountProportions = new CostAccountProportion().selectList(new LambdaQueryWrapper<CostAccountProportion>()
                    .eq(CostAccountProportion::getCostAccountItemId, input.getItemId())
                    .apply("JSON_EXTRACT(account_object, '$.value') ={0}", CostAccountProportionType.DOCNURSEGROUP.getGroupArrange())
            );
        }
        else if (CostAccountProportionType.DEPT.getType().equals(type.toUpperCase())) {
            //根据科室类型
             costAccountProportions = new CostAccountProportion().selectList(new LambdaQueryWrapper<CostAccountProportion>()
                    .eq(CostAccountProportion::getCostAccountItemId, input.getItemId())
                    .apply("JSON_EXTRACT(account_object, '$.value') ={0}", CostAccountProportionType.DEPT.getGroupArrange())
            );

        } else if (CostAccountProportionType.USER.getType().equals(type.toUpperCase())) {
            //根据人员类型
            costAccountProportions = new CostAccountProportion().selectList(new LambdaQueryWrapper<CostAccountProportion>()
                    .eq(CostAccountProportion::getCostAccountItemId, input.getItemId())
                    .apply("JSON_EXTRACT(account_object, '$.value') ={0}", CostAccountProportionType.USER.getGroupArrange())
            );


        } else {
            throw new BizException("不支持的核算比例查询类型");
        }
        List<CostAccountProportionVo> vos = new ArrayList<>();
//        costAccountProportions.forEach(costAccountProportion -> {
//            List<CostAccountProportionRelation> costAccountProportionRelations = new CostAccountProportionRelation().selectList(new LambdaQueryWrapper<CostAccountProportionRelation>()
//                    .eq(CostAccountProportionRelation::getCostAccountProportionId, costAccountProportion.getId())
//            );
//            costAccountProportionRelations.forEach(costAccountProportionRelation -> {
//                CostAccountProportionVo costAccountProportionVo = new CostAccountProportionVo();
//                costAccountProportionVo.setTypeGroup(costAccountProportion.getTypeGroup());
//                costAccountProportionVo.setTypeGroupId(costAccountProportion.getTypeGroupId());
//                costAccountProportionVo.setId(costAccountProportionRelation.getId());
//                costAccountProportionVo.setProportion(costAccountProportionRelation.getProportion());
//                vos.add(costAccountProportionVo);
//            });
//        });
        costAccountProportions.forEach(costAccountProportion -> {
            CostAccountProportionVo costAccountProportionVo = new CostAccountProportionVo();
            costAccountProportionVo.setTypeGroup(costAccountProportion.getTypeGroup());
            costAccountProportionVo.setTypeGroupId(costAccountProportion.getTypeGroupId());
            costAccountProportionVo.setId(costAccountProportion.getId());
            vos.add(costAccountProportionVo);
        });
        return vos;
    }
}
