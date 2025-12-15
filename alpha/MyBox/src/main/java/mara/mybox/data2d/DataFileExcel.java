package mara.mybox.data2d;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.List;
import mara.mybox.data2d.operate.Data2DReadDefinition;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.data2d.writer.DataFileExcelWriter;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class DataFileExcel extends DataFile {

    protected List<String> sheetNames;
    protected boolean currentSheetOnly;

    public DataFileExcel() {
        dataType = DataType.Excel;
    }

    public void cloneAll(DataFileExcel d) {
        try {
            if (d == null) {
                return;
            }
            super.cloneData(d);
            sheetNames = d.sheetNames;
            currentSheetOnly = d.currentSheetOnly;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void resetData() {
        super.resetData();
        sheetNames = null;
    }

    @Override
    public Data2DDefinition queryDefinition(Connection conn) {
        if (conn == null || dataType == null || file == null || sheet == null) {
            return null;
        }
        return tableData2DDefinition.queryFileSheet(conn, dataType, file, sheet);
    }

    public void initFile(File file, String sheetName) {
        super.initFile(file);
        sheet = sheetName;
        sheetNames = null;
    }

    @Override
    public boolean checkForLoad() {
        if (sheet == null) {
            sheet = "sheet1";
        }
        return true;
    }

    @Override
    public boolean checkForSave() {
        if (sheet == null) {
            sheet = "sheet1";
        }
        if (dataName == null || dataName.isBlank()) {
            if (!isTmpData()) {
                dataName = file.getName();
            } else {
                dataName = DateTools.nowString();
            }
            dataName += " - " + sheet;
        }
        return true;
    }

    @Override
    public long loadDataDefinition(Connection conn) {
        Data2DReadDefinition reader = Data2DReadDefinition.create(this);
        if (reader == null) {
            hasHeader = false;
            return -2;
        }
        reader.setTask(task).start();
        return super.loadDataDefinition(conn);
    }

    public boolean newSheet(String sheetName) {
        if (file == null || !file.exists() || file.length() == 0) {
            return false;
        }
        File tmpFile = FileTmpTools.getTempFile();
        File tmpDataFile = FileTmpTools.getTempFile();
        FileCopyTools.copyFile(file, tmpDataFile);
        try (Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
            Sheet targetSheet = targetBook.createSheet(sheetName);
            List<List<String>> data = tmpData(3, 3);
            for (int r = 0; r < data.size(); r++) {
                if (task == null || task.isCancelled()) {
                    break;
                }
                List<String> values = data.get(r);
                Row targetRow = targetSheet.createRow(r);
                for (int col = 0; col < values.size(); col++) {
                    Cell targetCell = targetRow.createCell(col, CellType.STRING);
                    targetCell.setCellValue(values.get(col));
                }
            }
            try (FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                targetBook.write(fileOut);
            }
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
        try {
            FileDeleteTools.delete(tmpDataFile);
            if (tmpFile == null || !tmpFile.exists()) {
                return false;
            }
            if (FileTools.override(tmpFile, file)) {
                initFile(file);
                hasHeader = false;
                sheet = sheetName;
                tableData2DDefinition.insertData(this);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    public boolean renameSheet(String newName) {
        if (file == null || !file.exists() || sheet == null) {
            return false;
        }
        String oldName = sheet;
        File tmpFile = FileTmpTools.getTempFile();
        File tmpDataFile = FileTmpTools.getTempFile();
        FileCopyTools.copyFile(file, tmpDataFile);
        try (Workbook book = WorkbookFactory.create(tmpDataFile)) {
            book.setSheetName(book.getSheetIndex(sheet), newName);
            try (FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                book.write(fileOut);
            }
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
        try {
            FileDeleteTools.delete(tmpDataFile);
            if (tmpFile == null || !tmpFile.exists()) {
                return false;
            }
            if (FileTools.override(tmpFile, file)) {
                sheet = newName;
                sheetNames.set(sheetNames.indexOf(oldName), sheet);
                tableData2DDefinition.updateData(this);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    public int deleteSheet(String name) {
        if (file == null || !file.exists()) {
            return -1;
        }
        File tmpFile = FileTmpTools.getTempFile();
        File tmpDataFile = FileTmpTools.getTempFile();
        FileCopyTools.copyFile(file, tmpDataFile);
        int index = -1;
        try (Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
            index = targetBook.getSheetIndex(name);
            if (index >= 0) {
                targetBook.removeSheetAt(index);
                try (FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                    targetBook.write(fileOut);
                }
            }
        } catch (Exception e) {
            error = e.toString();
            return -1;
        }
        try {
            FileDeleteTools.delete(tmpDataFile);
            if (tmpFile == null || !tmpFile.exists()) {
                return -1;
            }
            if (index < 0) {
                return -1;
            }
            if (FileTools.override(tmpFile, file)) {
                return index;
            } else {
                return -1;
            }
        } catch (Exception e) {
            error = e.toString();
            return -1;
        }
    }

    @Override
    public Data2DWriter selfWriter() {
        DataFileExcelWriter writer = new DataFileExcelWriter();
        writer.setBaseFile(file)
                .setSheetName(sheet)
                .setTargetData(this)
                .setDataName(dataName)
                .setPrintFile(file)
                .setWriteHeader(hasHeader)
                .setColumns(columns)
                .setHeaderNames(columnNames())
                .setRecordTargetFile(true)
                .setRecordTargetData(true);
        return writer;
    }


    /*
        get/set
     */
    public List<String> getSheetNames() {
        return sheetNames;
    }

    public void setSheetNames(List<String> sheetNames) {
        this.sheetNames = sheetNames;
    }

    public boolean isCurrentSheetOnly() {
        return currentSheetOnly;
    }

    public DataFileExcel setCurrentSheetOnly(boolean currentSheetOnly) {
        this.currentSheetOnly = currentSheetOnly;
        return this;
    }

}
