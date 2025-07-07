package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.enums.kpi.CategoryEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.FormulaParamEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.MemberEnum;
import com.hscloud.hs.cost.account.mapper.kpi.*;
import com.hscloud.hs.cost.account.mapper.second.AttendanceMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import com.hscloud.hs.cost.account.model.entity.second.GrantUnit;
import com.hscloud.hs.cost.account.model.vo.kpi.*;
import com.hscloud.hs.cost.account.service.kpi.task.TaskCaculateService;
import com.hscloud.hs.cost.account.service.second.IGrantUnitService;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.service.kpi.IKpiReportConfigService;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 报表多选配置 服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KpiReportConfigService extends ServiceImpl<KpiReportConfigMapper, KpiReportConfig> implements IKpiReportConfigService {

    @Autowired
    private IGrantUnitService grantUnitService;
    @Autowired
    private KpiReportConfigMapper kpiReportConfigMapper;
    @Autowired
    private KpiAccountTaskMapper kpiAccountTaskMapper;
    @Autowired
    private KpiMemberCopyMapper kpiMemberCopyMapper;
    @Autowired
    private KpiUserAttendanceCopyMapper kpiUserAttendanceCopyMapper;
    @Autowired
    private KpiAccountUnitCopyMapper kpiAccountUnitCopyMapper;
    @Autowired
    private KpiAccountTaskChildMapper kpiAccountTaskChildMapper;
    @Autowired
    private KpiCalculateMapper kpiCalculateMapper;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    private KpiAccountUnitMapper kpiAccountUnitMapper;
    @Autowired
    private KpiItemCopyMapper kpiItemCopyMapper;
    @Autowired
    private KpiReportConfigCopyMapper kpiReportConfigCopyMapper;
    @Autowired
    private KpiReportConfigPowerMapper kpiReportConfigPowerMapper;
    @Autowired
    private KpiConfigMapper kpiConfigMapper;
    @Autowired
    private KpiCategoryMapper kpiCategoryMapper;
    @Autowired
    private KpiReportConfigImportMapper kpiReportConfigImportMapper;
    @Autowired
    private KpiReportYearImportMapper kpiReportYearImportMapper;
    @Autowired
    private AttendanceMapper attendanceMapper;
    @Autowired
    private TaskCaculateService taskService;
    @Autowired
    private KpiUserAttendanceMapper kpiUserAttendanceMapper;

    @Override
    @Transactional(readOnly = false)
    public void enable(KpiIndexEnableDto input) {
        kpiReportConfigMapper.updateStatusById(input);
    }

    @Override
    public KpiCalculateReportVO2 report(KpiReportConfigDto input) {
        KpiCalculateReportVO2 output = new KpiCalculateReportVO2();
        KpiReportConfig r = new KpiReportConfig();
        List<KpiReportConfigCopy> reportConfigs = kpiReportConfigCopyMapper.selectList(
                new QueryWrapper<KpiReportConfigCopy>()
                        .eq("id", input.getReportId())
                        .eq("task_child_id", input.getTaskChildId())
        );
        if (!CollectionUtil.isEmpty(reportConfigs)) {
            BeanUtils.copyProperties(reportConfigs.get(0), r);
        }
        if (r.getId() == null) {
            r = kpiReportConfigMapper.selectById(input.getReportId());
        }
        KpiFormulaDto2.FieldListDTO param = JSONObject.parseObject(r.getRange(), KpiFormulaDto2.FieldListDTO.class);
        CalAllDto allo = new CalAllDto();
        allo.setParam(param);
        //final boolean skipall=param.getParamType().equals(FormulaParamEnum.P_19.getType())||param.getParamType().equals(FormulaParamEnum.P_29.getType());
        List<Long> memberList = getMemberList(allo, input.getTaskChildId());

        if (StringUtil.isNullOrEmpty(r.getIndex())) {
            throw new BizException("报表配置有误");
        }
        Long period = kpiAccountTaskChildMapper.getPeriod(input.getTaskChildId());
        Long lastPeriod = getLastPeriod(period);
        Long lasttaskChildId = kpiAccountTaskChildMapper.getTaskChildId(lastPeriod);
        if (lasttaskChildId == null) {
            lasttaskChildId = 0L;
        }
        List<KpiReportConfigIndexDto> indexs = JSONObject.parseArray(r.getIndex(), KpiReportConfigIndexDto.class);
        List<String> lastYearCodes = Linq.of(indexs).where(o -> "Y".equals(o.getLastMonth())).select(o -> o.getCode()).toList();
        List<KpiCalculateConfigDto> caItemNow = new ArrayList<>();
        List<KpiCalculateConfigDto> caItemLast = new ArrayList<>();
        List<KpiAccountUnitCopy> accountUnits;
        List<KpiUserAttendanceCopy> userAttendances;

        if (period.toString().endsWith("13")) {
            accountUnits = kpiAccountUnitCopyMapper.getList(
                    new QueryWrapper<KpiAccountUnitCopy>()
                            .eq("1", "1"));
            userAttendances = kpiUserAttendanceCopyMapper.getList2(period - 1);
        } else {
            accountUnits = kpiAccountUnitCopyMapper.selectList(
                    new QueryWrapper<KpiAccountUnitCopy>()
                            .eq("task_child_id", input.getTaskChildId())
            );
            userAttendances = kpiUserAttendanceCopyMapper.selectList(
                    new QueryWrapper<KpiUserAttendanceCopy>()
                            .eq("task_child_id", input.getTaskChildId())
            );
        }
        List<String> itemIndexNow = Linq.of(indexs).where(c -> "1".equals(c.getType())).select(c -> c.getCode()).toList();
        List<KpiItemCopy> kpiItems = new ArrayList<>();
        if (!itemIndexNow.isEmpty()) {
            kpiItems = kpiItemCopyMapper.selectList(
                    new QueryWrapper<KpiItemCopy>()
                            .eq("task_child_id", input.getTaskChildId())
                            .in("code", itemIndexNow)
                            .select("caliber,code"));
            List<KpiCalculateConfigDto> itemNow = kpiItemCopyMapper.getList(
                    new QueryWrapper<KpiCalculateConfigDto>()
                            .eq("a.period", period)
                            .in("a.code", itemIndexNow)
                            .eq("a.task_child_id", input.getTaskChildId())
                            .ne("a.value", 0));
            caItemNow.addAll(itemNow);
        }
        List<String> caIndexNow = Linq.of(indexs).where(c -> "2".equals(c.getType())).select(c -> c.getCode()).toList();
        if (!caIndexNow.isEmpty()) {
            List<KpiCalculateConfigDto> caNow = kpiCalculateMapper.getList2(
                    new QueryWrapper<KpiCalculateConfigDto>()
                            .eq("period", period)
                            .in("code", caIndexNow)
                            .eq("task_child_id", input.getTaskChildId())
                            .in("imputation_type", Arrays.asList("1,0".split(",")))
                            .ne("value", 0));
            caItemNow.addAll(caNow);
        }
        List<String> itemIndexLast = Linq.of(indexs).where(c -> "1".equals(c.getType()) && "Y".equals(c.getLastMonth())).select(c -> c.getCode()).toList();
        if (!itemIndexLast.isEmpty()) {
            List<KpiCalculateConfigDto> itemLast = kpiItemCopyMapper.getList(
                    new QueryWrapper<KpiCalculateConfigDto>()
                            .eq("a.period", lastPeriod)
                            .in("a.code", itemIndexLast)
                            .eq("a.task_child_id", lasttaskChildId)
                            .ne("a.value", 0));
            caItemLast.addAll(itemLast);
        }
        List<String> caIndexLast = Linq.of(indexs).where(c -> "2".equals(c.getType()) && "Y".equals(c.getLastMonth())).select(c -> c.getCode()).toList();
        if (!caIndexLast.isEmpty()) {
            List<KpiCalculateConfigDto> caLast = kpiCalculateMapper.getList2(
                    new QueryWrapper<KpiCalculateConfigDto>()
                            .eq("period", lastPeriod)
                            .in("code", caIndexLast)
                            .eq("task_child_id", lasttaskChildId)
                            .in("imputation_type", Arrays.asList("1,0".split(",")))
                            .ne("value", 0));
            caItemLast.addAll(caLast);
        }

        List<KpiItemCopy> finalKpiItems = kpiItems;
        List<KpiCalculateConfigDto> finalCaItemNow1 = caItemNow;
        List<KpiCalculateConfigDto> finalCaItemLast1 = caItemLast;

        List<KpiKeyValueVO2> gdNow = Linq.of(caItemNow).where(t -> "4".equals(t.getCaliber())).groupBy(t -> new KpiKeyValueVO2(t.getCode(), t.getName()))
                .select(t -> new KpiKeyValueVO2(t.getKey().getKey(), t.getKey().getName(),
                        Linq.of(finalCaItemNow1).where(x -> t.getKey().getKey().equals(x.getCode()) && t.getKey().getName().equals(x.getName())).select(x -> x.getValue()).sumDecimal())).toList();
        List<KpiKeyValueVO2> gdLast = Linq.of(caItemLast).where(t -> "4".equals(t.getCaliber())).groupBy(t -> new KpiKeyValueVO2(t.getCode(), t.getName()))
                .select(t -> new KpiKeyValueVO2(t.getKey().getKey(), t.getKey().getName(),
                        Linq.of(finalCaItemLast1).where(x -> t.getKey().getKey().equals(x.getCode()) && t.getKey().getName().equals(x.getName())).select(x -> x.getValue()).sumDecimal())).toList();
        caItemNow = Linq.of(caItemNow).where(t -> !"4".equals(t.getCaliber()) && (memberList.contains(t.getDeptId()) || memberList.contains(t.getUserId()))).toList();
        caItemLast = Linq.of(caItemLast).where(t -> !"4".equals(t.getCaliber()) && (memberList.contains(t.getDeptId()) || memberList.contains(t.getUserId()))).toList();

        List<KpiCalculateConfigDto> finalCaItemNow = caItemNow;
        List<KpiCalculateConfigDto> finalCaItemLast = caItemLast;
        List<String> sumCodes = Linq.of(indexs).where(o -> "Y".equals(o.getSum())).select(o -> o.getCode()).toList();
        output.setSum(Linq.of(caItemNow).where(t -> sumCodes.contains(t.getCode())).groupBy(t -> new KpiKeyValueVO2(t.getCode(), t.getName()))
                .select(t -> new KpiKeyValueVO2(t.getKey().getKey(), t.getKey().getName(), Linq.of(finalCaItemNow).where(x -> x.getCode().equals(t.getKey().getKey())).select(x -> x.getValue()).sumDecimal())).toList());

        List<KpiReportConfigImport> imports = kpiReportConfigImportMapper.selectList(
                new QueryWrapper<KpiReportConfigImport>()
                        .eq("task_child_id", input.getTaskChildId())
                        .eq("report_id", input.getReportId())
        );
        JSONArray results = new JSONArray();
        if (!StringUtil.isNullOrEmpty(r.getImpCode())) {
            List<KpiDeptUserIdVO> list = new ArrayList<>();
            List<KpiMemberCopy> members = kpiMemberCopyMapper.selectList(
                    new QueryWrapper<KpiMemberCopy>()
                            .eq("host_code", r.getImpCode())
                            .eq("member_type", "imputation_dept_emp")
                            .eq("task_child_id", input.getTaskChildId())
            );

            for (Long x : Linq.of(caItemNow).select(x -> x.getUserId()).distinct()) {
                List<Long> depts = Linq.of(members).where(m -> m.getMemberId().equals(x)).select(m -> m.getHostId()).distinct().toList();
                if (depts.isEmpty()) {
                    KpiDeptUserIdVO vo = new KpiDeptUserIdVO(0L, x);
                    list.add(vo);
                } else {
                    for (Long dept : depts) {
                        KpiDeptUserIdVO vo = new KpiDeptUserIdVO(dept, x);
                        list.add(vo);
                    }
                }
            }
            List<SysDictItem> dict = kpiCalculateMapper.getDicts(SecurityUtils.getUser().getTenantId());

            list.parallelStream().forEach(t -> {
                boolean add_flag = false;
                List<KpiUserAttendanceCopy> users = Linq.of(userAttendances).where(q -> q.getUserId().equals(t.getUserId())).toList();
                if (!users.isEmpty()) {
                    JSONObject jo = new JSONObject();
                    jo.put("userId", t.getUserId());
                    jo.put("userName", users.get(0).getEmpName());
                    jo.put("userType", String.join(",", Linq.of(finalCaItemNow)
                            .where(x -> t.getUserId().equals(x.getUserId())
                                    && !StringUtil.isNullOrEmpty(x.getUnitType())).select(x -> x.getUserType()).distinct().toList()));

                    KpiAccountUnitCopy dept = Linq.of(accountUnits).firstOrDefault(x -> x.getId().equals(t.getDeptId()));
                    if (dept != null) {
                        jo.put("deptName", dept.getName());
                        SysDictItem s = Linq.of(dict).firstOrDefault(d -> d.getItemValue().equals(dept.getCategoryCode()));
                        if (s != null) {
                            jo.put("groupName", s.getLabel());
                        }
                        SysDictItem dict3 = Linq.of(dict).firstOrDefault(d -> d.getItemValue().equals(dept.getAccountTypeCode()) && "kpi_unit_calc_type".equals(d.getDictType()));
                        if (dict3 != null) {
                            jo.put("unitType", dict3.getLabel());
                        }
                        SysDictItem dict2 = Linq.of(dict).firstOrDefault(d -> d.getItemValue().equals(dept.getAccountUserCode()) && "user_type".equals(d.getDictType()));
                        if (dict2 != null) {
                            jo.put("deptUserType", dict2.getLabel());
                        }
                    }else if (t.getDeptId() == 0){
                        jo.put("deptName","独立核算单元");
                    }
                    jo.put("deptId", t.getDeptId());

                    List<KpiReportConfigImport> import_list = Linq.of(imports).where(i -> users.get(0).getEmpName().equals(i.getUserName())).toList();
                    for (KpiReportConfigIndexDto index : indexs) {
                        KpiItemCopy kpiItemCopy = Linq.of(finalKpiItems).firstOrDefault(m ->
                                m.getCode().equals(index.getCode()) && "4".equals(m.getCaliber()));
                        List<KpiCalculateConfigDto> nows = Linq.of(finalCaItemNow).
                                where(m -> m.getCode().equals(index.getCode()) && t.getUserId().equals(m.getUserId()) &&
                                        (!"1".equals(m.getUserImp()) || t.getDeptId().equals(m.getDeptId()))
                                ).toList();
                        BigDecimal nowValue = new BigDecimal(0);
                        if (kpiItemCopy != null) {
                            KpiKeyValueVO2 vo = Linq.of(gdNow).firstOrDefault(m -> m.getKey().equals(index.getCode()));
                            if (vo != null) {
                                nowValue = vo.getValue();
                            }
                            jo.put(index.getCode(), nowValue);
                        } else {
                            nowValue = Linq.of(nows).select(n -> n.getValue()).sumDecimal();
                            jo.put(index.getCode(), nowValue);
                            if (!index.getType().equals("1")) {
                                jo.put(index.getCode() + "_ids", String.join(",", Linq.of(nows).select(n -> n.getId()).toList()));
                            } else {
                                jo.put(index.getCode() + "_itemid", String.join(",", Linq.of(nows).select(n -> n.getId()).toList()));
                            }
                        }
                        if (!import_list.isEmpty()) {
                            KpiReportConfigImport im = Linq.of(import_list).firstOrDefault(i -> index.getCode().equals(i.getCode()));
                            if (im != null) {
                                jo.put(index.getCode() + "_import", im.getValue());
                            }
                        }
                        if (!nowValue.equals(new BigDecimal(0))) {
                            add_flag = true;
                        }
                    }
                    if (add_flag) {
                        synchronized (results) {
                            results.add(jo);
                        }
                    }
                }
            });
        } else if ("1".equals(r.getCaliber()))//人
        {
            Linq.of(caItemNow).groupBy(t -> t.getUserId()).parallelStream().forEach(t -> {
                List<KpiUserAttendanceCopy> users = Linq.of(userAttendances).where(q -> q.getUserId().equals(t.getKey())).toList();
                if (!users.isEmpty()) {
                    JSONObject jo = new JSONObject();
                    jo.put("userId", users.get(0).getUserId());
                    jo.put("userName", users.get(0).getEmpName());
                    /*List<String> userTypes = Linq.of(users).where(u->!StringUtil.isNullOrEmpty(u.getUserType())).select(u -> u.getUserType()).toList();
                    List<String> groups = Linq.of(users).where(u->!StringUtil.isNullOrEmpty(u.getAccountGroup())).select(u -> u.getAccountGroup()).toList();
                    List<String> attGroups = Linq.of(users).where(u->!StringUtil.isNullOrEmpty(u.getAttendanceGroup())).select(u -> u.getAttendanceGroup()).toList();
                    jo.put("userType",String.join(",", userTypes));
                    jo.put("caGroup",String.join(",", groups));
                    jo.put("empGroup",String.join(",", attGroups));*/

                    List<String> units = Linq.of(users).select(u -> u.getAccountUnit().toString()).toList();
                    List<String> names = Linq.of(users).where(u -> !StringUtil.isNullOrEmpty(u.getAccountUnitName())).select(u -> u.getAccountUnitName()).toList();
                    jo.put("deptId", String.join(",", units));
                    jo.put("deptName", String.join(",", names));

                    KpiCalculateConfigDto first = t.first();
                    jo.put("unitType", first.getUnitType());
                    jo.put("userType", first.getUserType());
                    jo.put("deptUserType", first.getDeptUserType());
                    jo.put("groupName", first.getGroupName());

                    List<KpiReportConfigImport> import_list = Linq.of(imports).where(i -> users.get(0).getEmpName().equals(i.getUserName())).toList();

                    for (KpiReportConfigIndexDto index : indexs) {
                        KpiItemCopy kpiItemCopy = Linq.of(finalKpiItems).firstOrDefault(m -> m.getCode().equals(index.getCode()) && "4".equals(m.getCaliber()));
                        List<KpiCalculateConfigDto> nows = Linq.of(t).where(m -> m.getCode().equals(index.getCode())).toList();
                        if (kpiItemCopy != null) {
                            KpiKeyValueVO2 vo = Linq.of(gdNow).firstOrDefault(m -> m.getKey().equals(index.getCode()));
                            BigDecimal nowValue = new BigDecimal(0);
                            if (vo != null) {
                                nowValue = vo.getValue();
                            }
                            jo.put(index.getCode(), nowValue);
                        } else {
                            jo.put(index.getCode(), Linq.of(nows).select(n -> n.getValue()).sumDecimal());
                            if (!index.getType().equals("1")) {
                                jo.put(index.getCode() + "_ids", String.join(",", Linq.of(nows).select(n -> n.getId()).toList()));
                            } else {
                                jo.put(index.getCode() + "_itemid", String.join(",", Linq.of(nows).select(n -> n.getId()).toList()));
                            }
                        }
                        if (!import_list.isEmpty()) {
                            KpiReportConfigImport im = Linq.of(import_list).firstOrDefault(i -> index.getCode().equals(i.getCode()));
                            if (im != null) {
                                jo.put(index.getCode() + "_import", im.getValue());
                            }
                        }

                        if (index.getLastMonth().equals("Y")) {
                            List<KpiCalculateConfigDto> lasts = Linq.of(finalCaItemLast).where(m -> m.getCode().equals(index.getCode()) && m.getUserId().equals(t.getKey())).toList();
                            if (kpiItemCopy != null) {
                                KpiKeyValueVO2 vo = Linq.of(gdLast).firstOrDefault(m -> m.getKey().equals(index.getCode()));
                                BigDecimal lastValue = new BigDecimal(0);
                                if (vo != null) {
                                    lastValue = vo.getValue();
                                }
                                jo.put(index.getCode() + "_last", lastValue);
                                jo.put(index.getCode() + "_sub", ((BigDecimal) jo.get(index.getCode())).subtract(lastValue));
                            } else {
                                jo.put(index.getCode() + "_last", Linq.of(lasts).select(n -> n.getValue()).sumDecimal());
                                jo.put(index.getCode() + "_sub", ((BigDecimal) jo.get(index.getCode())).subtract((BigDecimal) jo.get(index.getCode() + "_last")));
                            }
                        }
                    }
                    synchronized (results) {
                        results.add(jo);
                    }
                }
            });
        } else {//科室
            if (!Linq.of(indexs).any(t -> !"1".equals(t.getType()))) {
                List<KpiItemCopy> list = Linq.of(kpiItems).where(t -> Linq.of(indexs).select(m -> m.getCode()).toList().contains(t.getCode())).toList();
                if (!Linq.of(list).any(t -> !"4".equals(t.getCaliber()))) {
                    KpiCalculateConfigDto dto = new KpiCalculateConfigDto();
                    dto.setDeptId(-1L);
                    dto.setCode("");
                    dto.setName("");
                    dto.setValue(new BigDecimal(0));
                    caItemNow.add(dto);
                }
            }
            //List<SysDictItem> dicts = kpiReportConfigMapper.getDicts(SecurityUtils.getUser().getTenantId());
            Linq.of(caItemNow).groupBy(t -> t.getDeptId()).parallelStream().forEach(t -> {
                KpiAccountUnitCopy entity = Linq.of(accountUnits).firstOrDefault(q -> q.getId().equals(t.getKey()));
                if (entity != null || t.getKey() == -1L) {
                    JSONObject jo = new JSONObject();
                    if (entity != null) {
                        jo.put("deptId", entity.getId());
                        jo.put("deptName", entity.getName());
                        /*SysDictItem dict = Linq.of(dicts).firstOrDefault(d -> d.getItemValue().equals(entity.getCategoryCode()) && "PMC_kpi_calculate_grouping".equals(d.getDictType()));
                        if (dict != null) {
                            jo.put("caGroup", dict.getLabel());
                        }
                        SysDictItem dict2 = Linq.of(dicts).firstOrDefault(d -> d.getItemValue().equals(entity.getAccountUserCode()) && "user_type".equals(d.getDictType()));
                        if (dict2 != null) {
                            jo.put("userType", dict2.getLabel());
                        }
                        SysDictItem dict3 = Linq.of(dicts).firstOrDefault(d -> d.getItemValue().equals(entity.getAccountTypeCode()) && "kpi_unit_calc_type".equals(d.getDictType()));
                        if (dict3 != null) {
                            jo.put("unitType", dict3.getLabel());
                        }*/
                        KpiCalculateConfigDto first = t.first();
                        jo.put("unitType", first.getUnitType());
                        jo.put("userType", first.getUserType());
                        jo.put("deptUserType", first.getDeptUserType());
                        jo.put("groupName", first.getGroupName());
                    }

                    List<KpiReportConfigImport> import_list = Linq.of(imports).where(i -> entity.getName().equals(i.getDeptName())).toList();
                    for (KpiReportConfigIndexDto index : indexs) {
                        KpiItemCopy kpiItemCopy = Linq.of(finalKpiItems).firstOrDefault(m -> m.getCode().equals(index.getCode()) && "4".equals(m.getCaliber()));
                        List<KpiCalculateConfigDto> nows = Linq.of(t).where(m -> m.getCode().equals(index.getCode())).toList();
                        if (kpiItemCopy != null) {
                            KpiKeyValueVO2 vo = Linq.of(gdNow).firstOrDefault(m -> m.getKey().equals(index.getCode()));
                            BigDecimal nowValue = new BigDecimal(0);
                            if (vo != null) {
                                nowValue = vo.getValue();
                            }
                            jo.put(index.getCode(), nowValue);
                        } else {
                            jo.put(index.getCode(), Linq.of(nows).select(n -> n.getValue()).sumDecimal());
                            if (!index.getType().equals("1")) {
                                jo.put(index.getCode() + "_ids", String.join(",", Linq.of(nows).select(n -> n.getId()).toList()));
                            } else {
                                jo.put(index.getCode() + "_itemid", String.join(",", Linq.of(nows).select(n -> n.getId()).toList()));
                            }
                        }
                        if (!import_list.isEmpty()) {
                            KpiReportConfigImport im = Linq.of(import_list).firstOrDefault(i -> index.getCode().equals(i.getCode()));
                            if (im != null) {
                                jo.put(index.getCode() + "_import", im.getValue());
                            }
                        }
                        if (index.getLastMonth().equals("Y")) {
                            List<KpiCalculateConfigDto> lasts = Linq.of(finalCaItemLast).where(m -> m.getCode().equals(index.getCode()) && m.getDeptId().equals(t.getKey())).toList();
                            if (kpiItemCopy != null) {
                                KpiKeyValueVO2 vo = Linq.of(gdLast).firstOrDefault(m -> m.getKey().equals(index.getCode()));
                                BigDecimal lastValue = new BigDecimal(0);
                                if (vo != null) {
                                    lastValue = vo.getValue();
                                }
                                jo.put(index.getCode() + "_last", lastValue);
                            } else {
                                jo.put(index.getCode() + "_last", Linq.of(lasts).select(n -> n.getValue()).sumDecimal());
                                jo.put(index.getCode() + "_sub", ((BigDecimal) jo.get(index.getCode())).subtract((BigDecimal) jo.get(index.getCode() + "_last")));
                            }
                        }
                    }
                    synchronized (results) {
                        results.add(jo);
                    }
                }
            });
        }
        output.setResults(results);

        List<KpiKeyValueVO> head = new ArrayList<>();
        for (KpiReportConfigIndexDto index : indexs) {
            if ("Y".equals(index.getLastMonth())) {
                head.add(new KpiKeyValueVO(index.getCode(), index.getHeadName() + "(本期)"));
                head.add(new KpiKeyValueVO(index.getCode() + "_last", index.getHeadName() + "(上期)"));
                head.add(new KpiKeyValueVO(index.getCode() + "_sub", index.getHeadName() + "(增幅)"));
            } else {
                head.add(new KpiKeyValueVO(index.getCode(), index.getHeadName()));
            }
        }
        output.setHead(head);
        return output;
    }

    @Override
    public List<KpiReportYearImport> yearReport(KpiReportYearDto input) {
        QueryWrapper<KpiReportYearImport> qw = new QueryWrapper<KpiReportYearImport>()
                .eq("task_child_id", input.getTaskChildId())
                .eq("report_id", input.getReportId());
        if (input.getDeptId()!=null){
            qw.eq("dept_id",input.getDeptId());
        }
        return kpiReportYearImportMapper.selectList(qw);
    }

    @Override
    public List<KpiReportYearImport> yearPowerReport(KpiReportYearDto input) {
        QueryWrapper<KpiReportYearImport> qw = new QueryWrapper<KpiReportYearImport>()
                .eq("task_child_id", input.getTaskChildId())
                .eq("report_id", input.getReportId());
        if (input.getDeptId()!=null){
            qw.eq("dept_id",input.getDeptId());
        }
        List<Long> depts = new ArrayList<>();
        //权限
        if (!SecurityUtils.getUser().getAdminFlag()) {
            List<KpiReportConfigPower> list = kpiReportConfigPowerMapper.selectList(
                    new QueryWrapper<KpiReportConfigPower>()
                            .eq("user_id", SecurityUtils.getUser().getId())
                            .eq("report_id", input.getReportId())
                            .eq("type", "3")
            );
            if (!CollectionUtil.isEmpty(list)) {
                depts.addAll(Linq.of(list).select(x -> x.getDeptId()).toList());
            }
            List<GrantUnit> list1 = grantUnitService.list(new QueryWrapper<GrantUnit>().like("leader_ids", SecurityUtils.getUser().getId()).select("ks_unit_ids", "ks_unit_ids_non_staff"));
            for (GrantUnit grantUnit : list1) {
                if (grantUnit != null) {
                    if (!StringUtil.isNullOrEmpty(grantUnit.getKsUnitIdsNonStaff())) {
                        for (String s : grantUnit.getKsUnitIdsNonStaff().split(",")) {
                            if (!StringUtil.isNullOrEmpty(s)) {
                                depts.add(Long.parseLong(s));
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(grantUnit.getKsUnitIds())) {
                        for (String s : grantUnit.getKsUnitIds().split(",")) {
                            if (!StringUtil.isNullOrEmpty(s)) {
                                depts.add(Long.parseLong(s));
                            }
                        }
                    }
                }
            }
            depts = depts.stream().distinct().collect(Collectors.toList());
        } else {
            depts.add(-200L);
        }
        if (CollectionUtil.isEmpty(depts)) {
            depts.add(-1L);
        }
        if (!CollectionUtil.isEmpty(depts)) {
            if (!Linq.of(depts).any(t -> t.equals(-200L))) {
                qw.in("dept_id", depts);
            }
        }

        return kpiReportYearImportMapper.selectList(qw);
    }

    public Long getLastPeriod(Long period) {
        if (((period - 1) + "").endsWith("00")) {
            String substring = period.toString().substring(0, 4);

            return Long.parseLong(Long.parseLong(substring) - 1 + "12");
        } else {
            return period - 1;
        }
    }

    @Override
    public KpiCalculateReportVO reportId(KpiCalculateReportDTO2 input) {
        KpiCalculateReportVO output = new KpiCalculateReportVO();
        List<KpiKeyValueVO> li = new ArrayList<>();
        KpiAccountTaskChild task = kpiAccountTaskChildMapper.selectById(input.getTaskChildId());
        QueryWrapper<KpiCalculate> qw = new QueryWrapper<KpiCalculate>()
                .eq("period", task.getPeriod())
                .eq("task_child_id", input.getTaskChildId())
                .in("id", Arrays.asList(input.getId().split(",")));
        List<KpiCalculate> calculates = kpiCalculateMapper.selectList(qw);
        calculates.forEach(r -> {
            li.addAll(Linq.of(JSON.parseObject(r.getResultJson(), KpiFormulaDto2.class).getFieldList()).select(t -> {
                KpiKeyValueVO dto = new KpiKeyValueVO();
                dto.setKey(t.getCode());
                dto.setValue(t.getFieldName());
                return dto;
            }).toList());
        });
        output.setCalculates(calculates);
        output.setSum(Linq.of(calculates).select(r -> r.getValue()).sumDecimal());
        output.setHead(Linq.of(li).stream().distinct().collect(Collectors.toList()));
        return output;
    }

    @Override
    public KpiCalculateReportVO2 reportSecond(KpiReportConfigDto input) {
        KpiCalculateReportVO2 output = new KpiCalculateReportVO2();
        KpiReportConfigCopy r = new KpiReportConfigCopy();
        List<KpiConfig> configs = new ArrayList<>();
        List<KpiReportConfigCopy> reportConfigs;
        if (input.getTaskChildId() != null) {//预览
            reportConfigs = new ArrayList<>();
            input.setTaskChildIds(new ArrayList<>());
            input.getTaskChildIds().add(input.getTaskChildId());
            KpiReportConfig kpiReportConfig = kpiReportConfigMapper.selectById(input.getReportId());
            KpiReportConfigCopy ri = new KpiReportConfigCopy();
            BeanUtils.copyProperties(kpiReportConfig, ri);
            reportConfigs.add(ri);
            KpiAccountTaskChild taskChild = kpiAccountTaskChildMapper.selectById(input.getTaskChildId());
            input.setPeriodsBegin(taskChild.getPeriod());
            input.setPeriodsEnd(taskChild.getPeriod());
        } else {
            configs = kpiConfigMapper.getList(
                    new QueryWrapper<KpiConfig>()
                            .eq("a.issued_flag", "Y")
                            .eq("b.send_flag", "Y")
                            .ge("a.period", input.getPeriodsBegin())
                            .le("a.period", input.getPeriodsEnd())
            );
            if (configs.isEmpty()) {
                throw new BizException("周期内数据未锁定并且下发");
            }
            input.setTaskChildIds(Linq.of(configs)
                    .where(x->x.getPeriod().toString().endsWith("01")
                            ||x.getPeriod().toString().endsWith("02")
                            ||x.getPeriod().toString().endsWith("03")
                            ||x.getPeriod().toString().endsWith("04")
                            ||x.getPeriod().toString().endsWith("05")
                            ||x.getPeriod().toString().endsWith("06")
                            ||x.getPeriod().toString().endsWith("07")
                            ||x.getPeriod().toString().endsWith("08")
                            ||x.getPeriod().toString().endsWith("09")
                            ||x.getPeriod().toString().endsWith("10")
                            ||x.getPeriod().toString().endsWith("11")
                            ||x.getPeriod().toString().endsWith("12"))
                    .select(x -> x.getTaskChildId()).toList());
            if (input.getTaskChildIds().isEmpty()) {
                return new KpiCalculateReportVO2();
            }
            reportConfigs = kpiReportConfigCopyMapper.selectList(
                    new QueryWrapper<KpiReportConfigCopy>()
                            .eq("id", input.getReportId())
                            .in("task_child_id", input.getTaskChildIds())
            );
            if (reportConfigs.isEmpty()) {
                reportConfigs = kpiReportConfigMapper.getList(
                        new QueryWrapper<KpiReportConfig>()
                                .eq("id", input.getReportId())
                                .in("task_child_id", input.getTaskChildIds())
                );
                if (reportConfigs.isEmpty()) {
                    throw new BizException("报表未备份");
                }
            }
        }
        if (!CollectionUtil.isEmpty(reportConfigs)) {
            BeanUtils.copyProperties(reportConfigs.get(0), r);
        }
        List<KpiReportConfigIndexDto> indexs = new ArrayList<>();
        for (KpiReportConfigCopy reportConfig : Linq.of(reportConfigs).orderByDescending(t -> t.getTaskChildId())) {
            List<KpiReportConfigIndexDto> kpiReportConfigIndexDto = JSONObject.parseArray(reportConfig.getIndex(), KpiReportConfigIndexDto.class);
            for (KpiReportConfigIndexDto indexDto : kpiReportConfigIndexDto) {
                if (Linq.of(indexs).count(t -> t.getCode().equals(indexDto.getCode())) == 0) {
                    indexs.add(indexDto);
                }
            }
        }
        if (indexs.isEmpty()) {
            throw new BizException("报表配置有误");
        }
        List<KpiCalculateConfigDto> caItemNow = new ArrayList<>();
        List<Long> depts = new ArrayList<>();
        //权限
        if (!SecurityUtils.getUser().getAdminFlag()) {
            List<KpiReportConfigPower> list = kpiReportConfigPowerMapper.selectList(
                    new QueryWrapper<KpiReportConfigPower>()
                            .eq("user_id", SecurityUtils.getUser().getId())
                            .eq("report_id", input.getReportId())
                            .eq("type", "3")
            );
            if (!CollectionUtil.isEmpty(list)) {
                depts.addAll(Linq.of(list).select(x -> x.getDeptId()).toList());
            }
            List<GrantUnit> list1 = grantUnitService.list(new QueryWrapper<GrantUnit>().like("leader_ids", SecurityUtils.getUser().getId()).select("ks_unit_ids", "ks_unit_ids_non_staff"));
            for (GrantUnit grantUnit : list1) {
                if (grantUnit != null) {
                    if (!StringUtil.isNullOrEmpty(grantUnit.getKsUnitIdsNonStaff())) {
                        for (String s : grantUnit.getKsUnitIdsNonStaff().split(",")) {
                            if (!StringUtil.isNullOrEmpty(s)) {
                                depts.add(Long.parseLong(s));
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(grantUnit.getKsUnitIds())) {
                        for (String s : grantUnit.getKsUnitIds().split(",")) {
                            if (!StringUtil.isNullOrEmpty(s)) {
                                depts.add(Long.parseLong(s));
                            }
                        }
                    }
                }
            }
            depts = depts.stream().distinct().collect(Collectors.toList());
        } else {
            depts.add(-200L);
        }
        if (CollectionUtil.isEmpty(depts)) {
            depts.add(-1L);
        }
        List<String> itemIndexNow = Linq.of(indexs).where(c -> "1".equals(c.getType())).select(c -> c.getCode()).toList();
        if (!itemIndexNow.isEmpty()) {
            QueryWrapper<KpiCalculateConfigDto> qw = new QueryWrapper<KpiCalculateConfigDto>()
                    .in("a.code", itemIndexNow)
                    .in("a.task_child_id", input.getTaskChildIds())
                    .ne("a.value", 0)
                    .ge("a.period", input.getPeriodsBegin())
                    .le("a.period", input.getPeriodsEnd());
            if (!CollectionUtil.isEmpty(depts)) {
                if (!Linq.of(depts).any(t -> t.equals(-200L))) {
                    qw.in("dept_id", depts);
                }
            }
            qw.apply("(user_id !=0 and user_id is not null )");
            if (!StringUtil.isNullOrEmpty(r.getEstablish())) {
                qw.eq("establish", r.getEstablish());
            }
            List<KpiCalculateConfigDto> itemNow = kpiItemCopyMapper.getList(qw);
            caItemNow.addAll(itemNow);
        }
        List<String> caIndexNow = Linq.of(indexs).where(c -> "2".equals(c.getType())).select(c -> c.getCode()).toList();
        if (!caIndexNow.isEmpty()) {
            QueryWrapper<KpiCalculateConfigDto> qw = new QueryWrapper<KpiCalculateConfigDto>()
                    .in("code", caIndexNow)
                    .in("task_child_id", input.getTaskChildIds())
                    .ne("value", 0)
                    .ge("period", input.getPeriodsBegin())
                    .le("period", input.getPeriodsEnd());
            if ("2".equals(r.getCaliber()) && "1".equals(r.getImpType())) {
                qw.eq("imputation_type", "2");
            } else {
                qw.in("imputation_type", Arrays.asList("1,0".split(",")));
            }
            if (!CollectionUtil.isEmpty(depts)) {
                if (!Linq.of(depts).any(t -> t.equals(-200L))) {
                    qw.in("dept_id", depts);
                }
            }
            if (!StringUtil.isNullOrEmpty(r.getEstablish())) {
                qw.eq("establish", r.getEstablish());
            }
            List<KpiCalculateConfigDto> caNow;
            if ("3".equals(r.getCaliber()) || "4".equals(r.getCaliber())) {
                qw.ne("value", 0);
                caNow = kpiCalculateMapper.getList3(qw);
            } else {
                caNow = kpiCalculateMapper.getList2(qw);
            }
            caItemNow.addAll(caNow);
        }

        //List<KpiCalculateConfigDto> finalCaItemNow = caItemNow;
        JSONArray results = new JSONArray();
        if ("1".equals(r.getCaliber())) {
            List<KpiReportConfigCopy> finalReportConfigs1 = reportConfigs;
            Linq.of(caItemNow).groupBy(t -> new KpiDeptUserIdVO(t.getDeptId(), t.getDeptName(), t.getUserId(), t.getUserName(), t.getPeriod()))
                    .parallelStream().forEach(t -> {
                        JSONObject jo = new JSONObject();
                        KpiReportConfigCopy config = Linq.of(finalReportConfigs1).firstOrDefault(conf -> t.getKey().getPeriod().equals(conf.getPeriod()));
                        if (input.getTaskChildId() != null) {
                            jo.put("taskChildId", input.getTaskChildId());
                        } else if (config != null) {
                            jo.put("taskChildId", config.getTaskChildId());
                        }
                        jo.put("period", t.getKey().getPeriod());
                        jo.put("userId", t.getKey().getUserId());
                        jo.put("userName", t.getKey().getUserName());
                        jo.put("deptId", t.getKey().getDeptId());
                        jo.put("deptName", t.getKey().getDeptName());
                        KpiCalculateConfigDto first = t.first();
                        jo.put("unitType", first.getUnitType());
                        jo.put("userType", first.getUserType());
                        jo.put("deptUserType", first.getDeptUserType());
                        jo.put("groupName", first.getGroupName());
                        jo.put("resultJson", first.getResultJson());

//                List<KpiCalculateConfigDto> all_in_list = Linq.of(caItemNow).where(m ->
//                        t.getKey().getPeriod().equals(m.getPeriod()) &&
//                                t.getKey().getUserId().equals(m.getUserId()) &&
//                                t.getKey().getDeptId().equals(m.getDeptId())).toList();
                        for (KpiReportConfigIndexDto index : indexs) {
                            List<KpiCalculateConfigDto> nows = Linq.of(t).where(m ->
                                    m.getCode().equals(index.getCode())).toList();
                            jo.put(index.getCode(), Linq.of(nows).select(n -> n.getValue()).sumDecimal());
                            //jo.put(index.getCode() + "_ids", String.join(",",Linq.of(nows).select(n->n.getId()).toList()));
                        }
                        synchronized (results) {
                            results.add(jo);
                        }
                    });
        } else if (("2".equals(r.getCaliber()) && "2".equals(r.getImpType()))) {
            List<KpiReportConfigCopy> finalReportConfigs = reportConfigs;
            Linq.of(caItemNow).groupBy(t -> new KpiDeptUserIdVO(t.getDeptId(), t.getDeptName(), t.getPeriod()))
                    .parallelStream().forEach(t -> {
                        JSONObject jo = new JSONObject();
                        KpiReportConfigCopy config = Linq.of(finalReportConfigs).firstOrDefault(conf -> t.getKey().getPeriod().equals(conf.getPeriod()));
                        if (input.getTaskChildId() != null) {
                            jo.put("taskChildId", input.getTaskChildId());
                        } else if (config != null) {
                            jo.put("taskChildId", config.getTaskChildId());
                        }
                        jo.put("deptId", t.getKey().getDeptId());
                        jo.put("deptName", t.getKey().getDeptName());
                        jo.put("period", t.getKey().getPeriod());
                        KpiCalculateConfigDto first = t.first();
                        jo.put("unitType", first.getUnitType());
                        jo.put("userType", first.getUserType());
                        jo.put("deptUserType", first.getDeptUserType());
                        jo.put("groupName", first.getGroupName());
                        jo.put("resultJson", first.getResultJson());

//                List<KpiCalculateConfigDto> all_in_list = Linq.of(caItemNow).where(m ->
//                        t.getKey().getPeriod().equals(m.getPeriod()) &&
//                                t.getKey().getDeptId().equals(m.getDeptId())).toList();
                        for (KpiReportConfigIndexDto index : indexs) {
                            List<KpiCalculateConfigDto> nows = Linq.of(t).where(m -> m.getCode().equals(index.getCode())).toList();
                            jo.put(index.getCode(), Linq.of(nows).select(n -> n.getValue()).sumDecimal());
                            jo.put(index.getCode() + "_ids", String.join(",", Linq.of(nows).select(n -> n.getId()).toList()));
                            jo.put(index.getCode() + "_impcodes", String.join(",", Linq.of(nows).select(n -> n.getImputationCode()).toList()));
                        }
                        synchronized (results) {
                            results.add(jo);
                        }
                    });
        } else if ("2".equals(r.getCaliber()) && "1".equals(r.getImpType())) {
            List<KpiReportConfigCopy> finalReportConfigs2 = reportConfigs;
            Linq.of(caItemNow).groupBy(t -> new KpiDeptUserIdVO(t.getDeptId(), t.getDeptName(), t.getUserId(), t.getUserName(), t.getPeriod()))
                    .parallelStream().forEach(t -> {
                        JSONObject jo = new JSONObject();
                        KpiReportConfigCopy config = Linq.of(finalReportConfigs2).firstOrDefault(conf -> t.getKey().getPeriod().equals(conf.getPeriod()));
                        if (input.getTaskChildId() != null) {
                            jo.put("taskChildId", input.getTaskChildId());
                        } else if (config != null) {
                            jo.put("taskChildId", config.getTaskChildId());
                        }
                        jo.put("period", t.getKey().getPeriod());
                        jo.put("userId", t.getKey().getUserId());
                        jo.put("userName", t.getKey().getUserName());
                        jo.put("deptId", t.getKey().getDeptId());
                        jo.put("deptName", t.getKey().getDeptName());
                        KpiCalculateConfigDto first = t.first();
                        jo.put("unitType", first.getUnitType());
                        jo.put("userType", first.getUserType());
                        jo.put("deptUserType", first.getDeptUserType());
                        jo.put("groupName", first.getGroupName());

                        for (KpiReportConfigIndexDto index : indexs) {
                            List<KpiCalculateConfigDto> nows = Linq.of(t).where(m -> m.getCode().equals(index.getCode())).toList();
                            jo.put(index.getCode(), Linq.of(nows).select(n -> n.getValue()).sumDecimal());
                            jo.put(index.getCode() + "_ids", String.join(",", Linq.of(nows).select(n -> n.getId()).toList()));
                        }
                        synchronized (results) {
                            results.add(jo);
                        }
                    });
        } else if ("3".equals(r.getCaliber()) || "4".equals(r.getCaliber())) {
            caItemNow.parallelStream().forEach(t -> {
                synchronized (results) {
                    results.add(JSONObject.parseObject(JSONObject.toJSONString(t)));
                }
            });
        }
        output.setResults(results);

        List<KpiKeyValueVO> head = new ArrayList<>();

        if ("3".equals(r.getCaliber()) || "4".equals(r.getCaliber())) {
            caItemNow.forEach(ca -> {
                if (!"1".equals(ca.getImputationType()) && !StringUtil.isNullOrEmpty(ca.getResultJson())) {
                    head.addAll(Linq.of(JSON.parseObject(ca.getResultJson(), KpiFormulaDto2.class).getFieldList()).select(t -> {
                        KpiKeyValueVO dto = new KpiKeyValueVO();
                        dto.setKey(t.getCode());
                        dto.setValue(t.getFieldName());
                        return dto;
                    }).toList());
                }
            });
        } else {
            for (KpiReportConfigIndexDto index : indexs) {
                head.add(new KpiKeyValueVO(index.getCode(), index.getHeadName()));
            }
        }
        List<String> sumCodes = Linq.of(indexs).where(o -> "Y".equals(o.getSum())).select(o -> o.getCode()).toList();
        output.setSum(Linq.of(caItemNow).where(t -> sumCodes.contains(t.getCode()))
                .groupBy(t -> new KpiKeyValueVO2(t.getCode(), t.getName()))
                .select(t -> new KpiKeyValueVO2(t.getKey().getKey(), t.getKey().getName(), Linq.of(caItemNow).where(x -> x.getCode().equals(t.getKey().getKey())).select(x -> x.getValue()).sumDecimal())).toList());

        output.setHead(Linq.of(head).stream().distinct().collect(Collectors.toList()));
        return output;
    }

    @Override
    @Transactional(readOnly = false)
    public void powerEd(KpiReportConfigPowerDto input) {
        kpiReportConfigPowerMapper.delete(
                new QueryWrapper<KpiReportConfigPower>()
                        .eq("report_id", input.getReportId()));

        List<KpiReportConfigPower> powers = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(input.getUserIds())) {
            for (String userId : input.getUserIds().split(",")) {
                KpiReportConfigPower power = new KpiReportConfigPower();
                power.setUserId(Long.parseLong(userId));
                power.setType("1");
                power.setReportId(input.getReportId());
                power.setTenantId(SecurityUtils.getUser().getTenantId());
                powers.add(power);
            }
        }
        if (!StringUtil.isNullOrEmpty(input.getGroupCodes())) {
            for (String groupCode : input.getGroupCodes().split(",")) {
                KpiReportConfigPower power = new KpiReportConfigPower();
                power.setGroupCode(groupCode);
                power.setType("2");
                power.setReportId(input.getReportId());
                power.setTenantId(SecurityUtils.getUser().getTenantId());
                powers.add(power);
            }
        }
        if (!CollectionUtil.isEmpty(input.getList())) {
            for (UserDeptDto userDept : input.getList()) {
                KpiReportConfigPower power = new KpiReportConfigPower();
                power.setUserId(userDept.getUserId());
                power.setDeptId(userDept.getDeptId());
                power.setType("3");
                power.setReportId(input.getReportId());
                power.setTenantId(SecurityUtils.getUser().getTenantId());
                powers.add(power);
            }
        }
        if (!powers.isEmpty()) {
            kpiReportConfigPowerMapper.insertBatchSomeColumn(powers);
        }
    }

    @Override
    public List<KpiReportConfigPowerListDTO> powerDetail(Long reportId) {
        List<KpiReportConfigPowerListDTO> list = new ArrayList<>();
        List<KpiReportConfigPower> powers = kpiReportConfigPowerMapper.selectList(
                new QueryWrapper<KpiReportConfigPower>()
                        .eq("report_id", reportId)
        );
        List<SysUser> users = kpiCalculateMapper.getUsers(SecurityUtils.getUser().getTenantId());
        List<KpiAccountUnit> depts = kpiAccountUnitMapper.selectList(
                new QueryWrapper<KpiAccountUnit>()
                        .eq("tenant_id", SecurityUtils.getUser().getTenantId())
                        .eq("busi_type", "1")
        );
        List<KpiCategory> kpiCategories = kpiCategoryMapper.selectList(
                new QueryWrapper<KpiCategory>()
                        .eq("category_type", "user_group")
        );
        for (KpiReportConfigPower power : powers) {
            KpiReportConfigPowerListDTO dto = new KpiReportConfigPowerListDTO();
            dto.setId(power.getId());
            dto.setType(power.getType());
            dto.setUserId(power.getUserId());
            dto.setGroupCode(power.getGroupCode());
            dto.setDeptId(power.getDeptId());
            dto.setReportId(power.getReportId());
            SysUser user = Linq.of(users).firstOrDefault(x -> x.getUserId().equals(power.getUserId()));
            if (user != null) {
                dto.setUserName(user.getName());
            }
            KpiAccountUnit dept = Linq.of(depts).firstOrDefault(x -> x.getId().equals(power.getDeptId()));
            if (dept != null) {
                dto.setDeptName(dept.getName());
            }
            KpiCategory kpiCategory = Linq.of(kpiCategories).firstOrDefault(x -> x.getCategoryCode().equals(power.getGroupCode()));
            if (kpiCategory != null) {
                dto.setGroupName(kpiCategory.getCategoryName());
            }

            list.add(dto);
        }

        return list;
    }

    @Override
    public List<KpiReportConfig> powerLeft() {
        return kpiReportConfigPowerMapper.getRportConfigs(SecurityUtils.getUser());
    }

    @Override
    public List<KpiConfig> sendList(Long input) {
        //1 锁定， 2 下发
        QueryWrapper<KpiConfig> qw = new QueryWrapper<>();
        if (input == 1) {
            qw.eq("a.issued_flag", "Y");
        } else if (input == 2) {
            qw.eq("b.send_flag", "Y");
        }
        return Linq.of(kpiConfigMapper.getList(qw)).where(r->!r.getPeriod().toString().endsWith("13")).toList();
    }

/*    @Override
    @Transactional(readOnly = false)
    public void importData(Long taskChildId, Long reportId, List<Map<Integer, String>> list) {
        KpiReportConfigCopy config = kpiReportConfigCopyMapper.selectOne(
                new QueryWrapper<KpiReportConfigCopy>()
                        .eq("task_child_id", taskChildId)
                        .eq("id", reportId)
        );
        if (config == null){
            throw new BizException("未找到报表");
        }
        kpiReportConfigImportMapper.delete(
                new QueryWrapper<KpiReportConfigImport>()
                        .eq("task_child_id", taskChildId)
                        .eq("report_id", reportId));

        List<KpiReportConfigIndexDto> indexs = JSONObject.parseArray(config.getIndex(), KpiReportConfigIndexDto.class);

        Map<Integer, String> top = list.get(0);
        List<KpiReportConfigImport> imports = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            String dept=null;
            String user =null;
            String out =null;
            for (int j = 0; j < list.size(); j++) {
                String name = top.get(j);
                while (dept == null && user == null && out ==null) {
                    if ("核算单元".equals(name)) {
                        dept = list.get(i).get(j);
                        break;
                    } else if ("核算人员".equals(name)) {
                        user = list.get(i).get(j);
                        break;
                    } else if ("摊出核算单元".equals(name)) {
                        out = list.get(i).get(j);
                        break;
                    }
                }
            }

            String finalUser = user;
            String finalDept = dept;
            int finalI = i;
            top.forEach((key, value) -> {
                for (KpiReportConfigIndexDto index : indexs) {
                    if (!StringUtil.isNullOrEmpty(value)
                            && (value.replace("\n","").replace(" ","").equals(index.getName())
                            || value.replace("\n","").replace(" ","").equals(index.getHeadName()))) {
                        KpiReportConfigImport entity = new KpiReportConfigImport();
                        entity.setTenantId(SecurityUtils.getUser().getTenantId());
                        entity.setTaskChildId(taskChildId);
                        entity.setReportId(reportId);
                        entity.setCreateDate(new Date());
                        entity.setPeriod(config.getPeriod());
                        entity.setName(index.getName());
                        entity.setCode(index.getCode());
                        entity.setUserName(finalUser);
                        entity.setDeptName(finalDept);

                        String s = list.get(finalI).get(key);
                        if (!StringUtil.isNullOrEmpty(s)) {
                            entity.setValue(new BigDecimal(s));
                        }
                        if (entity.getValue() != null) {
                            imports.add(entity);
                        }
                    }
                }
            });
        }
        if (!imports.isEmpty()){
            kpiReportConfigImportMapper.insertBatchSomeColumn(imports);
        }
    }*/

    @Override
    @Transactional(readOnly = false)
    public List<KpiReportConfigImport> importData(Long taskChildId, Long reportId, List<Map<Integer, String>> list, boolean isYearImport,Long period) {
        KpiReportConfigCopy config = null;
        if (!isYearImport) {
            config = kpiReportConfigCopyMapper.selectOne(
                    new QueryWrapper<KpiReportConfigCopy>()
                            .eq("task_child_id", taskChildId)
                            .eq("id", reportId)
            );
        }
        if (config == null){
            config = new KpiReportConfigCopy();
            KpiReportConfig kpiReportConfig = kpiReportConfigMapper.selectById(reportId);
            BeanUtil.copyProperties(kpiReportConfig,config);
            config.setPeriod(period);
            config.setTaskChildId(taskChildId);
        }
        /*if (config == null) {
            throw new BizException("报表不存在");
        }
*/
        KpiAccountTaskChild taskChild = kpiAccountTaskChildMapper.selectById(taskChildId);
        kpiReportConfigImportMapper.delete(
                new QueryWrapper<KpiReportConfigImport>()
                        .eq("task_child_id", taskChildId)
                        .eq("report_id", reportId));
        kpiReportYearImportMapper.delete(
                new QueryWrapper<KpiReportYearImport>()
                        .eq("task_child_id", taskChildId)
                        .eq("report_id", reportId));
        List<SysUser> users;
        List<KpiAccountUnit> units;
        List<UserIdAndDeptId> userDept;
        List<String> not_exist = new ArrayList<>();
        List<KpiReportYearImport> yearImports = new ArrayList<>();
        if (isYearImport) {
            users = kpiCalculateMapper.getUsers(SecurityUtils.getUser().getTenantId());
            units = kpiAccountUnitMapper.selectList(
                    new QueryWrapper<KpiAccountUnit>()
                            .eq("del_flag", "0")
                            .eq("status", "0")
                            .eq("tenant_id", SecurityUtils.getUser().getTenantId())
                            .eq("busi_type", "1")
            );
            userDept = kpiUserAttendanceMapper.getUserDept();
        } else {
            users = new ArrayList<>();
            units = new ArrayList<>();
            userDept = new ArrayList<>();
        }
        List<KpiReportConfigIndexDto> indexs = JSONObject.parseArray(config.getIndex(), KpiReportConfigIndexDto.class);
        Map<Integer, String> top = list.get(0);
        List<KpiReportConfigImport> imports = new ArrayList<>();
        List<Attendance> atts = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            String dept = null;
            String user = null;
            String out = null;
            List<KpiReportConfigImport> oneLine = new ArrayList<>();
            for (int j = 0; j <= top.size(); j++) {
                String name = top.get(j);
                if ("核算单元".equals(name)) {
                    dept = list.get(i).get(j);
                }
                if ("核算人员".equals(name) || "姓名".equals(name)) {
                    user = list.get(i).get(j);
                }
                if ("摊出核算单元".equals(name)) {
                    out = list.get(i).get(j);
                }
            }

            String finalUser = user==null?"":user;
            String finalDept = dept==null?"":dept;

            int finalI = i;
            JSONObject obj = new JSONObject();
            AtomicInteger count = new AtomicInteger();
            top.forEach((key, value) -> {
                for (KpiReportConfigIndexDto index : indexs) {
                    if (!StringUtil.isNullOrEmpty(value)
                            && (value.replace("\n", "").replace(" ", "").equals(index.getName())
                            || value.replace("\n", "").replace(" ", "").equals(index.getHeadName()))) {
                        KpiReportConfigImport entity = new KpiReportConfigImport();
                        entity.setTenantId(SecurityUtils.getUser().getTenantId());
                        entity.setTaskChildId(taskChildId);
                        entity.setReportId(reportId);
                        entity.setCreateDate(new Date());
                        entity.setPeriod(taskChild.getPeriod());
                        entity.setName(index.getName());
                        entity.setCode(index.getCode());
                        entity.setUserName(finalUser);
                        entity.setDeptName(finalDept);

                        String s = list.get(finalI).get(key);
                        if (!StringUtil.isNullOrEmpty(s)) {
                            entity.setValue(new BigDecimal(s));
                        }
                        if (entity.getValue() != null) {
                            oneLine.add(entity);
                        }
                    }
                }
                obj.put(count + "_" + value, list.get(finalI).get(key) == null ? "" : list.get(finalI).get(key));
                count.getAndIncrement();
            });
            if (isYearImport) {
                KpiReportYearImport year = new KpiReportYearImport();
                year.setTenantId(SecurityUtils.getUser().getTenantId());
                year.setTaskChildId(taskChildId);
                year.setReportId(reportId);
                year.setCreateDate(new Date());
                year.setPeriod(config.getPeriod());

                Attendance att = new Attendance();
                att.setCycle(year.getPeriod()+"");
                att.setTenantId(SecurityUtils.getUser().getTenantId());

                boolean success=true;
                KpiAccountUnit unit=null;
                SysUser use =null;
                if ("1".equals(config.getCaliber()) || "5".equals(config.getCaliber())) {
                    unit = Linq.of(units).firstOrDefault(x -> finalDept.equals(x.getName()));
                    use = Linq.of(users).firstOrDefault(x -> finalUser.equals(x.getUsername()));
                    if(unit==null&&!StringUtil.isNullOrEmpty(finalDept)&&!finalDept.equals(finalUser))
                    {
                        success=false;
                        not_exist.add(finalDept);
                    }
                    if(use==null)
                    {
                        success=false;
                        not_exist.add(finalUser);
                    }
                }
                else if("2".equals(config.getCaliber()))
                {
                    unit = Linq.of(units).firstOrDefault(x -> finalDept.equals(x.getName()));
                    if(unit==null)
                    {
                        success=false;
                        not_exist.add(finalDept);
                    }
                }
                if(success)
                {
                    if(use!=null) {
                        year.setUserId(use.getUserId());
                        year.setUserName(finalUser);
                        year.setEmpCode(use.getJobNumber());

                        att.setUserId(year.getUserId());
                        att.setEmpName(year.getUserName());
                        att.setEmpCode(use.getJobNumber());
                        oneLine.forEach(r -> {
                            r.setUserId(year.getUserId());
                        });
                    }
                    if(dept!=null)
                    {
                        year.setDeptId(unit.getId());
                        year.setDeptName(finalDept);

                        att.setAccountUnitName(finalDept);
                        att.setAccountUnitId(year.getDeptId()!= null ?year.getDeptId()+"":null);
                        oneLine.forEach(r -> {
                            r.setDeptId(year.getDeptId());
                        });
                    }
                }
                if ("5".equals(config.getCaliber())) {
                    atts.add(att);
                }
                year.setJson(JSONObject.toJSONString(obj));
                yearImports.add(year);

            }
            imports.addAll(oneLine);
        }

        if (isYearImport) {
            if (!not_exist.isEmpty()) {
                throw new BizException(JSONObject.toJSONString(Linq.of(not_exist).distinct().toList()) + "核算单元或核算人员有误");
            }
            if (!yearImports.isEmpty()) {
                List<List<KpiReportYearImport>> partition = ListUtils.partition(yearImports, 1000);
                partition.forEach(r -> {
                    kpiReportYearImportMapper.insertBatchSomeColumn(r);
                });
            }
            if (!atts.isEmpty()){
                if (config.getPeriod().toString().endsWith("13")) {
                    attendanceMapper.deleteByPeriod(config.getPeriod());
                }
                List<List<Attendance>> partition = ListUtils.partition(atts, 1000);
                partition.forEach(r -> {
                    attendanceMapper.insertBatchSomeColumn(r);
                });
            }
        } else {
            if (!imports.isEmpty()) {
                List<List<KpiReportConfigImport>> partition = ListUtils.partition(imports, 1000);
                partition.forEach(r -> {
                    kpiReportConfigImportMapper.insertBatchSomeColumn(r);
                });
            }
        }

        return imports;
    }

    @Override
    @Transactional(readOnly = false)
    public void yearImportData(Long taskChildId, Long reportId, List<Map<Integer, String>> list) {
        KpiAccountTask task = kpiAccountTaskMapper.selectOne(
                new QueryWrapper<KpiAccountTask>()
                        .eq("task_child_id", taskChildId)
        );
        /*if ("Y".equals(task.getIssuedFlag())){
            throw new BizException("任务已锁定，无法导入数据");
        }*/
        List<KpiReportConfigImport> imports = importData(taskChildId, reportId, list, true,task.getPeriod());
        if (imports.isEmpty()){
            return;
        }
        List<KpiCalculate> cas = new ArrayList<>();
        kpiCalculateMapper.delete(
                new QueryWrapper<KpiCalculate>()
                        .eq("period", task.getPeriod())
                        .eq("task_child_id", taskChildId)
                        .eq("plan_child_code", reportId.toString()));
        /*List<KpiIndex> indexs = kpiIndexMapper.selectList(
                new QueryWrapper<KpiIndex>()
                        .in("code", Linq.of(imports).select(x -> x.getCode()).distinct().toList())
        );
        List<KpiMember> members = new ArrayList<>();
        List<KpiCalculate> impCas = new ArrayList<>();
        KpiIndex impIndex = new KpiIndex();

        if (!StringUtil.isNullOrEmpty(config.getImpCode())) {
            members = kpiMemberMapper.selectList(
                    new QueryWrapper<KpiMember>()
                            .eq("host_code", config.getImpCode())
                            .eq("member_type", "imputation_dept_emp")
            );
            impIndex = Linq.of(indexs).firstOrDefault(x -> "1".equals(x.getImpFlag()));
            if (impIndex != null && !"2".equals(config.getCaliber())) {
                impCas = kpiCalculateMapper.selectList(
                        new QueryWrapper<KpiCalculate>()
                                .eq("task_child_id", taskChildId)
                                .eq("period", config.getPeriod())
                                .eq("code", impIndex.getCode())
                                .eq("imputation_type", "1")
                );
            }
        }*/
        for (KpiReportConfigImport entity : imports) {
            KpiCalculate calculate = new KpiCalculate();
            calculate.setTaskChildId(entity.getTaskChildId());
            calculate.setCode(entity.getCode());
            calculate.setName(entity.getName());
            calculate.setValue(entity.getValue());
            calculate.setUserId(entity.getUserId());
            calculate.setDeptId(entity.getDeptId());
            calculate.setCreatedDate(entity.getCreateDate());
            calculate.setUserName(entity.getUserName());
            calculate.setDeptName(entity.getDeptName());
            calculate.setTenantId(entity.getTenantId());
            calculate.setPeriod(entity.getPeriod());
            calculate.setImputationType("0");
            calculate.setPlanChildCode(reportId.toString());
            cas.add(calculate);
            /*if (impIndex != null && "1".equals(impIndex.getImpFlag())) {
                if ("2".equals(config.getCaliber())) {
                    calculate.setImputationType("1");
                    calculate.setImputationCode(SnowflakeGenerator.ID() + "");
                } else {
                    calculate.setImputationType("2");
                    KpiMember first = Linq.of(members).firstOrDefault(x -> x.getMemberId().equals(entity.getUserId()));
                    if (first!=null){
                        KpiCalculate impDept = Linq.of(impCas).firstOrDefault(x -> first.getHostId().equals(x.getDeptId()));
                        if (impDept!=null){
                            calculate.setImputationCode(impDept.getImputationCode());
                        }
                    }
                }
            }*/
            /*if ("1".equals(config.getCaliber())) {
                Attendance att = new Attendance();
                att.setUserId(entity.getUserId());
                att.setAccountUnitId(entity.getDeptId().toString());
                att.setCycle(entity.getPeriod().toString());
                att.setEmpName(entity.getUserName());
                att.setDeptName(entity.getDeptName());
                att.setTenantId(SecurityUtils.getUser().getTenantId());
                atts.add(att);

                if ("1".equals(config.getCaliber())) {
                    if (!atts.isEmpty()) {
                    attendanceMapper.delete(
                            new QueryWrapper<Attendance>()
                                    .eq("`cycle`", config.getPeriod())
                    );
                        attendanceMapper.insertBatchSomeColumn(atts);
                    }
                }
            }*/
        }
        taskService.part(task.getPeriod(),kpiCalculateMapper,jdbcTemplate);
        List<List<KpiCalculate>> partition = ListUtils.partition(cas, 1000);
        partition.forEach(r -> {
            kpiCalculateMapper.insertBatchSomeColumn(r);
        });

    }

    @Override
    public List<KpiReportConfigListDTO> getList(String group, String type, String name, Long taskChildId,String status) {
        QueryWrapper qw = new QueryWrapper<>();
        QueryWrapper qw2 = new QueryWrapper<>();
        qw.orderByAsc("seq");
        qw2.orderByAsc("seq");
        if (!StringUtil.isNullOrEmpty(group)) {
            qw.eq("`group`", group);
            qw2.eq("`group`", group);
        }
        if (!StringUtil.isNullOrEmpty(type)) {
            qw.in("type", Arrays.asList(type.split(",")));
            qw2.in("type", Arrays.asList(type.split(",")));
        }
        if (!StringUtil.isNullOrEmpty(name)) {
            qw.like("name", name);
            qw2.like("name", name);
        }
        if (!StringUtil.isNullOrEmpty(status)) {
            qw.like("status", status);
            qw2.like("status", status);
        }

        List<KpiCategory> cates = kpiCategoryMapper.selectList(
                new QueryWrapper<KpiCategory>()
                        .eq("category_type", CategoryEnum.REPORT_GROUP)
        );
        List<KpiReportConfigListDTO> list = new ArrayList<>();
        if (taskChildId != null) {
            KpiAccountTask task = kpiAccountTaskMapper.selectOne(
                    new QueryWrapper<KpiAccountTask>()
                            .eq("task_child_id", taskChildId)
            );
            if (task.getPeriod().toString().endsWith("13") ) {
                qw.in("type", Arrays.asList("3,4".split(",")));
                qw2.in("type", Arrays.asList("3,4".split(",")));
            }
            qw.eq("task_child_id", taskChildId);
            if ("Y".equals(task.getIssuedFlag())) {
                list = kpiReportConfigMapper.selectList2(qw);
            }
        }

        if (list.isEmpty()) {
            list = kpiReportConfigMapper.selectList(qw2);
        }
        for (KpiReportConfigListDTO r : list) {
            KpiCategory cate = Linq.of(cates).firstOrDefault(c -> c.getCategoryCode().equals(r.getGroup()));
            if (cate != null) {
                r.setGroupName(cate.getCategoryName());
            }
        }
        return list;
    }

    //报表getMemberList
    private List<Long> getMemberList(CalAllDto allDto, Long task_child_id) {

        KpiAccountTaskChild taskChild = kpiAccountTaskChildMapper.selectOne(
                new QueryWrapper<KpiAccountTaskChild>()
                        .eq("id", task_child_id)
        );
        KpiAccountTask task = kpiAccountTaskMapper.selectOne(
                new QueryWrapper<KpiAccountTask>()
                        .eq("id", taskChild.getTaskId())
        );
        List<SysUser> users = kpiCalculateMapper.getUsers(SecurityUtils.getUser().getTenantId());        if (task == null) {
            throw new BizException("任务不存在");
        }
        List<KpiMemberCopy> kpiMembers = new ArrayList<>();
        List<KpiUserAttendanceCopy> kpiUserAttendances = new ArrayList<>();
        List<KpiAccountUnitCopy> kpiAccountUnits = new ArrayList<>();
        if ("N".equals(task.getTestFlag()) && !task.getPeriod().toString().endsWith("13")) {
            kpiMembers = kpiMemberCopyMapper.selectList(
                    new QueryWrapper<KpiMemberCopy>()
                            .eq("task_child_id", task_child_id)
            );
            kpiUserAttendances = kpiUserAttendanceCopyMapper.selectList(
                    new QueryWrapper<KpiUserAttendanceCopy>()
                            .eq("task_child_id", task_child_id)
            );
            kpiAccountUnits = kpiAccountUnitCopyMapper.selectList(
                    new QueryWrapper<KpiAccountUnitCopy>()
                            .eq("task_child_id", task_child_id)
            );
        } else {
            String str = MemberEnum.IMPUTATION_DEPT_EMP.getType() + "," + MemberEnum.ROLE_EMP.getType() + "," + MemberEnum.EMP_TYPE.getType();
            kpiMembers = kpiMemberCopyMapper.getList(
                    new QueryWrapper<KpiMemberCopy>()
                            .in("period", Arrays.asList((task.getPeriod() + ",0").split(",")))
                            .eq("tenant_id", task.getTenantId())
                            .in("member_type", Arrays.asList(str.split(",")))
                            .eq("busi_type", "1")
            );
            kpiUserAttendances = kpiUserAttendanceCopyMapper.getList(
                    new QueryWrapper<KpiUserAttendanceCopy>()
                            .eq("period", task.getPeriod())
                            .eq("del_flag", "0")
                            .eq("tenant_id", task.getTenantId())
                            .eq("busi_type", "1")
            );
            kpiAccountUnits = kpiAccountUnitCopyMapper.getList(
                    new QueryWrapper<KpiAccountUnitCopy>()
                            .eq("del_flag", "0")
                            .eq("status", "0")
                            .eq("tenant_id", task.getTenantId())
                            .eq("busi_type", "1")
            );
            if (kpiUserAttendances.isEmpty()) {
                kpiUserAttendances = kpiUserAttendanceCopyMapper.getList2(task.getPeriod() - 1);
            }
        }

        List<Long> rt = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(allDto.getParam().getParamType())) {
            FormulaParamEnum formulaParamEnum = FormulaParamEnum.find(allDto.getParam().getParamType());
            switch (formulaParamEnum) {
                case P_11://自定义人员
                    rt.addAll(Linq.of(allDto.getParam().getParamValues()).select(r -> Long.parseLong(r.getValue())).toList());
                    break;
                case P_12://人员类型（字典对应user_type）
                    List<Long> users2 = Linq.of(kpiMembers).where(t -> t.getMemberType().equals(MemberEnum.EMP_TYPE.getType())
                                    && Linq.of(allDto.getParam().getParamValues()).select(x -> x.getValue()).toList().contains(t.getMemberCode()))
                            .select(t -> t.getHostId()).toList();
                    rt.addAll(users2);
                    break;
                case P_14://按人员分组
                    List<Long> users_role = Linq.of(kpiMembers).where(t -> t.getMemberType().equals(MemberEnum.ROLE_EMP.getType())
                                    && Linq.of(allDto.getParam().getParamValues()).select(x -> x.getValue()).toList().contains(t.getHostCode()))
                            .select(t -> t.getMemberId()).toList();
                    rt.addAll(users_role);
                    break;
                case P_15://按工作性质
                    List<Long> users_job = Linq.of(kpiUserAttendances).where(t ->
                                    Linq.of(allDto.getParam().getParamValues()).select(x -> x.getValue()).toList()
                                            .contains(t.getJobNature()))
                            .select(t -> t.getUserId()).toList();
                    rt.addAll(users_job);
                    break;
                case P_19:
                    rt.addAll(Linq.of(kpiUserAttendances).select(r -> r.getUserId()).toList());
                    break;
                case P_100://所有人员
                    rt.addAll(Linq.of(users).select(r -> r.getUserId()).toList());
                    break;
                case P_21://自定义科室
                    rt.addAll(Linq.of(allDto.getParam().getParamValues()).select(r -> Long.parseLong(r.getValue())).toList());
                    break;
                case P_22://核算类型（字典对应kpi_calculate_type）
                    List<KpiAccountUnitCopy> account_type = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && Linq.of(allDto.getParam().getParamValues()).select(r -> r.getValue()).toList().contains(t.getAccountTypeCode())).toList();

                    rt.addAll(Linq.of(account_type).select(r -> r.getId()).toList());
                    break;
                case P_23://核算分组
                    List<KpiAccountUnitCopy> account_group = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && Linq.of(allDto.getParam().getParamValues()).select(r -> r.getValue()).toList().contains(t.getCategoryCode())).toList();
                    rt.addAll(Linq.of(account_group).select(r -> r.getId()).toList());
                    break;
                case P_24://本人员负责科室
                    List<KpiAccountUnitCopy> account_dept = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && t.getResponsiblePersonId() != null && Arrays.asList(t.getResponsiblePersonId().split(",")).contains(allDto.getMemberId().toString())).toList();
                    rt.addAll(Linq.of(account_dept).select(r -> r.getId()).toList());
                    break;
                case P_25://科室单元人员类型（字典对应user_type）
                    List<KpiAccountUnitCopy> account_rylx = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && Linq.of(allDto.getParam().getParamValues()).select(r -> r.getValue()).toList().contains(t.getAccountUserCode())).toList();
                    rt.addAll(Linq.of(account_rylx).select(r -> r.getId()).toList());
                    break;
                case P_29:
                    rt.addAll(Linq.of(kpiAccountUnits).select(r -> r.getId()).toList());
                    break;
            }

            //口径剔除
            if (allDto.getParam().getParamExcludes() != null && !allDto.getParam().getParamExcludes().isEmpty()) {
                List<Long> list = new ArrayList<>();

                for (KpiFormulaDto2.MemberListDTO r : allDto.getParam().getParamExcludes()) {
                    if (r.getType() == null
                            || r.getType().equals(FormulaParamEnum.P_11.getType())
                            || r.getType().equals(FormulaParamEnum.P_21.getType())) {
                        list.add(Long.parseLong(r.getValue()));
                    } else {
                        list.addAll(getMemberList(r.getType(), r.getValue(), kpiMembers, kpiUserAttendances, kpiAccountUnits));
                    }
                }
                rt.removeAll(list);
            }
        }
        return rt;
    }

    //取口径剔除对象
    private List<Long> getMemberList(String paramType, String value, List<KpiMemberCopy> kpiMembers,
                                     List<KpiUserAttendanceCopy> kpiUserAttendances, List<KpiAccountUnitCopy> kpiAccountUnits) {
        List<Long> rt = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(paramType)) {
            FormulaParamEnum formulaParamEnum = FormulaParamEnum.find(paramType);
            switch (formulaParamEnum) {
                case P_12://人员类型（字典对应user_type）
                    List<Long> users = Linq.of(kpiMembers).where(t -> t.getMemberType().equals(MemberEnum.EMP_TYPE.getType())
                                    && value.equals(t.getMemberCode()))
                            .select(t -> t.getHostId()).toList();
                    rt.addAll(users);
                    break;
                case P_14://按人员分组
                    List<Long> users_role = Linq.of(kpiMembers).where(t -> t.getMemberType().equals(MemberEnum.ROLE_EMP.getType())
                                    && value.equals(t.getHostCode()))
                            .select(t -> t.getMemberId()).toList();
                    rt.addAll(users_role);
                    break;
                case P_15://按工作性质
                    List<Long> users_job = Linq.of(kpiUserAttendances).where(t ->
                                    value.equals(t.getJobNature()))
                            .select(t -> t.getUserId()).toList();
                    rt.addAll(users_job);
                    break;
                case P_22://核算类型（字典对应kpi_calculate_type）
                    List<KpiAccountUnitCopy> account_type = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && value.equals(t.getAccountTypeCode())).toList();
                    rt.addAll(Linq.of(account_type).select(r -> r.getId()).toList());
                    break;
                case P_23://核算分组
                    List<KpiAccountUnitCopy> account_group = Linq.of(kpiAccountUnits).where(t -> t.getStatus().equals("0") && t.getDelFlag().equals("0")
                            && value.equals(t.getCategoryCode())).toList();
                    rt.addAll(Linq.of(account_group).select(r -> r.getId()).toList());
                    break;
            }
        }
        return rt;
    }
}
