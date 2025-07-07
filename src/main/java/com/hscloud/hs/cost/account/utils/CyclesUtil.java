package com.hscloud.hs.cost.account.utils;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/5/8 9:36]
 */
@Slf4j
public class CyclesUtil {

    public static void main(String[] args) {

        Multimap<String, String> f = ArrayListMultimap.create();
        f.put("0000000000000000001", "0000000000000000001+IF(0000000000000000002>0000000000000000003,0000000000000000004,0000000000000000005)*MAX(0000000000000000006,0000000000000000007)-MIN" +
                "(0000000000000000008,0000000000000000009)");//自身循环依赖
        f.put("0000000000000000002", "0000000000000000003+0000000000000000003");//2-3循环依赖
        f.put("0000000000000000003", "0000000000000000002+0000000000000000002");//2-3循环依赖
        f.put("0000000000000000004", "0000000000000000005+0000000000000000005");//4-5-6循环依赖
        f.put("0000000000000000004", "0000000000000000006+0000000000000000006");//4-6循环依赖
        f.put("0000000000000000004", "0000000000000000007+0000000000000000007");
        f.put("0000000000000000005", "0000000000000000006+0000000000000000006");
        f.put("0000000000000000006", "0000000000000000004+0000000000000000004");
        f.put("0000000000000000007", "0000000000000000008+0000000000000000008");//789循环依赖
        f.put("0000000000000000008", "0000000000000000009+0000000000000000009");//789循环依赖
        f.put("0000000000000000009", "0000000000000000007+0000000000000000007");//789循环依赖
        Graph<String> graph = new Graph<>();
        for (Map.Entry<String, String> entry : f.entries()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Set<String> longs = extractLongNumbers(value);
            for (String aLong : longs) {
                graph.addEdge(key, aLong, String.valueOf(key));
            }
        }
        List<List<Pair<Node<String>, String>>> allCycles = findAllCycles(graph);
        allCycles.forEach(cycle -> {
            List<String> collect = cycle.stream().map(e -> String.format("%s[%s]", e.getKey().getName(), e.getValue())).collect(Collectors.toList());
            log.info("cycle:{}", StrUtil.join("->", collect));
        });
    }

    public static <T> List<List<Pair<Node<T>, String>>> findAllCycles(Graph<T> graph) {
        Set<Node<T>> visited = new HashSet<>();
        List<Pair<Node<T>, String>> path = new ArrayList<>();
        List<List<Pair<Node<T>, String>>> cycles = new ArrayList<>();

        for (Node<T> node : graph.nodes.values()) {
            cycles.addAll(findAllCyclesUtil(node, visited, path));
        }
        //将cycles去重
        Set<String> cycleSet = new HashSet<>();
        List<List<Pair<Node<T>, String>>> distinctCycles = new ArrayList<>();
        for (List<Pair<Node<T>, String>> cycle : cycles) {
            List<String> collect = cycle.stream().map(e -> String.format("%s[%s]", e.getKey().getName(), e.getValue())).sorted(
                    Comparator.comparing(e -> e)
            ).collect(Collectors.toList());
            String join = StrUtil.join("->", collect);
            if (!cycleSet.contains(join)) {
                cycleSet.add(join);
                distinctCycles.add(cycle);
            }
        }
        return distinctCycles;
    }

    private static <T> List<List<Pair<Node<T>, String>>> findAllCyclesUtil(Node<T> node, Set<Node<T>> visited, List<Pair<Node<T>, String>> path) {
        List<List<Pair<Node<T>, String>>> cycles = new ArrayList<>();
        visited.add(node);

        for (Edge<T> edge : node.edges) {
            Pair<Node<T>, String> nodeObjectPair = Pair.of(node, edge.getDesc());
            path.add(nodeObjectPair);
            Node<T> adjacentNode = edge.to;
            if (!visited.contains(adjacentNode)) {
                cycles.addAll(findAllCyclesUtil(adjacentNode, visited, path));
            } else {
                for (int i = 0; i < path.size(); i++) {
                    Pair<Node<T>, String> nodeStringPair = path.get(i);
                    if (nodeStringPair.getKey().equals(adjacentNode)) {
                        cycles.add(new ArrayList<>(path.subList(i, path.size())));
                    }
                }
            }
            path.remove(nodeObjectPair);
        }
        visited.remove(node);

        return cycles;
    }

    /**
     * 提取19位数字
     *
     * @param input 输入
     * @return {@link List}<{@link Long}>
     */
    public static Set<String> extractLongNumbers(String input) {
        Set<String> result = new HashSet<>();
        Pattern pattern = Pattern.compile("\\b\\d{19}\\b");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    @Getter
    @Setter
    public static class Node<T> {
        T name;
        List<Edge<T>> edges;

        public Node(T name) {
            this.name = name;
            this.edges = new ArrayList<>();
        }
    }

    @Getter
    @Setter
    public static class Edge<T> {
        Node<T> from;
        Node<T> to;
        /**
         * 边的描述
         */
        String desc;

        public Edge(Node<T> from, Node<T> to, String desc) {
            this.from = from;
            this.to = to;
            this.desc = desc;
        }
    }

    @Getter
    @Setter
    @Slf4j
    public static class Graph<T> {
        Map<T, Node<T>> nodes;

        public Graph() {
            this.nodes = new HashMap<>();
        }

        private Node<T> addNode(T name) {
            Node<T> node = new Node<T>(name);
            nodes.put(name, node);
            return node;
        }

        public void addEdge(T fromName, T toName, String desc) {
            if (Objects.isNull(fromName) || Objects.isNull(toName)) {
                log.warn("Invalid edge: fromName={}, toName={}", fromName, toName);
                return;
            }
            Node<T> fromNode;
            if (!nodes.containsKey(fromName)) {
                fromNode = addNode(fromName);
            } else {
                fromNode = nodes.get(fromName);
            }
            Node<T> toNode;
            if (!nodes.containsKey(toName)) {
                toNode = addNode(toName);
            } else {
                toNode = nodes.get(toName);
            }

            Edge<T> edge = new Edge<>(fromNode, toNode, desc);
            fromNode.edges.add(edge);
        }
    }

}


