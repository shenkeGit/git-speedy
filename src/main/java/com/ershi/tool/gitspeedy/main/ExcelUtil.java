package com.ershi.tool.gitspeedy.main;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 读取Excel
 */
public class ExcelUtil {
    private static String[] dateFormat = {"yyyy-MM-dd", "yyyyMMdd", "yyyy/MM/dd", "yyyy年MM月dd日"};

    private static final Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

    public static final Map<String,Integer> DATE_REGEX = Map.of("\\d{4}-\\d{2}-\\d{2}",0,
            "\\d{8}",1,
            "\\d{4}/\\d{2}/\\d{2}",2,
            "(\\d){4}年(\\d){1,2}月(\\d){1,2}日",3);

    /**
     * 解析Excel
     *
     * @param file 文件
     * @throws Exception 异常
     * @author qianjie
     * @date 2017年3月10日 下午3:35:41
     */
    public static ArrayList<ArrayList<ArrayList<String>>> getExcel(File file,Map<String,String> formatDate) throws Exception {
        ArrayList<ArrayList<ArrayList<String>>> sheetsData = new ArrayList<ArrayList<ArrayList<String>>>();
        String flag = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        if ("xls".equalsIgnoreCase(flag)) {
            try {
                sheetsData = ExcelUtil.readXls(file,formatDate);
            } catch (Exception e) {
                sheetsData = ExcelUtil.read2003(file);
            }
        } else if ("xlsx".equalsIgnoreCase(flag)) {
            sheetsData = ExcelUtil.readXlsx(file,formatDate);
        }
        return sheetsData;
    }

    /**
     * 解析Excel
     *
     * @param file 文件
     * @throws Exception 异常
     * @author qianjie
     * @date 2017年3月10日 下午3:35:41
     */
    public static ArrayList<ArrayList<ArrayList<String>>> getVaExcel(File file,Map<String,String> formatDate) {
        ArrayList<ArrayList<ArrayList<String>>> sheetsData = new ArrayList<ArrayList<ArrayList<String>>>();
        String flag = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        try {
            if ("xls".equalsIgnoreCase(flag)) {
                try {
                    sheetsData = ExcelUtil.readXls(file,formatDate);
                } catch (Exception e) {
                    sheetsData = ExcelUtil.read2003(file);
                }

            } else if ("xlsx".equalsIgnoreCase(flag)) {
                sheetsData = ExcelUtil.readXlsx(file,formatDate);
            }
        } catch (Exception e) {
            return sheetsData;
        }
        return sheetsData;
    }

