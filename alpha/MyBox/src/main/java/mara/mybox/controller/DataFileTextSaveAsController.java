package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-7-19
 * @License Apache License Version 2.0
 */
public class DataFileTextSaveAsController extends BaseChildController {

    protected DataFileTextController fileController;

    @FXML
    protected ControlTextOptions optionsController;

    public void setParameters(DataFileTextController parent) {
        try {
            fileController = parent;
            if (fileController == null || fileController.dataFileText == null) {
                close();
                return;
            }
            baseName = fileController.baseName;
            baseTitle = fileController.baseTitle;
            setFileType(fileController.TargetFileType);
            setTitle(fileController.getTitle());

            optionsController.setControls(baseName + "Write", false, true);

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
    public static DataFileTextSaveAsController open(DataFileTextController parent) {
        try {
            if (parent == null) {
                return null;
            }
            DataFileTextSaveAsController controller = (DataFileTextSaveAsController) WindowTools.branchStage(
                    parent, Fxmls.DataFileTextSaveAsFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
