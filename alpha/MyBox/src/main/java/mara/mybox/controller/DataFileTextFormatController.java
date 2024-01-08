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
public class DataFileTextFormatController extends BaseChildController {

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
            setFileType(fileController.TargetFileType);
            setTitle(message("Format") + " - " + fileController.getTitle());

            optionsController.setControls(baseName + "Read", true, true);
            optionsController.withNamesCheck.setSelected(fileController.dataFileText.isHasHeader());
            optionsController.setDelimiterName(fileController.dataFileText.getDelimiter());
            optionsController.setCharset(fileController.dataFileText.getCharset());

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
    public static DataFileTextFormatController open(DataFileTextController parent) {
        try {
            if (parent == null) {
                return null;
            }
            DataFileTextFormatController controller = (DataFileTextFormatController) WindowTools.branchStage(
                    parent, Fxmls.DataFileTextFormatFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
