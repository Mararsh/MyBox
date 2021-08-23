package mara.mybox.controller;

import java.io.File;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-23
 * @License Apache License Version 2.0
 */
public abstract class BaseDataFileController_Equal extends BaseDataFileController_ColMenu {

    protected abstract File setFileSelectedColsDo(String value);

    @Override
    protected void setFileSelectedCols() {
        if (sourceFile == null || pagesNumber <= 1) {
            setPageSelectedCols();
            return;
        }
        if (!checkBeforeNextAction() || totalSize <= 0) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String value = askValue(message("NoticeAllChangeUnrecover"), message("SetFileSelectedColsValues"), defaultColValue);
            if (value == null) {
                return;
            }
            task = new SingletonTask<Void>() {
                StringBuilder s;

                @Override
                protected boolean handle() {
                    File tmpFile = setFileSelectedColsDo(value);
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    backup();
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    loadPage(currentPage);
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

}
