package mara.mybox.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.Languages;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

/**
 * @Author Mara
 * @CreateDate 2021-5-20
 * @License Apache License Version 2.0
 */
public class PptSplitController extends BaseBatchFileController {

    @FXML
    protected ControlFileSplit splitWayController;

    public PptSplitController() {
        baseTitle = Languages.message("PptSplit");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.PPTS, VisitHistory.FileType.PPTS);
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
        try {
            int total;

            try ( SlideShow ppt = SlideShowFactory.create(srcFile)) {
                total = ppt.getSlides().size();
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
                return e.toString();
            }
            String suffix = FileNameTools.getFileSuffix(srcFile);
            switch (splitWayController.splitType) {
                case PagesNumber:
                    splitByPagesSize(srcFile, targetPath, total, suffix, splitWayController.pagesNumber);
                    break;
                case FilesNumber:
                    int pagesNumber = total / splitWayController.filesNumber;
                    if (total % splitWayController.filesNumber > 0) {
                        pagesNumber++;
                    }
                    splitByPagesSize(srcFile, targetPath, total, suffix, pagesNumber);
                    break;
                case StartEndList:
                    splitByList(srcFile, targetPath, suffix);
                    break;
                default:
                    break;
            }
            updateInterface("CompleteFile");
            totalItemsHandled += total;
            return MessageFormat.format(Languages.message("HandlePagesGenerateNumber"),
                    totalItemsHandled, targetFiles.size());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return e.toString();
        }
    }

    protected void splitByPagesSize(File srcFile, File targetPath, int total, String suffix, int pagesSize) {
        try {
            int start = 0, end, index = 0;
            boolean pptx = "pptx".equalsIgnoreCase(suffix);
            while (start < total) {
                if (task == null || task.isCancelled()) {
                    return;
                }
                end = start + pagesSize;
                targetFile = makeTargetFile(srcFile, ++index, suffix, targetPath);
                if (pptx) {
                    if (savePPTX(srcFile, targetFile, start, end)) {
                        targetFileGenerated(targetFile);
                    }
                } else {
                    if (savePPT(srcFile, targetFile, start, end)) {
                        targetFileGenerated(targetFile);
                    }
                }
                start = end;
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void splitByList(File srcFile, File targetPath, String suffix) {
        try {
            int start = 0, end, index = 0;
            boolean pptx = "pptx".equalsIgnoreCase(suffix);
            for (int i = 0; i < splitWayController.startEndList.size();) {
                if (task == null || task.isCancelled()) {
                    return;
                }
                // To user, both start and end are 1-based. To codes, both start and end are 0-based.
                // To user, both start and end are included. To codes, start is included while end is excluded.
                start = splitWayController.startEndList.get(i++) - 1;
                end = splitWayController.startEndList.get(i++);
                targetFile = makeTargetFile(srcFile, ++index, suffix, targetPath);
                if (pptx) {
                    if (savePPTX(srcFile, targetFile, start, end)) {
                        targetFileGenerated(targetFile);
                    }
                } else {
                    if (savePPT(srcFile, targetFile, start, end)) {
                        targetFileGenerated(targetFile);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public File makeTargetFile(File srcFile, int index, String ext, File targetPath) {
        try {
            String filePrefix = FileNameTools.getFilePrefix(srcFile);
            String splitPrefix = filePrefix + "_" + index;
            String splitSuffix = (ext.startsWith(".") ? "" : ".") + ext;

            File slidePath = targetPath;
            if (targetSubdirCheck.isSelected()) {
                slidePath = new File(targetPath, filePrefix);
            }
            return makeTargetFile(splitPrefix, splitSuffix, slidePath);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    // Include start, and exlucde end. Both start and end are 0-based
    protected boolean savePPT(File srcFile, File targetFile, int start, int end) {
        try ( HSLFSlideShow ppt = new HSLFSlideShow(new FileInputStream(srcFile))) {
            List<HSLFSlide> slides = ppt.getSlides();
            int total = slides.size();
            if (start > end || start >= total) {
                return false;
            }
            // https://stackoverflow.com/questions/51419421/split-pptx-slideshow-with-apache-poi
            // Need delete shapes for ppt
            for (int i = total - 1; i >= end; i--) {
                HSLFSlide slide = slides.get(i);
                Iterator<HSLFShape> iterator = slide.iterator();
                if (iterator != null) {
                    while (iterator.hasNext()) {
                        slide.removeShape(iterator.next());
                    }
                }
                ppt.removeSlide(i);
            }
            for (int i = 0; i < start; i++) {
                HSLFSlide slide = slides.get(0);
                Iterator<HSLFShape> iterator = slide.iterator();
                if (iterator != null) {
                    while (iterator.hasNext()) {
                        slide.removeShape(iterator.next());
                    }
                }
                ppt.removeSlide(0);
            }
            ppt.write(targetFile);
            return targetFile.exists();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    // Include start, and exlucde end. Both start and end are 0-based
    protected boolean savePPTX(File srcFile, File targetFile, int start, int end) {
        try ( XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(srcFile));
                 FileOutputStream out = new FileOutputStream(targetFile)) {
            List<XSLFSlide> slides = ppt.getSlides();
            int total = slides.size();
            // Looks need not remove shapes for pptx in current version
            for (int i = total - 1; i >= end; i--) {
                ppt.removeSlide(i);
            }
            for (int i = 0; i < start; i++) {
                ppt.removeSlide(0);
            }
            ppt.write(out);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
        return targetFile != null && targetFile.exists();
    }

}
