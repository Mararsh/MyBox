package mara.mybox.controller;

import mara.mybox.controller.base.PdfBatchBaseController;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import mara.mybox.tools.FileTools;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.ValueTools;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * @Author Mara
 * @CreateDate 2018-6-22
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfExtractImagesController extends PdfBatchBaseController {

    @FXML
    protected CheckBox appendPageNumber;
    @FXML
    protected CheckBox appendIndex;

    public PdfExtractImagesController() {
        baseTitle = AppVaribles.getMessage("PdfExtractImages");
        browseTargets = true;
    }

    @Override
    public void initializeNext2() {
        try {

            appendPageNumber.setSelected(AppVaribles.getUserConfigBoolean("PdfAppendPageNumber"));
            appendIndex.setSelected(AppVaribles.getUserConfigBoolean("PdfAppendIndex"));

            operationBarController.startButton.disableProperty().bind(
                    Bindings.isEmpty(sourceSelectionController.sourceFileInput.textProperty())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(Bindings.isEmpty(sourceSelectionController.fromPageInput.textProperty()))
                            .or(Bindings.isEmpty(sourceSelectionController.toPageInput.textProperty()))
                            .or(Bindings.isEmpty(acumFromInput.textProperty()))
                            .or(sourceSelectionController.sourceFileInput.styleProperty().isEqualTo(badStyle))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(sourceSelectionController.fromPageInput.styleProperty().isEqualTo(badStyle))
                            .or(sourceSelectionController.toPageInput.styleProperty().isEqualTo(badStyle))
                            .or(acumFromInput.styleProperty().isEqualTo(badStyle))
            );

            previewButton.disableProperty().bind(
                    Bindings.isEmpty(sourceSelectionController.sourceFileInput.textProperty())
                            .or(operationBarController.startButton.disableProperty())
                            .or(operationBarController.startButton.textProperty().isNotEqualTo(AppVaribles.getMessage("Start")))
                            .or(previewInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
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
            AppVaribles.setUserConfigValue("pei_appendPageNumber", appendPageNumber.isSelected());
            AppVaribles.setUserConfigValue("pei_appendIndex", appendIndex.isSelected());
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
                                if (subdirCheck.isSelected()) {
                                    currentParameters.targetPath = currentParameters.targetRootPath + "/" + currentParameters.targetPrefix;
                                    File Path = new File(currentParameters.targetPath + "/");
                                    if (!Path.exists()) {
                                        Path.mkdirs();
                                    }
                                }
                            }

                            int count = handleCurrentFile();
                            markFileHandled(currentParameters.currentIndex,
                                    MessageFormat.format(AppVaribles.getMessage("TotalExtractedImagesCount"), count));

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
                            if (currentParameters.acumDigit < 1) {
                                currentParameters.acumDigit = (doc.getNumberOfPages() + "").length();
                            }
                            if (!isPreview && currentParameters.isBatch) {
                                currentParameters.toPage = doc.getNumberOfPages() - 1;
                            }
                            currentParameters.currentNameNumber = currentParameters.acumStart;
                            int total = currentParameters.toPage - currentParameters.fromPage + 1;
                            for (currentParameters.currentPage = currentParameters.startPage;
                                    currentParameters.currentPage <= currentParameters.toPage; currentParameters.currentPage++) {
                                if (isCancelled()) {
                                    break;
                                }
                                PDPage page = doc.getPage(currentParameters.currentPage);
                                extractCurrentPage(page);

                                currentParameters.currentTotalHandled++;
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
                                actualParameters.finalTargetName = makeFilename(pdxObject.getSuffix(), currentParameters.currentPage, index++);
                                targetFiles.add(new File(actualParameters.finalTargetName));
                                ImageFileWriters.writeImageFile(pdxObject.getImage(), pdxObject.getSuffix(), actualParameters.finalTargetName);
//                                ImageIO.write(pdxObject.getImage(), pdxObject.getSuffix(), new File(currentParameters.actualParameters.finalTargetName));
                                currentParameters.currentNameNumber++;
                                if (isPreview) {
                                    break;
                                }
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

    protected String makeFilename(String suffix, int page, int index) {
        String pageNumber = currentParameters.currentNameNumber + "";
        if (currentParameters.fill) {
            pageNumber = ValueTools.fillLeftZero(currentParameters.currentNameNumber, currentParameters.acumDigit);
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
