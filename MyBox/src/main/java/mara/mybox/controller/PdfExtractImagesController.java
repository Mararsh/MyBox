/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template sourceFile, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javax.imageio.ImageIO;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.tools.FileTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import mara.mybox.tools.ValueTools;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class PdfExtractImagesController extends PdfBaseController {

    @FXML
    protected CheckBox appendPageNumber;
    @FXML
    protected CheckBox appendIndex;

    @Override
    protected void initializeNext2() {
        try {

            appendPageNumber.setSelected(AppVaribles.getConfigBoolean("pei_appendPageNumber"));
            appendIndex.setSelected(AppVaribles.getConfigBoolean("pei_appendIndex"));

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
            );

            previewButton.disableProperty().bind(
                    Bindings.isEmpty(sourceFileInput.textProperty())
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
            AppVaribles.setConfigValue("pei_appendPageNumber", appendPageNumber.isSelected());
            AppVaribles.setConfigValue("pei_appendIndex", appendIndex.isSelected());
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
                            if (currentParameters.acumDigit < 1) {
                                currentParameters.acumDigit = (doc.getNumberOfPages() + "").length();
                            }
                            if (!isPreview && currentParameters.isBatch) {
                                currentParameters.toPage = doc.getNumberOfPages() - 1;
                            }
                            PDPageTree pageTree = doc.getPages();
                            currentParameters.currentNameNumber = currentParameters.acumStart;
                            int total = currentParameters.toPage - currentParameters.fromPage + 1;
                            for (currentParameters.currentPage = currentParameters.startPage;
                                    currentParameters.currentPage <= currentParameters.toPage; currentParameters.currentPage++) {
                                if (isCancelled()) {
                                    break;
                                }
                                PDPage page = pageTree.get(currentParameters.currentPage);
                                extractCurrentPage(page);

                                currentParameters.currentTotalHandled++;
                                int pages = currentParameters.currentPage - currentParameters.fromPage + 1;
                                updateProgress(pages, total);
                                updateMessage(pages + "/" + total);
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                }

                protected void extractCurrentPage(PDPage pdPage) {
                    try {
                        PDResources pdResources = pdPage.getResources();
                        Iterable<COSName> iterable = pdResources.getXObjectNames();
                        if (iterable != null) {
                            Iterator<COSName> pageIterator = iterable.iterator();
                            int index = 0;
                            while (pageIterator.hasNext()) {
                                if (isCancelled()) {
                                    break;
                                }
                                COSName cosName = pageIterator.next();
                                if (!pdResources.isImageXObject(cosName)) {
                                    continue;
                                }
                                PDImageXObject pdxObject = (PDImageXObject) pdResources.getXObject(cosName);
                                currentParameters.finalTargetName = makeFilename(pdxObject.getSuffix(), currentParameters.currentPage, index++);
                                ImageIO.write(pdxObject.getImage(), pdxObject.getSuffix(), new File(currentParameters.finalTargetName));
                                currentParameters.currentNameNumber++;
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

    protected String makeFilename(String suffix, int page, int index) {
        String pageNumber = currentParameters.currentNameNumber + "";
        if (currentParameters.fill) {
            pageNumber = ValueTools.fillNumber(currentParameters.currentNameNumber, currentParameters.acumDigit);
        }
        String fname = currentParameters.targetPath + "/" + currentParameters.targetPrefix + "_" + pageNumber;
        if (appendPageNumber.isSelected()) {
            fname += "_page" + page;
        }
        if (appendIndex.isSelected()) {
            fname += "_index" + index;
        }
        fname += "." + suffix;
        return fname;
    }

}
