package mara.mybox.controller;

import java.io.File;
import java.util.Iterator;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.image.file.ImageJpeg2000File;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfExtractImagesBatchController extends PdfBatchController {

    public PdfExtractImagesBatchController() {
        baseTitle = AppVariables.message("PdfExtractImagesBatch");
        browseTargets = true;
    }

    @Override
    public int handleCurrentPage() {
        int index = 0;
        try {
            PDPage page = doc.getPage(currentParameters.currentPage - 1);  // 0-based
            PDResources pdResources = page.getResources();
            Iterable<COSName> iterable = pdResources.getXObjectNames();
            if (iterable != null) {
                Iterator<COSName> pageIterator = iterable.iterator();
                while (pageIterator.hasNext()) {
                    if (task.isCancelled()) {
                        break;
                    }
                    COSName cosName = pageIterator.next();
                    if (!pdResources.isImageXObject(cosName)) {
                        continue;
                    }
                    PDImageXObject pdxObject = (PDImageXObject) pdResources.getXObject(cosName);
                    String namePrefix = FileTools.getFilePrefix(currentParameters.currentSourceFile.getName())
                            + "_page" + currentParameters.currentPage + "_index" + index;
                    String suffix = pdxObject.getSuffix();
                    if (ImageJpeg2000File.isJpeg2000(suffix)) {
                        suffix = "jpg";
                    }
                    File tFile = makeTargetFile(namePrefix, "." + suffix, currentParameters.currentTargetPath);
                    ImageFileWriters.writeImageFile(pdxObject.getImage(), suffix, tFile.getAbsolutePath());
                    actualParameters.finalTargetName = tFile.getAbsolutePath();
                    targetFiles.add(tFile);
                    if (isPreview) {
                        break;
                    }
                    index++;
                }
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
        return index;
    }

}
