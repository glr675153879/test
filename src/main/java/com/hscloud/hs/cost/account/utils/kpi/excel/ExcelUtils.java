package com.hscloud.hs.cost.account.utils.kpi.excel;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.model.dto.kpi.FieldDto;
import com.hscloud.hs.cost.account.model.dto.kpi.excel.ExeclDto;
import com.pig4cloud.pigx.common.core.exception.BizException;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.ss.usermodel.*;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExcelUtils {
    /**
     * 根据sheetName 去匹配
     *
     * @param execlDtos
     * @param sheetname
     * @param tClass
     * @param <T>
     * @return
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static <T> List<T> readExcel2(List<ExeclDto> execlDtos, String sheetname, Class<T> tClass) throws IOException, IllegalAccessException, InstantiationException {
        return readExcelDetail(Linq.of(execlDtos).where(t -> t.getSheetName().contains(sheetname)).firstOrDefault(), tClass);
    }

    public static <T> List<T> readExcel2(List<ExeclDto> execlDtos, Integer seq, Class<T> tClass) throws IOException, IllegalAccessException, InstantiationException {
        return readExcelDetail(Linq.of(execlDtos).where(t -> Objects.equals(t.getSheetSno(), seq)).firstOrDefault(), tClass);
    }

    /**
     * 特殊导入 数据第三行 所有sheet页
     *
     * @param execlDtos
     * @param tClass
     * @param <T>
     * @return
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static <T> List<T> readExcelDetail(ExeclDto execlDtos, Class<T> tClass) throws IOException, IllegalAccessException, InstantiationException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
        //0字段名 1 字段中文 >2数据
        //List<String> strings = execlDtos.getDatas().get(0).stream().map(String::toLowerCase).map(String::trim).collect(Collectors.toList());
        List<String> strings = execlDtos.getDatas().get(0).stream().map(String::trim).collect(Collectors.toList());
        List<T> listBean = new ArrayList<T>();
        Field[] fields = tClass.getDeclaredFields();
        List<FieldDto> lif = new ArrayList<>();
        for (Field field : fields) {
            Schema annotation = field.getAnnotation(Schema.class);
            //String name = field.getName().toLowerCase();
            //注释
            String key = annotation.description();
//            //特判一些字段不参与判断
//            if (tClass.equals(ExcelMzfBgkDto.class)) {
//                if (StringUtils.equalsAny(key, "临床症状", "病情转归", "病情转归其他")) {
//                    continue;
//                }
//            }
            field.setAccessible(true);
            FieldDto in = new FieldDto();
            in.setSeq(strings.indexOf(key.trim()));
            //in.setSeq(strings.indexOf(name.trim()));
            in.setField(field);
            lif.add(in);
        }
        List<FieldDto> fieldDtos = Linq.of(lif).where(t -> t.getSeq() > -1).orderBy(FieldDto::getSeq).toList();
        if (Linq.of(fieldDtos).count() != strings.size()) {
            throw new BizException("模板不正确");
        }
        T uBean = null;
        for (int i = 1; i < execlDtos.getDatas().size(); i++) {
            uBean = tClass.newInstance();
            List<String> listStr = execlDtos.getDatas().get(i);
            for (int j = 0; j < listStr.size(); j++) {
                String dataString = listStr.get(j);
                if (!dataString.isEmpty()) {
                    Field field = fieldDtos.get(j).getField();
                    Class<?> type = field.getType();
                    // 只支持8中基本类型和String类型 如有其他类型 请自行添加
                    if (type == String.class) {
                        field.set(uBean, dataString.trim());
                    } else if (type == Integer.class || type == int.class) {
                        field.set(uBean, Integer.parseInt(dataString));
                    } else if (type == Double.class || type == double.class) {
                        field.set(uBean, Double.parseDouble(dataString));
                    } else if (type == BigDecimal.class) {
                        field.set(uBean, NumberUtils.createBigDecimal(dataString));
                    }else if (type == Float.class || type == float.class) {
                        field.set(uBean, Float.parseFloat(dataString));
                    } else if (type == Long.class || type == long.class) {
                        field.set(uBean, Long.parseLong(dataString));
                    } else if (type == Boolean.class || type == boolean.class) {
                        field.set(uBean, Boolean.parseBoolean(dataString));
                    } else if (type == Short.class || type == short.class) {
                        field.set(uBean, Short.parseShort(dataString));
                    } else if (type == Byte.class || type == byte.class) {
                        field.set(uBean, Byte.parseByte(dataString));
                    } else if (type == Character.class || type == char.class) {
                        field.set(uBean, dataString.charAt(0));
                    } else if (type == Date.class) {
                        try {
                            if (dataString.contains(":")) {
                                field.set(uBean, simpleDateFormat.parse(dataString.replace("/", "-")));
                            } else {
                                field.set(uBean, simpleDateFormat2.parse(dataString.replace("/", "-")));
                            }
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            listBean.add(uBean);
        }
        return listBean;
    }

    /**
     * @methodName: readExcel 读取excel工具类
     * 1页sheet  数据第二行
     * @param: [is, tClass]  传入的实体类,成员变量类型只能是基本类型和字符串
     * @return: java.util.List<T>
     * @Description: 读取excel文件, 将其转换为javabean
     */
    public static <T> List<T> readExcel2Bean(InputStream is, Class<T> tClass) throws IOException, IllegalAccessException, InstantiationException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<List<String>> list = ExcelUtils.readExcel(is);
        List<T> listBean = new ArrayList<T>();
        Field[] fields = tClass.getDeclaredFields();
        T uBean = null;
        for (int i = 1; i < list.size(); i++) {// i=1是因为第一行不要
            uBean = tClass.newInstance();
            List<String> listStr = list.get(i);
            for (int j = 0; j < listStr.size(); j++) {
                if (j >= fields.length) {
                    break;
                }
                Field field = fields[j];
                Schema annotation = field.getAnnotation(Schema.class);
                String name = annotation.description();
                String dataString = listStr.get(j);
                field.setAccessible(true);
                if (dataString.length() > 0 && dataString != null) {
                    Class<?> type = field.getType();
                    // 只支持8中基本类型和String类型 如有其他类型 请自行添加
                    if (type == String.class) {
                        field.set(uBean, dataString);
                    } else if (type == Integer.class || type == int.class) {
                        field.set(uBean, Integer.parseInt(dataString));
                    } else if (type == Double.class || type == double.class) {
                        field.set(uBean, Double.parseDouble(dataString));
                    } else if (type == Float.class || type == float.class) {
                        field.set(uBean, Float.parseFloat(dataString));
                    } else if (type == Long.class || type == long.class) {
                        field.set(uBean, Long.parseLong(dataString));
                    } else if (type == Boolean.class || type == boolean.class) {
                        field.set(uBean, Boolean.parseBoolean(dataString));
                    } else if (type == Short.class || type == short.class) {
                        field.set(uBean, Short.parseShort(dataString));
                    } else if (type == Byte.class || type == byte.class) {
                        field.set(uBean, Byte.parseByte(dataString));
                    } else if (type == Character.class || type == char.class) {
                        field.set(uBean, dataString.charAt(0));
                    } else if (type == Date.class) {
                        try {
                            field.set(uBean, simpleDateFormat.parse(dataString));
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            listBean.add(uBean);
        }
        return listBean;
    }

    /**
     * Excel读取 操作,返回内容 1页sheet
     */
    private static List<List<String>> readExcel(InputStream is) throws IOException {
        Workbook wb = null;
        try {
            wb = WorkbookFactory.create(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /** 得到第一个sheet */
        Sheet sheet = wb.getSheetAt(0);
        /** 得到Excel的行数 */
        int totalRows = sheet.getPhysicalNumberOfRows();
        /** 得到Excel的列数 */
        int totalCells = 0;
        if (totalRows >= 1 && sheet.getRow(0) != null) {
            totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
        }
        List<List<String>> dataLst = new ArrayList<List<String>>();
        /** 循环Excel的行 */
        for (int r = 0; r < totalRows; r++) {
            Row row = sheet.getRow(r);
            if (row == null)
                continue;
            List<String> rowLst = new ArrayList<String>();
            if (row.getCell(0) == null) {
                break;
            }
            /** 循环Excel的列 */
            for (int c = 0; c < totalCells; c++) {
                Cell cell = row.getCell(c);
                String cellValue = "";
                if (null != cell) {
                    HSSFDataFormatter hSSFDataFormatter = new HSSFDataFormatter();
                    cellValue = hSSFDataFormatter.formatCellValue(cell);
                }
                rowLst.add(cellValue);
            }
            /** 保存第r行的第c列 */
            dataLst.add(rowLst);
        }
        return dataLst;
    }


    /**
     * Excel读取 操作,返回内容
     */
    public static List<ExeclDto> readExcelAllSheet(InputStream is) throws IOException {
        List<ExeclDto> execlDtos = new ArrayList<>();
        Workbook wb = null;
        try {
            wb = WorkbookFactory.create(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int numberOfSheets = wb.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            /** 得到第一个sheet */
            Sheet sheet = wb.getSheetAt(i);
            /** 得到Excel的行数 */
            int totalRows = sheet.getPhysicalNumberOfRows();
            /** 得到Excel的列数 */
            int totalCells = 0;
            if (totalRows >= 1 && sheet.getRow(0) != null) {
                totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
            }
            List<List<String>> dataLst = new ArrayList<List<String>>();
            /** 循环Excel的行 */
            for (int r = 0; r < totalRows; r++) {
                Row row = sheet.getRow(r);
                if (row == null || row.getPhysicalNumberOfCells() == 0)
                    continue;
                List<String> rowLst = new ArrayList<String>();
                ////获取Excel当前行的第一位，若未null则不处理 ---> 防止poi读取Excel多读取没有数据的一行，导致非空字段为空
                //避免空串行
                if (row.getCell(0) == null || row.getCell(0).getCellType().getCode() == 3) {
                    break;
                }
                /** 循环Excel的列 */
                for (int c = 0; c < totalCells; c++) {
                    Cell cell = row.getCell(c);
                    String cellValue = "";
                    if (null != cell) {
                        int code = cell.getCellType().getCode();
                        HSSFDataFormatter hSSFDataFormatter = new HSSFDataFormatter();
                        if (code == 0) {
                            short dataFormat = cell.getCellStyle().getDataFormat();
                            if (DateUtil.isCellDateFormatted(cell)) {
                                if (dataFormat == 31 || dataFormat == 57 || dataFormat == 58 || dataFormat == 14) {
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    double numericCellValue = cell.getNumericCellValue();
                                    Date javaDate = DateUtil.getJavaDate(numericCellValue);
                                    cellValue = simpleDateFormat.format(javaDate);
                                } else {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    cellValue = sdf.format(cell.getDateCellValue());
                                }
                            } else {
                                cellValue = hSSFDataFormatter.formatCellValue(cell);
                            }
                        } else {
                            cellValue = hSSFDataFormatter.formatCellValue(cell);
                        }
                    }
                    rowLst.add(cellValue);
                }
                /** 保存第r行的第c列 */
                dataLst.add(rowLst);
            }
            if (CollectionUtil.isNotEmpty(dataLst)) {
                List<String> collect = dataLst.get(0).stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
                // 去除List集合中元素的空格
                for (int x = 0; x < collect.size(); x++) {
                    String element = collect.get(x);
                    if (element.length() > 1) {
                        String start = element.substring(0, 1);
                        String end = element.substring(element.length() - 1);
                        String end2 = end.replaceAll("(\\u00A0+| )", "");
                        String start2 = start.replaceAll("(\\u00A0+| )", "");
                        String finals = start2 + element.substring(1, element.length() - 1) + end2;
                        collect.set(x, finals);
                    }
                }
                dataLst.set(0, collect);
                ExeclDto execlDto = new ExeclDto(dataLst, sheet.getSheetName(), i);
                execlDtos.add(execlDto);
            }
        }
        return execlDtos;
    }

    public static void writeExcel(HttpServletResponse response, List<? extends Object> data, String fileName, String sheetName, Class clazz) throws Exception {
        //表头样式
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        //设置表头居中对齐
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //内容样式
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        //设置内容靠左对齐
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
        EasyExcel.write(getOutputStream(fileName, response), clazz).excelType(ExcelTypeEnum.XLSX).sheet(sheetName).registerWriteHandler(horizontalCellStyleStrategy).doWrite(data);
    }

    private static OutputStream getOutputStream(String fileName, HttpServletResponse response) throws Exception {
        fileName = URLEncoder.encode(fileName, "UTF-8");
        response.setContentType("application/octet-stream");
//        response.setCharacterEncoding("utf8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        return response.getOutputStream();
    }

    public static void main(String[] args) throws ClassNotFoundException {
        Class<?> aClass = Class.forName("org.jxls.transform.poi.PoiTransformer");
        System.out.println(aClass);
    }


}