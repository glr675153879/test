package com.hscloud.hs.cost.account.service.imputation.monitor;

import com.hscloud.hs.cost.account.constant.ChangeModelConstant;
import com.hscloud.hs.cost.account.constant.enums.imputation.ImputationTableEnum;
import com.hscloud.hs.cost.account.constant.enums.imputation.ImputationType;
import com.hscloud.hs.cost.account.constant.enums.imputation.YesNoEnum;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationChangeLog;
import com.hscloud.hs.cost.account.service.imputation.IImputationService;
import com.hscloud.hs.cost.account.service.monitor.DataChangeMonitor;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.pig4cloud.pigx.common.data.monitor.enums.DataOpEnum;
import com.pig4cloud.pigx.common.data.monitor.pojo.ChangeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author xiechenyu
 * @Description：归集科室单元变更日志
 * @date 2024/4/26 10:42
 */
@Slf4j
@Component
public class ImputationDeptUnitChangeService implements DataChangeMonitor {
    private static final String ADD_CLINICAL_NURSE = "新增科室单元\"%s\",对应人员类型、科室绩效点数、住院绩效点数分别为\"%s、%s、%s\"";
    private static final String UPDATE_CLINICAL_NURSE = "科室单元\"%s\",对应人员类型、科室绩效点数、住院绩效点数分别由\"%s、%s、%s\"变更为\"%s、%s、%s\"";
    private static final String UPDATE_COST_PERFORMANCE_DATA = "科室单元\"%s\",是否纳入总院水电分摊、是否纳入鄞州门诊水电分摊分别由\"%s、%s\"变更为\"%s、%s\"";
    private static final String ADD_OTHER_ACCOUNTING = "新增科室单元\"%s\",对应人员类型为\"%s\"";
    private static final String UPDATE_OTHER_ACCOUNTING = "科室单元\"%s\",对应人员类型由\"%s\"变更为\"%s\"";
    private static final String ADD_ADMIN_MIDDLE = "新增姓名\"%s\",对应人员类型、绩效参考群体、岗位系数分别为\"%s、%s、%s\"";
    private static final String UPDATE_ADMIN_MIDDLE = "姓名\"%s\",对应人员类型、绩效参考群体、岗位系数分别由\"%s、%s、%s\"变更为\"%s、%s、%s\"";
    private static final String ADD_ADMIN_CHIEF = "新增姓名\"%s\",对应人员类型、职务、岗位系数分别为\"%s、%s、%s\"";
    private static final String UPDATE_ADMIN_CHIEF = "姓名\"%s\",对应人员类型、职务、岗位系数分别由\"%s、%s、%s\"变更为\"%s、%s、%s\"";
    private static final String ADD_ADMIN_GENERAL = "新增科室单元\"%s\",对应人员类型、绩效参考群体、岗位系数分别为\"%s、%s、%s\"";
    private static final String UPDATE_ADMIN_GENERAL = "科室单元\"%s\",对应人员类型、绩效参考群体、岗位系数分别由\"%s、%s、%s\"变更为\"%s、%s、%s\"";
    private static final String DELETE_DEPT = "删除科室单元\"%s\"";
    private static final String DELETE_USER = "删除人员\"%s\"";
    private static final String ADD_DEPT = "新增科室单元\"%s\",是否纳入总院水电分摊为\"%s\"、是否纳入鄞州门诊水电分摊为\"%s\"";
    @Autowired
    private IImputationService imputationService;

    @Override
    public String getTableName() {
        return ImputationTableEnum.IM_IMPUTATION_DEPT_UNIT.getCode();
    }

