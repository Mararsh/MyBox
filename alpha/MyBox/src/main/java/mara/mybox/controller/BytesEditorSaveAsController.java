package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-7-19
 * @License Apache License Version 2.0
 */
public class BytesEditorSaveAsController extends BaseChildController {

    protected BytesEditorController fileController;

    public void setParameters(BytesEditorController parent) {
        try {
            fileController = parent;
            if (fileController == null) {
                close();
                return;
            }
            baseName = fileController.baseName;
            setFileType(fileController.TargetFileType);
            setTitle(message("SaveAs") + " - " + fileController.getTitle());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        try {
            targetFile = saveAsFile();
            if (targetFile == null) {
                return;
            }
            fileController.saveAsType = saveAsType;
            fileController.saveAs(targetFile);
            if (closeAfterCheck.isSelected()) {
                close();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        static methods
     */
    public static BytesEditorSaveAsController open(BytesEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            BytesEditorSaveAsController controller = (BytesEditorSaveAsController) WindowTools.branchStage(
                    parent, Fxmls.BytesEditorSaveAsFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
