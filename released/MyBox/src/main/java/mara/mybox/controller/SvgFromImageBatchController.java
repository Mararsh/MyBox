package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.SvgTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-7-26
 * @License Apache License Version 2.0
 */
public class SvgFromImageBatchController extends BaseBatchFileController {

    protected ImageAttributes attributes;

    @FXML
    protected ControlSvgFromImage optionsController;

    public SvgFromImageBatchController() {
        baseTitle = message("ImageToSvg");
        targetFileSuffix = "svg";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image, VisitHistory.FileType.SVG);
    }

    @Override
    public boolean makeActualParameters() {
        return super.makeActualParameters() && optionsController.pickValues();
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }
            BufferedImage image = ImageFileReaders.readImage(currentTask, srcFile);
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Canceled");
            }
            if (image == null) {
                return message("InvalidData");
            }
            String svg = SvgTools.imagefileToSvg(currentTask, this, srcFile, optionsController);
            if (svg == null || svg.isBlank()) {
                return message("Failed");
            }
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Canceled");
            }
            target = TextFileTools.writeFile(target, svg, Charset.forName("utf-8"));
            if (target != null && target.exists()) {
                targetFileGenerated(target);
                return message("Successful");
            } else {
                return message("Failed");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
