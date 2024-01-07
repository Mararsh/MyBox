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
public class DataFileExcelFormatController extends BaseChildController {

    protected DataFileExcelController fileController;

    @FXML
    protected CheckBox sourceWithNamesCheck;

    public DataFileExcelFormatController() {
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

            sourceWithNamesCheck.setSelected(UserConfig.getBoolean(baseName + "SourceWithNames", true));
            sourceWithNamesCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (!isSettingValues) {
                    UserConfig.setBoolean(baseName + "SourceWithNames", newValue);
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
