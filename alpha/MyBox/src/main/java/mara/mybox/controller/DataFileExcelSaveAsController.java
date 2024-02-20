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
public class DataFileExcelSaveAsController extends BaseChildController {

    protected DataFileExcelController fileController;

    @FXML
    protected CheckBox targetWithNamesCheck, currentOnlyCheck;

    public void setParameters(DataFileExcelController parent) {
        try {
            fileController = parent;
            if (fileController == null || fileController.dataFileExcel == null) {
                close();
                return;
            }
            baseName = fileController.baseName;
            setFileType(fileController.TargetFileType);
            setTitle(message("SaveAs") + " - " + fileController.getTitle());

            targetWithNamesCheck.setSelected(UserConfig.getBoolean(baseName + "TargetWithNames", true));
            currentOnlyCheck.setSelected(UserConfig.getBoolean(baseName + "CurrentOnly", false));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        try {
            UserConfig.setBoolean(baseName + "TargetWithNames", targetWithNamesCheck.isSelected());
            UserConfig.setBoolean(baseName + "CurrentOnly", currentOnlyCheck.isSelected());
            fileController.saveAsType = saveAsType;
            if (closeAfterCheck.isSelected()) {
                close();
            }
            fileController.saveAsAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        static methods
     */
    public static DataFileExcelSaveAsController open(DataFileExcelController parent) {
        try {
            if (parent == null) {
                return null;
            }
            DataFileExcelSaveAsController controller = (DataFileExcelSaveAsController) WindowTools.branchStage(
                    parent, Fxmls.DataFileExcelSaveAsFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
