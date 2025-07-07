package com.hscloud.hs.cost.account.constant;

public interface CacheConstants {



    Long duration = 60 * 60 * 24L;

    /**
     * 指标缓存
     */
    String COST_INDEX = "cost_index";
    //绩效签发 右侧计算
    String COST_SIGN_RIGHT = "cost_sign_right";


    String COST_ITEM_DIMENSION = "cost_item_dimension:%s:%s:%s:%s";

    /**
     * redis缓存，cost_task_execute_result_item表
     */
    String COST_TASK_EXECUTE_RESULT_ITEM = "cost_task_execute_result_item";


    /**
     * 任务结果全量数据缓存
     */
    String COST_TASK_RESULT = "cost_task_result:";


    /**
     * 二次分配任务缓存
     */
    String SECOND_DISTRIBUTION_TASK_UNIT_DETAIL = "second_distribution_task_unit_detail:%s";

    /**
     * 二次分配导入错误日志
     */
    String SEC_IMPORT_ERRLOG = "second::import::";

    /**
     * 同步方法单元方案
     */
    String SYNC_BY_PROGRAMME = "second::syncByProgramme::";

    /**
     * 二次绩效计算
     */
    String SEC_DOCOUNT = "second:docount:";
    /**
     * 二次分配 科室领导
     */
    String SEC_DEPT_LEADER = "second:dept:leader";


    /**
     * 一次分配归集 同步上月
     */
    String IMP_LASTMONTH = "imputation::lastMonth::";
    /**
     * 二次绩效计算
     */
    String SEC_USER_CRUD = "second:user:";

    /**
     * 人员管理导入错误记录前缀
     */
    public static final String IMPORT_ERROR_COST_USER_ATTENDANCE = "import:error:cost:user:attendance:";

    /**
     * rw值导入异常
     */
    String RW_IMPORT_ERROR = "rw:import:error";

    /**
     * 科室面积导入异常
     */
    String DEPT_AREA_IMPORT_ERROR = "dept_area_import:error";

    /**
     * 核算项计算
     */
    String KPI_ITEM_CALCULATE = "kpi:item-calculate:";

    /**
     * 二次分配下发
     */
    String SEC_START_PROJECT = "second:start:project";
    String SEC_START_DETAIL = "second:start:detail";
    String SEC_START_ITEM = "second:start:item";
    String SEC_START_ATTENDANCE = "second:start:attendance";
    String SEC_START_ACCOUNTITEMVALUE = "second:start:accountItemValue";
}
