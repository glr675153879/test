package com.hscloud.hs.cost.account.service.impl.userAttendance;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.mapper.userAttendance.CostUserAttendanceMapper;
import com.hscloud.hs.cost.account.model.dto.userAttendance.*;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import com.hscloud.hs.cost.account.model.entity.userAttendance.*;
import com.hscloud.hs.cost.account.model.vo.userAttendance.CustomFieldVO;
import com.hscloud.hs.cost.account.model.vo.userAttendance.ValidateJobNumberVO;
import com.hscloud.hs.cost.account.model.vo.userAttendance.export.ImportErrListVO;
import com.hscloud.hs.cost.account.model.vo.userAttendance.export.ImportErrVo;
import com.hscloud.hs.cost.account.service.CostAccountUnitService;
import com.hscloud.hs.cost.account.service.impl.CostAccountUnitServiceImpl;
import com.hscloud.hs.cost.account.service.imputation.IImputationDeptUnitService;
import com.hscloud.hs.cost.account.service.second.IAttendanceService;
import com.hscloud.hs.cost.account.service.userAttendance.ICostUserAttendanceConfigService;
import com.hscloud.hs.cost.account.service.userAttendance.ICostUserAttendanceCustomFieldsService;
import com.hscloud.hs.cost.account.service.userAttendance.ICostUserAttendanceService;
import com.hscloud.hs.cost.account.service.userAttendance.IFirstDistributionAttendanceFormulaService;
import com.hscloud.hs.cost.account.utils.DataProcessUtil;
import com.hscloud.hs.cost.account.utils.DmoUtil;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import com.pig4cloud.pigx.admin.api.dto.GetUserListCustomDTO;
import com.pig4cloud.pigx.admin.api.dto.UserInfo;
import com.pig4cloud.pigx.admin.api.entity.SysDept;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.*;
import com.pig4cloud.pigx.common.core.constant.SecurityConstants;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.security.service.PigxUser;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.swagger.v3.oas.annotations.media.Schema;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.ONE;
import static com.baomidou.mybatisplus.core.toolkit.StringPool.ZERO;
import static org.apache.commons.codec.CharEncoding.UTF_8;

