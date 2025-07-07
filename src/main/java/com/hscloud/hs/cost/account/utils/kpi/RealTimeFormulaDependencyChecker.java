package com.hscloud.hs.cost.account.utils.kpi;

import java.util.*;

public class RealTimeFormulaDependencyChecker {
    private Map<String, List<String>> dependencyGraph;
    private Set<String> visiting;
    private Set<String> visited;

    public RealTimeFormulaDependencyChecker() {
        this.dependencyGraph = new HashMap<>();
        this.visiting = new HashSet<>();
        this.visited = new HashSet<>();
    }

    // 添加公式的依赖关系并实时校验循环依赖
    public boolean addDependency(String formula, String dependsOn) {
        if (formula.equals(dependsOn)){
            System.out.println("指标不能出现在依赖项中");
            return false;
        }
        // 在添加新依赖之前，检查是否会产生循环依赖
        if (willCreateCycle(formula, dependsOn)) {
            System.out.println("无法添加依赖 " + formula + " -> " + dependsOn + "，因为这将导致循环依赖！");
            return false; // 添加失败
        }

        // 如果不会产生循环依赖，添加依赖关系
        dependencyGraph.putIfAbsent(formula, new ArrayList<>());
        dependencyGraph.putIfAbsent(dependsOn, new ArrayList<>());
        dependencyGraph.get(formula).add(dependsOn);
        return true; // 添加成功
    }

    // 检查添加依赖是否会导致循环依赖
    private boolean willCreateCycle(String newFormula, String dependsOn) {
        // 清空访问状态
        visiting.clear();
        visited.clear();

        // 使用深度优先搜索检查是否存在环
        return dfsCheckCycle(dependsOn, newFormula);
    }

    // 深度优先搜索检查环
    private boolean dfsCheckCycle(String dependsOn, String newFormula) {
        if (visiting.contains(dependsOn)) {
            return true; // 发现环
        }
        if (visited.contains(dependsOn)) {
            return false; // 已处理过
        }

        visiting.add(dependsOn); // 标记为正在访问
        for (String dependentFormula : dependencyGraph.getOrDefault(dependsOn, Collections.emptyList())) {
            if (dependentFormula.equals(newFormula) || dfsCheckCycle(dependentFormula, newFormula)) {
                return true; // 如果发现环
            }
        }
        visiting.remove(dependsOn); // 访问结束，移除标记
        visited.add(dependsOn); // 标记为已访问
        return false;
    }

    public static void main(String[] args) {
        RealTimeFormulaDependencyChecker checker = new RealTimeFormulaDependencyChecker();

        // 添加依赖关系并进行实时校验
        checker.addDependency("指标A", "指标B");
        checker.addDependency("指标A", "指标A");
        checker.addDependency("指标A", "指标C");

        // 尝试添加循环依赖
        checker.addDependency("指标C", "指标A"); // 这条依赖会导致循环
    }
}
