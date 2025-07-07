package com.hscloud.hs.cost.account.utils;


import com.hscloud.hs.cost.account.constant.enums.SecondDistributionPJJXRegularSelectIndexEnum;
import com.hscloud.hs.cost.account.constant.enums.UnitMapEnum;
import com.hscloud.hs.cost.account.constant.enums.report.OperatorEnum;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemCondDto;
import com.hscloud.hs.cost.account.model.entity.OdsDisKslyxx;
import com.hscloud.hs.cost.account.model.pojo.DwsFinanceWardShare;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Admin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class SqlUtil {

    private final JdbcTemplate jdbcTemplate;
    public static final String[] INTER_KEYWORDS = {"create ", "drop ", "alter ", "truncate ", "update ", "delete ", "insert ",
            //"exec ",
            "execute "};

    /**
     * 接口工具校验sql是否合法，DDl操作
     */
    public static boolean isDdlSql(String sql) {
        //sql统一转小写比较
        String lowerCaseSql = sql.toLowerCase();
        //校验sql中的敏感词汇 禁止增删改 DDL操作
        for (String keyword : INTER_KEYWORDS) {
            boolean contains = lowerCaseSql.contains(keyword);
            if (contains) {
                return true;
            }
        }
        // 涉及到非业务操作检测 dba_ 为oracle系统表开头
        if (lowerCaseSql.contains("dba_")) {
            return true;
        }
        return false;
    }


    public String executeSql(String sql, Map<String, String> params) {
        // Use NamedParameterJdbcTemplate for safe querying
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

        // Replace parameters in the SQL statement
        String sqlWithParams = replaceSqlParameters(sql, params);


        // Create a map for the parameters
        MapSqlParameterSource parameters = new MapSqlParameterSource(params);

        // Execute the SQL query and retrieve a single result
        return namedParameterJdbcTemplate.queryForObject(sqlWithParams, parameters, String.class);
    }

    private String replaceSqlParameters(String sql, Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String paramName = entry.getKey();
            String paramPlaceholder = "#{" + paramName + "}";
            if (sql.contains(paramPlaceholder)) {
                sql = sql.replace(paramPlaceholder, ":" + paramName);
            }

            paramPlaceholder = "${" + paramName + "}";
            if (sql.contains(paramPlaceholder)) {
                sql = sql.replace(paramPlaceholder, ":" + paramName);
            }
        }
        sql = sql.replaceAll("\\s+", " ").trim();
        return sql;
    }


    /**
     * 根据部门id获取部门code
     *
     * @param deptIds 部门id
     * @return 部门编码
     */
    public Map<Long, String> getDeptCodesByDeptIds(List<Long> deptIds) {
        String placeHolders = String.join(", ", Collections.nCopies(deptIds.size(), "?"));
        String sql = String.format(
                "SELECT d.dept_id, d.code FROM sys_dept as d WHERE d.dept_id IN (%s)",
                placeHolders);

        List<Map<Long, String>> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<Long, String> result = new HashMap<>();
            result.put(rs.getLong("dept_id"), rs.getString("code"));
            return result;
        }, deptIds.toArray());

        // Merge the list of maps into a single map
        Map<Long, String> mergedResult = new HashMap<>();
        for (Map<Long, String> result : results) {
            mergedResult.putAll(result);
        }
        return mergedResult;
    }

    /**
     * 根据部门id获取用户id
     *
     * @param deptIds 部门id
     * @return 用户id
     */
    public Map<Long, String> getUserIdsAndNamesByDeptIds(List<Long> deptIds) {
        if (deptIds.isEmpty()) {
            log.error("getUserIdsAndNamesByDeptIds error,deptIds cannot be empty");
            throw new BizException("获取人员信息失败，查不到对应科室单元关联的科室信息");
        }
        String placeHolders = String.join(", ", Collections.nCopies(deptIds.size(), "?"));
        String sql = String.format(
                "SELECT DISTINCT u.user_id, u.name " +
                        "FROM sys_user_dept as ud " +
                        "INNER JOIN sys_user as u ON ud.user_id = u.user_id " +
                        "WHERE ud.dept_id IN (%s)",
                placeHolders);

        List<Map<Long, String>> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<Long, String> result = new HashMap<>();
            result.put(rs.getLong("user_id"), rs.getString("name"));
            return result;
        }, deptIds.toArray());

        // Merge the list of maps into a single map
        Map<Long, String> mergedResult = new HashMap<>();
        for (Map<Long, String> result : results) {
            mergedResult.putAll(result);
        }
        return mergedResult;
    }

    /**
     * 根据用户id获取用户名称
     *
     * @param userId 部门id
     * @return 用户id
     */
    public String getUserNamesByUserIds(Long userId) {
        String sql = "select name from sys_user where user_id = ? ";
        Object[] params = {userId};
        String name = jdbcTemplate.queryForObject(sql, params, String.class);
        return name;
    }

    /**
     * 获取分摊金额
     *
     * @param unitId
     * @param configId
     * @param bedBorrow
     * @return
     */
    public BigDecimal getAssessedAmount(Long unitId, Long configId, String bedBorrow) {
        try {
            String sql = "select share_fee from nfjx_dw.dws_finance_ward_share where account_unit_id = ? and account_item_id = ? and is_borrow_bed = ?";
            Object[] params = {unitId, configId, bedBorrow};
            BigDecimal shareFee = jdbcTemplate.queryForObject(sql, params, BigDecimal.class);
            return shareFee;
        } catch (EmptyResultDataAccessException e) {
            // 查询结果为空，处理逻辑
            // 例如，返回一个默认值或者抛出一个自定义的异常信息
            return BigDecimal.ZERO; // 返回默认值 0
            // 或者抛出一个自定义的异常信息
            //throw new RuntimeException("查询结果为空");
        }
    }

    /**
     * 追溯物品统计信息的落库
     */
    public void insertOdsDisKslyxx(OdsDisKslyxx odsDisKslyxx) {
        String sql = "insert into nfjx_ods.ods_dis_kslyxx(dept_code, dept_name, disposable_num, non_disposable_num, disposable_amount," +
                "non_disposable_amount, num, amount, dt, seq, create_date) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int rows = jdbcTemplate.update(sql, odsDisKslyxx.getDeptCode(), odsDisKslyxx.getDeptName(), odsDisKslyxx.getDisposableNum()
                , odsDisKslyxx.getDisposableNum(), odsDisKslyxx.getDisposableAmount(), odsDisKslyxx.getNonDisposableAmount(),
                odsDisKslyxx.getNum(), odsDisKslyxx.getAmount(), odsDisKslyxx.getDt(), odsDisKslyxx.getSeq(), odsDisKslyxx.getCreateDate());
    }

    /**
     * 获取追溯物品统计信息
     */
    public List<OdsDisKslyxx> getOdsDisKslyxx() {
        String sql = "select * from nfjx_ods.ods_dis_kslyxx";
        List<OdsDisKslyxx> objects = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OdsDisKslyxx.class));
        return objects;
    }

    public List<DwsFinanceWardShare> getWardShareNurse(Long unitId, Long configId, String bedBorrow, String accountStartTime, String accountEndTime) {

        String sql = "select * from nfjx_dw.dws_finance_ward_share where nurse_unit_id = ? and account_item_id = ? and is_borrow_bed = ? and account_period between ? and ?";
        Object[] params = {unitId, configId, bedBorrow, accountStartTime, accountEndTime};
        List<DwsFinanceWardShare> shareFee = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            DwsFinanceWardShare dwsFinanceWardShare = new DwsFinanceWardShare();
            dwsFinanceWardShare.setNurseUnitId(rs.getString("nurse_unit_id"));
            dwsFinanceWardShare.setNurseUnitName(rs.getString("nurse_unit_name"));
            dwsFinanceWardShare.setAccountUnitId(rs.getString("account_unit_id"));
            dwsFinanceWardShare.setAccountUnitName(rs.getString("account_unit_name"));
            dwsFinanceWardShare.setAccountItemId(rs.getLong("account_item_id"));
            dwsFinanceWardShare.setShareFee(rs.getString("share_fee"));
            return dwsFinanceWardShare;
        });
        return shareFee;
    }


    public List<DwsFinanceWardShare> getWardShareDoc(Long unitId, Long configId, String bedBorrow, String accountStartTime, String accountEndTime) {

        String sql = "select * from nfjx_dw.dws_finance_ward_share where account_unit_id = ? and account_item_id = ? and is_borrow_bed = ?and account_period between ? and ?";
        Object[] params = {unitId, configId, bedBorrow, accountStartTime, accountEndTime};
        List<DwsFinanceWardShare> shareFee = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            DwsFinanceWardShare dwsFinanceWardShare = new DwsFinanceWardShare();
            dwsFinanceWardShare.setNurseUnitId(rs.getString("nurse_unit_id"));
            dwsFinanceWardShare.setNurseUnitName(rs.getString("nurse_unit_name"));
            dwsFinanceWardShare.setAccountUnitId(rs.getString("account_unit_id"));
            dwsFinanceWardShare.setAccountUnitName(rs.getString("account_unit_name"));
            dwsFinanceWardShare.setAccountItemId(rs.getLong("account_item_id"));
            dwsFinanceWardShare.setShareFee(rs.getString("share_fee"));
            return dwsFinanceWardShare;
        });
        return shareFee;
    }

    public Map<Long, String> getDeptCodesByUserIds(List<Long> userIds) {

        String placeHolders = String.join(", ", Collections.nCopies(userIds.size(), "?"));
        String sql = String.format(
                "SELECT d.user_id, d.job_number FROM sys_user as d WHERE d.user_id IN (%s)",
                placeHolders);

        List<Map<Long, String>> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<Long, String> result = new HashMap<>();
            result.put(rs.getLong("user_id"), rs.getString("job_number"));
            return result;
        }, userIds.toArray());

        // Merge the list of maps into a single map
        Map<Long, String> mergedResult = new HashMap<>();
        for (Map<Long, String> result : results) {
            mergedResult.putAll(result);
        }
        return mergedResult;
    }


    public List<Long> getUserIdsByDeptIds(List<Long> deptIds) {
        if (deptIds.isEmpty()) {
            log.error("getUserIdsByDeptIds error, deptIds cannot be empty");
            throw new BizException("获取人员信息失败，查不到对应科室单元关联的科室信息");
        }

        String placeHolders = String.join(", ", Collections.nCopies(deptIds.size(), "?"));
        String sql = String.format(
                "SELECT DISTINCT user_id " +
                        "FROM sys_user_dept " +
                        "WHERE dept_id IN (%s)",
                placeHolders);

        List<Long> results = jdbcTemplate.queryForList(sql, Long.class, deptIds.toArray());

        return results;
    }


    //获取数据小组的核算值
    public BigDecimal getIndexCount(String detailDim, List<Long> deptCodes, String indexName) {
        try {
            String deptCodesSql = String.join(",", Collections.nCopies(deptCodes.size(), "?"));
            String sql = "SELECT SUM(account_fee) FROM nfjx_ads.ads_cost_share_temp_new WHERE account_unit_id IN (" + deptCodesSql + ") AND account_period = ? AND class_name = ?";
            Object[] params = deptCodes.toArray(new Object[deptCodes.size()]);
            params = ArrayUtils.add(params, detailDim);
            params = ArrayUtils.add(params, indexName);
            BigDecimal totalSum = jdbcTemplate.queryForObject(sql, params, BigDecimal.class);
            return totalSum != null ? totalSum : BigDecimal.ZERO;
        } catch (EmptyResultDataAccessException e) {
            // 查询结果为空，处理逻辑
            // 例如，返回一个默认值或者抛出一个自定义的异常信息
            return BigDecimal.ZERO; // 返回默认值 0
            // 或者抛出一个自定义的异常信息
            //throw new RuntimeException("查询结果为空");
        }
    }

    public String getColumnNameByItemId(Long itemId) {

        try {
            String sql = "SELECT column_name  FROM nfjx_dim.dim_item_columns_map WHERE item_id = ?";
            Object[] params = {itemId};
            String columnName = jdbcTemplate.queryForObject(sql, params, String.class);
            return columnName != null ? columnName : "";
        } catch (EmptyResultDataAccessException e) {
            // 查询结果为空，处理逻辑
            return ""; // 返回默认值
        }
    }

    public BigDecimal getItemCount(List<Long> unitIds, String indexName, String detailDim, Long itemId) {
        try {
            String deptCodesSql = String.join(",", Collections.nCopies(unitIds.size(), "?"));
            String sql = "SELECT sum(account_fee) FROM nfjx_ads.ads_cost_share_temp_new WHERE account_unit_id IN (" + deptCodesSql + " ) AND  account_period  = ? AND class_name = ?  AND item_id = ?";
            Object[] params = unitIds.toArray(new Object[unitIds.size()]);
            params = ArrayUtils.add(params, detailDim);
            params = ArrayUtils.add(params, indexName);
            params = ArrayUtils.add(params, itemId);
            BigDecimal totalSum = jdbcTemplate.queryForObject(sql, params, BigDecimal.class);
            return totalSum != null ? totalSum : BigDecimal.ZERO;
        } catch (EmptyResultDataAccessException e) {
            // 查询结果为空，处理逻辑
            // 例如，返回一个默认值或者抛出一个自定义的异常信息
            return BigDecimal.ZERO; // 返回默认值 0
            // 或者抛出一个自定义的异常信息
            //throw new RuntimeException("查询结果为空");
        }
    }

    public BigDecimal getItemShareCount(Long unitId, String indexName, String accountType, String detailDim, Long itemId) {
        try {
            String sql = "SELECT sum(account_fee) FROM nfjx_ads.ads_cost_share_temp_new WHERE account_period  = ? AND class_name = ? AND account_type = ? AND item_id = ? AND account_unit_id = ?";
            Object[] params = {detailDim, indexName, accountType, itemId, unitId};
            // 执行查询
            BigDecimal totalSum = jdbcTemplate.queryForObject(sql, params, BigDecimal.class);
            return totalSum != null ? totalSum : BigDecimal.ZERO;
        } catch (EmptyResultDataAccessException e) {
            // 查询结果为空，处理逻辑
            // 例如，返回一个默认值或者抛出一个自定义的异常信息
            return BigDecimal.ZERO; // 返回默认值 0
            // 或者抛出一个自定义的异常信息
            //throw new RuntimeException("查询结果为空");
        }
    }

    public BigDecimal getSharedCost(Long unitId, String detailDim, String indexName, String itemName) {
        try {
            String sql = "SELECT sum(account_fee)  FROM nfjx_ads.ads_cost_share_temp_new WHERE account_period  = ? AND class_name = ? AND item_name = ?AND account_unit_id = ? ";
            Object[] params = {detailDim, indexName, itemName, unitId};

            // 执行查询
            BigDecimal totalSum = jdbcTemplate.queryForObject(sql, params, BigDecimal.class);

            return totalSum != null ? totalSum : BigDecimal.ZERO;
        } catch (EmptyResultDataAccessException e) {
            // 查询结果为空，处理逻辑
            // 例如，返回一个默认值或者抛出一个自定义的异常信息
            return BigDecimal.ZERO; // 返回默认值 0
            // 或者抛出一个自定义的异常信息
            //throw new RuntimeException("查询结果为空");
        }
    }

    public BigDecimal geTotalCount(List<Long> unitIds, String detailDim) {
        try {
            String deptCodesSql = String.join(",", Collections.nCopies(unitIds.size(), "?"));
            String sql = "SELECT sum(account_fee) FROM nfjx_ads.ads_cost_share_temp_new WHERE account_unit_id IN (" + deptCodesSql + ") AND account_period = ?";
            List<Object> params = new ArrayList<>(unitIds);
            params.add(detailDim);
            Object[] paramsArray = params.toArray();
            // 执行查询
            BigDecimal totalSum = jdbcTemplate.queryForObject(sql, BigDecimal.class, paramsArray);

            return totalSum != null ? totalSum : BigDecimal.ZERO;
        } catch (EmptyResultDataAccessException e) {
            // 查询结果为空，处理逻辑
            // 例如，返回一个默认值或者抛出一个自定义的异常信息
            return BigDecimal.ZERO; // 返回默认值 0
            // 或者抛出一个自定义的异常信息
            //throw new RuntimeException("查询结果为空");
        }

    }

    public Map<Long, String> getSecondDistributionTask(String detailDim) {
        try {
            String sql = "SELECT account_unit_id, amount_grant FROM nfjx_ads.ads_performance_grant WHERE account_period = ?";
            List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql, detailDim);

            // 遍历查询结果，将每一行记录转换为一个Map对象，其中Key为account_unit_id，Value为amount_grant
            Map<Long, String> resultMap = new HashMap<>();
            for (Map<String, Object> map : resultList) {
                Long accountUnitId = ((Number) map.get("account_unit_id")).longValue();
                String amountGrant = (String) map.get("amount_grant");
                resultMap.put(accountUnitId, amountGrant);
            }

            return resultMap;
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyMap();
        }
    }

    //获取发放单位总人数
    public BigDecimal getNumberOfUnitPeople(String groupCode, String period) {
        if (UnitMapEnum.DOCKER.getUnitGroup().equals(groupCode)) {
            //医生组
            String sql = "SELECT count(*) " +
                    "FROM nfjx_ads.ads_income_performance_score_doc_head " +
                    "WHERE account_period = ? " +
                    "and";
            jdbcTemplate.queryForList(sql, period);


        } else if (UnitMapEnum.NURSE.getUnitGroup().equals(groupCode)) {
            //护理组


        } else if (UnitMapEnum.MEDICAL_SKILL.getUnitGroup().equals(groupCode)) {
            //医技组


        } else if (UnitMapEnum.ADMINISTRATION.getUnitGroup().equals(groupCode)) {
            //todo 行政组

        }
        return BigDecimal.ZERO;
    }

    //医生组相关平均绩效信息获取
    public BigDecimal getDocPJJXInfo(String value, String period, Long unitId) {
        //默认结果值为0
        BigDecimal res = BigDecimal.ZERO;
        if (SecondDistributionPJJXRegularSelectIndexEnum.KZRRS.getValue().equals(value)) {
            //科主任人数
            String sql = "SELECT count(*) " +
                    "FROM nfjx_ads.ads_income_performance_score_doc_head " +
                    "WHERE account_period = ? " +
                    "and account_unit_id = ? " +
                    "and zw = '" + SecondDistributionPJJXRegularSelectIndexEnum.KZRRS.getTitle() + "'";
            Object[] params = {period, unitId};
            res = jdbcTemplate.queryForObject(sql, params, BigDecimal.class);

        } else if (SecondDistributionPJJXRegularSelectIndexEnum.FKZRRS.getValue().equals(value)) {
            //副科主任人数
            String sql = "SELECT count(*) " +
                    "FROM nfjx_ads.ads_income_performance_score_doc_head " +
                    "WHERE account_period = ? " +
                    "and account_unit_id = ? " +
                    "and zw = '" + SecondDistributionPJJXRegularSelectIndexEnum.FKZRRS.getTitle() + "'";

            Object[] params = {period, unitId};
            res = jdbcTemplate.queryForObject(sql, params, BigDecimal.class);

        } else if (SecondDistributionPJJXRegularSelectIndexEnum.JSGWRS.getValue().equals(value)) {
            //技术顾问人数
            String sql = "SELECT count(*) " +
                    "FROM nfjx_ads.ads_income_performance_score_doc_head " +
                    "WHERE account_period = ? " +
                    "and account_unit_id = ? " +
                    "and zw = '" + SecondDistributionPJJXRegularSelectIndexEnum.JSGWRS.getTitle() + "'";

            Object[] params = {period, unitId};
            res = jdbcTemplate.queryForObject(sql, params, BigDecimal.class);
        }
        return res;
    }

    //医技组相关平均绩效信息获取
    public BigDecimal getYJPJJXInfo(String value, String period, Long unitId) {
        //默认结果值为0
        BigDecimal res = BigDecimal.ZERO;
        if (SecondDistributionPJJXRegularSelectIndexEnum.KZRRS.getValue().equals(value)) {
            //科主任人数
            String sql = "SELECT count(*) " +
                    "FROM nfjx_ads.ads_income_performance_score_doc_head " +
                    "WHERE account_period = ? " +
                    "and account_unit_id = ? " +
                    "and zw = '" + SecondDistributionPJJXRegularSelectIndexEnum.KZRRS.getTitle() + "'";

            Object[] params = {period, unitId};
            res = jdbcTemplate.queryForObject(sql, params, BigDecimal.class);

        } else if (SecondDistributionPJJXRegularSelectIndexEnum.FKZRRS.getValue().equals(value)) {
            //副科主任人数
            String sql = "SELECT count(*) " +
                    "FROM nfjx_ads.ads_income_performance_score_doc_head " +
                    "WHERE account_period = ? " +
                    "and account_unit_id = ? " +
                    "and zw = '" + SecondDistributionPJJXRegularSelectIndexEnum.FKZRRS.getTitle() + "'";

            Object[] params = {period, unitId};
            res = jdbcTemplate.queryForObject(sql, params, BigDecimal.class);

        }
        return res;
    }

    //护理组相关平均绩效信息获取
    public BigDecimal getNursePJJXInfo(String value, String period, Long unitId) {
        //默认结果值为0
        BigDecimal res = BigDecimal.ZERO;
        if (SecondDistributionPJJXRegularSelectIndexEnum.HSZRW.getValue().equals(value)) {
            //护士长人数
            String sql = "SELECT count(*) " +
                    "FROM nfjx_ads.ads_income_performance_score_nur_head " +
                    "WHERE account_period = ? " +
                    "and account_unit_id = ? " +
                    "and zw = '" + SecondDistributionPJJXRegularSelectIndexEnum.HSZRW.getTitle() + "'";

            Object[] params = {period, unitId};
            res = jdbcTemplate.queryForObject(sql, params, BigDecimal.class);

        } else if (SecondDistributionPJJXRegularSelectIndexEnum.FHSZRWW.getValue().equals(value)) {
            //副护士长人数
            String sql = "SELECT count(*) " +
                    "FROM nfjx_ads.ads_income_performance_score_nur_head " +
                    "WHERE account_period = ? " +
                    "and account_unit_id = ? " +
                    "and zw = '" + SecondDistributionPJJXRegularSelectIndexEnum.FHSZRWW.getTitle() + "'";

            Object[] params = {period, unitId};
            res = jdbcTemplate.queryForObject(sql, params, BigDecimal.class);

        }
        return res;
    }

    /**
     * 构建sql where条件
     *
     * @param condList 查询条件list
     * @return sql where条件
     */
    public String buildWhereCondition(List<KpiItemCondDto> condList) {
        StringBuilder condition = new StringBuilder();

        Iterator<KpiItemCondDto> iterator = condList.stream()
                .filter(condDto -> !"group".equals(condDto.getType()) || !CollectionUtils.isEmpty(condDto.getData()))
                .iterator();
        while (iterator.hasNext()) {
            KpiItemCondDto condDto = iterator.next();

            if ("group".equals(condDto.getType())) {
                List<KpiItemCondDto> data = condDto.getData();
                if (!CollectionUtils.isEmpty(data)) {
                    condition.append("(").append(buildWhereCondition(data)).append(")");
                    if (iterator.hasNext()) {
                        condition.append(" ").append(condDto.getConnector()).append(" ");
                    }
                }
                continue;
            }

            condition.append(condDto.getTableName()).append(".").append(condDto.getFieldName()).append(" ");

            String operator = condDto.getOperator();
            condition.append(operator).append(" ");

            if (!OperatorEnum.IS_NULL.getOperator().equals(operator) &&
                    !OperatorEnum.IS_NOT_NULL.getOperator().equals(operator)) {
                condition.append(condDto.getFieldValue());
            }

            if (iterator.hasNext()) {
                condition.append(" ").append(condDto.getConnector()).append(" ");
            }
        }

        return condition.toString();
    }

    /**
     * 处理查询条件格式
     *
     * @param condList 查询条件list
     */
    public void dealCond(List<KpiItemCondDto> condList) {
        List<String> numberTypes = Arrays.asList("int", "bigint", "smallint", "tinyint", "mediumint", "float", "double", "decimal");

        condList.forEach(condDto -> {
            if ("group".equals(condDto.getType())) {
                List<KpiItemCondDto> data = condDto.getData();
                if (!CollectionUtils.isEmpty(data)) {
                    dealCond(data);
                }
            } else {
                String operator = condDto.getOperator();
                String fieldValue = condDto.getFieldValue();
                String fieldType = condDto.getFieldType();

                if (OperatorEnum.IN.getOperator().equals(operator) || OperatorEnum.NOT_IN.getOperator().equals(operator)) {
                    StringBuilder formattedValues = new StringBuilder("(");
                    Arrays.stream(fieldValue.split(",")).forEach(value -> {
                        if (!numberTypes.contains(fieldType)) {
                            if (!value.startsWith("'")) {
                                formattedValues.append("'");
                            }
                            formattedValues.append(value);
                            if (!value.endsWith("'")) {
                                formattedValues.append("'");
                            }
                        } else {
                            formattedValues.append(value);
                        }
                        formattedValues.append(",");
                    });
                    formattedValues.setCharAt(formattedValues.length() - 1, ')');

                    condDto.setFieldValue(formattedValues.toString());
                } else if (OperatorEnum.NE.getOperator().equals(operator)) {
                    condDto.setOperator("<>");  // 统一替换!=为<>
                } else if (!numberTypes.contains(fieldType) && fieldValue != null) {
                    StringBuilder formattedValues = new StringBuilder();
                    if (!fieldValue.startsWith("'")) {
                        formattedValues.append("'");
                    }
                    formattedValues.append(fieldValue);
                    if (!fieldValue.endsWith("'")) {
                        formattedValues.append("'");
                    }

                    condDto.setFieldValue(formattedValues.toString());
                }
            }
        });
    }
}
