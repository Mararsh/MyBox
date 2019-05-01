package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import mara.mybox.controller.base.ImageBatchBaseController;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.image.ImageConvert;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.value.AppVaribles;
import mara.mybox.data.ImageAttributes;
import mara.mybox.tools.FileTools;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import org.apache.pdfbox.rendering.ImageType;
import mara.mybox.image.file.ImageFileReaders;

/**
 * @Author Mara
 * @CreateDate 2018-6-26
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageConverterController extends ImageBatchBaseController {

    public ImageAttributes imageAttributes = new ImageAttributes();

    @FXML
    public ImageConverterAttributesController imageConverterAttributesController;

    public ImageConverterController() {
        baseTitle = AppVaribles.getMessage("ImageConverter");

    }

    @Override
    public void initializeNext2() {
        try {
            viewButton.setDisable(true);

            imageConverterAttributesController.parentFxml = myFxml;
            imageConverterAttributesController.originalButton.setDisable(true);

            startButton.disableProperty().bind(
                    Bindings.isEmpty(sourceFileInput.textProperty())
                            .or(sourceFileInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(imageConverterAttributesController.xInput.styleProperty().isEqualTo(badStyle))
                            .or(imageConverterAttributesController.yInput.styleProperty().isEqualTo(badStyle))
                            .or(imageConverterAttributesController.qualityBox.disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageConverterAttributesController.qualityInput.styleProperty().isEqualTo(badStyle)))
                            .or(imageConverterAttributesController.colorBox.disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageConverterAttributesController.thresholdInput.styleProperty().isEqualTo(badStyle)))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void viewAction(ActionEvent event) {
        FxmlStage.openImageViewer(getClass(), null, sourceFile);
    }

    @Override
    public void sourceFileChanged(final File file) {
        if (file == null) {
            return;
        }
        viewButton.setDisable(true);
        imageConverterAttributesController.originalButton.setDisable(true);
        imageConverterAttributesController.xInput.setText("");
        imageConverterAttributesController.yInput.setText("");
        imageConverterAttributesController.ratioBox.setDisable(true);
        imageConverterAttributesController.getAttributes().setSourceWidth(0);
        imageConverterAttributesController.getAttributes().setSourceHeight(0);
        loadImageInformation(file);
    }

    @Override
    public void afterImageInfoLoaded() {
        if (imageInformation != null) {
            viewButton.setDisable(false);
            imageConverterAttributesController.originalButton.setDisable(false);
            imageConverterAttributesController.getAttributes().setSourceWidth(imageInformation.getWidth());
            imageConverterAttributesController.getAttributes().setSourceHeight(imageInformation.getHeight());
            imageConverterAttributesController.setOriginalSize();
            imageConverterAttributesController.ratioBox.setDisable(false);
        }
    }

    @Override
    public boolean makeActualParameters() {
        if (!super.makeActualParameters()) {
            return false;
        }

        if (imageConverterAttributesController != null) {
            actualParameters.aSize = appendSize.isSelected();
            actualParameters.aColor = appendColor.isSelected();
            actualParameters.aCompression = appendCompressionType.isSelected();
            actualParameters.aQuality = appendQuality.isSelected();

            AppVaribles.setUserConfigValue(appendSizeKey, actualParameters.aSize);
            AppVaribles.setUserConfigValue(appendColorKey, actualParameters.aColor);
            AppVaribles.setUserConfigValue(appendCompressionTypeKey, actualParameters.aCompression);
            AppVaribles.setUserConfigValue(appendQualityKey, actualParameters.aQuality);
        }
        return true;
    }

    @Override
    public void makeMoreParameters() {
        makeSingleParameters();
    }

    @Override
    public void doCurrentProcess() {
        try {
            if (currentParameters == null) {
                return;
            }
            imageAttributes = imageConverterAttributesController.getAttributes();
            if (imageAttributes == null) {
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

                            if (handleCurrentFile()) {
                                markFileHandled(currentParameters.currentIndex, AppVaribles.getMessage("Successful"));
                            } else {
                                markFileHandled(currentParameters.currentIndex, AppVaribles.getMessage("Failed"));
                            }

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
                        BufferedImage bufferedImage = ImageFileReaders.readImage(currentParameters.sourceFile);
                        int w = imageAttributes.getTargetWidth();
                        int h = imageAttributes.getTargetHeight();
                        if (w <= 0 && currentParameters.isBatch) {
                            w = bufferedImage.getWidth();
                        }
                        if (h <= 0 && currentParameters.isBatch) {
                            h = bufferedImage.getHeight();
                        }
                        String targetName = makeFilename(w, h);
                        if (targetName == null) {
                            return false;
                        }
                        actualParameters.finalTargetName = targetName;
                        bufferedImage = ImageConvert.scaleImage(bufferedImage, w, h);
                        bufferedImage = ImageFileWriters.convertColor(bufferedImage, imageAttributes);
                        ImageFileWriters.writeImageFile(bufferedImage, imageAttributes, actualParameters.finalTargetName);
                        targetFile = new File(actualParameters.finalTargetName);
                        if (targetFile.exists()) {
                            targetFiles.add(targetFile);
                            return true;
                        } else {
                            return false;
                        }
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
                if (ImageType.BINARY == imageAttributes.getColorSpace()) {
                    if (imageAttributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_THRESHOLD) {
                        fname += "_" + "BINARY-Threshold";
                        if (imageAttributes.getThreshold() >= 0) {
                            fname += "-" + imageAttributes.getThreshold();
                        }
                    } else if (imageAttributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_OTSU) {
                        fname += "_" + "BINARY-OTSU";
                    } else {
                        fname += "_" + imageAttributes.getColorSpace();
                    }
                } else {
                    fname += "_" + imageAttributes.getColorSpace();
                }
            }
            if (imageAttributes.getCompressionType() != null
                    && !AppVaribles.getMessage("None").equals(imageAttributes.getCompressionType())) {
                if (appendCompressionType.isSelected()) {
                    fname += "_" + imageAttributes.getCompressionType().replace(" ", "_");
                }
                if (appendQuality.isSelected() && "jpg".equals(imageAttributes.getImageFormat())) {
                    fname += "_quality-" + imageAttributes.getQuality() + "%";
                }
            }
            if (appendSize.isSelected()) {
                fname += "_" + w + "x" + h;
            }
            fname += "." + imageAttributes.getImageFormat();
            return fname;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
