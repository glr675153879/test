package com.hscloud.hs.cost.account.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * EXCEL导出工具类
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Component
public class ExcelUtil {
    private String dataFormat = "m/d/yy h:mm";

    private Map<Workbook, CellStyle> dateStyleMaps = new HashMap<Workbook, CellStyle>();

    // 表头集
    private String[] heanders;

    // bean的名称集
    private String[] beannames;

    private Map<String, Boolean> lineMap;

    private Map<String, Map<Object, Object>> formatMap = new HashMap<>();

    public ExcelUtil() {

    }

    public ExcelUtil(String[] heanders, String[] beannames) {
        this.heanders = heanders;
        this.beannames = beannames;
    }

    public ExcelUtil(String[] heanders, String[] beannames, Map<String, Boolean> lineMap) {
        this.heanders = heanders;
        this.beannames = beannames;
        this.lineMap = lineMap;
    }

    public ExcelUtil(String[] heanders, String[] beannames, Map<String, Boolean> lineMap, Map<String, Map<Object, Object>> formatMap) {
        this.heanders = heanders;
        this.beannames = beannames;
        this.lineMap = lineMap;
        this.formatMap = formatMap;
    }

    /**
     * 普通文件导出
     *
     * @param dateList
     * @param sheetname
     * @param isEntity
     * @param isXLS
     * @return
     * @throws IOException
     */
    public Workbook doExportXLS(List dateList, String sheetname, boolean isEntity, boolean isXLS) throws IOException {
        Workbook wb = null;
        if (isXLS)
            wb = new HSSFWorkbook();
        else
            wb = new XSSFWorkbook();
        if (dateList.size() > 32767) {
            createXLSEntityBulk(wb, dateList);
        } else {
            Sheet sheet = wb.createSheet(sheetname);
            createXLSHeader(wb, sheet);
            if (isEntity) {
                createXLSEntity(wb, sheet, dateList);
            } else {
                createXLS(wb, sheet, dateList);
            }
        }
        // 清除以前缓存的样式
        dateStyleMaps.clear();
        return wb;
    }

