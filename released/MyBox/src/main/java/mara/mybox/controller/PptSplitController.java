package mara.mybox.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
    protected ControlSplit splitController;

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

            splitController.setParameters(this);

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(splitController.valid)
                            .or(targetPathController.valid.not())
            );

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            int total;
            try (SlideShow ppt = SlideShowFactory.create(srcFile)) {
                total = ppt.getSlides().size();
            } catch (Exception e) {
                MyBoxLog.error(e);
                return e.toString();
            }
            targetFilesCount = 0;
            targetFiles = new LinkedHashMap<>();
            String suffix = FileNameTools.suffix(srcFile.getName());
            switch (splitController.splitType) {
                case Size:
                    splitByPagesSize(srcFile, targetPath, total, suffix, splitController.size);
                    break;
                case Number:
                    splitByPagesSize(srcFile, targetPath, total, suffix,
                            splitController.size(total, splitController.number));
                    break;
                case List:
                    splitByList(srcFile, targetPath, suffix);
                    break;
                default:
                    break;
            }
            updateInterface("CompleteFile");
            totalItemsHandled += total;
            return MessageFormat.format(Languages.message("HandlePagesGenerateNumber"),
                    totalItemsHandled, targetFilesCount);
        } catch (Exception e) {
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
        }
    }

    protected void splitByList(File srcFile, File targetPath, String suffix) {
        try {
            int start = 0, end, index = 0;
            boolean pptx = "pptx".equalsIgnoreCase(suffix);
            List<Integer> list = splitController.list;
            for (int i = 0; i < list.size();) {
                if (task == null || task.isCancelled()) {
                    return;
                }
                // To user, both start and end are 1-based. To codes, both start and end are 0-based.
                // To user, both start and end are included. To codes, start is included while end is excluded.
                start = list.get(i++) - 1;
                end = list.get(i++);
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
            MyBoxLog.error(e);
        }
    }

    public File makeTargetFile(File srcFile, int index, String ext, File targetPath) {
        try {
            String filePrefix = FileNameTools.prefix(srcFile.getName());
            String splitPrefix = filePrefix + "_" + index;
            String splitSuffix = (ext.startsWith(".") ? "" : ".") + ext;

            File slidePath = targetPath;
            if (targetSubdirCheck.isSelected()) {
                slidePath = new File(targetPath, filePrefix);
            }
            return makeTargetFile(splitPrefix, splitSuffix, slidePath);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    // Include start, and exlucde end. Both start and end are 0-based
    protected boolean savePPT(File srcFile, File targetFile, int start, int end) {
        try (HSLFSlideShow ppt = new HSLFSlideShow(new FileInputStream(srcFile))) {
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
            MyBoxLog.error(e);
            return false;
        }
    }

    // Include start, and exlucde end. Both start and end are 0-based
    protected boolean savePPTX(File srcFile, File targetFile, int start, int end) {
        try (XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(srcFile));
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
            MyBoxLog.error(e);
            return false;
        }
        return targetFile != null && targetFile.exists();
    }

}
