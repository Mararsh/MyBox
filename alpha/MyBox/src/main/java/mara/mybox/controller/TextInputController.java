package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-11-15
 * @License Apache License Version 2.0
 */
public class TextInputController extends BaseController {

    protected SimpleBooleanProperty notify;

    @FXML
    protected Label titleLabel;
    @FXML
    protected TextArea textArea;
    @FXML
    protected CheckBox wrapCheck;

    public TextInputController() {
        baseTitle = Languages.message("Table");
    }

    public void setParameters(BaseController parent, String title, String initValue) {
        try {
            parentController = parent;
            if (parent != null) {
                baseName = parent.baseName;
            }
            getMyStage().centerOnScreen();
            titleLabel.setText(title);

            notify = new SimpleBooleanProperty();

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
            MyBoxLog.debug(e.toString());

        }
    }

    public String getText() {
        return textArea.getText();
    }

    @FXML
    @Override
    public void okAction() {
        notify.set(!notify.get());
    }

    @FXML
    @Override
    public void cancelAction() {
        closeStage();
    }

    public SimpleBooleanProperty getNotify() {
        return notify;
    }

    public void setNotify(SimpleBooleanProperty notify) {
        this.notify = notify;
    }

}
