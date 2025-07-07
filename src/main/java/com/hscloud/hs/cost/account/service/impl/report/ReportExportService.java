package com.hscloud.hs.cost.account.service.impl.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.tree.Tree;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.model.dto.report.ReportDataDto;
import com.hscloud.hs.cost.account.model.entity.report.Report;
import com.hscloud.hs.cost.account.model.vo.report.ReportTableDataVo;
import com.hscloud.hs.cost.account.service.report.IReportHeadService;
import com.hscloud.hs.cost.account.service.report.IReportService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 报表设计表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportExportService {

    private final IReportService reportService;
    private final IReportHeadService reportHeadService;

    @SneakyThrows
    public void reportDataExport(ReportDataDto dto, HttpServletResponse response) {
        Report byReportCode = reportService.getByReportCode(dto.getReportCode());
        ReportTableDataVo reportTableDataVo = reportService.reportData(dto);
        List<Tree<Long>> tree = reportHeadService.tree(byReportCode.getId(), true);
        //将树转为二维list
        List<List<String>> heads = new ArrayList<>();
        List<String> rowKey = new ArrayList<>();
        tree = Linq.of(tree).where(x->!"task_child_id".equals(x.get("fieldText")) && !"task_child_id".equals(x.get("fieldViewAlias"))).toList();
        for (Tree<Long> longTree : tree) {
            tree2List(longTree, heads, rowKey, new ArrayList<>());
        }

        String fileName = URLEncoder.encode(byReportCode.getName(), "UTF-8");
        response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xlsx");
        response.setContentType("application/octet-stream");
        //将表格数据转为List
        List<List<Object>> dataList = transfer(rowKey, reportTableDataVo.getRows());
        EasyExcel.write(response.getOutputStream()).autoCloseStream(true).sheet("sheet1").head(heads).doWrite(dataList);
    }

    private static List<List<Object>> transfer(List<String> rowKey, List<Map<String, Object>> rows) {
        if (CollUtil.isEmpty(rows)) {
            return new ArrayList<>();
        }
        List<List<Object>> dataList = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            List<Object> list = new ArrayList<>();
            for (String key : rowKey) {
                list.add(row.get(key));
            }
            dataList.add(list);
        }
        return dataList;
    }

    private static void tree2List(Tree<Long> longTree, List<List<String>> heads, List<String> rowKey, List<String> es) {
        es.add(String.valueOf(longTree.get("fieldViewAlias")));
        if (CollUtil.isEmpty(longTree.getChildren())) {
            //已经到叶子结点了，则填入数据
            heads.add(es);
            rowKey.add(String.valueOf(longTree.get("fieldName")));
            return;
        }
        for (Tree<Long> child : longTree.getChildren()) {
            tree2List(child, heads, rowKey, new ArrayList<>(es));
        }
    }

    public static void main(String[] args) {
        List<Map> rows = JSON.parseArray(reportDataStr, Map.class);
        List<Map> tree = JSON.parseArray(treeStr, Map.class);
        //将树转为二维list
        List<List<String>> heads = new ArrayList<>();
        List<String> rowKey = new ArrayList<>();
        for (Map<String, Object> longTree : tree) {
            tree2List2(longTree, heads, rowKey, new ArrayList<>());
        }
        //将表格数据转为List
        List<List<Object>> dataList = transfer2(rowKey, rows);
        EasyExcel.write(FileUtil.getOutputStream("D:\\test.xlsx")).autoCloseStream(true).sheet().head(heads).doWrite(dataList);
    }

    private static void tree2List2(Map<String, Object> longTree, List<List<String>> heads, List<String> rowKey, List<String> es) {
        es.add(String.valueOf(longTree.get("fieldViewAlias")));
        List<Map> map2 = (List<Map>) longTree.get("children");
        if (CollUtil.isEmpty(map2)) {
            //已经到叶子结点了，则填入数据
            heads.add(es);
            rowKey.add(String.valueOf(longTree.get("fieldName")));
            return;
        }
        for (Map<String, Object> child : map2) {
            tree2List2(child, heads, rowKey, new ArrayList<>(es));
        }
    }

    private static List<List<Object>> transfer2(List<String> rowKey, List<Map> rows) {
        if (CollUtil.isEmpty(rows)) {
            return new ArrayList<>();
        }
        List<List<Object>> dataList = new ArrayList<>();
        for (Map row : rows) {
            List<Object> list = new ArrayList<>();
            for (String key : rowKey) {
                list.add(row.get(key));
            }
            dataList.add(list);
        }
        return dataList;
    }

    public static final String reportDataStr = "[{\"account_period\":\"202309\",\"account_unit_id\":\"1703742340506267651\",\"account_unit_name\":\"心病科\",\"out_work_performance\":1205," +
            "\"cases_total_points\":77,\"non_drug_ratio\":0.577,\"disbursement_ratio\":1.022,\"subtotal\":8861,\"high_disease_work_performance_100\":8,\"high_disease_work_performance_total\":800," +
            "\"drgs_total_cost\":0,\"profit_loss\":0,\"profit_loss_ratio\":0,\"disease_operation_total\":0,\"admission_visits\":38,\"admission_visits_mid_high\":13,\"secondary_surgery\":11499," +
            "\"third_level_surgery\":0,\"fourth_surgery_1\":0,\"interventional_therapy\":80500,\"day_surgery\":0,\"treatment\":0,\"fourth_surgery_2\":0,\"surgery_income\":0,\"fourth_surgery_3\":0," +
            "\"painless_workload_order\":2,\"painless_workload_narcotism\":0,\"in_hospital_consultation\":135,\"major_surgical_consultation\":10,\"multi_consultation\":8," +
            "\"night_consultation\":null,\"nursing_chi_med_treat\":48902,\"out_chi_mdi_posts\":9563,\"inpat_chi_mdi_posts\":186,\"winter_dusease_summer_tread\":0,\"bedisde_x_ray\":0," +
            "\"breast_resonance\":0,\"workload_performance_total\":33376,\"b23760a41f\":31743.3,\"01eef6d6b4\":800,\"5bfc4ba838\":6664},{\"account_period\":\"202309\"," +
            "\"account_unit_id\":\"1703742338585276418\",\"account_unit_name\":\"老年病科\",\"out_work_performance\":189,\"cases_total_points\":24,\"non_drug_ratio\":0.822,\"disbursement_ratio\":0.838," +
            "\"subtotal\":3344,\"high_disease_work_performance_100\":0,\"high_disease_work_performance_total\":0,\"drgs_total_cost\":0,\"profit_loss\":0,\"profit_loss_ratio\":0," +
            "\"disease_operation_total\":0,\"admission_visits\":13,\"admission_visits_mid_high\":5,\"secondary_surgery\":0,\"third_level_surgery\":0,\"fourth_surgery_1\":0," +
            "\"interventional_therapy\":0,\"day_surgery\":0,\"treatment\":43474,\"fourth_surgery_2\":0,\"surgery_income\":0,\"fourth_surgery_3\":0,\"painless_workload_order\":1," +
            "\"painless_workload_narcotism\":0,\"in_hospital_consultation\":0,\"major_surgical_consultation\":0,\"multi_consultation\":0,\"night_consultation\":null,\"nursing_chi_med_treat\":81709," +
            "\"out_chi_mdi_posts\":444,\"inpat_chi_mdi_posts\":381,\"winter_dusease_summer_tread\":0,\"bedisde_x_ray\":0,\"breast_resonance\":0,\"workload_performance_total\":15843," +
            "\"b23760a41f\":13111.77,\"01eef6d6b4\":0,\"5bfc4ba838\":2480},{\"account_period\":\"202309\",\"account_unit_id\":\"1703742340376244229\",\"account_unit_name\":\"脾胃病科\"," +
            "\"out_work_performance\":9150,\"cases_total_points\":178,\"non_drug_ratio\":0.672,\"disbursement_ratio\":1.118,\"subtotal\":23941,\"high_disease_work_performance_100\":10," +
            "\"high_disease_work_performance_total\":1000,\"drgs_total_cost\":0,\"profit_loss\":0,\"profit_loss_ratio\":0,\"disease_operation_total\":0,\"admission_visits\":157," +
            "\"admission_visits_mid_high\":119,\"secondary_surgery\":0,\"third_level_surgery\":0,\"fourth_surgery_1\":0,\"interventional_therapy\":1860,\"day_surgery\":0,\"treatment\":0," +
            "\"fourth_surgery_2\":0,\"surgery_income\":0,\"fourth_surgery_3\":0,\"painless_workload_order\":216,\"painless_workload_narcotism\":0,\"in_hospital_consultation\":76," +
            "\"major_surgical_consultation\":0,\"multi_consultation\":2,\"night_consultation\":null,\"nursing_chi_med_treat\":222368,\"out_chi_mdi_posts\":27290,\"inpat_chi_mdi_posts\":651," +
            "\"winter_dusease_summer_tread\":0,\"bedisde_x_ray\":0,\"breast_resonance\":0,\"workload_performance_total\":92380,\"b23760a41f\":79962.6,\"01eef6d6b4\":1000,\"5bfc4ba838\":17942}," +
            "{\"account_period\":\"202309\",\"account_unit_id\":\"1703742337402482690\",\"account_unit_name\":\"肺病科\",\"out_work_performance\":5428,\"cases_total_points\":131,\"non_drug_ratio\":0" +
            ".753,\"disbursement_ratio\":0.993,\"subtotal\":19552,\"high_disease_work_performance_100\":5,\"high_disease_work_performance_total\":500,\"drgs_total_cost\":0,\"profit_loss\":0," +
            "\"profit_loss_ratio\":0,\"disease_operation_total\":0,\"admission_visits\":94,\"admission_visits_mid_high\":41,\"secondary_surgery\":0,\"third_level_surgery\":0,\"fourth_surgery_1\":0," +
            "\"interventional_therapy\":0,\"day_surgery\":0,\"treatment\":0,\"fourth_surgery_2\":0,\"surgery_income\":0,\"fourth_surgery_3\":0,\"painless_workload_order\":4," +
            "\"painless_workload_narcotism\":0,\"in_hospital_consultation\":82,\"major_surgical_consultation\":0,\"multi_consultation\":6,\"night_consultation\":null," +
            "\"nursing_chi_med_treat\":241915,\"out_chi_mdi_posts\":18013,\"inpat_chi_mdi_posts\":1233,\"winter_dusease_summer_tread\":0,\"bedisde_x_ray\":0,\"breast_resonance\":0," +
            "\"workload_performance_total\":64817,\"b23760a41f\":53340.05,\"01eef6d6b4\":500,\"5bfc4ba838\":14693},{\"account_period\":\"202309\",\"account_unit_id\":\"1703742337868050435\"," +
            "\"account_unit_name\":\"感染科\",\"out_work_performance\":2873,\"cases_total_points\":87,\"non_drug_ratio\":0.782,\"disbursement_ratio\":0.803,\"subtotal\":10900," +
            "\"high_disease_work_performance_100\":2,\"high_disease_work_performance_total\":200,\"drgs_total_cost\":0,\"profit_loss\":0,\"profit_loss_ratio\":0,\"disease_operation_total\":0," +
            "\"admission_visits\":43,\"admission_visits_mid_high\":33,\"secondary_surgery\":0,\"third_level_surgery\":0,\"fourth_surgery_1\":0,\"interventional_therapy\":0,\"day_surgery\":0," +
            "\"treatment\":0,\"fourth_surgery_2\":0,\"surgery_income\":0,\"fourth_surgery_3\":0,\"painless_workload_order\":4,\"painless_workload_narcotism\":0,\"in_hospital_consultation\":45," +
            "\"major_surgical_consultation\":0,\"multi_consultation\":1,\"night_consultation\":null,\"nursing_chi_med_treat\":195355,\"out_chi_mdi_posts\":11657,\"inpat_chi_mdi_posts\":1235," +
            "\"winter_dusease_summer_tread\":0,\"bedisde_x_ray\":0,\"breast_resonance\":0,\"workload_performance_total\":41561,\"b23760a41f\":32751.45,\"01eef6d6b4\":200,\"5bfc4ba838\":8195}," +
            "{\"account_period\":\"202309\",\"account_unit_id\":\"1703742339445108742\",\"account_unit_name\":\"内分泌科\",\"out_work_performance\":9362,\"cases_total_points\":250,\"non_drug_ratio\":0" +
            ".785,\"disbursement_ratio\":1.065,\"subtotal\":39210,\"high_disease_work_performance_100\":6,\"high_disease_work_performance_total\":600,\"drgs_total_cost\":0,\"profit_loss\":0," +
            "\"profit_loss_ratio\":0,\"disease_operation_total\":0,\"admission_visits\":135,\"admission_visits_mid_high\":93,\"secondary_surgery\":0,\"third_level_surgery\":0," +
            "\"fourth_surgery_1\":0,\"interventional_therapy\":0,\"day_surgery\":0,\"treatment\":0,\"fourth_surgery_2\":0,\"surgery_income\":0,\"fourth_surgery_3\":0,\"painless_workload_order\":20," +
            "\"painless_workload_narcotism\":0,\"in_hospital_consultation\":111,\"major_surgical_consultation\":0,\"multi_consultation\":2,\"night_consultation\":null," +
            "\"nursing_chi_med_treat\":299484,\"out_chi_mdi_posts\":31910,\"inpat_chi_mdi_posts\":3678,\"winter_dusease_summer_tread\":0,\"bedisde_x_ray\":0,\"breast_resonance\":0," +
            "\"workload_performance_total\":108381,\"b23760a41f\":91175.2,\"01eef6d6b4\":600,\"5bfc4ba838\":29438},{\"account_period\":\"202309\",\"account_unit_id\":\"1703742341026361346\"," +
            "\"account_unit_name\":\"中医经典（内分泌二）科\",\"out_work_performance\":10953,\"cases_total_points\":48,\"non_drug_ratio\":0.825,\"disbursement_ratio\":1.088,\"subtotal\":7935," +
            "\"high_disease_work_performance_100\":5,\"high_disease_work_performance_total\":500,\"drgs_total_cost\":0,\"profit_loss\":0,\"profit_loss_ratio\":0,\"disease_operation_total\":0," +
            "\"admission_visits\":46,\"admission_visits_mid_high\":18,\"secondary_surgery\":0,\"third_level_surgery\":0,\"fourth_surgery_1\":0,\"interventional_therapy\":0,\"day_surgery\":0," +
            "\"treatment\":0,\"fourth_surgery_2\":0,\"surgery_income\":0,\"fourth_surgery_3\":0,\"painless_workload_order\":6,\"painless_workload_narcotism\":0,\"in_hospital_consultation\":1," +
            "\"major_surgical_consultation\":0,\"multi_consultation\":0,\"night_consultation\":null,\"nursing_chi_med_treat\":203670,\"out_chi_mdi_posts\":26049,\"inpat_chi_mdi_posts\":768," +
            "\"winter_dusease_summer_tread\":0,\"bedisde_x_ray\":0,\"breast_resonance\":0,\"workload_performance_total\":50398,\"b23760a41f\":38694.4,\"01eef6d6b4\":500,\"5bfc4ba838\":5940}," +
            "{\"account_period\":\"202309\",\"account_unit_id\":\"1703742339709349892\",\"account_unit_name\":\"肾病科\",\"out_work_performance\":3686,\"cases_total_points\":165,\"non_drug_ratio\":0" +
            ".707,\"disbursement_ratio\":1.073,\"subtotal\":23300,\"high_disease_work_performance_100\":2,\"high_disease_work_performance_total\":200,\"drgs_total_cost\":0,\"profit_loss\":0," +
            "\"profit_loss_ratio\":0,\"disease_operation_total\":0,\"admission_visits\":117,\"admission_visits_mid_high\":76,\"secondary_surgery\":0,\"third_level_surgery\":0," +
            "\"fourth_surgery_1\":0,\"interventional_therapy\":0,\"day_surgery\":0,\"treatment\":0,\"fourth_surgery_2\":0,\"surgery_income\":0,\"fourth_surgery_3\":0,\"painless_workload_order\":14," +
            "\"painless_workload_narcotism\":0,\"in_hospital_consultation\":34,\"major_surgical_consultation\":0,\"multi_consultation\":1,\"night_consultation\":null," +
            "\"nursing_chi_med_treat\":190819,\"out_chi_mdi_posts\":8088,\"inpat_chi_mdi_posts\":770,\"winter_dusease_summer_tread\":0,\"bedisde_x_ray\":0,\"breast_resonance\":0," +
            "\"workload_performance_total\":62482,\"b23760a41f\":54755.75,\"01eef6d6b4\":200,\"5bfc4ba838\":17498},{\"account_period\":\"202309\",\"account_unit_id\":\"1703742337469591556\"," +
            "\"account_unit_name\":\"风湿免疫科\",\"out_work_performance\":1359,\"cases_total_points\":60,\"non_drug_ratio\":0.615,\"disbursement_ratio\":1.094,\"subtotal\":7367," +
            "\"high_disease_work_performance_100\":11,\"high_disease_work_performance_total\":1100,\"drgs_total_cost\":0,\"profit_loss\":0,\"profit_loss_ratio\":0,\"disease_operation_total\":0," +
            "\"admission_visits\":63,\"admission_visits_mid_high\":23,\"secondary_surgery\":0,\"third_level_surgery\":0,\"fourth_surgery_1\":0,\"interventional_therapy\":0,\"day_surgery\":0," +
            "\"treatment\":0,\"fourth_surgery_2\":0,\"surgery_income\":0,\"fourth_surgery_3\":0,\"painless_workload_order\":5,\"painless_workload_narcotism\":0,\"in_hospital_consultation\":27," +
            "\"major_surgical_consultation\":0,\"multi_consultation\":1,\"night_consultation\":null,\"nursing_chi_med_treat\":118474,\"out_chi_mdi_posts\":9192,\"inpat_chi_mdi_posts\":1112," +
            "\"winter_dusease_summer_tread\":0,\"bedisde_x_ray\":0,\"breast_resonance\":0,\"workload_performance_total\":31700,\"b23760a41f\":25750.9,\"01eef6d6b4\":1100,\"5bfc4ba838\":5535}," +
            "{\"account_period\":\"202309\",\"account_unit_id\":\"1703742340573376517\",\"account_unit_name\":\"血液内科\",\"out_work_performance\":401,\"cases_total_points\":57,\"non_drug_ratio\":0" +
            ".561,\"disbursement_ratio\":0.813,\"subtotal\":5230,\"high_disease_work_performance_100\":13,\"high_disease_work_performance_total\":1300,\"drgs_total_cost\":0,\"profit_loss\":0," +
            "\"profit_loss_ratio\":0,\"disease_operation_total\":0,\"admission_visits\":66,\"admission_visits_mid_high\":17,\"secondary_surgery\":0,\"third_level_surgery\":0,\"fourth_surgery_1\":0," +
            "\"interventional_therapy\":0,\"day_surgery\":0,\"treatment\":0,\"fourth_surgery_2\":0,\"surgery_income\":0,\"fourth_surgery_3\":0,\"painless_workload_order\":1," +
            "\"painless_workload_narcotism\":0,\"in_hospital_consultation\":23,\"major_surgical_consultation\":0,\"multi_consultation\":0,\"night_consultation\":null," +
            "\"nursing_chi_med_treat\":83444,\"out_chi_mdi_posts\":4171,\"inpat_chi_mdi_posts\":1119,\"winter_dusease_summer_tread\":0,\"bedisde_x_ray\":0,\"breast_resonance\":0," +
            "\"workload_performance_total\":24256,\"b23760a41f\":20359.3,\"01eef6d6b4\":1300,\"5bfc4ba838\":3900}]";

    public static final String treeStr = "[{\"id\":\"1800406829987106817\",\"parentId\":\"0\",\"weight\":0,\"fieldId\":\"1800331982019858434\",\"sort\":0,\"fieldViewAlias\":\"核算周期\"," +
            "\"fieldText\":\"account_period\",\"fieldType\":\"1\",\"fieldName\":\"account_period\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\"," +
            "\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":\"1\"},{\"id\":\"1800332839683723266\",\"parentId\":\"0\",\"weight\":1,\"fieldId\":\"1800331982053412865\",\"sort\":1," +
            "\"fieldViewAlias\":\"核算单元（医生组)\",\"fieldText\":\"account_unit_name\",\"fieldType\":\"1\",\"fieldName\":\"account_unit_name\",\"reportDbId\":\"1800331981998886914\"," +
            "\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":\"1\"},{\"id\":\"1800332962581024770\",\"parentId\":\"0\",\"weight\":1," +
            "\"fieldId\":\"1800331982065995777\",\"sort\":1,\"fieldViewAlias\":\"门诊工作量绩效\",\"fieldText\":\"门诊工作量绩效\",\"fieldType\":\"1\",\"fieldName\":\"out_work_performance\"," +
            "\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"1\",\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800333030377754625\"," +
            "\"parentId\":\"0\",\"weight\":1,\"fieldId\":\"1800332296915619842\",\"sort\":1,\"fieldViewAlias\":\"病种工作量\",\"fieldText\":null,\"fieldType\":\"3\",\"fieldName\":null," +
            "\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null," +
            "\"children\":[{\"id\":\"1800333108085624833\",\"parentId\":\"1800333030377754625\",\"weight\":0,\"fieldId\":\"1800331982082772993\",\"sort\":null,\"fieldViewAlias\":\"病历总点数\"," +
            "\"fieldText\":\"病历总点数\",\"fieldType\":\"1\",\"fieldName\":\"cases_total_points\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\"," +
            "\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800333137382838273\",\"parentId\":\"1800333030377754625\",\"weight\":0,\"fieldId\":\"1800331982095355905\",\"sort\":null," +
            "\"fieldViewAlias\":\"非药耗比\",\"fieldText\":\"非药耗比\",\"fieldType\":\"1\",\"fieldName\":\"non_drug_ratio\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\"," +
            "\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800333175198683137\",\"parentId\":\"1800333030377754625\",\"weight\":0," +
            "\"fieldId\":\"1800331982112133122\",\"sort\":null,\"fieldViewAlias\":\"拨付比\",\"fieldText\":\"拨付比\",\"fieldType\":\"1\",\"fieldName\":\"disbursement_ratio\"," +
            "\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800346785136349186\"," +
            "\"parentId\":\"1800333030377754625\",\"weight\":0,\"fieldId\":\"1800346723454914561\",\"sort\":null,\"fieldViewAlias\":\"病种小计\",\"fieldText\":null,\"fieldType\":\"2\"," +
            "\"fieldName\":\"5bfc4ba838\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"1\",\"searchFlag\":null}]}," +
            "{\"id\":\"1800333304374857730\",\"parentId\":\"0\",\"weight\":1,\"fieldId\":\"1800332360782286850\",\"sort\":1,\"fieldViewAlias\":\"病种运营绩效\",\"fieldText\":null,\"fieldType\":\"3\"," +
            "\"fieldName\":null,\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null," +
            "\"children\":[{\"id\":\"1800333611578265601\",\"parentId\":\"1800333304374857730\",\"weight\":0,\"fieldId\":\"1800333474441302018\",\"sort\":null,\"fieldViewAlias\":\"高病种工作量绩效\"," +
            "\"fieldText\":null,\"fieldType\":\"3\",\"fieldName\":null,\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\"," +
            "\"searchFlag\":null,\"children\":[{\"id\":\"1800333712870707201\",\"parentId\":\"1800333611578265601\",\"weight\":0,\"fieldId\":\"1800331982145687553\",\"sort\":null," +
            "\"fieldViewAlias\":\"高病种工作量绩效\",\"fieldText\":\"高病种工作量绩效\",\"fieldType\":\"1\",\"fieldName\":\"high_disease_work_performance_100\",\"reportDbId\":\"1800331981998886914\"," +
            "\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800347639901949954\",\"parentId\":\"1800333611578265601\"," +
            "\"weight\":0,\"fieldId\":\"1800347546666766337\",\"sort\":null,\"fieldViewAlias\":\"高病种小计\",\"fieldText\":null,\"fieldType\":\"2\",\"fieldName\":\"01eef6d6b4\"," +
            "\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"1\",\"searchFlag\":null}]},{\"id\":\"1800333648647524353\"," +
            "\"parentId\":\"1800333304374857730\",\"weight\":0,\"fieldId\":\"1800333548902780929\",\"sort\":null,\"fieldViewAlias\":\"本科医保DRGs盈亏考核\",\"fieldText\":null,\"fieldType\":\"3\"," +
            "\"fieldName\":null,\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null," +
            "\"children\":[{\"id\":\"1800333903971586050\",\"parentId\":\"1800333648647524353\",\"weight\":0,\"fieldId\":\"1800331982175047681\",\"sort\":null,\"fieldViewAlias\":\"DRGS总费用\"," +
            "\"fieldText\":\"DRGS总费用\",\"fieldType\":\"1\",\"fieldName\":\"drgs_total_cost\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\"," +
            "\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800333989283729409\",\"parentId\":\"1800333648647524353\",\"weight\":0,\"fieldId\":\"1800331982191824898\",\"sort\":null," +
            "\"fieldViewAlias\":\"医保盈亏\",\"fieldText\":\"医保盈亏\",\"fieldType\":\"1\",\"fieldName\":\"profit_loss\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\"," +
            "\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800334032384397314\",\"parentId\":\"1800333648647524353\",\"weight\":0," +
            "\"fieldId\":\"1800331982212796417\",\"sort\":null,\"fieldViewAlias\":\"盈亏率\",\"fieldText\":\"盈亏率\",\"fieldType\":\"1\",\"fieldName\":\"profit_loss_ratio\"," +
            "\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800334076927905793\"," +
            "\"parentId\":\"1800333648647524353\",\"weight\":0,\"fieldId\":\"1800331982229573633\",\"sort\":null,\"fieldViewAlias\":\"病种运营绩效小计\",\"fieldText\":\"病种运营绩效小计\",\"fieldType\":\"1\"," +
            "\"fieldName\":\"disease_operation_total\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\"," +
            "\"searchFlag\":null}]}]},{\"id\":\"1800334115775549441\",\"parentId\":\"0\",\"weight\":1,\"fieldId\":\"1800331982246350850\",\"sort\":1,\"fieldViewAlias\":\"入院访视\"," +
            "\"fieldText\":\"入院访视\",\"fieldType\":\"1\",\"fieldName\":\"admission_visits\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"1\"," +
            "\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800334153251655682\",\"parentId\":\"0\",\"weight\":1,\"fieldId\":\"1800331982263128065\",\"sort\":1," +
            "\"fieldViewAlias\":\"入院访视除行政/中高层\",\"fieldText\":\"入院访视除行政/中高层\",\"fieldType\":\"1\",\"fieldName\":\"admission_visits_mid_high\",\"reportDbId\":\"1800331981998886914\"," +
            "\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"1\",\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800334210147389441\",\"parentId\":\"0\",\"weight\":1," +
            "\"fieldId\":\"1800332418047119361\",\"sort\":1,\"fieldViewAlias\":\"手术医生工作量\",\"fieldText\":null,\"fieldType\":\"3\",\"fieldName\":null,\"reportDbId\":\"1800331981998886914\"," +
            "\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null,\"children\":[{\"id\":\"1800334254095306754\"," +
            "\"parentId\":\"1800334210147389441\",\"weight\":0,\"fieldId\":\"1800331982275710977\",\"sort\":null,\"fieldViewAlias\":\"二级手术\",\"fieldText\":\"二级手术\",\"fieldType\":\"1\"," +
            "\"fieldName\":\"secondary_surgery\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null}," +
            "{\"id\":\"1800334285271568385\",\"parentId\":\"1800334210147389441\",\"weight\":0,\"fieldId\":\"1800331982292488193\",\"sort\":null,\"fieldViewAlias\":\"三级手术\",\"fieldText\":\"三级手术\"," +
            "\"fieldType\":\"1\",\"fieldName\":\"third_level_surgery\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\"," +
            "\"searchFlag\":null},{\"id\":\"1800334314786885634\",\"parentId\":\"1800334210147389441\",\"weight\":0,\"fieldId\":\"1800331982305071106\",\"sort\":null,\"fieldViewAlias\":\"四级手术\"," +
            "\"fieldText\":\"四级手术\",\"fieldType\":\"1\",\"fieldName\":\"fourth_surgery_1\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\"," +
            "\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800334364183203841\",\"parentId\":\"1800334210147389441\",\"weight\":0,\"fieldId\":\"1800331982321848322\",\"sort\":null," +
            "\"fieldViewAlias\":\"介入治疗\",\"fieldText\":\"介入治疗\",\"fieldType\":\"1\",\"fieldName\":\"interventional_therapy\",\"reportDbId\":\"1800331981998886914\"," +
            "\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800334424396632066\",\"parentId\":\"1800334210147389441\"," +
            "\"weight\":0,\"fieldId\":\"1800331982334431234\",\"sort\":null,\"fieldViewAlias\":\"日间手术\",\"fieldText\":\"日间手术\",\"fieldType\":\"1\",\"fieldName\":\"day_surgery\"," +
            "\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800334464175411201\"," +
            "\"parentId\":\"1800334210147389441\",\"weight\":0,\"fieldId\":\"1800331982351208450\",\"sort\":null,\"fieldViewAlias\":\"治疗\",\"fieldText\":\"治疗\",\"fieldType\":\"1\"," +
            "\"fieldName\":\"treatment\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null}," +
            "{\"id\":\"1800334548761939969\",\"parentId\":\"1800334210147389441\",\"weight\":0,\"fieldId\":\"1800331982367985666\",\"sort\":null,\"fieldViewAlias\":\"四级手术-1\"," +
            "\"fieldText\":\"四级手术-1\",\"fieldType\":\"1\",\"fieldName\":\"fourth_surgery_2\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\"," +
            "\"formulaFlag\":\"0\",\"searchFlag\":null}]},{\"id\":\"1800334641342812162\",\"parentId\":\"0\",\"weight\":1,\"fieldId\":\"1800332448573263874\",\"sort\":1,\"fieldViewAlias\":\"麻醉医生\"," +
            "\"fieldText\":null,\"fieldType\":\"3\",\"fieldName\":null,\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\"," +
            "\"searchFlag\":null,\"children\":[{\"id\":\"1800334726919196674\",\"parentId\":\"1800334641342812162\",\"weight\":0,\"fieldId\":\"1800331982380568578\",\"sort\":null," +
            "\"fieldViewAlias\":\"手术收入\",\"fieldText\":\"手术收入\",\"fieldType\":\"1\",\"fieldName\":\"surgery_income\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\"," +
            "\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800334758355505154\",\"parentId\":\"1800334641342812162\",\"weight\":0," +
            "\"fieldId\":\"1800331982393151489\",\"sort\":null,\"fieldViewAlias\":\"麻醉医生四级手术\",\"fieldText\":\"麻醉医生四级手术\",\"fieldType\":\"1\",\"fieldName\":\"fourth_surgery_3\"," +
            "\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null}]},{\"id\":\"1800334813716123650\"," +
            "\"parentId\":\"0\",\"weight\":1,\"fieldId\":\"1800332516260941826\",\"sort\":1,\"fieldViewAlias\":\"无痛工作量\",\"fieldText\":null,\"fieldType\":\"3\",\"fieldName\":null," +
            "\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null," +
            "\"children\":[{\"id\":\"1800334868548259842\",\"parentId\":\"1800334813716123650\",\"weight\":0,\"fieldId\":\"1800331982405734402\",\"sort\":null,\"fieldViewAlias\":\"无痛工作量-开单\"," +
            "\"fieldText\":\"无痛工作量-开单\",\"fieldType\":\"1\",\"fieldName\":\"painless_workload_order\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\"," +
            "\"sonReportFlag\":\"1\",\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800334920926728193\",\"parentId\":\"1800334813716123650\",\"weight\":0," +
            "\"fieldId\":\"1800331982418317313\",\"sort\":null,\"fieldViewAlias\":\"无痛工作量-麻醉科\",\"fieldText\":\"无痛工作量-麻醉科\",\"fieldType\":\"1\",\"fieldName\":\"painless_workload_narcotism\"," +
            "\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null}]},{\"id\":\"1800334980586508289\"," +
            "\"parentId\":\"0\",\"weight\":1,\"fieldId\":\"1800332547739193346\",\"sort\":1,\"fieldViewAlias\":\"会诊工作量\",\"fieldText\":null,\"fieldType\":\"3\",\"fieldName\":null," +
            "\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null," +
            "\"children\":[{\"id\":\"1800335077525262337\",\"parentId\":\"1800334980586508289\",\"weight\":0,\"fieldId\":\"1800331982435094530\",\"sort\":null,\"fieldViewAlias\":\"院内会诊\"," +
            "\"fieldText\":\"院内会诊\",\"fieldType\":\"1\",\"fieldName\":\"in_hospital_consultation\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\"," +
            "\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800335166394175490\",\"parentId\":\"1800334980586508289\",\"weight\":0," +
            "\"fieldId\":\"1800331982447677442\",\"sort\":null,\"fieldViewAlias\":\"重大手术会诊\",\"fieldText\":\"重大手术会诊\",\"fieldType\":\"1\",\"fieldName\":\"major_surgical_consultation\"," +
            "\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800335231754014721\"," +
            "\"parentId\":\"1800334980586508289\",\"weight\":0,\"fieldId\":\"1800331982464454657\",\"sort\":null,\"fieldViewAlias\":\"多学科会诊\",\"fieldText\":\"多学科会诊\",\"fieldType\":\"1\"," +
            "\"fieldName\":\"multi_consultation\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null}]}," +
            "{\"id\":\"1800335296212078594\",\"parentId\":\"0\",\"weight\":1,\"fieldId\":\"1800332610498564097\",\"sort\":1,\"fieldViewAlias\":\"中草药特色工作量\",\"fieldText\":null,\"fieldType\":\"3\"," +
            "\"fieldName\":null,\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null," +
            "\"children\":[{\"id\":\"1800335358921117697\",\"parentId\":\"1800335296212078594\",\"weight\":0,\"fieldId\":\"1800331982477037569\",\"sort\":null,\"fieldViewAlias\":\"护理特色中医治疗\"," +
            "\"fieldText\":\"护理特色中医治疗\",\"fieldType\":\"1\",\"fieldName\":\"nursing_chi_med_treat\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\"," +
            "\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800335409038856194\",\"parentId\":\"1800335296212078594\",\"weight\":0," +
            "\"fieldId\":\"1800331982489620481\",\"sort\":null,\"fieldViewAlias\":\"门诊中药贴数\",\"fieldText\":\"门诊中药贴数\",\"fieldType\":\"1\",\"fieldName\":\"out_chi_mdi_posts\"," +
            "\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null},{\"id\":\"1800335446825340930\"," +
            "\"parentId\":\"1800335296212078594\",\"weight\":0,\"fieldId\":\"1800331982506397698\",\"sort\":null,\"fieldViewAlias\":\"住院中药贴数\",\"fieldText\":\"住院中药贴数\",\"fieldType\":\"1\"," +
            "\"fieldName\":\"inpat_chi_mdi_posts\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\",\"searchFlag\":null}]}," +
            "{\"id\":\"1800335530598174721\",\"parentId\":\"0\",\"weight\":1,\"fieldId\":\"1800331982535757826\",\"sort\":1,\"fieldViewAlias\":\"床旁B超和拍片\",\"fieldText\":\"床旁B超和拍片\"," +
            "\"fieldType\":\"1\",\"fieldName\":\"bedisde_x_ray\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\",\"formulaFlag\":\"0\"," +
            "\"searchFlag\":null},{\"id\":\"1800353754018582529\",\"parentId\":\"0\",\"weight\":1,\"fieldId\":\"1800353697521307650\",\"sort\":1,\"fieldViewAlias\":\"医生组工作量绩效合计\"," +
            "\"fieldText\":null,\"fieldType\":\"2\",\"fieldName\":\"b23760a41f\",\"reportDbId\":\"1800331981998886914\",\"reportId\":\"1800329224269172737\",\"sonReportFlag\":\"0\"," +
            "\"formulaFlag\":\"1\",\"searchFlag\":null}]";

}
