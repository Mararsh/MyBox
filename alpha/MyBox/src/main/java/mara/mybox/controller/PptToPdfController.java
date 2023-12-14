package mara.mybox.controller;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Calendar;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;

/**
 * @Author Mara
 * @CreateDate 2021-10-10
 * @License Apache License Version 2.0
 */
public class PptToPdfController extends BaseBatchFileController {

    @FXML
    protected ControlPdfWriteOptions pdfOptionsController;

    public PptToPdfController() {
        baseTitle = Languages.message("PptToPdf");
        targetFileSuffix = "pdf";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.PPTS, VisitHistory.FileType.PDF);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            pdfOptionsController.set(baseName, true);

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(tableView.getItems())
                    .or(targetPathController.valid.not())
            );

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        File target = makeTargetFile(srcFile, targetPath);
        if (target == null) {
            return message("Skip");
        }
        File tmpFile = FileTmpTools.getTempFile();
        try (PDDocument document = new PDDocument(AppVariables.PdfMemUsage);
                SlideShow ppt = SlideShowFactory.create(srcFile)) {
            PDDocumentInformation info = new PDDocumentInformation();
            info.setCreationDate(Calendar.getInstance());
            info.setModificationDate(Calendar.getInstance());
            info.setProducer("MyBox v" + AppValues.AppVersion);
            info.setAuthor(pdfOptionsController.authorInput.getText());
            document.setDocumentInformation(info);
            document.setVersion(1.0f);

            List<Slide> slides = ppt.getSlides();
            int width = ppt.getPageSize().width;
            int height = ppt.getPageSize().height;
            int count = 0, total = slides.size();
            for (Slide slide : slides) {
                if (task == null || task.isCancelled()) {
                    return message("Cancelled");
                }
                BufferedImage slideImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = slideImage.createGraphics();
                if (AppVariables.ImageHints != null) {
                    g.addRenderingHints(AppVariables.ImageHints);
                }
                slide.draw(g);
                if (task == null || task.isCancelled()) {
                    return message("Cancelled");
                }
                PdfTools.writePage(task,
                        document, "png", slideImage, ++count, total, pdfOptionsController);
            }
            PDPage page = document.getPage(0);
            PDPageXYZDestination dest = new PDPageXYZDestination();
            dest.setPage(page);
            dest.setZoom(pdfOptionsController.zoom / 100.0f);
            dest.setTop((int) page.getCropBox().getHeight());
            PDActionGoTo action = new PDActionGoTo();
            action.setDestination(dest);
            document.getDocumentCatalog().setOpenAction(action);

            document.save(tmpFile);
            document.close();
        } catch (Exception e) {
            updateLogs(e.toString());
            return e.toString();
        }
        if (FileTools.override(tmpFile, target)) {
            targetFileGenerated(target);
            return message("Successful");
        } else {
            return message("Failed");
        }
    }

}
