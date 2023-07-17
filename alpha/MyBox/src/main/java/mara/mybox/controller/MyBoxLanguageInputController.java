package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mara.mybox.controller.MyBoxLanguagesController.LanguageItem;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2022-9-20
 * @License Apache License Version 2.0
 */
public class MyBoxLanguageInputController extends BaseChildController {

    protected MyBoxLanguagesController languagesController;
    protected SimpleBooleanProperty notify;

    @FXML
    protected Tab englishTab, chineseTab;
    @FXML
    protected TextField keyInput;
    @FXML
    protected Label nameLabel;
    @FXML
    protected TextArea englishArea, chineseArea, inputArea;

    public MyBoxLanguageInputController() {
        baseTitle = Languages.message("ManageLanguages");
    }

    public void setParameters(MyBoxLanguagesController languagesController, LanguageItem item) {
        try {
            if (languagesController == null || item == null) {
                close();
                return;
            }
            this.languagesController = languagesController;
            notify = new SimpleBooleanProperty();

            nameLabel.setText(languagesController.langName);
            keyInput.setText(item.getKey());
            englishArea.setText(item.getEnglish());
            chineseArea.setText(item.getChinese());
            inputArea.setText(item.getValue());
            inputArea.requestFocus();

        } catch (Exception e) {
            MyBoxLog.debug(e);

        }
    }

    public String getInput() {
        return inputArea.getText();
    }

    @FXML
    @Override
    public void copyAction() {
        if (englishTab.isSelected()) {
            inputArea.setText(englishArea.getText());
        } else {
            inputArea.setText(chineseArea.getText());
        }
    }

    @FXML
    @Override
    public void clearAction() {
        inputArea.clear();
    }

    @FXML
    @Override
    public void okAction() {
        notify.set(!notify.get());
    }

    public SimpleBooleanProperty getNotify() {
        return notify;
    }

    public void setNotify(SimpleBooleanProperty notify) {
        this.notify = notify;
    }

    @Override
    public void cleanPane() {
        try {
            notify = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    public static MyBoxLanguageInputController open(MyBoxLanguagesController parent, LanguageItem item) {
        try {
            MyBoxLanguageInputController controller = (MyBoxLanguageInputController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.MyBoxLanguageInputFxml, true);
            controller.setParameters(parent, item);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
