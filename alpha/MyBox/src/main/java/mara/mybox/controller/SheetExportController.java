package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SoundTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-2
 * @License Apache License Version 2.0
 */
public class SheetExportController extends BaseTaskController {

    protected String filePrefix;

    @FXML
    protected SheetExportDataController dataController;
    @FXML
    protected VBox dataVBox, formatVBox, pdfOptionsVBox, targetVBox;
    @FXML
    protected ControlPdfWriteOptions pdfOptionsController;
    @FXML
    protected ControlDataConvert convertController;

    public SheetExportController() {
        baseTitle = Languages.message("Export");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            convertController.setControls(this, pdfOptionsController);

            pdfOptionsController.pixSizeRadio.setDisable(true);
            pdfOptionsController.standardSizeRadio.fire();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        try {
            if (targetPath == null) {
                popError(message("InvalidParameters"));
                return false;
            }
            if (dataController.sheetController.columns == null) {
                popError(message("NoData"));
                return false;
            }

            if (dataController.sheetController.sourceFile == null) {
                if (dataController.sheetController instanceof ControlMatrixEdit) {
                    filePrefix = ((ControlMatrixEdit) (dataController.sheetController)).nameInput.getText();
                }
            } else {
                filePrefix = FileNameTools.getFilePrefix(dataController.sourceFile.getName());
            }
            if (filePrefix == null || filePrefix.isBlank()) {
                filePrefix = "";
            }
            filePrefix += "_" + new Date().getTime();

            return convertController.initParameters();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    protected void beforeTask() {
        try {
            dataVBox.setDisable(true);
            formatVBox.setDisable(true);
            targetVBox.setDisable(true);
            pdfOptionsVBox.setDisable(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected boolean doTask() {
        try {
            List<Integer> cols = dataController.cols();

            List<String> names = new ArrayList<>();
            for (int c : cols) {
                names.add(dataController.sheetController.columns.get(c).getName());
            }
            convertController.names = names;
            if (dataController.rowAllRadio.isSelected()) {
                return dataController.sheetController.exportCols(this, cols);
            } else {
                return dataController.sheetController.exportRowsCols(this, dataController.rows(), cols);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    protected void afterSuccess() {
        try {
            SoundTools.miao3();
            if (targetPath != null && targetPath.exists()) {
                browseURI(targetPath.toURI());
                recordFileOpened(targetPath);
            } else {
                popInformation(message("NoFileGenerated"));
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void afterTask() {
        try {
            dataVBox.setDisable(false);
            formatVBox.setDisable(false);
            targetVBox.setDisable(false);
            pdfOptionsVBox.setDisable(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void cancelAction() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        convertController.closeWriters();
    }

}
