package mara.mybox.controller;

import java.io.File;
import mara.mybox.data.FileInformation;

/**
 * @Author Mara
 * @CreateDate 2018-11-28
 * @License Apache License Version 2.0
 */
public class ControlFilesTable extends BaseBatchTableController<FileInformation> {

    @Override
    protected FileInformation create(File file) {
        return new FileInformation(file);
    }

}
