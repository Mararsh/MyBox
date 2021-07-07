package mara.mybox.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.db.data.TextClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlWindow;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2021-7-3
 * @License Apache License Version 2.0
 */
public class TextInMyBoxClipboardController extends BaseController {

    @FXML
    protected ControlTextsClipboard clipboardController;
    @FXML
    protected TextArea textArea;
    @FXML
    protected Button editButton;

    public TextInMyBoxClipboardController() {
        baseTitle = message("TextInMyBoxClipboard");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            clipboardController.setParameters(textArea, false);
            clipboardController.tableView.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends TextClipboard> ov, TextClipboard t, TextClipboard t1) -> {
                        TextClipboard selected = clipboardController.tableView.getSelectionModel().getSelectedItem();
                        if (selected != null) {
                            textArea.setText(selected.getText());
                        } else {
                            textArea.setText(null);
                        }
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            FxmlControl.setTooltip(copyButton, new Tooltip(message("CopyToSystemClipboard")));
            copyButton.disableProperty().bind(Bindings.isEmpty(textArea.textProperty()));
            editButton.disableProperty().bind(Bindings.isEmpty(textArea.textProperty()));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void copyAction() {
        copyToSystemClipboard(textArea.getText());
    }

    @FXML
    public void editAction(ActionEvent event) {
        TextEditerController controller = (TextEditerController) FxmlWindow.openStage(CommonValues.TextEditerFxml);
        controller.loadContexts(textArea.getText());
        controller.toFront();
    }

    /*
        static methods
     */
    public static TextInMyBoxClipboardController oneOpen() {
        TextInMyBoxClipboardController controller = null;
        Stage stage = FxmlWindow.findStage(message("TextInMyBoxClipboard"));
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (TextInMyBoxClipboardController) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (TextInMyBoxClipboardController) FxmlWindow.openStage(CommonValues.TextInMyBoxClipboardFxml);
        }
        if (controller != null) {
            controller.toFront();
        }
        return controller;
    }

    public static void updateClipboard() {
        Platform.runLater(() -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    Object controller = stage.getUserData();
                    if (controller == null) {
                        continue;
                    }
                    if (controller instanceof TextInMyBoxClipboardController) {
                        try {
                            ((TextInMyBoxClipboardController) controller).clipboardController.refreshAction();
                        } catch (Exception e) {
                        }
                    }
                } else if (window instanceof Popup) {
                    Object object = window.getUserData();
                    if (object != null && object instanceof PopTextsClipboardController) {
                        try {
                            ((PopTextsClipboardController) object).clipboardController.refreshAction();
                        } catch (Exception e) {
                        }
                    }
                }

            }
        });
    }

}
