package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public abstract class BaseBatchFFmpegController extends BaseBatchFileController {

    @FXML
    protected ControlFFmpegOptions ffmpegOptionsController;

    public BaseBatchFFmpegController() {
        baseTitle = Languages.message("MediaInformation");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Media);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            ffmpegOptionsController.setParameters(this);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
