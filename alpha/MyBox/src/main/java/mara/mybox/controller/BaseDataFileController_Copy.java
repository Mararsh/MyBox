package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.table.ColumnDefinition;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-23
 * @License Apache License Version 2.0
 */
public abstract class BaseDataFileController_Copy extends BaseDataFileController_Delete {

    @Override
    public void copyFileSelectedCols() {
        if (colsCheck == null) {
            popError(message("NoData"));
            return;
        }
        if (sourceFile == null || pagesNumber <= 1) {
            copyPageSelectedColsToDataClipboard();
            return;
        }
        List<ColumnDefinition> selectedColumns = new ArrayList<>();
        for (int c = 0; c < colsCheck.length; c++) {
            if (colsCheck[c].isSelected()) {
                selectedColumns.add(columns.get(c));
            }
        }
        if (selectedColumns.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private File file;

                @Override
                protected boolean handle() {
                    file = fileSelectedCols();
                    return file != null;
                }

                @Override
                protected void whenSucceeded() {
                    DataFileCSVController.open(file, true, ',');
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
