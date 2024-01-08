package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
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
public class DataFileExcelFormatController extends BaseChildController {

    protected DataFileExcelController fileController;

    @FXML
    protected CheckBox sourceWithNamesCheck;

    public void setParameters(DataFileExcelController parent) {
        try {
            fileController = parent;
            if (fileController == null || fileController.dataFileExcel == null) {
                close();
                return;
            }
            baseName = fileController.baseName;
            setFileType(fileController.TargetFileType);
            setTitle(message("Format") + " - " + fileController.getTitle());

            sourceWithNamesCheck.setSelected(UserConfig.getBoolean(baseName + "SourceWithNames", true));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        UserConfig.setBoolean(baseName + "SourceWithNames", sourceWithNamesCheck.isSelected());
        fileController.refreshFile();
        if (closeAfterCheck.isSelected()) {
            close();
        }
    }


    /*
        static methods
     */
    public static DataFileExcelFormatController open(DataFileExcelController parent) {
        try {
            if (parent == null) {
                return null;
            }
            DataFileExcelFormatController controller = (DataFileExcelFormatController) WindowTools.branchStage(
                    parent, Fxmls.DataFileExcelFormatFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
