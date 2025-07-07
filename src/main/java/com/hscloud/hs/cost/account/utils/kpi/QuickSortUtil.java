package com.neu.statistics.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuickSortUtil {

    public static void main(String[] args) {
        List<Double> arr= new ArrayList<>();

        System.out.println("原始数组："+ Arrays.toString(arr.toArray()));
        //获取数组arr的长度

        List<Double> newList = quickSort(arr, 0, arr.size() - 1);
        System.out.println("排序后的数组："+ Arrays.toString(newList.toArray()));

    }
    public static List<Double> quickSort(List<Double> arr, int left, int right) {

        //递归结束条件left < right
        if (left < right) {
            // 通过分区函数得到基准元素的索引
            int pivotIndex = partition(arr, left, right);
            //递归对基准元素左边的子数组进行快速排序
            quickSort(arr, left, pivotIndex - 1);
            //递归对基准元素右边的子数组进行快速排序
            quickSort(arr, pivotIndex + 1, right);
        }

        return arr;

    }

    public static int partition(List<Double> arr, int left, int right) {
        // 选择最后一个元素作为基准元素
        double pivot = arr.get(right);
        int i = left;
        //int[] arr = new int[]{5,7,3,3,6,4};

        //循环数组，如果满足条件，则将满足条件的元素交换到arr[i]，同时i++,循环完成之后i之前的元素则全部为小于基准元素的元素
        for (int j = left; j < right; j++) {
            if (arr.get(j) < pivot) {
                if (j != i) {
                    swap(arr, i, j);
                }
                i++;//数交换之后，需要左指针i右移
            }
        }

        // 交换 arr[i] 和基准元素
        swap(arr, i, right);
        //返回基准元素的下标
        return i;
    }

    public static void swap(List<Double> arr, int left, int right) {
        double temp = arr.get(left);
        //交换list中的两个元素
        arr.set(left, arr.get(right));
        arr.set(right, temp);
    }


}