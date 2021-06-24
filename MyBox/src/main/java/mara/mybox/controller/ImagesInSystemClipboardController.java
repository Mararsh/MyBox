package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.stage.Stage;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.db.table.TableImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageConvert;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.IntTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-1-22
 * @License Apache License Version 2.0
 */
public class ImagesInSystemClipboardController extends ImageViewerController {

    private int recordedNumber, checkInterval, scaledWidth;
    private String filePrefix;
    private Image lastImage;
    private boolean handling;
    private Connection conn;
    private Clipboard clipboard;
    private TableImageClipboard tableImageClipboard;

    @FXML
    protected Button openPathButton, clipboardButton, clearBoardButton;
    @FXML
    protected CheckBox saveCheck, clipboardCheck;
    @FXML
    protected Label recordLabel;
    @FXML
    protected ComboBox<String> intervalSelector, widthSelector;

    public ImagesInSystemClipboardController() {
        baseTitle = AppVariables.message("ImagesInSystemClipboard");
        TipsLabelKey = "RecordImagesTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tableImageClipboard = new TableImageClipboard();
            clipboard = Clipboard.getSystemClipboard();

            saveCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Save", false));
            saveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "Save", newValue);
                }
            });

            clipboardCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Clipboard", false));
            clipboardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "Clipboard", newValue);
                }
            });

            List<String> values = Arrays.asList(message("OriginalSize"),
                    "512", "1024", "256", "128", "2048", "100", "80", "4096");
            widthSelector.getItems().addAll(values);
            int v = AppVariables.getUserConfigInt(baseName + "ScaledWidth", -1);
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
                            FxmlControl.setEditorNormal(widthSelector);
                        } catch (Exception e) {
                            FxmlControl.setEditorBadStyle(widthSelector);
                            return;
                        }
                    }
                    AppVariables.setUserConfigInt(baseName + "ScaledWidth", scaledWidth);
                }
            });

            intervalSelector.getItems().addAll(Arrays.asList("1000", "500", "800", "1500", "2000"));
            checkInterval = AppVariables.getUserConfigInt(baseName + "Interval", 1000);
            if (checkInterval <= 0) {
                checkInterval = 1000;
            }
            intervalSelector.setValue(checkInterval + "");
            intervalSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(intervalSelector.getValue());
                        if (v > 0) {
                            checkInterval = v;
                            intervalSelector.getEditor().setStyle(null);
                            AppVariables.setUserConfigInt(baseName + "Interval", v);
                        } else {
                            intervalSelector.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        intervalSelector.getEditor().setStyle(badStyle);
                    }
                }
            });

            openPathButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        FxmlControl.setTooltip(clearButton, new Tooltip(message("DeleteSysTemporaryPathFiles")));
        FxmlControl.setTooltip(clipboardButton, new Tooltip(message("ImagesInMyBoxClipboard")));
        FxmlControl.setTooltip(clearBoardButton, new Tooltip(message("ClearSystemClipboard")));

        refreshAction();
    }

    @FXML
    protected void openTargetPath(ActionEvent event) {
        view(new File(targetPathInput.getText()));
    }

    @FXML
    public void clearTmp() {
        FxmlStage.openStage(CommonValues.FilesDeleteSysTempFxml);
    }

    @FXML
    protected void myBoxClipBoard() {
        ImagesInMyBoxClipboardController.oneOpen();
    }

    @FXML
    @Override
    public void startAction() {
        try {
            handling = false;
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
                conn = null;
            }
            if (startButton.getUserData() == null) {
                ControlStyle.setNameIcon(startButton, message("StopRecording"), "iconStop.png");
                startButton.setUserData("started");
                startButton.applyCss();
                getMyStage().setIconified(true);
                recordedNumber = 0;
                recordLabel.setText(MessageFormat.format(AppVariables.message("RecordingImages"), 0));
                if (targetPath != null) {
                    if (targetPrefixInput.getText().trim().isEmpty()) {
                        filePrefix = targetPath.getAbsolutePath() + File.separator;
                    } else {
                        filePrefix = targetPath.getAbsolutePath() + File.separator
                                + targetPrefixInput.getText().trim() + "-";
                    }
                }
                attributes = formatController.attributes;
                String imageFormat = attributes.getImageFormat();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (handling || !clipboard.hasImage()) {
                                    return;
                                }
                                handling = true;
                                Image clip = clipboard.getImage();
                                if (clip == null) {
                                    handling = false;
                                    return;
                                }
                                if (FxmlImageManufacture.sameImage(lastImage, clip)) {
                                    handling = false;
                                    return;
                                }
                                lastImage = clip;
                                recordedNumber++;
                                recordLabel.setText(MessageFormat.format(AppVariables.message("RecordingImages"), recordedNumber));

                                loadImage(clip);

                                if (saveCheck.isSelected()) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(clip, null);
                                            if (scaledWidth > 0) {
                                                bufferedImage = ImageManufacture.scaleImageWidthKeep(bufferedImage, scaledWidth);
                                            }
                                            File file = new File(filePrefix + DateTools.nowString3() + "-"
                                                    + IntTools.getRandomInt(1000) + "." + imageFormat);
                                            while (file.exists()) {
                                                file = new File(filePrefix + DateTools.nowString3() + "-"
                                                        + IntTools.getRandomInt(1000) + "." + imageFormat);
                                            }
                                            BufferedImage converted = ImageConvert.convertColorSpace(bufferedImage, attributes);
                                            ImageFileWriters.writeImageFile(converted, attributes, file.getAbsolutePath());
                                        }
                                    }.start();
                                }
                                if (clipboardCheck.isSelected()) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(clip, null);
                                                if (scaledWidth > 0) {
                                                    bufferedImage = ImageManufacture.scaleImageWidthKeep(bufferedImage, scaledWidth);
                                                }
                                                ImageClipboard data = ImageClipboard.create(bufferedImage, ImageClipboard.ImageSource.SystemClipBoard);
                                                if (conn == null || conn.isClosed()) {
                                                    conn = DerbyBase.getConnection();
                                                }
                                                tableImageClipboard.insertData(conn, data);
                                                ControlImagesClipboard.updateClipboards();
                                            } catch (Exception e) {
                                                MyBoxLog.debug(e.toString());
                                            }
                                        }
                                    }.start();
                                }
                                handling = false;
                            }
                        });

                    }
                }, 0, checkInterval);
            } else {
                ControlStyle.setNameIcon(startButton, message("StartRecording"), "iconStart.png");
                startButton.setUserData(null);
                startButton.applyCss();
                recordLabel.setText("");
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
        lastImage = clipboard.getImage();
        loadImage(lastImage);
    }

    @FXML
    @Override
    public void clearAction() {
        clipboard.clear();
        lastImage = null;
        loadImage(null);
    }

    /*
        static methods
     */
    public static ImagesInSystemClipboardController oneOpen() {
        ImagesInSystemClipboardController controller = null;
        Stage stage = FxmlStage.findStage(message("ImagesInSystemClipboard"));
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (ImagesInSystemClipboardController) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (ImagesInSystemClipboardController) FxmlStage.openStage(CommonValues.ImagesInSystemClipboardFxml);
        }
        if (controller != null) {
            controller.toFront();
        }
        return controller;
    }

}
