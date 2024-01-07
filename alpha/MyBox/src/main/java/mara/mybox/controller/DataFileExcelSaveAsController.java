package mara.mybox.controller;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.db.data.VisitHistory;
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

    public DataFileExcelSaveAsController() {
        baseTitle = message("EditExcel");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Excel);
    }

    public void setParameters(DataFileExcelController parent) {
        try {
            fileController = parent;
            if (fileController == null || fileController.dataFileExcel == null) {
                close();
                return;
            }
            baseName = fileController.baseName;
            setTitle(fileController.getTitle());

            targetWithNamesCheck.setSelected(UserConfig.getBoolean(baseName + "TargetWithNames", true));
            targetWithNamesCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        if (!isSettingValues) {
                            UserConfig.setBoolean(baseName + "TargetWithNames", newValue);
                        }
                    });

            currentOnlyCheck.setSelected(UserConfig.getBoolean(baseName + "CurrentOnly", false));
            currentOnlyCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (!isSettingValues) {
                    UserConfig.setBoolean(baseName + "CurrentOnly", newValue);
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        fileController.saveAsAction();
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
