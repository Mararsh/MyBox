package mara.mybox.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import mara.mybox.db.data.VisitHistory;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

/**
 * @Author Mara
 * @CreateDate 2021-5-19
 * @License Apache License Version 2.0
 */
public class PptxMergeController extends FilesMergeController {

    protected XMLSlideShow targetPPT;

    public PptxMergeController() {
        baseTitle = Languages.message("PptxMerge");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.PPTX, VisitHistory.FileType.PPTX);
    }

    @Override
    protected boolean openWriter() {
        try {
            targetPPT = new XMLSlideShow();
            return true;
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
            return false;
        }
    }

    @Override
    public String handleFile(File srcFile) {
        if (!match(srcFile)) {
            return Languages.message("Skip") + ": " + srcFile;
        }
        try ( XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(srcFile))) {
            for (XSLFSlide srcSlide : ppt.getSlides()) {
                if (task == null || task.isCancelled()) {
                    return message("Cancelled");
                }
                try {
                    XSLFSlide targetSlide = targetPPT.createSlide();
                    targetSlide.importContent(srcSlide);
                } catch (Exception e) {
                    updateLogs(e.toString(), true, true);
                }
            }
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
            return srcFile + " " + e.toString();
        }
        return Languages.message("Handled") + ": " + srcFile;
    }

    @Override
    protected boolean closeWriter() {
        try ( BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile))) {
            targetPPT.write(out);
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
            return false;
        }
        return true;
    }

}
