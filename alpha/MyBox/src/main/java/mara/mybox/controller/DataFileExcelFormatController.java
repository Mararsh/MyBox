package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-7-19
 * @License Apache License Version 2.0
 */
public class DataFileExcelFormatController extends BaseChildController {

    protected BaseData2DLoadController fileController;

    @FXML
    protected CheckBox sourceWithNamesCheck;

    public void setParameters(BaseData2DLoadController parent) {
        try {
            fileController = parent;
            if (fileController == null || fileController.data2D == null) {
                close();
                return;
            }
            baseName = fileController.baseName;
            setFileType(fileController.TargetFileType);
            setTitle(message("Format") + " - " + fileController.getTitle());

            sourceWithNamesCheck.setSelected(fileController.data2D.isHasHeader());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (fileController == null || !fileController.isShowing()
                || fileController.data2D == null
                || fileController.data2D.getFile() == null) {
            close();
            return;
        }
        File file = fileController.data2D.getFile();
        if (file == null || !file.exists()) {
            close();
            return;
        }
        fileController.loadExcelFile(file,
                fileController.data2D.getSheet(),
                sourceWithNamesCheck.isSelected());
        if (closeAfterCheck.isSelected()) {
            close();
        }
    }


    /*
        static methods
     */
    public static DataFileExcelFormatController open(BaseData2DLoadController parent) {
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