    @Override
    public Map<String, Object> dealDataChange(String handleType, ChangeData record) {
        Map<String, Object> before = record.getBefore();
        Map<String, Object> after = record.getAfter();
        log.info("before: {}, after: {}", before, after);
        ImputationChangeLog imputationChangeLog = new ImputationChangeLog();
        imputationChangeLog.setChangeTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        imputationChangeLog.setChangeModel(ChangeModelConstant.IMPUTATION_PERSON);

        String imputationCode = Objects.isNull(after) ? before.get("imputation_code") + "" : after.get("imputation_code") + "";
        if (DataOpEnum.CREATE.getCode().equals(handleType)) {
            imputationChangeLog.setChangeType(DataOpEnum.CREATE.getDesc());
            imputationChangeLog.setTenantId(Long.valueOf(after.get("tenant_id") + ""));
            imputationChangeLog.setCreateBy(CommonUtils.getNullOrObject(after.get("create_by")));
            imputationChangeLog.setChangeUserName(CommonUtils.getNullOrObject(after.get("create_by")));
            Long imputationId = Long.parseLong(after.get("imputation_id") + "");
            imputationChangeLog.setImputationId(imputationId);
            imputationService.setImputation(imputationChangeLog, imputationId);
            if (Objects.equals(imputationCode, ImputationType.CLINICAL_NURSE_PERFORMANCE_DATA_IMPUTATION.toString())) {
                imputationChangeLog.setChangeItem(after.get("account_unit_name") + "");
                imputationChangeLog.setChangeDesc(String.format(ADD_CLINICAL_NURSE, after.get("account_unit_name"), CommonUtils.getDicLabel(after.get("person_type")), after.get("dept_performance_points"), after.get("hospital_performance_points")));
                imputationChangeLog.insert();

            } else if (Objects.equals(imputationCode, ImputationType.OTHER_ACCOUNTING_UNIT_PERFORMANCE_DATA_IMPUTATION.toString())) {
                imputationChangeLog.setChangeItem(after.get("account_unit_name") + "");
                imputationChangeLog.setChangeDesc(String.format(ADD_OTHER_ACCOUNTING, after.get("account_unit_name"), CommonUtils.getDicLabel(after.get("person_type"))));
                imputationChangeLog.insert();

            } else if (Objects.equals(imputationCode, ImputationType.COST_PERFORMANCE_DATA_IMPUTATION.toString())
                    || Objects.equals(imputationCode, ImputationType.REWARD_PUNISHMENT_DATA_IMPUTATION.toString())) {
                imputationChangeLog.setChangeItem(after.get("account_unit_name") + "");
                YesNoEnum includedWaterAndEle = YesNoEnum.getByCode(after.get("included_water_and_ele").toString());
                YesNoEnum includedYinZhouWaterAndEle = YesNoEnum.getByCode(after.get("included_yin_zhou_water_and_ele").toString());

                if(Objects.nonNull(includedWaterAndEle)&&Objects.nonNull(includedYinZhouWaterAndEle)){
                    imputationChangeLog.setChangeDesc(String.format(ADD_DEPT, after.get("account_unit_name")
                            ,includedWaterAndEle.getDesc(),includedYinZhouWaterAndEle.getDesc()));
                }
                imputationChangeLog.insert();

            } else if (Objects.equals(imputationCode, ImputationType.ADMIN_MIDDLE_HIGH_PERFORMANCE_DATA_IMPUTATION.toString()) ||
                    Objects.equals(imputationCode, ImputationType.ADMIN_NON_STAFF_PERFORMANCE_DATA_IMPUTATION.toString())) {
                imputationChangeLog.setChangeItem(after.get("user_name") + "");
                imputationChangeLog.setChangeDesc(String.format(ADD_ADMIN_MIDDLE, after.get("user_name"), CommonUtils.getDicLabel(after.get("person_type")), CommonUtils.getDicLabel(after.get("reference_group")), after.get("post_rate")));
                imputationChangeLog.insert();

            } else if (Objects.equals(imputationCode, ImputationType.ADMIN_CHIEF_NURSE_PERFORMANCE_DATA_IMPUTATION.toString())) {
                imputationChangeLog.setChangeItem(after.get("user_name") + "");
                imputationChangeLog.setChangeDesc(String.format(ADD_ADMIN_CHIEF, after.get("user_name"), CommonUtils.getDicLabel(after.get("person_type")), CommonUtils.getDicLabel(after.get("post_name")), after.get("post_rate")));
                imputationChangeLog.insert();
            } else if (Objects.equals(imputationCode, ImputationType.ADMIN_GENERAL_PERFORMANCE_DATA_IMPUTATION.toString())) {
                imputationChangeLog.setChangeItem(after.get("account_unit_name") + "");
                imputationChangeLog.setChangeDesc(String.format(ADD_ADMIN_GENERAL, after.get("account_unit_name"), CommonUtils.getDicLabel(after.get("person_type")), CommonUtils.getDicLabel(after.get("reference_group")), after.get("post_rate")));
                imputationChangeLog.insert();
            }
        } else if (DataOpEnum.UPDATE.getCode().equals(handleType)) {
            imputationChangeLog.setChangeType(DataOpEnum.UPDATE.getDesc());
            imputationChangeLog.setTenantId(Long.valueOf(after.get("tenant_id") + ""));
            imputationChangeLog.setCreateBy(CommonUtils.getNullOrObject(after.get("update_by")));
            imputationChangeLog.setChangeUserName(CommonUtils.getNullOrObject(after.get("update_by")));
            Long imputationId = Long.parseLong(after.get("imputation_id") + "");
            imputationChangeLog.setImputationId(imputationId);
            imputationService.setImputation(imputationChangeLog, imputationId);

            if (!Objects.equals(before.get("del_flag"), after.get("del_flag"))) {
                imputationChangeLog.setChangeType(DataOpEnum.DELETE.getDesc());
                if (Objects.equals(imputationCode, ImputationType.ADMIN_NON_STAFF_PERFORMANCE_DATA_IMPUTATION.toString()) ||
                        Objects.equals(imputationCode, ImputationType.ADMIN_MIDDLE_HIGH_PERFORMANCE_DATA_IMPUTATION.toString()) ||
                        Objects.equals(imputationCode, ImputationType.ADMIN_CHIEF_NURSE_PERFORMANCE_DATA_IMPUTATION.toString())) {
                    imputationChangeLog.setChangeItem(before.get("user_name") + "");
                    imputationChangeLog.setChangeDesc(String.format(DELETE_USER, before.get("user_name")));
                    imputationChangeLog.insert();
                } else {
                    imputationChangeLog.setChangeItem(before.get("account_unit_name") + "");
                    imputationChangeLog.setChangeDesc(String.format(DELETE_DEPT, before.get("account_unit_name")));
                    imputationChangeLog.insert();
                }
            } else {
                if (Objects.equals(imputationCode, ImputationType.CLINICAL_NURSE_PERFORMANCE_DATA_IMPUTATION.toString())) {
                    imputationChangeLog.setChangeItem(before.get("account_unit_name") + "");
                    imputationChangeLog.setChangeDesc(String.format(UPDATE_CLINICAL_NURSE, before.get("account_unit_name"),
                            CommonUtils.getDicLabel(before.get("person_type")), before.get("dept_performance_points"), before.get("hospital_performance_points"),
                            CommonUtils.getDicLabel(after.get("person_type")), after.get("dept_performance_points"), after.get("hospital_performance_points")));
                    imputationChangeLog.insert();
                } else if (Objects.equals(imputationCode, ImputationType.OTHER_ACCOUNTING_UNIT_PERFORMANCE_DATA_IMPUTATION.toString())) {
                    imputationChangeLog.setChangeItem(before.get("account_unit_name") + "");
                    imputationChangeLog.setChangeDesc(String.format(UPDATE_OTHER_ACCOUNTING, after.get("account_unit_name"), CommonUtils.getDicLabel(before.get("person_type")), CommonUtils.getDicLabel(after.get("person_type"))));
                    imputationChangeLog.insert();
                } else if (Objects.equals(imputationCode, ImputationType.ADMIN_MIDDLE_HIGH_PERFORMANCE_DATA_IMPUTATION.toString()) ||
                        Objects.equals(imputationCode, ImputationType.ADMIN_NON_STAFF_PERFORMANCE_DATA_IMPUTATION.toString())) {
                    imputationChangeLog.setChangeItem(before.get("user_name") + "");
                    imputationChangeLog.setChangeDesc(String.format(UPDATE_ADMIN_MIDDLE,
                            before.get("user_name"), CommonUtils.getDicLabel(before.get("person_type")), CommonUtils.getDicLabel(before.get("reference_group")), before.get("post_rate"),
                            CommonUtils.getDicLabel(after.get("person_type")), CommonUtils.getDicLabel(after.get("reference_group")), after.get("post_rate")));
                    imputationChangeLog.insert();
                } else if (Objects.equals(imputationCode, ImputationType.ADMIN_CHIEF_NURSE_PERFORMANCE_DATA_IMPUTATION.toString())) {
                    imputationChangeLog.setChangeItem(before.get("user_name") + "");
                    imputationChangeLog.setChangeDesc(String.format(UPDATE_ADMIN_CHIEF,
                            before.get("user_name"), CommonUtils.getDicLabel(before.get("person_type")), CommonUtils.getDicLabel(before.get("post_name")), before.get("post_rate"),
                            CommonUtils.getDicLabel(after.get("person_type")), CommonUtils.getDicLabel(after.get("post_name")), after.get("post_rate")));
                    imputationChangeLog.insert();
                } else if (Objects.equals(imputationCode, ImputationType.ADMIN_GENERAL_PERFORMANCE_DATA_IMPUTATION.toString())) {
                    imputationChangeLog.setChangeItem(before.get("account_unit_name") + "");
                    imputationChangeLog.setChangeDesc(String.format(UPDATE_ADMIN_GENERAL,
                            before.get("account_unit_name"), CommonUtils.getDicLabel(before.get("person_type")), CommonUtils.getDicLabel(before.get("reference_group")), before.get("post_rate"),
                            CommonUtils.getDicLabel(after.get("person_type")), CommonUtils.getDicLabel(after.get("reference_group")), after.get("post_rate")));

                    imputationChangeLog.insert();
                }
                else if(Objects.equals(imputationCode, ImputationType.COST_PERFORMANCE_DATA_IMPUTATION.toString())){
                    imputationChangeLog.setChangeItem(after.get("account_unit_name") + "");
                    //成本支出数据归集
                    YesNoEnum beforeIncludedWaterAndEle = YesNoEnum.getByCode(String.valueOf(before.get("included_water_and_ele")));
                    YesNoEnum beforeIncludedYinZhouWaterAndEle = YesNoEnum.getByCode(String.valueOf(before.get("included_yin_zhou_water_and_ele")));
                    YesNoEnum includedWaterAndEle = YesNoEnum.getByCode(String.valueOf(after.get("included_water_and_ele")));
                    YesNoEnum includedYinZhouWaterAndEle = YesNoEnum.getByCode(String.valueOf(after.get("included_yin_zhou_water_and_ele")));
                    // 检查所有 YesNoEnum 变量是否都不为 null
                    boolean allEnumsNotNull = Stream.of(beforeIncludedWaterAndEle, beforeIncludedYinZhouWaterAndEle, includedWaterAndEle, includedYinZhouWaterAndEle)
                            .allMatch(Objects::nonNull);
                    if (allEnumsNotNull) {
                        String changeDesc = String.format(UPDATE_COST_PERFORMANCE_DATA,
                                after.get("account_unit_name"),
                                beforeIncludedWaterAndEle.getDesc(),
                                beforeIncludedYinZhouWaterAndEle.getDesc(),
                                includedWaterAndEle.getDesc(),
                                includedYinZhouWaterAndEle.getDesc());
                        imputationChangeLog.setChangeDesc(changeDesc);
                    }
                    imputationChangeLog.insert();
                }
            }

        }
        return null;
    }

}
