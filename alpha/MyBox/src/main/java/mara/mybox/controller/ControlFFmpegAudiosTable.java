package mara.mybox.controller;

import mara.mybox.db.data.VisitHistory;

/**
 * @Author Mara
 * @CreateDate 2019-12-8
 * @Description
 * @License Apache License Version 2.0
 */
public class ControlFFmpegAudiosTable extends FFmpegMediasTableController {

    public ControlFFmpegAudiosTable() {
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Audio);
    }

}
