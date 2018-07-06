package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.tools.FileTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * @Author Mara
 * @CreateDate 2018-7-1
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfExtractTextsController extends PdfBaseController {

    private String separator;

    @FXML
    protected CheckBox separatorCheck;
    @FXML
    protected TextField separatorInput;

    @Override
    protected void initializeNext2() {
        try {
            targetIsFile = true;
            operationBarController.startButton.disableProperty().bind(
                    Bindings.isEmpty(sourceSelectionController.sourceFileInput.textProperty())
                            .or(Bindings.isEmpty(targetSelectionController.targetPathInput.textProperty()))
                            .or(Bindings.isEmpty(targetSelectionController.targetFileInput.textProperty()))
                            .or(Bindings.isEmpty(sourceSelectionController.fromPageInput.textProperty()))
                            .or(Bindings.isEmpty(sourceSelectionController.toPageInput.textProperty()))
                            .or(sourceSelectionController.sourceFileInput.styleProperty().isEqualTo(badStyle))
                            .or(targetSelectionController.targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(sourceSelectionController.fromPageInput.styleProperty().isEqualTo(badStyle))
                            .or(sourceSelectionController.toPageInput.styleProperty().isEqualTo(badStyle))
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
    protected void sourceFileChanged(final File file) {
        super.sourceFileChanged(file);
        targetSelectionController.targetFileInput.setText(FileTools.getFilePrefix(file.getName()) + ".txt");

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
            isTxt = true;

            currentParameters.startTime = new Date();
            currentParameters.currentTotalHandled = 0;
            separator = separatorInput.getText();
            if (!separatorCheck.isSelected() || separator == null || separator.isEmpty()) {
                separator = null;
            }
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
                        finalTargetName = currentParameters.targetPath + "/"
                                + currentParameters.targetPrefix + ".txt";
                        FileWriter writer = new FileWriter(finalTargetName, false);
                        try (PDDocument doc = PDDocument.load(currentParameters.sourceFile, currentParameters.password)) {
                            if (currentParameters.acumDigit < 1) {
                                currentParameters.acumDigit = (doc.getNumberOfPages() + "").length();
                            }
                            if (!isPreview && currentParameters.isBatch) {
                                currentParameters.toPage = doc.getNumberOfPages() - 1;
                            }
                            currentParameters.currentNameNumber = currentParameters.acumStart;
                            int total = currentParameters.toPage - currentParameters.fromPage + 1;

                            PDFTextStripper stripper = new PDFTextStripper();
                            for (currentParameters.currentPage = currentParameters.startPage;
                                    currentParameters.currentPage <= currentParameters.toPage; currentParameters.currentPage++) {
                                if (isCancelled()) {
                                    writer.close();
                                    break;
                                }
                                stripper.setStartPage(currentParameters.currentPage + 1);
                                stripper.setEndPage(currentParameters.currentPage + 1);
                                String text = stripper.getText(doc);
                                if (text != null && !text.trim().isEmpty()) {
                                    writer.write(text);
                                    if (separator != null) {
                                        String s = separator.replace("<Page Number>", currentParameters.currentPage + " ");
                                        s = s.replace("<Total Number>", doc.getNumberOfPages() + "");
                                        writer.write(s);
                                        writer.write(System.getProperty("line.separator"));
                                    }
                                    writer.flush();
                                }

                                currentParameters.currentTotalHandled++;
                                int pages = currentParameters.currentPage - currentParameters.fromPage + 1;
                                updateProgress(pages, total);
                                updateMessage(pages + "/" + total);
                            }
                            writer.close();
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

}
