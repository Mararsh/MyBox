package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.ImageAttributes;
import mara.mybox.tools.FileTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import mara.mybox.tools.ImageGrayTools;
import mara.mybox.tools.ImageWriters;
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
public class PdfConvertImagesController extends PdfBaseController {

    public PdfConvertImagesController() {
    }

    @Override
    protected void initializeNext2() {
        try {
            imageAttributesController.setParentFxml(myFxml);
            appendDensity.setSelected(AppVaribles.getConfigBoolean("pci_aDensity"));
            appendColor.setSelected(AppVaribles.getConfigBoolean("pci_aColor"));
            appendCompressionType.setSelected(AppVaribles.getConfigBoolean("pci_aCompression"));
            appendQuality.setSelected(AppVaribles.getConfigBoolean("pci_aQuality"));

            startButton.disableProperty().bind(
                    Bindings.isEmpty(sourceFileInput.textProperty())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(Bindings.isEmpty(fromPageInput.textProperty()))
                            .or(Bindings.isEmpty(toPageInput.textProperty()))
                            .or(Bindings.isEmpty(acumFromInput.textProperty()))
                            .or(sourceFileInput.styleProperty().isEqualTo(badStyle))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(fromPageInput.styleProperty().isEqualTo(badStyle))
                            .or(toPageInput.styleProperty().isEqualTo(badStyle))
                            .or(acumFromInput.styleProperty().isEqualTo(badStyle))
                            .or(imageAttributesController.getDensityInput().styleProperty().isEqualTo(badStyle))
                            .or(imageAttributesController.getQualityBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageAttributesController.getQualityInput().styleProperty().isEqualTo(badStyle)))
                            .or(imageAttributesController.getColorBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageAttributesController.getThresholdInput().styleProperty().isEqualTo(badStyle)))
            );

            previewButton.disableProperty().bind(
                    Bindings.isEmpty(sourceFileInput.textProperty())
                            .or(imageAttributesController.getRawSelect().selectedProperty())
                            .or(startButton.disableProperty())
                            .or(startButton.textProperty().isNotEqualTo(AppVaribles.getMessage("Start")))
                            .or(previewInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
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
            currentParameters.startTime = new Date();
            currentParameters.currentTotalHandled = 0;
            updateInterface("Started");
            task = new Task<Void>() {
                @Override
                protected Void call() {
                    try {

                        for (; currentParameters.currentFileIndex < sourceFiles.size(); currentParameters.currentFileIndex++) {
                            if (isCancelled()) {
                                break;
                            }
                            File file = sourceFiles.get(currentParameters.currentFileIndex);
                            currentParameters.sourceFile = file;
                            updateInterface("StartFile");
                            if (currentParameters.isBatch) {
                                currentParameters.targetPrefix = FileTools.getFilePrefix(file.getName());
                                if (subdirCheck.isSelected()) {
                                    currentParameters.targetPath = currentParameters.targetRootPath + "/" + currentParameters.targetPrefix;
                                    File Path = new File(currentParameters.targetPath + "/");
                                    if (!Path.exists()) {
                                        Path.mkdir();
                                    }
                                }
                            }

                            handleCurrentFile();

                            if (isCancelled() || isPreview) {
                                break;
                            }
                            currentParameters.acumStart = 0;
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

                private void handleCurrentFile() {
                    try {
                        try (PDDocument doc = PDDocument.load(currentParameters.sourceFile, currentParameters.password)) {
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
                                currentParameters.finalTargetName = makeFilename();
                                convertCurrentPage(renderer);
                                currentParameters.currentTotalHandled++;
                                currentParameters.currentNameNumber++;

                                int pages = currentParameters.currentPage - currentParameters.fromPage + 1;
                                updateProgress(pages, total);
                                updateMessage(pages + "/" + total);
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.toString());
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
            ImageAttributes attributes = imageAttributesController.getAttributes();
            BufferedImage image;
            if (ImageType.BINARY == attributes.getColorSpace()) {
                if (attributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_THRESHOLD
                        && attributes.getThreshold() >= 0) {
                    image = renderer.renderImageWithDPI(currentParameters.currentPage, attributes.getDensity(), ImageType.RGB);
                    image = ImageGrayTools.color2BinaryWithPercentage(image, attributes.getThreshold());
                } else if (attributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_OTSU) {
                    image = renderer.renderImageWithDPI(currentParameters.currentPage, attributes.getDensity(), ImageType.RGB);
                    image = ImageGrayTools.color2Binary(image);
                } else {
                    image = renderer.renderImageWithDPI(currentParameters.currentPage, attributes.getDensity(), attributes.getColorSpace());
                }
            } else {
                image = renderer.renderImageWithDPI(currentParameters.currentPage, attributes.getDensity(), attributes.getColorSpace());
            }
            ImageWriters.writeImageFile(image, attributes, currentParameters.finalTargetName);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected String makeFilename() {
        ImageAttributes attributes = imageAttributesController.getAttributes();
        String pageNumber = currentParameters.currentNameNumber + "";
        if (currentParameters.fill) {
            pageNumber = ValueTools.fillNumber(currentParameters.currentNameNumber, currentParameters.acumDigit);
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
        if (attributes.getCompressionType() != null && !"none".equals(attributes.getCompressionType())) {
            if (currentParameters.aCompression) {
                fname += "_" + attributes.getCompressionType().replace(" ", "_");
            }
            if (currentParameters.aQuality) {
                fname += "_quality-" + attributes.getQuality() + "%";
            }
        }
        fname += "." + attributes.getImageFormat();
        return fname;
    }

}
