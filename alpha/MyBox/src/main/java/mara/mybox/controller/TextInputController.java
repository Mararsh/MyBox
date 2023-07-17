package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-11-15
 * @License Apache License Version 2.0
 */
public class TextInputController extends BaseInputController {

    @FXML
    protected TextArea textArea;
    @FXML
    protected CheckBox wrapCheck;

    public void setParameters(BaseController parent, String title, String initValue) {
        try {
            super.setParameters(parent, title);

            textArea.setText(initValue);
            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "Wrap", true));
            wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Wrap", newValue);
                    textArea.setWrapText(newValue);
                }
            });
            textArea.setWrapText(wrapCheck.isSelected());

        } catch (Exception e) {
            MyBoxLog.debug(e);

        }
    }

    @Override
    public String getInputString() {
        return textArea.getText();
    }

    public static TextInputController open(BaseController parent, String title, String initValue) {
        try {
            TextInputController controller = (TextInputController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.TextInputFxml, true);
            controller.setParameters(parent, title, initValue);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
