package com.hscloud.hs.cost.account.model.dto.dataReport;

import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.pig4cloud.pigx.admin.api.entity.SysDept;
import lombok.Data;

import javax.persistence.Entity;
import java.util.List;

@Data
@Entity
public class SysUserDto {
    private boolean userSelectable;
    private boolean deptSelectable;
    private boolean roleSelectable;
    private List<SysDept> deptList;
    private List<DataReportUserDto> userList;
    private List<OapiV2UserGetResponse.UserRole> roleList;
}
