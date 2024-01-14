package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.SvgTools;
import mara.mybox.tools.XmlTools;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Document;

/**
 * @Author Mara
 * @CreateDate 2023-6-17
 * @License Apache License Version 2.0
 */
public class SvgToPDFController extends BaseBatchFileController {

    @FXML
    protected XmlOptionsController xmlOptionsController;
    @FXML
    protected ControlSvgTranscode svgOptionsController;

    public SvgToPDFController() {
        baseTitle = message("SvgToPDF");
        targetFileSuffix = "pdf";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.SVG, VisitHistory.FileType.PDF);
    }

    @Override
    public boolean makeActualParameters() {
        svgOptionsController.checkInputs();
        return super.makeActualParameters();
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        File target = makeTargetFile(srcFile, targetPath);
        if (target == null) {
            return message("Skip");
        }
        Document doc = XmlTools.fileToDoc(currentTask, this, srcFile);
        if (currentTask == null || !currentTask.isWorking()) {
            return message("Canceled");
        }
        if (doc == null) {
            return message("Failed");
        }
        svgOptionsController.checkValues(doc);
        File tmpFile = SvgTools.fileToPDF(currentTask, this, srcFile,
                svgOptionsController.width,
                svgOptionsController.height,
                svgOptionsController.area);
        if (currentTask == null || !currentTask.isWorking()) {
            return message("Canceled");
        }
        if (tmpFile == null || !tmpFile.exists()) {
            return message("Failed");
        }
        if (FileTools.override(tmpFile, target, true)) {
            targetFileGenerated(target);
            return message("Successful");
        } else {
            return message("Failed");
        }
    }

}
