package com.hscloud.hs.cost.account.model.dto.kpi;

import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAllocationRuleCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemResultCopy;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class KpiAllocationRuleCaDto extends KpiAllocationRuleCopy {
    private List<String> allocationIndexs_list=new ArrayList<>();

    private List<String> allocationItems_list=new ArrayList<>();

    private List<Long> inMembersEmp_list=new ArrayList<>();

    private List<Long> inMembersDept_list=new ArrayList<>();

    private List<Long> outMembersImp_list=new ArrayList<>();

    private List<Long> outMembersEmp_list=new ArrayList<>();

    private List<Long> outMembersDept_list=new ArrayList<>();

    private List<KpiFieldItemDto> memberCodes_list=new ArrayList<>();

    @Data
    public static class outDept{
        private List<Long> out_dept;
        private List<String> out_dept_group;
        private List<Long> out_dept_except;
    }
}