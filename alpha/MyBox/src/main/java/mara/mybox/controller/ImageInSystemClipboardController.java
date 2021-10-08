package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ImageClipboardMonitor;
import mara.mybox.fxml.ImageClipboardTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.StyleTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import static mara.mybox.value.AppVariables.imageClipboardMonitor;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-1-22
 * @License Apache License Version 2.0
 */
public class ImageInSystemClipboardController extends ImageViewerController {

    private int scaledWidth;
    private String filePrefix;
    private Clipboard clipboard;

    @FXML
    protected Button openPathButton, clearBoardButton;
    @FXML
    protected CheckBox saveCheck, copyCheck;
    @FXML
    protected Label recordLabel, numberLabel;
    @FXML
    protected ComboBox<String> intervalSelector, widthSelector;

    public ImageInSystemClipboardController() {
        baseTitle = message("ImagesInSystemClipboard");
        TipsLabelKey = "RecordImagesTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            clipboard = Clipboard.getSystemClipboard();

            saveCheck.setSelected(ImageClipboardTools.isSave());
            saveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    ImageClipboardTools.setSave(newValue);
                }
            });

            copyCheck.setSelected(ImageClipboardTools.isCopy());
            copyCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    ImageClipboardTools.setCopy(newValue);
                }
            });

            List<String> values = Arrays.asList(message("OriginalSize"),
                    "512", "1024", "256", "128", "2048", "100", "80", "4096");
            widthSelector.getItems().addAll(values);
            int v = ImageClipboardTools.getWidth();
            if (v <= 0) {
                scaledWidth = -1;
                widthSelector.getSelectionModel().select(0);
            } else {
                scaledWidth = v;
                widthSelector.setValue(v + "");
            }
            widthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (message("OriginalSize").equals(newValue)) {
                        scaledWidth = -1;
                    } else {
                        try {
                            scaledWidth = Integer.valueOf(newValue);
                            ValidationTools.setEditorNormal(widthSelector);
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(widthSelector);
                            return;
                        }
                    }
                    ImageClipboardTools.setWidth(scaledWidth);
                }
            });

            intervalSelector.getItems().addAll(Arrays.asList("1000", "500", "800", "1500", "2000"));
            intervalSelector.setValue(ImageClipboardTools.getMonitorInterval() + "");
            intervalSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(intervalSelector.getValue());
                        if (v > 0) {
                            intervalSelector.getEditor().setStyle(null);
                            ImageClipboardTools.setMonitorInterval(v);
                            startMonitor();
                        } else {
                            intervalSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                        }
                    } catch (Exception e) {
                        intervalSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                    }
                }
            });

            openPathButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        updateStatus();
        refreshAction();
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(clearButton, new Tooltip(message("DeleteSysTemporaryPathFiles")));
            NodeStyleTools.setTooltip(clearBoardButton, new Tooltip(message("ClearSystemClipboard")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    protected void openTargetPath(ActionEvent event) {
        view(new File(targetPathInput.getText()));
    }

    @FXML
    public void clearTmp() {
        WindowTools.openStage(Fxmls.FilesDeleteSysTempFxml);
    }

    public void startMonitor() {
        try {
            targetPath = new File(targetPathInput.getText());
            if (targetPath != null && targetPath.exists()) {
                if (targetPrefixInput.getText().trim().isEmpty()) {
                    filePrefix = targetPath.getAbsolutePath() + File.separator;
                } else {
                    filePrefix = targetPath.getAbsolutePath() + File.separator
                            + targetPrefixInput.getText().trim() + "-";
                }
            } else {
                filePrefix = null;
            }
            if (imageClipboardMonitor != null) {
                imageClipboardMonitor.cancel();
                imageClipboardMonitor = null;
            }
            imageClipboardMonitor = new ImageClipboardMonitor()
                    .start(ImageClipboardTools.getMonitorInterval(), formatController.getAttributes(), filePrefix);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public synchronized void startAction() {
        try {
            if (ImageClipboardTools.isMonitoring()) {
                ImageClipboardTools.stopImageClipboardMonitor();
            } else {
                startMonitor();
            }
            updateStatus();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public synchronized void updateStatus() {
        try {
            if (ImageClipboardTools.isMonitoring()) {
                StyleTools.setNameIcon(startButton, message("StopRecording"), "iconStop.png");
                startButton.applyCss();
                recordLabel.setText(message("MonitoringImageInSystemClipboardAndNotice"));
            } else {
                StyleTools.setNameIcon(startButton, message("StartRecording"), "iconStart.png");
                startButton.applyCss();
                recordLabel.setText(message("NotMonitoringImageInSystemClipboard"));
            }
            if (ImageClipboardTools.isMonitoringCopy()) {
                NodeStyleTools.setTooltip(copyToSystemClipboardButton, new Tooltip(message("CopyToClipboards") + "\nCTRL+c / ALT+c"));
            } else {
                NodeStyleTools.setTooltip(copyToSystemClipboardButton, new Tooltip(message("CopyToSystemClipboard") + "\nCTRL+c / ALT+c"));
            }
            if (ImageClipboardTools.isSave() && filePrefix == null) {
                popError(message("ImageNotSaveDueInvalidPath"));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void refreshAction() {
        if (!clipboard.hasImage()) {
            popError(message("NoImageInClipboard"));
            return;
        }
        loadClip(clipboard.getImage(), -1);
    }

    public synchronized void loadClip(Image clip, int number) {
        if (number > 0) {
            numberLabel.setText(MessageFormat.format(message("RecordingImages"), number));
        } else {
            numberLabel.setText("");
        }
        if (clip == null) {
            return;
        }
        loadImage(clip);
    }

    @FXML
    @Override
    public void clearAction() {
        clipboard.clear();
        loadImage(null);
    }

    /*
        static methods
     */
    public static ImageInSystemClipboardController running() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof ImageInSystemClipboardController) {
                return (ImageInSystemClipboardController) object;
            }
        }
        return null;
    }

    public static ImageInSystemClipboardController oneOpen() {
        ImageInSystemClipboardController controller = running();
        if (controller == null) {
            controller = (ImageInSystemClipboardController) WindowTools.openStage(Fxmls.ImageInSystemClipboardFxml);
        } else {
            controller.toFront();
        }
        return controller;
    }

    public static void updateSystemClipboardStatus() {
        Platform.runLater(() -> {
            ImageInSystemClipboardController controller = running();
            if (controller != null) {
                controller.updateStatus();
            }
        });
    }

}
