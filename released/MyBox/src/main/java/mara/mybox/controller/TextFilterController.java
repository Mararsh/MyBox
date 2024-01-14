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
public class TextFilterController extends BaseChildController {

    protected BaseTextController fileController;
    protected String filterConditionsString = "";

    @FXML
    protected ControlTextFilter filterController;

    public void setParameters(BaseTextController parent) {
        try {
            fileController = parent;
            if (fileController == null || fileController.sourceInformation == null) {
                close();
                return;
            }
            baseName = fileController.baseName;
            setFileType(fileController.TargetFileType);
            setTitle(message("FilterLines") + " - " + fileController.getTitle());

            filterController.isBytes = fileController.isBytes();
            filterController.maxLen = fileController.sourceInformation.getPageSize();
            filterController.checkFilterStrings();

            okButton.disableProperty().bind(filterController.valid.not());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (!filterController.pickValue()) {
            popError(message("InvalidParameters"));
            return;
        }
        boolean ok = fileController.filter(filterController);
        if (ok && closeAfterCheck.isSelected()) {
            close();
        }
    }


    /*
        static methods
     */
    public static TextFilterController open(BaseTextController parent) {
        try {
            if (parent == null) {
                return null;
            }
            TextFilterController controller = (TextFilterController) WindowTools.branchStage(
                    parent, Fxmls.TextFilterFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
