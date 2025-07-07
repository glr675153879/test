package com.hscloud.hs.cost.account.service.impl.dataReport;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bestvike.linq.Linq;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hscloud.hs.cost.account.constant.enums.SignEncryptType;
import com.hscloud.hs.cost.account.constant.enums.YesNoEnum;
import com.hscloud.hs.cost.account.constant.enums.dataReport.ReportRecordStatusEnum;
import com.hscloud.hs.cost.account.mapper.dataReport.CostReportRecordMapper;
import com.hscloud.hs.cost.account.model.dto.CostDataCollectionDto;
import com.hscloud.hs.cost.account.model.dto.CostImportErroDto;
import com.hscloud.hs.cost.account.model.dto.GatewayApiDto;
import com.hscloud.hs.cost.account.model.dto.dataReport.BatchAssignDto;
import com.hscloud.hs.cost.account.model.dto.dataReport.CostReportDetailInfoDto;
import com.hscloud.hs.cost.account.model.dto.dataReport.CostReportDetailRecordDto;
import com.hscloud.hs.cost.account.model.dto.dataReport.CostReportItemDto;
import com.hscloud.hs.cost.account.model.entity.DataCollectionUrl;
import com.hscloud.hs.cost.account.model.entity.dataReport.*;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import com.hscloud.hs.cost.account.model.pojo.ResponseData;
import com.hscloud.hs.cost.account.model.vo.dataReport.AssignResultVo;
import com.hscloud.hs.cost.account.service.CostAccountUnitService;
import com.hscloud.hs.cost.account.service.dataReport.*;
import com.hscloud.hs.cost.account.service.impl.DataCollectionUrlServiceImpl;
import com.hscloud.hs.cost.account.service.impl.kpi.KpiAccountUnitService;
import com.hscloud.hs.cost.account.utils.DataProcessUtil;
import com.hscloud.hs.cost.account.utils.ExcelUtil;
import com.hscloud.hs.cost.account.utils.GatewayApiClient;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import com.hscloud.hs.cost.account.utils.report.CircleUtil;
import com.pig4cloud.pigx.admin.api.dto.UserInfo;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteDictService;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.security.service.PigxUser;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.pig4cloud.pigx.common.core.constant.SecurityConstants.FROM_IN;
import static org.apache.commons.codec.CharEncoding.UTF_8;

