package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import mara.mybox.db.data.TextClipboard;
import mara.mybox.db.table.TableTextClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.cell.TableNumberCell;
import mara.mybox.fxml.cell.TableTextCell;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-4
 * @License Apache License Version 2.0
 */
public class ControlTextClipboard extends BaseDataTableController<TextClipboard> {

    protected Clipboard clipboard;
    protected TextInputControl textInput;
    protected boolean inputEditable;

    @FXML
    protected TableColumn<TextClipboard, String> textColumn;
    @FXML
    protected TableColumn<TextClipboard, Date> timeColumn;
    @FXML
    protected TableColumn<TextClipboard, Long> lengthColumn;
    @FXML
    protected Button useButton, panesMenuButton;
    @FXML
    protected TextArea textArea;
    @FXML
    protected Label textLabel, topLabel;
    @FXML
    protected HBox buttonsBox;
    @FXML
    protected CheckBox noDupCheck;

    public ControlTextClipboard() {
        baseTitle = Languages.message("MyBoxClipboard");
    }

    @Override
    public void setTableDefinition() {
        tableDefinition = new TableTextClipboard();
    }

    @Override
    protected void initColumns() {
        try {
            textColumn.setCellValueFactory(new PropertyValueFactory<>("text"));
            textColumn.setCellFactory(new TableTextCell());

            lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
            lengthColumn.setCellFactory(new TableNumberCell());

            timeColumn.setCellValueFactory(new PropertyValueFactory<>("createTime"));
            timeColumn.setCellFactory(new TableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected int checkSelected() {
        if (isSettingValues) {
            return -1;
        }
        int selection = super.checkSelected();
        TextClipboard selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            textArea.setText(selected.getText());
        }
        return selection;
    }

    public void setParameters(TextInputControl textInput) {
        try {
            this.textInput = textInput;
            tableView.requestFocus();

            inputEditable = textInput != null && !textInput.isDisable() && textInput.isEditable();
            clipboard = Clipboard.getSystemClipboard();
            copyToSystemClipboardButton.setDisable(true);
            copyToMyBoxClipboardButton.setDisable(true);
            editButton.setDisable(true);
            useButton.setDisable(true);
            if (!inputEditable) {
                thisPane.getChildren().remove(topLabel);
                buttonsBox.getChildren().remove(useButton);
            }

            noDupCheck.setSelected(UserConfig.getUserConfigBoolean("TextClipboardNoDuplication", true));
            noDupCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean ov, Boolean nv) {
                    UserConfig.setUserConfigBoolean("TextClipboardNoDuplication", noDupCheck.isSelected());
                }
            });
            textArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String ov, String nv) {
                    int len = nv == null ? 0 : nv.length();
                    textLabel.setText(Languages.message("Length") + ": " + len);
                    copyToSystemClipboardButton.setDisable(len == 0);
                    copyToMyBoxClipboardButton.setDisable(len == 0);
                    editButton.setDisable(len == 0);
                    useButton.setDisable(!inputEditable || len == 0);
                }
            });

            refreshAction();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void pasteContentInSystemClipboard() {
        synchronized (this) {
            String clip = clipboard.getString();
            if (clip == null) {
                popInformation(Languages.message("NoTextInClipboard"));
                return;
            }
            TextClipboardTools.copyToMyBoxClipboard(myController, clip);
        }
    }

    @Override
    public void itemDoubleClicked() {
        useAction();
    }

    @FXML
    public void useAction() {
        if (textInput == null || !inputEditable) {
            inputEditable = false;
            buttonsBox.getChildren().remove(useButton);
            return;
        }
        String s = textArea.getSelectedText();
        if (s == null || s.isEmpty()) {
            s = textArea.getText();
        }
        if (s == null || s.isEmpty()) {
            popError(Languages.message("NoData"));
            return;
        }
        textInput.insertText(textInput.getAnchor(), s);
    }

    @FXML
    @Override
    public void copyToMyBoxClipboard() {
        TextClipboardTools.copyToMyBoxClipboard(myController, textArea);
    }

    @FXML
    @Override
    public void copyToSystemClipboard() {
        TextClipboardTools.copyToSystemClipboard(myController, textArea);
    }

    @FXML
    @Override
    public void editAction(ActionEvent event) {
        String s = textArea.getSelectedText();
        if (s == null || s.isEmpty()) {
            s = textArea.getText();
        }
        if (s == null || s.isEmpty()) {
            popError(Languages.message("NoData"));
            return;
        }
        TextEditerController controller = (TextEditerController) WindowTools.openStage(Fxmls.TextEditerFxml);
        controller.loadContexts(s);
        controller.toFront();
    }

    @FXML
    @Override
    public void refreshAction() {
        loadTableData();
        if (TextClipboardTools.isMonitoring()) {
            bottomLabel.setText(Languages.message("MonitoringTextInSystemClipboard"));
        } else {
            bottomLabel.setText(Languages.message("NotMonitoringTextInSystemClipboard"));
        }
    }

    /*
        static methods
     */
    public static void updateMyBoxClipboard() {
        Platform.runLater(() -> {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                Object object = window.getUserData();
                if (object == null) {
                    continue;
                }
                if (object instanceof TextClipboardPopController) {
                    ((TextClipboardPopController) object).clipboardController.refreshAction();
                }
                if (object instanceof TextInMyBoxClipboardController) {
                    ((TextInMyBoxClipboardController) object).clipboardController.refreshAction();
                }
            }
        });
    }

}
