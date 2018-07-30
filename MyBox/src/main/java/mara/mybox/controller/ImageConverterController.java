package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javax.imageio.ImageIO;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.image.ImageConverter;
import mara.mybox.image.ImageGrayTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.ImageAttributes;
import mara.mybox.tools.FileTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import org.apache.pdfbox.rendering.ImageType;

/**
 * @Author Mara
 * @CreateDate 2018-6-26
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageConverterController extends ImageBaseController {

    @FXML
    protected Button viewButton;

    @Override
    protected void initializeNext2() {
        try {
            viewButton.setDisable(true);

            imageConverterAttributesController.setParentFxml(myFxml);
            imageConverterAttributesController.getOriginalButton().setDisable(true);

            operationBarController.startButton.disableProperty().bind(
                    Bindings.isEmpty(sourceFileInput.textProperty())
                            .or(sourceFileInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(targetSelectionController.targetPathInput.textProperty()))
                            .or(targetSelectionController.targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(imageConverterAttributesController.getxInput().styleProperty().isEqualTo(badStyle))
                            .or(imageConverterAttributesController.getyInput().styleProperty().isEqualTo(badStyle))
                            .or(imageConverterAttributesController.getQualityBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageConverterAttributesController.getQualityInput().styleProperty().isEqualTo(badStyle)))
                            .or(imageConverterAttributesController.getColorBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageConverterAttributesController.getThresholdInput().styleProperty().isEqualTo(badStyle)))
            );
            operationBarController.openTargetButton.setVisible(false);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void showImage(ActionEvent event) {
        showImageManufacture(sourceFile.getAbsolutePath());
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

    @Override
    protected void makeMoreParameters() {
        makeSingleParameters();
    }

    @Override
    protected void doCurrentProcess() {
        try {
            if (currentParameters == null) {
                return;
            }
            attributes = imageConverterAttributesController.getAttributes();
            if (attributes == null) {
                return;
            }
            currentParameters.startTime = new Date();
            currentParameters.currentTotalHandled = 0;
            updateInterface("Started");
            task = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        for (; currentParameters.currentIndex < sourceFiles.size();) {
                            if (isCancelled()) {
                                break;
                            }
                            File file = sourceFiles.get(currentParameters.currentIndex);
                            currentParameters.sourceFile = file;
                            if (currentParameters.isBatch) {
                                currentParameters.targetPrefix = FileTools.getFilePrefix(file.getName());
                                if (currentParameters.createSubDir) {
                                    currentParameters.targetPath = currentParameters.targetRootPath + "/" + currentParameters.targetPrefix;
                                    File Path = new File(currentParameters.targetPath + "/");
                                    if (!Path.exists()) {
                                        Path.mkdirs();
                                    }
                                }
                            }

                            handleCurrentFile();
                            markFileHandled(currentParameters.currentIndex);

                            currentParameters.currentIndex++;
                            updateProgress(currentParameters.currentIndex, sourceFiles.size());
                            updateMessage(currentParameters.currentIndex + "/" + sourceFiles.size());
                            currentParameters.currentTotalHandled++;

                            if (isCancelled() || isPreview) {
                                break;
                            }

                        }
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                    return null;
                }

                private boolean handleCurrentFile() {
                    try {
                        BufferedImage bufferImage = ImageIO.read(currentParameters.sourceFile);
                        int w = attributes.getTargetWidth();
                        int h = attributes.getTargetHeight();
                        if (w <= 0 && currentParameters.isBatch) {
                            w = bufferImage.getWidth();
                        }
                        if (h <= 0 && currentParameters.isBatch) {
                            h = bufferImage.getHeight();
                        }
                        currentParameters.finalTargetName = makeFilename(w, h);
                        if (currentParameters.finalTargetName == null) {
                            return false;
                        }
                        BufferedImage newImage = ImageConverter.scaleImage(bufferImage, w, h);
                        int color = bufferImage.getType();
                        logger.debug(color);
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

//                        ImageIO.write(newImage, attributes.getImageFormat(), new File(targetFile));
                        ImageFileWriters.writeImageFile(newImage, attributes, currentParameters.finalTargetName);
                        return true;
                    } catch (Exception e) {
                        logger.error(e.toString());
                        return false;
                    }
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    updateInterface("Done");
                }

                @Override
                protected void cancelled() {
                    super.cancelled();
                    updateInterface("Canceled");
                }

                @Override
                protected void failed() {
                    super.failed();
                    updateInterface("Failed");
                }
            };
            operationBarController.progressValue.textProperty().bind(task.messageProperty());
            operationBarController.progressBar.progressProperty().bind(task.progressProperty());
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            updateInterface("Failed");
            logger.error(e.toString());
        }
    }

    protected String makeFilename(int w, int h) {
        try {
            String fname = currentParameters.targetPath + "/" + currentParameters.targetPrefix;
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
                fname += "_" + w + "x" + h;
            }
            fname += "." + attributes.getImageFormat();
            return fname;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
