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
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.image.ImageGrayTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.ValueTools;
import org.apache.pdfbox.io.MemoryUsageSetting;
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

//            sourceSelectionController.fromPageInput.setText(AppVaribles.getConfigValue(PdfSourceFromKey, "0"));
//            sourceSelectionController.toPageInput.setText(AppVaribles.getConfigValue(PdfSourceToKey, ""));
            operationBarController.startButton.disableProperty().bind(
                    Bindings.isEmpty(sourceSelectionController.sourceFileInput.textProperty())
                            .or(Bindings.isEmpty(targetSelectionController.targetPathInput.textProperty()))
                            .or(Bindings.isEmpty(sourceSelectionController.fromPageInput.textProperty()))
                            .or(Bindings.isEmpty(sourceSelectionController.toPageInput.textProperty()))
                            .or(Bindings.isEmpty(acumFromInput.textProperty()))
                            .or(sourceSelectionController.sourceFileInput.styleProperty().isEqualTo(badStyle))
                            .or(targetSelectionController.targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(sourceSelectionController.fromPageInput.styleProperty().isEqualTo(badStyle))
                            .or(sourceSelectionController.toPageInput.styleProperty().isEqualTo(badStyle))
                            .or(acumFromInput.styleProperty().isEqualTo(badStyle))
                            .or(pdfConvertAttributesController.getDensityInput().styleProperty().isEqualTo(badStyle))
                            .or(pdfConvertAttributesController.getQualityBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(pdfConvertAttributesController.getQualityInput().styleProperty().isEqualTo(badStyle)))
                            .or(pdfConvertAttributesController.getColorBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(pdfConvertAttributesController.getThresholdInput().styleProperty().isEqualTo(badStyle)))
            );

            previewButton.disableProperty().bind(
                    Bindings.isEmpty(sourceSelectionController.sourceFileInput.textProperty())
                            .or(pdfConvertAttributesController.getRawSelect().selectedProperty())
                            .or(operationBarController.startButton.disableProperty())
                            .or(operationBarController.startButton.textProperty().isNotEqualTo(AppVaribles.getMessage("Start")))
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
                                if (currentParameters.createSubDir) {
                                    currentParameters.targetPath = currentParameters.targetRootPath + "/" + currentParameters.targetPrefix;
                                    File Path = new File(currentParameters.targetPath + "/");
                                    if (!Path.exists()) {
                                        Path.mkdirs();
                                    }
                                }
                            }

                            handleCurrentFile();
                            markFileHandled(currentParameters.currentFileIndex);

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
                        try (PDDocument doc = PDDocument.load(currentParameters.sourceFile, currentParameters.password,
                                AppVaribles.PdfMemUsage)) {
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
                                finalTargetName = makeFilename();
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

    protected void convertCurrentPage(PDFRenderer renderer) {
        try {
            ImageAttributes attributes = pdfConvertAttributesController.getAttributes();
            BufferedImage image;
            if (ImageType.BINARY == attributes.getColorSpace()) {
                if (attributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_THRESHOLD
                        && attributes.getThreshold() >= 0) {
                    image = renderer.renderImageWithDPI(currentParameters.currentPage, attributes.getDensity(), ImageType.RGB);
                    image = ImageGrayTools.color2BinaryWithPercentage(image, attributes.getThreshold());
                } else if (attributes.getBinaryConversion() == ImageAttributes.BinaryConversion.BINARY_OTSU) {
                    image = renderer.renderImageWithDPI(currentParameters.currentPage, attributes.getDensity(), ImageType.RGB);
                    image = ImageGrayTools.color2BinaryByCalculation(image);
                } else {
                    image = renderer.renderImageWithDPI(currentParameters.currentPage, attributes.getDensity(), ImageType.BINARY);
                }
            } else {
                image = renderer.renderImageWithDPI(currentParameters.currentPage, attributes.getDensity(), attributes.getColorSpace());
            }
            ImageFileWriters.writeImageFile(image, attributes, finalTargetName);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected String makeFilename() {
        ImageAttributes attributes = pdfConvertAttributesController.getAttributes();
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
