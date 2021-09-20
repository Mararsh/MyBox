package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-25
 * @License Apache License Version 2.0
 */
public class DataFileTextController extends BaseDataFileController {

    @FXML
    protected ControlSheetText sheetController;
    @FXML
    protected ControlTextOptions readOptionsController, writeOptionsController;

    public DataFileTextController() {
        baseTitle = message("EditTextDataFile");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
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

            readOptionsController.setControls(baseName + "Read", true);
            writeOptionsController.setControls(baseName + "Write", false);
            pickOptions();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void pickOptions() {
        try {
            sheetController.sourceCharset = readOptionsController.charset;
            sheetController.sourceWithNames = readOptionsController.withNamesCheck.isSelected();
            sheetController.sourceDelimiterName = readOptionsController.delimiterName;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
        sheetController.saveAsType = saveAsType;
        sheetController.targetCharset = writeOptionsController.charset;
        sheetController.targetDelimiterName = writeOptionsController.delimiterName;
        sheetController.targetWithNames = writeOptionsController.withNamesCheck.isSelected();
        sheetController.saveAs();
    }

}
