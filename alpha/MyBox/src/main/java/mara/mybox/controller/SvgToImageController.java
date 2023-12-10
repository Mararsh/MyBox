package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.ImageConvertTools;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.SvgTools;
import mara.mybox.tools.XmlTools;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Document;

/**
 * @Author Mara
 * @CreateDate 2023-6-17
 * @License Apache License Version 2.0
 */
public class SvgToImageController extends BaseBatchFileController {

    protected ImageAttributes attributes;

    @FXML
    protected ControlXmlOptions xmlOptionsController;
    @FXML
    protected ControlSvgTranscode svgOptionsController;
    @FXML
    protected ControlImageFormat formatController;

    public SvgToImageController() {
        baseTitle = message("SvgToImage");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.SVG, VisitHistory.FileType.Image);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            formatController.setParameters(this, false);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean makeActualParameters() {
        attributes = formatController.attributes;
        svgOptionsController.checkInputs();
        return super.makeActualParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        File target = makeTargetFile(srcFile, targetPath);
        if (target == null) {
            return message("Skip");
        }
        Document doc = XmlTools.fileToDoc(task, this, srcFile);
        if (doc == null) {
            if (task != null && !task.isWorking()) {
                return message("Failed");
            } else {
                return message("Canceled");
            }
        }
        svgOptionsController.checkValues(doc);
        File tmpFile = SvgTools.fileToImage(task, this, srcFile,
                svgOptionsController.width,
                svgOptionsController.height,
                svgOptionsController.area);
        if (tmpFile == null || !tmpFile.exists()) {
            if (task != null && !task.isWorking()) {
                return message("Failed");
            } else {
                return message("Canceled");
            }
        }
        if (ImageConvertTools.convertColorSpace(task, tmpFile, attributes, target)) {
            targetFileGenerated(target);
            return message("Successful");
        } else {
            return message("Failed");
        }
    }

    @Override
    public File makeTargetFile(File srcFile, File targetPath) {
        try {
            String namePrefix = FileNameTools.prefix(srcFile.getName());
            String nameSuffix = "";
            if (srcFile.isFile()) {
                nameSuffix = "." + attributes.getImageFormat();
            }
            return makeTargetFile(namePrefix, nameSuffix, targetPath);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
