/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javax.imageio.ImageIO;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.image.ImageConverter;
import mara.mybox.image.ImageGrayTools;
import mara.mybox.image.ImageTools;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.ImageAttributes;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import mara.mybox.imagefile.ImageFileWriters;
import org.apache.pdfbox.rendering.ImageType;

/**
 * @Author Mara
 * @CreateDate 2018-6-26
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageConverterController extends ImageBaseController {

    private ImageAttributes attributes;
    private String targetFile;

    @FXML
    protected Pane filesTable;
    @FXML
    protected FilesTableController filesTableController;
    @FXML
    protected CheckBox appendSize, appendColor, appendCompressionType, appendQuality;
    @FXML
    protected Button openTargetButton, startButton, viewButton;
    @FXML
    protected Pane imageConverterAttributes;
    @FXML
    protected ImageConverterAttributesController imageConverterAttributesController;
    @FXML
    protected VBox paraBox;

    @Override
    protected void initializeNext2() {
        try {
            imageConverterAttributesController.setParentFxml(myFxml);
            appendSize.setSelected(AppVaribles.getConfigBoolean("ic_aDensity"));
            appendColor.setSelected(AppVaribles.getConfigBoolean("ic_aColor"));
            appendCompressionType.setSelected(AppVaribles.getConfigBoolean("ic_aCompression"));
            appendQuality.setSelected(AppVaribles.getConfigBoolean("ic_aQuality"));

            if (targetPathInput != null) {
                targetPathInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        try {
                            final File file = new File(newValue);
                            if (file.isDirectory()) {
                                AppVaribles.setConfigValue("imageTargetPath", file.getPath());
                            } else {
                                AppVaribles.setConfigValue("imageTargetPath", file.getParent());
                            }
                            if (openTargetButton != null) {
                                if (!file.exists()) {
                                    openTargetButton.setDisable(true);
                                } else {
                                    openTargetButton.setDisable(false);
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                });
                FxmlTools.setFileValidation(targetPathInput);
            }

            startButton.disableProperty().bind(
                    Bindings.isEmpty(sourceFileInput.textProperty())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(sourceFileInput.styleProperty().isEqualTo(badStyle))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(imageConverterAttributesController.getxInput().styleProperty().isEqualTo(badStyle))
                            .or(imageConverterAttributesController.getyInput().styleProperty().isEqualTo(badStyle))
                            .or(imageConverterAttributesController.getQualityBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageConverterAttributesController.getQualityInput().styleProperty().isEqualTo(badStyle)))
                            .or(imageConverterAttributesController.getColorBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageConverterAttributesController.getThresholdInput().styleProperty().isEqualTo(badStyle)))
            );

            viewButton.setDisable(true);
            imageConverterAttributesController.getOriginalButton().setDisable(true);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void showImage(ActionEvent event) {
        showImage(sourceFile.getAbsolutePath());
    }

    @FXML
    protected void selectTargetPath() {
        if (targetPathInput == null) {
            return;
        }
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = new File(AppVaribles.getConfigValue("imageTargetPath", System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            chooser.setInitialDirectory(path);
            File directory = chooser.showDialog(getMyStage());
            if (directory != null) {
                targetPathInput.setText(directory.getPath());
                AppVaribles.setConfigValue("imageTargetPath", directory.getPath());
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void startProcess(ActionEvent event) {
        try {
            attributes = imageConverterAttributesController.getAttributes();
            imageConverterAttributesController.getFinalXY();
            task = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        bufferImage = ImageIO.read(sourceFile);
                        targetFile = makeFilename();

//                        logger.debug("gray:" + BufferedImage.TYPE_BYTE_GRAY + "  binary: " + BufferedImage.TYPE_BYTE_BINARY);
//                        logger.debug("argb:" + BufferedImage.TYPE_INT_ARGB + "  rgb: " + BufferedImage.TYPE_INT_RGB);
                        int color = ImageTools.getColorType(bufferImage);
                        BufferedImage newImage = bufferImage;
                        if (attributes.getSourceWidth() != attributes.getTargetWidth()
                                || attributes.getSourceHeight() != attributes.getTargetHeight()) {
                            Image scaledImage = bufferImage.getScaledInstance(attributes.getTargetWidth(), attributes.getTargetHeight(), BufferedImage.SCALE_DEFAULT);
                            newImage = ImageConverter.toBufferedImage(scaledImage, color);
//                            logger.debug("newImage color:" + ImageTools.getColorType(newImage));
                        }
                        if (ImageType.BINARY == attributes.getColorSpace()) {
                            if (attributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_THRESHOLD
                                    && attributes.getThreshold() >= 0) {
                                newImage = ImageGrayTools.color2BinaryWithPercentage(newImage, attributes.getThreshold());
                            } else if (color != BufferedImage.TYPE_BYTE_BINARY) {
                                newImage = ImageGrayTools.color2Binary(newImage);
                            }
                        } else if (color != BufferedImage.TYPE_BYTE_GRAY && ImageType.GRAY == attributes.getColorSpace()) {
                            newImage = ImageGrayTools.color2Gray(newImage);
                        }
//                        logger.debug("Colored converted:" + ImageTools.getColorType(newImage));

                        ImageFileWriters.writeImageFile(newImage, attributes, targetFile);

                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            showImage(targetFile);
                            try {
                            } catch (Exception e) {
                                logger.error(e.toString());
                            }
                        }
                    });
                }

                @Override
                protected void cancelled() {
                    super.cancelled();

                }

                @Override
                protected void failed() {
                    super.failed();

                }
            };
            openLoadingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {

            logger.error(e.toString());
        }
    }

    @FXML
    protected void openTargetPath() {
        try {
            Desktop.getDesktop().browse(new File(targetPathInput.getText()).toURI());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void sourceFileChanged(final File file) {
        if (file == null) {
            return;
        }
        viewButton.setDisable(true);
        imageConverterAttributesController.getOriginalButton().setDisable(true);
        imageConverterAttributesController.getxInput().setText("");
        imageConverterAttributesController.getyInput().setText("");
        imageConverterAttributesController.getRatioBox().setDisable(true);
        imageConverterAttributesController.getAttributes().setSourceWidth(0);
        imageConverterAttributesController.getAttributes().setSourceHeight(0);
        loadImage(file, true);
    }

    @Override
    protected void afterImageLoaded() {
        if (imageInformation != null) {
            viewButton.setDisable(false);
            imageConverterAttributesController.getOriginalButton().setDisable(false);
            imageConverterAttributesController.getAttributes().setSourceWidth(imageInformation.getxPixels());
            imageConverterAttributesController.getAttributes().setSourceHeight(imageInformation.getyPixels());
            imageConverterAttributesController.setOriginalSize();
            imageConverterAttributesController.getRatioBox().setDisable(false);
        }
    }

    protected String makeFilename() {
        String fname = targetPathInput.getText() + "/" + targetPrefixInput.getText();
        if (appendColor.isSelected()) {
            if (ImageType.BINARY == attributes.getColorSpace()) {
                if (attributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_THRESHOLD) {
                    fname += "_" + "BINARY-Threshold";
                    if (attributes.getThreshold() >= 0) {
                        fname += "-" + attributes.getThreshold() + "%";
                    }
                } else if (attributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_OTSU) {
                    fname += "_" + "BINARY-OTSU";
                } else {
                    fname += "_" + attributes.getColorSpace();
                }
            } else {
                fname += "_" + attributes.getColorSpace();
            }
        }
        if (attributes.getCompressionType() != null
                && !AppVaribles.getMessage("None").equals(attributes.getCompressionType())) {
            if (appendCompressionType.isSelected()) {
                fname += "_" + attributes.getCompressionType().replace(" ", "_");
            }
            if (appendQuality.isSelected() && "jpg".equals(attributes.getImageFormat())) {
                fname += "_quality-" + attributes.getQuality() + "%";
            }
        }
        if (appendSize.isSelected()) {
            fname += "_" + attributes.getTargetWidth() + "x" + attributes.getTargetHeight();
        }
        fname += "." + attributes.getImageFormat();
        return fname;
    }

}
