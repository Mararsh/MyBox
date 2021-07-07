package mara.mybox.controller;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.stage.Stage;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlWindow;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import static mara.mybox.value.AppVariables.startTextClipboardMonitor;
import static mara.mybox.value.AppVariables.stopTextClipboardMonitor;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2021-7-3
 * @License Apache License Version 2.0
 */
public class TextInSystemClipboardController extends BaseController {

    private String lastText;
    private Connection conn;
    private Clipboard clipboard;

    @FXML
    protected Button clipboardButton, clearBoardButton, editButton;
    @FXML
    protected CheckBox accCheck;
    @FXML
    protected Label recordLabel;
    @FXML
    protected ComboBox<String> intervalSelector;
    @FXML
    protected TextArea textArea;

    public TextInSystemClipboardController() {
        baseTitle = AppVariables.message("TextInSystemClipboard");
        TipsLabelKey = "RecordTextTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            clipboard = Clipboard.getSystemClipboard();

            accCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Accumulate", false));
            accCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "Accumulate", newValue);
                }
            });

            intervalSelector.getItems().addAll(Arrays.asList("100", "200", "500", "1000", "1500", "2000"));
            int checkInterval = AppVariables.getUserConfigInt("TextClipboardMonitorInterval", 100);
            if (checkInterval > 0) {
                intervalSelector.setValue(checkInterval + "");
            }
            intervalSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(intervalSelector.getValue());
                        if (v > 0) {
                            intervalSelector.getEditor().setStyle(null);
                            if (AppVariables.getUserConfigBoolean("MonitorTextClipboard", true)) {
                                startTextClipboardMonitor(v);
                            }
                        } else {
                            intervalSelector.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        intervalSelector.getEditor().setStyle(badStyle);
                    }
                }
            });

            editButton.disableProperty().bind(Bindings.isEmpty(textArea.textProperty()));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        FxmlControl.setTooltip(clipboardButton, new Tooltip(message("TextInMyBoxClipboard")));
        FxmlControl.setTooltip(clearBoardButton, new Tooltip(message("ClearSystemClipboard")));

        setMonitor();
        refreshAction();
    }

    @FXML
    protected void myBoxClipBoard() {
        TextInMyBoxClipboardController controller = TextInMyBoxClipboardController.oneOpen();
        if (controller != null) {
            controller.toFront();
        }
    }

    @FXML
    @Override
    public synchronized void startAction() {
        if (AppVariables.getUserConfigBoolean("MonitorTextClipboard", true)) {
            AppVariables.setUserConfigValue("MonitorTextClipboard", false);
            stopTextClipboardMonitor();
        } else {
            AppVariables.setUserConfigValue("MonitorTextClipboard", true);
            startTextClipboardMonitor();
        }
        setMonitor();
    }

    public synchronized void setMonitor() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
                conn = null;
            }
            if (AppVariables.getUserConfigBoolean("MonitorTextClipboard", true)) {
                ControlStyle.setNameIcon(startButton, message("StopRecording"), "iconStop.png");
                startButton.applyCss();
//                getMyStage().setIconified(true);
                recordLabel.setText(message("MonitoringTexts"));

                int checkInterval = AppVariables.getUserConfigInt("TextClipboardMonitorInterval", 100);
                if (checkInterval <= 0) {
                    checkInterval = 100;
                }
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public synchronized void run() {
                                if (clipboard.hasString()) {
                                    loadClip();
                                }
                            }
                        });
                    }
                }, 0, checkInterval);
            } else {
                ControlStyle.setNameIcon(startButton, message("StartRecording"), "iconStart.png");
                startButton.applyCss();
                recordLabel.setText(message("NotMonitoringTexts"));
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void refreshAction() {
        if (!clipboard.hasString()) {
            popError(message("NoTextInClipboard"));
            return;
        }
        loadClip();
    }

    public synchronized String loadClip() {
        String clip = clipboard.getString();
        if (clip == null || clip.isEmpty()) {
            return null;
        }
        if (lastText != null && clip.equals(lastText)) {
            return null;
        }
        lastText = clip;
        if (accCheck.isSelected()) {
            textArea.appendText((textArea.getLength() > 0 ? "\n" : "") + lastText);
        } else {
            textArea.setText(lastText);
        }
        return clip;
    }

    @FXML
    @Override
    public void clearAction() {
        clipboard.clear();
        lastText = null;
        textArea.setText(null);
    }

    @FXML
    public void editAction() {
        TextEditerController controller = (TextEditerController) FxmlWindow.openStage(CommonValues.TextEditerFxml);
        controller.loadContexts(textArea.getText());
        controller.toFront();
    }

    /*
        static methods
     */
    public static TextInSystemClipboardController oneOpen() {
        TextInSystemClipboardController controller = null;
        Stage stage = FxmlWindow.findStage(message("TextInSystemClipboard"));
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (TextInSystemClipboardController) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (TextInSystemClipboardController) FxmlWindow.openStage(CommonValues.TextInSystemClipboardFxml);
        }
        return controller;
    }

}
