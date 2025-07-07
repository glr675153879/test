package com.hscloud.hs.cost.account.utils.kpi;

import java.util.*;

/**
 *公式检验循环依赖
 */
public class FormulaDependencyChecker {
    private Map<String, List<String>> dependencyGraph;
    private Set<String> visiting;
    private Set<String> visited;

    public FormulaDependencyChecker() {
        this.dependencyGraph = new HashMap<>();
        this.visiting = new HashSet<>();
        this.visited = new HashSet<>();
    }

    // 添加公式的依赖关系
    public void addDependency(String formula, String dependsOn) {
        dependencyGraph.putIfAbsent(formula, new ArrayList<>());
        dependencyGraph.get(formula).add(dependsOn);
    }

    // 检查是否存在循环依赖
    public boolean hasCycle() {
        for (String formula : dependencyGraph.keySet()) {
            if (dfs(formula)) {
                return true; // 如果发现环
            }
        }
        return false; // 没有环
    }

    // 深度优先搜索
    private boolean dfs(String formula) {
        if (visiting.contains(formula)) {
            return true; // 发现环
        }
        if (visited.contains(formula)) {
            return false; // 已处理过
        }

        visiting.add(formula); // 标记为正在访问
        for (String dependentFormula : dependencyGraph.getOrDefault(formula, Collections.emptyList())) {
            if (dfs(dependentFormula)) {
                return true; // 如果邻居中发现环
            }
        }
        visiting.remove(formula); // 访问结束，移除标记
        visited.add(formula); // 标记为已访问
        return false;
    }

    public static void main(String[] args) {
        FormulaDependencyChecker checker = new FormulaDependencyChecker();

        // 添加公式与其依赖关系
        checker.addDependency("FormulaA", "FormulaB");
        checker.addDependency("FormulaA", "FormulaA");
        checker.addDependency("FormulaB", "FormulaC");
        checker.addDependency("FormulaC", "FormulaA"); // 这条依赖引入了循环

        // 检查是否存在循环依赖
        if (checker.hasCycle()) {
            System.out.println("存在循环依赖");
        } else {
            System.out.println("没有循环依赖");
        }
    }
}