    public static ArrayList<ArrayList<ArrayList<String>>> getVaExcel(byte[] bytes,String fileName,Map<String,String> formatDate) {
        ArrayList<ArrayList<ArrayList<String>>> sheetsData = new ArrayList<ArrayList<ArrayList<String>>>();
        String flag = fileName.substring(fileName.lastIndexOf(".") + 1);
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            if ("xls".equalsIgnoreCase(flag)) {
                try {
                    sheetsData = ExcelUtil.readInputStreamXls(formatDate, bais);
                } catch (Exception e) {
                    sheetsData = ExcelUtil.readInputStream2003(bais);
                }

            } else if ("xlsx".equalsIgnoreCase(flag)) {
                sheetsData = ExcelUtil.readInputStreamXlsx(formatDate,bais);
            }
        } catch (Exception e) {
            return sheetsData;
        }
        return sheetsData;
    }

    public static ArrayList<ArrayList<ArrayList<String>>> getVaExcel(InputStream inputStream,String fileName,Map<String,String> formatDate) throws Exception {
        ArrayList<ArrayList<ArrayList<String>>> sheetsData = new ArrayList<ArrayList<ArrayList<String>>>();
        ByteArrayOutputStream boStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(bytes)) > -1) {
                boStream.write(bytes,0,len);
            }
            boStream.flush();
        } catch (Exception e) {
            throw new Exception();
        }
        InputStream stream1 = new ByteArrayInputStream(boStream.toByteArray());
        if (fileName.endsWith("xls")) {
            try {
                sheetsData = ExcelUtil.readInputStreamXls(formatDate,stream1);
            } catch (Exception e) {
                InputStream stream2 = new ByteArrayInputStream(boStream.toByteArray());
                sheetsData = ExcelUtil.readInputStream2003(stream2);
            }
        } else if (fileName.endsWith("xlsx")) {
            sheetsData = ExcelUtil.readInputStreamXlsx(formatDate,inputStream);
            inputStream.close();
        }
        return sheetsData;
    }


    /**
     * 解析Excel-xls
     *
     * @param upFile 文件
     * @throws Exception 异常
     * @author qianjie
     * @date 2017年3月10日 下午3:29:01
     */
    public static ArrayList<ArrayList<ArrayList<String>>> readXls(File upFile,Map<String,String> formatDate) throws Exception {
        InputStream is = new FileInputStream(upFile);
        return readInputStreamXls(formatDate, is);
    }

    private static ArrayList<ArrayList<ArrayList<String>>> readInputStreamXls(Map<String, String> formatDate, InputStream is) throws IOException {
        ArrayList<ArrayList<ArrayList<String>>> excelList = new ArrayList<ArrayList<ArrayList<String>>>();
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(is);
        // Read the Sheet
        for (int numSheet = 0; numSheet < hssfWorkbook.getNumberOfSheets(); numSheet++) {
            HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(numSheet);
            if (hssfSheet == null) {
                continue;
            }
            ArrayList<ArrayList<String>> sheetList = new ArrayList<ArrayList<String>>();
            // Read the Row
            for (int rowNum = 0; rowNum <= hssfSheet.getLastRowNum(); rowNum++) {
                HSSFRow hssfRow = hssfSheet.getRow(rowNum);
                if (hssfRow != null) {
                    ArrayList<String> rowList = new ArrayList<String>();
                    for (int cellNum = 0; cellNum <= hssfRow.getLastCellNum(); cellNum++) {
                        HSSFCell hssfCell = hssfRow.getCell(cellNum);
                        String value = "";
                        if (hssfRow.getCell(cellNum) != null) {
                            if (hssfCell.getCellType() == CellType.BOOLEAN) {
                                value = String.valueOf(hssfCell.getBooleanCellValue());
                            } else if (hssfCell.getCellType() == CellType.NUMERIC) {
                                String format = hssfCell.getCellStyle().getDataFormatString();
                                if (format.toLowerCase().contains("yy") || (format.toLowerCase().contains("m") && format.toLowerCase().contains("d"))) {
                                    format = hssfCell.getCellStyle().getDataFormatString();
                                    format = format.replaceAll("[\"|\'|;|@]", "");
                                    format = format.replaceAll("m", "M");
                                    Date date = hssfCell.getDateCellValue();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                                    value = String.valueOf(sdf.format(date));
                                    formatDate.put(value,format);
                                } else if (format.startsWith("reserved")) {
                                    Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(hssfCell.getNumericCellValue());
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                                    value = String.valueOf(sdf.format(date));
                                } else {
                                    if (format.endsWith("%")) {
                                        value = String.valueOf(hssfCell.getNumericCellValue() * 100);
                                    } else {
                                        value = BigDecimal.valueOf(hssfCell.getNumericCellValue()).toPlainString();
                                    }
                                }
                            } else if (hssfCell.getCellType() == CellType.STRING) {
                                value = String.valueOf(hssfCell.getStringCellValue());
                            } else if (hssfCell.getCellType() == CellType.FORMULA) {
                                value = String.valueOf(hssfCell.getCellFormula());
                            }
                        }
                        rowList.add(value);
                    }
                    sheetList.add(rowList);
                } else {
                    sheetList.add(new ArrayList<String>());
                }
            }
            excelList.add(sheetList);
        }
        hssfWorkbook.close();
        is.close();
        return excelList;
    }

    /**
     * 解析Excel-xlsx
     *
     * @param upFile 文件
     * @throws Exception 异常
     * @author qianjie
     * @date 2017年3月10日 下午3:36:55
     */
    public static ArrayList<ArrayList<ArrayList<String>>> readXlsx(File upFile,Map<String,String> formatDate) throws Exception {
        InputStream is = new FileInputStream(upFile);
        return readInputStreamXlsx(formatDate, is);
    }

    private static ArrayList<ArrayList<ArrayList<String>>> readInputStreamXlsx(Map<String, String> formatDate, InputStream is) throws IOException {
        ArrayList<ArrayList<ArrayList<String>>> excelList = new ArrayList<ArrayList<ArrayList<String>>>();
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(is);
        // Read the Sheet
        for (int numSheet = 0; numSheet < xssfWorkbook.getNumberOfSheets(); numSheet++) {
            XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(numSheet);
            if (xssfSheet == null) {
                continue;
            }
            ArrayList<ArrayList<String>> sheetList = new ArrayList<ArrayList<String>>();
            // Read the Row
            for (int rowNum = 0; rowNum <= xssfSheet.getLastRowNum(); rowNum++) {
                XSSFRow xssfRow = xssfSheet.getRow(rowNum);
                if (xssfRow != null) {
                    ArrayList<String> rowList = new ArrayList<String>();
                    for (int cellNum = 0; cellNum <= xssfRow.getLastCellNum(); cellNum++) {
                        XSSFCell xssfCell = xssfRow.getCell(cellNum);
                        String value = "";
                        if (xssfRow.getCell(cellNum) != null) {
                            if (xssfCell.getCellType() == CellType.BOOLEAN) {
                                value = String.valueOf(xssfCell.getBooleanCellValue());
                            } else if (xssfCell.getCellType() == CellType.NUMERIC) {
                                String format = xssfCell.getCellStyle().getDataFormatString();
                                if (format.toLowerCase().contains("yy") || (format.toLowerCase().contains("m") && format.toLowerCase().contains("d"))) {
                                    format = xssfCell.getCellStyle().getDataFormatString();
                                    format = format.replaceAll("[\"|\'|;|@]", "");
                                    format = format.replaceAll("m", "M");
                                    Date date = xssfCell.getDateCellValue();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                                    value = String.valueOf(sdf.format(date));
                                    formatDate.put(value,format);
                                } else if (format.startsWith("reserved")) {
                                    Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(xssfCell.getNumericCellValue());
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
                                    value = String.valueOf(sdf.format(date));
                                } else {
                                    if (format.endsWith("%")) {
                                        value = String.valueOf(xssfCell.getNumericCellValue() * 100);
                                    } else {
                                        value = BigDecimal.valueOf(xssfCell.getNumericCellValue()).toPlainString();
                                    }
                                }
                            } else if (xssfCell.getCellType() == CellType.STRING) {
                                value = String.valueOf(xssfCell.getStringCellValue());
                            } else if (xssfCell.getCellType() == CellType.FORMULA) {
                                value = String.valueOf(xssfCell.getCellFormula());
                            }
                        }
                        rowList.add(value);
                    }
                    sheetList.add(rowList);
                } else {
                    sheetList.add(new ArrayList<String>());
                }
            }
            excelList.add(sheetList);
        }
        xssfWorkbook.close();
        is.close();
        return excelList;
    }

    /**
     * 兼容性高-效率低
     *
     * @param upFile 文件
     * @throws Exception 异常
     * @author qianjie
     * @date 2017年3月13日 下午5:15:30
     */
    public static ArrayList<ArrayList<ArrayList<String>>>  read2003(File upFile) throws Exception {
        InputStream inputs = new FileInputStream(upFile);
        return readInputStream2003(inputs);
    }

    /**
     * 兼容性高-效率低
     *
     * @throws Exception 异常
     * @author qianjie
     * @date 2017年3月13日 下午5:15:30
     */
    public static ArrayList<ArrayList<ArrayList<String>>>  readInputStream2003(InputStream inputs) throws Exception {
        ArrayList<ArrayList<ArrayList<String>>> excelList = new ArrayList<ArrayList<ArrayList<String>>>();
        Workbook wb = Workbook.getWorkbook(inputs);
        // 获得了Workbook对象之后，就可以通过它得到Sheet（工作表）对象了
        Sheet[] sheets = wb.getSheets();
        // 对每个工作表进行循环
        if (sheets != null && sheets.length > 0) {
            // 得到当前工作表的行数
            for (int i = 0; i < sheets.length; i++) {
                Sheet sheet = sheets[i];
                if (sheet == null) {
                    continue;
                }
                ArrayList<ArrayList<String>> sheetList = new ArrayList<>();
                int rowNum = sheet.getRows();
                //得到当前行的所有单元格
                for (int j = 0; j < rowNum; j++) {
                    Cell[] cells = sheet.getRow(j);
                    ArrayList<String> rowList = new ArrayList<>();
                    if (cells != null && cells.length > 0) {
                        // 读取当前单元格的值
                        for (int k = 0; k < cells.length; k++) {
                            Cell cell = cells[k];
                            String value = "";
                            if (cell.getType() == jxl.CellType.NUMBER) {
                                value = new BigDecimal(cell.getContents().trim().replace(",", "")).toPlainString();
                            } else {
                                value = cell.getContents().trim();
                            }
                            rowList.add(value);
                        }
                    }
                    sheetList.add(rowList);
                }
                excelList.add(sheetList);
            }
        }
        // 最后关闭资源，释放内存
        wb.close();
        return excelList;
    }


    /**
     * 科目列为数字处理
     *
     * @param str 字符串
     * @author 雷盼
     * @date 2018/3/13 11:18
     */
    public static String subjectLineString(String str) {
        str = str.trim();
        int num = str.indexOf(".");
        if (num < 0) {
            return str;
        } else {
            try {
                if (Double.parseDouble(str) == Double.parseDouble(str.substring(0, num))) {
                    return str.substring(0, num);
                }
            } catch (Exception e) {
                //异常不处理
            }
        }
        return str;
    }

    /**
     * code为double类型处理
     *
     * @param str 字符串
     * @author 雷盼
     * @date 2019/7/30
     */
    public static String codeLineString(String str) {
        if (str.length() <= 30) {
            return str;
        }
        str = str.trim();
        String[] split = str.split("\\.");
        if (split.length == 2) {
            return new BigDecimal(str).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        }
        return str;
    }


}
