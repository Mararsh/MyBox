package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
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
 * @CreateDate 2021-7-3
 * @License Apache License Version 2.0
 */
public class TextInMyBoxClipboardController extends BaseDataTableController<TextClipboard> {

    protected Clipboard clipboard;

    @FXML
    protected TableColumn<TextClipboard, String> textColumn;
    @FXML
    protected TableColumn<TextClipboard, Date> timeColumn;
    @FXML
    protected TableColumn<TextClipboard, Long> lengthColumn;

    @FXML
    protected TextArea textArea;
    @FXML
    protected Label textLabel;
    @FXML
    protected CheckBox noDupCheck;

    public TextInMyBoxClipboardController() {
        baseTitle = Languages.message("TextInMyBoxClipboard");
        TipsLabelKey = "TextInMyBoxClipboardTips";
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

    @Override
    public void initControls() {
        try {
            super.initControls();

            clipboard = Clipboard.getSystemClipboard();
            copyToSystemClipboardButton.setDisable(true);
            copyToMyBoxClipboardButton.setDisable(true);
            editButton.setDisable(true);

            noDupCheck.setSelected(UserConfig.getBoolean("TextClipboardNoDuplication", true));
            noDupCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean("TextClipboardNoDuplication", noDupCheck.isSelected());
                }
            });
            textArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String ov, String nv) {
                    textChanged(nv);
                }
            });

            refreshAction();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void textChanged(String nv) {
        int len = nv == null ? 0 : nv.length();
        textLabel.setText(Languages.message("Length") + ": " + len);
        copyToSystemClipboardButton.setDisable(len == 0);
        copyToMyBoxClipboardButton.setDisable(len == 0);
        editButton.setDisable(len == 0);
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

    @FXML
    @Override
    public void copyToMyBoxClipboard() {
        String s = textArea.getSelectedText();
        if (s == null || s.isEmpty()) {
            s = textArea.getText();
        }
        if (s == null || s.isEmpty()) {
            popError(Languages.message("CopyNone"));
            return;
        }
        TextClipboardTools.copyToMyBoxClipboard(myController, s);
    }

    @FXML
    @Override
    public void copyToSystemClipboard() {
        String s = textArea.getSelectedText();
        if (s == null || s.isEmpty()) {
            s = textArea.getText();
        }
        if (s == null || s.isEmpty()) {
            popError(Languages.message("CopyNone"));
            return;
        }
        TextClipboardTools.copyToSystemClipboard(myController, s);
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
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.loadContents(s);
        controller.toFront();
    }

    @FXML
    @Override
    public void refreshAction() {
        loadTableData();
        updateStatus();
    }

    public void updateStatus() {
        if (TextClipboardTools.isMonitoring()) {
            bottomLabel.setText(Languages.message("MonitoringTextInSystemClipboard"));
        } else {
            bottomLabel.setText(Languages.message("NotMonitoringTextInSystemClipboard"));
        }
    }

    /*
        static methods
     */
    public static TextInMyBoxClipboardController oneOpen() {
        TextInMyBoxClipboardController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object == null) {
                continue;
            }
            if (object instanceof TextInMyBoxClipboardController) {
                controller = (TextInMyBoxClipboardController) object;
                controller.toFront();
                controller.refreshAction();
                break;
            }
        }
        if (controller == null) {
            controller = (TextInMyBoxClipboardController) WindowTools.openStage(Fxmls.TextInMyBoxClipboardFxml);
        }
        return controller;
    }

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
                    ((TextClipboardPopController) object).refreshAction();
                }
                if (object instanceof TextInMyBoxClipboardController) {
                    ((TextInMyBoxClipboardController) object).refreshAction();
                }
            }
        });
    }

    public static void updateMyBoxClipboardStatus() {
        Platform.runLater(() -> {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                Object object = window.getUserData();
                if (object == null) {
                    continue;
                }
                if (object instanceof TextClipboardPopController) {
                    ((TextClipboardPopController) object).updateStatus();
                }
                if (object instanceof TextInMyBoxClipboardController) {
                    ((TextInMyBoxClipboardController) object).updateStatus();
                }
            }
        });
    }

}
