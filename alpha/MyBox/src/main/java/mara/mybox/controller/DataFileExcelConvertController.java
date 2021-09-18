package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * @Author Mara
 * @CreateDate 2020-12-05
 * @License Apache License Version 2.0
 */
public class DataFileExcelConvertController extends BaseDataConvertController {

    @FXML
    protected CheckBox withNamesCheck;

    public DataFileExcelConvertController() {
        baseTitle = Languages.message("ExcelConvert");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Excel, VisitHistory.FileType.All);
    }

    @Override
    public void initOptionsSection() {
        try {
            super.initOptionsSection();
            convertController.setControls(this, pdfOptionsController);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean matchType(File file) {
        String suffix = FileNameTools.getFileSuffix(file.getName());
        if (suffix == null) {
            return false;
        }
        suffix = suffix.trim().toLowerCase();
        return "xlsx".equals(suffix) || "xls".equals(suffix);
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        String result;
        String filePrefix = FileNameTools.getFilePrefix(srcFile.getName());
        try ( Workbook wb = WorkbookFactory.create(srcFile)) {
            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                Sheet sheet = wb.getSheetAt(s);
                updateLogs(Languages.message("Reading") + " " + Languages.message("Sheet") + ":" + sheet.getSheetName());
                convertController.names = new ArrayList<>();
                for (Row row : sheet) {
                    if (task == null || task.isCancelled()) {
                        return message("Cancelled");
                    }
                    if (row == null) {
                        continue;
                    }
                    List<String> rowData = new ArrayList<>();
                    for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
                        rowData.add(MicrosoftDocumentTools.cellString(row.getCell(c)));
                    }
                    if (convertController.names.isEmpty()) {
                        if (withNamesCheck.isSelected()) {
                            convertController.names.addAll(rowData);
                            convertController.openWriters(filePrefix + "_" + sheet.getSheetName());
                            continue;
                        } else {
                            for (int c = 1; c <= rowData.size(); c++) {
                                convertController.names.add(Languages.message("Field") + c);
                            }
                            convertController.openWriters(filePrefix + "_" + sheet.getSheetName());
                        }
                    }
                    convertController.writeRow(rowData);
                }
                convertController.closeWriters();
            }
            result = Languages.message("Handled");
        } catch (Exception e) {
            result = e.toString();
        }
        return result;
    }

}
