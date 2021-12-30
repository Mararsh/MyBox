package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import static mara.mybox.value.Languages.message;
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
        baseTitle = message("ExcelConvert");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Excel, VisitHistory.FileType.All);
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
        try ( Workbook wb = WorkbookFactory.create(srcFile)) {
            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                Sheet sheet = wb.getSheetAt(s);
                updateLogs(message("Reading") + " " + message("Sheet") + ":" + sheet.getSheetName());
                List<String> names = null;
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
                    if (names == null) {
                        names = new ArrayList<>();
                        if (withNamesCheck.isSelected()) {
                            names.addAll(rowData);
                            convertController.setParameters(targetPath, names, filePrefix(srcFile), skip);
                            continue;
                        } else {
                            for (int c = 1; c <= rowData.size(); c++) {
                                names.add(message("Column") + c);
                            }
                            convertController.setParameters(targetPath, names, filePrefix(srcFile), skip);
                        }
                    }
                    convertController.writeRow(rowData);
                }
                convertController.closeWriters();
            }
            result = message("Handled");
        } catch (Exception e) {
            result = e.toString();
        }
        return result;
    }

}
