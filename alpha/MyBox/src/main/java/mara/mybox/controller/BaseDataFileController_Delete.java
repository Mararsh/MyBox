package mara.mybox.controller;

import java.io.File;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-23
 * @License Apache License Version 2.0
 */
public abstract class BaseDataFileController_Delete extends BaseDataFileController_Equal {

    protected abstract File deleteFileSelectedColsDo();

    // columns have been changed before call this
    @Override
    protected void deleteSelectedCols() {
        if (sourceFile == null || pagesNumber <= 1) {
            super.deleteSelectedCols();
            return;
        }
        if (!checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            if (!PopTools.askSure(baseTitle, message("DeleteSelectedCols") + "\n"
                    + message("NoticeAllChangeUnrecover"))) {
                return;
            }
            task = new BaseController_Attributes.SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    saveColumns();
                    File tmpFile = deleteFileSelectedColsDo();
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    backup();
                    return FileTools.rename(tmpFile, sourceFile);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    dataChanged = false;
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
