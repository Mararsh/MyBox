package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import mara.mybox.data.PdfInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
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
public class PdfSplitBatchController extends BaseBatchPdfController {

    @FXML
    protected ControlFileSplit splitWayController;

    public PdfSplitBatchController() {
        baseTitle = Languages.message("PdfSplitBatch");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(splitWayController.valid)
                            .or(targetPathController.valid.not())
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        doc = null;
        targetFiles = new ArrayList<>();
        try {
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
                            + FileNameTools.prefix(currentParameters.currentSourceFile.getName()));
                    if (!currentParameters.currentTargetPath.exists()) {
                        currentParameters.currentTargetPath.mkdirs();
                    }
                }
                if (null != splitWayController.splitType) {
                    switch (splitWayController.splitType) {
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
        return MessageFormat.format(Languages.message("HandlePagesGenerateNumber"),
                currentParameters.toPage - currentParameters.fromPage, targetFiles.size());
    }

    private int splitByPagesSize(PDDocument source) {
        try {
            MyBoxLog.console(currentParameters.fromPage + " " + currentParameters.toPage);
            Splitter splitter = new Splitter();
            splitter.setStartPage(currentParameters.fromPage);  // 1-based
            splitter.setEndPage(currentParameters.toPage);
            splitter.setMemoryUsageSetting(AppVariables.pdfMemUsage);
            splitter.setSplitAtPage(splitWayController.pagesNumber);
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
            if (total % splitWayController.filesNumber == 0) {
                len = total / splitWayController.filesNumber;
            } else {
                len = total / splitWayController.filesNumber + 1;
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
            for (int i = 0; i < splitWayController.startEndList.size();) {
                int start = splitWayController.startEndList.get(i++);
                int end = splitWayController.startEndList.get(i++);
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
            info.setProducer("MyBox v" + AppValues.AppVersion);
            info.setAuthor(UserConfig.getString("AuthorKey", System.getProperty("user.name")));
            String targetPrefix = FileNameTools.prefix(currentParameters.currentSourceFile.getName());
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