/**
 * 我的上报 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CostReportRecordService extends ServiceImpl<CostReportRecordMapper, CostReportRecord> implements ICostReportRecordService {

    private final ICostReportDetailInfoService costReportDetailInfoService;
    private final ICostReportDetailCostService costReportDetailCostService;
    private final ICostReportTaskService costReportTaskService;
    private final ICostReportItemService costReportItemService;
    private final DataCollectionUrlServiceImpl dataCollectionUrlService;
    private final ICostReportRecordFileInfoService costReportRecordFileInfoService;
    private final CostAccountUnitService costAccountUnitService;
    private final RemoteUserService remoteUserService;
    private final DataProcessUtil dataProcessUtil;
    private final RedisUtil redisUtil;
    private final CostClusterUnitService costClusterUnitService;
    private final RemoteDictService remoteDictService;
    private final ExcelUtil excelUtil;
    private final StringRedisTemplate redisTemplate;

    private final IDrgsInfoService drgsInfoService;
    private final KpiAccountUnitService kpiAccountUnitService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<?> handleFileUpload(MultipartFile file, Long recordId) {
        int successCount = 0;  // 成功导入的数据条数
        int failCount = 0;  // 导入失败的数据条数
        int skipCount = 0;  // 导入跳过的数据的条数
        int size;  // 总数据条数

        Map<String, String> errorLogMap = new HashMap<>();
        // 转化数据
        if (!file.isEmpty()) {
            try (InputStream inputStream = file.getInputStream()) {

                // 获取核算分组字典
                Map<String, String> kpiAccountGroupMap = getAccountGroupMap();

                Workbook workbook = new XSSFWorkbook(inputStream);
                // 第一个工作表
                Sheet sheet = workbook.getSheetAt(0);
                // 获取行数
                size = sheet.getPhysicalNumberOfRows();

                List<Map<String, Object>> dataList = new ArrayList<>();

                // 列数
                Row headerRow = sheet.getRow(0);
                int colCount = headerRow.getPhysicalNumberOfCells();

                // 读取每一行数据
                for (int i = 1; i < size; i++) {
                    Row row = sheet.getRow(i);
                    Map<String, Object> data = new HashMap<>();
                    // 遍历每一列
                    for (int colIndex = 0; colIndex < colCount; colIndex++) {
                        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        String headerValue = headerRow.getCell(colIndex).getStringCellValue();
                        data.put(headerValue, excelUtil.getCellValue(cell));
                    }
                    dataList.add(data);
                }
                log.info("dataList: " + dataList);

                // 清空历史数据
                costReportDetailInfoService.remove(new LambdaQueryWrapper<CostReportDetailInfo>().eq(CostReportDetailInfo::getRecordId, recordId));
                costReportDetailCostService.remove(new LambdaQueryWrapper<CostReportDetailCost>().eq(CostReportDetailCost::getRecordId, recordId));

                // 保存数据到detailInfo表
                CostReportRecord record = getById(recordId);
                // 排除表头行
                int lineNo = 1;
                int columnNo;
                boolean isBad;
                for (int i = 0; i < dataList.size(); i++) {

                    Map<String, Object> data = dataList.get(i);

                    // 前置判断（核算项全为空就跳过）
                    int itemVal = 0, blankVal = 0;
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        String k = entry.getKey();
                        Object v = entry.getValue();
                        LambdaQueryWrapper<CostReportItem> qr = new LambdaQueryWrapper<>();
                        qr.eq(CostReportItem::getName, k);
                        CostReportItem item = costReportItemService.getOne(qr);
                        if (ObjectUtil.isNotEmpty(item)) {
                            // 是核算项
                            itemVal++;
                            if (StringUtils.isBlank(v.toString())) {
                                blankVal++;
                            }
                        }
                    }
                    log.info("核算项数量：{}，空白核算项数量：{}", itemVal, blankVal);
                    if (itemVal == blankVal) {
                        log.info("当前核算项的值为空，跳过");
                        continue;
                    }

                    // 数据导入
                    try {
                        isBad = false;
                        lineNo = i + 2;
                        columnNo = 0;
                        columnNo++;
                        boolean isMeasureType = true;
                        boolean isDeptDistinguished = false;
                        // 数据以及逻辑校验
                        CostReportDetailInfo costReportDetailInfo = new CostReportDetailInfo();
                        costReportDetailInfo.setRecordId(recordId);
                        String itemList = record.getItemList();
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            JsonNode jsonNodeArray = objectMapper.readTree(itemList);
                            for (JsonNode jsonNode : jsonNodeArray) {
                                CostReportItem item = new CostReportItem();
                                item.setIsDeptDistinguished(jsonNode.get("isDeptDistinguished").asText());
                                if (item.getIsDeptDistinguished().equals("1")) {
                                    isDeptDistinguished = true;
                                }
                            }
                        } catch (IOException e) {
                            log.warn("转换异常", e);
                        }
                        if (isDeptDistinguished) {
                            if (ObjectUtil.isNotEmpty(data.get("科别"))) {
                                String deptTypeName = data.get("科别").toString();
                                switch (deptTypeName) {
                                    case "门诊":
                                        costReportDetailInfo.setDeptType("OUTPATIENT");
                                        break;
                                    case "住院":
                                        costReportDetailInfo.setDeptType("HOSPITALIZATION");
                                        break;
                                    case "病区":
                                        costReportDetailInfo.setDeptType("WARD");
                                        break;
                                    default:
                                        log.error("科别无法被读取");
                                        errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 科别无法被读取");
                                        isBad = true;
                                        break;
                                }
                            } else {
                                log.error("科别为空");
                                errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 科别为空");
                                isBad = true;
                            }
                        }
                        // 口径颗粒度为全院时，只读取第一条数据
                        if (record.getReportType().equals("1")) {
                            // 如果已经存在数据，则忽视导入
                            LambdaQueryWrapper<CostReportDetailInfo> qr = new LambdaQueryWrapper<>();
                            qr.eq(CostReportDetailInfo::getRecordId, recordId);
                            List<CostReportDetailInfo> list = costReportDetailInfoService.list(qr);
                            if (!list.isEmpty()) {
                                break;
                            }
                            if (successCount > 0) {
                                break;
                            }
                        }
                        // 核算单元
                        if (record.getReportType().equals("2")) {
                            if (ObjectUtil.isNotEmpty(data.get("核算单元类型"))) {
                                String measureTypeName = data.get("核算单元类型").toString();
                                if (measureTypeName.equals("科室单元")) {
                                    isMeasureType = true;
                                } else if (measureTypeName.equals("归集单元")) {
                                    isMeasureType = false;
                                } else {
                                    log.error("核算单元类型无法被读取");
                                    errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 核算单元类型无法被读取");
                                    isBad = true;
                                }
                            } else {
                                log.error("核算单元类型为空");
                                errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 核算单元类型为空");
                                isBad = true;
                            }
                            if (ObjectUtil.isNotEmpty(data.get("核算单元名称"))) {
                                if (isMeasureType) {
                                    if (!ObjectUtil.isNotEmpty(data.get("核算分组"))) {
                                        log.error("核算分组为空");
                                        errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 核算分组为空");
                                        isBad = true;
                                    } else {
                                        String measureUnitName = data.get("核算单元名称").toString();
                                        String accountGroupName = data.get("核算分组").toString();
                                        String accountGroupCode = kpiAccountGroupMap.getOrDefault(accountGroupName, null);

                                        // todo 区分导入type（绩效这边的核算分组和科室成本那边的不一样）
                                        List<KpiAccountUnit> unitList = kpiAccountUnitService.list(Wrappers.<KpiAccountUnit>lambdaQuery()
                                                .eq(KpiAccountUnit::getName, measureUnitName)
                                                .eq(KpiAccountUnit::getCategoryCode, accountGroupCode)
                                                .eq(KpiAccountUnit::getStatus, 0)
                                                .eq(KpiAccountUnit::getBusiType, 1));

                                        if (CollectionUtil.isNotEmpty(unitList)) {
                                            costReportDetailInfo.setMeasureUnit(dataProcessUtil.processList(unitList));
                                            // 核算分组(组装) {"label":"行政组","value":"HSDX003"}
                                            String accountGroup = createAccountGroup(accountGroupName, accountGroupCode);
                                            costReportDetailInfo.setMeasureGroup(accountGroup);
                                        } else {
                                            log.error("核算单元名称或者分组有误");
                                            errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 核算单元名称或者分组有误");
                                            isBad = true;
                                        }
                                    }
                                } else {
                                    String clusterUnitName = data.get("核算单元名称").toString();
                                    LambdaQueryWrapper<CostClusterUnit> qr = new LambdaQueryWrapper<>();
                                    String formattedClusterUnitName = clusterUnitName;
                                    try {
                                        int jobIdInt = Double.valueOf(clusterUnitName).intValue();
                                        formattedClusterUnitName = String.valueOf(jobIdInt);
                                    } catch (Exception e) {
                                        log.error("归集单元名称无法被读取");
                                    }
                                    CostClusterUnit costClusterUnit = costClusterUnitService.getOne(qr
                                            .eq(CostClusterUnit::getName, formattedClusterUnitName)
                                            .last("LIMIT 1"));
                                    if (ObjectUtil.isNotEmpty(costClusterUnit)) {
                                        List<CostClusterUnit> costClusterUnits = new ArrayList<>();
                                        costClusterUnits.add(costClusterUnit);
                                        costReportDetailInfo.setClusterUnits(dataProcessUtil.processList(costClusterUnits));
                                    } else {
                                        log.error("核算单元名称或者分组有误");
                                        errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 核算单元名称或者分组有误");
                                        isBad = true;
                                    }
                                }
                            } else {
                                log.error("核算单元名称为空");
                                errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 核算单元名称为空");
                                isBad = true;
                            }
                        }

                        // 核算单元
                        if (record.getReportType().equals("7")) {
                            if (ObjectUtil.isNotEmpty(data.get("核算单元名称"))) {
                                if (isMeasureType) {
                                    String measureUnitName = data.get("核算单元名称").toString();

                                    List<KpiAccountUnit> unitList = kpiAccountUnitService.list(Wrappers.<KpiAccountUnit>lambdaQuery()
                                            .eq(KpiAccountUnit::getName, measureUnitName)
                                            .eq(KpiAccountUnit::getStatus, 0)
                                            .eq(KpiAccountUnit::getBusiType, 2));

                                    if (CollectionUtil.isNotEmpty(unitList)) {
                                        costReportDetailInfo.setMeasureUnit(dataProcessUtil.processList(unitList));
                                    } else {
                                        log.error("核算单元名称有误");
                                        errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 核算单元名称或者分组有误");
                                        isBad = true;
                                    }
                                }
                            } else {
                                log.error("核算单元名称为空");
                                errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 核算单元名称为空");
                                isBad = true;
                            }
                        }

                        // 备注字段添加
                        if (ObjectUtil.isNotEmpty(data.get("备注"))) {
                            costReportDetailInfo.setNote(data.get("备注").toString());
                        }

                        // 人员
                        if (record.getReportType().equals("4")) {
                            if (StrUtil.isNotBlank(data.get("核算单元名称").toString())) {
                                // 核算单元名称信息
                                String measureUnitName = data.get("核算单元名称").toString();

                                // todo 区分导入type（绩效这边的核算分组和科室成本那边的不一样）
                                List<KpiAccountUnit> unitList = kpiAccountUnitService.list(Wrappers.<KpiAccountUnit>lambdaQuery()
                                        .eq(KpiAccountUnit::getName, measureUnitName));

                                if (CollectionUtil.isNotEmpty(unitList)) {
                                    costReportDetailInfo.setMeasureUnit(dataProcessUtil.processList(unitList));
                                } else {
                                    log.error("核算单元名称有误");
                                    errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 核算单元名称或者分组有误");
                                    isBad = true;
                                }
                            }

                            // 人员信息
                            if (ObjectUtil.isNotEmpty(data.get("工号"))) {
                                columnNo++;
                                String jobNumber = data.get("工号").toString();

                                if (data.get("工号") instanceof Double) {
                                    jobNumber = String.valueOf(((Double) data.get("工号")).longValue());
                                }

//                                String jobId = data.get("工号").toString();
//                                String jobNumber = "";
//                                try {
//                                    int jobIdInt = Double.valueOf(jobId).intValue();
//                                    jobNumber = String.valueOf(jobIdInt);
//                                } catch (Exception e) {
//                                    log.info("工号为字符串");
//                                    jobNumber = jobId;
//                                }
                                // 获取人员信息
                                UserInfo userInfo = remoteUserService.infoByJobNumber(jobNumber, FROM_IN).getData();
                                if (ObjectUtil.isNotEmpty(userInfo)) {
                                    List<SysUser> userInfos = new ArrayList<>();
                                    userInfos.add(userInfo.getSysUser());
                                    costReportDetailInfo.setUser(dataProcessUtil.processList(userInfos));
                                    // 工号
                                    costReportDetailInfo.setJobNumber(jobNumber);
                                } else {
                                    log.error("人员无法被读取");
                                    errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 人员无法被读取,工号" + jobNumber);
                                    isBad = true;
                                }
                            } else {
                                log.error("工号为空");
                                errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 工号为空");
                                isBad = true;
                            }
                        }

                        // 核算单元+人员（导入的时候，核算单元或人员有一项导入即可）
                        if (record.getReportType().equals("3")) {

                            // 获取核算单元类型
                            if (ObjectUtil.isNotEmpty(data.get("核算单元类型"))) {
                                String measureTypeName = data.get("核算单元类型").toString();
                                if (measureTypeName.equals("科室单元")) {
                                    isMeasureType = true;
                                } else if (measureTypeName.equals("归集单元")) {
                                    isMeasureType = false;
                                } else if ("核算人员".equals(measureTypeName)) {

                                } else {
                                    log.error("核算单元类型无法被读取");
                                    errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 核算单元类型无法被读取");
                                    isBad = true;
                                }
                            } else {
                                log.error("核算单元类型为空");
                                errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 核算单元类型为空");
                                isBad = true;
                            }

                            // 判断核算单元或人员有一项导入
                            if (!isBad && (ObjectUtil.isEmpty(data.get("核算单元名称")) || ObjectUtil.isEmpty(data.get("核算分组")))
                                    && ObjectUtil.isEmpty(data.get("工号"))) {
                                errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 核算单元或人员信息都为空");
                                isBad = true;
                            }

                            if (!isBad && !(ObjectUtil.isEmpty(data.get("核算单元名称")) || ObjectUtil.isEmpty(data.get("核算分组")))) {
                                // 获取核算单元名称
                                if (ObjectUtil.isNotEmpty(data.get("核算单元名称"))) {
                                    if (ObjectUtil.isNotEmpty(data.get("核算分组"))) {
                                        if (isMeasureType) {
                                            String measureUnitName = data.get("核算单元名称").toString();
                                            String accountGroupName = data.get("核算分组").toString();
                                            String accountGroupCode = kpiAccountGroupMap.getOrDefault(accountGroupName, null);

                                            List<KpiAccountUnit> unitList = kpiAccountUnitService.list(Wrappers.<KpiAccountUnit>lambdaQuery()
                                                    .eq(KpiAccountUnit::getName, measureUnitName)
                                                    .eq(KpiAccountUnit::getCategoryCode, accountGroupCode));

                                            if (CollectionUtil.isNotEmpty(unitList)) {
                                                costReportDetailInfo.setMeasureUnit(dataProcessUtil.processList(unitList));
                                                // 核算分组
                                                String accountGroup = createAccountGroup(accountGroupName, accountGroupCode);
                                                costReportDetailInfo.setMeasureGroup(accountGroup);
                                            } else {
                                                log.error("核算单元名称或者分组有误");
                                                errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 核算单元名称或者分组有误");
                                                isBad = true;
                                            }
                                        } else {
                                            String clusterUnitName = data.get("核算单元名称").toString();
                                            LambdaQueryWrapper<CostClusterUnit> qr = new LambdaQueryWrapper<>();
                                            String formattedClusterUnitName = clusterUnitName;
                                            try {
                                                int jobIdInt = Double.valueOf(clusterUnitName).intValue();
                                                formattedClusterUnitName = String.valueOf(jobIdInt);
                                            } catch (Exception e) {
                                                log.error("归集单元名称无法被读取");
                                            }
                                            CostClusterUnit costClusterUnit = costClusterUnitService.getOne(qr
                                                    .eq(CostClusterUnit::getName, formattedClusterUnitName)
                                                    .last("LIMIT 1"));
                                            if (ObjectUtil.isNotEmpty(costClusterUnit)) {
                                                List<CostClusterUnit> costClusterUnits = new ArrayList<>();
                                                costClusterUnits.add(costClusterUnit);
                                                costReportDetailInfo.setClusterUnits(dataProcessUtil.processList(costClusterUnits));
                                            } else {
                                                log.error("核算单元名称或者分组有误");
                                                errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 核算单元名称或者分组有误");
                                                isBad = true;
                                            }
                                        }
                                    } else {
                                        log.error("核算分组为空");
                                        errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 核算分组为空");
                                        isBad = true;
                                    }
                                } else {
                                    log.error("核算单元名称为空");
                                    errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 核算单元名称为空");
                                    isBad = true;
                                }
                            } else if (!isBad && !ObjectUtil.isEmpty(data.get("工号"))) {
                                // 人员信息
                                if (ObjectUtil.isNotEmpty(data.get("工号"))) {
                                    columnNo++;
                                    String jobId = data.get("工号").toString();
                                    String jobNumber = "";
                                    try {
                                        int jobIdInt = Double.valueOf(jobId).intValue();
                                        jobNumber = String.valueOf(jobIdInt);
                                    } catch (Exception e) {
                                        log.info("工号为字符串");
                                        jobNumber = jobId;
                                    }
                                    // 获取人员信息
                                    UserInfo userInfo = remoteUserService.infoByJobNumber(jobNumber, FROM_IN).getData();
                                    if (ObjectUtil.isNotEmpty(userInfo)) {
                                        List<SysUser> userInfos = new ArrayList<>();
                                        userInfos.add(userInfo.getSysUser());
                                        costReportDetailInfo.setUser(dataProcessUtil.processList(userInfos));
                                        // 工号
                                        costReportDetailInfo.setJobNumber(jobNumber);
                                        costReportDetailInfo.setAccountingUnitType("2");
                                    } else {
                                        log.error("人员无法被读取");
                                        errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 人员无法被读取");
                                        isBad = true;
                                    }
                                } else {
                                    log.error("工号为空");
                                    errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, 工号为空");
                                    isBad = true;
                                }
                            }
                        }

                        costReportDetailInfoService.save(costReportDetailInfo);

                        // 是否撤回(当前行没有导入的数据，就skip)
                        boolean ifSkip = true;

                        // 保存数据到detailCost表
                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                            columnNo++;
                            String k = entry.getKey();
                            Object v = entry.getValue();

                            try {
                                List<CostReportDetailCost> costReportDetailCosts = new ArrayList<>();
                                LambdaQueryWrapper<CostReportItem> qr = new LambdaQueryWrapper<>();
                                qr.eq(CostReportItem::getName, k);
                                CostReportItem item = costReportItemService.getOne(qr);
                                if (ObjectUtil.isNotEmpty(item)) {
                                    // 核算项进行值校验
                                    String dataType = item.getDataType();
                                    long vInt = 0L;
                                    if (v instanceof String) {
                                        if (StringUtils.isNotBlank(v.toString()) && !isNumeric((String) v)) {
                                            errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, " + k + " 输入值不是数字");
                                            isBad = true;
                                        }
                                    } else if (v instanceof Double) {
                                        vInt = ((Double) v).longValue();
                                        switch (dataType) {
                                            case "0": // 正
                                                if (vInt < 0) {
                                                    errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, " + k + " 输入值为负数，应为正数");
                                                    isBad = true;
                                                }
                                                break;
                                            case "1": // 负
                                                if (vInt >= 0) {
                                                    errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, " + k + " 输入值为正数，应为负数");
                                                    isBad = true;
                                                }
                                                break;
                                        }
                                    }
                                    CostReportDetailCost costReportDetailCost = new CostReportDetailCost();
                                    costReportDetailCost.setName(k);
                                    costReportDetailCost.setRecordId(recordId);
                                    costReportDetailCost.setItemId(item.getId());
                                    costReportDetailCost.setDetailInfoId(costReportDetailInfo.getId());
                                    costReportDetailCost.setMeasureUnit(item.getMeasureUnit());
                                    costReportDetailCost.setDataType(item.getDataType());
                                    costReportDetailCosts.add(costReportDetailCost);
                                    if (!isBad && StringUtils.isNotBlank(v.toString())) {
                                        costReportDetailCost.setAmt(new BigDecimal(v + ""));
                                    }
                                }
                                if (!isBad) {
                                    if (costReportDetailCosts.size() > 0) {
                                        ifSkip = false;
                                    }
                                    costReportDetailCostService.saveBatch(costReportDetailCosts);
                                } else {
                                    costReportDetailInfoService.removeById(costReportDetailInfo);
                                    costReportDetailCostService.remove(Wrappers.<CostReportDetailCost>lambdaQuery()
                                            .eq(CostReportDetailCost::getDetailInfoId, costReportDetailInfo.getId())
                                            .eq(CostReportDetailCost::getRecordId, costReportDetailInfo.getRecordId()));
                                }
                            } catch (Exception e) {
                                errorLogMap.put((failCount) + "-" + columnNo, "第" + lineNo + "行, " + ((BizException) e).getDefaultMessage());
                                isBad = true;
                            }
                        }

                        if (!isBad) {
                            successCount++;
                        } else {
                            throw new BizException("数值校验异常");
                        }
                    } catch (Exception e) {
                        failCount++;
                    }
                }
                // todo 存日志
                String key = "errorLog" + SecurityUtils.getUser().getId();
                redisUtil.set(key, errorLogMap, 60, TimeUnit.MINUTES);// 一小时后过期
                if (successCount == size) {
                    return R.ok(successCount);
                } else {
                    Map<String, Object> result = new HashMap<>();
                    result.put("successCount", successCount);
                    result.put("failCount", failCount);
                    result.put("skipCount", skipCount);
                    List<CostImportErroDto> temp = new ArrayList<>();
                    errorLogMap.forEach((k, value) -> {
                        String s = k.split("-")[0];
                        String line = value.split(",")[0];
                        String errorInfo = value.replace(line + ",", "").trim();
                        CostImportErroDto costImportErroDto = Linq.of(temp).firstOrDefault(t -> t.getSeq() == Long.parseLong(s));
                        if (costImportErroDto == null) {
                            costImportErroDto = new CostImportErroDto();
                            costImportErroDto.setSeq(Long.parseLong(s));
                            costImportErroDto.setLine(line);
                            costImportErroDto.getErrorInfo().add(errorInfo);
                            temp.add(costImportErroDto);
                        } else {
                            costImportErroDto.getErrorInfo().add(errorInfo);
                        }
                    });
                    result.put("errorList", temp);
                    return R.ok(result);
                }
            } catch (IOException e) {
                throw new BizException("导入失败");
            }
        }
        return R.failed("空文件，导入失败!");
    }

    private Map<String, String> getAccountGroupMap() {
        // 声明出参
        Map<String, String> kpiAccountGroupMap = new HashMap<>();
        // 获取核算分组字典
        R<List<SysDictItem>> kpiCalculateGroupingR = remoteDictService.getDictByType("kpi_calculate_grouping");
        if (kpiCalculateGroupingR.isOk() && CollectionUtil.isNotEmpty(kpiCalculateGroupingR.getData())) {
            for (SysDictItem data : kpiCalculateGroupingR.getData()) {
                kpiAccountGroupMap.put(data.getLabel(), data.getItemValue());
            }
        }
        return kpiAccountGroupMap;
    }

    // 手动创建字典json格式
    private String createAccountGroup(String label, String value) {
        JSONObject accountGroup = new JSONObject();
        accountGroup.put("label", label);
        accountGroup.put("value", value);
        return accountGroup.toJSONString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssignResultVo assign(Long taskId) {
        // 获取当前上报任务
        CostReportTask costReportTask = costReportTaskService.getById(taskId);
        // 指定上报任务的下次核算周期作为下发周期
        AssignResultVo assign = assign(taskId, costReportTask.getCalculateCircle());
        if(assign == null){
            throw new BizException("下发失败");
        }
        if(assign.getCode() != 0){
            throw new BizException(assign.getMsg());
        }
        // 更新上报任务的下次核算周期
        costReportTaskService.updateNextCalculateCircle(costReportTask);
        return null;
    }

    /**
     * 分派
     *
     * @param taskId          任务ID
     * @param calculateCircle 下发周期
     * @return {@link AssignResultVo }
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssignResultVo assign(Long taskId, String calculateCircle) {
        CostReportTask costReportTask = costReportTaskService.getById(taskId);
        // 上报任务存在校验
        if (Objects.isNull(costReportTask)) {
            log.info("assign 上报任务不存在 id={}", taskId);
            AssignResultVo assignResultVo = new AssignResultVo();
            assignResultVo.setCode(1);
            assignResultVo.setMsg("上报任务不存在：" + taskId);
            return assignResultVo;
        }
        // 上报任务状态校验
        if (YesNoEnum.YES.getValue().equals(costReportTask.getStatus())) {
            log.info("assign 当前上报任务已被停用 id={} name:{}", taskId, costReportTask.getTaskName());
            AssignResultVo assignResultVo = new AssignResultVo();
            assignResultVo.setCode(1);
            assignResultVo.setMsg("上报任务被停用：" + costReportTask.getTaskName());
            return assignResultVo;
        }
        if (ifRwTaskDataNotInit(costReportTask.getReportType(), calculateCircle, costReportTask.getTaskName(), costReportTask.getType())) {
            log.info("assign 数据未获取完成,请抽取完成后下发 id={} name:{}", taskId, costReportTask.getTaskName());
            AssignResultVo assignResultVo = new AssignResultVo();
            assignResultVo.setCode(1);
            assignResultVo.setMsg("数据未获取完成,请抽取完成后下发：" + costReportTask.getTaskName());
            return assignResultVo;
        }

        // 判断当前任务是否已经初始化
        CostReportRecord costReportRecord;
        List<CostReportRecord> costReportRecords = list(Wrappers.<CostReportRecord>lambdaQuery()
                .eq(CostReportRecord::getTaskId, taskId)
                .eq(CostReportRecord::getCalculateCircle, calculateCircle));
        if (costReportRecords.size() > 1) {
            log.info("assign 当前任务下发找到多笔数据,请联系管理员处理 id={} name:{}", taskId, costReportTask.getTaskName());
            AssignResultVo assignResultVo = new AssignResultVo();
            assignResultVo.setCode(1);
            assignResultVo.setMsg("当前任务下发找到多笔数据,请联系管理员处理：" + costReportTask.getTaskName());
            return assignResultVo;
        } else if (costReportRecords.size() == 1) {
            costReportRecord = costReportRecords.get(0);
        } else {
            // 初始化任务 + 新下发任务
            costReportRecord = initRecord(costReportTask, null, calculateCircle);
            // 继承上个月上报任务数据
            inheritPreviousRecordHandle(costReportRecord.getId());
            updateReportRecordStatus(costReportRecord.getId(), ReportRecordStatusEnum.UNREPORT.getVal());
        }

        // 获取/预处理当前上报任务涉及上报项
        List<CostReportItemDto> itemVoList = costReportTask.queryItemVoList();
        Set<Long> itemSet = itemVoList.stream().map(CostReportItemDto::getId).map(Long::valueOf).collect(Collectors.toSet());

        if (ReportRecordStatusEnum.INIT.getVal().equals(costReportRecord.getStatus())) {
            // 状态：任务已初始化，未下发
            updateReportRecordStatus(costReportRecord.getId(), ReportRecordStatusEnum.UNREPORT.getVal());
            log.info("assign 更新为下发状态 id={} name:{}", taskId, costReportTask.getTaskName());
            AssignResultVo assignResultVo = new AssignResultVo();
            assignResultVo.setCode(0);
            assignResultVo.setMsg("");
            return assignResultVo;
        } else if (ReportRecordStatusEnum.UNREPORT.getVal().equals(costReportRecord.getStatus())
                || ReportRecordStatusEnum.EXPIRED.getVal().equals(costReportRecord.getStatus())
                || ReportRecordStatusEnum.REJECT.getVal().equals(costReportRecord.getStatus())
                || ReportRecordStatusEnum.REPORTED.getVal().equals(costReportRecord.getStatus())
                || ReportRecordStatusEnum.APPROVE.getVal().equals(costReportRecord.getStatus())) {
            // 任务未上报 & 已上报 & 驳回 （都已经保存数据）
            // 保存对应上报项内容，删除不存在的上报项内容
            // 口径颗粒度是否改变
            boolean ifChangeReportType = !Objects.equals(costReportTask.getReportType(), costReportRecord.getReportType());

            if (ifChangeReportType) {
                // 口径颗粒度改变，全清
                costReportDetailCostService.remove(Wrappers.<CostReportDetailCost>lambdaQuery()
                        .eq(CostReportDetailCost::getRecordId, costReportRecord.getId()));
                costReportDetailInfoService.remove(Wrappers.<CostReportDetailInfo>lambdaQuery()
                        .eq(CostReportDetailInfo::getRecordId, costReportRecord.getId()));
                removeById(costReportRecord.getId());
                // 重新下发
                CostReportRecord costReportRecord1 = initRecord(costReportTask, null, calculateCircle);
                log.info("assign 1未上报状态继承上月数据 id={} name:{}", taskId, costReportTask.getTaskName());
                // 继承上个月上报任务数据
                inheritPreviousRecordHandle(costReportRecord1.getId());
                updateReportRecordStatus(costReportRecord1.getId(), ReportRecordStatusEnum.UNREPORT.getVal());
            } else {
                // 口径颗粒度未改变，清除部分
                List<CostReportDetailCost> costReportDetailCosts = costReportDetailCostService.list(Wrappers.<CostReportDetailCost>lambdaQuery()
                        .eq(CostReportDetailCost::getRecordId, costReportRecord.getId()));
                for (CostReportDetailCost costReportDetailCost : costReportDetailCosts) {
                    if (!itemSet.contains(costReportDetailCost.getItemId())) {
                        costReportDetailCostService.removeById(costReportDetailCost);
                    }
                }
                // 更新ReportRecord
                initRecord(costReportTask, costReportRecord.getId(), calculateCircle);
                if(ReportRecordStatusEnum.UNREPORT.getVal().equals(costReportRecord.getStatus())){
                    log.info("assign 2未上报状态继承上月数据 id={} name:{}", taskId, costReportTask.getTaskName());
                    // 继承上个月上报任务数据
                    inheritPreviousRecordHandle(costReportRecord.getId());
                }
                updateReportRecordStatus(costReportRecord.getId(), ReportRecordStatusEnum.UNREPORT.getVal());
            }


            log.info("成功重置上报任务 id:{} status:{} name:{}", taskId, costReportRecord.getStatus(), costReportTask.getTaskName());
            AssignResultVo assignResultVo = new AssignResultVo();
            assignResultVo.setCode(0);
            assignResultVo.setMsg("成功");
            return assignResultVo;
        } else {
            log.info("错误状态 id:{} status:{} name:{}", taskId, costReportRecord.getStatus(), costReportTask.getTaskName());
            AssignResultVo assignResultVo = new AssignResultVo();
            assignResultVo.setCode(1);
            assignResultVo.setMsg("当前任务下发找到多笔数据,请联系管理员处理:" + costReportTask.getTaskName() + ":" + costReportRecord.getStatus());
            return assignResultVo;
        }

    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public byte[] generateExcelLogFile(Map<String, String> errorLogMap) {
        // 1. 创建表单
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Error Log" + SecurityUtils.getUser().getId());
        // 2. 填值
        int rowNum = 0;
        for (Map.Entry<String, String> entry : errorLogMap.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowNum);  // Write the key to the first cell
            row.createCell(1).setCellValue(entry.getValue());  // Write the value to the second cell
        }
        // 3. 生成文件
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inheritPreviousRecordHandle(Long recordId) {

        // 获取当前下发任务
        CostReportRecord reportRecord = getById(recordId);

        // 获取任务id
        Long taskId = reportRecord.getTaskId();
        // 获取上一周期下发任务
        // 获取当前任务信息
        CostReportTask costReportTask = costReportTaskService.getById(taskId);
        if (YesNoEnum.NO.getValue().equals(costReportTask.getIfInherit())) return;

        // 当前上报任务类型
        String reportType = reportRecord.getReportType();
        // 上一周期事件
        String calculateCircle = reportRecord.getCalculateCircle();
        String preCalculateCircle = CircleUtil.getPreYearMonth(costReportTask.getFrequencyType(), calculateCircle).toString();

        // 上一周期下发任务获取(只获取一条)
        CostReportRecord preCostReportRecord = this.getOne(Wrappers.<CostReportRecord>lambdaQuery().eq(CostReportRecord::getTaskId, taskId)
                .eq(CostReportRecord::getReportType, reportType)
                .eq(CostReportRecord::getCalculateCircle, preCalculateCircle), false);
        if (preCostReportRecord == null) {
            log.info("当前recoreId={},taskId={}的下发任务未找到需要继承的内容！", recordId, taskId);
            return;
        }

        // 数据全清
        costReportDetailCostService.remove(Wrappers.<CostReportDetailCost>lambdaQuery()
                .eq(CostReportDetailCost::getRecordId, recordId));
        costReportDetailInfoService.remove(Wrappers.<CostReportDetailInfo>lambdaQuery()
                .eq(CostReportDetailInfo::getRecordId, recordId));

        // 获取上一周期的数据行信息
        List<CostReportDetailInfo> costReportDetailInfos = costReportDetailInfoService.list(Wrappers.<CostReportDetailInfo>lambdaQuery()
                .eq(CostReportDetailInfo::getRecordId, preCostReportRecord.getId()));

        // 继承
        if (CollectionUtil.isEmpty(costReportDetailInfos)) return;
        for (CostReportDetailInfo costReportDetailInfo : costReportDetailInfos) {
            costReportDetailInfo.setId(null);
            costReportDetailInfo.setRecordId(reportRecord.getId());
            costReportDetailInfoService.save(costReportDetailInfo);
        }

    }


    @Override
    public String getReportRecordStatus(Long taskId) {

        // 获取当前上报任务
        CostReportTask costReportTask = costReportTaskService.getById(taskId);
        // 上报任务存在校验
        if (Objects.isNull(costReportTask)) {
            throw new BizException("当前id= " + taskId + "上报任务不存在！");
        }
        // 上报任务状态校验
        if (YesNoEnum.YES.getValue().equals(costReportTask.getStatus())) {
            throw new BizException("当前上报任务已被停用！");
        }

        // 判断当前任务是否已经初始化
        CostReportRecord costReportRecord = null;
        List<CostReportRecord> costReportRecords = list(Wrappers.<CostReportRecord>lambdaQuery()
                .eq(CostReportRecord::getTaskId, taskId)
                .eq(CostReportRecord::getCalculateCircle, costReportTask.getCalculateCircle()));
        if (costReportRecords.size() > 1) {
            // 正常：一条任务对应一笔周期内的上报任务
            throw new BizException("当前任务下发找到多笔数据，请联系管理员处理！");
        } else if (costReportRecords.size() == 1) {
            costReportRecord = costReportRecords.get(0);
        }

        // 0初始化 1未上报，2已上报，3过期， 4被驳回， 5通过
        return Objects.isNull(costReportRecord) ? ReportRecordStatusEnum.INIT.getVal() : costReportRecord.getStatus();
    }

    /**
     * 批量下发
     * 每个下发独立执行，互不干扰
     *
     * @param batchAssignDto 批量分配数据
     * @return {@link List }<{@link AssignResultVo }>
     */
    @Override
    public List<AssignResultVo> batchAssign(BatchAssignDto batchAssignDto) {
        List<AssignResultVo> resultVo = new ArrayList<>();
        String calculateCircle = batchAssignDto.getCalculateCircle();
        for (Long taskId : batchAssignDto.getTaskIds()) {
            try {
                AssignResultVo assign = ((ICostReportRecordService) AopContext.currentProxy()).assign(taskId, calculateCircle);
                resultVo.add(assign);
            } catch (IllegalStateException e) {
                log.error("batchAssign proxy异常", e);
                AssignResultVo assignResultVo = new AssignResultVo();
                assignResultVo.setCode(1);
                assignResultVo.setMsg(e.getMessage());
                resultVo.add(assignResultVo);
            } catch (Exception e) {
                log.error("batchAssign proxy异常", e);
                AssignResultVo assignResultVo = new AssignResultVo();
                assignResultVo.setCode(1);
                if (e instanceof BizException) {
                    assignResultVo.setMsg(((BizException) e).getDefaultMessage());
                } else {
                    assignResultVo.setMsg(e.getMessage());
                }
                resultVo.add(assignResultVo);
            }
        }
        return resultVo;
    }

    /**
     * 更新上报任务状态
     *
     * @param recordId 上报任务id
     * @param status   状态
     */
    private void updateReportRecordStatus(Long recordId, String status) {
        CostReportRecord reportRecord = getById(recordId);
        reportRecord.setStatus(status);// 未上报
        updateById(reportRecord);
    }

    // 清理Redis中的错误信息
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeErrorLog() {
        PigxUser user = SecurityUtils.getUser();
        String key = "errorLog: " + user.getId();
        return redisTemplate.delete(key);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approve(CostReportRecord costReportRecord) {
        getById(costReportRecord.getId());
        costReportRecord.setStatus("5");// 已通过
        return updateById(costReportRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reject(CostReportRecord costReportRecord) {
        getById(costReportRecord.getId());
        costReportRecord.setStatus("4");// 已驳回
        return updateById(costReportRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(CostReportRecord costReportRecord) {
        getById(costReportRecord.getId());
        costReportRecord.setStatus("2");// 已上报
        costReportRecord.setReportTime(LocalDateTime.now());
        return updateById(costReportRecord);
    }

    /**
     * 每个月初生成本核算周期内的任务(record)
     */
    @Override
    @XxlJob("initiateJobHandler")
    public void initiateJobHandler() {
        log.info("initiateJobHandler start");

        List<CostReportTask> costReportTasks = costReportTaskService.list(Wrappers.<CostReportTask>lambdaQuery()
                .eq(CostReportTask::getStatus, "0")
                .isNotNull(CostReportTask::getCalculateCircle)
                .ne(CostReportTask::getCalculateCircle, ""));
        log.info("初步待生成上报记录的任务：{}", costReportTasks.size());

        // 生成任务对应的record
        for (CostReportTask costReportTask : costReportTasks) {
            try {
                // 判断这个任务当前月份是否需要生成 上报记录
                if (!canInitRecord(costReportTask)) {
                    continue;
                }
                // 生成 上报记录
                initRecord(costReportTask, null, costReportTask.getCalculateCircle());
                // 更新上报任务的下一次核算周期
                costReportTaskService.updateNextCalculateCircle(costReportTask);
            } catch (Exception e) {
                log.error("当前任务创建上报记录失败：{}", costReportTask.getTaskName(), e);
            }

        }
        log.info("initiateJobHandler end");
    }


    private CostReportRecord initRecord(CostReportTask costReportTask, Long id, String calculateCircle) {
        // 创建上报记录
        CostReportRecord costReportRecord = new CostReportRecord();
        BeanUtils.copyProperties(costReportTask, costReportRecord);
        costReportRecord.setId(id);
        costReportRecord.setCalculateCircle(calculateCircle);
        costReportRecord.setTaskId(costReportTask.getId());
        // 设置上报记录的开始截止时间
        DateTime beginMonthDate = DateUtil.beginOfMonth(new Date());
        int startTime = Integer.parseInt(costReportTask.getStartTime());
        int endTime = Integer.parseInt(costReportTask.getEndTime());
        if (startTime > 0) {
            costReportRecord.setStartTime(LocalDateTimeUtil.of(beginMonthDate.offsetNew(DateField.DAY_OF_YEAR, startTime - 1)));
        } else {
            costReportRecord.setStartTime(LocalDateTimeUtil.of(beginMonthDate.offsetNew(DateField.MONTH, 1).offsetNew(DateField.DAY_OF_YEAR, startTime)));
        }
        if (endTime > 0) {
            costReportRecord.setEndTime(LocalDateTimeUtil.of(beginMonthDate.offsetNew(DateField.DAY_OF_YEAR, endTime - 1)));
        } else {
            costReportRecord.setEndTime(LocalDateTimeUtil.of(beginMonthDate.offsetNew(DateField.MONTH, 1).offsetNew(DateField.DAY_OF_YEAR, endTime)));
        }
        // 上报记录
        costReportRecord.setStatus("0");
        // 保存上报人信息
        JSONArray jsonArray = JSON.parseArray(costReportTask.getUserList());
        List<String> names = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            names.add(jsonObject.getString("name"));
            ids.add(jsonObject.getString("id"));
        }
        costReportRecord.setUserId(CollUtil.join(ids, ","));
        costReportRecord.setUserName(CollUtil.join(names, ","));
        // 保存科室信息
        costReportRecord.setDeptList(costReportTask.getReportDeptList());
        log.info("costReportRecord saveOrUpdate ：{} ：{}", costReportRecord.getId(), costReportRecord.getTaskName());
        saveOrUpdate(costReportRecord);
        return costReportRecord;
    }

    /**
     * 是否可以初始化数据
     *
     * @param e e
     * @return boolean
     */
    private boolean canInitRecord(CostReportTask e) {
        Date basisDate = new Date();
        int currentYear = DateUtil.year(basisDate);
        int currentQuarter = DateUtil.quarter(basisDate);
        int currentMonth = DateUtil.month(basisDate) + 1;
        YearMonth currentYearMonth = YearMonth.of(currentYear, currentMonth);
        if (StrUtil.isBlank(e.getFrequencyType()) || !JSON.isValidObject(e.getFrequencyType())) {
            log.warn("frequencyType is null or invalid: {}", e.getTaskName());
            return false;
        }
        JSONObject jsonObject = JSON.parseObject(e.getFrequencyType());
        String frequencyTypeValue = jsonObject.getString("value");

        // 任务当前周期
        String calculateCircle = e.getCalculateCircle();
        YearMonth taskYearMonth = YearMonth.parse(calculateCircle);
        // if (currentYearMonth.compareTo(taskYearMonth) < 0) {
        //     log.info("首个周期未到，不生成任务：{}", e.getTaskName());
        //     return false;
        // }
        // 判断当前周期是否存在任务
        boolean exists = exists(Wrappers.<CostReportRecord>lambdaQuery()
                .eq(CostReportRecord::getTaskId, e.getId())
                .eq(CostReportRecord::getCalculateCircle, e.getCalculateCircle()));
        if (exists) {
            log.info("任务周期已存在，不生成任务：{}", e.getTaskName());
            return false;
        }
        if (Objects.equals(frequencyTypeValue, "MONTH")) {
            log.info("frequencyType为MONTH，生成任务:{}", e.getTaskName());
            return true;
        }
        if (Objects.equals(frequencyTypeValue, "QUARTER")) {
            if (currentMonth - (currentQuarter * 3 - 3) == 1) {
                // 每季度1月1/4/7/10创建
                log.info("{frequencyType为QUARTER，生成任务:{}", e.getTaskName());
                return true;
            }
        }
        if (Objects.equals(frequencyTypeValue, "YEAR") && currentMonth == 1) {
            // 每年1月创建
            log.info("frequencyType为YEAR，生成任务:{}", e.getTaskName());
            return true;
        }
        log.warn("frequencyType is not valid: {} :{}", e.getTaskName(), e.getFrequencyType());
        return false;
    }

    /**
     * RW任务的基础数据是否未准备好
     *
     * @param reportType      上报想类型
     * @param calculateCircle 周期
     * @param taskName        任务名称
     * @param type            类型
     * @return boolean
     */
    private boolean ifRwTaskDataNotInit(String reportType, String calculateCircle, String taskName, String type) {
        // 如果是rw类型，检查rw数据是否已存在，不存在则不创建个人上报
        if (Objects.equals("5", reportType)) {
            if (StrUtil.isBlank(calculateCircle)) {
                log.warn("{}的核算周期为空，不生成任务", taskName);
                return true;
            }
            Integer rwSize = drgsInfoService.getRwJobHandler(calculateCircle.replace("-", ""), type);
            if (rwSize == 0) {
                log.info("{}的rwSize为0:{}", taskName, rwSize);
                return true;
            }
        }
        return false;
    }

    /**
     * 每天04:00:00激活今日下发的任务(record)
     */
    @Override
    @XxlJob("assignJobHandler")
    @Transactional(rollbackFor = Exception.class)
    public void assignJobHandler() {
        log.info("assignJobHandler start");
        Date now = new Date();
        DateTime endOfDay = DateUtil.endOfDay(now);
        // 获取今天及之前的待激活任务
        List<CostReportRecord> costReportRecords = list(Wrappers.<CostReportRecord>lambdaQuery()
                .eq(CostReportRecord::getStatus, "0")
                .le(CostReportRecord::getStartTime, endOfDay));
        for (CostReportRecord costReportRecord : costReportRecords) {
            if (ifRwTaskDataNotInit(costReportRecord.getReportType(), costReportRecord.getCalculateCircle(), costReportRecord.getTaskName(), costReportRecord.getType())) {
                continue;
            }
            // 未上报
            costReportRecord.setStatus("1");
        }
        updateBatchById(costReportRecords);
        log.info("assignJobHandler end");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<?> editAndSave(List<CostReportDetailInfoDto> costReportDetailInfo) {
        List<CostReportDetailInfoDto> saveOrUpdateList = costReportDetailInfo.stream()
                .filter(x -> Objects.equals(x.getIsRemoved(), "0"))
                .collect(Collectors.toList());
        List<CostReportDetailInfoDto> toRemoveList = costReportDetailInfo.stream()
                .filter(x -> "1".equals(x.getIsRemoved()))
                .collect(Collectors.toList());
        // 删除列表
        for (CostReportDetailInfoDto toRemoveItem : toRemoveList) {
            LambdaQueryWrapper<CostReportDetailCost> qr = new LambdaQueryWrapper<>();
            qr.eq(CostReportDetailCost::getDetailInfoId, toRemoveItem.getId());
            List<CostReportDetailCost> toRemoveCosts = costReportDetailCostService.list(qr);
            costReportDetailCostService.removeByIds(toRemoveCosts);
        }
        costReportDetailInfoService.removeByIds(toRemoveList.stream().map(CostReportDetailInfoDto::getId).collect(Collectors.toList()));
        // 增改列表
        for (CostReportDetailInfoDto info : saveOrUpdateList) {
            CostReportDetailInfo detailInfo = processDetailInfo(info);
            if (ObjectUtil.isNotEmpty(detailInfo.getId())) {
                costReportDetailInfoService.updateById(detailInfo);
            } else {
                costReportDetailInfoService.save(detailInfo);
            }
            // 逐个更新费用
            List<CostReportDetailCost> costReportDetailCosts = info.getCostList();
            for (CostReportDetailCost cost : costReportDetailCosts) {
                cost.setRecordId(info.getRecordId());
                cost.setDetailInfoId(info.getId());
                // todo 有问题,拿不到该列id
                if (ObjectUtil.isNotEmpty(cost.getId())) {
                    costReportDetailCostService.updateById(cost);
                } else {
                    cost.setDetailInfoId(detailInfo.getId());
                    costReportDetailCostService.save(cost);
                }
            }
        }
        return R.ok();
    }

    @Override
    public CostReportDetailRecordDto detailList(CostReportRecord costReportRecord) {
        CostReportDetailRecordDto costReportDetailRecordDto = new CostReportDetailRecordDto();
        List<CostReportDetailInfoDto> costReportDetailInfoDtoList = new ArrayList<>();
        List<CostReportDetailInfo> costReportDetailInfos = costReportDetailInfoService.list(
                new LambdaQueryWrapper<CostReportDetailInfo>().eq(CostReportDetailInfo::getRecordId, costReportRecord.getId())
        );
        for (CostReportDetailInfo costReportDetailInfo : costReportDetailInfos) {
            CostReportDetailInfoDto costReportDetailInfoDto = new CostReportDetailInfoDto();
            BeanUtils.copyProperties(costReportDetailInfo, costReportDetailInfoDto);
            costReportDetailInfoDto.setUserInfo(costReportDetailInfo.getUser());
            costReportDetailInfoDto.setMeasureUnitInfo(costReportDetailInfo.getMeasureUnit());
            costReportDetailInfoDto.setClusterUnitsInfo(costReportDetailInfo.getClusterUnits());
            costReportDetailInfoDtoList.add(costReportDetailInfoDto);
        }

        List<Long> list = Linq.of(costReportDetailInfoDtoList).select(CostReportDetailInfoDto::getId).toList();
        if (!list.isEmpty()) {
            LambdaQueryWrapper<CostReportDetailCost> qr = new LambdaQueryWrapper<>();
            qr.eq(CostReportDetailCost::getRecordId, costReportRecord.getId())
                    .in(CostReportDetailCost::getDetailInfoId, list);
            List<CostReportDetailCost> costReportDetailCosts = costReportDetailCostService.list(qr);
            for (CostReportDetailInfoDto costReportDetailInfoDto : costReportDetailInfoDtoList) {
                List<CostReportDetailCost> lis = Linq.of(costReportDetailCosts).where(r -> Objects.equals(r.getDetailInfoId(), costReportDetailInfoDto.getId())).toList();
                costReportDetailInfoDto.setCostList(lis);
            }
        }
        // 添加附件信息
        List<CostReportRecordFileInfo> fileInfos = costReportRecordFileInfoService.list(
                new LambdaQueryWrapper<CostReportRecordFileInfo>()
                        .eq(CostReportRecordFileInfo::getRecordId, costReportRecord.getId())
        );
        costReportDetailRecordDto.setFileInfos(fileInfos);
        costReportDetailRecordDto.setCostReportDetailInfo(costReportDetailInfoDtoList);
        return costReportDetailRecordDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object getRwData(CostDataCollectionDto input) {
        // 获取code对应的访问数据采集中心的基础入参详情（url、应用key、应用密钥、API名称、加密类型）
        DataCollectionUrl one = dataCollectionUrlService.getOne(new LambdaQueryWrapper<DataCollectionUrl>()
                .eq(DataCollectionUrl::getCode, input.getCode()));
        if (one == null) throw new BizException("当前code对应的数据内容不存在");

        // 入参处理：
        JSONObject parameter = JSON.parseObject(JSON.toJSONString(input.getParameter()));

        GatewayApiDto gatewayApiDto = new GatewayApiDto();
        // url
        gatewayApiDto.setUrl(one.getUrl());
        // 应用密钥
        gatewayApiDto.setAppSecret(one.getAppSecret());
        // 应用key
        gatewayApiDto.setAppKey(one.getAppKey());
        // API名称 todo 不确定是否正确保存
        gatewayApiDto.setHeaderMethodType(one.getAppName());
//            gatewayApiDto.setHeaderMethodType("POST");
        // APPCode todo
        gatewayApiDto.setAppCode(one.getAppName());

        gatewayApiDto.setMethod(one.getAppName());
        // 时间戳
        String timestamp = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        gatewayApiDto.setTimeStamp(timestamp);
        // 签名
        String sign = DigestUtils.md5Hex(one.getAppKey() + one.getAppSecret() + one.getAppName() + timestamp);
        gatewayApiDto.setSign(sign);
        // 加密类型
        if (one.getSignEncryptType().equals(SignEncryptType.SHA256.getCode()))
            gatewayApiDto.setSignEncryptType(SignEncryptType.SHA256);
        else if (one.getSignEncryptType().equals(SignEncryptType.MD5.getCode()))
            gatewayApiDto.setSignEncryptType(SignEncryptType.MD5);
        else if (one.getSignEncryptType().equals(SignEncryptType.SM3.getCode()))
            gatewayApiDto.setSignEncryptType(SignEncryptType.SM3);
        else throw new BizException("当前加密类型不合法!");
        // 封装请求参数
        GatewayApiClient gatewayApiClient = new GatewayApiClient(gatewayApiDto);
        // 获取数据采集中心的数据
        ResponseData responseData = gatewayApiClient.doPost(parameter);
        if (responseData == null) throw new BizException("数据采集中心返回数据为空");
        // todo 添加isEditable字段

        // 出参处理：
        // 字典大类和小类处理
        if ("DICTCODE".equals(input.getCode()) && parameter.get("type") != null) {
            // 这里返回数据不能分页（字典接口一定不能分页），不然会报错！
            List<JSONObject> lists = (List<JSONObject>) responseData.getData();
            if ("1".equals(parameter.get("type"))) {
                // 只要大类信息
                lists = lists.stream().peek(r -> {
                    r.remove("type2_code");
                    r.remove("type2_name");
                }).distinct().collect(Collectors.toList());
            } else if ("2".equals(parameter.get("type"))) {
                // 只要小类信息
                lists.stream().forEach(r -> {
                    r.remove("type1_code");
                    r.remove("type1_name");
                });
            }
            responseData.setData(lists);
        }
        return responseData.getData();
    }

    @Override
    public R downloadTemplate(Long recordId, HttpServletResponse response) {
        String fileName = "fileName";
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        try (OutputStream out = response.getOutputStream();) {
            CostReportRecord record = getById(recordId);
            Long taskId = record.getTaskId();
            CostReportTask task = costReportTaskService.getById(taskId);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(UTF_8);
            ExcelWriter writer = EasyExcelFactory.write(out)//.automaticMergeHead(false)
                    .build();
            // 动态添加表头，适用一些表头动态变化的场景
            WriteSheet sheet1 = new WriteSheet();
            sheet1.setSheetName(record.getTaskName() + record.getCalculateCircle());
            sheet1.setSheetNo(0);
            // 创建一个表格，用于 Sheet 中使用
            WriteTable table = new WriteTable();
            table.setTableNo(1);
            // 查全部
            table.setHead(getHead(task));
            // 空数据
            List<CostReportDetailInfoDto> emptyList = new ArrayList<>();
            writer.write(emptyList, sheet1, table);
            writer.finish();
        } catch (Exception e) {
            log.error("导出模板失败", e);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean uploadFile(CostReportRecordFileInfo fileInfo) {
        return costReportRecordFileInfoService.save(fileInfo);
    }


    /**
     * 表头
     *
     * @param task
     * @return
     */
    private List<List<String>> getHead(CostReportTask task) {
        List<List<String>> total = new ArrayList<>();
        // 详情信息
        List<String> infoList = new ArrayList<>();
        // 根据颗粒度获取通用表头 口径颗粒度 1全院 2核算单元 3核算单元+人员 4人员 5RW
        switch (task.getReportType()) {
            case "7":
                infoList.add("核算单元名称");
                total.add(infoList);
                break;
            case "2":
                infoList.add("核算单元类型");
                total.add(infoList);
                infoList = new ArrayList<>();
                infoList.add("核算单元名称");
                total.add(infoList);
                infoList = new ArrayList<>();
                infoList.add("核算分组");
                total.add(infoList);
                break;
            case "3":
                infoList.add("核算单元类型");
                total.add(infoList);
                infoList = new ArrayList<>();
                infoList.add("核算单元名称");
                total.add(infoList);
                infoList = new ArrayList<>();
                infoList.add("核算分组");
                total.add(infoList);
                infoList = new ArrayList<>();
                infoList.add("人员");
                total.add(infoList);
                infoList = new ArrayList<>();
                infoList.add("工号");
                total.add(infoList);
                break;
            case "4":
                infoList.add("人员");
                total.add(infoList);
                infoList = new ArrayList<>();
                infoList.add("工号");
                total.add(infoList);
                infoList = new ArrayList<>();
                infoList.add("核算单元名称");
                total.add(infoList);
                break;
            default:
                break;
        }
        String itemList = task.getItemList();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNodeArray = objectMapper.readTree(itemList);
            for (int i = 0; i < jsonNodeArray.size(); i++) {
                JsonNode jsonNode = jsonNodeArray.get(i);
                List<String> costList = new ArrayList<>();
                CostReportItem item = new CostReportItem();
                item.setId(Long.valueOf(jsonNode.get("id").asText()));
                item.setName(jsonNode.get("name").asText());
                item.setDataType(jsonNode.get("dataType").asText());
                item.setIsDeptDistinguished(jsonNode.get("isDeptDistinguished").asText());
                if (i == 0 && item.getIsDeptDistinguished().equals("1")) {
                    List<String> costList1 = new ArrayList<>();
                    costList1.add("科别");
                    total.add(costList1);
                }
                costList.add(item.getName());
                total.add(costList);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // 备注字段添加
        infoList = new ArrayList<>();
        infoList.add("备注");
        total.add(infoList);

        return total;
    }


    private CostReportDetailInfo processDetailInfo(CostReportDetailInfoDto costReportDetailInfo) {
        CostReportDetailInfo detailInfo = new CostReportDetailInfo();
        BeanUtils.copyProperties(costReportDetailInfo, detailInfo);
        detailInfo.setMeasureUnit(dataProcessUtil.processList(costReportDetailInfo.getMeasureUnit()));
        detailInfo.setClusterUnits(dataProcessUtil.processList(costReportDetailInfo.getClusterUnits()));
        if (ObjectUtil.isNotEmpty(costReportDetailInfo.getUser())) {
            detailInfo.setUser(dataProcessUtil.processList(costReportDetailInfo.getUser().getUserList()));
        }
        return detailInfo;
    }

    public static void main(String[] args) {
        DateTime offset = DateUtil.parse("20251201").offset(DateField.MONTH, 1).offset(DateField.DAY_OF_YEAR, -1);
        System.out.println(offset.toString("yyyy-MM-dd"));
        // List<DateTime> dateTimes = DateUtil.rangeToList(DateUtil.parse("2024-03-01"), DateUtil.parse("2024-03-01"), DateField.DAY_OF_MONTH);
        // List<DateTime> dateTimes = DateUtil.rangeToList(DateUtil.parse("2024-01-01"), DateUtil.parse("2024-12-31"), DateField.DAY_OF_MONTH);
        // for (DateTime dateTime : dateTimes) {
        //     // Date targetDateTime = nextDate(dateTime, "YEAR", 2, 28);
        //     // Date targetDateTime = nextDate(dateTime, "YEAR", 2, -2);
        //     // Date targetDateTime = nextDate(dateTime, "QUARTER", 2, 28);
        //     // Date targetDateTime = nextDate(dateTime, "QUARTER", 2, -2);
        //     Date targetDateTime = nextDate(dateTime, "MONTH", null, 28);
        //     // Date targetDateTime = nextDate(dateTime, "MONTH", null, -2);
        //     System.out.println(DateUtil.formatDate(dateTime) + "\t" + DateUtil.formatDate(targetDateTime));
        // }
    }

    /**
     * 获取基准日期后符合的周期
     *
     * @param basisDateTime 基准时刻
     * @param cycleType     YEAR：年 QUARTER：季度 MONTH：月
     * @param targetMonth   年模式下第几月
     * @param targetDay     年、季度、月模式下第几天（-x 倒数第x天）
     * @return {@link Date }
     */
    public static Date nextDate(Date basisDateTime, String cycleType, Integer targetMonth, int targetDay) {
        DateTime basisDate = DateUtil.beginOfDay(basisDateTime);
        int basisYear = DateUtil.year(basisDate);
        int basisQuarter = DateUtil.quarter(basisDate);
        int basisMonth = DateUtil.month(basisDate) + 1;
        // 季度的第几个月
        int basisMonthOfQuarter = basisMonth - (basisQuarter * 3 - 3);
        int basisDay = DateUtil.dayOfMonth(basisDate);

        // 正数模式的第几天（入参中负数表示倒数第几天，需要转换为正数比较）
        int targetDayPositiveNumber;
        if (targetDay < 0) {
            targetDayPositiveNumber = DateUtil.getLastDayOfMonth(basisDate) + targetDay + 1;
        } else {
            targetDayPositiveNumber = targetDay;
        }

        if (Objects.equals("YEAR", cycleType)) {
            if (basisMonth < targetMonth || (basisMonth == targetMonth && basisDay < targetDayPositiveNumber)) {
                // 如果今年的月日小于目标月日，则返回今年设定月日
                return getDate(basisYear, targetMonth, targetDay);
            } else {
                // 如果今年的月日大于目标月日，则返回明年设定月日
                return getDate((basisYear + 1), +targetMonth, targetDay);
            }
        } else if (Objects.equals("QUARTER", cycleType)) {
            if (basisMonthOfQuarter < targetMonth || (basisMonthOfQuarter == targetMonth && basisDay < targetDayPositiveNumber)) {
                // 如果今季的月日<目标月日，则返回今季设定月日
                return getDate(basisYear, (basisQuarter * 3 - 3) + targetMonth, targetDay);
            } else {
                // 如果今季的月日>=目标月日，则返回下季设定月日
                if (basisQuarter == 4) {
                    return getDate((basisYear + 1), targetMonth, targetDay);
                } else {
                    return getDate((basisYear), (basisQuarter * 3) + targetMonth, targetDay);
                }
            }
        } else if (Objects.equals("MONTH", cycleType)) {
            // 如果今月的日小于目标日，则返回今月日
            if (basisDay < targetDayPositiveNumber) {
                return getDate(basisYear, basisMonth, targetDay);
            } else {
                if (basisMonth == 12) {
                    return getDate((basisYear + 1), 1, targetDay);
                } else {
                    return getDate(basisYear, (basisMonth + 1), targetDay);
                }
            }
        } else {
            return null;
        }
    }

    /**
     * 获取日期
     *
     * @param year  年
     * @param month 月
     * @param day   天（-x 倒数第x天）
     * @return {@link Date }
     */
    public static Date getDate(int year, int month, int day) {
        if (day > 0) {
            return DateUtil.parse(year + "-" + month + "-" + day);
        } else {
            DateTime parse = DateUtil.parse(year + "-" + month + "-" + 1);
            DateTime offset = DateUtil.offsetMonth(parse, 1);
            return DateUtil.offsetDay(offset, day);
        }
    }

}
