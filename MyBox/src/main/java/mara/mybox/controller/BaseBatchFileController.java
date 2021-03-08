package mara.mybox.controller;

import java.io.File;
import mara.mybox.data.FileInformation;

/**
 * @Author Mara
 * @CreateDate 2019-4-28
 * @License Apache License Version 2.0
 */
public class BaseBatchFileController extends BaseBatchController<FileInformation> {

    public void startFile(File file) {
        isSettingValues = true;
        tableData.clear();
        tableData.add(new FileInformation(file));
        tableView.refresh();
        isSettingValues = false;
        startAction();
    }

}
