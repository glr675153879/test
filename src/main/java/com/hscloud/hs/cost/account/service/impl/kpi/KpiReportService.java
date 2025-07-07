package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.enums.kpi.CategoryEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.MemberEnum;
import com.hscloud.hs.cost.account.mapper.kpi.*;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.dto.kpi.excel.ExcelKpiCalculateDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.*;
import com.hscloud.hs.cost.account.service.kpi.IKpiReportService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 核算任务表(cost_account_task) 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class KpiReportService extends ServiceImpl<KpiReportMapper, KpiReport> implements IKpiReportService {

    @Autowired
    private KpiReportMapper kpiReportMapper;
    @Autowired
    private KpiReportDetailMapper kpiReportDetailMapper;
    @Autowired
    private KpiCalculateMapper kpiCalculateMapper;
    @Autowired
    private KpiUserAttendanceCopyMapper kpiUserAttendanceCopyMapper;
    @Autowired
    private KpiAccountUnitCopyMapper kpiAccountUnitCopyMapper;
    @Autowired
    private KpiConfigMapper kpiConfigMapper;
    @Autowired
    private KpiCategoryMapper kpiCategoryMapper;
    @Autowired
    private KpiMemberCopyMapper kpiMemberCopyMapper;
    @Autowired
    private KpiItemCopyMapper kpiItemCopyMapper;
    @Autowired
    private KpiAccountTaskMapper kpiAccountTaskMapper;
    @Autowired
    private KpiCalculateCompMapper kpiCalculateCompMapper;

    @Override
    public void cu(KpiCodeDTO input) {
        if (input.getId() == null) {
            KpiReport e = kpiReportMapper.getOne(input.getCode());
            if (e != null) {
                throw new BizException("code已存在");
            }
            KpiReport report = new KpiReport();
            report.setName(input.getName())
                    .setUserGroup(input.getUserGroup())
                    .setTenantId(SecurityUtils.getUser().getTenantId())
                    .setCode(input.getCode())
                    .setCaliber(input.getCaliber())
                    .setType(input.getType())
                    .setCreatedDate(new Date())
                    .setCreatedId(SecurityUtils.getUser().getId());
            kpiReportMapper.insert(report);
        } else {
            KpiReport report = kpiReportMapper.selectById(input.getId());
            report.setName(input.getName())
                    .setUserGroup(input.getUserGroup())
                    .setTenantId(SecurityUtils.getUser().getTenantId())
                    .setCode(input.getCode())
                    .setCaliber(input.getCaliber())
                    .setType(input.getType())
                    .setUpdatedDate(new Date())
                    .setUpdatedId(SecurityUtils.getUser().getId());
            kpiReportMapper.updateById(report);
        }

        if (!input.getList().isEmpty()){
            KpiReportDetailDTO dto = new KpiReportDetailDTO();
            dto.setCode(input.getCode());
            dto.setList(input.getList());
            detailCu(dto);
        }
    }

    @Override
    public void detailCu(KpiReportDetailDTO input) {
        kpiReportDetailMapper.delete(new QueryWrapper<KpiReportDetail>().eq("report_code",input.getCode()));
        input.getList().forEach(r -> {
            KpiReportDetail entity = new KpiReportDetail();
            entity.setCaliber(r.getCaliber())
                    .setCode(r.getCode())
                    .setName(r.getName())
                    .setIndexCode(r.getIndexCode())
                    .setTenantId(SecurityUtils.getUser().getTenantId())
                    .setReportCode(input.getCode())
                    .setCreatedDate(new Date())
                    .setCreatedId(SecurityUtils.getUser().getId());
            kpiReportDetailMapper.insert(entity);
        });
    }

    @Override
    public void reportDel(String reportCode){
        kpiReportDetailMapper.delete(new QueryWrapper<KpiReportDetail>().eq("report_code",reportCode));
        kpiReportMapper.delete(new QueryWrapper<KpiReport>().eq("code",reportCode));
    }

    @Override
    public List<String> importData(List<ExcelKpiCalculateDTO> excelList,Long taskChildId) {
        if (excelList == null || excelList.isEmpty()){
            throw new BizException("导入数据为空");
        }
        List<String> list = new ArrayList<>();
        KpiAccountTask task = kpiAccountTaskMapper.getIndexCode(taskChildId);
        if (task == null || StringUtil.isNullOrEmpty(task.getIndexCode())) {
            throw new BizException("该任务indexCode为空，不能导入");
        }
        List<KpiCalculate> cas = kpiCalculateMapper.selectList(
                new QueryWrapper<KpiCalculate>()
                        .eq("task_child_id", taskChildId)
                        .eq("code",task.getIndexCode())
                        .in("imputation_type",Arrays.asList("0,1".split(",")))
                        .select("id", "dept_id", "user_id","dept_name","user_name","period"));
        for (ExcelKpiCalculateDTO input : excelList) {
            if (input.getUnit() !=null) {
                KpiCalculate calculate = Linq.of(cas).firstOrDefault(ca -> input.getUnit().equals(ca.getDeptName()) || input.getUnit().equals(ca.getUserName()));
                if (calculate != null) {
                    calculate.setCompValue(input.getValue());
                    kpiCalculateMapper.updateCompValue(calculate);
                }else{
                    list.add(input.getUnit());
                }
            }
        }
        return list;
    }

    @Override
    public List<String> importData(Long taskChildId, List<Map<Integer, String>> list,String imputationCode) {
        KpiAccountTask task = kpiAccountTaskMapper.getIndexCode(taskChildId);
        if (task == null || StringUtil.isNullOrEmpty(task.getIndexCode())) {
            throw new BizException("该任务indexCode为空，不能导入");
        }
        List<String> rt = new ArrayList<>();
        List<KpiCalculate> cas = kpiCalculateMapper.selectList(
                new QueryWrapper<KpiCalculate>()
                        .eq("period", task.getPeriod())
                        .eq("task_child_id", taskChildId)
                        .eq("code", task.getIndexCode())
                        .in(StringUtil.isNullOrEmpty(imputationCode),"imputation_type",Arrays.asList("0,1".split(",")))
                        .eq(!StringUtil.isNullOrEmpty(imputationCode),"imputation_type","2")
                        .select("period", "id", "name", "user_name", "dept_name","out_name", "user_id", "dept_id","code","tenant_id","imputation_type","imputation_code")
        );
        if (cas.isEmpty()) {
            return null;
        }

        Map<Integer, String> top = list.get(0);
        String name = cas.get(0).getName();
        List<KpiReportConfigImport> imports = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            KpiReportConfigImport entity = new KpiReportConfigImport();
            int finalI = i;
            top.forEach((key, value) -> {
                if ("核算单元".equals(value)) {
                    entity.setDeptName(list.get(finalI).get(key));
                } else if ("核算人员".equals(value)) {
                    entity.setUserName(list.get(finalI).get(key));
                } else if ("摊出核算单元".equals(value)) {
                    entity.setOutName(list.get(finalI).get(key));
                }else if (!StringUtil.isNullOrEmpty(name) && name.replace("\n","").replace(" ","").equals(value)) {
                    String s = list.get(finalI).get(key);
                    if (!StringUtil.isNullOrEmpty(s)) {
                        s=s.replace("\n","").replace(" ","");
                        if (!NumberUtil.isNumber(s)){
                            throw new BizException("第"+finalI+"行不是数字");
                        }else {
                            entity.setValue(new BigDecimal(s));
                        }
                    }
                }
            });
            if (entity.getValue() != null) {
                imports.add(entity);
            }
        }
        List<KpiCalculateComp> comps = new ArrayList<>();
        for (KpiReportConfigImport i : imports) {
            KpiCalculate first = null;
            List<KpiCalculate> cas2 = cas;

            if (!StringUtil.isNullOrEmpty(i.getOutName())){
                cas2 = Linq.of(cas).where(x -> i.getOutName().equals(x.getOutName())).toList();
            }
            if (!StringUtil.isNullOrEmpty(i.getUserName()) && !StringUtil.isNullOrEmpty(i.getDeptName())) {
                first = Linq.of(cas2).firstOrDefault(x -> i.getUserName().equals(x.getUserName())
                        && i.getDeptName().equals(x.getDeptName()));
            } else if (!StringUtil.isNullOrEmpty(i.getUserName())) {
                first = Linq.of(cas2).firstOrDefault(x -> i.getUserName().equals(x.getUserName()));
            } else if (!StringUtil.isNullOrEmpty(i.getDeptName())) {
                first = Linq.of(cas2).firstOrDefault(x -> i.getDeptName().equals(x.getDeptName()));
            }

            if (first != null){
                first.setCompValue(i.getValue());
                //kpiCalculateMapper.updateCompValue(first);

                KpiCalculateComp comp = new KpiCalculateComp();
                BeanUtils.copyProperties(first,comp);
                comps.add(comp);
            }else{
                rt.add(StringUtil.isNullOrEmpty(i.getUserName())? i.getDeptName():i.getUserName());
            }
        }

        if (!comps.isEmpty()){
            kpiCalculateCompMapper.delete(
                    new QueryWrapper<KpiCalculateComp>()
                            .eq("period", task.getPeriod())
                            .eq("code",task.getIndexCode())
                            .in(StringUtil.isNullOrEmpty(imputationCode),"imputation_type",Arrays.asList("0,1".split(",")))
                            .eq(!StringUtil.isNullOrEmpty(imputationCode),"imputation_type","2")
            );
            kpiCalculateCompMapper.insertBatchSomeColumn(comps);
        }
        return rt;
    }


    @Override
    public IPage<KpiReportVO> getPage(KpiCodePageDTO input) {
        IPage<KpiReportVO> page = kpiReportMapper.page(new Page<>(input.getCurrent(), input.getSize()), input);
        return page;
    }

    @Override
    public IPage<KpiReportDetailVO> getlist(KpiCodeDetailPageDTO input) {
        if (StringUtil.isNullOrEmpty(input.getReport_code())) {
            throw new BizException("接口code不能为空");
        }
        return kpiReportMapper.selectList(new Page<>(input.getCurrent(), input.getSize()), input);
    }

    @Override
    public KpiReportCodeVO report(KpiReportCodeDTO input) {
        try{
            List<KpiCalculate> results = new ArrayList<>();
            List<KpiCalculate> cas_gj = new ArrayList<>();
            //List<Long> inuserList = new ArrayList<>();
            //List<Long> outuserList = new ArrayList<>();
            KpiReportCodeVO rt = new KpiReportCodeVO();
            KpiReport report = new KpiReport();
            if (StringUtil.isNullOrEmpty(input.getCode()) && StringUtil.isNullOrEmpty(input.getIndexCodes())&& StringUtil.isNullOrEmpty(input.getItemCodes())) {
                throw new BizException("接口code和indexCodes和itemCodes不能都为空");
            }
            if (input.getCycle() == null) {
                throw new BizException("周期不能为空");
            }

            KpiConfig kpiConfig = kpiConfigMapper.selectOne(
                    new QueryWrapper<KpiConfig>()
                            .eq("period", input.getCycle())
                            .eq("issued_flag", "Y")
            );
            if (kpiConfig == null) {
                throw new BizException(input.getCycle() + "周期未锁定");
            }

            String type = "1";//1 普通，2归集 3核算项
            String caliber="";//1 人，2，科室,3，人+科室
            List<String> codes =new ArrayList<>();
            List<KpiReportDetail> list =new ArrayList<>();
            if(!StringUtil.isNullOrEmpty(input.getIndexCodes()) || !StringUtil.isNullOrEmpty(input.getItemCodes()))
            {
                if (!StringUtil.isNullOrEmpty(input.getIndexCodes())) {
                    type = "2";
                    caliber = "1";
                    codes = new ArrayList<>(Arrays.asList(input.getIndexCodes().split(",")));
                    for (String code : codes) {
                        KpiReportDetail kpiReportDetail = new KpiReportDetail();
                        kpiReportDetail.setIndexCode(code);
                        kpiReportDetail.setCode(code);
                        list.add(kpiReportDetail);
                    }
                }
                else if (!StringUtil.isNullOrEmpty(input.getItemCodes())){
                    type="3";
                    codes = new ArrayList<>(Arrays.asList(input.getItemCodes().split(",")));
                    List<KpiItemCopy> items = kpiItemCopyMapper.selectList(
                            new QueryWrapper<KpiItemCopy>()
                                    .eq("task_child_id",kpiConfig.getTaskChildId())
                                    .in("code", codes)
                                    .select("code","item_name")
                    );
                    if (items.isEmpty()){
                        throw new BizException("itemCodes无数据");
                    }
                    for (KpiItemCopy item : items) {
                        KpiReportDetail kpiReportDetail = new KpiReportDetail();
                        kpiReportDetail.setIndexCode(item.getCode());
                        kpiReportDetail.setCode(item.getCode());
                        kpiReportDetail.setName(item.getItemName());
                        list.add(kpiReportDetail);
                    }
                    caliber = "1";
                }
            }
            else {
                report = kpiReportMapper.getOne(input.getCode());
                if (report == null) {
                    throw new BizException("接口code有误");
                }
                if(report.getType().equals("1")) {
                    list = kpiReportDetailMapper.selectList(
                            new QueryWrapper<KpiReportDetail>()
                                    .eq("report_code", input.getCode())
                    );
                }
                else {
                    KpiReportDetail kpiReportDetail = new KpiReportDetail();
                    kpiReportDetail.setIndexCode(report.getCode());
                    kpiReportDetail.setCode(report.getCode());
                    list.add(kpiReportDetail);
                }
                type = report.getType();
                caliber=report.getCaliber();
                codes = Linq.of(list).select(r -> r.getIndexCode()).toList();
            }

            List<KpiUserAttendanceCopy> userAttList = kpiUserAttendanceCopyMapper.selectList(
                        new QueryWrapper<KpiUserAttendanceCopy>()
                                .eq("task_child_id", kpiConfig.getTaskChildId())
                                .eq("period", input.getCycle())
                                .select(KpiUserAttendanceCopy.class, i ->!i.getProperty().equals("originCustomFields") &&!i.getProperty().equals("customFields"))
                );
            List<KpiAccountUnitCopy> accountUnitList = kpiAccountUnitCopyMapper.selectList(
                        new QueryWrapper<KpiAccountUnitCopy>()
                                .eq("task_child_id", kpiConfig.getTaskChildId())
                );
            List<KpiMemberCopy> members = kpiMemberCopyMapper.selectList(
                    new QueryWrapper<KpiMemberCopy>()
                            .eq("task_child_id", kpiConfig.getTaskChildId())
                            .eq("member_type", MemberEnum.ROLE_EMP.getType())
            );

            //先过滤人
            if(codes.isEmpty())
            {
                results = Linq.of(userAttList).groupBy(user -> new KpiKeyValueVO(user.getUserId().toString(), user.getEmpName()))
                        .select(user->new KpiCalculate(Long.parseLong(user.getKey().getKey()), user.getKey().getValue())).toList();
            }
            else if ("2".equals(type) || "1".equals(type)) {
                QueryWrapper<KpiCalculate> qw = new QueryWrapper<KpiCalculate>()
                        .eq("period", input.getCycle())
                        .eq("task_child_id", kpiConfig.getTaskChildId())
                        .in("code", codes);
                if ("Y".equals(input.getFilterZero())) {
                    qw.ne("value", 0);
                }
                if ("1".equals(type)) {
                    qw.eq("imputation_type", "0");
                    if (!StringUtil.isNullOrEmpty(input.getUserIds())) {
                        qw.in("user_id", Arrays.asList(input.getUserIds().split(",")));
                    } else if (!StringUtil.isNullOrEmpty(input.getDeptIds())) {
                        qw.in("dept_id", Arrays.asList(input.getDeptIds().split(",")));
                    }
                } else if ("2".equals(type)) {
                    QueryWrapper<KpiCalculate> qw_gj = new QueryWrapper<>();
                    if (!StringUtil.isNullOrEmpty(input.getDeptIds())) {
                        qw_gj.in("dept_id", Arrays.asList(input.getDeptIds().split(",")));
                    }
                    qw_gj.eq("imputation_type", "1")
                            .eq("period", input.getCycle())
                            .eq("task_child_id", kpiConfig.getTaskChildId())
                            .in("code", codes);
                    qw_gj.select("code", "value",
                            "dept_id", "emp_id", "imputation_type", "imputation_code", "user_id", "user_name", "dept_name");
                    cas_gj = kpiCalculateMapper.selectList(qw_gj);
                    if (!cas_gj.isEmpty()) {
                        qw.in("imputation_code", Linq.of(cas_gj).select(i -> i.getImputationCode()).toList())
                                .eq("imputation_type", "2");
                    } else {
                        List<String> empty = new ArrayList<>();
                        empty.add("empty");
                        qw.in("imputation_code", empty)
                                .eq("imputation_type", "2");
                    }
                }
                qw.select("id", "period", "plan_child_code", "task_child_id", "code", "value",
                        "dept_id", "emp_id", "imputation_type", "imputation_code", "user_id", "user_name", "dept_name"
                        , "group_name", "out_name", "allocation_name", "allocation_type", "name", "plan_code","establish");
                results = kpiCalculateMapper.selectList(qw);
            }
            else if ("3".equals(type)){
                QueryWrapper<KpiCalculate> qw_item = new QueryWrapper<>();
                qw_item.eq("period",input.getCycle())
                        .in("code", codes)
                        .eq("task_child_id",kpiConfig.getTaskChildId())
                        .eq("busi_type","1");
                if ("Y".equals(input.getFilterZero())) {
                    qw_item.ne("value", 0);
                }
                results = kpiReportMapper.getItemCas(qw_item);
                for (KpiCalculate result : results) {
                    KpiAccountUnitCopy g = Linq.of(accountUnitList).firstOrDefault(t -> t.getId().equals(result.getDeptId()));
                    if (g != null) {
                        result.setDeptId(g.getId());
                        result.setDeptName(g.getName());
                    }
                    KpiUserAttendanceCopy h = Linq.of(userAttList).firstOrDefault(t -> t.getUserId().equals(result.getUserId()));
                    if (h != null) {
                        result.setUserId(h.getUserId());
                        result.setUserName(h.getEmpName());
                    }
                    KpiReportDetail x = Linq.of(list).firstOrDefault(t -> t.getCode().equals(result.getCode()));
                    if (x != null) {
                        result.setName(x.getName());
                    }
                }
            }

            if("2".equals(type))
            {
                for (KpiCalculate result : results) {
                    KpiCalculate g = Linq.of(cas_gj).firstOrDefault(gj -> gj.getImputationCode().equals(result.getImputationCode()));
                    if (g != null) {
                        result.setDeptId(g.getDeptId());
                        result.setDeptName(g.getDeptName());
                    }
                }
            }

            if ("1".equals(caliber)){
                //核算人员分组名 like编外
                /*List<String> out_category_codes = kpiCategoryMapper.getBwCode("编外");
                List<String> in_category_codes = kpiCategoryMapper.getBwCode("编内");
                List<KpiMemberCopy> outmembers = Linq.of(members).where(m -> out_category_codes.contains(m.getHostCode())).toList();
                List<KpiMemberCopy> inmembers = Linq.of(members).where(m -> in_category_codes.contains(m.getHostCode())).toList();
                outuserList = Linq.of(outmembers).select(r -> r.getMemberId()).toList();
                inuserList = Linq.of(inmembers).select(r -> r.getMemberId()).toList();*/
                if ("Y".equals(input.getUserType())) {
                    //List<Long> finalUserList2 = outuserList;
                    results = Linq.of(results).where(c -> "0".equals(c.getEstablish())).toList();
                } else if ("N".equals(input.getUserType())){
                    //List<Long> finalUserList1 = inuserList;
                    results = Linq.of(results).where(c -> "1".equals(c.getEstablish())).toList();
                }

                if (!input.getCycle().toString().endsWith("13") && !StringUtil.isNullOrEmpty(report.getUserGroup())) {
                    String userGroup = report.getUserGroup();
                    List<Long> userIds = Linq.of(members).where(m -> Arrays.asList(userGroup.split(",")).contains(m.getHostCode())).select(m -> m.getMemberId()).distinct().toList();
                    results = Linq.of(results).where(x -> userIds.contains(x.getUserId())).toList();
                }
            }
            List<JSONObject> objs = new ArrayList<>();
            if ("1".equals(type)) {
                List<KpiUserAttendanceCopy> finalUserAttList = userAttList;
                String finalCaliber1 = caliber;
                //List<Long> finalUserList = inuserList;
                //List<Long> finaloutUserList = outuserList;
                objs = Linq.of(results).groupBy(g -> finalCaliber1.equals("1") ? new KpiCalculateGroupByVo(g.getUserId(), g.getUserName()) : new KpiCalculateGroupByVo(g.getDeptId(), g.getDeptName())).select(
                        r -> {
                            JSONObject obj = new JSONObject();
                            obj.put("cycle", input.getCycle());
                            KpiCalculate first = r.toList().get(0);
                            if (finalCaliber1.equals("1")) {
                                if ("1".equals(first.getEstablish())){
                                    obj.put("userType","N");
                                }else if("0".equals(first.getEstablish())){
                                    obj.put("userType","Y");
                                }else{
                                    obj.put("userType","");
                                }
                                obj.put("userId", r.getKey().getUserId());
                                obj.put("userName", r.getKey().getUserName());
                            } else {
                                obj.put("deptId", r.getKey().getUserId());
                                obj.put("deptName", r.getKey().getUserName());
                            }
                            List<KpiUserAttendanceCopy> users = Linq.of(finalUserAttList).where(u -> u.getUserId().equals(obj.get("userId"))).toList();
                            if (!users.isEmpty()) {
                                List<String> units = Linq.of(users).select(u -> u.getAccountUnit().toString()).toList();
                                List<String> names = Linq.of(users).select(u -> u.getAccountUnitName()).toList();
                                obj.put("deptId", String.join(",", units));
                                obj.put("deptName", String.join(",", names));
                            }
                            return obj;
                        }
                ).toList();
            } else {
                objs = Linq.of(results).groupBy(g -> new KpiCalculateGroupByVo(g.getUserId(), g.getUserName(), g.getDeptId(), g.getDeptName(),g.getCode(),g.getName())).select(
                        r -> {
                            JSONObject obj = new JSONObject();
                            obj.put("cycle", input.getCycle());
                            obj.put("userId", r.getKey().getUserId());
                            obj.put("userName", r.getKey().getUserName());
                            obj.put("deptId", r.getKey().getDeptId());
                            obj.put("deptName", r.getKey().getDeptName());
                            obj.put("itemValue", r.sumDecimal(m->m.getValue()));
                            obj.put("itemName",r.getKey().getName());
                            obj.put("itemCode", r.getKey().getCode());
                            return obj;
                        }
                ).toList();
            }
            //遍历每个人或科室对应的计算结果
            for (JSONObject obj : objs) {
                if ("1".equals(type) ) {
                    String finalCaliber = caliber;
                    for (KpiReportDetail kpiReportDetail : list) {
                        BigDecimal ca = Linq.of(results).where(c -> (finalCaliber.equals("1") ? c.getUserId().equals(obj.get("userId")) : c.getDeptId().equals(obj.get("deptId")))
                                && c.getCode().equals(kpiReportDetail.getIndexCode())).select(t -> t.getValue()).sumDecimal();
                        obj.put(kpiReportDetail.getCode(), ca);
                    }
                }
            }
            rt.setList(objs);
            return rt;
        }catch (Exception e){
                KpiReportCodeVO kpiReportCodeVO = new KpiReportCodeVO();
                kpiReportCodeVO.setList(new ArrayList<>());
                return kpiReportCodeVO;
        }
    }
}
