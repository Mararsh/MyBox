package mara.mybox.controller;

import java.io.File;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-23
 * @License Apache License Version 2.0
 */
public abstract class BaseDataFileController_Size extends BaseDataFileController_Copy {

    @Override
    protected void addColsNumber() {
        if (sourceFile == null || pagesNumber <= 1) {
            super.addColsNumber();
            return;
        }
        if (!checkBeforeNextAction()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String value = askValue(message("NoticeAllChangeUnrecover"), message("AddColsNumber"), "1");
            int number;
            try {
                number = Integer.parseInt(value);
            } catch (Exception e) {
                popError(message("InvalidData"));
                return;
            }
            task = new SingletonTask<Void>() {
                StringBuilder s;

                @Override
                protected boolean handle() {
                    int col = colsCheck == null ? 0 : colsCheck.length;
                    makeColumns(col, number);
                    saveColumns();
                    File tmpFile = insertFileColDo(col, true, number);
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
