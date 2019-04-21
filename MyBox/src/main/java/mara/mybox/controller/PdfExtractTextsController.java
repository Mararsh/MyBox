package mara.mybox.controller;

import mara.mybox.controller.base.PdfBatchBaseController;
import java.io.File;
import java.io.FileWriter;
import java.text.MessageFormat;
import java.util.Date;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import mara.mybox.tools.FileTools;
import static mara.mybox.fxml.FxmlControl.badStyle;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * @Author Mara
 * @CreateDate 2018-7-1
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfExtractTextsController extends PdfBatchBaseController {

    private String separator;

    @FXML
    protected CheckBox separatorCheck;
    @FXML
    protected TextField separatorInput;

    public PdfExtractTextsController() {
        baseTitle = AppVaribles.getMessage("PdfExtractTexts");

    }

    @Override
    public void initializeNext2() {
        try {
            operationBarController.startButton.disableProperty().bind(
                    Bindings.isEmpty(sourceSelectionController.sourceFileInput.textProperty())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(Bindings.isEmpty(targetFileInput.textProperty()))
                            .or(Bindings.isEmpty(sourceSelectionController.fromPageInput.textProperty()))
                            .or(Bindings.isEmpty(sourceSelectionController.toPageInput.textProperty()))
                            .or(sourceSelectionController.sourceFileInput.styleProperty().isEqualTo(badStyle))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
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
    public void sourceFileChanged(final File file) {
        super.sourceFileChanged(file);
        targetFileInput.setText(FileTools.getFilePrefix(file.getName()) + ".txt");

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
            separator = separatorInput.getText();
            if (!separatorCheck.isSelected() || separator == null || separator.isEmpty()) {
                separator = null;
            }
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
                                actualParameters.finalTargetName = currentParameters.targetPath + "/"
                                        + FileTools.getFilePrefix(file.getName()) + ".txt";
                            }
                            targetFiles.add(new File(actualParameters.finalTargetName));
                            int count = handleCurrentFile();
                            markFileHandled(currentParameters.currentIndex,
                                    MessageFormat.format(AppVaribles.getMessage("TotalExtractedCharactersCount"), count));

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
                        File file = new File(actualParameters.finalTargetName);
                        if (file.exists()) {
                            file.delete();
                        }
                        FileWriter writer = new FileWriter(actualParameters.finalTargetName, false);
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
                                    count += text.length();
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
