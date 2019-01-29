package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.fxml.FxmlScopeTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.IntRectangle;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2018-12-03
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageSampleController extends ImageViewerController {

    private double scale;
    private int sampleWidth, sampleHeight;
    private boolean isSettingValues;

    @FXML
    private ToolBar opBar;
    @FXML
    private HBox cropBox, sampleBox, showBox;
    @FXML
    private Button saveButton;
    @FXML
    private CheckBox viewCheck;
    @FXML
    private ComboBox<String> widthBox, heightBox;
    @FXML
    private Label sampleLabel, cropLabel;
    @FXML
    private TextField cropLeftXInput, cropLeftYInput, cropRightXInput, cropRightYInput;

    @Override
    protected void initializeNext2() {
        try {
            scrollPane.setDisable(true);
            cropBox.setDisable(true);
            sampleBox.setDisable(true);
            showBox.setDisable(true);
            opBar.setDisable(true);

            cropLeftXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkCropValues();
                }
            });
            cropLeftYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkCropValues();
                }
            });
            cropRightXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkCropValues();
                }
            });
            cropRightYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkCropValues();
                }
            });

            List<String> values = Arrays.asList("1", "2", "3", "4", "5", "6", "8", "9", "10", "15", "20",
                    "25", "30", "50", "80", "100", "200", "500", "800", "1000");
            widthBox.getItems().addAll(values);
            widthBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkSample();
                }
            });
            widthBox.getSelectionModel().select("1");

            heightBox.getItems().addAll(values);
            heightBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkSample();
                }
            });
            heightBox.getSelectionModel().select("1");

            saveButton.disableProperty().bind(
                    widthBox.getEditor().styleProperty().isEqualTo(badStyle)
                            .or(heightBox.getEditor().styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(widthBox.getEditor().textProperty()))
                            .or(Bindings.isEmpty(heightBox.getEditor().textProperty()))
                            .or(cropLeftXInput.styleProperty().isEqualTo(badStyle))
                            .or(cropLeftYInput.styleProperty().isEqualTo(badStyle))
                            .or(cropRightXInput.styleProperty().isEqualTo(badStyle))
                            .or(cropRightYInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(cropLeftXInput.textProperty()))
                            .or(Bindings.isEmpty(cropLeftYInput.textProperty()))
                            .or(Bindings.isEmpty(cropRightXInput.textProperty()))
                            .or(Bindings.isEmpty(cropRightYInput.textProperty()))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkSample() {
        if (image == null) {
            return;
        }
        try {
            int v = Integer.valueOf(widthBox.getSelectionModel().getSelectedItem());
            if (v > 0) {
                sampleWidth = v;
                widthBox.getEditor().setStyle(null);
            } else {
                widthBox.getEditor().setStyle(badStyle);
            }
        } catch (Exception e) {
            widthBox.getEditor().setStyle(badStyle);
        }

        try {
            int v = Integer.valueOf(heightBox.getSelectionModel().getSelectedItem());
            if (v > 0) {
                sampleHeight = v;
                heightBox.getEditor().setStyle(null);
            } else {
                heightBox.getEditor().setStyle(badStyle);
            }
        } catch (Exception e) {
            heightBox.getEditor().setStyle(badStyle);
        }

        if (sampleWidth < 1 || sampleHeight < 1) {
            sampleLabel.setText(getMessage("InvalidParameters"));
        } else {
            sampleLabel.setText(
                    getMessage("ImageSize") + ": "
                    + imageInformation.getWidth() + "x" + imageInformation.getHeight()
                    + "  " + getMessage("SampledSize") + ": "
                    + (int) ((cropRightX - cropLeftX) / sampleWidth)
                    + "x" + (int) ((cropRightY - cropLeftY) / sampleHeight));
        }
    }

    private void checkCropValues() {
        if (isSettingValues) {
            return;
        }
        boolean areaValid = true;
        try {
            cropLeftX = Integer.valueOf(cropLeftXInput.getText());
            if (cropLeftX >= 0 && cropLeftX <= imageInformation.getWidth()) {
                cropLeftXInput.setStyle(null);
            } else {
                cropLeftXInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            cropLeftXInput.setStyle(badStyle);
            areaValid = false;
        }

        try {
            cropLeftY = Integer.valueOf(cropLeftYInput.getText());
            if (cropLeftY >= 0 && cropLeftY <= imageInformation.getHeight()) {
                cropLeftYInput.setStyle(null);
            } else {
                cropLeftYInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            cropLeftYInput.setStyle(badStyle);
            areaValid = false;
        }

        try {
            cropRightX = Integer.valueOf(cropRightXInput.getText());
            if (cropRightX >= 0 && cropRightX <= imageInformation.getWidth()) {
                cropRightXInput.setStyle(null);
            } else {
                cropRightXInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            cropRightXInput.setStyle(badStyle);
            areaValid = false;
        }

        try {
            cropRightY = Integer.valueOf(cropRightYInput.getText());
            if (cropRightY >= 0 && cropRightY <= imageInformation.getHeight()) {
                cropRightYInput.setStyle(null);
            } else {
                cropRightYInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            cropRightYInput.setStyle(badStyle);
            areaValid = false;
        }

        if (cropLeftX >= cropRightX) {
            cropRightXInput.setStyle(badStyle);
            areaValid = false;
        }

        if (cropLeftY >= cropRightY) {
            cropRightYInput.setStyle(badStyle);
            areaValid = false;
        }

        if (areaValid) {
            indicateCropScope();
            checkSample();
        } else {
            popError(getMessage("InvalidRectangle"));
        }

    }

    private void indicateCropScope() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    int lineWidth = 1;
                    if (image.getWidth() >= 200) {
                        lineWidth = (int) image.getWidth() / 200;
                    }
                    final Image newImage = FxmlScopeTools.indicateRectangle(image,
                            Color.RED, lineWidth,
                            new IntRectangle((int) (cropLeftX / scale), (int) (cropLeftY / scale),
                                    (int) (cropRightX / scale), (int) (cropRightY / scale)));
                    if (task.isCancelled()) {
                        return null;
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImage(newImage);
//                            popInformation(AppVaribles.getMessage("CropComments"));
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void afterImageLoaded() {
        try {
            super.afterImageLoaded();
            if (image == null) {
                return;
            }

            scrollPane.setDisable(false);
            cropBox.setDisable(false);
            sampleBox.setDisable(false);
            showBox.setDisable(false);
            opBar.setDisable(false);

            if (imageInformation.isIsSampled()) {
                scale = imageInformation.getWidth() / image.getWidth();
            } else {
                scale = 1;
            }
            isSettingValues = true;
            cropRightXInput.setText(imageInformation.getWidth() * 3 / 4 + "");
            cropRightYInput.setText(imageInformation.getHeight() * 3 / 4 + "");
            cropLeftXInput.setText(imageInformation.getWidth() / 4 + "");
            cropLeftYInput.setText(imageInformation.getHeight() / 4 + "");
            isSettingValues = false;
            checkCropValues();

            fitSize();

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @Override
    protected void handleSampledImage() {
//            logger.debug(availableMem + "  " + pixelsSize + "  " + requiredMem + " " + sampledWidth + " " + sampledSize);
        if (imageInformation.getIndex() > 0) {
            getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath()
                    + " - " + getMessage("Image") + " " + imageInformation.getIndex() + " " + getMessage("Sampled"));
        } else {
            getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath() + " " + getMessage("Sampled"));
        }

        if (bottomLabel != null) {
            if (sizes == null) {
                bottomLabel.setText(getMessage("ImageSampled") + "\n\n"
                        + getMessage("ImagePartComments"));
            } else {
                int sampledSize = (int) (image.getWidth() * image.getHeight() * imageInformation.getColorChannels() / (1014 * 1024));
                String msg = MessageFormat.format(getMessage("ImageTooLarge"),
                        imageInformation.getWidth(), imageInformation.getHeight(), imageInformation.getColorChannels(),
                        sizes.get("pixelsSize"), sizes.get("requiredMem"), sizes.get("availableMem"),
                        (int) image.getWidth(), (int) image.getHeight(), sampledSize) + "\n\n"
                        + getMessage("ImagePartComments");
                bottomLabel.setText(msg);
            }
        }
    }

    @FXML
    @Override
    public void clickImage(MouseEvent event) {
        if (image == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        imageView.setCursor(Cursor.HAND);

        int x = (int) Math.round(event.getX() * image.getWidth() * scale / imageView.getBoundsInLocal().getWidth());
        int y = (int) Math.round(event.getY() * image.getHeight() * scale / imageView.getBoundsInLocal().getHeight());

        if (event.getButton() == MouseButton.SECONDARY) {

            isSettingValues = true;
            cropRightXInput.setText(x + "");
            cropRightYInput.setText(y + "");
            isSettingValues = false;

        } else if (event.getButton() == MouseButton.PRIMARY) {

            isSettingValues = true;
            cropLeftXInput.setText(x + "");
            cropLeftYInput.setText(y + "");
            isSettingValues = false;

        }

        checkCropValues();
        bottomLabel.setText("");

    }

    @FXML
    private void allAction(ActionEvent event) {
        isSettingValues = true;
        cropLeftXInput.setText("0");
        cropLeftYInput.setText("0");
        cropRightXInput.setText(imageInformation.getWidth() + "");
        cropRightYInput.setText(imageInformation.getHeight() + "");
        isSettingValues = false;
        checkCropValues();
    }

    @FXML
    public void saveAction(ActionEvent event) {
        if (image == null || sampleWidth < 1 || sampleHeight < 1) {
            return;
        }
        final FileChooser fileChooser = new FileChooser();
        File path = new File(AppVaribles.getUserConfigPath(targetPathKey, CommonValues.UserFilePath));
        fileChooser.setInitialDirectory(path);
        fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
        final File file = fileChooser.showSaveDialog(getMyStage());
        if (file == null) {
            return;
        }
        AppVaribles.setUserConfigValue(targetPathKey, file.getParent());

        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                final String filename = file.getAbsolutePath();
                String format = FileTools.getFileSuffix(filename);
                BufferedImage bufferedImage;
                bufferedImage = ImageFileReaders.readFileBySample(imageInformation.getImageFormat(),
                        sourceFile.getAbsolutePath(),
                        cropLeftX, cropLeftY, cropRightX, cropRightY,
                        sampleWidth, sampleHeight);
                if (task.isCancelled()) {
                    return null;
                }
                ok = ImageFileWriters.writeImageFile(bufferedImage, format, filename);
                if (task.isCancelled()) {
                    return null;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok && file.exists()) {
                            popInformation(AppVaribles.getMessage("Successful"));
                            if (viewCheck.isSelected()) {
                                try {
                                    final ImageViewerController controller
                                            = (ImageViewerController) openStage(CommonValues.ImageViewerFxml, false, true);
                                    controller.setBaseTitle(AppVaribles.getMessage("ImageViewer"));
                                    controller.loadImage(filename);
                                } catch (Exception e) {
                                    logger.error(e.toString());
                                }
                            }
                        } else {
                            popError(AppVaribles.getMessage("Failed"));
                        }
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
