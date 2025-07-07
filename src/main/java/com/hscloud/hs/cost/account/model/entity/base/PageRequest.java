package com.hscloud.hs.cost.account.model.entity.base;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class PageRequest<T> extends Page<T> {

    /**
     * 查询条件  q["name_LK"]="a"
     */
    private Map<String,Object> q = new HashMap<>();

    /**
     * page里自带的 orders list
     * 排序条件  orders[0] = {"column":"name","asc":true}
     */

    public  PageRequest(){
        //this.wrapper = this.buildQueryWrapper();
    }

    public QueryWrapper<T> getWrapper() {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        try{
            for (String key : q.keySet()){
                Object value = q.get(key);
                if (value == null) continue;
                key = URLDecoder.decode(key, "UTF-8");
                if(value instanceof String){
                    if (value.equals("")) continue;
                    value = URLDecoder.decode(value+"", "UTF-8");
                }
                String[] arr = key.split("_");
                String code = StrUtil.toUnderlineCase(arr[0]);
                String type = arr.length == 1?"EQ":arr[1].toUpperCase();
                switch (type){
                    case "EQ":
                        wrapper.eq(code,value);
                        break;
                    case "LK":
                        wrapper.like(code,value);
                        break;
                    case "NE":
                        wrapper.ne(code,value);
                        break;
                    case "GT":
                        wrapper.gt(code,value);
                        break;
                    case "GE":
                        wrapper.ge(code,value);
                        break;
                    case "LT":
                        wrapper.lt(code,value);
                        break;
                    case "LE":
                        wrapper.le(code,value);
                        break;
                    case "BT":
                        if (value instanceof String){
                            String[] valurArr = ((String) value).split(",");
                            wrapper.between(code, valurArr[0],valurArr[1]);
                        }
                        break;
                    case "NBT":
                        if (value instanceof String){
                            String[] valurArr = ((String) value).split(",");
                            wrapper.notBetween(code, valurArr[0],valurArr[1]);
                        }
                        break;
                    case "NLK":
                        wrapper.notLike(code,value);
                        break;
                    case "LLK":
                        wrapper.likeLeft(code,value);
                        break;
                    case "RLK":
                        wrapper.likeRight(code,value);
                        break;
                    case "NOT":
                        wrapper.notIn(code);
                        break;
                    case "IS":
                        wrapper.isNull(code);
                        break;
                    case "IN":
                        if (value instanceof String){
                            String[] valurArr = ((String) value).split(",");
                            wrapper.in(code, Arrays.asList(valurArr));
                        }
                        break;
                    case "NIN":
                        if (value instanceof String){
                            String[] valurArr = ((String) value).split(",");
                            wrapper.notIn(code, Arrays.asList(valurArr));
                        }
                        break;
                }
            }
            for (OrderItem orderItem : this.orders){
                String column = StrUtil.toUnderlineCase(orderItem.getColumn());
                wrapper.orderBy(true,orderItem.isAsc(),column);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return wrapper;
    }

    public Page<T> getPage() {
        return new Page<>(this.current, this.size, this.total, this.searchCount);
    }
}
