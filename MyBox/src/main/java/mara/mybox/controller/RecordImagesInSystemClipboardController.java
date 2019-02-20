package mara.mybox.controller;

import mara.mybox.fxml.FxmlStage;
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
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import mara.mybox.fxml.image.ImageTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.image.ImageBinary;
import mara.mybox.image.ImageConvert;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.data.ImageAttributes;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.tools.ValueTools;

/**
 * @Author Mara
 * @CreateDate 2019-1-22
 * @Description
 * @License Apache License Version 2.0
 */
public class RecordImagesInSystemClipboardController extends BaseController {

    private int recordedNumber;
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
    private Button openButton;
    @FXML
    protected TitledPane targetPane, optionsPane;
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

    public RecordImagesInSystemClipboardController() {
        targetPathKey = "SnapshotsTargetPath";
        TipsLabelKey = "RecordImagesTips";

        fileExtensionFilter = CommonValues.ImageExtensionFilter;
    }

    @Override
    protected void initializeNext() {
        try {
            recordTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkRecordType();
                }
            });
            checkRecordType();

            targetPathInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkRecordType();
                }
            });
            targetPathInput.setText(AppVaribles.getUserConfigPath(targetPathKey).getAbsolutePath());

            imageTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkFormat();
                }
            });
            checkFormat();

            jpegBox.getItems().addAll(Arrays.asList("100", "75", "90", "50", "60", "80", "30", "10"));
            jpegBox.valueProperty().addListener(new ChangeListener<String>() {
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
            logger.debug(e.toString());
        }

    }

    private void checkRecordType() {
        targetPathInput.setStyle(null);
        startButton.setDisable(false);
        RadioButton selected = (RadioButton) recordTypeGroup.getSelectedToggle();
        if (AppVaribles.getMessage("View").equals(selected.getText())) {
            recordType = RecordType.View;
            targetPane.setDisable(true);
            openButton.setDisable(true);

        } else {

            if (AppVaribles.getMessage("Save").equals(selected.getText())) {
                recordType = RecordType.Save;
            } else if (AppVaribles.getMessage("SaveAndView").equals(selected.getText())) {
                recordType = RecordType.SaveAndView;
            }

            targetPane.setDisable(false);
            File file = new File(targetPathInput.getText());
            if (!file.exists() || !file.isDirectory()) {
                targetPathInput.setStyle(badStyle);
                openButton.setDisable(true);
                startButton.setDisable(true);
            } else {
                AppVaribles.setUserConfigValue(targetPathKey, file.getPath());
                targetPath = file;
                openButton.setDisable(false);
            }
        }
    }

    private void checkFormat() {
        jpegBox.setDisable(true);
        jpegBox.setStyle(null);
        thresholdInput.setDisable(true);

        RadioButton selected = (RadioButton) imageTypeGroup.getSelectedToggle();
        if (AppVaribles.getMessage("PNG").equals(selected.getText())) {
            imageType = ImageType.PNG;
        } else if (AppVaribles.getMessage("CCITT4").equals(selected.getText())) {
            imageType = ImageType.TIFF;
            thresholdInput.setDisable(false);
        } else if (AppVaribles.getMessage("JpegQuailty").equals(selected.getText())) {
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

    @FXML
    @Override
    protected void selectTargetPath(ActionEvent event) {
        if (targetPathInput == null) {
            return;
        }
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = AppVaribles.getUserConfigPath(targetPathKey);
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            targetPathInput.setText(directory.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void openTargetPath(ActionEvent event) {
        FxmlStage.openTarget(getClass(), null, new File(targetPathInput.getText()).getAbsolutePath());
    }

    @FXML
    @Override
    public void startAction() {
        try {
            isHandling = false;
            if (AppVaribles.getMessage("StartRecording").equals(startButton.getText())) {
                targetPane.setDisable(true);
                optionsPane.setDisable(true);
                startButton.setText(AppVaribles.getMessage("StopRecording"));
                getMyStage().setIconified(true);
                recordedNumber = 0;
                recordLabel.setText(MessageFormat.format(AppVaribles.getMessage("RecordingImages"), 0));
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
                                if (lastImage != null && ImageTools.isImageSame(lastImage, image)) {
                                    isHandling = false;
                                    return;
                                }
                                lastImage = image;
                                isHandling = false;
                                if (saveImages) {
                                    saveImage(image);
                                }
                                recordedNumber++;
                                recordLabel.setText(MessageFormat.format(AppVaribles.getMessage("RecordingImages"), recordedNumber));
                                if (viewImages) {
                                    ImageViewerController controller = FxmlStage.openImageViewer(getClass(), null);
                                    controller.loadImage(image);
                                    controller.getMyStage().setMaximized(true);
                                }
                            }
                        });

                    }
                }, 0, 2000);
            } else {
                targetPane.setDisable(false);
                optionsPane.setDisable(false);
                startButton.setText(AppVaribles.getMessage("StartRecording"));
                recordLabel.setText("");
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void saveImage(Image image) {
        BufferedImage bufferedImage = ImageTools.getBufferedImage(image);
        ImageAttributes attributes = new ImageAttributes();
        switch (imageType) {
            case TIFF:
                ImageBinary imageBinary = new ImageBinary(bufferedImage, threshold);
                bufferedImage = imageBinary.operate();
                attributes.setImageFormat("tif");
                attributes.setCompressionType("CCITT T.6");
                break;
            case JPG:
                bufferedImage = ImageConvert.clearAlpha(bufferedImage);
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
                + ValueTools.getRandomInt(1000) + "." + attributes.getImageFormat());
        while (file.exists()) {
            file = new File(filePrefix + DateTools.nowString3() + "-"
                    + ValueTools.getRandomInt(1000) + "." + attributes.getImageFormat());
        }
        ImageFileWriters.writeImageFile(bufferedImage, attributes, file.getAbsolutePath());
    }

}