/**
 * 人员考勤表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostUserAttendanceService extends ServiceImpl<CostUserAttendanceMapper, CostUserAttendance> implements ICostUserAttendanceService {


    private final UserAttendanceLogService userAttendanceLogService;

    private final DmoUtil dmoUtil;

    private final IFirstDistributionAttendanceFormulaService firstDistributionAttendanceFormulaService;

    private final IImputationDeptUnitService ImputationDeptUnitService;

    private final RedisUtil redisUtil;

    private final static String empList = "叶海、洪善贻、王晖";

    private final static String medGroup1 = "医生组、医技组、护理组";

    private final static String medGroup2 = "行政组、药剂组";

    private static final Long FORMULA_ID_SJCQTS = 1L;

    private static final Long FORMULA_ID_YCXJXCQTS = 2L;

    private static final String UPDATE = "\"%s\"由\"%s\"变更为\"%s\"";
    private final CostAccountUnitServiceImpl costAccountUnitServiceImpl;
    private final CostUserAttendanceCustomFieldDataService costUserAttendanceCustomFieldDataService;

    private final long HRThirdId = 1783384115337363458L;

    private static final String ZHONGZHISHI = "中治室";

    private final DataProcessUtil dataProcessUtil;

    private final ICostUserAttendanceCustomFieldsService costUserAttendanceCustomFieldsService;

    private final CostAccountUnitService costAccountUnitService;

    // private final RemoteThirdAccountUnitService remoteThirdAccountUnitService;

    private final RemoteDeptService remoteDeptService;

    private final RemoteUserService remoteUserService;

    private final RemoteUserFeignServiceClient remoteUserFeignServiceClient;

    private final IAttendanceService attendanceService;

    private final ICostUserAttendanceConfigService costUserAttendanceConfigService;

    private final RemoteMappingBaseService remoteMappingBaseService;
    @Value("rsPurpose:RS")
    private String rsPurpose;


    @Override
    public void downloadTemplate(String dt, HttpServletResponse response) {
        try {
            List<CostUserAttendanceCustomFields> customFields = costUserAttendanceCustomFieldsService.listByDt(dt);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(UTF_8);
            EasyExcel.write(response.getOutputStream()).autoCloseStream(true).sheet("模板").head(getHead(customFields)).doWrite(new ArrayList<>());
        } catch (Exception e) {
            log.error("导出报错", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R handleFileUpload(MultipartFile file) {
        // 转化数据
        if (!file.isEmpty()) {
            try (InputStream inputStream = file.getInputStream()) {
                Workbook workbook = new XSSFWorkbook(inputStream);
                Sheet sheet = workbook.getSheetAt(0);

                List<Map<String, Object>> dataList = new ArrayList<>();
                Iterator<Row> rowIterator = sheet.iterator();

                if (rowIterator.hasNext()) {
                    Row headerRow = rowIterator.next();
                    while (rowIterator.hasNext()) {
                        Row row = rowIterator.next();
                        Map<String, Object> data = new HashMap<>();
                        Iterator<Cell> cellIterator = row.iterator();

                        int cellIndex = 0;
                        while (cellIterator.hasNext()) {
                            Cell cell = cellIterator.next();
                            String headerValue = headerRow.getCell(cellIndex).getStringCellValue();
                            data.put(headerValue, getCellValue(cell));
                            cellIndex++;
                        }
                        dataList.add(data);
                    }
                }
                List<CostUserAttendance> costUserAttendanceList = new ArrayList<>();
                for (Map<String, Object> data : dataList) {
                    // process data...
                    CostUserAttendanceDownloadDto costUserAttendanceDownloadDto = new CostUserAttendanceDownloadDto();
                    CostUserAttendance costUserAttendance = new CostUserAttendance();
                    Field[] fields = CostUserAttendanceDownloadDto.class.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.isAnnotationPresent(Schema.class)) {
                            Schema schema = field.getAnnotation(Schema.class);
                            String description = schema.description();
                            if (data.containsKey(description)) {
                                try {
                                    field.setAccessible(true);
                                    Object value = data.get(description);
                                    PropertyEditor editor = PropertyEditorManager.findEditor(field.getType());
                                    if (editor != null) {
                                        editor.setAsText(value.toString());
                                        field.set(costUserAttendanceDownloadDto, editor.getValue());
                                    } else {
                                        field.set(costUserAttendanceDownloadDto, value);
                                    }
                                } catch (Exception e) {
                                    System.out.println("设置属性值" + description + "失败: " + e.getMessage());
                                }
                            }
                        }
                    }
                    // 自定义字段
                    List<CostUserAttendanceCustomFields> customFields = costUserAttendanceCustomFieldsService.list();
                    List<CustomFieldVO> valueList = new ArrayList<>();
                    for (CostUserAttendanceCustomFields field : customFields) {
                        if (data.containsKey(field.getName())) {
                            try {
                                Object value = data.get(field.getName());
                                CustomFieldVO customFieldVO = new CustomFieldVO();
                                customFieldVO.setNum(value.toString());
                                customFieldVO.setName(field.getName());
                                Long id = costUserAttendanceCustomFieldsService.getOne(new QueryWrapper<CostUserAttendanceCustomFields>().eq("name", field.getName())).getId();
                                customFieldVO.setId(id.toString());
                                valueList.add(customFieldVO);
                            } catch (Exception e) {
                                System.out.println("设置自定义属性值" + field.getName() + "失败: " + e.getMessage());
                            }
                        }
                    }
                    BeanUtils.copyProperties(costUserAttendanceDownloadDto, costUserAttendance);
                    costUserAttendance.setAttendanceGroupDays(new BigDecimal(costUserAttendanceDownloadDto.getAttendanceGroupDays()));
                    costUserAttendance.setRewardIndex(new BigDecimal(costUserAttendanceDownloadDto.getRewardIndex()));
                    costUserAttendance.setRegisteredRate(new BigDecimal(costUserAttendanceDownloadDto.getRegisteredRate()));
                    StringBuilder sb = new StringBuilder();
                    for (CustomFieldVO value : valueList) {
                        sb.append("{ id:").append(value.getId()).append(",name:'").append(value.getName()).append("',num:").append(value.getNum()).append("},");
                    }
                    if (!valueList.isEmpty()) {
                        sb.deleteCharAt(sb.length() - 1);
                    }
                    costUserAttendance.setCustomFields(sb.toString());
                    String dt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                    costUserAttendance.setDt(dt);
                    costUserAttendanceList.add(costUserAttendance);
                    saveBatch(costUserAttendanceList);
                }
            } catch (IOException e) {
                System.out.println("文件上传失败");
            }
        }
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportErrVo uploadFile(String[][] xlsDataArr, ExcelImportDTO dto, String dt) {
        // 此周期的自定义字段
        List<CostUserAttendanceCustomFields> customFields = costUserAttendanceCustomFieldsService.listByDt(dt);
        List<CostAccountUnit> costAccountUnits = costAccountUnitService.list();
        List<SysUser> sysUsers = remoteUserService.userListByCustom(new GetUserListCustomDTO(), SecurityConstants.FROM_IN).getData();

        List<List<String>> head = getErrorHead();
        List<ImportErrListVO> details = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        if (Objects.equals("1", dto.getOverwriteFlag())) {
            // 1覆盖模式：删除此周期所有数据
            super.remove(new LambdaQueryWrapper<CostUserAttendance>().eq(CostUserAttendance::getDt, dt));
        }
        // 查出当前周期所有数据
        List<CostUserAttendance> costUserAttendanceDbs = super.list(Wrappers.<CostUserAttendance>lambdaQuery().eq(CostUserAttendance::getDt, dt));
        // 导入数据
        for (int i = 1; i < xlsDataArr.length; i++) {
            String[] rowData = xlsDataArr[i];
            // 错误说明
            List<String> contentList = new ArrayList<>();
            try {
                // 校验并组装行数据
                CostUserAttendance costUserAttendanceExcel = checkAndTransferRowData(dt, rowData, contentList, customFields, costAccountUnits, sysUsers);
                if (CollUtil.isEmpty(contentList)) {
                    CostUserAttendance costUserAttendance = costUserAttendanceExcel;
                    // 增量导入：根据工号、科室单元是否已存在决定是更新还是新增
                    if (Objects.equals("2", dto.getOverwriteFlag())) {
                        costUserAttendance = checkDbExist(costUserAttendanceExcel, costUserAttendanceDbs);
                    }
                    costUserAttendance.setDt(dt);
                    super.saveOrUpdate(costUserAttendance);
                    // // 每次新增后将excel放入到db中，防止excel本身有重复数据
                    // costUserAttendanceDbs.add(costUserAttendance);
                }
            } catch (Exception e) {
                log.error("导入报错", e);
                String message = e.getMessage();
                contentList.add(message);
            }
            if (CollUtil.isNotEmpty(contentList)) {
                // 生成错误说明
                failCount++;
                ImportErrListVO build = ImportErrListVO.builder().lineNum(i + 1).data(ImmutableList.of(rowData[2], rowData[1], rowData[0])).contentList(contentList).content(StrUtil.join(";",
                        contentList)).build();
                details.add(build);
                // 生成错误说明
                if (Objects.equals("2", dto.getContinueFlag())) {
                    break;
                }
            } else {
                successCount++;
            }
        }
        ImportErrVo build = ImportErrVo.builder().details(details).successCount(successCount).failCount(failCount).head(head).build();
        redisUtil.set(CacheConstants.IMPORT_ERROR_COST_USER_ATTENDANCE + dt, JSON.toJSONString(build), 30, TimeUnit.MINUTES);
        return build;
    }

    /**
     * 检查excel行数据生成错误报告
     * 并且返回
     *
     * @param rowData
     * @param contentList
     * @param customFields
     * @param costAccountUnits
     * @param sysUsers
     * @return {@link CostUserAttendance }
     */
    private CostUserAttendance checkAndTransferRowData(String dt, String[] rowData, List<String> contentList, List<CostUserAttendanceCustomFields> customFields,
                                                       List<CostAccountUnit> costAccountUnits, List<SysUser> sysUsers) {
        // 获取周期月份的自然天数
        DateTime yyyyMM = DateUtil.parse(dt, "yyyyMM");
        BigDecimal lastDayOfMonth = new BigDecimal(yyyyMM.getLastDayOfMonth());
        CostUserAttendance costUserAttendance = new CostUserAttendance();
        String accountUnitName = rowData[0];// 科室单元
        CostAccountUnit costAccountUnit = null;
        if (StrUtil.isBlank(accountUnitName)) {
            contentList.add("科室单元缺失");
        } else {
            Optional<CostAccountUnit> any = costAccountUnits.stream().filter(e -> Objects.equals(accountUnitName, e.getName())).findAny();
            if (any.isPresent()) {
                costAccountUnit = any.get();
                costUserAttendance.setAccountUnit(String.format("[{\"id\":\"%s\",\"name\":\"%s\"}]", costAccountUnit.getId(), costAccountUnit.getName()));
            } else {
                contentList.add("科室单元不存在，请先在系统新增");
            }
        }

        String attendanceGroup = rowData[1];// 考勤组
        costUserAttendance.setAttendanceGroup(attendanceGroup);
        String empName = rowData[2];// 姓名
        String empId = rowData[3];// 工号
        costUserAttendance.setEmpId(empId);
        costUserAttendance.setEmpName(empName);
        if (StrUtil.isBlank(empName)) {
            contentList.add("姓名缺失");
        }
        if (StrUtil.isBlank(empId)) {
            contentList.add("工号缺失");
        } else {
            Optional<SysUser> any = sysUsers.stream().filter(e -> Objects.equals(empId, e.getJobNumber())).findAny();
            if (any.isPresent()) {
                SysUser sysUser = any.get();
                if (StrUtil.isNotBlank(empName) && !Objects.equals(empName, sysUser.getName())) {
                    contentList.add("工号与姓名不匹配");
                }
            } else {
                contentList.add("工号不存在");
            }
        }
        String accountGroupStr = rowData[4];// 核算组别
        if (StrUtil.isBlank(accountGroupStr)) {
            contentList.add("核算组别缺失");
        } else {
            if (Objects.nonNull(costAccountUnit)) {
                if (StrUtil.contains(costAccountUnit.getAccountGroupCode(), accountGroupStr)) {
                    costUserAttendance.setAccountGroup(costAccountUnit.getAccountGroupCode());
                } else {
                    contentList.add("核算组别与系统维护的不一致");
                }
            }
        }

        String jobNature = rowData[5];// 工作性质
        costUserAttendance.setJobNature(jobNature);
        String titles = rowData[6];// 职称
        costUserAttendance.setTitles(titles);
        String dutiesName = rowData[7];// 职务
        costUserAttendance.setDutiesName(dutiesName);
        String post = rowData[8];// 岗位
        costUserAttendance.setPost(post);
        String reward = rowData[9];// 是否拿奖金
        if (StrUtil.isBlank(reward)) {
            contentList.add("是否拿奖金缺失");
        } else if (Objects.equals("是", reward)) {
            costUserAttendance.setReward("1");
        } else if (Objects.equals("否", reward)) {
            costUserAttendance.setReward("0");
        } else {
            contentList.add("是否拿奖金错误");
        }
        String rewardIndex = rowData[10];// 奖金系数
        if (StrUtil.isNotBlank(rewardIndex)) {
            if (!NumberUtil.isNumber(rewardIndex)) {
                contentList.add("奖金系数需要传数值");
            } else {
                costUserAttendance.setRewardIndex(NumberUtil.toBigDecimal(rewardIndex));
            }
        }
        String noRewardReason = rowData[11];// 不拿奖金原因
        costUserAttendance.setNoRewardReason(noRewardReason);
        String attendanceGroupDays = rowData[12];// 当前考勤组所在天数
        if (StrUtil.isNotBlank(attendanceGroupDays)) {
            if (!NumberUtil.isNumber(attendanceGroupDays)) {
                contentList.add("当前考勤组所在天数需要传数值");
            } else {
                BigDecimal attendanceGroupDaysNumber = NumberUtil.toBigDecimal(attendanceGroupDays);
                if (NumberUtil.isGreater(attendanceGroupDaysNumber, lastDayOfMonth) || NumberUtil.isLess(attendanceGroupDaysNumber, BigDecimal.ZERO)) {
                    contentList.add("当前考勤组所在天数不在月份天数范围内");
                } else {
                    costUserAttendance.setAttendanceGroupDays(attendanceGroupDaysNumber);
                }
            }
        }
        String attendDays = rowData[13];// 出勤天数
        if (StrUtil.isNotBlank(attendDays)) {
            if (!NumberUtil.isNumber(attendDays)) {
                contentList.add("出勤天数需要传数值");
            } else {
                BigDecimal attendDaysNumber = NumberUtil.toBigDecimal(attendDays);
                if (NumberUtil.isGreater(attendDaysNumber, lastDayOfMonth) || NumberUtil.isLess(attendDaysNumber, BigDecimal.ZERO)) {
                    contentList.add("出勤天数不在月份天数范围内");
                } else {
                    costUserAttendance.setAttendDays(attendDaysNumber);
                }
            }
        }
        String attendCount = rowData[14];// 出勤次数
        if (StrUtil.isNotBlank(attendCount)) {
            if (!NumberUtil.isNumber(attendCount)) {
                contentList.add("出勤次数需要传数值");
            } else {
                BigDecimal attendCountNumber = NumberUtil.toBigDecimal(attendCount);
                if (NumberUtil.isLess(attendCountNumber, BigDecimal.ZERO)) {
                    contentList.add("出勤次数填写有误");
                } else {
                    costUserAttendance.setAttendCount(attendCount);
                }
            }
        }
        String attendRate = rowData[15];// 出勤系数
        if (StrUtil.isNotBlank(attendRate)) {
            if (!NumberUtil.isNumber(attendRate)) {
                contentList.add("出勤系数需要传数值");
            } else {
                costUserAttendance.setAttendRate(NumberUtil.toBigDecimal(attendRate));
            }
        }
        String registeredRate = rowData[16];// 在册系数
        if (StrUtil.isNotBlank(registeredRate)) {
            if (!NumberUtil.isNumber(registeredRate)) {
                contentList.add("在册系数需要传数值");
            } else {
                costUserAttendance.setRegisteredRate(NumberUtil.toBigDecimal(registeredRate));
            }
        }
        String oneKpiAttendDays = rowData[17];// 一次性绩效出勤天数
        if (StrUtil.isNotBlank(oneKpiAttendDays)) {
            if (!NumberUtil.isNumber(oneKpiAttendDays)) {
                contentList.add("一次性绩效出勤天数需要传数值");
            } else {
                costUserAttendance.setOneKpiAttendDays(NumberUtil.toBigDecimal(oneKpiAttendDays));
            }
        }
        String oneKpiAttendRate = rowData[18];// 一次性绩效出勤系数
        if (StrUtil.isNotBlank(oneKpiAttendRate)) {
            if (!NumberUtil.isNumber(oneKpiAttendRate)) {
                contentList.add("一次性绩效出勤系数需要传数值");
            } else {
                costUserAttendance.setOneKpiAttendRate(NumberUtil.toBigDecimal(oneKpiAttendRate));
            }
        }
        int pos = 19;
        if (customFields.size() + 19 != rowData.length) {
            contentList.add("列个数错误，请与模板比对");
        } else {
            List<Map<String, Object>> mapList = new ArrayList<>();
            for (CostUserAttendanceCustomFields customField : customFields) {
                String cellData = rowData[pos++];
                try {
                    checkCustomField(customField, cellData, lastDayOfMonth);
                } catch (BizException e) {
                    contentList.add(e.getDefaultMessage());
                } catch (Exception e) {
                    contentList.add(e.getMessage());
                }
                mapList.add(ImmutableMap.of("id", customField.getColumnId(), "name", customField.getName(), "num", cellData));
            }
            List<String> collect = mapList.stream().map(JSON::toJSONString).collect(Collectors.toList());
            costUserAttendance.setCustomFields(CollUtil.join(collect, ","));
            // costUserAttendance.setOriginCustomFields(CollUtil.join(collect, "$|"));
        }
        return costUserAttendance;
    }

    public static void checkCustomField(CostUserAttendanceCustomFields customField, String cellData, BigDecimal lastDayOfMonth) {
        // 必填校验
        if (Objects.equals("1", customField.getRequireFlag()) && StrUtil.isBlank(cellData)) {
            throw new BizException(String.format("[%s]不能为空", customField.getName()));
        }
        if (StrUtil.isNotBlank(cellData)) {
            // 检查数据
            if (Objects.equals("01", customField.getFieldType())) {
                boolean isNumber = NumberUtil.isNumber(cellData);
                if (!isNumber) {
                    throw new BizException(String.format("[%s][%s]不是数值类型", customField.getName(), cellData));
                } else {
                    BigDecimal cellValue = NumberUtil.toBigDecimal(cellData);
                    if (Objects.equals("0101", customField.getFieldCheck())) {
                        if (NumberUtil.isGreater(cellValue, lastDayOfMonth) || NumberUtil.isLess(cellValue, BigDecimal.ZERO)) {
                            throw new BizException(String.format("[%s][%s]不在月份天数范围内", customField.getName(), cellData));
                        }
                    } else if (Objects.equals("0102", customField.getFieldCheck())) {
                        if (NumberUtil.isLess(cellValue, BigDecimal.ZERO)) {
                            throw new BizException(String.format("[%s][%s]不是正数", customField.getName(), cellData));
                        }
                    } else if (Objects.equals("0103", customField.getFieldCheck())) {
                        if (NumberUtil.isGreater(cellValue, BigDecimal.ONE) || NumberUtil.isLess(cellValue, BigDecimal.ZERO)) {
                            throw new BizException(String.format("[%s][%s]不在0到1范围内", customField.getName(), cellData));
                        }
                    }
                }
            } else if (Objects.equals("04", customField.getFieldType())) {
                boolean isNumber = NumberUtil.isLong(cellData);
                if (!isNumber) {
                    throw new BizException(String.format("[%s][%s]不是整数类型", customField.getName(), cellData));
                } else {
                    long cellValue = NumberUtil.parseLong(cellData);
                    if (Objects.equals("0401", customField.getFieldCheck())) {// 0401:正整数
                        if (cellValue <= 0) {
                            throw new BizException(String.format("[%s][%s]不是正整数", customField.getName(), cellData));
                        }
                    } else if (Objects.equals("0402", customField.getFieldCheck())) {// 0402:非负整数
                        if (cellValue < 0) {
                            throw new BizException(String.format("[%s][%s]不是非负整数", customField.getName(), cellData));
                        }
                    } else if (Objects.equals("0403", customField.getFieldCheck())) {// 0403:负整数
                        if (cellValue >= 0) {
                            throw new BizException(String.format("[%s][%s]不是负整数", customField.getName(), cellData));
                        }
                    } else if (Objects.equals("0404", customField.getFieldCheck())) {// 0404:非正整数
                        if (cellValue > 0) {
                            throw new BizException(String.format("[%s][%s]不是非正整数", customField.getName(), cellData));
                        }
                    }
                }
            } else if (Objects.equals("03", customField.getFieldType())) {
                List<String> selectOptions = StrUtil.split(customField.getFieldCheck(), ";");
                if (!CollUtil.contains(selectOptions, cellData)) {
                    throw new BizException(String.format("[%s][%s]选项不存在", customField.getName(), cellData));
                }
            }
        }
    }

    private CostUserAttendance checkDbExist(CostUserAttendance excelData, List<CostUserAttendance> costUserAttendanceDbs) {
        String accountUnit = excelData.getAccountUnit();
        JSONArray array = JSON.parseArray(accountUnit);
        Optional<CostUserAttendance> dbDataOptional = costUserAttendanceDbs.stream().filter(e -> Objects.equals(e.getEmpId(), excelData.getEmpId()) && StrUtil.contains(e.getAccountUnit(),
                array.getJSONObject(0).getString("id"))).findAny();
        CostUserAttendance costUserAttendance = dbDataOptional.orElseGet(CostUserAttendance::new);
        BeanUtil.copyProperties(excelData, costUserAttendance, CopyOptions.create().ignoreNullValue());
        return costUserAttendance;
    }

    public static List<List<String>> getErrorHead() {
        List<List<String>> errorHead = new ArrayList<>();
        errorHead.add(ImmutableList.of("行号"));
        errorHead.add(ImmutableList.of("姓名"));
        errorHead.add(ImmutableList.of("考勤组"));
        errorHead.add(ImmutableList.of("科室单元"));
        errorHead.add(ImmutableList.of("错误说明"));
        return errorHead;
    }

    @Override
    public List<CostUserAttendanceDto> toMatchList(QueryWrapper<CostUserAttendance> wrapper) {
        // 系统没有给匹配科室单元的数据
        List<CostUserAttendanceDto> rtnList2 = new ArrayList<>();
        List<CostUserAttendanceDto> rtnList = new ArrayList<>();
        LambdaQueryWrapper<CostUserAttendance> qr = wrapper.lambda();
        qr.eq(CostUserAttendance::getIsEdited, "0");
        qr.isNull(CostUserAttendance::getAccountUnit);
        List<CostUserAttendance> list = list(qr);
        for (CostUserAttendance item : list) {
            CostUserAttendanceDto dto = new CostUserAttendanceDto();
            BeanUtils.copyProperties(item, dto);
            String accountUnitInfo = item.getAccountUnit();
            if (StringUtils.isAllBlank(accountUnitInfo)) {
                List<AccountUnitDto> accountUnits = new ArrayList<>();
                dto.setAccountUnits(accountUnits);
                rtnList.add(dto);
            }
            ObjectMapper mapper = new ObjectMapper();
            AccountUnitDto accountUnitDto = new AccountUnitDto();
            try {
                JsonNode rootNode = mapper.readTree(accountUnitInfo);
                JsonNode jsonObject = rootNode.get(0); // 获取第一个对象
                accountUnitDto.setName(jsonObject.get("name").asText());
                accountUnitDto.setId(jsonObject.get("id").asText());
            } catch (Exception ignored) {
            }
            List<AccountUnitDto> accountUnits = new ArrayList<>();
            if (ObjectUtil.isEmpty(accountUnitDto)) {
                accountUnits.add(accountUnitDto);
                dto.setAccountUnits(accountUnits);
                rtnList.add(dto);
            }
        }
        // 继承上月科室
        for (CostUserAttendanceDto rItem : rtnList) {
            String dt = rItem.getDt();
            YearMonth yearMonth = YearMonth.parse(dt, DateTimeFormatter.ofPattern("yyyyMM"));
            YearMonth nextYearMonth = yearMonth.minusMonths(1);
            String nextDt = nextYearMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));
            CostUserAttendance lastMonthData = getOne(new QueryWrapper<CostUserAttendance>()
                    .eq("emp_name", rItem.getEmpName())
                    .eq("dt", nextDt)
                    .last("limit 1"));
            if (rItem.getAccountUnits().isEmpty()) {
                rtnList2.add(rItem);
            } else {
                try {
                    rItem.setAccountUnit(lastMonthData.getAccountUnit());
                } catch (Exception ignored) {
                }
            }
        }
        return rtnList2;
    }

    // 用户调整过的全量数据，全部展示在这里（上月、上上月...）。这些数据不管他们的考勤组对应科室单元是什么，都继承上月数据（是否要用锁定后的数据无所谓）
    // 本月手动>上月继承>本月系统
    @Override
    public Page<CostUserAttendanceDto> manualMatchList(PageRequest<CostUserAttendance> pr) {
        Page<CostUserAttendanceDto> rtnPage = new Page<>();
        LambdaQueryWrapper<CostUserAttendance> qr = pr.getWrapper().lambda();
        qr.eq(CostUserAttendance::getIsEdited, "1");
        Page<CostUserAttendance> originData = page(pr.getPage(), qr);
        List<CostUserAttendance> targetList = originData.getRecords();
        List<CostUserAttendanceDto> rtnList = new ArrayList<>();
        for (CostUserAttendance item : targetList) {
            CostUserAttendanceDto dto = new CostUserAttendanceDto();
            // 1添加本月手动
            if (item.getIsEdited().equals(ONE)) {
                BeanUtils.copyProperties(item, dto);
                String accountUnitInfo = item.getAccountUnit();
                ObjectMapper mapper = new ObjectMapper();
                AccountUnitDto accountUnitDto = new AccountUnitDto();
                try {
                    JsonNode rootNode = mapper.readTree(accountUnitInfo);
                    JsonNode jsonObject = rootNode.get(0); // 获取第一个对象
                    accountUnitDto.setName(jsonObject.get("name").asText());
                    accountUnitDto.setId(jsonObject.get("id").asText());
                } catch (Exception ignored) {
                }
                List<AccountUnitDto> accountUnits = new ArrayList<>();
                accountUnits.add(accountUnitDto);
                dto.setAccountUnits(accountUnits);
                rtnList.add(dto);
            } else if (item.getIsEdited().equals(ZERO)) {// 添加上月继承(如果有数值)
                CostUserAttendance lastMonthData = getOne(new QueryWrapper<CostUserAttendance>()
                        .eq("emp_name", item.getEmpName())
                        .eq("dt", LocalDateTime.now().minusMonths(1))
                        .eq("account_unit", item.getAccountUnit())
                        .last("limit 1"));
                if (lastMonthData != null) {
                    BeanUtils.copyProperties(lastMonthData, dto);
                    String accountUnitInfo = item.getAccountUnit();
                    ObjectMapper mapper = new ObjectMapper();
                    AccountUnitDto accountUnitDto = new AccountUnitDto();
                    try {
                        JsonNode rootNode = mapper.readTree(accountUnitInfo);
                        JsonNode jsonObject = rootNode.get(0); // 获取第一个对象
                        accountUnitDto.setName(jsonObject.get("name").asText());
                        accountUnitDto.setId(jsonObject.get("id").asText());
                    } catch (Exception ignored) {
                    }
                    List<AccountUnitDto> accountUnits = new ArrayList<>();
                    accountUnits.add(accountUnitDto);
                    dto.setAccountUnits(accountUnits);
                    rtnList.add(dto);
                }
            }// 本月系统不展示

//            if(!item.getDt().equals(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM")))) {
//                BeanUtils.copyProperties(item, dto);
//                rtnList.add(dto);
//            }else {
//                BeanUtils.copyProperties(item, dto);
//                String accountUnitInfo = item.getAccountUnit();
//                ObjectMapper mapper = new ObjectMapper();
//                AccountUnitDto accountUnitDto = new AccountUnitDto();
//                try {
//                    JsonNode rootNode = mapper.readTree(accountUnitInfo);
//                    JsonNode jsonObject = rootNode.get(0); // 获取第一个对象
//                    accountUnitDto.setName(jsonObject.get("name").asText());
//                    accountUnitDto.setId(jsonObject.get("id").asText());
//                } catch (Exception ignored) {
//                }
//                List<AccountUnitDto> accountUnits = new ArrayList<>();
//                accountUnits.add(accountUnitDto);
//                dto.setAccountUnits(accountUnits);
//                rtnList.add(dto);
//            }
        }
        rtnPage.setRecords(rtnList);
        rtnPage.setCurrent(originData.getCurrent());
        rtnPage.setTotal(originData.getTotal());
        return rtnPage;
    }

    // 指完全没有人为改动，按照系统匹配规则来的数据显示在这里
    @Override
    public Page<CostUserAttendanceDto> sysMatchList(PageRequest<CostUserAttendance> pr) {
        Page<CostUserAttendanceDto> rtnPage = new Page<>();
        LambdaQueryWrapper<CostUserAttendance> qr = pr.getWrapper().lambda();
        qr.isNotNull(CostUserAttendance::getAccountUnit);
        qr.eq(CostUserAttendance::getIsEdited, "0");
        Page<CostUserAttendance> originData = page(pr.getPage(), qr);
        List<CostUserAttendance> current = originData.getRecords();
        List<CostUserAttendanceDto> rtnList = new ArrayList<>();
        for (CostUserAttendance item : current) {
            CostUserAttendanceDto dto = new CostUserAttendanceDto();
            BeanUtils.copyProperties(item, dto);
            String accountUnitInfo = item.getAccountUnit();
            ObjectMapper mapper = new ObjectMapper();
            AccountUnitDto accountUnitDto = new AccountUnitDto();
            try {
                JsonNode rootNode = mapper.readTree(accountUnitInfo);
                JsonNode jsonObject = rootNode.get(0); // 获取第一个对象
                accountUnitDto.setName(jsonObject.get("name").asText());
                accountUnitDto.setId(jsonObject.get("id").asText());
            } catch (Exception ignored) {
            }
            List<AccountUnitDto> accountUnits = new ArrayList<>();
            accountUnits.add(accountUnitDto);
            dto.setAccountUnits(accountUnits);
            rtnList.add(dto);
        }
        rtnPage.setRecords(rtnList);
        rtnPage.setCurrent(originData.getCurrent());
        rtnPage.setTotal(originData.getTotal());
        return rtnPage;
    }

    @Override
    public List<String> historyList(QueryWrapper<CostUserAttendance> wrapper) {
        List<String> rtnList = new ArrayList<>();
        List<CostUserAttendance> dateList = list();
        if (!CollectionUtils.isEmpty(dateList)) {
            Map<String, Long> monthlyCounts = dateList.stream().collect(Collectors.groupingBy(CostUserAttendance::getDt, Collectors.counting()));
            monthlyCounts.forEach((month, count) -> rtnList.add(month));
            rtnList.sort(Collections.reverseOrder());
        }
        return rtnList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean editWithCustomFields(CostUserAttendanceEditDto dto) {
        List<CostUserAttendance> customParams = dto.getCustomParams();
        if (CollUtil.isEmpty(customParams)) {
            throw new BizException("请传入编辑数据");
        }
        CostUserAttendance newEntity = customParams.get(0);
        // 获取周期月份的自然天数
        DateTime yyyyMM = DateUtil.parse(newEntity.getDt(), "yyyyMM");
        BigDecimal lastDayOfMonth = new BigDecimal(yyyyMM.getLastDayOfMonth());

        CostUserAttendance oldEntity = getById(newEntity.getId());
        List<AccountUnitDto> actList = newEntity.getAccountUnits();
        newEntity.setAccountUnit(dataProcessUtil.processList(actList));

        if (CollUtil.isEmpty(actList)) {
            throw new BizException("科室单元必填");
        }

        if (StrUtil.isBlank(newEntity.getReward())) {
            throw new BizException("是否拿奖金必填");
        }

        if (Objects.nonNull(newEntity.getAttendanceGroupDays())) {// 当前考勤组所在天数
            if (NumberUtil.isGreater(newEntity.getAttendanceGroupDays(), lastDayOfMonth) || NumberUtil.isLess(newEntity.getAttendanceGroupDays(), BigDecimal.ZERO)) {
                throw new BizException("当前考勤组所在天数填写有误");
            }
        }

        if (Objects.nonNull(newEntity.getAttendDays())) {// 实际出勤天数
            if (NumberUtil.isGreater(newEntity.getAttendDays(), lastDayOfMonth) || NumberUtil.isLess(newEntity.getAttendDays(), BigDecimal.ZERO)) {
                throw new BizException("出勤天数填写有误");
            }
        }

        if (StrUtil.isNotBlank(newEntity.getAttendCount())) {// 出勤次数
            if (!NumberUtil.isNumber(newEntity.getAttendCount())) {
                throw new BizException("出勤次数填写有误");
            }
            BigDecimal attendCountNumber = NumberUtil.toBigDecimal(newEntity.getAttendCount());
            if (NumberUtil.isLess(attendCountNumber, BigDecimal.ZERO)) {
                throw new BizException("出勤次数填写有误");
            }
        }

        // 处理自定义字段
        List<CostUserAttendanceCustomFields> customFields = costUserAttendanceCustomFieldsService.listByDt(newEntity.getDt());
        Map<String, CostUserAttendanceCustomFields> collect = customFields.stream().collect(Collectors.toMap(CostUserAttendanceCustomFields::getColumnId, e -> e, (v1, v2) -> v1));
        if (CollUtil.isNotEmpty(newEntity.getCustomFieldList())) {
            for (CustomFieldVO customFieldVO : newEntity.getCustomFieldList()) {
                if (!collect.containsKey(customFieldVO.getId())) {
                    throw new BizException(String.format("[%s][%s]自定义字段不存在", customFieldVO.getName(), customFieldVO.getId()));
                }
                CostUserAttendanceCustomFields customFieldsDB = collect.get(customFieldVO.getId());
                checkCustomField(customFieldsDB, customFieldVO.getNum(), lastDayOfMonth);
                customFieldVO.setName(customFieldsDB.getName());
            }
            List<String> customFieldStrList = newEntity.getCustomFieldList().stream().map(JSON::toJSONString).collect(Collectors.toList());
            newEntity.setCustomFields(CollUtil.join(customFieldStrList, ","));
        }
        super.

                update(Wrappers.<CostUserAttendance>lambdaUpdate().
                        set(CostUserAttendance::getDt, newEntity.getDt()).
                        set(CostUserAttendance::getEmpId, newEntity.getEmpId()).
                        set(CostUserAttendance::getEmpName, newEntity.getEmpName()).
                        set(CostUserAttendance::getAttendanceGroup, newEntity.getAttendanceGroup()).
                        set(CostUserAttendance::getUserType, newEntity.getUserType()).
                        set(CostUserAttendance::getDutiesName, newEntity.getDutiesName()).
                        set(CostUserAttendance::getAccountGroup, newEntity.getAccountGroup()).
                        set(CostUserAttendance::getTitles, newEntity.getTitles()).
                        set(CostUserAttendance::getAccountUnit, newEntity.getAccountUnit()).
                        set(CostUserAttendance::getDeptCode, newEntity.getDeptCode()).
                        set(CostUserAttendance::getDeptName, newEntity.getDeptName()).
                        set(CostUserAttendance::getAttendCount, newEntity.getAttendCount()).
                        set(CostUserAttendance::getAttendRate, newEntity.getAttendRate()).
                        set(CostUserAttendance::getRegisteredRate, newEntity.getRegisteredRate()).
                        set(CostUserAttendance::getJobNature, newEntity.getJobNature()).
                        set(CostUserAttendance::getAttendDays, newEntity.getAttendDays()).
                        set(CostUserAttendance::getPost, newEntity.getPost()).
                        set(CostUserAttendance::getReward, newEntity.getReward()).
                        set(CostUserAttendance::getRewardIndex, newEntity.getRewardIndex()).
                        set(CostUserAttendance::getNoRewardReason, newEntity.getNoRewardReason()).
                        set(CostUserAttendance::getAttendanceGroupDays, newEntity.getAttendanceGroupDays()).
                        set(CostUserAttendance::getOneKpiAttendDays, newEntity.getOneKpiAttendDays()).
                        set(CostUserAttendance::getOneKpiAttendRate, newEntity.getOneKpiAttendRate()).
                        set(CostUserAttendance::getTreatRoomDays, newEntity.getTreatRoomDays()).
                        set(CostUserAttendance::getCustomFields, newEntity.getCustomFields()).
                        set(CostUserAttendance::getOriginCustomFields, newEntity.getOriginCustomFields()).
                        eq(CostUserAttendance::getId, newEntity.getId()));
        // TODO:zyj 优化日志
        // 日志
        UserAttendanceLog attendanceLog = new UserAttendanceLog();
        PigxUser user = SecurityUtils.getUser();
        attendanceLog.setDt(newEntity.getDt());
        attendanceLog.setJobNumber(user.getJobNumber());
        attendanceLog.setOpsById(user.getId());
        attendanceLog.setOpsBy(user.getName());
        attendanceLog.setOpsTime(LocalDateTime.now());
        attendanceLog.setOpsType(ONE);
        attendanceLog.setOpsItem(oldEntity.getEmpName());
        // 逐个比对并记录原值和新值
        List<String> changes = new ArrayList<>();
        List<AccountUnitDto> newAccountUnitInfo = newEntity.getAccountUnits();
        if (!CollectionUtils.isEmpty(newAccountUnitInfo)) {
            String newAccountUnitName = newAccountUnitInfo.get(0).getName();
            // 更新考勤组
            String newAccountUnitId = newAccountUnitInfo.get(0).getId();
            CostAccountUnit costAccountUnit = costAccountUnitService.getById(newAccountUnitId);
            newEntity.setAccountGroup(costAccountUnit.getAccountGroupCode());
            changes.add(String.format("科室单元变更为【%S】", newAccountUnitName));
            newEntity.setIsEdited("1");// 标为已编辑
        }
        if (!StrUtil.equals(oldEntity.getAttendanceGroup(), newEntity.getAttendanceGroup())) {
            changes.add(String.format(UPDATE, "考勤组", oldEntity.getAttendanceGroup(), newEntity.getAttendanceGroup()));
        }
        if (!StrUtil.equals(oldEntity.getDutiesName(), newEntity.getDutiesName())) {
            changes.add(String.format(UPDATE, "职务", oldEntity.getDutiesName(), newEntity.getDutiesName()));
        }
        if (!StrUtil.equals(oldEntity.getTitles(), newEntity.getTitles())) {
            changes.add(String.format(UPDATE, "职称", oldEntity.getTitles(), newEntity.getTitles()));
        }
        if (!StrUtil.equals(oldEntity.getDeptCode(), newEntity.getDeptCode())) {
            changes.add(String.format(UPDATE, "科室编码", oldEntity.getDeptCode(), newEntity.getDeptCode()));
        }
        if (!StrUtil.equals(oldEntity.getDeptName(), newEntity.getDeptName())) {
            changes.add(String.format(UPDATE, "科室名称", oldEntity.getDeptName(), newEntity.getDeptName()));
        }
        if (!StrUtil.equals(oldEntity.getAttendCount(), newEntity.getAttendCount())) {
            changes.add(String.format(UPDATE, "出勤次数", oldEntity.getAttendCount(), newEntity.getAttendCount()));
        }
        if (!NumberUtil.equals(oldEntity.getAttendRate(), newEntity.getAttendRate())) {
            changes.add(String.format(UPDATE, "出勤系数", oldEntity.getAttendRate(), newEntity.getAttendRate()));
        }
        if (!NumberUtil.equals(oldEntity.getRegisteredRate(), newEntity.getRegisteredRate())) {
            changes.add(String.format(UPDATE, "在册系数", oldEntity.getRegisteredRate(), newEntity.getRegisteredRate()));
        }
        if (!StrUtil.equals(oldEntity.getJobNature(), newEntity.getJobNature())) {
            changes.add(String.format(UPDATE, "工作性质", oldEntity.getJobNature(), newEntity.getJobNature()));
        }
        if (!NumberUtil.equals(oldEntity.getAttendDays(), newEntity.getAttendDays())) {
            changes.add(String.format(UPDATE, "实际出勤天数", oldEntity.getAttendDays(), newEntity.getAttendDays()));
        }
        if (!StrUtil.equals(oldEntity.getPost(), newEntity.getPost())) {
            changes.add(String.format(UPDATE, "岗位", oldEntity.getPost(), newEntity.getPost()));
        }
        if (!StrUtil.equals(oldEntity.getReward(), newEntity.getReward())) {
            changes.add(String.format(UPDATE, "是否拿奖金", oldEntity.getReward(), newEntity.getReward()));
        }
        if (!NumberUtil.equals(oldEntity.getRewardIndex(), newEntity.getRewardIndex())) {
            changes.add(String.format(UPDATE, "奖金系数", oldEntity.getRewardIndex(), newEntity.getRewardIndex()));
        }
        if (!StrUtil.equals(oldEntity.getNoRewardReason(), newEntity.getNoRewardReason())) {
            changes.add(String.format(UPDATE, "不拿奖金原因", oldEntity.getNoRewardReason(), newEntity.getNoRewardReason()));
        }
        if (!NumberUtil.equals(oldEntity.getAttendanceGroupDays(), newEntity.getAttendanceGroupDays())) {
            changes.add(String.format(UPDATE, "当前考勤组所在天数", oldEntity.getAttendanceGroupDays(), newEntity.getAttendanceGroupDays()));
        }
        if (!NumberUtil.equals(oldEntity.getOneKpiAttendDays(), newEntity.getOneKpiAttendDays())) {
            changes.add(String.format(UPDATE, "一次性绩效出勤天数", oldEntity.getOneKpiAttendDays(), newEntity.getOneKpiAttendDays()));
        }
        if (!NumberUtil.equals(oldEntity.getOneKpiAttendRate(), newEntity.getOneKpiAttendRate())) {
            changes.add(String.format(UPDATE, "一次性绩效出勤系数", oldEntity.getOneKpiAttendRate(), newEntity.getOneKpiAttendRate()));
        }
        if (!NumberUtil.equals(oldEntity.getTreatRoomDays(), newEntity.getTreatRoomDays())) {
            changes.add(String.format(UPDATE, "中治室", oldEntity.getTreatRoomDays(), newEntity.getTreatRoomDays()));
        }
        try {
            if (CollUtil.isNotEmpty(customFields)) {
                // 自定义字段数值读取
                String oldCustomFields = oldEntity.getCustomFields();
                String newCustomFields = newEntity.getCustomFields();
                String oldStr = "[" + oldCustomFields.replaceAll("}(?=,)", "},") + "]";
                String newStr = "[" + newCustomFields.replaceAll("}(?=,)", "},") + "]";
                List<CustomFieldVO> oldCustomFieldVOS = JSON.parseArray(oldStr, CustomFieldVO.class);
                List<CustomFieldVO> newCustomFieldVOS = JSON.parseArray(newStr, CustomFieldVO.class);
                for (CostUserAttendanceCustomFields customField : customFields) {
                    String columnId = customField.getColumnId();
                    CustomFieldVO oldCustomFieldVO = CollUtil.getFirst(CollUtil.filter(oldCustomFieldVOS, e -> e.getId().equals(columnId)));
                    CustomFieldVO newCustomFieldVO = CollUtil.getFirst(CollUtil.filter(newCustomFieldVOS, e -> e.getId().equals(columnId)));
                    String oldValue = Objects.isNull(oldCustomFieldVO) ? null : oldCustomFieldVO.getNum();
                    String newValue = Objects.isNull(newCustomFieldVO) ? null : oldCustomFieldVO.getNum();
                    if (!Objects.equals(oldValue, newValue)) {
                        changes.add(String.format(UPDATE, customField.getName(), oldValue, newValue));
                    }
                }
            }
        } catch (Exception e) {
            log.info("生成自定义字段变更日志失败", e);
        }
        attendanceLog.setDescription(changes.toString());
        userAttendanceLogService.save(attendanceLog);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateData(CostUserAttendanceEditDto costUserAttendanceList) {
        String dt = costUserAttendanceList.getDt();
        for (CostUserAttendance costUserAttendance : costUserAttendanceList.getCustomParams()) {
            CostUserAttendance oldEntity = getById(costUserAttendance.getId());
            List<AccountUnitDto> actList = costUserAttendance.getAccountUnits();
            costUserAttendance.setAccountUnit(dataProcessUtil.processList(actList));
            // 日志
            UserAttendanceLog log = new UserAttendanceLog();
            PigxUser user = SecurityUtils.getUser();
            log.setDt(dt);
            log.setJobNumber(user.getJobNumber());
            log.setOpsById(user.getId());
            log.setOpsBy(user.getName());
            log.setOpsTime(LocalDateTime.now());
            log.setOpsType(ONE);
            log.setOpsItem(oldEntity.getEmpName());
            // 逐个比对并记录原值和新值
            StringBuilder changes = new StringBuilder();
            List<AccountUnitDto> newAccountUnitInfo = costUserAttendance.getAccountUnits();
            if (!CollectionUtils.isEmpty(newAccountUnitInfo)) {
                String newAccountUnitName = newAccountUnitInfo.get(0).getName();
                // 更新考勤组
                String newAccountUnitId = newAccountUnitInfo.get(0).getId();
                CostAccountUnit costAccountUnit = costAccountUnitService.getById(newAccountUnitId);
                costUserAttendance.setAccountGroup(costAccountUnit.getAccountGroupCode());
                changes.append("科室单元变更为 ").append(newAccountUnitName);
                costUserAttendance.setIsEdited("1");// 标为已编辑
            }
            String newUserTypeInfo = costUserAttendance.getUserType();
            String oldUserTypeInfo = oldEntity.getUserType();
            if (newUserTypeInfo != null) {
                JSONObject newUserTypeJson = JSONObject.parseObject(newUserTypeInfo);
                String newUserType = newUserTypeJson.getString("label");
                JSONObject oldUserTypeJson = JSONObject.parseObject(oldUserTypeInfo);
                String oldUserType = ObjectUtil.isNotEmpty(oldUserTypeInfo) ? oldUserTypeJson.getString("label") : "";
                if (ObjectUtil.isNotEmpty(newUserType)) {
                    changes.append(String.format(UPDATE, "人员类型", oldUserType, newUserType));
                }
            }
            if (ObjectUtil.isNotEmpty(costUserAttendance.getAttendDays())) {
                changes.append(String.format(UPDATE, "出勤次数", oldEntity.getAttendDays(), costUserAttendance.getAttendDays()));
            }
            if (ObjectUtil.isNotEmpty(costUserAttendance.getRegisteredRate())) {
                changes.append(String.format(UPDATE, "在册系数", oldEntity.getRegisteredRate(), costUserAttendance.getRegisteredRate()));
            }
            log.setDescription(changes.toString());


            userAttendanceLogService.save(log);
        }
        return updateBatchById(costUserAttendanceList.getCustomParams());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean lockData(String dt) {
        boolean rtn = false; // 是否有数据被锁定
        LambdaQueryWrapper<CostUserAttendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CostUserAttendance::getDt, dt);
        List<CostUserAttendance> rtnList = list(wrapper);
        for (CostUserAttendance item : rtnList) {
            if (item.getIsLocked().equals("1")) {
                item.setIsLocked("0");
            } else {
                rtn = true;
                item.setIsLocked("1");
            }
        }
        // 将该月数据移植到原考勤表(cost_attendance)并删除同核算周期内旧数据
        if (rtn) {
            List<Attendance> targetList = new ArrayList<>();
            List<CostAccountUnit> costAccountUnits = costAccountUnitService.list();
            Map<Long, CostAccountUnit> costUserAttendanceMap = costAccountUnits.stream().collect(Collectors.toMap(CostAccountUnit::getId, a -> a));
            for (CostUserAttendance item : rtnList) {
                Attendance attendance = new Attendance();
                try {
                    UserInfo userInfo = remoteUserService.allInfoByJobNumber(item.getEmpId()).getData();
                    SysUser user = userInfo == null ? new SysUser() : userInfo.getSysUser();
                    attendance.setUserId(user.getUserId());
                    attendance.setEmpCode(item.getEmpId());
                    attendance.setEmpName(user.getName());
                    attendance.setCycle(item.getDt());
                    if (!StringUtil.isEmpty(item.getAccountUnit())) {
                        JSONArray accountUnitJsonArray = JSONObject.parseArray(item.getAccountUnit());
                        JSONObject firstObject = accountUnitJsonArray.getJSONObject(0);
                        String unitName = firstObject.getString("name");
                        String unitId = firstObject.getString("id");
                        attendance.setAccountUnitId(unitId);
                        CostAccountUnit costAccountUnit = costUserAttendanceMap.get(Long.valueOf(unitId));
                        if (ObjectUtil.isEmpty(costAccountUnit)) {
                            attendance.setGroupName("");
                        } else {
                            String grpIngo = costAccountUnit.getAccountGroupCode();
                            JSONObject grpJson = JSONObject.parseObject(grpIngo);
                            String grpName = grpJson.getString("label");
                            attendance.setGroupName(grpName);
                            attendance.setAccountUnitName(unitName);
                            attendance.setTitle(item.getTitles());
                        }
                    }
                    if (StringUtils.isNotBlank(item.getDeptCode())) {
                        String deptCode = item.getDeptCode();
                        R<SysDept> sysDeptR = remoteDeptService.getByCode(deptCode, SecurityConstants.FROM_IN);
                        if (sysDeptR.isOk() && sysDeptR.getData() != null) {
                            SysDept sysDept = sysDeptR.getData();
                            attendance.setDeptId(sysDept.getDeptId());
                            attendance.setDeptName(sysDept.getName());
                        } else {
                            log.warn("根据部门编码获取部门信息失败，部门编码：" + deptCode);
                        }
                    }
                    attendance.setWorkRate(item.getAttendRate() + "");
                    attendance.setGroupWorkdays(item.getAttendanceGroupDays() + "");
                    attendance.setZaiceRate(item.getRegisteredRate() + "");
                    attendance.setWorkType(item.getJobNature());
                    attendance.setWorkdayszl(item.getAttendDays() + "");
                    attendance.setIfGetAmt(item.getReward().equals(ZERO) ? "否" : "是");
                    attendance.setCustomFields(item.getCustomFields());
                    attendance.setTenantId(user.getTenantId());
                    attendance.setWorkdays(String.valueOf(item.getAttendanceGroupDays()));
                    targetList.add(attendance);
                } catch (Exception e) {
                    log.error("锁定数据时，用户信息获取失败:" + item.getEmpName(), e);
                }
            }
            // 删除同周期的原数据(如果有)
            attendanceService.remove(new QueryWrapper<Attendance>().eq("cycle", dt));
            attendanceService.saveBatch(targetList);
            System.out.println("数据移植完成");
        }
        return updateBatchById(rtnList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IPage<CostUserAttendance> pageData(Page<CostUserAttendance> page, QueryWrapper<CostUserAttendance> wrapper) {
        LambdaQueryWrapper<CostUserAttendance> qr = wrapper.lambda();
        qr.orderByAsc(CostUserAttendance::getDt);
        IPage<CostUserAttendance> pageData = page(page, qr);
        List<CostUserAttendance> costUserAttendanceList = pageData.getRecords();
        if (costUserAttendanceList.isEmpty()) {
            return null;
        }
        for (CostUserAttendance costUserAttendance : costUserAttendanceList) {
            // 自定义字段数值读取
            String customFields = costUserAttendance.getCustomFields();
            try {
                String inputList = "[" + customFields.replaceAll("}(?=,)", "},") + "]";
                List<CustomFieldVO> customFieldVOS = new ArrayList<>();
                JSONArray jsonArray = JSONArray.parseArray(inputList);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.getString("id");
                    String name = jsonObject.getString("name");
                    String num = jsonObject.getString("num");
                    CustomFieldVO customFieldVO = new CustomFieldVO(id, name, num);
                    customFieldVOS.add(customFieldVO);
                }
                costUserAttendance.setCustomFieldList(customFieldVOS);
            } catch (Exception e) {
                log.error("自定义字段解析异常", e);
            }
            // 科室单元信息
            String accountUnitInfo = costUserAttendance.getAccountUnit();
            ObjectMapper mapper = new ObjectMapper();
            AccountUnitDto accountUnitDto = new AccountUnitDto();
            try {
                JsonNode rootNode = mapper.readTree(accountUnitInfo);
                JsonNode jsonObject = rootNode.get(0); // 获取第一个对象
                String id = jsonObject.get("id").asText();
                String name = jsonObject.get("name").asText();
                accountUnitDto.setName(name);
                accountUnitDto.setId(id);
                try {
                    CostAccountUnit unitInfo = costAccountUnitService.getById(id);
                    if (unitInfo != null) {
                        String actGrp = unitInfo.getAccountGroupCode();
                        costUserAttendance.setAccountGroup(actGrp);
                    }
                } catch (Exception ignored) {

                }
            } catch (Exception ignored) {
            }
            List<AccountUnitDto> accountUnits = new ArrayList<>();
            accountUnits.add(accountUnitDto);
            costUserAttendance.setAccountUnits(accountUnits);
        }
//        List<CostUserAttendance> rtnList = renderData(costUserAttendanceList);
//        updateBatchById(rtnList); 计算后更新到数据库
//        pageData.setRecords(rtnList);
        return pageData;
    }

    private List<CostUserAttendance> renderData(List<CostUserAttendance> costUserAttendanceList) {
        String date = costUserAttendanceList.get(0).getDt();
        LocalDate localDate = LocalDate.of(Integer.parseInt(date.substring(0, 4)), Integer.parseInt(date.substring(4, 6)), 1);
        int daysOfMonth = localDate.lengthOfMonth();
        for (CostUserAttendance costUserAttendance : costUserAttendanceList) {
            // 自定义字段数值读取
            String customFields = costUserAttendance.getCustomFields();
            try {
                String inputList = "[" + customFields.replaceAll("}(?=,)", "},") + "]";
                List<CustomFieldVO> customFieldVOS = new ArrayList<>();
                JSONArray jsonArray = JSONArray.parseArray(inputList);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.getString("id");
                    String name = jsonObject.getString("name");
                    String num = jsonObject.getString("num");
                    CustomFieldVO customFieldVO = new CustomFieldVO(id, name, num);
                    customFieldVOS.add(customFieldVO);
                }
                costUserAttendance.setCustomFieldList(customFieldVOS);
            } catch (Exception e) {
                log.error("自定义字段解析异常", e);
            }
            // 科室单元信息
            String accountUnitInfo = costUserAttendance.getAccountUnit();
            ObjectMapper mapper = new ObjectMapper();
            AccountUnitDto accountUnitDto = new AccountUnitDto();
            try {
                JsonNode rootNode = mapper.readTree(accountUnitInfo);
                JsonNode jsonObject = rootNode.get(0); // 获取第一个对象
                String id = jsonObject.get("id").asText();
                String name = jsonObject.get("name").asText();
                accountUnitDto.setName(name);
                accountUnitDto.setId(id);
                try {
                    CostAccountUnit unitInfo = costAccountUnitService.getById(id);
                    if (unitInfo != null) {
                        String actGrp = unitInfo.getAccountGroupCode();
                        costUserAttendance.setAccountGroup(actGrp);
                    }
                } catch (Exception ignored) {

                }
            } catch (Exception ignored) {
            }

//            实际出勤天数 - 动态公式计算
            if (StringUtils.isNotEmpty(costUserAttendance.getAccountUnit())) {
                if (costUserAttendance.getAccountUnit().contains("中治室") && "1".equals(costUserAttendance.getIsEdited())) {
                    System.out.println("中治室,跳过计算");
                } else {
                    costUserAttendance.setAttendDays(new BigDecimal(firstDistributionAttendanceFormulaService.calculateAttendDays(FORMULA_ID_SJCQTS, costUserAttendance)));
                    // 一次性绩效出勤天数 - 公式计算
                    costUserAttendance.setOneKpiAttendDays(new BigDecimal(firstDistributionAttendanceFormulaService.calculateAttendDays(FORMULA_ID_YCXJXCQTS, costUserAttendance)));
                }
            } else {
                costUserAttendance.setAttendDays(new BigDecimal(firstDistributionAttendanceFormulaService.calculateAttendDays(FORMULA_ID_SJCQTS, costUserAttendance)));
                // 一次性绩效出勤天数 - 公式计算
                costUserAttendance.setOneKpiAttendDays(new BigDecimal(firstDistributionAttendanceFormulaService.calculateAttendDays(FORMULA_ID_YCXJXCQTS, costUserAttendance)));
            }

            // 出勤系数
            if (costUserAttendance.getReward().equals("1")) {
                BigDecimal attendDays = costUserAttendance.getAttendDays();
                BigDecimal attendRate = attendDays.divide(BigDecimal.valueOf(daysOfMonth), 6, RoundingMode.HALF_UP);
                costUserAttendance.setAttendRate(attendRate);
            } else {
                costUserAttendance.setAttendRate(new BigDecimal(0));
            }
            // 在册系数 逻辑
            if (customFields.contains("中治室")) {
                costUserAttendance.setRegisteredRate(costUserAttendance.getAttendRate());
            } else if (Objects.equals(costUserAttendance.getRewardIndex(), new BigDecimal(0))) {
                costUserAttendance.setRegisteredRate(new BigDecimal(0));
            } else {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(costUserAttendance.getAccountGroup());
                    String labelValue = jsonObject.getString("label");
                    if (medGroup1.contains(labelValue)) {  // 核算分组如果是医生组、医技组、护理组=当前考勤组所在天数/自然月天数*奖金系数
                        costUserAttendance.setRegisteredRate(costUserAttendance.getAttendanceGroupDays().divide(new BigDecimal(daysOfMonth), MathContext.DECIMAL64).multiply(costUserAttendance.getRewardIndex()));
                    } else if (medGroup2.contains(labelValue)) {  // 核算分组如果是行政组、药剂组=奖金系数
                        costUserAttendance.setRegisteredRate(costUserAttendance.getRewardIndex());
                    }
                } catch (Exception ignored) {
                }
            }
            // 一次性绩效系数
            BigDecimal oneKpiAttendDays = costUserAttendance.getOneKpiAttendDays();
            BigDecimal oneKpiAttendRate = oneKpiAttendDays.divide(BigDecimal.valueOf(daysOfMonth), 6, RoundingMode.HALF_UP);
            costUserAttendance.setOneKpiAttendRate(oneKpiAttendRate);

            // 出勤次数 ：需要算出勤次数的人员为叶海、洪善贻、王晖+工作性质是柔性引进的人员，计算规则：天数/0.5（半天算一次），其他人都展示0
            if (StringUtil.isEmpty(costUserAttendance.getAttendCount()) || costUserAttendance.getAttendCount().equals(ZERO)) {
                if (empList.contains(costUserAttendance.getEmpName()) && costUserAttendance.getJobNature().equals("柔性引进")) {
                    BigDecimal attendDays = costUserAttendance.getAttendDays();
                    BigDecimal attendTimes = attendDays.divide(new BigDecimal("0.5"), 6, RoundingMode.HALF_UP);
                    costUserAttendance.setAttendCount(attendTimes + "");
                } else {
                    costUserAttendance.setAttendCount(ZERO);
                }
            }

            List<AccountUnitDto> accountUnits = new ArrayList<>();
            accountUnits.add(accountUnitDto);
            costUserAttendance.setAccountUnits(accountUnits);
        }
        return costUserAttendanceList;
    }

    /**
     * 每个月8号获取上月人员考勤数据的任务
     */
    @XxlJob("importUserAttendanceDataJob")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importData(String dt) throws IOException {

        // String lastDt;
        // if (StringUtil.isEmpty(dt)) {
        //     Date now = new Date();
        //     dt = DateUtil.format(DateUtil.offsetMonth(now, -1), "yyyyMM");
        //     lastDt = DateUtil.format(DateUtil.offsetMonth(now, -2), "yyyyMM");
        // } else {
        //     DateTime yyyyMM = DateUtil.parse(dt, "yyyyMM");
        //     lastDt = DateUtil.format(DateUtil.offsetMonth(yyyyMM, -1), "yyyyMM");
        // }
        // int daysOfMonth = DateUtil.parse(dt, "yyyyMM").getLastDayOfMonth();
        // // 查询costUserAttendanceConfigService数据
        // CostUserAttendanceConfig costUserAttendanceConfig = costUserAttendanceConfigService.getByDt(dt);
        // if (Objects.isNull(costUserAttendanceConfig)) {
        //     costUserAttendanceConfig = new CostUserAttendanceConfig();
        //     costUserAttendanceConfig.setDt(dt);
        //     // 默认导入模式
        //     costUserAttendanceConfig.setPattern("1");
        //     costUserAttendanceConfigService.save(costUserAttendanceConfig);
        // }
        // if (Objects.equals("1", costUserAttendanceConfig.getPattern())) {
        //     log.info("导入模式，不走数据中台采集数据");
        //     return;
        // }
        // // 删除同周期的原数据(如果有)
        // remove(new QueryWrapper<CostUserAttendance>().eq("dt", dt));
        // List<CostUserAttendance> list = dmoUtil.userAttendanceList(dt);
        //
        // InnerAllDataVO rsData = remoteMappingBaseService.allData(MappingGroupAttributeEnum.COST.getCode(), rsPurpose, SecurityConstants.FROM_IN).getData();
        //
        // List<CostUserAttendance> rtnList = new ArrayList<>(); // 返回的数据
        // for (CostUserAttendance item : list) {
        //     // 1.尝试继承上月科室单元信息
        //     CostUserAttendance dataLastMonth = getOne(new QueryWrapper<CostUserAttendance>()
        //             .eq("emp_name", item.getEmpName())
        //             .eq("dt", lastDt)
        //             .eq("is_edited", "1")
        //             .eq("attendance_group", item.getAttendanceGroup())
        //             .last("limit 1"));
        //     if (dataLastMonth != null && dataLastMonth.getAccountUnit() != null) {
        //         item.setAccountUnit(dataLastMonth.getAccountUnit());
        //         String unitStr = dataLastMonth.getAccountUnit();
        //         JSONArray accountUnitJsonArray = JSONObject.parseArray(unitStr);
        //         JSONObject firstObject = accountUnitJsonArray.getJSONObject(0);
        //         String id = firstObject.getString("id");
        //         CostAccountUnit costAccountUnitInfo = costAccountUnitService.getById(id);
        //         item.setAccountGroup(costAccountUnitInfo.getAccountGroupCode());
        //         setGrpInfo(item, daysOfMonth);
        //     } else {
        //         // 2-1.当一个人事科室对应多个核算单元时，第一次配置时，科室单元空着，让用户在“变动人员处理”中确认该人员对应的科室单元，后面默认继承上次的科室单元
        //         String accountUnitInfo = item.getAccountUnit();
        //         JSONArray accountUnitJsonArray = JSONObject.parseArray(accountUnitInfo);
        //         JSONObject firstObject = accountUnitJsonArray.getJSONObject(0);
        //         String accountUnit = firstObject.getString("name");
        //
        //         // 人事系统下，所以只有一个核算单元
        //         rsData.autoFilter(null ,accountUnit);
        //         thirdAccountUnits = thirdAccountUnits.stream().filter(x -> x.getThirdId() == HRThirdId).collect(Collectors.toList());
        //         String unitIds = "";
        //         if (!CollectionUtils.isEmpty(thirdAccountUnits)) {
        //             unitIds = thirdAccountUnits.get(0).getUnitIds();
        //         }
        //         if (StringUtils.isEmpty(unitIds)) {
        //             item.setAccountUnit(null);
        //         } else if (unitIds.contains(",") || item.getJobNature().equals("柔性引进") || item.getJobNature().equals("退休返聘")) {
        //             // 2-2. 对应多条数据, 本月新增了柔性引进和退休返聘人员时，这些人的科室单元空着，让用户在“变动人员处理”中确认该人员对应的科室单元，后面默认继承上次的科室单元
        //             item.setAccountUnit(null);
        //         } else {
        //             try {
        //                 AccountUnitDto accountUnitIdAndNameDto = new AccountUnitDto();
        //                 String id = thirdAccountUnits.get(0).getUnitIds();
        //                 CostAccountUnit costAccountUnit = costAccountUnitService.getById(id);
        //                 accountUnitIdAndNameDto.setId(id);
        //                 accountUnitIdAndNameDto.setName(costAccountUnit.getName());
        //                 List<AccountUnitDto> accountUnitIdAndNameDtoList = new ArrayList<>();
        //                 accountUnitIdAndNameDtoList.add(accountUnitIdAndNameDto);
        //                 item.setAccountUnit(JSON.toJSONString(accountUnitIdAndNameDtoList));
        //                 item.setAccountGroup(costAccountUnit.getAccountGroupCode());
        //                 setGrpInfo(item, daysOfMonth);
        //             } catch (Exception ignored) {
        //             }
        //         }
        //     }
        //     // 3.解析中治室字段 等数据中台返参
        //     List<CustomFieldVO> customFieldVOS = getCustomFieldsList(item);
        //     for (CustomFieldVO customFieldVO : customFieldVOS) {
        //         if (customFieldVO.getName().equals(ZHONGZHISHI)) {
        //             CostUserAttendance extra = new CostUserAttendance();
        //             BeanUtils.copyProperties(item, extra);
        //             List<ThirdAccountUnit> thirdAccountUnits = remoteThirdAccountUnitService.getByName(ZHONGZHISHI).getData();
        //             thirdAccountUnits = thirdAccountUnits.stream().filter(x -> x.getThirdId() == HRThirdId).collect(Collectors.toList());
        //             List<AccountUnitDto> accountUnitDtos = new ArrayList<>();
        //             AccountUnitDto accountUnitDto = new AccountUnitDto();
        //             String id = thirdAccountUnits.get(0).getUnitIds();
        //             CostAccountUnit costAccountUnit = costAccountUnitService.getById(id);
        //             accountUnitDto.setId(id);
        //             accountUnitDto.setName(costAccountUnit.getName());
        //             accountUnitDtos.add(accountUnitDto);
        //             String accountUnitString = JSON.toJSONString(accountUnitDtos);
        //             extra.setAccountUnit(accountUnitString);
        //             extra.setAttendanceGroupDays(new BigDecimal(String.valueOf(customFieldVO.getNum())));
        //             extra.setIsEdited("1");// 用于计算公式识别，跳过计算逻辑
        //             extra.setAttendDays(new BigDecimal(String.valueOf(customFieldVO.getNum())));
        //             extra.setOneKpiAttendDays(new BigDecimal(String.valueOf(customFieldVO.getNum())));
        //             CostAccountUnit costAccountUnitInfo = costAccountUnitService.getById(id);
        //             extra.setAccountGroup(costAccountUnitInfo.getAccountGroupCode());
        //             setGrpInfo(extra, daysOfMonth);
        //             rtnList.add(extra);
        //         } else {
        //             item.setTreatRoomDays(new BigDecimal(0));
        //         }
        //     }
        // }
        // rtnList.addAll(list);
        // saveBatch(rtnList);
    }

    private void setGrpInfo(CostUserAttendance item, int daysOfMonth) {
        JSONObject jsonObject = JSONObject.parseObject(item.getAccountGroup());
        String labelValue = jsonObject.getString("label");
        // 在册系数 逻辑
        if (Objects.equals(item.getRewardIndex(), new BigDecimal(0))) {
            item.setRegisteredRate(new BigDecimal(0));
        } else if (medGroup1.contains(labelValue)) {  // 核算分组如果是医生组、医技组、护理组=当前考勤组所在天数/自然月天数*奖金系数
            item.setRegisteredRate(item.getAttendanceGroupDays().divide(new BigDecimal(daysOfMonth), MathContext.DECIMAL64).multiply(item.getRewardIndex()));
        } else if (medGroup2.contains(labelValue)) {  // 核算分组如果是行政组、药剂组=奖金系数
            item.setRegisteredRate(item.getRewardIndex());
        }
    }

    @Override
    public R validateData(String dt) {
        List<DimMonthEmpIncome> outpatientFeeList = dmoUtil.outpatientFeeList(dt);
        List<CostUserAttendance> list = list(new QueryWrapper<CostUserAttendance>().eq("dt", dt));
        // 找出人员id不在list中的数据，封装
        List<String> idList = list.stream().map(CostUserAttendance::getEmpId).collect(Collectors.toList());
        Map<String, String> map = new HashMap<>();
        for (DimMonthEmpIncome item : outpatientFeeList) {
            if (!idList.contains(item.getEmpCode())) {
                map.put(item.getEmpCode(), item.getEmpName());
            }
        }
        return R.ok(map);
    }

    @Override
    public List<ValidateJobNumberVO> validateJobNumber(String dt) {
        List<SysUser> sysUsers = remoteUserService.userListByCustom(new GetUserListCustomDTO(), SecurityConstants.FROM_IN).getData();
        List<String> jobNumberList = sysUsers.stream().map(SysUser::getJobNumber).filter(Objects::nonNull).collect(Collectors.toList());
        List<CostUserAttendance> costUserAttendances = list(new LambdaQueryWrapper<CostUserAttendance>().eq(CostUserAttendance::getDt, dt));
        List<ValidateJobNumberVO> results = new ArrayList<>();
        for (CostUserAttendance costUserAttendance : costUserAttendances) {
            if (!jobNumberList.contains(costUserAttendance.getEmpId())) {
                ValidateJobNumberVO validateJobNumberVO = new ValidateJobNumberVO();
                validateJobNumberVO.setEmpId(costUserAttendance.getEmpId());
                validateJobNumberVO.setEmpName(costUserAttendance.getEmpName());
                results.add(validateJobNumberVO);
            }
        }
        return results;
    }

    @Override
    public void exportData(PageRequest<CostUserAttendance> pr, HttpServletResponse response) {
        try {
            LambdaQueryWrapper<CostUserAttendance> qrTemp = pr.getWrapper().lambda();
            qrTemp.orderByAsc(CostUserAttendance::getDt);
            List<CostUserAttendance> list = list(qrTemp);
            List<CostUserAttendanceCustomFields> customFields = costUserAttendanceCustomFieldsService.listByDt((String.valueOf(pr.getQ().get("dt"))));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(UTF_8);
            EasyExcel.write(response.getOutputStream())
                    .autoCloseStream(true)
                    .sheet("模板")
                    .head(getHead(customFields))
                    .doWrite(getContent(list, customFields));
        } catch (Exception e) {
            log.error("导出报错", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateData(String dt) {
        System.out.println("开始计算...");
        // 查询costUserAttendanceConfigService数据
        CostUserAttendanceConfig costUserAttendanceConfig = costUserAttendanceConfigService.getByDt(dt);

        if (Objects.equals("1", costUserAttendanceConfig.getPattern())) {
            log.info("导入模式，不用计算");
            return;
        }
        LambdaQueryWrapper<CostUserAttendance> qr = new LambdaQueryWrapper<>();
        qr.eq(CostUserAttendance::getDt, dt);
        List<CostUserAttendance> costUserAttendanceList = list(qr);
        if (costUserAttendanceList.isEmpty()) {
            return;
        }
        List<CostUserAttendance> rtnList = renderData(costUserAttendanceList);
        updateBatchById(rtnList);
    }


    /**
     * 返回内容
     *
     * @param
     * @return 参数
     */
//    private List<List<Object>> getContent(List<CostUserAttendance> userList){
//        List<List<Object>> totalContent = new ArrayList<>();
//        for (CostUserAttendance costUserAttendance : userList) {
//            List<Object> list = new ArrayList<>();
//            list.add(costUserAttendance.getEmpName());
//            totalContent.add(list);
//        }
//        return totalContent;
//    }
    private List<List<Object>> getContent(List<CostUserAttendance> userList, List<CostUserAttendanceCustomFields> customFieldsList) throws JsonProcessingException {
        List<List<Object>> totalContent = new ArrayList<>();
        List<CostAccountUnit> costAccountUnits = costAccountUnitService.list();
        Map<Long, CostAccountUnit> map = costAccountUnits.stream().collect(Collectors.toMap(CostAccountUnit::getId, a -> a));
        for (CostUserAttendance costUserAttendance : userList) {
            log.info("time:{}", LocalDateTime.now());
            List<Object> list = new ArrayList<>();

            String accountGroupStr = "";
            // 科室单元
            try {
                String accountUnitStr = costUserAttendance.getAccountUnit();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(accountUnitStr);
                if (rootNode.isEmpty()) {
                    list.add("");
                } else {
                    JsonNode jsonObject = rootNode.get(0);
                    String name = jsonObject.get("name").asText();
                    String id = jsonObject.get("id").asText();
                    CostAccountUnit costAccountUnit = map.get(Long.parseLong(id));
                    accountGroupStr = costAccountUnit.getAccountGroupCode();
                    list.add(name);
                }
            } catch (Exception e) {
                list.add("");
            }
            // 考勤组
            list.add(costUserAttendance.getAttendanceGroup());
            // 姓名
            list.add(costUserAttendance.getEmpName());
            // 工号
            list.add(costUserAttendance.getEmpId());
            // 核算组别
            if (StringUtil.isEmpty(accountGroupStr)) {
                list.add("");
            } else {
                ObjectMapper mapper1 = new ObjectMapper();
                JsonNode rootNode1 = mapper1.readTree(accountGroupStr);
                String labelValue1 = rootNode1.get("label").asText();
                list.add(labelValue1);
            }
            // 工作性质
            list.add(costUserAttendance.getJobNature());
            // 职称
            list.add(costUserAttendance.getTitles());
            // 职务
            list.add(costUserAttendance.getDutiesName());
            // 岗位
            list.add(costUserAttendance.getPost());
            // 是否拿奖金
            list.add(costUserAttendance.getReward().equals("1") ? "是" : "否");
            // 奖金系数
            list.add(costUserAttendance.getRewardIndex());
            // 不拿奖金原因
            list.add(costUserAttendance.getNoRewardReason());
            // 当前考勤组所在天数
            list.add(costUserAttendance.getAttendanceGroupDays());
            // 出勤天数
            list.add(costUserAttendance.getAttendDays());
            // 出勤次数
            list.add(StringUtil.isEmpty(costUserAttendance.getAttendCount()) ? "0" : costUserAttendance.getAttendCount());
            // 出勤系数
            list.add(costUserAttendance.getAttendRate());
            // 在册系数
            list.add(costUserAttendance.getRegisteredRate());
            // 一次性绩效出勤天数
            list.add(costUserAttendance.getOneKpiAttendDays());
            // 一次性出勤系数
            list.add(costUserAttendance.getOneKpiAttendRate());

            List<CustomFieldVO> customFieldVOS = getCustomFieldsList(costUserAttendance);
            costUserAttendance.setCustomFieldList(customFieldVOS);

            for (CostUserAttendanceCustomFields customField : customFieldsList) {
                Object value = null;
                for (CustomFieldVO customFieldVO : customFieldVOS) {
                    if (Objects.equals(customField.getColumnId(), customFieldVO.getId())) {
                        value = customFieldVO.getNum();
                    }
                }
                list.add(value);
            }
            System.out.println("time end" + LocalDateTime.now());
            totalContent.add(list);
        }
        System.out.println("============================end===========================");
        return totalContent;
    }

    private List<CustomFieldVO> getCustomFieldsList(CostUserAttendance costUserAttendance) {
        String customFields = costUserAttendance.getCustomFields();
        String inputList = "[" + customFields.replaceAll("}(?=,)", "},") + "]";
        List<CustomFieldVO> customFieldVOS = new ArrayList<>();
        JSONArray jsonArray = JSONArray.parseArray(inputList);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String id = jsonObject.getString("id");
            String name = jsonObject.getString("name");
            String num = jsonObject.getString("num");
            CustomFieldVO customFieldVO = new CustomFieldVO(id, name, num);
            customFieldVOS.add(customFieldVO);
        }
        return customFieldVOS;
    }

    // 处理单元格数据
    private Object getCellValue(Cell cell) {
        Object result;
        switch (cell.getCellType()) {
            case STRING:
                result = cell.getStringCellValue();
                break;
            case NUMERIC:
                result = cell.getNumericCellValue();
                break;
            default:
                result = "";
        }
        return result;
    }

    /**
     * 表头
     */
    public static List<List<String>> getHead(List<CostUserAttendanceCustomFields> progDetailList) {
        List<List<String>> total = new ArrayList<>();
        // 固定字段
        total.add(ImmutableList.of("科室单元"));
        total.add(ImmutableList.of("考勤组"));
        total.add(ImmutableList.of("姓名"));
        total.add(ImmutableList.of("工号"));
        total.add(ImmutableList.of("核算组别"));
        total.add(ImmutableList.of("工作性质"));
        total.add(ImmutableList.of("职称"));
        total.add(ImmutableList.of("职务"));
        total.add(ImmutableList.of("岗位"));
        total.add(ImmutableList.of("是否拿奖金"));
        total.add(ImmutableList.of("奖金系数"));
        total.add(ImmutableList.of("不发奖金原因"));
        total.add(ImmutableList.of("当前考勤组所在天数"));
        total.add(ImmutableList.of("出勤天数"));
        total.add(ImmutableList.of("出勤次数"));
        total.add(ImmutableList.of("出勤系数"));
        total.add(ImmutableList.of("在册系数"));
        total.add(ImmutableList.of("一次性绩效出勤天数"));
        total.add(ImmutableList.of("一次性绩效出勤系数"));
        // 自定义字段
        for (CostUserAttendanceCustomFields detail : progDetailList) {
            total.add(ImmutableList.of(detail.getName()));
        }
        return total;
    }

    /**
     * 将实体类的字段转化为表头列表
     *
     * @param clazz 实体类Class对象
     * @return 包含字段名的表头列表
     */
    public static List<String> convertEntityToHeaders(Class<?> clazz) {
        List<String> headers = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Schema.class)) {
                Schema schema = field.getAnnotation(Schema.class);
                String description = schema.description();
                headers.add(description);
            }
        }
        return headers;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pullCustomFields(String dt) {
        // 推送前清空数据
        costUserAttendanceCustomFieldDataService.remove(Wrappers.<CostUserAttendanceCustomFieldData>lambdaQuery().eq(CostUserAttendanceCustomFieldData::getDt, dt));
        List<CostUserAttendance> list = list(new QueryWrapper<CostUserAttendance>().eq("dt", dt));
        List<CostUserAttendanceCustomFieldData> rtnList = new ArrayList<>();
        for (CostUserAttendance item : list) {
            CostUserAttendanceCustomFieldData customFieldData = new CostUserAttendanceCustomFieldData();

            customFieldData.setDt(dt);
            customFieldData.setEmpName(item.getEmpName());
            customFieldData.setEmpId(item.getEmpId());
            customFieldData.setUserAttendanceId(item.getId());

            // 自定义字段数值读取
            String customFields = item.getCustomFields();
            List<CustomFieldVO> customFieldVOS = new ArrayList<>();
            try {
                String inputList = "[" + customFields.replaceAll("}(?=,)", "},") + "]";
                JSONArray jsonArray = JSONArray.parseArray(inputList);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.getString("id");
                    String name = jsonObject.getString("name");
                    String num = jsonObject.getString("num");
                    CustomFieldVO customFieldVO = new CustomFieldVO(id, name, num);
                    customFieldVOS.add(customFieldVO);
                }
            } catch (Exception e) {
                log.error("自定义字段解析异常", e);
            }
            for (CustomFieldVO customField : customFieldVOS) {
                CostUserAttendanceCustomFieldData dataEntry = new CostUserAttendanceCustomFieldData();
                BeanUtils.copyProperties(customFieldData, dataEntry);

                dataEntry.setName(customField.getName());
                dataEntry.setColumnId(customField.getId());
                dataEntry.setValue(customField.getNum());
                rtnList.add(dataEntry);
            }
        }
        costUserAttendanceCustomFieldDataService.saveOrUpdateBatch(rtnList);
    }

    @Override
    @SneakyThrows
    public void downloadError(String dt, HttpServletResponse response) {
        String errorInfo = (String) redisUtil.get(CacheConstants.IMPORT_ERROR_COST_USER_ATTENDANCE + dt);
        if (StrUtil.isBlank(errorInfo)) {
            throw new BizException("找不到错误记录或错误记录已过期");
        }
        ImportErrVo importErrVo = JSON.parseObject(errorInfo, ImportErrVo.class);
        List<List<Object>> tableData = importErrVo.getDetails().stream().map(e -> {
            List<Object> rowData = new ArrayList<>();
            rowData.add(e.getLineNum());
            rowData.addAll(e.getData());
            rowData.add(e.getContent());
            return rowData;
        }).collect(Collectors.toList());
        EasyExcel.write(response.getOutputStream()).autoCloseStream(true).sheet("sheet1").head(importErrVo.getHead()).doWrite(tableData);
    }
}
