package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FileTools;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.tools.ValueTools;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import static mara.mybox.objects.AppVaribles.getUserConfigValue;

/**
 * @Author Mara
 * @CreateDate 2018-9-10
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfSplitController extends PdfBaseController {

    final private String AuthorKey;
    private int splitType, pagesNumber, filesNumber;
    private List<Integer> startEndList;

    @FXML
    private ToggleGroup splitGroup;
    @FXML
    private TextField PagesNumberInput, FilesNumberInput, ListInput, authorInput;
    @FXML
    protected HBox pdfMemBox;
    @FXML
    private ToggleGroup pdfMemGroup;
    @FXML
    private RadioButton pdfMem500MRadio, pdfMem1GRadio, pdfMem2GRadio, pdfMemUnlimitRadio;

    public static class PdfSplitType {

        public static int PagesNumber = 1;
        public static int FilesNumber = 2;
        public static int StartEndList = 3;

    }

    public PdfSplitController() {
        AuthorKey = "AuthorKey";
    }

    @Override
    protected void initializeNext2() {
        try {
            allowPaused = false;

            initOptionsSection();

            operationBarController.startButton.disableProperty().bind(
                    Bindings.isEmpty(sourceSelectionController.sourceFileInput.textProperty())
                            .or(Bindings.isEmpty(sourceSelectionController.fromPageInput.textProperty()))
                            .or(Bindings.isEmpty(sourceSelectionController.toPageInput.textProperty()))
                            .or(sourceSelectionController.sourceFileInput.styleProperty().isEqualTo(badStyle))
                            .or(sourceSelectionController.fromPageInput.styleProperty().isEqualTo(badStyle))
                            .or(sourceSelectionController.toPageInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(targetSelectionController.targetPathInput.textProperty()))
                            .or(targetSelectionController.targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(PagesNumberInput.styleProperty().isEqualTo(badStyle))
                            .or(FilesNumberInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void initOptionsSection() {
        try {
            splitGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkSplitType();
                }
            });
            checkSplitType();

            PagesNumberInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkPagesNumber();
                }
            });

            FilesNumberInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkFilesNumber();
                }
            });

            ListInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkStartEndList();
                }
            });

            authorInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    AppVaribles.setUserConfigValue(AuthorKey, newValue);
                }
            });
            authorInput.setText(AppVaribles.getUserConfigValue(AuthorKey, System.getProperty("user.name")));

            Tooltip tips = new Tooltip(getMessage("PdfMemComments"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(pdfMemBox, tips);

            checkPdfMem();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void checkSplitType() {
        PagesNumberInput.setDisable(true);
        FilesNumberInput.setDisable(true);
        ListInput.setDisable(true);
        PagesNumberInput.setStyle(null);
        FilesNumberInput.setStyle(null);
        ListInput.setStyle(null);

        RadioButton selected = (RadioButton) splitGroup.getSelectedToggle();
        if (AppVaribles.getMessage("PagesNumberOfEachFile").equals(selected.getText())) {
            splitType = PdfSplitType.PagesNumber;
            PagesNumberInput.setDisable(false);
            checkPagesNumber();

        } else if (AppVaribles.getMessage("NumberOfFilesDividedEqually").equals(selected.getText())) {
            splitType = PdfSplitType.FilesNumber;
            FilesNumberInput.setDisable(false);
            checkFilesNumber();

        } else if (AppVaribles.getMessage("StartEndList").equals(selected.getText())) {
            splitType = PdfSplitType.StartEndList;
            ListInput.setDisable(false);
            checkStartEndList();
        }
    }

    private void checkPagesNumber() {
        try {
            pagesNumber = Integer.valueOf(PagesNumberInput.getText());
            if (pagesNumber >= 0) {
                PagesNumberInput.setStyle(null);
            } else {
                pagesNumber = 0;
                PagesNumberInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            pagesNumber = 0;
            PagesNumberInput.setStyle(badStyle);
        }
    }

    private void checkFilesNumber() {
        try {
            filesNumber = Integer.valueOf(FilesNumberInput.getText());
            if (filesNumber >= 0) {
                FilesNumberInput.setStyle(null);
            } else {
                filesNumber = 0;
                FilesNumberInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            filesNumber = 0;
            FilesNumberInput.setStyle(badStyle);
        }
    }

    private void checkStartEndList() {
        startEndList = new ArrayList<>();
        try {
            String[] list = ListInput.getText().split(",");
            for (String item : list) {
                String[] values = item.split("-");
                if (values.length != 2) {
                    continue;
                }
                try {
                    int start = Integer.valueOf(values[0].trim());
                    int end = Integer.valueOf(values[1].trim());
                    startEndList.add(start);
                    startEndList.add(end);
                } catch (Exception e) {
                }
            }
            if (startEndList.isEmpty()) {
                ListInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            ListInput.setStyle(badStyle);
        }
    }

    protected void checkPdfMem() {
        String pm = getUserConfigValue("PdfMemDefault", "1GB");
        switch (pm) {
            case "1GB":
                pdfMem1GRadio.setSelected(true);
                break;
            case "2GB":
                pdfMem2GRadio.setSelected(true);
                break;
            case "Unlimit":
                pdfMemUnlimitRadio.setSelected(true);
                break;
            case "500MB":
            default:
                pdfMem500MRadio.setSelected(true);
        }
    }

    @FXML
    protected void PdfMem500MB(ActionEvent event) {
        AppVaribles.setPdfMem("500MB");
    }

    @FXML
    protected void PdfMem1GB(ActionEvent event) {
        AppVaribles.setPdfMem("1GB");
    }

    @FXML
    protected void PdfMem2GB(ActionEvent event) {
        AppVaribles.setPdfMem("2GB");
    }

    @FXML
    protected void pdfMemUnlimit(ActionEvent event) {
        AppVaribles.setPdfMem("Unlimit");
    }

    @FXML
    protected void mouseEnterPane(MouseEvent event) {
        checkPdfMem();
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
            currentParameters.targetPath = new File(currentParameters.targetPath).getAbsolutePath();

            final MemoryUsageSetting memSettings = AppVaribles.PdfMemUsage.setTempDir(AppVaribles.getTempPathFile());

            updateInterface("Started");
            task = new Task<Void>() {
                private boolean fail;
                private List<String> files, wrongList;

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

                            files = new ArrayList<>();
                            wrongList = new ArrayList<>();
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

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String s = "";
                                if (!wrongList.isEmpty()) {
                                    s = AppVaribles.getMessage("WrongStartEnd") + "\n" + wrongList + "\n";
                                    ListInput.setStyle(badStyle);
                                }
                                if (!files.isEmpty()) {
                                    s += "\n" + AppVaribles.getMessage("FilesGeneratedUnder")
                                            + " " + currentParameters.targetPath;
                                    int num = files.size();
                                    if (num > 10) {
                                        num = 10;
                                    }
                                    for (int i = 0; i < num; i++) {
                                        s += "\n" + files.get(i);
                                    }
                                }
                                if (!s.isEmpty()) {
                                    popInformation(s);
                                }

                            } catch (Exception e) {
                                logger.error(e.toString());
                            }
                        }
                    });
                    return null;
                }

                private void handleCurrentFile() {
                    try {
                        try (PDDocument doc = PDDocument.load(currentParameters.sourceFile, currentParameters.password,
                                AppVaribles.PdfMemUsage)) {
                            if (currentParameters.acumDigit < 1) {
                                currentParameters.acumDigit = (doc.getNumberOfPages() + "").length();
                            }
                            if (!isPreview && currentParameters.isBatch) {
                                currentParameters.toPage = doc.getNumberOfPages() - 1;
                            }
                            currentParameters.currentNameNumber = currentParameters.acumStart;

                            if (splitType == PdfSplitType.PagesNumber) {
                                splitByPagesSize(doc);
                            } else if (splitType == PdfSplitType.FilesNumber) {
                                splitByFilesNumber(doc);
                            } else if (splitType == PdfSplitType.StartEndList) {
                                splitByList(doc);
                            }
                            int num = files.size();
//                            if (num == 0) num = doc.getNumberOfPages();
                            updateProgress(num, num);
                            updateMessage(num + "/" + num);
                        }

                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                }

                private void splitByPagesSize(PDDocument source) {
                    try {

                        Splitter splitter = new Splitter();
                        splitter.setStartPage(currentParameters.startPage + 1);
                        splitter.setEndPage(currentParameters.toPage + 1);
                        splitter.setMemoryUsageSetting(memSettings);
                        splitter.setSplitAtPage(pagesNumber);
                        List<PDDocument> docs = splitter.split(source);
                        writeFiles(docs);
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                }

                private void splitByFilesNumber(PDDocument source) {
                    try {
                        int total = currentParameters.toPage - currentParameters.startPage + 1;
                        int pagesNumber;
                        if (total % filesNumber == 0) {
                            pagesNumber = total / filesNumber;
                        } else {
                            pagesNumber = total / filesNumber + 1;
                        }
                        Splitter splitter = new Splitter();
                        splitter.setStartPage(currentParameters.startPage + 1);
                        splitter.setEndPage(currentParameters.toPage + 1);
                        splitter.setMemoryUsageSetting(memSettings);
                        splitter.setSplitAtPage(pagesNumber);
                        List<PDDocument> docs = splitter.split(source);
                        writeFiles(docs);
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                }

                private void splitByList(PDDocument source) {
                    try {
                        List<PDDocument> docs = new ArrayList<>();
                        for (int i = 0; i < startEndList.size();) {
                            int start = startEndList.get(i++);
                            int end = startEndList.get(i++);
                            if (start < currentParameters.startPage
                                    || end > currentParameters.toPage) {
                                wrongList.add(start + "-" + end);
                                continue;
                            }
                            Splitter splitter = new Splitter();
                            splitter.setStartPage(start + 1);
                            splitter.setEndPage(end + 1);
                            splitter.setMemoryUsageSetting(memSettings);
                            splitter.setSplitAtPage(end - start + 1);
                            docs.add(splitter.split(source).get(0));
                        }
                        writeFiles(docs);
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                }

                private void writeFiles(List<PDDocument> docs) {
                    try {
                        if (docs == null || docs.isEmpty()) {
                            return;
                        }
                        files = new ArrayList<>();
                        currentParameters.currentNameNumber = 0;
                        PDDocumentInformation info = new PDDocumentInformation();
                        info.setCreationDate(Calendar.getInstance());
                        info.setModificationDate(Calendar.getInstance());
                        info.setProducer("MyBox v" + CommonValues.AppVersion);
                        info.setAuthor(authorInput.getText());
                        for (PDDocument doc : docs) {
                            String pageNumber = ValueTools.fillNumber(currentParameters.currentNameNumber,
                                    (filesNumber + "").length());
                            String fname = currentParameters.targetPrefix + "_" + pageNumber + ".pdf";
                            String fullname = currentParameters.targetPath + "/" + fname;

                            doc.setDocumentInformation(info);

                            PDPage page = doc.getPage(0);
                            PDPageXYZDestination dest = new PDPageXYZDestination();
                            dest.setPage(page);
                            dest.setZoom(1f);
                            dest.setTop((int) page.getCropBox().getHeight());
                            PDActionGoTo action = new PDActionGoTo();
                            action.setDestination(dest);
                            doc.getDocumentCatalog().setOpenAction(action);

                            doc.save(fullname);
                            doc.close();
                            files.add(fname);
                            currentParameters.currentNameNumber++;
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
