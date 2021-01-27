package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageAttributes;
import mara.mybox.image.ImageBinary;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.IntTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-1-22
 * @Description
 * @License Apache License Version 2.0
 */
public class RecordImagesInSystemClipboardController extends BaseController {

    private int recordedNumber, checkInterval;
    private ImageType imageType;
    private int jpegQuality, threshold;
    private String filePrefix;
    private RecordType recordType;
    private Image lastImage;
    private boolean isHandling;

    private enum RecordType {
        Save, View, SaveAndView
    }

    private enum ImageType {
        PNG, JPG, TIFF
    }

    @FXML
    protected Button openTargetButton, functionsButton;
    @FXML
    protected TitledPane targetPane;
    @FXML
    protected RadioButton saveRadio, viewRadio, saveAndViewRadio;
    @FXML
    protected Label recordLabel;
    @FXML
    protected ComboBox<String> jpegBox;
    @FXML
    protected ToggleGroup recordTypeGroup, imageTypeGroup;
    @FXML
    protected TextField thresholdInput;
    @FXML
    protected HBox typeBox;

    public RecordImagesInSystemClipboardController() {
        baseTitle = AppVariables.message("RecordImagesInSystemClipBoard");

        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Image);
        TipsLabelKey = "RecordImagesTips";

        sourceExtensionFilter = CommonFxValues.ImageExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            checkInterval = 1000;

            recordTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    RadioButton selected = (RadioButton) recordTypeGroup.getSelectedToggle();
                    AppVariables.setUserConfigValue("RecordSystemClipboardType", selected.getText());
                    checkRecordType();
                }
            });
            String savedType = AppVariables.getUserConfigValue("RecordSystemClipboardType", message("Save"));
            if (message("View").equals(savedType)) {
                viewRadio.fire();
            } else if (message("Save").equals(savedType)) {
                saveRadio.fire();
            } else if (message("SaveAndView").equals(savedType)) {
                saveAndViewRadio.fire();
            }
            checkRecordType();

            imageTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkFormat();
                }
            });
            checkFormat();

            jpegBox.getItems().addAll(Arrays.asList("100", "75", "90", "50", "60", "80", "30", "10"));
            jpegBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    checkJpegQuality();
                }
            });
            jpegBox.getSelectionModel().select(0);
            checkJpegQuality();

            thresholdInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkThreshold();
                }
            });
            checkThreshold();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    @Override
    public void checkTargetPathInput() {
        checkRecordType();
    }

    private void checkRecordType() {
        targetPathInput.setStyle(null);
        startButton.setDisable(false);
        RadioButton selected = (RadioButton) recordTypeGroup.getSelectedToggle();
        if (AppVariables.message("View").equals(selected.getText())) {
            recordType = RecordType.View;
            targetPane.setDisable(true);
            openTargetButton.setDisable(true);

        } else {

            if (AppVariables.message("Save").equals(selected.getText())) {
                recordType = RecordType.Save;
            } else if (AppVariables.message("SaveAndView").equals(selected.getText())) {
                recordType = RecordType.SaveAndView;
            }

            targetPane.setDisable(false);
            File file = new File(targetPathInput.getText());
            if (!file.exists() || !file.isDirectory()) {
                targetPathInput.setStyle(badStyle);
                openTargetButton.setDisable(true);
                startButton.setDisable(true);
            } else {
                AppVariables.setUserConfigValue(targetPathKey, file.getPath());
                targetPath = file;
                openTargetButton.setDisable(false);
            }
        }
    }

    private void checkFormat() {
        jpegBox.setDisable(true);
        jpegBox.setStyle(null);
        thresholdInput.setDisable(true);

        RadioButton selected = (RadioButton) imageTypeGroup.getSelectedToggle();
        if (AppVariables.message("PNG").equals(selected.getText())) {
            imageType = ImageType.PNG;
        } else if (AppVariables.message("CCITT4").equals(selected.getText())) {
            imageType = ImageType.TIFF;
            thresholdInput.setDisable(false);
        } else if (AppVariables.message("JpegQuailty").equals(selected.getText())) {
            imageType = ImageType.JPG;
            jpegBox.setDisable(false);
            checkJpegQuality();
        }
    }

    private void checkJpegQuality() {
        jpegQuality = 100;
        try {
            jpegQuality = Integer.valueOf(jpegBox.getSelectionModel().getSelectedItem());
            if (jpegQuality >= 0 && jpegQuality <= 100) {
                jpegBox.setStyle(null);
            } else {
                jpegBox.setStyle(badStyle);
            }
        } catch (Exception e) {
            jpegBox.setStyle(badStyle);
        }
    }

    private void checkThreshold() {
        try {
            if (thresholdInput.getText().isEmpty()) {
                threshold = -1;
                thresholdInput.setStyle(null);
                return;
            }
            threshold = Integer.valueOf(thresholdInput.getText());
            if (threshold >= 0 && threshold <= 255) {
                thresholdInput.setStyle(null);
            } else {
                threshold = -1;
                thresholdInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            threshold = -1;
            thresholdInput.setStyle(badStyle);
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        FxmlControl.setTooltip(functionsButton, new Tooltip(message("DeleteSysTemporaryPathFiles")));
        startButton.requestFocus();
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
    @Override
    public void startAction() {
        try {
            isHandling = false;
            if (startButton.getUserData() == null) {
                targetPane.setDisable(true);
                typeBox.setDisable(true);
                ControlStyle.setNameIcon(startButton, message("StopRecording"), "iconStop.png");
                startButton.setUserData("started");
                startButton.applyCss();
                getMyStage().setIconified(true);
                recordedNumber = 0;
                recordLabel.setText(MessageFormat.format(AppVariables.message("RecordingImages"), 0));
                final boolean saveImages
                        = (recordType == RecordType.Save || recordType == RecordType.SaveAndView);
                final boolean viewImages
                        = (recordType == RecordType.View || recordType == RecordType.SaveAndView);
                if (targetPath != null) {
                    if (targetPrefixInput.getText().trim().isEmpty()) {
                        filePrefix = targetPath.getAbsolutePath() + File.separator;
                    } else {
                        filePrefix = targetPath.getAbsolutePath() + File.separator
                                + targetPrefixInput.getText().trim() + "-";
                    }
                }
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (isHandling) {
                            return;
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                final Image image = SystemTools.fetchImageInClipboard(false);
                                if (image == null) {
                                    return;
                                }
                                isHandling = true;
                                if (lastImage != null && FxmlImageManufacture.sameImage(lastImage, image)) {
                                    isHandling = false;
                                    return;
                                }
                                lastImage = image;
                                isHandling = false;
                                if (saveImages) {
                                    saveImage(image);
                                }
                                recordedNumber++;
                                recordLabel.setText(MessageFormat.format(AppVariables.message("RecordingImages"), recordedNumber));
                                if (viewImages) {
                                    ImageViewerController controller = FxmlStage.openImageViewer(image);
                                    controller.getMyStage().setMaximized(true);
                                }
                            }
                        });

                    }
                }, 0, checkInterval);
            } else {
                targetPane.setDisable(false);
                typeBox.setDisable(false);
                ControlStyle.setNameIcon(startButton, message("StartRecording"), "iconStart.png");
                startButton.setUserData(null);
                startButton.applyCss();
                recordLabel.setText("");
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    private void saveImage(Image image) {
        BufferedImage bufferedImage = FxmlImageManufacture.bufferedImage(image);
        ImageAttributes attributes = new ImageAttributes();
        switch (imageType) {
            case TIFF:
                ImageBinary imageBinary = new ImageBinary(bufferedImage, threshold);
                bufferedImage = imageBinary.operate();
                attributes.setImageFormat("tif");
                attributes.setCompressionType("CCITT T.6");
                break;
            case JPG:
                bufferedImage = ImageManufacture.removeAlpha(bufferedImage);
                attributes.setImageFormat("jpg");
                attributes.setCompressionType("JPEG");
                attributes.setQuality(jpegQuality);
                break;
            case PNG:
            default:
                attributes.setImageFormat("png");
                break;

        }
        File file = new File(filePrefix + DateTools.nowString3() + "-"
                + IntTools.getRandomInt(1000) + "." + attributes.getImageFormat());
        while (file.exists()) {
            file = new File(filePrefix + DateTools.nowString3() + "-"
                    + IntTools.getRandomInt(1000) + "." + attributes.getImageFormat());
        }
        ImageFileWriters.writeImageFile(bufferedImage, attributes, file.getAbsolutePath());
    }

}
