package com.hscloud.hs.cost.account.utils;

import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Data
public class BudgetUtils {
    public static void main(String[] args) {
        System.out.println("单开工具类，用于全面预算编码解析等调用");
    }

    public Integer analysisBudgetCode(String code) {
        int level = 0;
        //翻译预算科目编码
        if(code == null){
            return 0;
        }
        int length = code.length();
        boolean isEven = length % 2 == 0 && length > 2;
        if(isEven){
            System.out.println("不符合编译规范，返回。。。");
            return 0;
        }else{
            System.out.println("符合编译规范，继续。。。");
            level = (length-1) / 2;
        }
        return level;
    }

    public String convertToKey(int lvl) {
        return "class_" + lvl;
    }

}
