package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-7-19
 * @License Apache License Version 2.0
 */
public class DataFileCSVSaveAsController extends BaseChildController {

    protected DataFileCSVController fileController;

    @FXML
    protected ControlTextOptions optionsController;

    public void setParameters(DataFileCSVController parent) {
        try {
            fileController = parent;
            if (fileController == null || fileController.dataFileCSV == null) {
                close();
                return;
            }
            baseName = fileController.baseName;
            setFileType(fileController.TargetFileType);
            setTitle(message("SaveAs") + " - " + fileController.getTitle());

            optionsController.setControls(baseName + "Write", false, false);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        try {
            UserConfig.setBoolean(baseName + "TargetWithNames", optionsController.withNamesCheck.isSelected());
            UserConfig.setString(baseName + "TargetCharset", optionsController.getCharsetName());
            UserConfig.setString(baseName + "TargetDelimiter", optionsController.getDelimiterName());
            fileController.saveAsType = saveAsType;
            if (closeAfterCheck.isSelected()) {
                close();
            }
            fileController.saveAs();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        static methods
     */
    public static DataFileCSVSaveAsController open(DataFileCSVController parent) {
        try {
            if (parent == null) {
                return null;
            }
            DataFileCSVSaveAsController controller = (DataFileCSVSaveAsController) WindowTools.branchStage(
                    parent, Fxmls.DataFileCSVSaveAsFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
