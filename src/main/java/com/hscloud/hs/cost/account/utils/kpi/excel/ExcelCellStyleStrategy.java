package com.hscloud.hs.cost.account.utils.kpi.excel;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.style.AbstractCellStyleStrategy;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Excel Converter字典转换器
 */
public class ExcelCellStyleStrategy extends AbstractCellStyleStrategy {

    /**
     * 构造方法，创建对象时传入需要定制的表头信息队列
     */
    public ExcelCellStyleStrategy() {
    }

    @Override
    protected void setHeadCellStyle(Cell cell, Head head, Integer relativeRowIndex) {
        // 处理表头的
    }

    @Override
    protected void setContentCellStyle(Cell cell, Head head, Integer relativeRowIndex) {
        try {
            CellType cellType = cell.getCellType();
            if (cellType.equals(CellType.STRING)) {
                String cellValue = cell.getStringCellValue();
                //判断是否含有中文
                Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
                Matcher m = p.matcher(cellValue);
                int length = 0;
                if (cellValue.contains(".")) {
                    length = cellValue.split("\\.")[0].length();
                } else {
                    length = cellValue.length();
                }
                if (NumberUtils.isCreatable(cellValue) && length <= 10) {
                    //short builtinFormat = HSSFDataFormat.getBuiltinFormat("0.00_ ");
                    //cell.getCellStyle().setDataFormat(builtinFormat);
                    cell.setCellType(CellType.NUMERIC);
                    cell.setCellValue(Double.parseDouble(cellValue));
                } else {
                    cell.setCellType(CellType.STRING);
                }
                //科学计数法
                if (cellValue.contains("E") && !m.find() && !cellValue.contains("X")) {
                    try {
                        BigDecimal bd1 = BigDecimal.valueOf(Double.parseDouble(cellValue));
                        cellValue = bd1.setScale(1, RoundingMode.HALF_UP).toPlainString();
                        cell.setCellValue(cellValue);
                    }catch (Exception e){

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}