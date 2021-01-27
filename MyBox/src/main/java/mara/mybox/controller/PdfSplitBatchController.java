package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import mara.mybox.data.PdfInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;

/**
 * @Author Mara
 * @CreateDate 2018-9-10
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfSplitBatchController extends PdfBatchController {

    private int pagesNumber, filesNumber;
    private List<Integer> startEndList;
    private PdfSplitType splitType;

    @FXML
    protected ToggleGroup splitGroup;
    @FXML
    protected TextField PagesNumberInput, FilesNumberInput, ListInput;

    public enum PdfSplitType {
        PagesNumber, FilesNumber, StartEndList
    }

    public PdfSplitBatchController() {
        baseTitle = AppVariables.message("PdfSplitBatch");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(PagesNumberInput.styleProperty().isEqualTo(badStyle))
                            .or(FilesNumberInput.styleProperty().isEqualTo(badStyle))
                            .or(ListInput.styleProperty().isEqualTo(badStyle))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
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
            FxmlControl.setTooltip(ListInput, new Tooltip(message("StartEndComments")));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
        if (AppVariables.message("PagesNumberOfEachFile").equals(selected.getText())) {
            splitType = PdfSplitType.PagesNumber;
            PagesNumberInput.setDisable(false);
            checkPagesNumber();

        } else if (AppVariables.message("NumberOfFilesDividedEqually").equals(selected.getText())) {
            splitType = PdfSplitType.FilesNumber;
            FilesNumberInput.setDisable(false);
            checkFilesNumber();

        } else if (AppVariables.message("StartEndList").equals(selected.getText())) {
            splitType = PdfSplitType.StartEndList;
            ListInput.setDisable(false);
            checkStartEndList();
        }
    }

    private void checkPagesNumber() {
        try {
            int v = Integer.valueOf(PagesNumberInput.getText());
            if (v > 0) {
                PagesNumberInput.setStyle(null);
                pagesNumber = v;
            } else {
                PagesNumberInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            PagesNumberInput.setStyle(badStyle);
        }
    }

    private void checkFilesNumber() {
        try {
            int v = Integer.valueOf(FilesNumberInput.getText());
            if (v > 0) {
                FilesNumberInput.setStyle(null);
                filesNumber = v;
            } else {
                FilesNumberInput.setStyle(badStyle);
            }
        } catch (Exception e) {
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
                    if (start > 0 && end >= start) {  // 1-based start
                        startEndList.add(start);
                        startEndList.add(end);
                    }
                } catch (Exception e) {
                }
            }
            if (startEndList.isEmpty()) {
                ListInput.setStyle(badStyle);
            } else {
                ListInput.setStyle(null);
            }
        } catch (Exception e) {
            ListInput.setStyle(badStyle);
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        doc = null;
        targetFiles = new ArrayList<>();
        try {
            countHandling(srcFile);
            currentParameters.currentSourceFile = srcFile;
            PdfInformation info = (PdfInformation) tableData.get(currentParameters.currentIndex);
            currentParameters.fromPage = info.getFromPage();
            if (actualParameters.fromPage <= 0) {
                actualParameters.fromPage = 1;
            }
            currentParameters.toPage = info.getToPage();
            currentParameters.password = info.getUserPassword();
            try ( PDDocument pd = PDDocument.load(currentParameters.currentSourceFile,
                    currentParameters.password, AppVariables.pdfMemUsage)) {
                doc = pd;
                if (currentParameters.toPage <= 0 || currentParameters.toPage > doc.getNumberOfPages()) {
                    currentParameters.toPage = doc.getNumberOfPages();
                }
                MyBoxLog.console(currentParameters.fromPage + " " + currentParameters.toPage);
                currentParameters.currentTargetPath = targetPath;
                if (currentParameters.targetSubDir) {
                    currentParameters.currentTargetPath = new File(targetPath.getAbsolutePath() + "/"
                            + FileTools.getFilePrefix(currentParameters.currentSourceFile.getName()));
                    if (!currentParameters.currentTargetPath.exists()) {
                        currentParameters.currentTargetPath.mkdirs();
                    }
                }
                if (null != splitType) {
                    switch (splitType) {
                        case PagesNumber:
                            splitByPagesSize(doc);
                            break;
                        case FilesNumber:
                            splitByFilesNumber(doc);
                            break;
                        case StartEndList:
                            splitByList(doc);
                            break;
                        default:
                            break;
                    }
                }
                doc.close();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        updateInterface("CompleteFile");
        return MessageFormat.format(AppVariables.message("HandlePagesGenerateNumber"),
                currentParameters.toPage - currentParameters.fromPage, targetFiles.size());
    }

    private int splitByPagesSize(PDDocument source) {
        try {
            MyBoxLog.console(currentParameters.fromPage + " " + currentParameters.toPage);
            Splitter splitter = new Splitter();
            splitter.setStartPage(currentParameters.fromPage);  // 1-based
            splitter.setEndPage(currentParameters.toPage);
            splitter.setMemoryUsageSetting(AppVariables.pdfMemUsage);
            splitter.setSplitAtPage(pagesNumber);
            List<PDDocument> docs = splitter.split(source);
            return writeFiles(docs);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return 0;
        }
    }

    private int splitByFilesNumber(PDDocument source) {
        try {
            int total = currentParameters.toPage - currentParameters.fromPage + 1;
            int len;
            if (total % filesNumber == 0) {
                len = total / filesNumber;
            } else {
                len = total / filesNumber + 1;
            }
            Splitter splitter = new Splitter();
            splitter.setStartPage(currentParameters.fromPage);  // 1-based
            splitter.setEndPage(currentParameters.toPage);  // 1-based
            splitter.setMemoryUsageSetting(AppVariables.pdfMemUsage);
            splitter.setSplitAtPage(len);
            List<PDDocument> docs = splitter.split(source);
            return writeFiles(docs);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return 0;
        }
    }

    private int splitByList(PDDocument source) {
        try {
            List<PDDocument> docs = new ArrayList<>();
            for (int i = 0; i < startEndList.size();) {
                int start = startEndList.get(i++);
                int end = startEndList.get(i++);
                if (start < currentParameters.fromPage) {
                    start = currentParameters.fromPage;
                }
                if (end > currentParameters.toPage) {
                    end = currentParameters.toPage;
                }
                Splitter splitter = new Splitter();
                splitter.setStartPage(start);  // 1-based start
                splitter.setEndPage(end);
                splitter.setMemoryUsageSetting(AppVariables.pdfMemUsage);
                splitter.setSplitAtPage(end - start + 1);
                docs.add(splitter.split(source).get(0));
            }
            return writeFiles(docs);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return 0;
        }
    }

    private int writeFiles(List<PDDocument> docs) {
        int index = 1;
        try {
            if (docs == null || docs.isEmpty()) {
                return 0;
            }
            PDDocumentInformation info = new PDDocumentInformation();
            info.setCreationDate(Calendar.getInstance());
            info.setModificationDate(Calendar.getInstance());
            info.setProducer("MyBox v" + CommonValues.AppVersion);
            info.setAuthor(AppVariables.getUserConfigValue("AuthorKey", System.getProperty("user.name")));
            String targetPrefix = FileTools.getFilePrefix(currentParameters.currentSourceFile.getName());
            int total = docs.size();
            for (PDDocument pd : docs) {
                pd.setDocumentInformation(info);
                pd.setVersion(1.0f);
                PDPage page = pd.getPage(0);
                PDPageXYZDestination dest = new PDPageXYZDestination();
                dest.setPage(page);
                dest.setZoom(1f);
                dest.setTop((int) page.getCropBox().getHeight());
                PDActionGoTo action = new PDActionGoTo();
                action.setDestination(dest);
                pd.getDocumentCatalog().setOpenAction(action);

                String namePrefix = targetPrefix + "_" + StringTools.fillLeftZero(index++, (total + "").length());
                File tFile = makeTargetFile(namePrefix, ".pdf", currentParameters.currentTargetPath);
                pd.save(tFile);
                pd.close();

                targetFileGenerated(tFile);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return index - 1;
    }

}
