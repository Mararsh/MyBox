package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.AppVariables.imageClipboardMonitor;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-1-22
 * @License Apache License Version 2.0
 */
public class ImageInSystemClipboardController extends ImageViewerController {

    private int scaledWidth;
    private String filePrefix;

    @FXML
    protected ControlPathInput targetPathInputController;
    @FXML
    protected Button openPathButton, clearBoardButton;
    @FXML
    protected CheckBox saveCheck, copyCheck;
    @FXML
    protected Label recordLabel, numberLabel, filesLabel;
    @FXML
    protected ComboBox<String> intervalSelector, widthSelector;
    @FXML
    protected ControlImageFormat formatController;

    public ImageInSystemClipboardController() {
        baseTitle = message("ImagesInSystemClipboard");
        TipsLabelKey = "RecordImagesTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            saveCheck.setSelected(ImageClipboardTools.isSave());
            saveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    checkTargetPath();
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
                            scaledWidth = Integer.parseInt(newValue);
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
                        int v = Integer.parseInt(intervalSelector.getValue());
                        if (v > 0) {
                            intervalSelector.getEditor().setStyle(null);
                            ImageClipboardTools.setMonitorInterval(v);
                            startMonitor();
                        } else {
                            intervalSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        intervalSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            targetPathInputController.baseName(baseName).init();

            targetPathInputController.notify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    checkTargetPath();
                }
            });

            targetPrefixInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    checkTargetPath();
                }
            });

            formatController.setParameters(this, false);

            formatController.notify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    if (imageClipboardMonitor != null) {
                        imageClipboardMonitor.setAttributes(formatController.getAttributes());
                    }
                }
            });

            openPathButton.disableProperty().bind(targetPathInputController.valid.not());

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }

    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        updateStatus();
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(clearButton, new Tooltip(message("DeleteJavaIOTemporaryPathFiles")));
            NodeStyleTools.setTooltip(clearBoardButton, new Tooltip(message("ClearSystemClipboard")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void openPath() {
        view(targetPathInputController.file);
    }

    @FXML
    public void clearTmp() {
        WindowTools.openStage(Fxmls.FilesDeleteJavaTempFxml);
    }

    public void startMonitor() {
        try {
            if (imageClipboardMonitor != null) {
                imageClipboardMonitor.cancel();
                imageClipboardMonitor = null;
            }
            checkTargetPath();
            imageClipboardMonitor = new ImageClipboardMonitor()
                    .start(ImageClipboardTools.getMonitorInterval(), formatController.getAttributes(), filePrefix);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void checkTargetPath() {
        try {
            targetPath = targetPathInputController.file;
            filesLabel.setText("");
            if (targetPath != null && targetPath.exists()) {
                if (targetPrefixInput.getText().trim().isEmpty()) {
                    filePrefix = targetPath.getAbsolutePath() + File.separator;
                } else {
                    filePrefix = targetPath.getAbsolutePath() + File.separator
                            + targetPrefixInput.getText().trim() + "-";
                }
                if (imageClipboardMonitor != null) {
                    filesLabel.setText(message("FilesSaved") + ": " + imageClipboardMonitor.getSavedNumber());
                }
            } else {
                filePrefix = null;
                if (ImageClipboardTools.isSave()) {
                    filesLabel.setText(message("ImageNotSaveDueInvalidPath"));
                }
            }
            if (imageClipboardMonitor != null) {
                imageClipboardMonitor.setFilePrefix(filePrefix);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
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
            MyBoxLog.debug(e);
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
            checkTargetPath();
            updateNumbers();
            refreshAction();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void updateNumbers() {
        if (imageClipboardMonitor != null) {
            numberLabel.setText(message("Read") + ": " + imageClipboardMonitor.getRecordNumber() + "   "
                    + message("Saved") + ": " + imageClipboardMonitor.getSavedNumber() + "   "
                    + message("Copied") + ": " + imageClipboardMonitor.getCopiedNumber());
        } else {
            numberLabel.setText("");
        }
        filesLabel.setText("");
    }

    @FXML
    @Override
    public void refreshAction() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (!clipboard.hasImage()) {
            popError(message("NoImageInClipboard"));
            return;
        }
        loadClip(clipboard.getImage());
    }

    public void loadClip(Image clip) {
        updateNumbers();
        if (clip == null) {
            return;
        }
        loadImage(clip);
    }

    public void filesInfo(String info) {
        filesLabel.setText(info);
    }

    @FXML
    @Override
    public void clearAction() {
        Clipboard.getSystemClipboard().clear();
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
        }
        controller.requestMouse();
        controller.updateStatus();
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
