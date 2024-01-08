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
public class DataFileCSVFormatController extends BaseChildController {

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
            setTitle(message("Format") + " - " + fileController.getTitle());

            optionsController.setControls(baseName + "Read", true, false);
            optionsController.withNamesCheck.setSelected(fileController.dataFileCSV.isHasHeader());
            optionsController.setDelimiterName(fileController.dataFileCSV.getDelimiter());
            optionsController.setCharset(fileController.dataFileCSV.getCharset());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        UserConfig.setBoolean(baseName + "SourceWithNames", optionsController.withNamesCheck.isSelected());
        UserConfig.setBoolean(baseName + "SourceAutoDetermine", optionsController.autoDetermine);
        UserConfig.setString(baseName + "SourceCharset", optionsController.getCharsetName());
        UserConfig.setString(baseName + "SourceDelimiter", optionsController.getDelimiterName());
        fileController.refreshFile();

        if (closeAfterCheck.isSelected()) {
            close();
        }
    }


    /*
        static methods
     */
    public static DataFileCSVFormatController open(DataFileCSVController parent) {
        try {
            if (parent == null) {
                return null;
            }
            DataFileCSVFormatController controller = (DataFileCSVFormatController) WindowTools.branchStage(
                    parent, Fxmls.DataFileCSVFormatFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
