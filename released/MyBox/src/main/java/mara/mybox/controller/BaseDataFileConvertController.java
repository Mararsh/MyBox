package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.operate.Data2DExport;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SoundTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-05
 * @License Apache License Version 2.0
 */
public abstract class BaseDataFileConvertController extends BaseBatchFileController {

    protected Data2DExport export;
    protected boolean skip;

    @FXML
    protected VBox convertVBox;
    @FXML
    protected ControlDataExport convertController;

    public BaseDataFileConvertController() {
        baseTitle = Languages.message("dataConvert");
    }

    @Override
    public void initOptionsSection() {
        try {
            super.initOptionsSection();
            convertController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean makeMoreParameters() {
        export = convertController.pickParameters(null);
        if (export == null) {
            return false;
        }
        export.setController(this);
        skip = targetPathController.isSkip();
        return super.makeMoreParameters();
    }

    public String filePrefix(File srcFile) {
        if (srcFile == null) {
            return null;
        }
        return FileNameTools.prefix(srcFile.getName());
    }

    @Override
    public void disableControls(boolean disable) {
        super.disableControls(disable);
        convertVBox.setDisable(disable);
    }

    @Override
    public void afterTask(boolean ok) {
        List<File> files = export.getPrintedFiles();
        targetFilesCount = files != null ? files.size() : 0;
        showCost();
        tableView.refresh();
        if (miaoCheck != null && miaoCheck.isSelected()) {
            SoundTools.miao3();
        }
        if (!isPreview && openCheck != null && !openCheck.isSelected()) {
            return;
        }
        if (targetFilesCount > 0) {
            File path = files.get(0).getParentFile();
            browseURI(path.toURI());
            recordFileOpened(path);
        } else {
            popInformation(message("NoFileGenerated"));
        }
    }

    @FXML
    @Override
    public void openTarget() {
    }

}
