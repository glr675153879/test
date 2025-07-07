package com.hscloud.hs.cost.account.service.kpi;

import com.hscloud.hs.cost.account.constant.enums.kpi.CodePrefixEnum;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiId;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CommCodeService {

    public String commCode(CodePrefixEnum prefix) {
        KpiId entity = new KpiId();
        entity.setCreatedDate(new Date());
        entity.insert();
        String result = convertToLetter(entity.getId());
        return prefix.getPrefix() + result;
    }

    public String convertToLetter(Long number) {
        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            long remainder = (number - 1) % 26;
            sb.insert(0, (char) ('A' + remainder));
            number = (number - 1) / 26;
        }
        return sb.toString().toLowerCase();
    }
}
