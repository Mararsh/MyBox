package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.SvgTools;
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
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }
            BufferedImage image = ImageFileReaders.readImage(srcFile);
            if (image == null) {
                return message("InvalidData");
            }
            File svgFile = SvgTools.imageToSvgFile(this, srcFile,
                    optionsController.myboxRadio.isSelected() ? optionsController.quantizationController : null,
                    optionsController.options);
            if (svgFile.exists() && FileTools.rename(svgFile, target, true)) {
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
