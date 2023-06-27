package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.SvgTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-6-24
 * @License Apache License Version 2.0
 */
public class SvgTranscodeOptionsController extends BaseChildController {

    protected SvgEditorController editor;
    protected String currentXML, target;

    @FXML
    protected ControlSvgTranscode optionsController;

    public void setParameters(SvgEditorController editor, String target) {
        try {
            if (!"image".equals(target) && !"pdf".equals(target)) {
                this.close();
                return;
            }
            currentXML = editor.currentXML;
            if (currentXML == null || currentXML.isBlank()) {
                editor.popError(message("NoData"));
                this.close();
                return;
            }

            this.editor = editor;
            this.target = target;

            optionsController.setSVG(editor.treeController.svg);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());

        }
    }

    @FXML
    @Override
    public void okAction() {
        optionsController.pickValues();
        if ("image".equals(target)) {
            File tmpFile = SvgTools.textToImageFile(this, currentXML,
                    optionsController.width,
                    optionsController.height,
                    optionsController.area);
            if (tmpFile != null && tmpFile.exists()) {
                if (tmpFile.length() > 0) {
                    ImageViewerController.openFile(tmpFile);
                } else {
                    FileDeleteTools.delete(tmpFile);
                }
            }
        } else if ("pdf".equals(target)) {
            File tmpFile = SvgTools.textToPDF(this, currentXML,
                    optionsController.width,
                    optionsController.height,
                    optionsController.area);
            if (tmpFile != null && tmpFile.exists()) {
                if (tmpFile.length() > 0) {
                    PdfViewController.open(tmpFile);
                } else {
                    FileDeleteTools.delete(tmpFile);
                }
            }
        }
        close();
    }

    public static SvgTranscodeOptionsController open(SvgEditorController editor, String target) {
        try {
            SvgTranscodeOptionsController controller = (SvgTranscodeOptionsController) WindowTools.openChildStage(
                    editor.getMyWindow(), Fxmls.SvgTranscodeOptionsFxml, true);
            controller.setParameters(editor, target);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
