package mara.mybox.controller;

import mara.mybox.controller.base.PdfBatchBaseController;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import mara.mybox.data.ControlStyle;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import mara.mybox.data.ImageAttributes;
import mara.mybox.tools.FileTools;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageBinary;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.ValueTools;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:09
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfConvertImagesController extends PdfBatchBaseController {

    @FXML
    public PdfConvertAttributesController pdfConvertAttributesController;

    public PdfConvertImagesController() {
        baseTitle = AppVaribles.getMessage("PdfConvertImages");
        browseTargets = true;
    }

    @Override
    public void initializeNext2() {
        try {
            startButton.disableProperty().bind(
                    Bindings.isEmpty(sourceSelectionController.sourceFileInput.textProperty())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(acumFromInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(sourceSelectionController.fromPageInput.textProperty()))
                            .or(Bindings.isEmpty(sourceSelectionController.toPageInput.textProperty()))
                            .or(Bindings.isEmpty(acumFromInput.textProperty()))
                            .or(sourceSelectionController.sourceFileInput.styleProperty().isEqualTo(badStyle))
                            .or(sourceSelectionController.fromPageInput.styleProperty().isEqualTo(badStyle))
                            .or(sourceSelectionController.toPageInput.styleProperty().isEqualTo(badStyle))
                            .or(pdfConvertAttributesController.densityInput.styleProperty().isEqualTo(badStyle))
                            .or(pdfConvertAttributesController.qualityBox.disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(pdfConvertAttributesController.qualityInput.styleProperty().isEqualTo(badStyle)))
                            .or(pdfConvertAttributesController.colorBox.disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(pdfConvertAttributesController.thresholdInput.styleProperty().isEqualTo(badStyle)))
            );

            previewButton.disableProperty().bind(
                    Bindings.isEmpty(sourceSelectionController.sourceFileInput.textProperty())
                            .or(pdfConvertAttributesController.rawSelect.selectedProperty())
                            .or(startButton.disableProperty())
                            .or(startButton.textProperty().isNotEqualTo(AppVaribles.getMessage("Start")))
                            .or(previewInput.styleProperty().isEqualTo(badStyle))
            );
            ControlStyle.setStyle(previewButton);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public boolean makeActualParameters() {
        if (!super.makeActualParameters()) {
            return false;
        }

        if (pdfConvertAttributesController != null) {
            actualParameters.aDensity = appendDensity.isSelected();
            actualParameters.aColor = appendColor.isSelected();
            actualParameters.aCompression = appendCompressionType.isSelected();
            actualParameters.aQuality = appendQuality.isSelected();

            AppVaribles.setUserConfigValue(appendDensityKey, actualParameters.aDensity);
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
            if (currentParameters == null || sourceFiles.isEmpty()) {
                return;
            }
            currentParameters.startTime = new Date();
            currentParameters.currentTotalHandled = 0;
            updateInterface("Started");
            task = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        for (; currentParameters.currentIndex < sourceFiles.size(); currentParameters.currentIndex++) {
                            if (isCancelled()) {
                                break;
                            }
                            File file = sourceFiles.get(currentParameters.currentIndex);
                            currentParameters.sourceFile = file;
                            updateInterface("StartFile");
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

                            int count = handleCurrentFile();
                            if (currentParameters.isBatch) {
                                markFileHandled(currentParameters.currentIndex,
                                        MessageFormat.format(AppVaribles.getMessage("TotalConvertedPagesCount"), count));
                            }
                            if (isCancelled() || isPreview) {
                                break;
                            }
                            currentParameters.acumStart = 1;
                            currentParameters.startPage = 0;
                            if (currentParameters.isBatch) {
                                updateInterface("CompleteFile");
                            }

                        }
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                    return null;
                }

                private int handleCurrentFile() {
                    int count = 0;
                    try {
                        try (PDDocument doc = PDDocument.load(currentParameters.sourceFile, currentParameters.password,
                                AppVaribles.pdfMemUsage)) {
                            if (actualParameters.acumDigit < 1) {
                                actualParameters.acumDigit = (doc.getNumberOfPages() + "").length();
                            }
                            if (!isPreview && currentParameters.isBatch) {
                                currentParameters.toPage = doc.getNumberOfPages() - 1;
                            }
                            PDFRenderer renderer = new PDFRenderer(doc);
                            currentParameters.currentNameNumber = currentParameters.acumStart;
                            int total = currentParameters.toPage - currentParameters.fromPage + 1;
                            for (currentParameters.currentPage = currentParameters.startPage;
                                    currentParameters.currentPage <= currentParameters.toPage; currentParameters.currentPage++) {
                                if (isCancelled()) {
                                    break;
                                }
                                actualParameters.finalTargetName = makeFilename();
                                targetFiles.add(new File(actualParameters.finalTargetName));
                                convertCurrentPage(renderer);
                                currentParameters.currentTotalHandled++;
                                currentParameters.currentNameNumber++;

                                int pages = currentParameters.currentPage - currentParameters.fromPage + 1;
                                updateProgress(pages, total);
                                updateMessage(pages + "/" + total);
                                count++;
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                    return count;
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
            progressValue.textProperty().bind(task.messageProperty());
            progressBar.progressProperty().bind(task.progressProperty());
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            updateInterface("Failed");
            logger.error(e.toString());
        }
    }

    protected void convertCurrentPage(PDFRenderer renderer) {
        try {
            ImageAttributes attributes = pdfConvertAttributesController.imageAttributes;
            BufferedImage image;
            if (ImageType.BINARY == attributes.getColorSpace()) {
                if (attributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_THRESHOLD
                        && attributes.getThreshold() >= 0) {
                    image = renderer.renderImageWithDPI(currentParameters.currentPage, attributes.getDensity(), ImageType.RGB);
                    ImageBinary imageBinary = new ImageBinary(image, attributes.getThreshold());
                    imageBinary.setIsDithering(attributes.isIsDithering());
                    image = imageBinary.operate();
                    image = ImageBinary.byteBinary(image);

                } else if (attributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_OTSU) {
                    image = renderer.renderImageWithDPI(currentParameters.currentPage, attributes.getDensity(), ImageType.RGB);
                    ImageBinary imageBinary = new ImageBinary(image, -1);
                    imageBinary.setCalculate(true);
                    imageBinary.setIsDithering(attributes.isIsDithering());
                    image = imageBinary.operate();
                    image = ImageBinary.byteBinary(image);

                } else {
                    if (attributes.isIsDithering()) {
                        image = renderer.renderImageWithDPI(currentParameters.currentPage, attributes.getDensity(), ImageType.RGB);
                        ImageBinary imageBinary = new ImageBinary(image, -1);
                        imageBinary.setIsDithering(true);
                        image = imageBinary.operate();
                        image = ImageBinary.byteBinary(image);
                    } else {
                        image = renderer.renderImageWithDPI(currentParameters.currentPage, attributes.getDensity(), ImageType.BINARY);
                    }
                }
            } else {
                image = renderer.renderImageWithDPI(currentParameters.currentPage, attributes.getDensity(), attributes.getColorSpace());
            }
            ImageFileWriters.writeImageFile(image, attributes, actualParameters.finalTargetName);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected String makeFilename() {
        ImageAttributes attributes = pdfConvertAttributesController.imageAttributes;
        String pageNumber = currentParameters.currentNameNumber + "";
        if (currentParameters.fill) {
            pageNumber = ValueTools.fillLeftZero(currentParameters.currentNameNumber, currentParameters.acumDigit);
        }
        String fname = currentParameters.targetPath + "/" + currentParameters.targetPrefix + "_" + pageNumber;
        if (currentParameters.aColor) {
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
        if (currentParameters.aDensity) {
            fname += "_" + attributes.getDensity() + "dpi";
        }
        if (attributes.getCompressionType() != null
                && !AppVaribles.getMessage("None").equals(attributes.getCompressionType())) {
            if (currentParameters.aCompression) {
                fname += "_" + attributes.getCompressionType().replace(" ", "_");
            }
            if (currentParameters.aQuality && "jpg".equals(attributes.getImageFormat())) {
                fname += "_quality-" + attributes.getQuality() + "%";
            }
        }
        fname += "." + attributes.getImageFormat();
        return fname;
    }

}
