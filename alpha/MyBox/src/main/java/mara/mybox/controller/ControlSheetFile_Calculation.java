package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-28
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetFile_Calculation extends ControlSheetFile_Operations {

    protected abstract File fileStatistic(List<Integer> calCols, List<Integer> disCols, boolean percentage);

    @Override
    public void statistic(List<Integer> calCols, List<Integer> disCols, boolean mode, boolean median, boolean percentage) {
        if (calCols == null || calCols.isEmpty() || sheetInputs == null || columns == null) {
            popError(message("NoData"));
            return;
        }
        if (sourceFile == null || pagesNumber <= 1) {
            statistic(rowsIndex(true), calCols, disCols, mode, median, percentage);
            return;
        }
        synchronized (this) {
            SingletonTask calTask = new SingletonTask<Void>(this) {
                private String[][] resultData = null;
                private File file;

                @Override
                protected boolean handle() {
                    // mode and mdian involve all data and can not handle row by row
                    if (mode || median) {
                        resultData = statistic(allRows(calCols), allRows(disCols), mode, median, percentage);
                        return resultData != null;
                    } else {
                        file = fileStatistic(calCols, disCols, percentage);
                        return file != null && file.exists();
                    }

                }

                @Override
                protected void whenSucceeded() {
                    if (resultData != null) {
                        DataFileCSVController controller = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
//                        controller.loadData(resultData, statisticColumns(calCols, disCols));
                        controller.toFront();
                    } else if (file != null) {
                        DataFileCSVController controller = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
                        controller.setFile(file, Charset.forName("UTF-8"), true, ',');
                        controller.toFront();
                    }

                }

            };
            start(calTask, false);
        }
    }

}
