package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.dto.userAttendance.DateDto;
import com.hscloud.hs.cost.account.model.dto.userAttendance.ExcelExportDTO;
import com.hscloud.hs.cost.account.model.dto.userAttendance.ExcelImportDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiHsUserRule;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendance;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserCalculationRule;
import com.hscloud.hs.cost.account.model.vo.userAttendance.ValidateJobNumberVO;
import com.hscloud.hs.cost.account.model.vo.userAttendance.export.ImportErrVo;
import com.pig4cloud.pigx.common.core.util.R;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 人员考勤表(cost_user_attendance) 服务接口类
 */
public interface IKpiUserAttendanceService extends IService<KpiUserAttendance> {


    IPage<KpiUserAttendance> pageData(Page<KpiUserAttendance> page, QueryWrapper<KpiUserAttendance> wrapper,
                                      String busiType, String period, Map<String, Object> q);

    Long insertData(KpiUserAttendance dto);


    Boolean editWithCustomFields(KpiUserAttendanceEditDto dto);

    void downloadTemplate(String dt, HttpServletResponse response);

    R handleFileUpload(MultipartFile file);


    ImportErrVo uploadFile(String[][] xlsDataArr, ExcelImportDTO dto, Long dt);


    void exportData(ExcelExportDTO period, HttpServletResponse response);


    IPage<KpiUserAttendanceDto> toMatchList(KpiChangeUseSearchDto dto);

    IPage<KpiUserAttendanceDto> manualMatchList(KpiChangeUseSearchDto searchDto);
    //Page<KpiUserAttendanceDto> manualMatchList(PageRequest<KpiUserAttendance> pr);

    //Page<KpiUserAttendanceDto> sysMatchList(PageRequest<KpiUserAttendance> pr);


    IPage<KpiUserAttendanceDto> sysMatchList(KpiChangeUseSearchDto searchDto);

    Boolean updateData(KpiUserAttendanceEditDto dto);

    List<Long> historyList(QueryWrapper<KpiUserAttendance> wrapper);


    //Boolean lockData(String dt, String busiType);

    Boolean lockData(String dt, String busiType, Long tenantId,boolean empRefresh);

    void importData(DateDto dto) throws IOException;

    R validateData(String dt, String busiType);

    List<ValidateJobNumberVO> validateJobNumber(String dt, String busiType);


    void calculateData(String dt, String busiType);

    // 将考勤表的自定义字段解析并放入一张单独的表格
    void pullCustomFields(String dt, String busiType, Long tenantId);


    void AddUser(KpiAccountUserAddDto dto);


    IPage<KpiAccountUserDto> hs_page(KpiAccountUseSearchDto dto);


    void editUser(KpiHsUserEditDto dto, String type);


    void delUser(Long id);

    void copyCustomFields(String period);


    void addDicCoefficient(KpiCoefficientDto2 dto);

    List<KpiCoefficientPageDto> pageCoefficient(KpiCoefficientDto dto);

    void addValueAdjust(KpiValueAdjustDto dtos);

    void copyValueAdjust(KpiValueAdjustCopyDto dtos);

    void delValueAdjust(Long id);

    IPage<KpiValueAdjustPageDto> pageValueAdjust(KpiValueAdjustSearchDto searchDto);


    List<KpiUserCalculationRule> pageCalculationRule(String busiType);


    void addCalculationRule(CalculationRuleInsertDto dto);

    List<AttendanceCheckDTO> attendanceCheck(String busiType, Long period);


    void addHsUserRule(HsUserRuleInsertDto dto);

    List<KpiHsUserRule> pageHsUserRule(HsUserRuleInsertDto dto);

    String getNewHsUser(HsUserRuleInsertDto dto);

    void hsUserRule_del(Long id);

    IPage<DicPageOutDto> protectDic(DicPageDto input);

    List<String> importData2(String categoryCode, List<Map<Integer, String>> list, String overwriteFlag);


    /**
     * 生成最新周期记录
     * @param
     * @return 配置表id
     */
    void editMonthDays(KpiAttendanceMonthDaysListDto dto);


    List<KpiAttendanceMonthDaysDto> monthDays(Long year) ;


    void copyAttendance( DateDto dto);
}
