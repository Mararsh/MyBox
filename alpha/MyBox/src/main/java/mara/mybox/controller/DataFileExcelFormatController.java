package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data2d.DataFileExcel;
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

    protected BaseData2DLoadController dataController;

    @FXML
    protected CheckBox sourceWithNamesCheck;

    public boolean isInvalid() {
        return dataController == null
                || !dataController.isShowing()
                || dataController.data2D == null
                || dataController.data2D.getFile() == null
                || !dataController.data2D.getFile().exists()
                || !(dataController.data2D instanceof DataFileExcel);
    }

    public void setParameters(BaseData2DLoadController parent) {
        try {
            dataController = parent;
            if (isInvalid()) {
                close();
                return;
            }
            baseName = dataController.baseName;
            setFileType(dataController.TargetFileType);
            setTitle(message("Format") + " - " + dataController.getTitle());

            sourceWithNamesCheck.setSelected(dataController.data2D.isHasHeader());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (isInvalid()) {
            close();
            return;
        }
        File file = dataController.data2D.getFile();
        if (file == null || !file.exists()) {
            close();
            return;
        }
        dataController.loadExcelFile(file,
                dataController.data2D.getSheet(),
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
