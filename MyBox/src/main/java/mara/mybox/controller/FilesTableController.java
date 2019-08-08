package mara.mybox.controller;

import java.io.File;
import mara.mybox.controller.base.*;
import mara.mybox.data.FileInformation;

/**
 * @Author Mara
 * @CreateDate 2018-11-28
 * @Description
 * @License Apache License Version 2.0
 */

/*
    T must be subClass of FileInformation
 */
public class FilesTableController extends TableController<FileInformation> {

    public FilesTableController() {
    }

    @Override
    protected FileInformation create(File file) {
        return new FileInformation(file);
    }

}
