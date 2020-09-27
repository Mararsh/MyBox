package mara.mybox.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2020-2-22
 * @License Apache License Version 2.0
 */
public class ExcelTools {

    public static boolean createXLSX(File file, List<String> columns,
            List<List<String>> rows) {
        try {
            if (file == null || columns == null || rows == null || columns.isEmpty()) {
                return false;

            }
            XSSFWorkbook wb = new XSSFWorkbook();
            XSSFSheet sheet = wb.createSheet("sheet1");
//            sheet.setDefaultColumnWidth(20);

            XSSFRow titleRow = sheet.createRow(0);
            XSSFCellStyle horizontalCenter = wb.createCellStyle();
            horizontalCenter.setAlignment(HorizontalAlignment.CENTER);
            for (int i = 0; i < columns.size(); i++) {
                XSSFCell cell = titleRow.createCell(i);
                cell.setCellValue(columns.get(i));
                cell.setCellStyle(horizontalCenter);
            }
            for (int i = 0; i < rows.size(); i++) {
                XSSFRow row = sheet.createRow(i + 1);
                List<String> values = rows.get(i);
                for (int j = 0; j < values.size(); j++) {
                    XSSFCell cell = row.createCell(j);
                    cell.setCellValue(values.get(j));
                }
            }
            for (int i = 0; i < columns.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            try ( OutputStream fileOut = new FileOutputStream(file)) {
                wb.write(fileOut);
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean createXLS(File file, List<String> columns,
            List<List<String>> rows) {
        try {
            if (file == null || columns == null || rows == null || columns.isEmpty()) {
                return false;

            }
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("sheet1");
//            sheet.setDefaultColumnWidth(40);

            HSSFRow titleRow = sheet.createRow(0);
            HSSFCellStyle horizontalCenter = wb.createCellStyle();
            horizontalCenter.setAlignment(HorizontalAlignment.CENTER);
            for (int i = 0; i < columns.size(); i++) {
                HSSFCell cell = titleRow.createCell(i);
                cell.setCellValue(columns.get(i));
                cell.setCellStyle(horizontalCenter);
            }
            for (int i = 1; i <= rows.size(); i++) {
                HSSFRow row = sheet.createRow(0);
                List<String> values = rows.get(i);
                for (int j = 0; j < values.size(); j++) {
                    HSSFCell cell = row.createCell(j);
                    cell.setCellValue(values.get(j));
                }
            }
            try ( OutputStream fileOut = new FileOutputStream(file)) {
                wb.write(fileOut);
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }

    }

}