    /**
     * 大文件导出
     *
     * @param dateList
     * @param sheetname
     * @param isEntity
     * @param isXLS
     * @param file
     * @return
     * @throws IOException
     * @throws InvalidFormatException
     */
    public Workbook doExportXLS(List dateList, String sheetname, boolean isEntity, boolean isXLS, File file) throws IOException, InvalidFormatException {
        Workbook wb = null;
        if (isXLS)
            wb = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(file)));
        else
            wb = new XSSFWorkbook(file);
        if (dateList.size() > 32767) {
            createXLSEntityBulk(wb, dateList);
        } else {
            Sheet sheet = wb.createSheet(sheetname);
            createXLSHeader(wb, sheet);
            if (isEntity) {
                createXLSEntity(wb, sheet, dateList);
            } else {
                createXLS(wb, sheet, dateList);
            }
        }
        // 清除以前缓存的样式
        dateStyleMaps.clear();
        return wb;
    }

    private void createXLSHeader(Workbook wb, Sheet sheet) {
        for (int i = 0; i < heanders.length; i++) {
            setStringValue(wb, sheet, (short) 0, (short) i, heanders[i]);
        }
    }

    private void createXLS(Workbook wb, Sheet sheet, List<Map<String, Object>> dateList) {
        for (int i = 1; i <= dateList.size(); i++) {
            Map<String, Object> object = dateList.get(i - 1);
            for (int j = 0; j < beannames.length; j++) {
                if (StringUtils.isEmpty(beannames[j])) {
                    this.doSetCell(wb, sheet, (short) i, (short) j, "");
                } else {
                    Object value = object.get(beannames[j]);
                    Map<Object, Object> format = formatMap.get(beannames[j]);
                    if (value != null && format != null) {
                        value = format.get(value);
                    }
                    this.doSetCell(wb, sheet, (short) i, (short) j, value);
                }
                this.doCellLine(wb, sheet, (short) i, (short) j);
            }
        }
    }

    private void doCellLine(Workbook wb, Sheet sheet, int rowNum, int colNum) {
//    	if(lineMap != null && lineMap.get(beannames[colNum])!=null && lineMap.get(beannames[colNum])){
//			Cell cell = this.getMyCell(sheet, rowNum, colNum);
//			CellStyle style = wb.createCellStyle();
//			style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
//			style.setBottomBorderColor(HSSFColor.BLACK.index);
//			style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//			style.setLeftBorderColor(HSSFColor.BLACK.index);
//			style.setBorderRight(HSSFCellStyle.BORDER_THIN);
//			style.setRightBorderColor(HSSFColor.BLACK.index);
//			style.setBorderTop(HSSFCellStyle.BORDER_THIN);
//			style.setTopBorderColor(HSSFColor.BLACK.index);
//			cell.setCellStyle(style);
//        }
    }

    private void createXLSEntity(Workbook wb, Sheet sheet, List<Object> dateList) {
        for (int i = 1; i <= dateList.size(); i++) {
            Object bean = dateList.get(i - 1);
            for (int j = 0; j < beannames.length; j++) {
                BeanWrapper bw = new BeanWrapperImpl(bean);
                if (StringUtils.isEmpty(beannames[j])) {
                    this.doSetCell(wb, sheet, (short) i, (short) j, "");
                } else {
                    Object value = bw.getPropertyValue(beannames[j]);
                    Map<Object, Object> format = formatMap.get(beannames[j]);
                    if (value != null && format != null) {
                        value = format.get(value);
                    }
                    this.doSetCell(wb, sheet, (short) i, (short) j, value);
                }
                this.doCellLine(wb, sheet, (short) i, (short) j);
            }
        }
    }

    /**
     * 导出数据比较多时大于32767条 add by yuhg 091228
     *
     * @param wb
     * @param dateList
     * @return
     */
    private Workbook createXLSEntityBulk(Workbook wb, List<Object> dateList) {
        int sublistIndex = 0; //起始位置
        int perSheetMaxSize = 32767;
        int sheetindex = 1;
        // 如果条数没有到结尾，继续
        while (sublistIndex < dateList.size()) {
            // 从起始位置取到结束
            List<Object> subList = dateList.subList(sublistIndex, dateList.size());
            //产生工作表对象
            Sheet sheet = wb.createSheet("" + sheetindex);
            // 标题
            createXLSHeader(wb, sheet);
            long row = 1;
            for (int i = 1; i <= subList.size(); i++) {
                // 读取一行，位置加1
                sublistIndex++;
                //原来有5条记录，那么就获取5。这个没有问题可是当我删除一条记录后，本应该获取4，他还获取5。所以I-1
                Object bean = subList.get(i - 1);
                // 数据
                for (int j = 0; j < beannames.length; j++) {
                    BeanWrapper bw = new BeanWrapperImpl(bean);
                    Object value = bw.getPropertyValue(beannames[j]);
                    Map<Object, Object> format = formatMap.get(beannames[j]);
                    if (value != null && format != null) {
                        value = format.get(value);
                    }
                    this.doSetCell(wb, sheet, (short) i, (short) j, bw.getPropertyValue(beannames[j]));
                }
                row++;
                if (row > perSheetMaxSize) {
                    // 跳到下一个sheet
                    break;
                }
            }
            // 下一个sheet
            sheetindex++;
        }
        // 清除以前缓存的样式
        dateStyleMaps.clear();
        return wb;
    }

    private void doSetCell(Workbook wb, Sheet sheet, int rowNum, int colNum, Object value) {
        if (value != null) {
            if (value instanceof Number) {
                setDoubleValue(sheet, rowNum, colNum, Double.valueOf(value.toString()));
            } else if (value instanceof String) {
                setStringValue(wb, sheet, rowNum, colNum, value.toString());
            } else if (value instanceof Date) {
                // 样式有数量限制，重用相同的样式
                CellStyle dateStyle = null;
                if (dateStyleMaps.containsKey(wb)) {
                    dateStyle = dateStyleMaps.get(wb);
                } else {
                    dateStyle = wb.createCellStyle();
                    dateStyleMaps.put(wb, dateStyle);
                }
                setDateValue(sheet, dateStyle, rowNum, colNum, (Date) value);
            }
        }
    }

    private void setDoubleValue(Sheet sheet, int rowNum, int colNum, Double value) {
        Cell cell = this.getMyCell(sheet, rowNum, colNum);
        cell.setCellType(CellType.NUMERIC);
        cell.setCellValue(value);
    }

    private void setDateValue(Sheet sheet, CellStyle dateStyle, int rowNum, int colNum, Date value) {
        Cell cell = this.getMyCell(sheet, rowNum, colNum);
        // 设定单元格日期显示格式
        // 指定日期显示格式
        dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat(dataFormat));
        cell.setCellStyle(dateStyle);
        cell.setCellValue(value);
    }

    private void setStringValue(Workbook wb, Sheet sheet, int rowNum, int colNum, String value) {
        Cell cell = this.getMyCell(sheet, rowNum, colNum);
        if (rowNum == 0) {//第一行字体加粗
            CellStyle style = wb.createCellStyle();
            Font font = wb.createFont();
            //font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            style.setFont(font);
            cell.setCellStyle(style);
        }
        // 单元格汉字编码转换
        //cell.setCellType(Cell.CELL_TYPE_STRING);
        // RichTextString str = new RichTextString(value);
        cell.setCellValue(value);
    }

    /**
     * 获得指定Cell
     *
     * @return Cell
     */
    private Cell getMyCell(Sheet sheet, int rowNum, int colNum) {
        Row row = sheet.getRow(rowNum);
        if (null == row) {
            row = sheet.createRow(rowNum);
        }
        Cell cell = row.getCell((short) colNum);
        if (null == cell) {
            cell = row.createCell((short) colNum);
        }
        return cell;
    }

    /**
     * 解决excel类型问题，获得数值
     *
     * @param cell
     * @return
     */
    public static String getValue(Cell cell) {
        String value = "";
        if (null == cell) {
            return value;
        }
        switch (cell.getCellType()) {
            // 数值型
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 如果是date类型则 ，获取该cell的date值
                    Date date = DateUtil.getJavaDate(cell.getNumericCellValue());
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    value = format.format(date);
                } else {// 纯数字
                    BigDecimal big = new BigDecimal(String.valueOf(cell.getNumericCellValue()));
                    value = big.toString();
                    // 解决1234.0 去掉后面的.0
                    if (null != value && !"".equals(value.trim())) {
                        String[] item = value.split("[.]");
                        if (1 < item.length && "0".equals(item[1])) {
                            value = item[0];
                        }
                    }
                }
                break;
            // 字符串类型
            case STRING:
                value = cell.getStringCellValue().toString();
                break;
            // 公式类型
            case FORMULA:
                // 读公式计算值
                value = String.valueOf(cell.getNumericCellValue());
                if (value.equals("NaN")) {// 如果获取的数据值为非法值,则转换为获取字符串
                    value = cell.getStringCellValue().toString();
                }
                break;
            // 布尔类型
            case BOOLEAN:
                value = " " + cell.getBooleanCellValue();
                break;
            // 空值
            case BLANK:
                value = "";
                break;
            // 故障
            case ERROR:
                value = "";
                break;
            default:
                value = cell.getStringCellValue().toString();
        }
        if ("null".endsWith(value.trim())) {
            value = "";
        }
        return value;
    }

    /**
     * 读取excel数据
     *
     * @param file
     * @param titleRows 表头行数
     * @return
     * @throws Exception
     */
    public static String[][] doExcelH(MultipartFile file, int titleRows) throws Exception {
        String[][] upExcel = null;
        InputStream input = file.getInputStream();
        XSSFWorkbook workBook = new XSSFWorkbook(input);
        XSSFSheet sheet = workBook.getSheetAt(0);
        if (sheet != null) {
            // i = 0 是标题栏
            for (int i = titleRows; i <= sheet.getPhysicalNumberOfRows() - 1; i++) {
                XSSFRow row0 = sheet.getRow(0);
                XSSFRow row = sheet.getRow(i);
                if (upExcel == null) {
                    upExcel = new String[sheet.getPhysicalNumberOfRows() - titleRows][row0.getPhysicalNumberOfCells()];
                }
                for (int j = 0; j < row0.getPhysicalNumberOfCells(); j++) {
                    XSSFCell cell = row.getCell(j);
                    String cellStr;
                    try {
                        cellStr = ExcelUtil.getValue(cell);
                    } catch (Exception e) {
                        throw new Exception("第" + (i + 1) + "行第" + (j + 1) + "列数据格式错误，请检查数据格式是否正确！", e);
                    }
                    upExcel[i - titleRows][j] = cellStr;
                }
            }
        }
        workBook.close();

        return upExcel;
    }

    /**
     * 将实体类的字段转化为表头列表
     *
     * @param clazz 实体类Class对象
     * @return 包含字段名的表头列表
     */
    public static List<String> convertEntityToHeaders(Class<?> clazz) {
        List<String> headers = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Schema.class)) {
                Schema schema = field.getAnnotation(Schema.class);
                String description = schema.description();
                headers.add(description);
            }
        }
        return headers;
    }

    //处理单元格数据
    public Object getCellValue(Cell cell) {
        Object result;
        switch (cell.getCellType()) {
            case STRING:
                result = cell.getStringCellValue();
                break;
            case NUMERIC:
                result = cell.getNumericCellValue();
                break;
            case BOOLEAN:
                result = cell.getBooleanCellValue();
                break;
            case FORMULA:
                result = cell.getCellFormula();
                break;
            // 处理其他类型...
            default:
                result = "";
        }
        return result;
    }
}
