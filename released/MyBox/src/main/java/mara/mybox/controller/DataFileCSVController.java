package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVFormat;

/**
 * @Author Mara
 * @CreateDate 2020-12-24
 * @License Apache License Version 2.0
 */
public class DataFileCSVController extends BaseDataFileController {

    protected Charset sourceCharset;
    protected CSVFormat sourceCsvFormat;

    @FXML
    protected ControlCsvOptions csvReadController, csvWriteController;
    @FXML
    protected VBox mainBox;
    @FXML
    protected ControlSheetCSV sheetController;

    public DataFileCSVController() {
        baseTitle = message("EditCSV");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.CSV);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            dataController = sheetController;
            dataController.setParent(this);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            csvReadController.setControls(baseName + "Read");
            csvWriteController.setControls(baseName + "Write");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void pickOptions() {
        try {
            sheetController.sourceCharset = csvReadController.charset;
            sheetController.sourceCsvDelimiter = csvReadController.delimiter;
            sheetController.autoDetermineSourceCharset = csvReadController.autoDetermine;
            sheetController.sourceWithNames = csvReadController.withNamesCheck.isSelected();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setFile(File file, Charset charset, boolean withName, char delimiter) {
        sourceFile = file;
        csvReadController.withNamesCheck.setSelected(withName);
        csvReadController.setDelimiter(delimiter);
        csvReadController.setCharset(charset);
        sheetController.initCurrentPage();
        sheetController.userSavedDataDefinition = false;
        loadFile();
    }

    @Override
    protected void updateInfoLabel() {
        String info = "";
        if (sourceFile != null) {
            info = message("FileSize") + ": " + FileTools.showFileSize(sourceFile.length()) + "\n"
                    + message("FileModifyTime") + ": " + DateTools.datetimeToString(sourceFile.lastModified()) + "\n"
                    + message("Charset") + ": " + sheetController.sourceCharset + "\n"
                    + message("Delimiter") + ": " + TextTools.delimiterMessage(sheetController.sourceDelimiterName) + "\n"
                    + message("FirstLineAsNames") + ": " + (sheetController.sourceWithNames ? message("Yes") : message("No")) + "\n";
        }
        if (sheetController.pagesNumber <= 1) {
            info += message("RowsNumber") + ":" + (sheetController.sheetInputs == null ? 0 : sheetController.sheetInputs.length) + "\n";
        } else {
            info += message("LinesNumberInFile") + ":" + sheetController.totalSize + "\n";
        }
        info += message("ColumnsNumber") + ": " + (sheetController.columns == null ? "0" : sheetController.columns.size()) + "\n"
                + message("CurrentPage") + ": " + StringTools.format(sheetController.currentPage)
                + " / " + StringTools.format(sheetController.pagesNumber) + "\n";
        if (sheetController.pagesNumber > 1 && sheetController.sheetInputs != null) {
            info += message("RowsRangeInPage")
                    + ": " + StringTools.format(sheetController.currentPageStart) + " - "
                    + StringTools.format(sheetController.currentPageStart + sheetController.sheetInputs.length - 1)
                    + " ( " + StringTools.format(sheetController.sheetInputs.length) + " )\n";
        }
        info += message("PageModifyTime") + ": " + DateTools.nowString();
        fileInfoLabel.setText(info);
    }

    @FXML
    @Override
    public void saveAsAction() {
        sheetController.sourceFile = sourceFile;
        sheetController.targetCharset = csvWriteController.charset;
        sheetController.targetCsvDelimiter = csvWriteController.delimiter;
        sheetController.targetWithNames = csvWriteController.withNamesCheck.isSelected();
        sheetController.saveAsType = saveAsType;
        sheetController.saveAs();
    }


    /*
        static
     */
    public static DataFileCSVController open(File file, Charset charset, boolean withNames, char delimiter) {
        DataFileCSVController controller = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
        controller.setFile(file, charset, withNames, delimiter);
        return controller;
    }

}