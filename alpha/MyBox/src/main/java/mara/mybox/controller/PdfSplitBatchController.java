package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import mara.mybox.data.PdfInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.pdfbox.Loader;
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
    protected ControlSplit splitController;

    public PdfSplitBatchController() {
        baseTitle = Languages.message("PdfSplitBatch");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            splitController.setParameters(this);

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(splitController.valid)
            );

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        try {
            doc = null;
            targetFilesCount = 0;
            targetFiles = new LinkedHashMap<>();
            PdfInformation info = currentPdf();
            currentParameters.fromPage = info.getFromPage();
            if (actualParameters.fromPage < 0) {
                actualParameters.fromPage = 0;
            }
            currentParameters.toPage = info.getToPage();
            currentParameters.password = info.getUserPassword();
            File pdfFile = currentSourceFile();
            try (PDDocument pd = Loader.loadPDF(pdfFile, currentParameters.password)) {
                doc = pd;
                if (currentParameters.toPage <= 0
                        || currentParameters.toPage > doc.getNumberOfPages()) {
                    currentParameters.toPage = doc.getNumberOfPages();
                }
                currentParameters.currentTargetPath = targetPath;
                if (currentParameters.targetSubDir) {
                    currentParameters.currentTargetPath = new File(targetPath.getAbsolutePath() + "/"
                            + FileNameTools.prefix(pdfFile.getName()));
                    if (!currentParameters.currentTargetPath.exists()) {
                        currentParameters.currentTargetPath.mkdirs();
                    }
                }
                if (null != splitController.splitType) {
                    switch (splitController.splitType) {
                        case Size:
                            splitByPagesSize(currentTask, doc);
                            break;
                        case Number:
                            splitByFilesNumber(currentTask, doc);
                            break;
                        case List:
                            splitByList(currentTask, doc);
                            break;
                        default:
                            break;
                    }
                }
                doc.close();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        if (currentTask == null || !currentTask.isWorking()) {
            return message("Canceled");
        }
        updateInterface("CompleteFile");
        return MessageFormat.format(Languages.message("HandlePagesGenerateNumber"),
                currentParameters.toPage - currentParameters.fromPage, targetFilesCount);
    }

    // 1-based  include end
    private Splitter splitter(int from, int to, int size) {
        try {
            if (from < 1 || to < 1 || to < from || size <= 0) {
                return null;
            }
            Splitter splitter = new Splitter();
            splitter.setStartPage(from);  // 1-based
            splitter.setEndPage(to);
            splitter.setSplitAtPage(size);
            return splitter;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    private int split(FxTask currentTask, PDDocument source, int from, int to, int size) {
        try {
            Splitter splitter = splitter(from, to, size);
            if (splitter == null) {
                return 0;
            }
            List<PDDocument> docs = splitter.split(source);
            return writeFiles(currentTask, docs);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return 0;
        }
    }

    private int splitByPagesSize(FxTask currentTask, PDDocument source) {
        return split(currentTask, source,
                currentParameters.fromPage + 1,
                currentParameters.toPage,
                splitController.size);
    }

    private int splitByFilesNumber(FxTask currentTask, PDDocument source) {
        try {
            int total = currentParameters.toPage - currentParameters.fromPage;
            int size = splitController.size(total, splitController.number);
            return split(currentTask, source,
                    currentParameters.fromPage + 1,
                    currentParameters.toPage,
                    size);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return 0;
        }
    }

    private int splitByList(FxTask currentTask, PDDocument source) {
        try {
            List<PDDocument> docs = new ArrayList<>();
            List<Integer> list = splitController.list;
            for (int i = 0; i < list.size();) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return 0;
                }
                int start = list.get(i++);
                int end = list.get(i++);
                if (start < currentParameters.fromPage + 1) {
                    start = currentParameters.fromPage + 1;
                }
                if (end > currentParameters.toPage) {
                    end = currentParameters.toPage;
                }
                Splitter splitter = splitter(start, end, end - start + 1);
                if (splitter == null) {
                    continue;
                }
                docs.add(splitter.split(source).get(0));
            }
            return writeFiles(currentTask, docs);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return 0;
        }
    }

    private int writeFiles(FxTask currentTask, List<PDDocument> docs) {
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
            String targetPrefix = FileNameTools.prefix(currentSourceFile().getName());
            int total = docs.size();
            for (PDDocument pd : docs) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return 0;
                }
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
            MyBoxLog.error(e);
        }
        return index - 1;
    }

}
