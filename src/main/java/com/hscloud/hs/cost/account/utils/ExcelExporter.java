package com.hscloud.hs.cost.account.utils;

import com.hscloud.hs.cost.account.model.pojo.AccountIndexCalculateInfo;
import com.hscloud.hs.cost.account.model.pojo.AccountItemCalculateInfo;
import com.hscloud.hs.cost.account.model.vo.CostAccountTaskResultDetailVo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ExcelExporter {

    public static void export(OutputStream outStream, CostAccountTaskResultDetailVo data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("任务结果");

            // Create headers
            int rowIdx = 0;
            CellStyle centeredStyle = createCenteredStyle(workbook); // Style for centered text
            List<AccountIndexCalculateInfo> indexInfos = data.getWholeAccountInfo().getAccountIndexCalculateInfoList();

            // Create top-level headers
            Row accountHeaderRow = sheet.createRow(rowIdx++);
            Row subHeaderRow = sheet.createRow(rowIdx++); // Create a separate row for all sub-headers

            int colIdx = 0;
            for (AccountIndexCalculateInfo indexInfo : indexInfos) {
                Cell cell = accountHeaderRow.createCell(colIdx);
                cell.setCellValue(indexInfo.getIndexName());
                cell.setCellStyle(centeredStyle); // Set the style here

                // Write sub-headers
                List<AccountItemCalculateInfo> itemInfos = indexInfo.getAccountItemCalculateInfoList();
                for (int i = 0; i < itemInfos.size(); i++) {
                    Cell subCell = subHeaderRow.createCell(colIdx + i);
                    subCell.setCellValue(itemInfos.get(i).getBizName());
                    subCell.setCellStyle(centeredStyle); // Set the style here
                }

                // Merge cells of the top-level header based on the number of sub-items
                if(itemInfos.size() > 1){
                    sheet.addMergedRegion(new CellRangeAddress(
                            accountHeaderRow.getRowNum(), // first row (0-based)
                            accountHeaderRow.getRowNum(), // last row (0-based)
                            colIdx, // first column (0-based)
                            colIdx + itemInfos.size() - 1  // last column (0-based)
                    ));
                }

                colIdx += itemInfos.size();
            }

            // Adjust the column width to fit the content
            for(int i = 0; i < colIdx; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write the data rows
            Row dataRow = sheet.createRow(rowIdx++);
            colIdx = 0;
            for (AccountIndexCalculateInfo indexInfo : indexInfos) {
                List<AccountItemCalculateInfo> itemInfos = indexInfo.getAccountItemCalculateInfoList();
                for (int i = 0; i < itemInfos.size(); i++) {
                    Cell dataCell = dataRow.createCell(colIdx + i);
                    dataCell.setCellValue(itemInfos.get(i).getBizCalculateValue() + "");
                }
                colIdx += itemInfos.size();
            }

            workbook.write(outStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static CellStyle createCenteredStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}
