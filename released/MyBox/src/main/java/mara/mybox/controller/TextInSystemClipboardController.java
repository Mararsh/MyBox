package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-3
 * @License Apache License Version 2.0
 */
public class TextInSystemClipboardController extends BaseController {

    protected String separateLine;
    protected Clipboard clipboard;

    @FXML
    protected Button clipboardButton, clearBoardButton;
    @FXML
    protected Label recordLabel;
    @FXML
    protected ComboBox<String> intervalSelector, separateSelector;
    @FXML
    protected TextArea textArea;
    @FXML
    protected CheckBox copyCheck, startCheck, wrapCheck;

    public TextInSystemClipboardController() {
        baseTitle = message("TextInSystemClipboard");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            clipboard = Clipboard.getSystemClipboard();

            intervalSelector.getItems().addAll(Arrays.asList("300", "200", "100", "500", "1000", "1500", "2000"));
            intervalSelector.setValue(TextClipboardTools.getMonitorInterval() + "");
            intervalSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(intervalSelector.getValue());
                        if (v > 0) {
                            intervalSelector.getEditor().setStyle(null);
                            TextClipboardTools.startTextClipboardMonitor(v);
                        } else {
                            intervalSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        intervalSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            copyCheck.setSelected(TextClipboardTools.isCopy());
            copyCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    TextClipboardTools.setCopy(newValue);
                }
            });

            startCheck.setSelected(UserConfig.getBoolean("TextClipboardMonitorStartWhenBoot", false));
            startCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    TextClipboardTools.setStartWhenBoot(newValue);
                }
            });

            editButton.disableProperty().bind(Bindings.isEmpty(textArea.textProperty()));

            separateSelector.getItems().addAll(Arrays.asList(message("NotAccumulate"),
                    message("BlankLine"), message("BlankLine2"),
                    "--------------------", "======================", "*********************", "######################",
                    "%%%%%%%%%%%%%%%%%%%%", "~~~~~~~~~~~~~~~~~~~~~~", "^^^^^^^^^^^^^^^^^^^^^", "......................"));
            String lineSelect = UserConfig.getString(baseName + "SeparateLine", message("NotAccumulate"));
            separateSelector.setValue(lineSelect);
            checkSeparateLine(lineSelect);
            separateSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    checkSeparateLine(newValue);
                    UserConfig.setString(baseName + "SeparateLine", newValue);
                }
            });

            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "Wrap", true));
            wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Wrap", newValue);
                    textArea.setWrapText(newValue);
                }
            });
            textArea.setWrapText(wrapCheck.isSelected());

            if (copyCheck.isSelected()) {
                TextClipboardTools.startTextClipboardMonitor();
            }
            updateStatus();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checkSeparateLine(String select) {
        try {
            if (message("NotAccumulate").equals(select)) {
                separateLine = null;
            } else if (message("BlankLine").equals(select)) {
                separateLine = "\n";
            } else if (message("BlankLine2").equals(select)) {
                separateLine = "\n\n";
            } else {
                separateLine = select + "\n";
            }
        } catch (Exception e) {
            intervalSelector.getEditor().setStyle(UserConfig.badStyle());
        }
    }

    @FXML
    @Override
    public synchronized void startAction() {
        if (TextClipboardTools.isMonitoring()) {
            TextClipboardTools.stopTextClipboardMonitor();
        } else {
            TextClipboardTools.startTextClipboardMonitor();
        }
        updateStatus();
    }

    public synchronized void updateStatus() {
        try {
            if (TextClipboardTools.isMonitoring()) {
                StyleTools.setNameIcon(startButton, message("StopRecording"), "iconStop.png");
                startButton.applyCss();
                recordLabel.setText(message("MonitoringTextInSystemClipboardAndNotice"));
            } else {
                StyleTools.setNameIcon(startButton, message("StartRecording"), "iconStart.png");
                startButton.applyCss();
                recordLabel.setText(message("NotMonitoringTextInSystemClipboard"));
            }
            if (TextClipboardTools.isMonitoringCopy()) {
                NodeStyleTools.setTooltip(copyToSystemClipboardButton, new Tooltip(message("CopyToClipboards") + "\nCTRL+c / ALT+c"));
            } else {
                NodeStyleTools.setTooltip(copyToSystemClipboardButton, new Tooltip(message("CopyToSystemClipboard") + "\nCTRL+c / ALT+c"));
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
        loadClip(clipboard.getString());
    }

    public void loadClip(String clip) {
        if (clip == null || clip.isEmpty()) {
            return;
        }
        if (separateLine == null || message("NotAccumulate").equals(separateLine)) {
            textArea.setText(clip);
        } else {
            textArea.appendText((textArea.getLength() > 0 ? "\n" + separateLine : "") + clip);
        }
        bottomLabel.setText(message("CharactersNumber") + ": " + textArea.getLength());
    }

    @FXML
    @Override
    public void clearAction() {
        textArea.setText(null);
    }

    @FXML
    public void editAction() {
        TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
        controller.loadContents(textArea.getText());
        controller.requestMouse();
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

    /*
        static methods
     */
    public static TextInSystemClipboardController running() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof TextInSystemClipboardController) {
                return (TextInSystemClipboardController) object;
            }
        }
        return null;
    }

    public static TextInSystemClipboardController oneOpen() {
        TextInSystemClipboardController controller = running();
        if (controller == null) {
            controller = (TextInSystemClipboardController) WindowTools.openStage(Fxmls.TextInSystemClipboardFxml);
        }
        controller.requestMouse();
        return controller;
    }

    public static void updateSystemClipboardStatus() {
        Platform.runLater(() -> {
            TextInSystemClipboardController controller = running();
            if (controller != null) {
                controller.updateStatus();
            }
        });
    }

}
